<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import {
    fetchComplete,
    fetchStream,
    pickContentFromResult,
    queryChatModels,
    queryRagTags,
    uploadRagFile,
    uploadRagGit
} from '../request/api';
import { normalizeError } from '../request/request';
import { applyStreamToken, createStreamAccumulator, parseThinkText } from '../utils/parseThink';
import { createTypewriter, DEFAULT_TYPEWRITER_SEGMENTS } from '../utils/typewriter';
import { useChatStore, useSettingsStore } from '../router/pinia';

const chatStore = useChatStore();
const settingsStore = useSettingsStore();

const models = ref([]);
const ragTags = ref([{ label: '不使用知识库', value: '' }]);

const messageScrollRef = ref(null);
const modelSelectRef = ref(null);
const ragSelectRef = ref(null);
const uploadRagSelectRef = ref(null);
const inputValue = ref('');
const showSettings = ref(false);
const showUploadModal = ref(false);
const isAtBottom = ref(true);
const modelDropdownOpen = ref(false);
const ragDropdownOpen = ref(false);
const uploadRagDropdownOpen = ref(false);

const typewriterState = reactive({
    lines: [],
    lineIndex: 0,
    playing: false
});

const uploadForm = reactive({
    mode: 'file',
    tagInput: '',
    selectedTag: '',
    file: null,
    fileName: '',
    fileSize: '',
    repoUrl: '',
    repoUsername: '',
    repoPassword: '',
    error: '',
    uploading: false
});

const settingsForm = reactive({
    type: settingsStore.type,
    temperature: settingsStore.temperature,
    topK: settingsStore.topK
});

const typewriterController = createTypewriter({
    segments: DEFAULT_TYPEWRITER_SEGMENTS,
    charDelay: 45,
    segmentPause: 3000,
    loop: true,
    onUpdate: ({ lines, lineIndex, playing }) => {
        typewriterState.lines = [...lines];
        typewriterState.lineIndex = lineIndex;
        typewriterState.playing = playing;
    }
});

const currentModel = computed({
    get: () => settingsStore.model,
    set: (value) => settingsStore.updateSettings({ model: value })
});

const currentModelLabel = computed(() => {
    const match = models.value.find((item) => item.value === currentModel.value);
    if (match?.label) return match.label;
    return models.value.length === 0 ? '暂无模型' : currentModel.value;
});

const currentRagTag = computed({
    get: () => settingsStore.ragTag,
    set: (value) => settingsStore.updateSettings({ ragTag: value })
});

const messages = computed(() => chatStore.currentMessages);
const sending = computed(() => chatStore.sending);
const hasMessages = computed(() => messages.value.length > 0);

const handleScroll = () => {
    const el = messageScrollRef.value;
    if (!el) return;
    const distance = el.scrollHeight - el.scrollTop - el.clientHeight;
    isAtBottom.value = distance < 80;
};

const scrollToBottom = (smooth = true) => {
    nextTick(() => {
        const el = messageScrollRef.value;
        if (!el) return;
        el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' });
    });
};

watch(
    messages,
    () => {
        if (isAtBottom.value) scrollToBottom(true);
    },
    { deep: true }
);

watch(
    () => chatStore.currentChatId,
    () => nextTick(() => scrollToBottom(false))
);

const startTypewriter = () => {
    if (hasMessages.value) return;
    typewriterController.start();
};

const stopTypewriter = () => {
    typewriterController.stop();
    typewriterState.lines = [];
    typewriterState.lineIndex = 0;
    typewriterState.playing = false;
};

watch(
    hasMessages,
    (val) => {
        if (val) {
            stopTypewriter();
        } else {
            startTypewriter();
        }
    },
    { immediate: true }
);

