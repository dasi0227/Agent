<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { marked } from 'marked';
import hljs from 'highlight.js';
import DOMPurify from 'dompurify';
import {
    fetchComplete,
    fetchStream,
    pickContentFromResult,
    dispatchArmory,
    queryChatModels,
    queryChatMcps,
    queryRagTags,
    uploadRagFile,
    uploadRagGit
} from '../request/api';
import { normalizeError } from '../request/request';
import { applyStreamToken, createStreamAccumulator, parseThinkText } from '../utils/StringUtil';
import { createTypewriter, DEFAULT_TYPEWRITER_SEGMENTS } from '../utils/TypeWriter';
import { useChatStore, useSettingsStore } from '../router/pinia';

const chatStore = useChatStore();
const settingsStore = useSettingsStore();

const models = ref([]);
const mcpTools = ref([]);
const ragTags = ref([{ label: '不使用知识库', value: '' }]);
const selectedMcpIds = ref([]);

const messageScrollRef = ref(null);
const modelSelectRef = ref(null);
const mcpSelectRef = ref(null);
const ragSelectRef = ref(null);
const uploadRagSelectRef = ref(null);
const inputValue = ref('');
const showSettings = ref(false);
const showUploadModal = ref(false);
const isAtBottom = ref(true);
const modelDropdownOpen = ref(false);
const mcpDropdownOpen = ref(false);
const ragDropdownOpen = ref(false);
const uploadRagDropdownOpen = ref(false);
const copiedMessageId = ref(null);
const copyTimer = ref(null);

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

const normalizeSettingValue = (value) => {
    if (value === null || value === undefined || value === '') return '';
    return value;
};

const settingsForm = reactive({
    type: settingsStore.type,
    temperature: normalizeSettingValue(settingsStore.temperature),
    presencePenalty: normalizeSettingValue(settingsStore.presencePenalty),
    maxCompletionTokens: normalizeSettingValue(settingsStore.maxCompletionTokens)
});

const settingsErrors = reactive({
    temperature: '',
    presencePenalty: '',
    maxCompletionTokens: ''
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

const currentModel = computed({
    get: () => settingsStore.model,
    set: (value) => settingsStore.updateSettings({ model: value })
});

const currentModelLabel = computed(() => {
    const match = models.value.find((item) => item.value === currentModel.value);
    if (match?.label) return match.label;
    return models.value.length === 0 ? '暂无模型' : currentModel.value;
});

const currentMcpLabel = computed(() => {
    if (!selectedMcpIds.value.length) return '不使用工具';
    if (selectedMcpIds.value.length === 1) {
        const match = mcpTools.value.find((item) => item.value === selectedMcpIds.value[0]);
        return match?.label || '不使用工具';
    }
    return '多个工具';
});

const currentRagTag = computed({
    get: () => settingsStore.ragTag,
    set: (value) => settingsStore.updateSettings({ ragTag: value })
});

const messages = computed(() => chatStore.currentMessages);
const currentChatSessionId = computed(() => chatStore.currentChat?.sessionId || '');
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

const fetchMcpTools = async () => {
    try {
        const resp = await queryChatMcps();
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
                const mcpId = item.mcpId || item.id || '';
                const mcpName = item.mcpName || item.name || mcpId;
                const mcpDesc = item.mcpDesc || item.desc || '';
                if (!mcpId) return null;
                return { label: mcpName || mcpId, value: mcpId, desc: mcpDesc };
            })
            .filter(Boolean);
        const seen = new Set();
        const unique = normalized.filter((item) => {
            if (seen.has(item.value)) return false;
            seen.add(item.value);
            return true;
        });
        mcpTools.value = unique;
        if (selectedMcpIds.value.length) {
            const valid = new Set(unique.map((item) => item.value));
            selectedMcpIds.value = selectedMcpIds.value.filter((id) => valid.has(id));
        }
    } catch (error) {
        console.warn('获取 MCP 工具失败', error);
    }
};

