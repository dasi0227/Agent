import { defineStore } from 'pinia';

const SETTINGS_KEY = 'chat_settings';
const CHATS_KEY = 'chat_sessions';

const defaultSettings = () => ({
    type: 'complete',
    temperature: 0.7,
    topK: 40,
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
                topK: this.topK,
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

const createEmptyChat = () => ({
    id: `chat_${Date.now()}`,
    title: '新会话',
    createdAt: Date.now(),
    messages: []
});

const loadChatState = () => {
    try {
        const raw = localStorage.getItem(CHATS_KEY);
        if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed.chats)) {
                return {
                    chats: parsed.chats,
                    currentChatId: parsed.currentChatId || parsed.chats[0]?.id || null
                };
            }
            if (Array.isArray(parsed)) {
                return {
                    chats: parsed,
                    currentChatId: parsed[0]?.id || null
                };
            }
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
                const newChat = createEmptyChat();
                this.chats.unshift(newChat);
                this.currentChatId = newChat.id;
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
