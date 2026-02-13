<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { marked } from 'marked';
import hljs from 'highlight.js';
import DOMPurify from 'dompurify';
import {
    dispatchArmory,
    executeAgentStream,
    insertSession,
    listSessions,
    listWorkAnswerMessages,
    listWorkSseMessages,
    queryAgentList,
    updateSession
} from '../request/api';
import { normalizeError } from '../request/request';
import { formatMcpJson } from '../utils/StringUtil';
import { useAgentSettingsStore, useAgentStore, useChatStore, useWelcomeLaunchStore } from '../router/pinia';

const router = useRouter();
const agentStore = useAgentStore();
const chatStore = useChatStore();
const settingsStore = useAgentSettingsStore();
const welcomeLaunchStore = useWelcomeLaunchStore();

const agentOptions = ref([]);
const pendingAgentId = ref('');
const agentDropdownOpen = ref(false);
const agentSelectRef = ref(null);

const leftScrollRef = ref(null);
const rightScrollRef = ref(null);
const inputValue = ref('');
const showSettings = ref(false);
const isLeftAtBottom = ref(true);
const isRightAtBottom = ref(true);
const messageLoading = ref(false);
const sendError = ref('');
const skipNextLoadSessionId = ref(null);
let autoRefreshTimer = null;

const settingsForm = reactive({
    maxRetry: settingsStore.maxRetry,
    maxRound: settingsStore.maxRound
});

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

const SESSION_INVALID_HINT = '会话已失效，请新建会话';

const isInvalidSessionErrorMessage = (message) => {
    if (!message) return false;
    return (
        message.includes('会话不存在') ||
        message.includes('会话已失效') ||
        message.includes('会话类型不匹配') ||
        message.includes('无权限访问该会话') ||
        message.includes('无权限修改该会话')
    );
};

const listRemoteWorkSessions = async () => {
    const resp = await listSessions();
    const list = pickData(resp, '获取会话失败') || [];
    const mapped = (Array.isArray(list) ? list : []).map(mapSession).filter(Boolean);
    return mapped.filter((item) => item.sessionType === 'work');
};

const refreshWorkSessions = async () => {
    const workList = await listRemoteWorkSessions();
    agentStore.setSessions(workList);
    return workList;
};

const dropInvalidWorkSession = (sessionId = agentStore.currentSessionId) => {
    if (sessionId) {
        agentStore.removeSession(sessionId);
    }
    if (agentStore.sessions.length === 0 && chatStore.chats.length === 0) {
        router.replace('/welcome');
    }
    sendError.value = SESSION_INVALID_HINT;
};

const ensureWorkSessionValid = async (session) => {
    if (!session?.sessionId) return null;
    try {
        const workList = await listRemoteWorkSessions();
        const matched = workList.find((item) => item.sessionId === session.sessionId || item.id === session.id);
        if (!matched) {
            dropInvalidWorkSession(session.id);
            return null;
        }
        return matched;
    } catch (error) {
        sendError.value = normalizeError(error).message || '获取会话失败';
        return null;
    }
};