const fetchTags = async () => {
    try {
        const resp = await queryRagTags();
        const list = Array.isArray(resp?.result)
            ? resp.result
            : Array.isArray(resp)
              ? resp
              : Array.isArray(resp?.data)
                ? resp.data
                : [];
        const unique = Array.from(new Set(['', ...list.filter((t) => typeof t === 'string')]));
        ragTags.value = unique.map((item) => ({
            label: item || '不使用知识库',
            value: item || ''
        }));
        if (!unique.includes(currentRagTag.value)) {
            currentRagTag.value = '';
        }
    } catch (error) {
        console.warn('获取知识库标签失败', error);
    }
};

const fetchModels = async () => {
    try {
        const resp = await queryChatModels();
        const list = Array.isArray(resp?.result)
            ? resp.result
            : Array.isArray(resp)
              ? resp
              : Array.isArray(resp?.data)
                ? resp.data
                : [];
        const normalized = list
            .map((item) => {
                if (typeof item === 'string') {
                    return { label: item, value: item };
                }
                if (item && typeof item === 'object') {
                    const clientId = item.clientId || item.modelId || item.id || '';
                    const modelName = item.modelName || item.name || clientId;
                    if (!clientId) return null;
                    return { label: modelName || clientId, value: clientId };
                }
                return null;
            })
            .filter(Boolean);
        const seen = new Set();
        const unique = normalized.filter((item) => {
            if (seen.has(item.value)) return false;
            seen.add(item.value);
            return true;
        });
        if (unique.length > 0) {
            models.value = unique;
            if (!unique.some((item) => item.value === currentModel.value)) {
                currentModel.value = unique[0].value;
            }
        }
    } catch (error) {
        console.warn('获取模型列表失败', error);
    }
};

onMounted(() => {
    scrollToBottom(false);
    attachListeners();
    fetchTags();
    fetchModels();
});

watch(currentModel, () => {
    fetchTags();
});

onBeforeUnmount(() => {
    chatStore.stopCurrentRequest();
    detachListeners();
    stopTypewriter();
});

const handleKeydown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
    if (event.key === 'Escape') {
        modelDropdownOpen.value = false;
        ragDropdownOpen.value = false;
        uploadRagDropdownOpen.value = false;
    }
};

const extractStreamParts = (payload) => {
    const data = payload?.result ?? payload;
    const finishReason = data?.finishReason || payload?.finishReason || null;
    if (typeof data === 'string') {
        return { token: data, finishReason };
    }
    if (!data || typeof data !== 'object') {
        return { token: '', finishReason };
    }
    if (Array.isArray(data.choices) && data.choices.length > 0) {
        const choice = data.choices[0] || {};
        const delta = choice.delta || choice.message || {};
    const answer = delta.content || delta.text || '';
    return {
        answer,
        finishReason: choice.finish_reason || finishReason,
        direct: true
    };
    }
    const answer = data.content || data.text || data.output?.content || data.output?.text || '';
    if (answer) {
        return { answer, finishReason, direct: true };
    }
    return { token: pickContentFromResult(data), finishReason };
};

const sendMessage = async () => {
    const content = inputValue.value.trim();
    if (!content || sending.value || !currentModel.value) return;
    const mode = settingsStore.type || 'complete';
    chatStore.stopCurrentRequest();
    stopTypewriter();
    chatStore.setSending(true);
    const controller = new AbortController();
    chatStore.setAbortController(controller);
    chatStore.addUserMessage(content);
    inputValue.value = '';
    scrollToBottom(true);
    if (mode === 'stream') {
        await runStream(content, controller);
    } else {
        await runComplete(content, controller);
    }
};

const runComplete = async (content, controller) => {
    const assistantMessage = chatStore.addAssistantMessage({ pending: true, content: '', think: '' });
    try {
        const response = await fetchComplete({
            clientId: currentModel.value,
            userMessage: content,
            signal: controller.signal
        });
        const text = pickContentFromResult(response);
        const { answer } = parseThinkText(text);
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: answer || '（无内容）',
            think: '',
            pending: false,
            error: null
        });
    } catch (error) {
        const friendly = normalizeError(error);
        const stopped = friendly.message && friendly.message.includes('取消');
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: stopped ? '已停止生成' : friendly.message || '请求失败',
            think: '',
            pending: false,
            error: friendly
        });
    } finally {
        chatStore.setSending(false);
        chatStore.setAbortController(null);
        scrollToBottom(true);
    }
};

