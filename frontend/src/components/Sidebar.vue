<script setup>
import { computed, reactive, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import logoImg from '../assets/logo.jpg';
import chatIconDark from '../assets/chat-white.svg';
import chatIconLight from '../assets/chat-black.svg';
import workIconDark from '../assets/work-white.svg';
import workIconLight from '../assets/work-black.svg';
import { useAgentStore, useAuthStore, useChatStore, useSettingsStore } from '../router/pinia';
import { deleteSession, insertSession, listSessions, updatePassword, updateSession } from '../request/api';
import { normalizeError } from '../request/request';

const router = useRouter();
const route = useRoute();

const chatStore = useChatStore();
const agentStore = useAgentStore();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

const isLogin = computed(() => authStore.isLogin);
const currentUser = computed(() => authStore.user || { username: '访客', role: 'guest' });
const avatarChar = computed(() => (currentUser.value.username || '访客').slice(0, 1).toUpperCase());
const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const chatIcon = computed(() => (isDarkTheme.value ? chatIconDark : chatIconLight));
const workIcon = computed(() => (isDarkTheme.value ? workIconDark : workIconLight));

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
const showProfile = ref(false);
const profileSaving = ref(false);
const profileError = ref('');
const profileForm = reactive({
    username: currentUser.value.username || '',
    oldPassword: '',
    newPassword: ''
});
const sessionLoading = ref(false);
const sessionError = ref('');
const sessionLimitError = ref('');

const pickData = (resp, message = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || message);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const normalizeSessionType = (value) => (value ? value.toString().toLowerCase() : '');

const mapSession = (session) => {
    if (!session) return null;
    return {
        id: session.id,
        sessionId: session.sessionId,
        sessionUser: session.sessionUser,
        title: session.sessionTitle || '新会话',
        sessionType: normalizeSessionType(session.sessionType || session.type),
        createdAt: session.createTime ? Date.parse(session.createTime) : Date.now(),
        messages: [],
        cards: []
    };
};

const handleNewSession = () => {
    showNewSessionPicker.value = true;
    sessionLimitError.value = '';
};

const loadSessions = async () => {
    sessionLoading.value = true;
    sessionError.value = '';
    try {
        const resp = await listSessions();
        const list = pickData(resp, '获取会话失败') || [];
        const normalized = (Array.isArray(list) ? list : [])
            .map(mapSession)
            .filter(Boolean);
        const chatsList = normalized.filter((item) => item.sessionType === 'chat');
        const agentList = normalized.filter((item) => item.sessionType === 'work');
        chatStore.setChats(chatsList);
        agentStore.setSessions(agentList);

        const nextChatId =
            chatStore.currentChatId && chatsList.some((item) => item.id === chatStore.currentChatId)
                ? chatStore.currentChatId
                : chatsList[0]?.id || null;
        const nextAgentId =
            agentStore.currentSessionId && agentList.some((item) => item.id === agentStore.currentSessionId)
                ? agentStore.currentSessionId
                : agentList[0]?.id || null;
        chatStore.setCurrentChatId(nextChatId);
        agentStore.setCurrentSessionId(nextAgentId);
    } catch (error) {
        sessionError.value = normalizeError(error).message || '获取会话失败';
        chatStore.setChats([]);
        agentStore.setSessions([]);
    } finally {
        sessionLoading.value = false;
    }
};

onMounted(() => {
    loadSessions();
});

const handleSelectChat = (chatId) => {
    if (route.path !== '/chat') {
        router.push('/chat');
    }
    if (chatId !== currentChatId.value) {
        chatStore.setCurrentChatId(chatId);
    }
};

const handleSelectAgent = (sessionId) => {
    if (route.path !== '/work') {
        router.push('/work');
    }
    if (sessionId !== currentAgentSessionId.value) {
        agentStore.setCurrentSessionId(sessionId);
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

const saveRenameChat = async (chat) => {
    if (!chat || editingChatId.value !== chat.id) {
        return;
    }
    const title = editChatTitle.value.trim() || '未命名会话';
    try {
        await updateSession({ id: chat.id, sessionTitle: title });
        chatStore.updateChatTitle(chat.id, title);
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '更新会话失败';
    }
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

const saveRenameAgent = async (session) => {
    if (!session || editingAgentId.value !== session.id) {
        return;
    }
    const title = editAgentTitle.value.trim() || '未命名会话';
    try {
        await updateSession({ id: session.id, sessionTitle: title });
        agentStore.updateSessionTitle(session.id, title);
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '更新会话失败';
    }
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

const handleDelete = async () => {
    if (!deleteTarget.value?.id) {
        showDeleteConfirm.value = false;
        return;
    }
    try {
        const target =
            deleteTarget.value.type === 'agent'
                ? agentStore.sessions.find((item) => item.id === deleteTarget.value.id)
                : chatStore.chats.find((item) => item.id === deleteTarget.value.id);
        if (!target) {
            throw new Error('会话不存在');
        }
        await deleteSession({ id: target.id, sessionId: target.sessionId });
        if (deleteTarget.value.type === 'agent') {
            agentStore.removeSession(deleteTarget.value.id);
        } else {
            chatStore.removeChat(deleteTarget.value.id);
        }
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '删除会话失败';
    }
    showDeleteConfirm.value = false;
    deleteTarget.value = { type: 'chat', id: '' };
    cancelRenameChat();
    cancelRenameAgent();
};

const closeNewSessionPicker = () => {
    showNewSessionPicker.value = false;
    sessionLimitError.value = '';
};

const confirmNewSession = async (type) => {
    sessionLimitError.value = '';
    const chatCount = chats.value.length;
    const workCount = agentSessions.value.length;
    if (type === 'chat' && chatCount >= 3) {
        sessionLimitError.value = 'Chat 会话已达到 3 个上限';
        return;
    }
    if (type === 'work' && workCount >= 3) {
        sessionLimitError.value = 'Work 会话已达到 3 个上限';
        return;
    }
    try {
        const resp = await insertSession({ sessionTitle: '新会话', sessionType: type });
        const created = mapSession(pickData(resp, '创建会话失败'));
        if (created) {
            if (type === 'work') {
                agentStore.upsertSession(created);
                agentStore.setCurrentSessionId(created.id);
                router.push('/work');
            } else {
                chatStore.upsertChat(created);
                chatStore.setCurrentChatId(created.id);
                router.push('/chat');
            }
            closeNewSessionPicker();
            sessionError.value = '';
            return;
        }
        await loadSessions();
        const session = type === 'work' ? agentStore.sessions[0] : chatStore.chats[0];
        if (!session) {
            throw new Error('创建会话失败');
        }
        if (type === 'work') {
            agentStore.setCurrentSessionId(session.id);
            router.push('/work');
        } else {
            chatStore.setCurrentChatId(session.id);
            router.push('/chat');
        }
        closeNewSessionPicker();
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '创建会话失败';
    }
};

const openProfile = () => {
    if (!authStore.isLogin) {
        router.push('/login');
        return;
    }
    profileForm.username = currentUser.value.username || '';
    profileForm.oldPassword = '';
    profileForm.newPassword = '';
    profileError.value = '';
    showProfile.value = true;
};

const closeProfile = () => {
    showProfile.value = false;
    profileSaving.value = false;
    profileError.value = '';
};

const saveProfile = async () => {
    profileError.value = '';
    profileSaving.value = true;
    if (profileForm.newPassword && !profileForm.oldPassword) {
        profileError.value = '请先输入旧密码';
        profileSaving.value = false;
        return;
    }
    try {
        const resp = await updatePassword({
            id: currentUser.value.id,
            username: profileForm.username,
            oldPassword: profileForm.oldPassword,
            newPassword: profileForm.newPassword
        });
        const payload = resp?.data || resp?.result || resp;
        const token = payload?.token || payload?.data?.token;
        const user =
            payload?.user ||
            payload?.data?.user ||
            (payload && (payload.username || payload.role || payload.id)
                ? {
                      id: payload.id ?? payload.data?.id,
                      username: payload.username ?? payload.data?.username,
                      role: payload.role ?? payload.data?.role
                  }
                : null);
        authStore.setAuth({
            token: token || authStore.token,
            user: user || authStore.user
        });
        showProfile.value = false;
    } catch (error) {
        profileError.value = error?.message || '更新失败，请稍后重试';
    } finally {
        profileSaving.value = false;
    }
};

const handleLogout = () => {
    authStore.clear();
    settingsStore.updateSettings({ token: '' });
    closeProfile();
    router.push('/login');
};

const toggleTheme = () => {
    settingsStore.updateSettings({ theme: isDarkTheme.value ? 'light' : 'dark' });
};
</script>

<template>
    <aside
        class="flex h-screen flex-col bg-[radial-gradient(120%_120%_at_0%_0%,#122544_0%,#0f172a_60%,#0b1220_100%)] p-[20px] text-[#e7ecf4] shadow-[10px_0_30px_rgba(0,0,0,0.08)] border-r border-[rgba(255,255,255,0.06)] max-[720px]:hidden"
    >
        <div class="mb-[12px] flex items-center justify-between gap-[12px]">
            <div class="flex items-center gap-[12px]">
                <div
                    class="h-[44px] w-[44px] overflow-hidden rounded-[14px] border border-[rgba(255,255,255,0.2)] bg-[radial-gradient(120%_120%_at_0%_0%,rgba(111,125,255,0.2),rgba(83,197,255,0.1))]"
                >
                    <img :src="logoImg" alt="Logo" class="h-full w-full object-cover block" />
                </div>
                <div>
                    <div class="text-[20px] font-bold">Dasi AI</div>
                    <div class="text-[14px] text-[rgba(231,236,244,0.7)]">RAG · MCP · OPENAI</div>
                </div>
            </div>
            <button
                class="grid h-[34px] w-[34px] place-items-center rounded-[10px] border border-[rgba(255,255,255,0.18)] bg-[rgba(255,255,255,0.08)] text-[rgba(231,236,244,0.9)] transition hover:bg-[rgba(255,255,255,0.14)] hover:text-white"
                type="button"
                :title="isDarkTheme ? '切换到白天' : '切换到黑天'"
                @click="toggleTheme"
            >
                <svg v-if="isDarkTheme" viewBox="0 0 24 24" class="h-[18px] w-[18px]" fill="currentColor" aria-hidden="true">
                    <path
                        d="M12 3.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5A.75.75 0 0112 3.75zm6.22 2.53a.75.75 0 011.06 1.06l-1.06 1.06a.75.75 0 11-1.06-1.06l1.06-1.06zM20.25 11.25a.75.75 0 010 1.5h-1.5a.75.75 0 010-1.5h1.5zm-2.47 6.72a.75.75 0 011.06-1.06l1.06 1.06a.75.75 0 11-1.06 1.06l-1.06-1.06zM12 18.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zm-6.22-.78a.75.75 0 011.06 0l1.06 1.06a.75.75 0 11-1.06 1.06l-1.06-1.06a.75.75 0 010-1.06zM3.75 12a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5A.75.75 0 013.75 12zm2.47-6.72a.75.75 0 011.06 0l1.06 1.06a.75.75 0 11-1.06 1.06L6.22 6.34a.75.75 0 010-1.06zM12 7.5a4.5 4.5 0 100 9 4.5 4.5 0 000-9z"
                    />
                </svg>
                <svg v-else viewBox="0 0 24 24" class="h-[18px] w-[18px]" fill="currentColor" aria-hidden="true">
                    <path
                        d="M21.752 15.002A9.718 9.718 0 0112 21.75 9.75 9.75 0 0112 2.25c.33 0 .658.016.983.048a.75.75 0 01.34 1.38 7.5 7.5 0 009.098 11.072.75.75 0 011.33.252z"
                    />
                </svg>
            </button>
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
            <div v-if="sessionLoading" class="text-[12px] text-[rgba(231,236,244,0.7)]">会话加载中...</div>
            <div v-else-if="sessionError" class="text-[12px] text-[#fca5a5]">{{ sessionError }}</div>

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

        <div class="flex items-center justify-between gap-[12px] rounded-[14px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.05)] p-[12px]">
            <button class="flex items-center gap-[10px] text-left" type="button" @click="openProfile">
                <div
                    class="grid h-[40px] w-[40px] place-items-center rounded-[12px] bg-[var(--avatar-bg)] font-bold text-[var(--avatar-text)]"
                >
                    {{ avatarChar }}
                </div>
                <div>
                    <div class="font-bold text-white">{{ currentUser.username || '访客' }}</div>
                </div>
            </button>
            <div class="flex items-center gap-[8px]">
                <button
                    v-if="isLogin"
                    class="main-logout rounded-[8px] px-2 py-1 text-[14px] text-[rgba(231,236,244,0.8)] transition hover:text-white"
                    type="button"
                    @click="handleLogout"
                >
                    退出登录
                </button>
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
                <div v-if="sessionLimitError" class="rounded-[10px] border border-[rgba(15,23,42,0.1)] bg-white px-[16px] py-[10px] text-[14px] text-[#dc2626] shadow-[0_12px_30px_rgba(15,23,42,0.12)]">
                    {{ sessionLimitError }}
                </div>
            </div>
        </div>

        <div
            v-if="showProfile"
            class="fixed inset-0 z-[25] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="closeProfile"
        >
            <div
                class="w-full max-w-[520px] rounded-[14px] border border-[var(--border-color)] bg-white text-[var(--text-primary)] shadow-[0_20px_50px_rgba(15,23,42,0.2)]"
            >
                <div class="flex items-center justify-between border-b border-[var(--border-color)] px-[18px] py-[14px]">
                    <div class="text-[16px] font-bold">个人资料</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" type="button" @click="closeProfile">×</button>
                </div>
                <div class="space-y-[14px] px-[18px] py-[16px]">
                    <div>
                        <div class="mb-[6px] text-[13px] text-[var(--text-secondary)]">用户名</div>
                        <input
                            v-model="profileForm.username"
                            class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] text-[14px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                            placeholder="请输入用户名"
                        />
                    </div>
                    <div class="grid grid-cols-2 gap-[12px] max-[520px]:grid-cols-1">
                        <div>
                            <div class="mb-[6px] text-[13px] text-[var(--text-secondary)]">旧密码</div>
                            <input
                                v-model="profileForm.oldPassword"
                                type="password"
                                class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] text-[14px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                                placeholder="修改密码时必填"
                            />
                        </div>
                        <div>
                            <div class="mb-[6px] text-[13px] text-[var(--text-secondary)]">新密码</div>
                            <input
                                v-model="profileForm.newPassword"
                                type="password"
                                class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] text-[14px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                                placeholder="输入新密码"
                            />
                        </div>
                    </div>
                    <div class="text-[12px] text-[#f87171]" v-if="profileError">{{ profileError }}</div>
                    <div class="flex items-center justify-end gap-[10px] border-t border-[var(--border-color)] pt-[14px]">
                        <button
                            class="rounded-[10px] border border-[var(--border-color)] bg-white px-[14px] py-[10px] text-[14px] font-semibold text-[var(--text-primary)] transition hover:bg-[#f7f9fc]"
                            type="button"
                            @click="closeProfile"
                        >
                            取消
                        </button>
                        <button
                            class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-[14px] font-semibold text-white transition hover:brightness-95 disabled:cursor-not-allowed disabled:opacity-70"
                            type="button"
                            :disabled="profileSaving"
                            @click="saveProfile"
                        >
                            {{ profileSaving ? '保存中...' : '保存' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </aside>
</template>
