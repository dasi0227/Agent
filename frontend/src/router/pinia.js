import { defineStore } from 'pinia';

const SETTINGS_KEY = 'chat_settings';
const AGENT_SETTINGS_KEY = 'agent_settings';
const AUTH_KEY = 'auth_info';

const defaultSettings = () => ({
    type: 'complete',
    temperature: null,
    presencePenalty: null,
    maxCompletionTokens: null,
    model: '',
    ragTag: '',
    token: '',
    theme: 'light'
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
        setToken(token) {
            this.token = token || '';
            this.persist();
        },
        setUser(user) {
            this.user = user || null;
            this.persist();
        },
        logout(redirectPath = '/login') {
            this.clear();
            window.location.href = redirectPath;
        },
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
                token: this.token,
                theme: this.theme
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

export const useChatStore = defineStore('chat', {
    state: () => ({
        chats: [],
        currentChatId: null,
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
        setChats(chats) {
            this.chats = Array.isArray(chats) ? chats : [];
        },
        setCurrentChatId(chatId) {
            this.currentChatId = chatId || null;
        },
        upsertChat(chat) {
            if (!chat) return;
            const idx = this.chats.findIndex((item) => item.id === chat.id);
            if (idx === -1) {
                this.chats.unshift(chat);
            } else {
                this.chats[idx] = { ...this.chats[idx], ...chat };
            }
        },
        updateChatTitle(chatId, newTitle) {
            const chat = this.chats.find((item) => item.id === chatId);
            if (chat) {
                chat.title = newTitle || '未命名会话';
            }
        },
        removeChat(chatId) {
            const idx = this.chats.findIndex((item) => item.id === chatId);
            if (idx === -1) return;
            this.chats.splice(idx, 1);
            if (this.currentChatId === chatId) {
                this.currentChatId = this.chats[0]?.id || null;
            }
        },
        setChatMessages(chatId, messages) {
            const chat = this.chats.find((item) => item.id === chatId);
            if (chat) {
                chat.messages = Array.isArray(messages) ? messages : [];
            }
        },
        addUserMessage(content) {
            const chat = this.currentChat;
            if (!chat) return null;
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
            return message;
        },
        addAssistantMessage(payload) {
            const chat = this.currentChat;
            if (!chat) return null;
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
            return message;
        },
        updateAssistantMessage(messageId, partial) {
            const chat = this.currentChat;
            if (!chat) return null;
            const target = chat.messages.find((item) => item.id === messageId);
            if (target) {
                Object.assign(target, partial);
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

export const useAgentStore = defineStore('agent', {
    state: () => ({
        sessions: [],
        currentSessionId: null,
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
        setSessions(sessions) {
            this.sessions = Array.isArray(sessions) ? sessions : [];
        },
        setCurrentSessionId(sessionId) {
            this.currentSessionId = sessionId || null;
        },
        upsertSession(session) {
            if (!session) return;
            const idx = this.sessions.findIndex((item) => item.id === session.id);
            if (idx === -1) {
                this.sessions.unshift(session);
            } else {
                this.sessions[idx] = { ...this.sessions[idx], ...session };
            }
        },
        updateSessionTitle(sessionId, newTitle) {
            const session = this.sessions.find((item) => item.id === sessionId);
            if (session) {
                session.title = newTitle || '未命名会话';
            }
        },
        removeSession(sessionId) {
            const idx = this.sessions.findIndex((item) => item.id === sessionId);
            if (idx === -1) return;
            this.sessions.splice(idx, 1);
            if (this.currentSessionId === sessionId) {
                this.currentSessionId = this.sessions[0]?.id || null;
            }
        },
        setSessionMessages(sessionId, messages) {
            const session = this.sessions.find((item) => item.id === sessionId);
            if (session) {
                session.messages = Array.isArray(messages) ? messages : [];
            }
        },
        setSessionCards(sessionId, cards) {
            const session = this.sessions.find((item) => item.id === sessionId);
            if (session) {
                session.cards = Array.isArray(cards) ? cards : [];
            }
        },
        addUserMessage(content) {
            const session = this.currentSession;
            if (!session) return null;
            const message = {
                id: `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'user',
                content,
                pending: false,
                error: null,
                createdAt: Date.now()
            };
            session.messages.push(message);
            return message;
        },
        addAssistantMessage(payload) {
            const session = this.currentSession;
            if (!session) return null;
            const message = {
                id: payload.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'assistant',
                content: payload.content || '',
                pending: Boolean(payload.pending),
                error: payload.error || null,
                createdAt: Date.now()
            };
            session.messages.push(message);
            return message;
        },
        updateAssistantMessage(messageId, partial) {
            const session = this.currentSession;
            if (!session) return null;
            const target = session.messages.find((item) => item.id === messageId);
            if (target) {
                Object.assign(target, partial);
            }
            return target;
        },
        addCard(payload) {
            const session = this.currentSession;
            if (!session) return null;
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