const mapMessage = (message) => ({
    id: message.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
    role: message.messageRole || message.role || 'assistant',
    content: message.messageContent || message.content || '',
    pending: false,
    error: null,
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

const currentAgentId = computed({
    get: () => {
        const session = agentStore.currentSession;
        if (session) {
            return session.agentId || '';
        }
        return pendingAgentId.value || '';
    },
    set: (value) => {
        const session = agentStore.currentSession;
        if (session?.id) {
            agentStore.setSessionAgent(session.id, value || '');
            return;
        }
        pendingAgentId.value = value || '';
    }
});

const currentAgentLabel = computed(() => {
    if (!currentAgentId.value) {
        return '选择AGENT';
    }
    const match = agentOptions.value.find((item) => item.value === currentAgentId.value);
    return match?.label || currentAgentId.value;
});

const fetchAgents = async () => {
    try {
        const resp = await queryAgentList();
        const list = Array.isArray(resp?.result)
            ? resp.result
            : Array.isArray(resp)
              ? resp
              : Array.isArray(resp?.data)
                ? resp.data
                : [];
        const normalized = list
            .map((item) => {
                if (!item || typeof item !== 'object') return null;
                const agentId = item.agentId || item.id || '';
                const agentName = item.agentName || item.name || agentId;
                const agentDesc = item.agentDesc || item.desc || '';
                if (!agentId) return null;
                return { label: agentName || agentId, value: agentId, desc: agentDesc };
            })
            .filter(Boolean);
        const seen = new Set();
        const unique = normalized.filter((item) => {
            if (seen.has(item.value)) return false;
            seen.add(item.value);
            return true;
        });
        agentOptions.value = unique;
        if (currentAgentId.value && !unique.some((item) => item.value === currentAgentId.value)) {
            currentAgentId.value = '';
        }
        return unique;
    } catch (error) {
        console.warn('获取 AGENT 列表失败', error);
        return [];
    }
};

const messages = computed(() => agentStore.currentMessages);
const cards = computed(() => agentStore.currentCards);
const sending = computed(() => agentStore.sending);
const userMessageCount = computed(() => messages.value.filter((item) => item.role === 'user').length);

const handleLeftScroll = () => {
    const el = leftScrollRef.value;
    if (!el) return;
    const distance = el.scrollHeight - el.scrollTop - el.clientHeight;
    isLeftAtBottom.value = distance < 80;
};

const handleRightScroll = () => {
    const el = rightScrollRef.value;
    if (!el) return;
    const distance = el.scrollHeight - el.scrollTop - el.clientHeight;
    isRightAtBottom.value = distance < 80;
};

const scrollLeftToBottom = (smooth = true) => {
    nextTick(() => {
        const el = leftScrollRef.value;
        if (!el) return;
        el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' });
    });
};

const scrollRightToBottom = (smooth = true) => {
    nextTick(() => {
        const el = rightScrollRef.value;
        if (!el) return;
        el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' });
    });
};

const loadWorkMessages = async (sessionId) => {
    if (!sessionId) {
        if (agentStore.currentSessionId) {
            agentStore.setSessionMessages(agentStore.currentSessionId, []);
            agentStore.setSessionCards(agentStore.currentSessionId, []);
        }
        return;
    }
    messageLoading.value = true;
    try {
        const [sseResp, answerResp] = await Promise.all([
            listWorkSseMessages({ sessionId }),
            listWorkAnswerMessages({ sessionId })
        ]);
        const sseList = pickData(sseResp, '获取会话卡片失败') || [];
        const answerList = pickData(answerResp, '获取会话消息失败') || [];
        const mappedCards = (Array.isArray(sseList) ? sseList : [])
            .map(mapCard)
            .filter(
                (card) =>
                    card.sectionType !== 'summarizer_overview' && card.sectionType !== 'replier_overview'
            );
        const mappedMessages = (Array.isArray(answerList) ? answerList : []).map(mapMessage);
        if (agentStore.currentSessionId) {
            agentStore.setSessionCards(agentStore.currentSessionId, mappedCards);
            agentStore.setSessionMessages(agentStore.currentSessionId, mappedMessages);
        }
    } catch (error) {
        const message = normalizeError(error).message || '获取消息失败';
        if (isInvalidSessionErrorMessage(message)) {
            dropInvalidWorkSession(agentStore.currentSessionId);
            return;
        }
        sendError.value = message;
    } finally {
        messageLoading.value = false;
    }
};

const ensureWorkSession = async ({ forceNew = false, sessionTitle = '新会话' } = {}) => {
    if (!forceNew && agentStore.currentSession) {
        if (!agentStore.currentSessionId && agentStore.currentSession?.id) {
            agentStore.setCurrentSessionId(agentStore.currentSession.id);
        }
        return agentStore.currentSession;
    }
    try {
        const resp = await insertSession({ sessionTitle: sessionTitle || '新会话', sessionType: 'work' });
        const created = mapSession(pickData(resp, '创建会话失败'));
        if (created) {
            agentStore.upsertSession(created);
            if (forceNew) {
                skipNextLoadSessionId.value = created.id;
            }
            agentStore.setCurrentSessionId(created.id);
            if (pendingAgentId.value) {
                agentStore.setSessionAgent(created.id, pendingAgentId.value);
            }
            return created;
        }
        const workList = await refreshWorkSessions();
        const session = workList[0] || null;
        if (!session) return null;
        agentStore.setCurrentSessionId(session.id);
        return session;
    } catch (error) {
        sendError.value = normalizeError(error).message || '创建会话失败';
        return null;
    }
};

const renameSessionIfNeeded = async (session, content) => {
    if (!session) return;
    if (session.title && session.title !== '新会话') return;
    const nextTitle = content.slice(0, 20) || '新会话';
    agentStore.updateSessionTitle(session.id, nextTitle);
    try {
        await updateSession({ id: session.id, sessionTitle: nextTitle });
    } catch (error) {
        sendError.value = normalizeError(error).message || '更新会话失败';
    }
};

watch(
    cards,
    () => {
        if (isLeftAtBottom.value) scrollLeftToBottom(true);
    },
    { deep: true }
);

watch(
    messages,
    () => {
        if (isRightAtBottom.value) scrollRightToBottom(true);
    },
    { deep: true }
);

watch(
    () => inputValue.value,
    () => {
        if (sendError.value && userMessageCount.value < 3) {
            sendError.value = '';
        }
    }
);

watch(
    userMessageCount,
    (count) => {
        if (count >= 3) {
            sendError.value = '当前会话已达到 3 条用户消息上限，请新建会话';
        }
    },
    { immediate: true }
);

watch(
    () => agentStore.currentSessionId,
    async (sessionId) => {
        sendError.value = '';
        if (sessionId && skipNextLoadSessionId.value === sessionId) {
            skipNextLoadSessionId.value = null;
            nextTick(() => {
                scrollLeftToBottom(false);
                scrollRightToBottom(false);
            });
            return;
        }
        const session = agentStore.currentSession;
        if (session?.sessionId) {
            await loadWorkMessages(session.sessionId);
        }
        nextTick(() => {
            scrollLeftToBottom(false);
            scrollRightToBottom(false);
        });
    },
    { immediate: true }
);

const toggleAgentDropdown = () => {
    if (agentOptions.value.length === 0) return;
    agentDropdownOpen.value = !agentDropdownOpen.value;
};

const selectAgent = async (value) => {
    currentAgentId.value = value;
    agentDropdownOpen.value = false;
    try {
        await dispatchArmory({ armoryType: 'work', armoryId: value });
    } catch (error) {
        console.warn('绑定 Work armory 失败', error);
    }
};

const handleClickOutside = (event) => {
    const target = event.target;
    const inAgent = agentSelectRef.value && agentSelectRef.value.contains(target);
    if (!inAgent) agentDropdownOpen.value = false;
};

const handleEscClose = (event) => {
    if (event.key === 'Escape') {
        agentDropdownOpen.value = false;
    }
};

const handleKeydown = (event) => {
    if (event.key === 'Enter' && event.metaKey) {
        event.preventDefault();
        sendMessage();
        return;
    }
    if (event.key === 'Escape') {
        agentDropdownOpen.value = false;
    }
};

const openSettings = () => {
    settingsForm.maxRetry = settingsStore.maxRetry;
    settingsForm.maxRound = settingsStore.maxRound;
    showSettings.value = true;
};

const saveSettings = () => {
    settingsStore.updateSettings({
        maxRetry: Number(settingsForm.maxRetry) || 2,
        maxRound: Number(settingsForm.maxRound) || 2
    });
    showSettings.value = false;
};

const rangeStyle = (value) => {
    const clamped = Math.min(3, Math.max(1, Number(value) || 1));
    const percent = ((clamped - 1) / 2) * 100;
    return {
        background: `linear-gradient(90deg, var(--accent-color) ${percent}%, var(--progress-track) ${percent}%)`
    };
};

const buildExecutePayload = (userMessage, sessionId, aiAgentId) => ({
    aiAgentId: aiAgentId || currentAgentId.value,
    userMessage,
    sessionId,
    maxRound: settingsStore.maxRound,
    maxRetry: settingsStore.maxRetry
});

const sendMessage = async (options = {}) => {
    const content = (options.content ?? inputValue.value).trim();
    const requestedAgentId = (options.agentId || '').trim();
    const resolvedAgentId = requestedAgentId || currentAgentId.value;
    if (!content || sending.value || !resolvedAgentId) return;
    sendError.value = '';
    const session = await ensureWorkSession({
        forceNew: Boolean(options.forceNew),
        sessionTitle: options.sessionTitle || '新会话'
    });
    if (!session) return;
    const validSession = await ensureWorkSessionValid(session);
    if (!validSession) return;
    if (userMessageCount.value >= 3) {
        sendError.value = '当前会话已达到 3 条用户消息上限，请新建会话';
        return;
    }
    if (validSession?.id) {
        agentStore.setSessionAgent(validSession.id, resolvedAgentId);
    }
    agentStore.stopCurrentRequest();
    agentStore.setSending(true);
    const controller = new AbortController();
    agentStore.setAbortController(controller);
    agentStore.addUserMessage(content);
    renameSessionIfNeeded(validSession, content);
    if (!Object.prototype.hasOwnProperty.call(options, 'content')) {
        inputValue.value = '';
    }
    scrollRightToBottom(true);
    await runExecute(content, controller, validSession.sessionId, resolvedAgentId);
};

const runExecute = async (content, controller, sessionId, aiAgentId) => {
    const assistantMessage = agentStore.addAssistantMessage({ pending: true, content: '' });
    const events = [];
    let closed = false;

    const finish = () => {
        if (closed) return;
        closed = true;
        const reversed = [...events].reverse();
        const summarizerEvent = reversed.find((item) => item?.sectionType === 'summarizer_overview');
        const replierEvent = reversed.find((item) => item?.sectionType === 'replier_overview');
        const answer = summarizerEvent?.sectionContent || replierEvent?.sectionContent || '';
        agentStore.updateAssistantMessage(assistantMessage.id, {
            content: answer || '（无内容）',
            pending: false,
            error: null
        });
        agentStore.setSending(false);
        agentStore.setAbortController(null);
        scrollRightToBottom(true);
    };

    const handleError = (error) => {
        if (closed) return;
        const friendly = normalizeError(error);
        closed = true;
        agentStore.updateAssistantMessage(assistantMessage.id, {
            content: friendly.message || '请求失败',
            pending: false,
            error: friendly
        });
        agentStore.setSending(false);
        agentStore.setAbortController(null);
        scrollRightToBottom(true);
    };

    try {
        await executeAgentStream({
            ...buildExecutePayload(content, sessionId, aiAgentId),
            signal: controller.signal,
            onData: (payload) => {
                if (typeof payload === 'string') {
                    handleError(new Error(payload));
                    return;
                }
                if (!payload || typeof payload !== 'object') return;
                const event = {
                    clientType: payload.clientType || '',
                    sectionType: payload.sectionType || '',
                    sectionContent: payload.sectionContent || '',
                    round: payload.round ?? null,
                    step: payload.step ?? null,
                    timestamp: payload.timestamp ?? null
                };
                events.push(event);
                if (
                    !(
                        event.sectionType === 'summarizer_overview' ||
                        event.sectionType === 'replier_overview'
                    )
                ) {
                    agentStore.addCard(event);
                }
                if (isLeftAtBottom.value) scrollLeftToBottom(true);
            },
            onError: handleError,
            onDone: finish
        });
    } catch (error) {
        handleError(error);
    }
};

const handleStop = () => {
    if (!sending.value) return;
    agentStore.stopCurrentRequest();
    const lastAssistant = [...messages.value].filter((item) => item.role === 'assistant').pop();
    if (lastAssistant && lastAssistant.pending) {
        agentStore.updateAssistantMessage(lastAssistant.id, {
            pending: false,
            error: { message: '已停止生成' }
        });
    }
};

const getContent = (message) => {
    if (!message?.content) return '';
    const raw = message.content.toString();
    return formatMcpJson(raw);
};

const getCardContent = (card) => {
    if (!card?.sectionContent) return '';
    return formatMcpJson(card.sectionContent);
};

const consumeWelcomeLaunchTask = async () => {
    const task = welcomeLaunchStore.takeTask('work');
    const prompt = (task?.prompt || '').trim();
    if (!prompt) return;

    if (!agentOptions.value.length) {
        await fetchAgents();
    }

    if (task.agentId) {
        pendingAgentId.value = task.agentId;
        currentAgentId.value = task.agentId;
        try {
            await dispatchArmory({ armoryType: 'work', armoryId: task.agentId });
        } catch (error) {
            console.warn('绑定 Work armory 失败', error);
        }
    } else if (!currentAgentId.value && agentOptions.value.length > 0) {
        currentAgentId.value = agentOptions.value[0].value;
    }

    if (!currentAgentId.value) {
        sendError.value = '暂无可用 AGENT，请先在后台配置 agent';
        return;
    }

    const session = await ensureWorkSession({
        forceNew: true,
        sessionTitle: task.sessionTitle || '新会话'
    });
    if (!session) return;
    const validSession = await ensureWorkSessionValid(session);
    if (!validSession) return;

    const resolvedAgentId = (task.agentId || currentAgentId.value || '').trim();
    if (!resolvedAgentId) {
        sendError.value = '暂无可用 AGENT，请先在后台配置 agent';
        return;
    }
    agentStore.setSessionAgent(validSession.id, resolvedAgentId);
    currentAgentId.value = resolvedAgentId;

    inputValue.value = prompt;
    await nextTick();
    await sendMessage({ agentId: resolvedAgentId });
};

onMounted(() => {
    document.addEventListener('click', handleClickOutside);
    window.addEventListener('keydown', handleEscClose);
    scrollLeftToBottom(false);
    scrollRightToBottom(false);
    fetchAgents().then(async () => {
        if (agentStore.currentSession) {
            await ensureWorkSessionValid(agentStore.currentSession);
        }
        await consumeWelcomeLaunchTask();
    });
    autoRefreshTimer = window.setInterval(async () => {
        if (sending.value || messageLoading.value) return;
        const session = agentStore.currentSession;
        const sessionId = session?.sessionId || '';
        if (!sessionId) return;
        await loadWorkMessages(sessionId);
    }, 5000);
});

onBeforeUnmount(() => {
    document.removeEventListener('click', handleClickOutside);
    window.removeEventListener('keydown', handleEscClose);
    if (autoRefreshTimer) {
        window.clearInterval(autoRefreshTimer);
        autoRefreshTimer = null;
    }
});
</script>

<template>
    <section class="grid h-screen grid-rows-[var(--header-height)_1fr_auto_var(--footer-height)] bg-[var(--bg-page)]">
        <header
            class="sticky top-0 z-10 h-[var(--header-height)] border-b border-[rgba(15,23,42,0.06)] bg-[var(--surface-3)] backdrop-blur-[6px]"
        >
            <div
                class="flex h-full w-full items-center justify-between gap-[12px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))] max-[720px]:pl-[8px] max-[720px]:pr-[calc(8px+var(--scrollbar-w))]"
            >
                <div class="flex items-center gap-[10px]">
                    <div class="relative flex items-center gap-[14px] font-semibold">
                        <label class="w-[36px] text-[14px] text-[var(--text-secondary)] text-right">AGENT</label>
                        <div
                            ref="agentSelectRef"
                            class="inline-flex min-h-[36px] min-w-[200px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                            :class="agentOptions.length === 0 ? 'cursor-not-allowed opacity-70' : ''"
                            @click="toggleAgentDropdown"
                        >
                            <span class="font-bold text-[var(--text-primary)]">{{ currentAgentLabel }}</span>
                            <span
                                class="caret transition-transform duration-150"
                                :class="agentDropdownOpen ? 'caret-open' : 'caret-closed'"
                            />
                        </div>
                        <div
                            v-if="agentDropdownOpen && agentOptions.length > 0"
                            class="absolute left-0 top-[calc(100%+6px)] z-[15] w-full rounded-[12px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_40px_rgba(15,23,42,0.12)] max-h-[240px] overflow-y-auto"
                        >
                            <div
                                v-for="item in agentOptions"
                                :key="item.value"
                                class="flex cursor-pointer items-center justify-between rounded-[10px] px-[12px] py-[10px] text-[var(--text-primary)] transition-colors duration-150 hover:bg-[#f5f7fb]"
                                :class="item.value === currentAgentId ? 'bg-[#e8f1ff] text-[var(--accent-color)] font-bold' : ''"
                                @click.stop="selectAgent(item.value)"
                            >
                                <span>{{ item.label }}</span>
                                <span v-if="item.value === currentAgentId" class="text-[13px]">✓</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="flex justify-end gap-[10px] max-[720px]:flex-wrap">
                    <button
                        class="inline-flex h-[36px] items-center justify-center rounded-[12px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[9px] font-bold leading-[1.1] text-white transition-all duration-200 hover:brightness-95"
                        type="button"
                        @click="openSettings"
                    >
                        回答设置
                    </button>
                </div>
            </div>
        </header>

        <div class="overflow-hidden bg-[var(--bg-page)]">
            <div class="grid h-full grid-cols-[1fr_auto_1fr] gap-0">
                <div
                    ref="leftScrollRef"
                    class="flex h-full flex-col overflow-y-auto py-[16px] pl-[24px] pr-[12px] scroll-smooth [scrollbar-gutter:auto] [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"
                    @scroll="handleLeftScroll"
                >
                    <div class="flex flex-1 flex-col gap-[12px]">
                        <div v-if="messageLoading" class="text-[12px] text-[var(--text-secondary)]">
                            加载会话消息中...
                        </div>
                        <div
                            v-for="card in cards"
                            :key="card.id"
                            class="rounded-[14px] border border-[var(--border-color)] bg-white px-[14px] py-[12px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                        >
                            <div class="mb-[8px] flex flex-wrap gap-[6px]">
                                <span class="rounded-full border border-[rgba(47,124,246,0.3)] bg-[rgba(47,124,246,0.08)] px-[10px] py-[2px] text-[12px] font-semibold text-[var(--accent-color)]">
                                    {{ card.clientType || '-' }}
                                </span>
                                <span class="rounded-full border border-[rgba(16,185,129,0.3)] bg-[rgba(16,185,129,0.12)] px-[10px] py-[2px] text-[12px] font-semibold text-[#10b981]">
                                    {{ card.sectionType || '-' }}
                                </span>
                                <span
                                    v-if="card.round !== null && card.round !== undefined"
                                    class="rounded-full border border-[rgba(139,92,246,0.3)] bg-[rgba(139,92,246,0.12)] px-[10px] py-[2px] text-[12px] font-semibold text-[#8b5cf6]"
                                >
                                    round: {{ card.round }}
                                </span>
                                <span
                                    v-if="card.step !== null && card.step !== undefined"
                                    class="rounded-full border border-[rgba(245,158,11,0.35)] bg-[rgba(245,158,11,0.12)] px-[10px] py-[2px] text-[12px] font-semibold text-[#f59e0b]"
                                >
                                    step: {{ card.step }}
                                </span>
                            </div>
                            <div class="whitespace-pre-wrap break-all [overflow-wrap:anywhere] text-[14px] leading-[1.6] text-[var(--text-primary)]">
                                {{ getCardContent(card) }}
                            </div>
                        </div>
                        <div v-if="cards.length === 0" class="flex flex-1 items-center justify-center text-[13px] text-[var(--text-secondary)]">
                            暂无执行记录
                        </div>
                    </div>
                </div>

                <div class="h-full w-[1px] border-l border-dashed border-[rgba(15,23,42,0.30)] dark-divider"></div>

                <div
                    ref="rightScrollRef"
                    class="flex h-full flex-col overflow-y-auto py-[16px] pl-[12px] pr-[24px] scroll-smooth [scrollbar-gutter:auto] [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"
                    @scroll="handleRightScroll"
                >
                    <div class="flex w-full flex-1 flex-col gap-[14px]">
                        <div
                            v-for="message in messages"
                            :key="message.id"
                            class="flex w-full"
                            :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
                        >
                            <div class="flex max-w-full flex-col gap-[6px]" :class="message.role === 'user' ? 'items-end' : 'items-start'">
                                <div
                                    class="relative w-fit max-w-[720px] rounded-[14px] px-[14px] py-[12px] shadow-[0_12px_30px_rgba(27,36,55,0.08)] border"
                                    :class="[
                                        message.error
                                            ? 'bg-[linear-gradient(135deg,#ffe4e4,#ffd6d6)] border-[#f3b6b6] text-[#b91c1c]'
                                            : message.role === 'user'
                                                ? 'bg-[var(--bubble-user-bg)] border-[var(--bubble-user-border)]'
                                                : 'bg-white border-[var(--border-color)]',
                                        message.pending ? 'border-dashed' : 'border-solid'
                                    ]"
                                >
                                    <div
                                        v-if="message.pending && message.role === 'assistant' && !message.content"
                                        class="inline-flex items-center gap-[8px]"
                                    >
                                        <div class="inline-flex items-center gap-[4px]">
                                            <span class="h-[6px] w-[6px] rounded-full bg-[#7b8190] animate-blink"></span>
                                            <span class="h-[6px] w-[6px] rounded-full bg-[#7b8190] animate-blink [animation-delay:0.2s]"></span>
                                            <span class="h-[6px] w-[6px] rounded-full bg-[#7b8190] animate-blink [animation-delay:0.4s]"></span>
                                        </div>
                                        <div class="text-[13px] text-[var(--text-secondary)]">执行中...</div>
                                    </div>
                                    <div
                                        v-else-if="message.role === 'user' || message.pending"
                                        class="whitespace-pre-wrap break-all [overflow-wrap:anywhere] leading-[1.6]"
                                        :class="message.error ? 'text-[#d14343]' : ''"
                                    >
                                        {{ getContent(message) }}
                                    </div>
                                    <div
                                        v-else
                                        class="markdown-body break-words [overflow-wrap:anywhere] leading-[1.6] [&_pre]:overflow-auto [&_pre]:rounded-[10px] [&_pre]:bg-[#0f172a] [&_pre]:p-[12px] [&_pre]:text-[#e2e8f0] [&_code]:rounded-[6px] [&_code]:bg-[#f1f5f9] [&_code]:px-[6px] [&_code]:py-[2px] [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:rounded-none"
                                        :class="message.error ? 'text-[#d14343]' : ''"
                                        v-html="renderMarkdown(getContent(message))"
                                    ></div>
                                </div>
                            </div>
                        </div>
                        <div v-if="messages.length === 0" class="flex flex-1 items-center justify-center text-[13px] text-[var(--text-secondary)]">
                            暂无对话记录
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="bg-[var(--bg-page)]">
            <div class="mx-auto flex w-full max-w-[900px] flex-col gap-0 py-[12px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
                <div class="flex flex-col gap-[10px] rounded-[16px] border border-[var(--border-color)] bg-white p-[12px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]">
                    <textarea
                        v-model="inputValue"
                        class="w-full resize-none rounded-[12px] border border-[var(--border-color)] bg-white px-[14px] py-[12px] text-[14px] shadow-[inset_0_1px_2px_rgba(15,23,42,0.06)] disabled:bg-[#f4f6fb]"
                        rows="3"
                        placeholder="输入问题，Command+Enter 发送"
                        :disabled="sending"
                        @keydown="handleKeydown"
                    ></textarea>
                    <div v-if="sendError" class="text-[12px] text-[var(--text-secondary)]">
                        {{ sendError }}
                    </div>
                    <div class="flex justify-end gap-[10px]">
                        <button
                            class="inline-flex h-[36px] items-center justify-center rounded-[12px] border border-[var(--border-color)] bg-white px-[14px] py-[9px] font-bold leading-[1.1] text-[var(--text-primary)] transition-all duration-200 hover:bg-[#f7f9fc] disabled:cursor-not-allowed disabled:opacity-70"
                            type="button"
                            :disabled="!sending"
                            @click="handleStop"
                        >
                            停止生成
                        </button>
                        <button
                            class="inline-flex h-[36px] items-center justify-center rounded-[12px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[9px] font-bold leading-[1.1] text-white transition-all duration-200 hover:brightness-95 disabled:cursor-not-allowed disabled:opacity-70"
                            type="button"
                            :disabled="sending || !inputValue.trim() || !currentAgentId || userMessageCount >= 3"
                            @click="sendMessage"
                        >
                            {{ sending ? '执行中…' : '发送' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <footer class="h-[var(--footer-height)] border-t border-[rgba(15,23,42,0.06)] bg-[var(--surface-3)] backdrop-blur-[6px]">
            <div
                class="mx-auto flex h-full w-full max-w-[900px] items-center justify-between pl-[24px] pr-[calc(24px+var(--scrollbar-w))] text-[13px] text-[var(--text-secondary)] max-[720px]:pl-[8px] max-[720px]:pr-[calc(8px+var(--scrollbar-w))]"
            >
                <span>© 2025 Dasi</span>
                <span>内容为 AI 生成，仅供参考，请注意甄别</span>
            </div>
        </footer>

        <div v-if="showSettings" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="showSettings = false">
            <div class="w-full max-w-[520px] rounded-[16px] border border-[var(--border-color)] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.2)]">
                <div class="flex items-center justify-between border-b border-[var(--border-color)] px-[18px] pt-[14px] pb-[10px]">
                    <div class="text-[18px] font-bold">回答设置</div>
                    <button class="text-[22px] text-[var(--text-secondary)]" type="button" @click="showSettings = false">×</button>
                </div>
                <div class="flex flex-col gap-[14px] px-[18px] py-[14px]">
                    <div class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">maxRetry（最多重试）</label>
                        <div class="flex flex-col gap-[10px]">
                            <input
                                v-model.number="settingsForm.maxRetry"
                                type="range"
                                min="1"
                                max="3"
                                step="1"
                                class="h-[6px] w-full cursor-pointer appearance-none rounded-full bg-[var(--progress-track)] [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:h-[14px] [&::-webkit-slider-thumb]:w-[14px] [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-[var(--accent-color)] [&::-webkit-slider-thumb]:shadow-[0_6px_14px_rgba(47,124,246,0.35)] [&::-moz-range-thumb]:h-[14px] [&::-moz-range-thumb]:w-[14px] [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-[var(--accent-color)]"
                                :style="rangeStyle(settingsForm.maxRetry)"
                            />
                            <div class="flex items-center justify-between px-[2px]">
                                <div v-for="value in [1, 2, 3]" :key="`retry-dot-${value}`" class="flex flex-col items-center gap-[6px]">
                                    <span
                                        class="h-[8px] w-[8px] rounded-full"
                                        :class="value === settingsForm.maxRetry ? 'bg-[var(--accent-color)]' : 'bg-[#cbd5e1]'"
                                    ></span>
                                    <span
                                        class="text-[12px]"
                                        :class="settingsForm.maxRetry === value ? 'text-[var(--accent-color)] font-semibold' : 'text-[var(--text-secondary)]'"
                                    >
                                        {{ value }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">maxRound（最大轮次）</label>
                        <div class="flex flex-col gap-[10px]">
                            <input
                                v-model.number="settingsForm.maxRound"
                                type="range"
                                min="1"
                                max="3"
                                step="1"
                                class="h-[6px] w-full cursor-pointer appearance-none rounded-full bg-[var(--progress-track)] [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:h-[14px] [&::-webkit-slider-thumb]:w-[14px] [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-[var(--accent-color)] [&::-webkit-slider-thumb]:shadow-[0_6px_14px_rgba(47,124,246,0.35)] [&::-moz-range-thumb]:h-[14px] [&::-moz-range-thumb]:w-[14px] [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-[var(--accent-color)]"
                                :style="rangeStyle(settingsForm.maxRound)"
                            />
                            <div class="flex items-center justify-between px-[2px]">
                                <div v-for="value in [1, 2, 3]" :key="`round-dot-${value}`" class="flex flex-col items-center gap-[6px]">
                                    <span
                                        class="h-[8px] w-[8px] rounded-full"
                                        :class="value === settingsForm.maxRound ? 'bg-[var(--accent-color)]' : 'bg-[#cbd5e1]'"
                                    ></span>
                                    <span
                                        class="text-[12px]"
                                        :class="settingsForm.maxRound === value ? 'text-[var(--accent-color)] font-semibold' : 'text-[var(--text-secondary)]'"
                                    >
                                        {{ value }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="flex justify-end gap-[10px] border-t border-[var(--border-color)] px-[18px] pt-[12px] pb-[16px]">
                    <button
                        class="inline-flex items-center justify-center rounded-[12px] border border-[var(--border-color)] bg-white px-[14px] py-[9px] font-bold leading-[1.1] text-[var(--text-primary)] transition-all duration-200 hover:bg-[#f7f9fc]"
                        type="button"
                        @click="showSettings = false"
                    >
                        取消
                    </button>
                    <button
                        class="inline-flex items-center justify-center rounded-[12px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[9px] font-bold leading-[1.1] text-white transition-all duration-200 hover:brightness-95"
                        type="button"
                        @click="saveSettings"
                    >
                        保存
                    </button>
                </div>
            </div>
        </div>
    </section>
</template>

<style scoped>
.caret {
    display: inline-block;
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 7px solid #94a3b8;
    transform-origin: center;
    transition: transform 0.15s ease;
}

.caret-open {
    transform: rotate(0deg);
}

.caret-closed {
    transform: rotate(-90deg);
}
</style>
