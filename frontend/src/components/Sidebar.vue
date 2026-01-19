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
    <aside
        class="flex h-screen flex-col bg-[radial-gradient(120%_120%_at_0%_0%,#122544_0%,#0f172a_60%,#0b1220_100%)] p-[20px] text-[#e7ecf4] shadow-[10px_0_30px_rgba(0,0,0,0.08)] border-r border-[rgba(255,255,255,0.06)] max-[720px]:hidden"
    >
        <div class="mb-[12px] flex items-center gap-[12px]">
            <div class="flex items-center gap-[12px]">
                <div
                    class="h-[44px] w-[44px] overflow-hidden rounded-[14px] border border-[rgba(255,255,255,0.2)] bg-[radial-gradient(120%_120%_at_0%_0%,rgba(111,125,255,0.2),rgba(83,197,255,0.1))] shadow-[0_10px_30px_rgba(83,197,255,0.35)]"
                >
                    <img :src="logoImg" alt="Logo" class="h-full w-full object-cover block" />
                </div>
                <div>
                    <div class="text-[16px] font-bold">Dasi Chat</div>
                    <div class="text-[12px] text-[rgba(231,236,244,0.7)]">RAG · MCP · AGENT</div>
                </div>
            </div>
        </div>

        <div
            class="mb-[12px] mt-[8px] flex flex-1 flex-col gap-[8px] overflow-y-auto pr-[4px] [scrollbar-gutter:stable_both-edges] [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"
        >
            <button
                class="mb-[6px] flex w-full justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.12)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                type="button"
                @click="handleNewChat"
            >
                ＋ 新建会话
            </button>
            <div
                v-for="chat in chats"
                :key="chat.id"
                :class="[
                    'w-full rounded-[12px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.04)] px-[12px] py-[10px] transition-all duration-200 hover:border-[rgba(111,125,255,0.8)] hover:bg-[rgba(255,255,255,0.07)]',
                    chat.id === currentChatId
                        ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.25),rgba(83,197,255,0.1))] shadow-[0_10px_20px_rgba(0,0,0,0.12)]'
                        : ''
                ]"
            >
                <div class="flex items-center justify-between gap-[10px]" @click="handleSelectChat(chat.id)">
                    <div class="min-w-0 flex flex-col">
                        <template v-if="editingId === chat.id">
                            <input
                                v-model="editTitle"
                                class="w-full rounded-[8px] border border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.08)] px-[8px] py-[6px] font-semibold text-[#e7ecf4]"
                                :placeholder="chat.title || '未命名会话'"
                                @keydown.enter.prevent="saveRename(chat)"
                                @keydown.esc.prevent="cancelRename"
                                @blur="saveRename(chat)"
                                @click.stop
                            />
                        </template>
                        <template v-else>
                            <div class="mb-[4px] font-semibold">{{ chat.title || '未命名会话' }}</div>
                        </template>
                        <div class="text-[12px] text-[rgba(231,236,244,0.7)]">{{ formatDate(chat.createdAt) }}</div>
                    </div>
                    <div class="flex shrink-0 gap-[6px]" @click.stop>
                        <button
                            class="grid h-[30px] w-[30px] place-items-center rounded-full border-[2px] border-[#f59e0b] text-[13px] text-[#f59e0b] transition-all duration-200 hover:bg-[rgba(245,158,11,0.1)]"
                            type="button"
                            title="重命名"
                            @click.stop="startRename(chat)"
                        >
                            ✎
                        </button>
                        <button
                            class="grid h-[30px] w-[30px] place-items-center rounded-full border-[2px] border-[#ef4444] text-[13px] text-[#ef4444] transition-all duration-200 hover:bg-[rgba(239,68,68,0.12)]"
                            type="button"
                            title="删除"
                            @click.stop="openDeleteConfirm(chat.id)"
                        >
                            🗑
                        </button>
                    </div>
                </div>
            </div>
            <div v-if="chats.length === 0" class="mt-[12px] text-[13px] text-[rgba(231,236,244,0.7)]">
                暂无会话，点击上方按钮开始
            </div>
        </div>

        <div
            class="flex items-center gap-[10px] rounded-[14px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.05)] p-[12px]"
        >
            <div
                class="grid h-[40px] w-[40px] place-items-center rounded-[12px] bg-[linear-gradient(135deg,#fef08a,#f59e0b)] font-bold text-[#0b1220]"
            >
                U
            </div>
            <div>
                <div class="font-bold text-white">访客</div>
                <div class="text-[12px] text-[rgba(231,236,244,0.72)]">在线</div>
            </div>
        </div>

        <div v-if="showDeleteConfirm" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="showDeleteConfirm = false">
            <div
                class="w-full max-w-[420px] rounded-[14px] border border-[rgba(255,255,255,0.1)] bg-[#0f172a] text-[#e7ecf4] shadow-[0_20px_50px_rgba(0,0,0,0.2)]"
            >
                <div class="flex items-center justify-between border-b border-[rgba(255,255,255,0.08)] px-[16px] py-[14px]">
                    <div class="text-[16px] font-bold">删除会话</div>
                    <button
                        class="text-[20px] text-[#e7ecf4]"
                        type="button"
                        @click="showDeleteConfirm = false"
                    >
                        ×
                    </button>
                </div>
                <div class="px-[16px] py-[14px]">
                    <div class="text-[14px]">确认删除当前会话吗？</div>
                </div>
                <div class="flex items-center justify-between border-b border-[rgba(255,255,255,0.08)] px-[16px] py-[14px]">
                    <button
                        class="flex items-center justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.08)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                        type="button"
                        @click="showDeleteConfirm = false"
                    >
                        取消
                    </button>
                    <button
                        class="flex items-center justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.08)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                        type="button"
                        @click="handleDelete"
                    >
                        确认删除
                    </button>
                </div>
            </div>
        </div>
    </aside>
</template>
