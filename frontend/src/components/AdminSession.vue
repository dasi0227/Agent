<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { marked } from 'marked';
import hljs from 'highlight.js';
import DOMPurify from 'dompurify';
import SidebarAdmin from './SidebarAdmin.vue';
import AppFooter from './AppFooter.vue';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { listAdminSessions, listChatMessages, listWorkAnswerMessages, listWorkSseMessages } from '../request/api';
import { normalizeError, notifyAdminError } from '../request/request';
import { formatMcpJson } from '../utils/StringUtil';

const router = useRouter();
const currentKey = ref('session');
const menuGroups = adminMenuGroups;

const sessions = ref([]);
const selectedSession = ref(null);
const detail = reactive({
    chatMessages: [],
    workCards: [],
    workMessages: []
});
const loading = reactive({
    list: false,
    detail: false
});

const renderer = new marked.Renderer();
renderer.code = (code, infostring) => {
    const lang = (infostring || '').match(/\S*/)?.[0] || '';
    if (lang && hljs.getLanguage(lang)) {
        return `<pre><code class="hljs language-${lang}">${hljs.highlight(code, {
            language: lang,
            ignoreIllegals: true
        }).value}</code></pre>`;
    }
    return `<pre><code class="hljs">${hljs.highlightAuto(code).value}</code></pre>`;
};
marked.setOptions({ breaks: true, gfm: true, renderer });

const renderMarkdown = (text) => {
    if (!text) return '';
    return DOMPurify.sanitize(marked.parse(text), { ADD_ATTR: ['class'] });
};

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

const mapSession = (session) => ({
    id: session.id,
    sessionId: session.sessionId,
    sessionUser: session.sessionUser,
    sessionTitle: session.sessionTitle || '新会话',
    sessionType: normalizeSessionType(session.sessionType || session.type),
    createTime: session.createTime
});

const mapChatMessage = (message) => ({
    id: message.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
    role: message.messageRole || message.role || 'assistant',
    content: message.messageContent || message.content || '',
    createdAt: message.createTime ? Date.parse(message.createTime) : Date.now()
});

