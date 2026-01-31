import { defineStore } from 'pinia';

const SETTINGS_KEY = 'chat_settings';
const CHATS_KEY = 'chat_sessions';
const AGENT_SETTINGS_KEY = 'agent_settings';
const AGENT_SESSIONS_KEY = 'agent_sessions';
const AUTH_KEY = 'auth_info';

const defaultSettings = () => ({
    type: 'complete',
    temperature: null,
    presencePenalty: null,
    maxCompletionTokens: null,
    model: '',
    ragTag: '',
    token: ''
});

const loadSettings = () => {
    try {
        const raw = localStorage.getItem(SETTINGS_KEY);
        if (!raw) {
            return defaultSettings();
        }
        return { ...defaultSettings(), ...JSON.parse(raw) };
    } catch (error) {
        console.warn('无法解析本地设置，使用默认值', error);
        return defaultSettings();
    }
};

const loadAuth = () => {
    try {
        const raw = localStorage.getItem(AUTH_KEY);
        if (!raw) {
            return { token: '', user: null };
        }
        const parsed = JSON.parse(raw);
        return {
            token: parsed.token || '',
            user: parsed.user || null
        };
    } catch (error) {
        console.warn('无法解析登录信息，已清空', error);
        return { token: '', user: null };
    }
};

export const getStoredAuth = () => loadAuth();

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: loadAuth().token,
        user: loadAuth().user
    }),
    getters: {
        isLogin(state) {
            return Boolean(state.token);
        },
        isAdmin(state) {
            return state.user?.role === 'admin';
        }
    },
    actions: {
        setAuth({ token, user }) {
            this.token = token || '';
            this.user = user || null;
            this.persist();
        },
        clear() {
            this.token = '';
            this.user = null;
            localStorage.removeItem(AUTH_KEY);
        },
        persist() {
            localStorage.setItem(
                AUTH_KEY,
                JSON.stringify({
                    token: this.token,
                    user: this.user
                })
            );
        }
    }
});

export const useSettingsStore = defineStore('settings', {
    state: () => ({
        ...loadSettings()
    }),
    actions: {
        updateSettings(partial) {
            Object.assign(this, partial);
            this.persist();
        },
        persist() {
            const payload = {
                type: this.type,
                temperature: this.temperature,
                presencePenalty: this.presencePenalty,
                maxCompletionTokens: this.maxCompletionTokens,
                model: this.model,
                ragTag: this.ragTag,
                token: this.token
            };
            localStorage.setItem(SETTINGS_KEY, JSON.stringify(payload));
        },
        reset() {
            Object.assign(this, defaultSettings());
            this.persist();
        }
    }
});

const defaultAgentSettings = () => ({
    maxRetry: 2,
    maxRound: 2
});

const loadAgentSettings = () => {
    try {
        const raw = localStorage.getItem(AGENT_SETTINGS_KEY);
        if (!raw) {
            return defaultAgentSettings();
        }
        return { ...defaultAgentSettings(), ...JSON.parse(raw) };
    } catch (error) {
        console.warn('无法解析 Agent 设置，使用默认值', error);
        return defaultAgentSettings();
    }
};

export const useAgentSettingsStore = defineStore('agentSettings', {
    state: () => ({
        ...loadAgentSettings()
    }),
    actions: {
        updateSettings(partial) {
            Object.assign(this, partial);
            this.persist();
        },
        persist() {
            const payload = {
                maxRetry: this.maxRetry,
                maxRound: this.maxRound
            };
            localStorage.setItem(AGENT_SETTINGS_KEY, JSON.stringify(payload));
        },
        reset() {
            Object.assign(this, defaultAgentSettings());
            this.persist();
        }
    }
});

const createChatSessionId = () => `chat_session_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`;

const createEmptyChat = () => ({
    id: `chat_${Date.now()}`,
    title: '新会话',
    createdAt: Date.now(),
    sessionId: createChatSessionId(),
    messages: []
});

const loadChatState = () => {
    try {
        const raw = localStorage.getItem(CHATS_KEY);
        if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed.chats)) {
                const chats = parsed.chats.map((chat) => ({
                    ...chat,
                    sessionId: chat.sessionId || createChatSessionId()
                }));
                return {
                    chats,
                    currentChatId: chats.length > 0 ? parsed.currentChatId || chats[0]?.id || null : null
                };
            }
            if (Array.isArray(parsed)) {
                const chats = parsed.map((chat) => ({
                    ...chat,
                    sessionId: chat.sessionId || createChatSessionId()
                }));
                return {
                    chats,
                    currentChatId: chats.length > 0 ? chats[0]?.id || null : null
                };
            }
            return {
                chats: [],
                currentChatId: null
            };
        }
    } catch (error) {
        console.warn('无法解析本地会话记录，使用新会话', error);
    }
    const chat = createEmptyChat();
    return {
        chats: [chat],
        currentChatId: chat.id
    };
};

