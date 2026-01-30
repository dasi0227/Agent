<script setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import logoImg from '../assets/logo.jpg';
import chatIcon from '../assets/chat.svg';
import workIcon from '../assets/work.svg';
import { useAgentStore, useChatStore } from '../router/pinia';

const router = useRouter();
const route = useRoute();

const chatStore = useChatStore();
const agentStore = useAgentStore();

const chats = computed(() => chatStore.chats);
const currentChatId = computed(() => chatStore.currentChatId);
const agentSessions = computed(() => agentStore.sessions);
const currentAgentSessionId = computed(() => agentStore.currentSessionId);
const isAgentRoute = computed(() => route.path.startsWith('/work'));
const isChatRoute = computed(() => route.path.startsWith('/chat'));

const showChatList = ref(true);
const showAgentList = ref(true);

const editingChatId = ref(null);
const editChatTitle = ref('');
const editingAgentId = ref(null);
const editAgentTitle = ref('');

const showDeleteConfirm = ref(false);
const deleteTarget = ref({ type: 'chat', id: '' });
const showNewSessionPicker = ref(false);

const handleNewSession = () => {
    showNewSessionPicker.value = true;
};

const handleSelectChat = (chatId) => {
    if (route.path !== '/chat') {
        router.push('/chat');
    }
    if (chatId !== currentChatId.value) {
        chatStore.switchChat(chatId);
    }
};