const runStream = async (content, controller) => {
    const accumulator = createStreamAccumulator();
    const assistantMessage = chatStore.addAssistantMessage({ pending: true, content: '', think: '' });
    let closed = false;

    const finishStream = () => {
        if (closed) return;
        closed = true;
        if (accumulator.carry) {
            if (accumulator.inThink) {
                // discard remaining think fragment
            } else {
                accumulator.answer += accumulator.carry;
            }
            accumulator.carry = '';
        }
        const answerText = accumulator.answer?.trim() || '';
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: answerText || accumulator.answer,
            think: '',
            pending: false
        });
        chatStore.setSending(false);
        chatStore.setAbortController(null);
        scrollToBottom(true);
    };

    const handleError = (error) => {
        if (closed) return;
        const friendly = normalizeError(error);
        if (friendly.message && friendly.message.toLowerCase().includes('取消')) {
            finishStream();
            return;
        }
        closed = true;
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: friendly.message || '请求失败',
            think: '',
            pending: false,
            error: friendly
        });
        chatStore.setSending(false);
        chatStore.setAbortController(null);
        scrollToBottom(true);
    };

    try {
        await fetchStream({
            clientId: currentModel.value,
            userMessage: content,
            signal: controller.signal,
            onData: (payload) => {
                const { token, answer, direct, finishReason } = extractStreamParts(payload);
                if (direct) {
                    if (answer) {
                        accumulator.answer += answer;
                    }
                } else if (token) {
                    applyStreamToken(accumulator, token);
                }
                if (direct || token) {
                    chatStore.updateAssistantMessage(assistantMessage.id, {
                        content: accumulator.answer,
                        think: '',
                        pending: true,
                        error: null
                    });
                    if (isAtBottom.value) scrollToBottom(true);
                }
                if (finishReason === 'stop') finishStream();
            },
            onError: handleError,
            onDone: finishStream
        });
    } catch (error) {
        handleError(error);
    }
};

const handleStop = () => {
    if (!sending.value) return;
    chatStore.stopCurrentRequest();
    const lastAssistant = [...messages.value].filter((item) => item.role === 'assistant').pop();
    if (lastAssistant && lastAssistant.pending) {
        chatStore.updateAssistantMessage(lastAssistant.id, {
            pending: false,
            error: { message: '已停止生成' }
        });
    }
};

const openSettings = () => {
    settingsForm.type = settingsStore.type;
    settingsForm.temperature = settingsStore.temperature;
    settingsForm.topK = settingsStore.topK;
    showSettings.value = true;
};

const saveSettings = () => {
    const safeTemperature = Math.min(2, Math.max(0, Number(settingsForm.temperature) || 0));
    const safeTopK = Math.max(1, Math.floor(Number(settingsForm.topK) || 1));
    settingsStore.updateSettings({
        type: settingsForm.type,
        temperature: safeTemperature,
        topK: safeTopK
    });
    showSettings.value = false;
};

const getContent = (message) => (message?.content ? message.content.toString().trimStart() : '');

const toggleModelDropdown = () => {
    if (models.value.length === 0) return;
    ragDropdownOpen.value = false;
    modelDropdownOpen.value = !modelDropdownOpen.value;
};

const selectModel = (value) => {
    currentModel.value = value;
    modelDropdownOpen.value = false;
};

const toggleRagDropdown = () => {
    modelDropdownOpen.value = false;
    ragDropdownOpen.value = !ragDropdownOpen.value;
};

const selectRag = (value) => {
    currentRagTag.value = value;
    uploadForm.tagInput = value;
    uploadForm.selectedTag = value;
    ragDropdownOpen.value = false;
};