const persistChatState = (chats, currentChatId) => {
    localStorage.setItem(
        CHATS_KEY,
        JSON.stringify({
            chats,
            currentChatId
        })
    );
};

const initialChatState = loadChatState();

export const useChatStore = defineStore('chat', {
    state: () => ({
        chats: initialChatState.chats,
        currentChatId: initialChatState.currentChatId,
        sending: false,
        abortController: null
    }),
    getters: {
        currentChat(state) {
            const chat = state.chats.find((item) => item.id === state.currentChatId);
            return chat || state.chats[0] || null;
        },
        currentMessages(state) {
            return this.currentChat?.messages || [];
        }
    },
    actions: {
        ensureChat() {
            if (!this.currentChatId) {
                const chat = createEmptyChat();
                this.chats.unshift(chat);
                this.currentChatId = chat.id;
                persistChatState(this.chats, this.currentChatId);
            }
            if (this.currentChat && !this.currentChat.sessionId) {
                this.currentChat.sessionId = createChatSessionId();
                persistChatState(this.chats, this.currentChatId);
            }
            return this.currentChat;
        },
        createChat() {
            const chat = createEmptyChat();
            this.chats.unshift(chat);
            this.currentChatId = chat.id;
            persistChatState(this.chats, this.currentChatId);
            return chat;
        },
        switchChat(chatId) {
            this.currentChatId = chatId;
            persistChatState(this.chats, this.currentChatId);
        },
        deleteChat(chatId) {
            const targetId = chatId || this.currentChatId;
            const idx = this.chats.findIndex((item) => item.id === targetId);
            if (idx === -1) {
                return;
            }
            this.chats.splice(idx, 1);

            if (this.chats.length > 0) {
                // Switch to the most recently created (assumed first because we unshift on create)
                this.currentChatId = this.chats[0].id;
            } else {
                this.currentChatId = null;
            }
            persistChatState(this.chats, this.currentChatId);
        },
        renameChat(chatId, newTitle) {
            const chat = this.chats.find((item) => item.id === chatId);
            if (chat) {
                chat.title = newTitle || '未命名会话';
                persistChatState(this.chats, this.currentChatId);
            }
        },
        addUserMessage(content) {
            const chat = this.ensureChat();
            const message = {
                id: `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'user',
                content,
                think: '',
                pending: false,
                error: null,
                createdAt: Date.now()
            };
            chat.messages.push(message);
            if (!chat.title || chat.title === '新会话') {
                chat.title = content.slice(0, 20) || '新会话';
            }
            persistChatState(this.chats, this.currentChatId);
            return message;
        },
        addAssistantMessage(payload) {
            const chat = this.ensureChat();
            const message = {
                id: payload.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'assistant',
                content: payload.content || '',
                think: payload.think || '',
                pending: Boolean(payload.pending),
                error: payload.error || null,
                createdAt: Date.now()
            };
            chat.messages.push(message);
            persistChatState(this.chats, this.currentChatId);
            return message;
        },
        updateAssistantMessage(messageId, partial) {
            const chat = this.ensureChat();
            const target = chat.messages.find((item) => item.id === messageId);
            if (target) {
                Object.assign(target, partial);
                persistChatState(this.chats, this.currentChatId);
            }
            return target;
        },
        setSending(flag) {
            this.sending = flag;
        },
        setAbortController(controller) {
            this.abortController = controller;
        },
        stopCurrentRequest() {
            if (this.abortController) {
                this.abortController.abort();
                this.abortController = null;
            }
            this.sending = false;
        }
    }
});

const createAgentSessionId = () => `agent_session_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`;

const createEmptyAgentSession = () => ({
    id: `agent_${Date.now()}`,
    title: '新会话',
    createdAt: Date.now(),
    sessionId: createAgentSessionId(),
    messages: [],
    cards: []
});

const loadAgentState = () => {
    try {
        const raw = localStorage.getItem(AGENT_SESSIONS_KEY);
        if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed.sessions)) {
                if (parsed.sessions.length === 0) {
                    return {
                        sessions: [],
                        currentSessionId: null
                    };
                }
                return {
                    sessions: parsed.sessions,
                    currentSessionId: parsed.currentSessionId || parsed.sessions[0]?.id || null
                };
            }
            if (Array.isArray(parsed)) {
                if (parsed.length === 0) {
                    return {
                        sessions: [],
                        currentSessionId: null
                    };
                }
                return {
                    sessions: parsed,
                    currentSessionId: parsed[0]?.id || null
                };
            }
            return {
                sessions: [],
                currentSessionId: null
            };
        }
    } catch (error) {
        console.warn('无法解析 Agent 会话记录，使用新会话', error);
    }
    const session = createEmptyAgentSession();
    return {
        sessions: [session],
        currentSessionId: session.id
    };
};

const persistAgentState = (sessions, currentSessionId) => {
    localStorage.setItem(
        AGENT_SESSIONS_KEY,
        JSON.stringify({
            sessions,
            currentSessionId
        })
    );
};

const initialAgentState = loadAgentState();

export const useAgentStore = defineStore('agent', {
    state: () => ({
        sessions: initialAgentState.sessions,
        currentSessionId: initialAgentState.currentSessionId,
        sending: false,
        abortController: null
    }),
    getters: {
        currentSession(state) {
            const session = state.sessions.find((item) => item.id === state.currentSessionId);
            return session || state.sessions[0] || null;
        },
        currentMessages() {
            return this.currentSession?.messages || [];
        },
        currentCards() {
            return this.currentSession?.cards || [];
        }
    },
    actions: {
        ensureSession() {
            if (!this.currentSessionId) {
                const session = createEmptyAgentSession();
                this.sessions.unshift(session);
                this.currentSessionId = session.id;
                persistAgentState(this.sessions, this.currentSessionId);
            }
            return this.currentSession;
        },
        createSession() {
            const session = createEmptyAgentSession();
            this.sessions.unshift(session);
            this.currentSessionId = session.id;
            persistAgentState(this.sessions, this.currentSessionId);
            return session;
        },
        switchSession(sessionId) {
            this.currentSessionId = sessionId;
            persistAgentState(this.sessions, this.currentSessionId);
        },
        deleteSession(sessionId) {
            const targetId = sessionId || this.currentSessionId;
            const idx = this.sessions.findIndex((item) => item.id === targetId);
            if (idx === -1) {
                return;
            }
            this.sessions.splice(idx, 1);

            if (this.sessions.length > 0) {
                this.currentSessionId = this.sessions[0].id;
            } else {
                this.currentSessionId = null;
            }
            persistAgentState(this.sessions, this.currentSessionId);
        },
        renameSession(sessionId, newTitle) {
            const session = this.sessions.find((item) => item.id === sessionId);
            if (session) {
                session.title = newTitle || '未命名会话';
                persistAgentState(this.sessions, this.currentSessionId);
            }
        },
        addUserMessage(content) {
            const session = this.ensureSession();
            const message = {
                id: `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'user',
                content,
                pending: false,
                error: null,
                createdAt: Date.now()
            };
            session.messages.push(message);
            if (!session.title || session.title === '新会话') {
                session.title = content.slice(0, 20) || '新会话';
            }
            persistAgentState(this.sessions, this.currentSessionId);
            return message;
        },
        addAssistantMessage(payload) {
            const session = this.ensureSession();
            const message = {
                id: payload.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'assistant',
                content: payload.content || '',
                pending: Boolean(payload.pending),
                error: payload.error || null,
                createdAt: Date.now()
            };
            session.messages.push(message);
            persistAgentState(this.sessions, this.currentSessionId);
            return message;
        },
        updateAssistantMessage(messageId, partial) {
            const session = this.ensureSession();
            const target = session.messages.find((item) => item.id === messageId);
            if (target) {
                Object.assign(target, partial);
                persistAgentState(this.sessions, this.currentSessionId);
            }
            return target;
        },
        addCard(payload) {
            const session = this.ensureSession();
            const card = {
                id: payload.id || `card_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                clientType: payload.clientType || '',
                sectionType: payload.sectionType || '',
                sectionContent: payload.sectionContent || '',
                round: payload.round ?? null,
                step: payload.step ?? null,
                timestamp: payload.timestamp ?? null
            };
            session.cards.push(card);
            persistAgentState(this.sessions, this.currentSessionId);
            return card;
        },
        setSending(flag) {
            this.sending = flag;
        },
        setAbortController(controller) {
            this.abortController = controller;
        },
        stopCurrentRequest() {
            if (this.abortController) {
                this.abortController.abort();
                this.abortController = null;
            }
            this.sending = false;
        }
    }
});