onMounted(() => {
    scrollToBottom(false);
    attachListeners();
    fetchTags();
    fetchModels();
    fetchMcpTools();
});

watch(currentModel, () => {
    fetchTags();
});

onBeforeUnmount(() => {
    chatStore.stopCurrentRequest();
    detachListeners();
    stopTypewriter();
    if (copyTimer.value) {
        clearTimeout(copyTimer.value);
    }
});

const handleKeydown = (event) => {
    if (event.key === 'Enter' && event.metaKey) {
        event.preventDefault();
        sendMessage();
        return;
    }
    if (event.key === 'Escape') {
        modelDropdownOpen.value = false;
        mcpDropdownOpen.value = false;
        ragDropdownOpen.value = false;
        uploadRagDropdownOpen.value = false;
    }
};

const renderMarkdown = (text) => {
    if (!text) return '';
    return DOMPurify.sanitize(marked.parse(text), { ADD_ATTR: ['class'] });
};

const copyText = async (text) => {
    if (!text) return;
    if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(text);
        return;
    }
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'absolute';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
};

const handleCopy = async (message) => {
    const text = getContent(message);
    if (!text) return;
    try {
        await copyText(text);
        copiedMessageId.value = message.id;
        if (copyTimer.value) {
            clearTimeout(copyTimer.value);
        }
        copyTimer.value = setTimeout(() => {
            copiedMessageId.value = null;
        }, 1200);
    } catch (error) {
        console.warn('复制失败', error);
    }
};