const toggleUploadRagDropdown = () => {
    uploadRagDropdownOpen.value = !uploadRagDropdownOpen.value;
};

const selectUploadRag = (value) => {
    uploadForm.selectedTag = value;
    uploadForm.tagInput = value;
    uploadRagDropdownOpen.value = false;
};

const handleClickOutside = (event) => {
    const target = event.target;
    const inModel = modelSelectRef.value && modelSelectRef.value.contains(target);
    const inRag = ragSelectRef.value && ragSelectRef.value.contains(target);
    const inUploadRag = uploadRagSelectRef.value && uploadRagSelectRef.value.contains(target);
    if (!inModel) modelDropdownOpen.value = false;
    if (!inRag) ragDropdownOpen.value = false;
    if (!inUploadRag) uploadRagDropdownOpen.value = false;
};

const handleEscClose = (event) => {
    if (event.key === 'Escape') {
        modelDropdownOpen.value = false;
        ragDropdownOpen.value = false;
        uploadRagDropdownOpen.value = false;
    }
};

const attachListeners = () => {
    document.addEventListener('click', handleClickOutside);
    window.addEventListener('keydown', handleEscClose);
};

const detachListeners = () => {
    document.removeEventListener('click', handleClickOutside);
    window.removeEventListener('keydown', handleEscClose);
};

const openUpload = () => {
    uploadForm.mode = 'file';
    uploadForm.tagInput = currentRagTag.value || '';
    uploadForm.selectedTag = currentRagTag.value || '';
    uploadForm.file = null;
    uploadForm.fileName = '';
    uploadForm.fileSize = '';
    uploadForm.repoUrl = '';
    uploadForm.repoUsername = '';
    uploadForm.repoPassword = '';
    uploadForm.error = '';
    uploadForm.uploading = false;
    uploadRagDropdownOpen.value = false;
    showUploadModal.value = true;
};

const closeUpload = () => {
    showUploadModal.value = false;
    uploadForm.mode = 'file';
    uploadForm.tagInput = '';
    uploadForm.selectedTag = '';
    uploadForm.file = null;
    uploadForm.fileName = '';
    uploadForm.fileSize = '';
    uploadForm.repoUrl = '';
    uploadForm.repoUsername = '';
    uploadForm.repoPassword = '';
    uploadForm.error = '';
    uploadForm.uploading = false;
};

const formatSize = (size) => {
    if (!size && size !== 0) return '';
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
};

const handleFileChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.name.toLowerCase().endsWith('.txt')) {
        uploadForm.error = '仅支持 .txt 文件';
        uploadForm.file = null;
        uploadForm.fileName = '';
        uploadForm.fileSize = '';
        event.target.value = '';
        return;
    }
    uploadForm.error = '';
    uploadForm.file = file;
    uploadForm.fileName = file.name;
    uploadForm.fileSize = formatSize(file.size);
};

const resolveUploadTag = () => {
    const input = uploadForm.tagInput.trim();
    if (input) return input;
    return uploadForm.selectedTag || '';
};