const mapCard = (message) => {
    const raw = message?.messageContent || '';
    let parsed = null;
    try {
        parsed = raw ? JSON.parse(raw) : null;
    } catch (error) {
        parsed = null;
    }
    const payload = parsed && typeof parsed === 'object' ? parsed : { sectionContent: raw };
    return {
        id: message.id || payload.id || `card_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
        clientType: payload.clientType || '',
        sectionType: payload.sectionType || '',
        sectionContent: payload.sectionContent || '',
        round: payload.round ?? null,
        step: payload.step ?? null,
        timestamp: payload.timestamp ?? null
    };
};

const mapWorkMessage = (message) => ({
    id: message.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
    role: message.messageRole || message.role || 'assistant',
    content: message.messageContent || message.content || '',
    createdAt: message.createTime ? Date.parse(message.createTime) : Date.now()
});

const formatTime = (value) => {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) router.push(target.path);
};

const loadSessions = async () => {
    loading.list = true;
    try {
        const resp = await listAdminSessions();
        const list = pickData(resp, '获取会话失败') || [];
        sessions.value = (Array.isArray(list) ? list : []).map(mapSession);
    } catch (error) {
        const msg = normalizeError(error).message || '获取会话失败';
        notifyAdminError(error, msg);
        sessions.value = [];
    } finally {
        loading.list = false;
    }
};

const clearDetail = () => {
    detail.chatMessages = [];
    detail.workCards = [];
    detail.workMessages = [];
};

const loadDetail = async (session) => {
    if (!session?.sessionId) return;
    loading.detail = true;
    clearDetail();
    try {
        if (session.sessionType === 'chat') {
            const resp = await listChatMessages({ sessionId: session.sessionId });
            const list = pickData(resp, '获取会话消息失败') || [];
            detail.chatMessages = (Array.isArray(list) ? list : []).map(mapChatMessage);
        } else {
            const [sseResp, answerResp] = await Promise.all([
                listWorkSseMessages({ sessionId: session.sessionId }),
                listWorkAnswerMessages({ sessionId: session.sessionId })
            ]);
            const sseList = pickData(sseResp, '获取会话卡片失败') || [];
            const answerList = pickData(answerResp, '获取会话消息失败') || [];
            detail.workCards = (Array.isArray(sseList) ? sseList : [])
                .map(mapCard)
                .filter(
                    (card) =>
                        card.sectionType !== 'summarizer_overview' && card.sectionType !== 'replier_overview'
                );
            detail.workMessages = (Array.isArray(answerList) ? answerList : []).map(mapWorkMessage);
        }
    } catch (error) {
        const msg = normalizeError(error).message || '获取会话详情失败';
        notifyAdminError(error, msg);
    } finally {
        loading.detail = false;
    }
};

const enterDetail = async (session) => {
    selectedSession.value = session;
    await loadDetail(session);
};

const backToGrid = () => {
    selectedSession.value = null;
    clearDetail();
};


onMounted(() => {
    loadSessions();
});
</script>

<template>
    <div class="admin-font flex h-screen bg-[var(--bg-page)] text-[var(--text-primary)]">
        <SidebarAdmin :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[var(--border-color)] bg-[var(--surface-1)] px-6 py-4 shadow-[var(--shadow-soft)]">
                <div class="text-[18px] font-semibold">
                    SESSION 查看
                </div>
                <div v-if="selectedSession" class="flex items-center gap-2"></div>
            </header>

            <div class="flex-1 overflow-auto p-6">
                <div v-if="!selectedSession">
                    <div class="mb-4 text-[13px] text-[var(--text-secondary)]">共 {{ sessions.length }} 个 Session</div>
                    <div v-if="loading.list" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>
                    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        <div
                            v-for="session in sessions"
                            :key="session.sessionId"
                            class="session-card group relative h-[180px] cursor-pointer overflow-hidden rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] shadow-[var(--shadow-soft)] transition duration-200 hover:-translate-y-1"
                            @click="enterDetail(session)"
                        >
                            <div class="session-card-inner">
                                <div class="session-card-face session-card-front flex h-full flex-col gap-3 p-4">
                                    <div class="flex items-center justify-between gap-3">
                                        <div
                                            class="inline-flex w-fit items-center rounded-full px-3 py-1 text-[14px] font-semibold uppercase tracking-[0.08em]"
                                            :class="session.sessionType === 'work' ? 'bg-[#ede9fe] text-[#7c3aed]' : 'bg-[#dbeafe] text-[#2563eb]'"
                                        >
                                            {{ session.sessionType || '-' }}
                                        </div>
                                        <div class="text-[12px] leading-none text-[var(--text-secondary)]">{{ formatTime(session.createTime) }}</div>
                                    </div>
                                    <div class="flex flex-1 flex-col justify-center space-y-[6px] text-center">
                                        <div class="text-[19px] font-semibold break-all">{{ session.sessionId || '-' }}</div>
                                        <div class="text-[18px] text-[var(--text-secondary)]">{{ session.sessionUser || '-' }}</div>
                                    </div>
                                </div>
                                <div class="session-card-face session-card-back flex h-full flex-col items-center justify-center p-4 text-center">
                                    <div class="text-[15px] leading-6 text-[var(--text-secondary)]">
                                        {{ session.sessionTitle || '未命名会话' }}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-if="sessions.length === 0" class="col-span-full rounded-[12px] border border-dashed border-[var(--border-color)] bg-[var(--surface-1)] px-4 py-6 text-center text-[13px] text-[var(--text-secondary)]">
                            暂无会话数据
                        </div>
                    </div>
                </div>

                <div v-else class="flex h-full flex-col gap-4">
                    <div class="flex flex-wrap items-center justify-between gap-3">
                        <div class="flex flex-wrap items-center gap-3">
                            <div class="text-[18px] font-semibold text-[var(--text-primary)]">{{ selectedSession.sessionId }}</div>
                            <span class="inline-flex items-center rounded-full bg-[var(--accent-soft)] px-3 py-1 text-[14px] font-medium text-[var(--accent-strong)]">
                                类型：{{ selectedSession.sessionType }}
                            </span>
                            <span class="inline-flex items-center rounded-full bg-[#ecfdf3] px-3 py-1 text-[14px] font-medium text-[#16a34a]">
                                用户：{{ selectedSession.sessionUser || '-' }}
                            </span>
                            <span class="inline-flex items-center rounded-full border border-[var(--border-color)] bg-[var(--surface-3)] px-3 py-1 text-[14px] font-medium text-[var(--text-primary)]">
                                {{ formatTime(selectedSession.createTime) }}
                            </span>
                        </div>
                        <button
                            class="rounded-[10px] border border-[var(--border-color)] px-3 py-2 text-[14px] font-semibold text-[var(--text-primary)] transition hover:bg-[var(--surface-2)]"
                            type="button"
                            @click="backToGrid"
                        >
                            返回列表
                        </button>
                    </div>

                    <div v-if="loading.detail" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>

                    <div v-else>
                        <div v-if="selectedSession.sessionType === 'chat'" class="rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                            <div class="flex flex-col gap-4">
                                <div
                                    v-for="message in detail.chatMessages"
                                    :key="message.id"
                                    class="flex w-full"
                                    :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
                                >
                                    <div class="max-w-[720px] rounded-[14px] border border-[var(--border-color)] px-[14px] py-[12px] shadow-[var(--shadow-soft)]"
                                        :class="message.role === 'user' ? 'bg-[var(--bubble-user-bg)]' : 'bg-[var(--surface-2)]'">
                                        <div
                                            v-if="message.role === 'user'"
                                            class="whitespace-pre-wrap break-all leading-[1.6] [overflow-wrap:anywhere]"
                                        >
                                            {{ message.content }}
                                        </div>
                                        <div
                                            v-else
                                            class="markdown-body break-words leading-[1.6] [overflow-wrap:anywhere] [&_pre]:overflow-auto [&_pre]:rounded-[10px] [&_pre]:bg-[#0f172a] [&_pre]:p-[12px] [&_pre]:text-[#e2e8f0] [&_code]:rounded-[6px] [&_code]:bg-[#f1f5f9] [&_code]:px-[6px] [&_code]:py-[2px] [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:rounded-none"
                                            v-html="renderMarkdown(message.content)"
                                        ></div>
                                    </div>
                                </div>
                                <div v-if="detail.chatMessages.length === 0" class="text-center text-[13px] text-[var(--text-secondary)]">
                                    暂无对话记录
                                </div>
                            </div>
                        </div>

                        <div v-else class="grid grid-cols-1 gap-6 lg:grid-cols-2">
                            <div class="rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                                <div class="mb-3 text-[14px] font-semibold">Work-SSE 卡片</div>
                                <div class="flex flex-col gap-3">
                                    <div
                                        v-for="card in detail.workCards"
                                        :key="card.id"
                                        class="rounded-[12px] border border-[var(--border-color)] bg-[var(--surface-2)] p-3"
                                    >
                                        <div class="mb-2 flex flex-wrap items-center gap-2 text-[12px] text-[var(--text-secondary)]">
                                            <span class="rounded-full bg-[var(--surface-4)] px-2 py-1">{{ card.clientType || '-' }}</span>
                                            <span class="rounded-full bg-[var(--surface-4)] px-2 py-1">{{ card.sectionType || '-' }}</span>
                                            <span v-if="card.round !== null" class="rounded-full bg-[var(--surface-4)] px-2 py-1">round: {{ card.round }}</span>
                                            <span v-if="card.step !== null" class="rounded-full bg-[var(--surface-4)] px-2 py-1">step: {{ card.step }}</span>
                                        </div>
                                        <div class="whitespace-pre-wrap text-[13px] text-[var(--text-secondary)]">
                                            {{ formatMcpJson(card.sectionContent) }}
                                        </div>
                                    </div>
                                    <div v-if="detail.workCards.length === 0" class="text-center text-[13px] text-[var(--text-secondary)]">
                                        暂无卡片记录
                                    </div>
                                </div>
                            </div>
                            <div class="rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                                <div class="mb-3 text-[14px] font-semibold">Work-Answer 对话</div>
                                <div class="flex flex-col gap-4">
                                    <div
                                        v-for="message in detail.workMessages"
                                        :key="message.id"
                                        class="flex w-full"
                                        :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
                                    >
                                        <div class="max-w-[720px] rounded-[14px] border border-[var(--border-color)] px-[14px] py-[12px] shadow-[var(--shadow-soft)]"
                                            :class="message.role === 'user' ? 'bg-[var(--bubble-user-bg)]' : 'bg-[var(--surface-2)]'">
                                            <div
                                                v-if="message.role === 'user'"
                                                class="whitespace-pre-wrap break-all leading-[1.6] [overflow-wrap:anywhere]"
                                            >
                                                {{ message.content }}
                                            </div>
                                            <div
                                                v-else
                                                class="markdown-body break-words leading-[1.6] [overflow-wrap:anywhere] [&_pre]:overflow-auto [&_pre]:rounded-[10px] [&_pre]:bg-[#0f172a] [&_pre]:p-[12px] [&_pre]:text-[#e2e8f0] [&_code]:rounded-[6px] [&_code]:bg-[#f1f5f9] [&_code]:px-[6px] [&_code]:py-[2px] [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:rounded-none"
                                                v-html="renderMarkdown(message.content)"
                                            ></div>
                                        </div>
                                    </div>
                                    <div v-if="detail.workMessages.length === 0" class="text-center text-[13px] text-[var(--text-secondary)]">
                                        暂无对话记录
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <AppFooter inner-class="px-6" />
        </div>
    </div>
</template>

<style scoped>
.admin-font {
    font-size: 15px;
}

.session-card {
    perspective: 1000px;
}

.session-card-inner {
    height: 100%;
    transition: transform 0.6s;
    transform-style: preserve-3d;
}

.session-card:hover .session-card-inner {
    transform: rotateY(180deg);
}

.session-card-face {
    position: absolute;
    inset: 0;
    backface-visibility: hidden;
}

.session-card-back {
    transform: rotateY(180deg);
}
</style>