const handleSelectAgent = (sessionId) => {
    if (route.path !== '/work') {
        router.push('/work');
    }
    if (sessionId !== currentAgentSessionId.value) {
        agentStore.switchSession(sessionId);
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

const formatTitle = (title) => {
    const raw = title || '未命名会话';
    if (raw.length > 7) {
        return `${raw.slice(0, 7)}..`;
    }
    return raw;
};

const startRenameChat = (chat) => {
    editingChatId.value = chat.id;
    editChatTitle.value = chat.title || '';
};

const saveRenameChat = (chat) => {
    if (!chat || editingChatId.value !== chat.id) {
        return;
    }
    const title = editChatTitle.value.trim() || '未命名会话';
    chatStore.renameChat(chat.id, title);
    editingChatId.value = null;
    editChatTitle.value = '';
};

const cancelRenameChat = () => {
    editingChatId.value = null;
    editChatTitle.value = '';
};

const startRenameAgent = (session) => {
    editingAgentId.value = session.id;
    editAgentTitle.value = session.title || '';
};

const saveRenameAgent = (session) => {
    if (!session || editingAgentId.value !== session.id) {
        return;
    }
    const title = editAgentTitle.value.trim() || '未命名会话';
    agentStore.renameSession(session.id, title);
    editingAgentId.value = null;
    editAgentTitle.value = '';
};

const cancelRenameAgent = () => {
    editingAgentId.value = null;
    editAgentTitle.value = '';
};

const openDeleteConfirm = (type, id) => {
    deleteTarget.value = { type, id };
    showDeleteConfirm.value = true;
};

const handleDelete = () => {
    if (!deleteTarget.value?.id) {
        showDeleteConfirm.value = false;
        return;
    }
    if (deleteTarget.value.type === 'agent') {
        agentStore.deleteSession(deleteTarget.value.id);
    } else {
        chatStore.deleteChat(deleteTarget.value.id);
    }
    showDeleteConfirm.value = false;
    deleteTarget.value = { type: 'chat', id: '' };
    cancelRenameChat();
    cancelRenameAgent();
};

const closeNewSessionPicker = () => {
    showNewSessionPicker.value = false;
};

const confirmNewSession = (type) => {
    if (type === 'work') {
        agentStore.createSession();
        router.push('/work');
    } else if (type === 'chat') {
        chatStore.createChat();
        router.push('/chat');
    }
    closeNewSessionPicker();
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
                    <div class="text-[16px] font-bold">Dasi AI</div>
                    <div class="text-[12px] text-[rgba(231,236,244,0.7)]">RAG · MCP · OPENAI</div>
                </div>
            </div>
        </div>

        <div
            class="mb-[12px] mt-[8px] flex flex-1 flex-col gap-[12px] overflow-y-auto pr-[4px] [scrollbar-gutter:stable_both-edges] [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"
        >
            <button
                class="mb-[6px] flex w-full justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.12)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                type="button"
                @click="handleNewSession"
            >
                ＋ 新建会话
            </button>

            <div class="flex flex-col gap-[8px]">
                <button
                    class="flex w-full items-center px-[4px] py-[6px] text-[20px] font-bold text-[#f8fafc] transition-all duration-200 hover:text-[#7bc8ff]"
                    :class="isChatRoute ? 'text-[#7bc8ff]' : ''"
                    type="button"
                    @click="showChatList = !showChatList"
                >
                    Chat 会话
                </button>
                <div v-if="showChatList" class="flex flex-col gap-[8px]">
                    <div
                        v-for="chat in chats"
                        :key="chat.id"
                        :class="[
                            'w-full rounded-[12px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.04)] px-[12px] py-[10px] transition-all duration-200 hover:border-[rgba(111,125,255,0.8)] hover:bg-[rgba(255,255,255,0.07)]',
                            chat.id === currentChatId && route.path.startsWith('/chat')
                                ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.25),rgba(83,197,255,0.1))] shadow-[0_10px_20px_rgba(0,0,0,0.12)]'
                                : ''
                        ]"
                    >
                        <div class="flex items-center justify-between gap-[10px]" @click="handleSelectChat(chat.id)">
                            <div class="min-w-0 flex flex-col">
                                <template v-if="editingChatId === chat.id">
                                    <input
                                        v-model="editChatTitle"
                                        class="w-full rounded-[8px] border border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.08)] px-[8px] py-[6px] font-semibold text-[#e7ecf4]"
                                        :placeholder="chat.title || '未命名会话'"
                                        @keydown.enter.prevent="saveRenameChat(chat)"
                                        @keydown.esc.prevent="cancelRenameChat"
                                        @blur="saveRenameChat(chat)"
                                        @click.stop
                                    />
                                </template>
                                <template v-else>
                                    <div class="mb-[4px] max-w-[140px] truncate font-semibold" :title="chat.title || '未命名会话'">
                                        {{ formatTitle(chat.title) }}
                                    </div>
                                </template>
                                <div class="text-[12px] text-[rgba(231,236,244,0.7)]">{{ formatDate(chat.createdAt) }}</div>
                            </div>
                            <div class="flex shrink-0 gap-[6px]" @click.stop>
                                <button
                                    class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#7bc8ff] hover:text-[#7bc8ff] hover:bg-[rgba(123,200,255,0.12)]"
                                    type="button"
                                    title="重命名"
                                    @click.stop="startRenameChat(chat)"
                                >
                                    ✎
                                </button>
                                <button
                                    class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#ef4444] hover:text-[#ef4444] hover:bg-[rgba(239,68,68,0.16)]"
                                    type="button"
                                    title="删除"
                                    @click.stop="openDeleteConfirm('chat', chat.id)"
                                >
                                    🗑
                                </button>
                            </div>
                        </div>
                    </div>
                    <div v-if="chats.length === 0" class="mt-[4px] text-[13px] text-[rgba(231,236,244,0.7)]">
                        暂无会话
                    </div>
                </div>
            </div>

            <div class="flex flex-col gap-[8px]">
                <button
                    class="flex w-full items-center px-[4px] py-[6px] text-[20px] font-bold text-[#f8fafc] transition-all duration-200 hover:text-[#7bc8ff]"
                    :class="isAgentRoute ? 'text-[#7bc8ff]' : ''"
                    type="button"
                    @click="showAgentList = !showAgentList"
                >
                    Work 会话
                </button>
                <div v-if="showAgentList" class="flex flex-col gap-[8px]">
                    <div
                        v-for="session in agentSessions"
                        :key="session.id"
                        :class="[
                            'w-full rounded-[12px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.04)] px-[12px] py-[10px] transition-all duration-200 hover:border-[rgba(111,125,255,0.8)] hover:bg-[rgba(255,255,255,0.07)]',
                            session.id === currentAgentSessionId && route.path.startsWith('/work')
                                ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.25),rgba(83,197,255,0.1))] shadow-[0_10px_20px_rgba(0,0,0,0.12)]'
                                : ''
                        ]"
                    >
                        <div class="flex items-center justify-between gap-[10px]" @click="handleSelectAgent(session.id)">
                            <div class="min-w-0 flex flex-col">
                                <template v-if="editingAgentId === session.id">
                                    <input
                                        v-model="editAgentTitle"
                                        class="w-full rounded-[8px] border border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.08)] px-[8px] py-[6px] font-semibold text-[#e7ecf4]"
                                        :placeholder="session.title || '未命名会话'"
                                        @keydown.enter.prevent="saveRenameAgent(session)"
                                        @keydown.esc.prevent="cancelRenameAgent"
                                        @blur="saveRenameAgent(session)"
                                        @click.stop
                                    />
                                </template>
                                <template v-else>
                                    <div class="mb-[4px] max-w-[140px] truncate font-semibold" :title="session.title || '未命名会话'">
                                        {{ formatTitle(session.title) }}
                                    </div>
                                </template>
                                <div class="text-[12px] text-[rgba(231,236,244,0.7)]">{{ formatDate(session.createdAt) }}</div>
                            </div>
                            <div class="flex shrink-0 gap-[6px]" @click.stop>
                                <button
                                    class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#7bc8ff] hover:text-[#7bc8ff] hover:bg-[rgba(123,200,255,0.12)]"
                                    type="button"
                                    title="重命名"
                                    @click.stop="startRenameAgent(session)"
                                >
                                    ✎
                                </button>
                                <button
                                    class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#ef4444] hover:text-[#ef4444] hover:bg-[rgba(239,68,68,0.16)]"
                                    type="button"
                                    title="删除"
                                    @click.stop="openDeleteConfirm('agent', session.id)"
                                >
                                    🗑
                                </button>
                            </div>
                        </div>
                    </div>
                    <div v-if="agentSessions.length === 0" class="mt-[4px] text-[13px] text-[rgba(231,236,244,0.7)]">
                        暂无会话
                    </div>
                </div>
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

        <div
            v-if="showNewSessionPicker"
            class="fixed inset-0 z-[30] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="closeNewSessionPicker"
        >
            <div class="flex flex-col items-center gap-[32px] -translate-y-[18px]">
                <div class="flex items-center gap-[32px]">
                    <button
                        class="flex h-[440px] w-[440px] flex-col items-center justify-center gap-[26px] rounded-[28px] border border-[rgba(0,0,0,0.08)] bg-[#f8fafc] text-[#0f172a] shadow-[0_24px_50px_rgba(15,23,42,0.18)] transition-all duration-200 hover:border-[#94a3b8] hover:bg-[#e2e8f0] hover:shadow-[0_30px_60px_rgba(15,23,42,0.22)]"
                        type="button"
                        @click="confirmNewSession('chat')"
                    >
                        <img :src="chatIcon" alt="Chat" class="h-[200px] w-[200px]" />
                        <div class="text-[40px] font-semibold">Chat Client</div>
                    </button>
                    <button
                        class="flex h-[440px] w-[440px] flex-col items-center justify-center gap-[26px] rounded-[28px] border border-[rgba(0,0,0,0.08)] bg-[#f8fafc] text-[#0f172a] shadow-[0_24px_50px_rgba(15,23,42,0.18)] transition-all duration-200 hover:border-[#94a3b8] hover:bg-[#e2e8f0] hover:shadow-[0_30px_60px_rgba(15,23,42,0.22)]"
                        type="button"
                        @click="confirmNewSession('work')"
                    >
                        <img :src="workIcon" alt="Work" class="h-[200px] w-[200px]" />
                        <div class="text-[40px] font-semibold">Work Agent</div>
                    </button>
                </div>
            </div>
        </div>
    </aside>
</template>