const handleUpload = async () => {
    uploadForm.error = '';
    if (uploadForm.mode === 'file') {
        const tag = resolveUploadTag();
        if (!tag) {
            uploadForm.error = '请选择或输入知识库标签';
            return;
        }
        if (!uploadForm.file) {
            uploadForm.error = '请选择 txt 文件';
            return;
        }
        uploadForm.uploading = true;
        try {
            await uploadRagFile({ ragTag: tag, file: uploadForm.file });
            await fetchTags();
            currentRagTag.value = tag;
            selectRag(tag);
            closeUpload();
        } catch (error) {
            const friendly = normalizeError(error);
            uploadForm.error = friendly.message || '上传失败，请重试';
        } finally {
            uploadForm.uploading = false;
        }
        return;
    }

    const repo = uploadForm.repoUrl.trim();
    const username = uploadForm.repoUsername.trim();
    const password = uploadForm.repoPassword;
    if (!repo) {
        uploadForm.error = '请输入 Git 仓库地址';
        return;
    }
    if (!username) {
        uploadForm.error = '请输入 Git 用户名';
        return;
    }
    if (!password) {
        uploadForm.error = '请输入 Git 密码';
        return;
    }
    uploadForm.uploading = true;
    try {
        await uploadRagGit({ repo, username, password });
        await fetchTags();
        const repoName = repo.split('/').pop()?.replace(/\.git$/i, '') || '';
        if (repoName) {
            currentRagTag.value = repoName;
            selectRag(repoName);
        }
        closeUpload();
    } catch (error) {
        const friendly = normalizeError(error);
        uploadForm.error = friendly.message || '上传失败，请重试';
    } finally {
        uploadForm.uploading = false;
    }
};
</script>