const extractStreamParts = (payload) => {
    const data = payload?.result ?? payload;
    const finishReason = data?.finishReason || payload?.finishReason || null;
    if (typeof data === 'string') {
        return { token: data, finishReason };
    }
    if (typeof data === 'number' || typeof data === 'boolean') {
        return { token: String(data), finishReason };
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

const syncSettingsFormWithStore = () => {
    settingsForm.type = settingsStore.type;
    settingsForm.temperature = normalizeSettingValue(settingsStore.temperature);
    settingsForm.presencePenalty = normalizeSettingValue(settingsStore.presencePenalty);
    settingsForm.maxCompletionTokens = normalizeSettingValue(settingsStore.maxCompletionTokens);
};

const validateSettingsBeforeSend = () => {
    syncSettingsFormWithStore();
    const valid = validateSettings(true);
    if (!valid) {
        showSettings.value = true;
    }
    return valid;
};

const normalizeMarkdownSpacing = (text) => {
    if (!text) return '';
    return text
        .split('```')
        .map((segment, index) => {
            if (index % 2 === 1) return segment;
            return segment
                .replace(/(^|\n)(\s*)(#{1,6})(\r?\n)(\S)/g, '$1$2$3 $5')
                .replace(/(^|\n)(\s*)([-*+])(\r?\n)(\S)/g, '$1$2$3 $5')
                .replace(/(^|\n)(\s*)(\d+\.)(\r?\n)(\S)/g, '$1$2$3 $5')
                .replace(/(^|\n)(\s*)(#{1,6})(\S)/g, '$1$2$3 $4')
                .replace(/(^|\n)(\s*)([-*+])(\S)/g, '$1$2$3 $4')
                .replace(/(^|\n)(\s*)(\d+\.)(\S)/g, '$1$2$3 $4');
        })
        .join('```');
};

const sendMessage = async () => {
    const content = inputValue.value.trim();
    if (!content || sending.value || !currentModel.value) return;
    if (!validateSettingsBeforeSend()) return;
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

const buildChatRequestPayload = (userMessage) => ({
    clientId: currentModel.value,
    userMessage,
    temperature: settingsStore.temperature ?? undefined,
    presencePenalty: settingsStore.presencePenalty ?? undefined,
    maxCompletionTokens: settingsStore.maxCompletionTokens ?? undefined,
    mcpIdList: selectedMcpIds.value.length ? [...selectedMcpIds.value] : [],
    sessionId: currentChatSessionId.value,
    ragTag: currentRagTag.value
});

const runComplete = async (content, controller) => {
    const assistantMessage = chatStore.addAssistantMessage({ pending: true, content: '', think: '' });
    try {
        const response = await fetchComplete({
            ...buildChatRequestPayload(content),
            signal: controller.signal
        });
        const text = pickContentFromResult(response);
        const { answer } = parseThinkText(text);
        const normalized = normalizeMarkdownSpacing(answer);
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: normalized || '（无内容）',
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
        const normalized = normalizeMarkdownSpacing(accumulator.answer);
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: normalized,
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
            ...buildChatRequestPayload(content),
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

const parseOptionalNumber = (value, options) => {
    if (value === '' || value === null || value === undefined) {
        return { value: null, error: '' };
    }
    const num = Number(value);
    if (Number.isNaN(num)) {
        return { value: null, error: `${options.label} 请输入数字` };
    }
    if (options.integer && !Number.isInteger(num)) {
        return { value: null, error: `${options.label} 必须是整数` };
    }
    if (num < options.min || num > options.max) {
        return { value: null, error: `${options.label} 取值范围 ${options.min} ~ ${options.max}` };
    }
    return { value: num, error: '' };
};

const validateSettings = (showErrors = true) => {
    const temperatureResult = parseOptionalNumber(settingsForm.temperature, {
        label: 'temperature',
        min: 0,
        max: 1,
        integer: false
    });
    const presenceResult = parseOptionalNumber(settingsForm.presencePenalty, {
        label: 'presence penalty',
        min: 0,
        max: 1,
        integer: false
    });
    const maxTokenResult = parseOptionalNumber(settingsForm.maxCompletionTokens, {
        label: 'max token',
        min: 1,
        max: 8192,
        integer: true
    });

    if (showErrors) {
        settingsErrors.temperature = temperatureResult.error;
        settingsErrors.presencePenalty = presenceResult.error;
        settingsErrors.maxCompletionTokens = maxTokenResult.error;
    }

    return !temperatureResult.error && !presenceResult.error && !maxTokenResult.error;
};

const buildChatSettingsPayload = () => {
    const temperatureResult = parseOptionalNumber(settingsForm.temperature, {
        label: 'temperature',
        min: 0,
        max: 1,
        integer: false
    });
    const presenceResult = parseOptionalNumber(settingsForm.presencePenalty, {
        label: 'presence penalty',
        min: 0,
        max: 1,
        integer: false
    });
    const maxTokenResult = parseOptionalNumber(settingsForm.maxCompletionTokens, {
        label: 'max token',
        min: 1,
        max: 8192,
        integer: true
    });

    return {
        temperature: temperatureResult.value ?? undefined,
        presencePenalty: presenceResult.value ?? undefined,
        maxCompletionTokens: maxTokenResult.value ?? undefined
    };
};

const openSettings = () => {
    syncSettingsFormWithStore();
    settingsErrors.temperature = '';
    settingsErrors.presencePenalty = '';
    settingsErrors.maxCompletionTokens = '';
    showSettings.value = true;
};

const saveSettings = () => {
    if (!validateSettings(true)) {
        return;
    }
    const payload = buildChatSettingsPayload();
    settingsStore.updateSettings({
        type: settingsForm.type,
        temperature: payload.temperature ?? null,
        presencePenalty: payload.presencePenalty ?? null,
        maxCompletionTokens: payload.maxCompletionTokens ?? null
    });
    showSettings.value = false;
};

const getContent = (message) => (message?.content ? message.content.toString() : '');

const toggleModelDropdown = () => {
    if (models.value.length === 0) return;
    mcpDropdownOpen.value = false;
    ragDropdownOpen.value = false;
    modelDropdownOpen.value = !modelDropdownOpen.value;
};

const toggleMcpDropdown = () => {
    modelDropdownOpen.value = false;
    ragDropdownOpen.value = false;
    mcpDropdownOpen.value = !mcpDropdownOpen.value;
};

const toggleMcpSelection = (value) => {
    const idx = selectedMcpIds.value.indexOf(value);
    if (idx >= 0) {
        selectedMcpIds.value.splice(idx, 1);
    } else {
        selectedMcpIds.value.push(value);
    }
};

const selectModel = async (value) => {
    currentModel.value = value;
    modelDropdownOpen.value = false;
    try {
        await dispatchArmory({ armoryType: 'chat', armoryId: value });
    } catch (error) {
        console.warn('绑定 Chat armory 失败', error);
    }
};

const toggleRagDropdown = () => {
    modelDropdownOpen.value = false;
    mcpDropdownOpen.value = false;
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
    const inMcp = mcpSelectRef.value && mcpSelectRef.value.contains(target);
    const inRag = ragSelectRef.value && ragSelectRef.value.contains(target);
    const inUploadRag = uploadRagSelectRef.value && uploadRagSelectRef.value.contains(target);
    if (!inModel) modelDropdownOpen.value = false;
    if (!inMcp) mcpDropdownOpen.value = false;
    if (!inRag) ragDropdownOpen.value = false;
    if (!inUploadRag) uploadRagDropdownOpen.value = false;
};

const handleEscClose = (event) => {
    if (event.key === 'Escape') {
        modelDropdownOpen.value = false;
        mcpDropdownOpen.value = false;
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
    const lowerName = file.name.toLowerCase();
    const isTxt = lowerName.endsWith('.txt');
    const isMd = lowerName.endsWith('.md');
    if (!isTxt && !isMd) {
        uploadForm.error = '仅支持 .txt 或 .md 文件';
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
            uploadForm.error = '请选择 txt 或 md 文件';
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
                class="flex h-full w-full items-center justify-between gap-[12px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))] max-[720px]:pl-[8px] max-[720px]:pr-[calc(8px+var(--scrollbar-w))]"
            >
                <div class="flex items-center gap-[10px]">
                    <div class="relative flex items-center gap-[14px] font-semibold">
                        <label class="w-[36px] text-[14px] text-[var(--text-secondary)] text-right">CLIENT</label>
                        <div
                            ref="modelSelectRef"
                            class="inline-flex min-h-[36px] min-w-[200px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                            :class="models.length === 0 ? 'cursor-not-allowed opacity-70' : ''"
                            @click="toggleModelDropdown"
                        >
                            <span class="font-bold text-[var(--text-primary)]">{{ currentModelLabel }}</span>
                            <span
                                class="caret transition-transform duration-150"
                                :class="modelDropdownOpen ? 'caret-open' : 'caret-closed'"
                            />
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

                    <div class="relative flex items-center gap-[6px] font-semibold">
                        <label class="w-[36px] text-[14px] text-[var(--text-secondary)] text-right">RAG</label>
                        <div
                            ref="ragSelectRef"
                            class="inline-flex min-h-[36px] min-w-[200px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                            @click="toggleRagDropdown"
                        >
                            <span class="font-bold text-[var(--text-primary)]">
                                {{ ragTags.find((t) => t.value === currentRagTag)?.label || '不使用知识库' }}
                            </span>
                            <span
                                class="caret transition-transform duration-150"
                                :class="ragDropdownOpen ? 'caret-open' : 'caret-closed'"
                            />
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

                    <div class="relative flex items-center gap-[6px] font-semibold">
                        <label class="w-[36px] text-[14px] text-[var(--text-secondary)] text-right">MCP</label>
                        <div
                            ref="mcpSelectRef"
                            class="inline-flex min-h-[36px] min-w-[200px] cursor-pointer items-center justify-between gap-[10px] rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] shadow-[0_12px_30px_rgba(27,36,55,0.08)]"
                            @click="toggleMcpDropdown"
                        >
                            <span class="font-bold text-[var(--text-primary)]">{{ currentMcpLabel }}</span>
                            <span
                                class="caret transition-transform duration-150"
                                :class="mcpDropdownOpen ? 'caret-open' : 'caret-closed'"
                            />
                        </div>
                        <div
                            v-if="mcpDropdownOpen"
                            class="absolute left-0 top-[calc(100%+6px)] z-[15] w-full rounded-[12px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_40px_rgba(15,23,42,0.12)] max-h-[240px] overflow-y-auto"
                            @click.stop
                        >
                            <div
                                v-if="mcpTools.length === 0"
                                class="flex items-center justify-between rounded-[10px] px-[12px] py-[10px] text-[var(--text-secondary)]"
                            >
                                <span>暂无工具</span>
                            </div>
                            <div
                                v-for="item in mcpTools"
                                :key="item.value"
                                class="flex cursor-pointer items-center justify-between rounded-[10px] px-[12px] py-[10px] text-[var(--text-primary)] transition-colors duration-150 hover:bg-[#f5f7fb]"
                                :class="selectedMcpIds.includes(item.value) ? 'bg-[#e8f1ff] text-[var(--accent-color)] font-bold' : ''"
                                @click.stop="toggleMcpSelection(item.value)"
                            >
                                <span>{{ item.label }}</span>
                                <span v-if="selectedMcpIds.includes(item.value)" class="text-[13px]">✓</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="flex justify-end gap-[10px] max-[720px]:flex-wrap">
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
                            <div class="flex max-w-full flex-col gap-[6px]" :class="message.role === 'user' ? 'items-end' : 'items-start'">
                                <div
                                    class="relative w-fit max-w-[720px] rounded-[14px] px-[14px] py-[12px] shadow-[0_12px_30px_rgba(27,36,55,0.08)] border"
                                    :class="[
                                        message.error
                                            ? 'bg-[linear-gradient(135deg,#ffe4e4,#ffd6d6)] border-[#f3b6b6] text-[#b91c1c]'
                                            : message.role === 'user'
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
                                    <div
                                        v-else-if="message.role === 'user' || message.pending"
                                        class="whitespace-pre-wrap leading-[1.6]"
                                        :class="message.error ? 'text-[#d14343]' : ''"
                                    >
                                        {{ getContent(message) }}
                                    </div>
                                    <div
                                        v-else
                                        class="markdown-body leading-[1.6] [&_pre]:overflow-auto [&_pre]:rounded-[10px] [&_pre]:bg-[#0f172a] [&_pre]:p-[12px] [&_pre]:text-[#e2e8f0] [&_code]:rounded-[6px] [&_code]:bg-[#f1f5f9] [&_code]:px-[6px] [&_code]:py-[2px] [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:rounded-none"
                                        :class="message.error ? 'text-[#d14343]' : ''"
                                        v-html="renderMarkdown(getContent(message))"
                                    ></div>
                                </div>
                                <div
                                    class="relative flex items-center gap-[6px]"
                                    :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
                                >
                                    <button
                                        type="button"
                                        class="flex h-[22px] w-[22px] items-center justify-center rounded-[6px] border border-[rgba(15,23,42,0.1)] bg-white text-[var(--text-secondary)] shadow-[0_6px_16px_rgba(15,23,42,0.12)] transition-colors duration-150 hover:text-[var(--accent-color)] disabled:cursor-not-allowed disabled:opacity-60"
                                        :disabled="!getContent(message)"
                                        aria-label="复制"
                                        @click.stop="handleCopy(message)"
                                    >
                                        <svg viewBox="0 0 24 24" class="h-[14px] w-[14px]" fill="none" stroke="currentColor" stroke-width="1.8">
                                            <path d="M8 8h9a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2v-9a2 2 0 0 1 2-2Z" />
                                            <path d="M6 16H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                                        </svg>
                                    </button>
                                    <div
                                        v-if="copiedMessageId === message.id"
                                        class="pointer-events-none absolute top-1/2 -translate-y-1/2 whitespace-nowrap rounded-[6px] border border-[rgba(15,23,42,0.1)] bg-white px-[8px] py-[4px] text-[12px] text-[var(--text-secondary)] shadow-[0_8px_20px_rgba(15,23,42,0.12)]"
                                        :class="message.role === 'user' ? 'right-[30px]' : 'left-[30px]'"
                                    >
                                        已复制
                                    </div>
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
                        <label for="temperature" class="font-semibold text-[var(--text-primary)]">temperature（随机性）</label>
                        <input
                            id="temperature"
                            v-model="settingsForm.temperature"
                            type="number"
                            min="0"
                            max="1"
                            step="0.1"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                            @input="settingsErrors.temperature = ''"
                            @blur="validateSettings(true)"
                        />
                        <div v-if="settingsErrors.temperature" class="text-[12px] text-[#d14343]">
                            {{ settingsErrors.temperature }}
                        </div>
                    </div>
                    <div class="flex flex-col gap-[8px]">
                        <label for="presencePenalty" class="font-semibold text-[var(--text-primary)]">presence penalty（减少重复）</label>
                        <input
                            id="presencePenalty"
                            v-model="settingsForm.presencePenalty"
                            type="number"
                            min="0"
                            max="1"
                            step="0.1"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                            @input="settingsErrors.presencePenalty = ''"
                            @blur="validateSettings(true)"
                        />
                        <div v-if="settingsErrors.presencePenalty" class="text-[12px] text-[#d14343]">
                            {{ settingsErrors.presencePenalty }}
                        </div>
                    </div>
                    <div class="flex flex-col gap-[8px]">
                        <label for="maxTokens" class="font-semibold text-[var(--text-primary)]">max token（回复长度）</label>
                        <input
                            id="maxTokens"
                            v-model="settingsForm.maxCompletionTokens"
                            type="number"
                            min="1"
                            max="8192"
                            step="1"
                            class="rounded-[12px] border border-[var(--border-color)] px-[12px] py-[10px] text-[14px]"
                            @input="settingsErrors.maxCompletionTokens = ''"
                            @blur="validateSettings(true)"
                        />
                        <div v-if="settingsErrors.maxCompletionTokens" class="text-[12px] text-[#d14343]">
                            {{ settingsErrors.maxCompletionTokens }}
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
                            :class="uploadForm.mode === 'file' ? 'border-[#4f8cff] bg-[#e8f1ff] text-[var(--accent-color)] shadow-[0_10px_24px_rgba(47,124,246,0.2)]' : ''"
                            type="button"
                            @click="uploadForm.mode = 'file'"
                        >
                            上传文件
                        </button>
                        <button
                            class="rounded-[10px] border border-[var(--border-color)] bg-[#f7f9fc] px-[12px] py-[8px] font-semibold text-[var(--text-secondary)]"
                            :class="uploadForm.mode === 'git' ? 'border-[#4f8cff] bg-[#e8f1ff] text-[var(--accent-color)] shadow-[0_10px_24px_rgba(47,124,246,0.2)]' : ''"
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
                        <label class="font-semibold text-[var(--text-primary)]">上传文件（仅 .txt / .md）</label>
                        <label class="block cursor-pointer rounded-[12px] border border-dashed border-[var(--border-color)] bg-[#f8fafc] px-[12px] py-[14px]">
                            <input
                                type="file"
                                accept=".txt,.md,text/plain,text/markdown"
                                class="hidden"
                                @change="handleFileChange"
                            />
                            <div v-if="uploadForm.fileName" class="flex items-center justify-between font-semibold text-[var(--text-primary)]">
                                <span class="name">{{ uploadForm.fileName }}</span>
                                <span class="text-[13px] text-[var(--text-secondary)]">{{ uploadForm.fileSize }}</span>
                            </div>
                            <div v-else class="text-[14px] text-[var(--text-secondary)]">点击选择或拖拽 TXT / MD 文件</div>
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
