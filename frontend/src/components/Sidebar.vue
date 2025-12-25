<script setup>
import { computed, ref } from 'vue';
import logoImg from '../assets/logo.jpg';
import { useChatStore } from '../router/pinia';

const chatStore = useChatStore();

const chats = computed(() => chatStore.chats);
const currentChatId = computed(() => chatStore.currentChatId);
const editingId = ref(null);
const editTitle = ref('');
const showDeleteConfirm = ref(false);
const targetChatId = ref('');

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

const startRename = (chat) => {
    editingId.value = chat.id;
    editTitle.value = chat.title || '';
};

const saveRename = (chat) => {
    if (!chat || editingId.value !== chat.id) {
        return;
    }
    const title = editTitle.value.trim() || '未命名会话';
    chatStore.renameChat(chat.id, title);
    editingId.value = null;
    editTitle.value = '';
};

const cancelRename = () => {
    editingId.value = null;
    editTitle.value = '';
};

const openDeleteConfirm = (chatId) => {
    targetChatId.value = chatId;
    showDeleteConfirm.value = true;
};

const handleDelete = () => {
    if (targetChatId.value) {
        chatStore.deleteChat(targetChatId.value);
    }
    showDeleteConfirm.value = false;
    targetChatId.value = '';
    cancelRename();
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
            >
                <div class="chat-row" @click="handleSelectChat(chat.id)">
                    <div class="chat-info">
                        <template v-if="editingId === chat.id">
                            <input
                                v-model="editTitle"
                                class="rename-input"
                                :placeholder="chat.title || '未命名会话'"
                                @keydown.enter.prevent="saveRename(chat)"
                                @keydown.esc.prevent="cancelRename"
                                @blur="saveRename(chat)"
                                @click.stop
                            />
                        </template>
                        <template v-else>
                            <div class="chat-title">{{ chat.title || '未命名会话' }}</div>
                        </template>
                        <div class="chat-meta">{{ formatDate(chat.createdAt) }}</div>
                    </div>
                    <div class="chat-actions">
                        <button
                            class="circle-btn"
                            type="button"
                            title="重命名"
                            @click.stop="startRename(chat)"
                        >
                            ✎
                        </button>
                        <button
                            class="circle-btn danger"
                            type="button"
                            title="删除"
                            @click.stop="openDeleteConfirm(chat.id)"
                        >
                            ⌫
                        </button>
                    </div>
                </div>
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

        <div v-if="showDeleteConfirm" class="modal-mask" @click.self="showDeleteConfirm = false">
            <div class="modal small">
                <div class="modal-header">
                    <div class="title">删除会话</div>
                    <button class="close" type="button" @click="showDeleteConfirm = false">×</button>
                </div>
                <div class="modal-body">
                    <div class="confirm-text">确认删除当前会话吗？</div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-ghost" type="button" @click="showDeleteConfirm = false">
                        取消
                    </button>
                    <button class="btn btn-primary danger" type="button" @click="handleDelete">
                        确认删除
                    </button>
                </div>
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
    padding: 10px 12px;
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

.chat-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
}

.chat-info {
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.chat-actions {
    display: flex;
    gap: 6px;
    flex-shrink: 0;
}

.circle-btn {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    border: 1px solid #f59e0b;
    background: transparent;
    color: #f59e0b;
    font-size: 13px;
    cursor: pointer;
    display: grid;
    place-items: center;
    transition: all 0.2s ease;
}

.circle-btn:hover {
    background: rgba(245, 158, 11, 0.1);
}

.circle-btn.danger {
    border-color: #ef4444;
    color: #ef4444;
}

.circle-btn.danger:hover {
    background: rgba(239, 68, 68, 0.12);
}

.rename-input {
    width: 100%;
    padding: 6px 8px;
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    background: rgba(255, 255, 255, 0.08);
    color: #e7ecf4;
    font-weight: 600;
}

.modal-mask {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    display: grid;
    place-items: center;
    padding: 20px;
    z-index: 20;
}

.modal {
    width: min(420px, 100%);
    background: #0f172a;
    border-radius: 14px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #e7ecf4;
}

.modal-header,
.modal-footer {
    padding: 14px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.modal-header .title {
    font-weight: 700;
    font-size: 16px;
}

.modal-body {
    padding: 14px 16px;
}

.confirm-text {
    font-size: 14px;
}

.close {
    border: none;
    background: transparent;
    font-size: 20px;
    color: #e7ecf4;
    cursor: pointer;
}

@media (max-width: 720px) {
    .sidebar {
        display: none;
    }
}
</style>