<template>
    <section class="grid h-screen grid-rows-[var(--header-height)_1fr_auto_var(--footer-height)] bg-[var(--bg-page)]">
        <header
            class="sticky top-0 z-10 h-[var(--header-height)] border-b border-[rgba(15,23,42,0.06)] bg-[#eef1f6] backdrop-blur-[6px]"
        >
            <div
                class="mx-auto flex h-full w-full max-w-[900px] items-center justify-between gap-[12px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))] max-[720px]:pl-[8px] max-[720px]:pr-[calc(8px+var(--scrollbar-w))]"
            >
                <div class="flex items-center gap-[10px] max-[720px]:flex-col max-[720px]:items-start">
                    <div class="relative flex items-center gap-[8px] font-semibold">
                        <label class="min-w-[48px] text-[12px] text-[var(--text-secondary)]">模型</label>
                        <div
                            ref="modelSelectRef"
                            class="inline-flex min-h-[36px] min-w-[200px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                            :class="models.length === 0 ? 'cursor-not-allowed opacity-70' : ''"
                            @click="toggleModelDropdown"
                        >
                            <span class="font-bold text-[var(--text-primary)]">{{ currentModelLabel }}</span>
                            <span
                                class="text-[var(--text-secondary)] transition-transform duration-200"
                                :class="{ 'rotate-180': modelDropdownOpen }"
                            >
                                ⌄
                            </span>
                        </div>
                        <div
                            v-if="modelDropdownOpen && models.length > 0"
                            class="absolute left-0 top-[calc(100%+6px)] z-[15] w-full rounded-[12px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_40px_rgba(15,23,42,0.12)] max-h-[240px] overflow-y-auto"
                        >
                            <div
                                v-for="item in models"
                                :key="item.value"
                                class="flex cursor-pointer items-center justify-between rounded-[10px] px-[12px] py-[10px] text-[var(--text-primary)] transition-colors duration-150 hover:bg-[#f5f7fb]"
                                :class="item.value === currentModel ? 'bg-[#e8f1ff] text-[var(--accent-color)] font-bold' : ''"
                                @click.stop="selectModel(item.value)"
                            >
                                <span>{{ item.label }}</span>
                                <span v-if="item.value === currentModel" class="text-[13px]">✓</span>
                            </div>
                        </div>
                    </div>

                    <div class="relative flex items-center gap-[8px] font-semibold">
                        <label class="min-w-[48px] text-[12px] text-[var(--text-secondary)]">知识库</label>
                        <div
                            ref="ragSelectRef"
                            class="inline-flex min-h-[36px] min-w-[200px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                            @click="toggleRagDropdown"
                        >
                            <span class="font-bold text-[var(--text-primary)]">
                                {{ ragTags.find((t) => t.value === currentRagTag)?.label || '不使用知识库' }}
                            </span>
                            <span
                                class="text-[var(--text-secondary)] transition-transform duration-200"
                                :class="{ 'rotate-180': ragDropdownOpen }"
                            >
                                ⌄
                            </span>
                        </div>
                        <div
                            v-if="ragDropdownOpen"
                            class="absolute left-0 top-[calc(100%+6px)] z-[15] w-full rounded-[12px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_40px_rgba(15,23,42,0.12)] max-h-[240px] overflow-y-auto"
                        >
                            <div
                                v-for="item in ragTags"
                                :key="item.value || 'empty'"
                                class="flex cursor-pointer items-center justify-between rounded-[10px] px-[12px] py-[10px] text-[var(--text-primary)] transition-colors duration-150 hover:bg-[#f5f7fb]"
                                :class="item.value === currentRagTag ? 'bg-[#e8f1ff] text-[var(--accent-color)] font-bold' : ''"
                                @click.stop="selectRag(item.value)"
                            >
                                <span>{{ item.label }}</span>
                                <span v-if="item.value === currentRagTag" class="text-[13px]">✓</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="flex gap-[10px] max-[720px]:flex-wrap">
                    <button
                        class="inline-flex h-[36px] items-center justify-center rounded-[12px] border border-[var(--border-color)] bg-white px-[14px] py-[9px] font-bold leading-[1.1] text-[var(--text-primary)] transition-all duration-200 hover:bg-[#f7f9fc]"
                        type="button"
                        @click="openUpload"
                    >
                        上传知识库
                    </button>
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
            <div
                ref="messageScrollRef"
                class="h-full overflow-y-auto bg-[var(--bg-page)] py-[16px] scroll-smooth [scrollbar-gutter:auto]"
                @scroll="handleScroll"
            >
                <div class="mx-auto w-full max-w-[900px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
                    <div class="flex w-full flex-col gap-[14px]">
                        <div v-if="messages.length === 0" class="flex flex-col items-start gap-[12px] py-[60px] text-[var(--text-primary)]">
                            <div
                                v-for="(line, idx) in typewriterState.lines"
                                :key="idx"
                                class="text-[20px] font-extrabold leading-[1.4] tracking-[0.3px] text-transparent opacity-[0.92] bg-[linear-gradient(120deg,rgba(47,124,246,0.68),rgba(15,23,42,0.76))] bg-clip-text"
                            >
                                {{ line }}
                                <span v-if="typewriterState.playing && idx === typewriterState.lineIndex" class="text-[var(--accent-color)] animate-caret">
                                    ▍
                                </span>
                            </div>
                        </div>
                        <div
                            v-for="message in messages"
                            :key="message.id"
                            class="flex w-full"
                            :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
                        >
                            <div
                                class="relative max-w-full w-fit rounded-[14px] px-[14px] py-[12px] shadow-[0_12px_30px_rgba(27,36,55,0.08)] border"
                                :class="[
                                    message.role === 'user'
                                        ? 'bg-[linear-gradient(135deg,#e5f4ff,#eaf4ff)] border-[#c5e2ff]'
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
                                    <div class="text-[13px] text-[var(--text-secondary)]">思考中</div>
                                </div>
                                <div v-else class="whitespace-pre-wrap leading-[1.6]" :class="message.error ? 'text-[#d14343]' : ''">
                                    {{ getContent(message) }}
                                </div>
                                <div
                                    v-if="message.error"
                                    class="mt-[6px] rounded-[8px] bg-[#fff3f3] px-[10px] py-[8px] text-[13px] text-[#d14343]"
                                >
                                    {{ message.error.message || '请求失败' }}
                                </div>
                            </div>
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
                        placeholder="输入问题，Enter 发送，Shift+Enter 换行"
                        :disabled="sending"
                        @keydown="handleKeydown"
                    ></textarea>
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
                            :disabled="sending || !inputValue.trim()"
                            @click="sendMessage"
                        >
                            {{ sending ? '生成中…' : '发送' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <footer class="h-[var(--footer-height)] border-t border-[rgba(15,23,42,0.06)] bg-[#eef1f6] backdrop-blur-[6px]">
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
                        <label class="font-semibold text-[var(--text-primary)]">模式</label>
                        <div class="flex gap-[12px]">
                            <label class="flex cursor-pointer items-center gap-[6px] rounded-[12px] border border-[var(--border-color)] bg-[#f7f9fc] px-[12px] py-[10px]">
                                <input v-model="settingsForm.type" type="radio" value="complete" />
                                <span>complete</span>
                            </label>
                            <label class="flex cursor-pointer items-center gap-[6px] rounded-[12px] border border-[var(--border-color)] bg-[#f7f9fc] px-[12px] py-[10px]">
                                <input v-model="settingsForm.type" type="radio" value="stream" />
                                <span>stream</span>
                            </label>
                        </div>
                    </div>
                    <div class="flex flex-col gap-[8px]">
                        <label for="temperature" class="font-semibold text-[var(--text-primary)]">temperature</label>
                        <input
                            id="temperature"
                            v-model.number="settingsForm.temperature"
                            type="number"
                            min="0"
                            max="2"
                            step="0.1"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                        />
                    </div>
                    <div class="flex flex-col gap-[8px]">
                        <label for="topk" class="font-semibold text-[var(--text-primary)]">topK</label>
                        <input
                            id="topk"
                            v-model.number="settingsForm.topK"
                            type="number"
                            min="1"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                        />
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

        <div v-if="showUploadModal" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="closeUpload">
            <div class="w-full max-w-[640px] rounded-[16px] border border-[var(--border-color)] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.2)]">
                <div class="flex items-center justify-between border-b border-[var(--border-color)] px-[18px] pt-[14px] pb-[10px]">
                    <div class="text-[18px] font-bold">上传知识库</div>
                    <button class="text-[22px] text-[var(--text-secondary)]" type="button" @click="closeUpload">×</button>
                </div>
                <div class="flex flex-col gap-[14px] px-[18px] py-[14px]">
                    <div class="flex gap-[10px]">
                        <button
                            class="rounded-[10px] border border-[var(--border-color)] bg-[#f7f9fc] px-[12px] py-[8px] font-semibold text-[var(--text-secondary)]"
                            :class="uploadForm.mode === 'file' ? 'border-[#c7dcff] bg-[#e8f1ff] text-[var(--accent-color)]' : ''"
                            type="button"
                            @click="uploadForm.mode = 'file'"
                        >
                            上传文件
                        </button>
                        <button
                            class="rounded-[10px] border border-[var(--border-color)] bg-[#f7f9fc] px-[12px] py-[8px] font-semibold text-[var(--text-secondary)]"
                            :class="uploadForm.mode === 'git' ? 'border-[#c7dcff] bg-[#e8f1ff] text-[var(--accent-color)]' : ''"
                            type="button"
                            @click="uploadForm.mode = 'git'"
                        >
                            上传 Git 仓库
                        </button>
                    </div>

                    <div v-if="uploadForm.mode === 'file'" class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">知识库标签</label>
                        <div class="flex items-center gap-[10px]">
                            <div class="relative flex items-center gap-[8px] font-semibold">
                                <div
                                    ref="uploadRagSelectRef"
                                    class="inline-flex min-h-[36px] min-w-[160px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                                    @click.stop="toggleUploadRagDropdown"
                                >
                                    <span class="font-bold text-[var(--text-primary)]">
                                        {{ ragTags.find((t) => t.value === uploadForm.selectedTag)?.label || '选择标签' }}
                                    </span>
                                    <span
                                        class="text-[var(--text-secondary)] transition-transform duration-200"
                                        :class="{ 'rotate-180': uploadRagDropdownOpen }"
                                    >
                                        ⌄
                                    </span>
                                </div>
                                <div
                                    v-if="uploadRagDropdownOpen"
                                    class="absolute left-0 top-[calc(100%+6px)] z-[15] w-full rounded-[12px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_40px_rgba(15,23,42,0.12)] max-h-[240px] overflow-y-auto"
                                >
                                    <div
                                        v-for="item in ragTags"
                                        :key="item.value || 'empty-upload'"
                                        class="flex cursor-pointer items-center justify-between rounded-[10px] px-[12px] py-[10px] text-[var(--text-primary)] transition-colors duration-150 hover:bg-[#f5f7fb]"
                                        :class="item.value === uploadForm.selectedTag ? 'bg-[#e8f1ff] text-[var(--accent-color)] font-bold' : ''"
                                        @click.stop="() => selectUploadRag(item.value)"
                                    >
                                        <span>{{ item.label }}</span>
                                        <span v-if="item.value === uploadForm.selectedTag" class="text-[13px]">✓</span>
                                    </div>
                                </div>
                            </div>
                            <input
                                v-model="uploadForm.tagInput"
                                type="text"
                                class="flex-1 rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                                placeholder="或输入新标签"
                            />
                        </div>
                    </div>

                    <div v-if="uploadForm.mode === 'file'" class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">上传文件（仅 .txt）</label>
                        <label class="block cursor-pointer rounded-[12px] border border-dashed border-[var(--border-color)] bg-[#f8fafc] px-[12px] py-[14px]">
                            <input
                                type="file"
                                accept=".txt,text/plain"
                                class="hidden"
                                @change="handleFileChange"
                            />
                            <div v-if="uploadForm.fileName" class="flex items-center justify-between font-semibold text-[var(--text-primary)]">
                                <span class="name">{{ uploadForm.fileName }}</span>
                                <span class="text-[13px] text-[var(--text-secondary)]">{{ uploadForm.fileSize }}</span>
                            </div>
                            <div v-else class="text-[14px] text-[var(--text-secondary)]">点击选择或拖拽 TXT 文件</div>
                        </label>
                    </div>

                    <div v-if="uploadForm.mode === 'git'" class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">Git 仓库地址</label>
                        <input
                            v-model="uploadForm.repoUrl"
                            type="text"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                            placeholder="https://github.com/xxx/xxx.git"
                        />
                    </div>

                    <div v-if="uploadForm.mode === 'git'" class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">Git 用户名</label>
                        <input
                            v-model="uploadForm.repoUsername"
                            type="text"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                            placeholder="请输入用户名"
                        />
                    </div>

                    <div v-if="uploadForm.mode === 'git'" class="flex flex-col gap-[8px]">
                        <label class="font-semibold text-[var(--text-primary)]">Git 密码</label>
                        <input
                            v-model="uploadForm.repoPassword"
                            type="password"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                            placeholder="请输入密码或 token"
                        />
                    </div>

                    <div v-if="uploadForm.error" class="rounded-[8px] bg-[#fff3f3] px-[10px] py-[8px] text-[13px] text-[#d14343]">
                        {{ uploadForm.error }}
                    </div>
                </div>
                <div class="flex justify-end gap-[10px] border-t border-[var(--border-color)] px-[18px] pt-[12px] pb-[16px]">
                    <button
                        class="inline-flex items-center justify-center rounded-[12px] border border-[var(--border-color)] bg-white px-[14px] py-[9px] font-bold leading-[1.1] text-[var(--text-primary)] transition-all duration-200 hover:bg-[#f7f9fc]"
                        type="button"
                        @click="closeUpload"
                    >
                        取消
                    </button>
                    <button
                        class="inline-flex items-center justify-center rounded-[12px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[9px] font-bold leading-[1.1] text-white transition-all duration-200 hover:brightness-95 disabled:cursor-not-allowed disabled:opacity-70"
                        type="button"
                        :disabled="uploadForm.uploading"
                        @click="handleUpload"
                    >
                        {{ uploadForm.uploading ? '上传中…' : '上传' }}
                    </button>
                </div>
            </div>
        </div>
    </section>
</template>
