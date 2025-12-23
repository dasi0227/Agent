<script setup>
import { computed } from 'vue';
import logoImg from '../assets/logo.jpg';
import { useChatStore } from '../router/pinia';

const chatStore = useChatStore();

const chats = computed(() => chatStore.chats);
const currentChatId = computed(() => chatStore.currentChatId);

const handleNewChat = () => {
    chatStore.createChat();
};

const handleSelectChat = (chatId) => {
    if (chatId !== currentChatId.value) {
        chatStore.switchChat(chatId);
    }
};

const formatDate = (timestamp) => {
    if (!timestamp) {
        return '';
    }
    return new Date(timestamp).toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
};
</script>

<template>
    <aside class="sidebar">
        <div class="sidebar-top">
            <div class="brand">
                <div class="logo">
                    <img :src="logoImg" alt="Logo" />
                </div>
                <div>
                    <div class="brand-name">Dasi Chat</div>
                    <div class="brand-desc">轻量 · 智能 · 对话</div>
                </div>
            </div>
        </div>

        <div class="sidebar-list">
            <button class="btn btn-ghost full" type="button" @click="handleNewChat">
                ＋ 新建会话
            </button>
            <div
                v-for="chat in chats"
                :key="chat.id"
                :class="['chat-item', { active: chat.id === currentChatId }]"
                @click="handleSelectChat(chat.id)"
            >
                <div class="chat-title">{{ chat.title || '未命名会话' }}</div>
                <div class="chat-meta">{{ formatDate(chat.createdAt) }}</div>
            </div>
            <div v-if="chats.length === 0" class="empty">
                暂无会话，点击上方按钮开始
            </div>
        </div>

        <div class="sidebar-user">
            <div class="avatar">U</div>
            <div class="user-info">
                <div class="user-name">访客</div>
                <div class="user-status">在线</div>
            </div>
        </div>
    </aside>
</template>

<style scoped>
.sidebar {
    display: flex;
    flex-direction: column;
    height: 100vh;
    padding: 20px;
    background: radial-gradient(120% 120% at 0% 0%, #122544 0%, #0f172a 60%, #0b1220 100%);
    color: #e7ecf4;
    border-right: 1px solid rgba(255, 255, 255, 0.06);
    box-shadow: 10px 0 30px rgba(0, 0, 0, 0.08);
}

.sidebar-top {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
}

.brand {
    display: flex;
    align-items: center;
    gap: 12px;
}

.logo {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 10px 30px rgba(83, 197, 255, 0.35);
    background: radial-gradient(120% 120% at 0% 0%, rgba(111, 125, 255, 0.2), rgba(83, 197, 255, 0.1));
}

.logo img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.brand-name {
    font-size: 16px;
    font-weight: 700;
}

.brand-desc {
    font-size: 12px;
    color: rgba(231, 236, 244, 0.7);
}

.sidebar-list {
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;
    margin: 8px 0 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.chat-item {
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 12px;
    padding: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.chat-item:hover {
    border-color: rgba(111, 125, 255, 0.8);
    background: rgba(255, 255, 255, 0.07);
}

.chat-item.active {
    border-color: #7bc8ff;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.12);
    background: linear-gradient(135deg, rgba(111, 125, 255, 0.25), rgba(83, 197, 255, 0.1));
}

.chat-title {
    font-weight: 600;
    margin-bottom: 4px;
}

.chat-meta {
    font-size: 12px;
    color: rgba(231, 236, 244, 0.7);
}

.empty {
    margin-top: 12px;
    color: rgba(231, 236, 244, 0.7);
    font-size: 13px;
}

.sidebar-user {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.08);
}

.avatar {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    background: linear-gradient(135deg, #fef08a, #f59e0b);
    color: #0b1220;
    font-weight: 700;
    display: grid;
    place-items: center;
}

.user-name {
    font-weight: 700;
    color: #fff;
}

.user-status {
    font-size: 12px;
    color: rgba(231, 236, 244, 0.72);
}

.btn {
    border: 1px solid rgba(255, 255, 255, 0.15);
    background: rgba(255, 255, 255, 0.08);
    color: #e7ecf4;
    padding: 10px 14px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-weight: 600;
}

.btn:hover {
    background: rgba(255, 255, 255, 0.16);
}

.btn-ghost {
    background: rgba(255, 255, 255, 0.12);
}

.btn.full {
    width: 100%;
    margin-bottom: 6px;
    justify-content: center;
    display: flex;
}

@media (max-width: 720px) {
    .sidebar {
        display: none;
    }
}
</style>
