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

const models = ref([{ label: 'deepseek-r1:1.5b', value: 'deepseek-r1:1.5b' }]);
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
        ragTags.value = unique.map((item, idx) => ({
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
        const unique = Array.from(new Set(list.filter((item) => typeof item === 'string' && item)));
        if (unique.length > 0) {
            models.value = unique.map((item) => ({ label: item, value: item }));
            if (!unique.includes(currentModel.value)) {
                currentModel.value = unique[0];
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
    }
};

const sendMessage = async () => {
    const content = inputValue.value.trim();
    if (!content || sending.value) return;
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
            model: currentModel.value,
            message: content,
            ragTag: currentRagTag.value,
            signal: controller.signal
        });
        const text = pickContentFromResult(response);
        const { think, answer } = parseThinkText(text);
        chatStore.updateAssistantMessage(assistantMessage.id, {
            content: answer || '（无内容）',
            think,
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
        chatStore.updateAssistantMessage(assistantMessage.id, { pending: false });
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
            model: currentModel.value,
            message: content,
            ragTag: currentRagTag.value,
            signal: controller.signal,
            onData: (payload) => {
                const data = payload?.result || payload;
                const token = pickContentFromResult(data);
                const finishReason = data?.finishReason || payload?.finishReason;
                if (token) {
                    applyStreamToken(accumulator, token);
                    chatStore.updateAssistantMessage(assistantMessage.id, {
                        content: accumulator.answer,
                        think: accumulator.think,
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

const hasThink = (message) => Boolean(message?.think && message.think.trim());
const getThink = (message) => (message?.think ? message.think.trim() : '');
const getContent = (message) => (message?.content ? message.content.toString().trimStart() : '');

const toggleModelDropdown = () => {
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
    <section class="chat">
        <header class="chat-header">
            <div class="content-container header-inner">
                <div class="select-group">
                    <div class="model-select">
                        <span class="select-label">模型</span>
                        <div ref="modelSelectRef" class="model-trigger" @click="toggleModelDropdown">
                            <span class="model-text">{{ currentModel }}</span>
                            <span class="arrow" :class="{ open: modelDropdownOpen }">⌄</span>
                        </div>
                        <div v-if="modelDropdownOpen" class="model-dropdown scrollable">
                            <div
                                v-for="item in models"
                                :key="item.value"
                                :class="['model-option', { active: item.value === currentModel }]"
                                @click.stop="selectModel(item.value)"
                            >
                                <span>{{ item.label }}</span>
                                <span v-if="item.value === currentModel" class="check">✓</span>
                            </div>
                        </div>
                    </div>

                    <div class="model-select">
                        <span class="select-label">知识库</span>
                        <div ref="ragSelectRef" class="model-trigger" @click="toggleRagDropdown">
                            <span class="model-text">
                                {{ ragTags.find((t) => t.value === currentRagTag)?.label || '不使用知识库' }}
                            </span>
                            <span class="arrow" :class="{ open: ragDropdownOpen }">⌄</span>
                        </div>
                        <div v-if="ragDropdownOpen" class="model-dropdown scrollable">
                            <div
                                v-for="item in ragTags"
                                :key="item.value || 'empty'"
                                :class="['model-option', { active: item.value === currentRagTag }]"
                                @click.stop="selectRag(item.value)"
                            >
                                <span>{{ item.label }}</span>
                                <span v-if="item.value === currentRagTag" class="check">✓</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="header-actions">
                    <button class="btn btn-ghost" type="button" @click="openUpload">上传知识库</button>
                    <button class="btn btn-primary" type="button" @click="openSettings">
                        回答设置
                    </button>
                </div>
            </div>
        </header>

        <div class="message-area">
            <div ref="messageScrollRef" class="message-scroll" @scroll="handleScroll">
                <div class="content-container">
                    <div class="messages">
                        <div v-if="messages.length === 0" class="welcome-state">
                            <div
                                v-for="(line, idx) in typewriterState.lines"
                                :key="idx"
                                class="welcome-line"
                            >
                                {{ line }}
                                <span
                                    v-if="typewriterState.playing && idx === typewriterState.lineIndex"
                                    class="cursor"
                                >
                                    ▍
                                </span>
                            </div>
                        </div>
                        <div
                            v-for="message in messages"
                            :key="message.id"
                            :class="['message-row', message.role]"
                        >
                            <div class="bubble" :class="{ pending: message.pending }">
                                <div v-if="hasThink(message)" class="think">
                                    {{ getThink(message) }}
                                </div>
                                <div
                                    v-if="message.pending && message.role === 'assistant' && !message.content"
                                    class="pending-block"
                                >
                                    <div class="typing">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                    <div class="pending-text">思考中</div>
                                </div>
                                <div v-else class="text" :class="{ error: message.error }">
                                    {{ getContent(message) }}
                                </div>
                                <div v-if="message.error" class="error-tip">
                                    {{ message.error.message || '请求失败' }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="input-area">
            <div class="content-container input-inner">
                <div class="input-card">
                    <textarea
                        v-model="inputValue"
                        class="input-textarea"
                        rows="3"
                        placeholder="输入问题，Enter 发送，Shift+Enter 换行"
                        :disabled="sending"
                        @keydown="handleKeydown"
                    ></textarea>
                    <div class="input-actions">
                        <button
                            class="btn btn-ghost"
                            type="button"
                            :disabled="!sending"
                            @click="handleStop"
                        >
                            停止生成
                        </button>
                        <button
                            class="btn btn-primary"
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

        <footer class="chat-footer">
            <div class="content-container footer-inner">
                <span>© 2025 Dasi</span>
                <span>内容为 AI 生成，仅供参考，请注意甄别</span>
            </div>
        </footer>

        <div v-if="showSettings" class="modal-mask" @click.self="showSettings = false">
            <div class="modal">
                <div class="modal-header">
                    <div class="title">回答设置</div>
                    <button class="close" type="button" @click="showSettings = false">×</button>
                </div>
                <div class="modal-body">
                    <div class="form-item">
                        <label>模式</label>
                        <div class="options">
                            <label class="option">
                                <input v-model="settingsForm.type" type="radio" value="complete" />
                                <span>complete</span>
                            </label>
                            <label class="option">
                                <input v-model="settingsForm.type" type="radio" value="stream" />
                                <span>stream</span>
                            </label>
                        </div>
                    </div>
                    <div class="form-item">
                        <label for="temperature">temperature</label>
                        <input
                            id="temperature"
                            v-model.number="settingsForm.temperature"
                            type="number"
                            min="0"
                            max="2"
                            step="0.1"
                        />
                    </div>
                    <div class="form-item">
                        <label for="topk">topK</label>
                        <input id="topk" v-model.number="settingsForm.topK" type="number" min="1" />
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-ghost" type="button" @click="showSettings = false">
                        取消
                    </button>
                    <button class="btn btn-primary" type="button" @click="saveSettings">保存</button>
                </div>
            </div>
        </div>

        <div v-if="showUploadModal" class="modal-mask" @click.self="closeUpload">
            <div class="modal large">
                <div class="modal-header">
                    <div class="title">上传知识库</div>
                    <button class="close" type="button" @click="closeUpload">×</button>
                </div>
                <div class="modal-body">
                    <div class="upload-mode">
                        <button
                            class="mode-btn"
                            :class="{ active: uploadForm.mode === 'file' }"
                            type="button"
                            @click="uploadForm.mode = 'file'"
                        >
                            上传文件
                        </button>
                        <button
                            class="mode-btn"
                            :class="{ active: uploadForm.mode === 'git' }"
                            type="button"
                            @click="uploadForm.mode = 'git'"
                        >
                            上传 Git 仓库
                        </button>
                    </div>

                    <div v-if="uploadForm.mode === 'file'" class="form-item">
                        <label>知识库标签</label>
                        <div class="tag-row">
                            <div class="model-select compact">
                                <div
                                    ref="uploadRagSelectRef"
                                    class="model-trigger"
                                    @click.stop="toggleUploadRagDropdown"
                                >
                                    <span class="model-text">
                                        {{ ragTags.find((t) => t.value === uploadForm.selectedTag)?.label || '选择标签' }}
                                    </span>
                                    <span class="arrow" :class="{ open: uploadRagDropdownOpen }">⌄</span>
                                </div>
                                <div v-if="uploadRagDropdownOpen" class="model-dropdown scrollable">
                                    <div
                                        v-for="item in ragTags"
                                        :key="item.value || 'empty-upload'"
                                        :class="[
                                            'model-option',
                                            { active: item.value === uploadForm.selectedTag }
                                        ]"
                                        @click.stop="
                                            () => {
                                                selectUploadRag(item.value);
                                            }
                                        "
                                    >
                                        <span>{{ item.label }}</span>
                                        <span v-if="item.value === uploadForm.selectedTag" class="check">✓</span>
                                    </div>
                                </div>
                            </div>
                            <input
                                v-model="uploadForm.tagInput"
                                type="text"
                                class="tag-input"
                                placeholder="或输入新标签"
                            />
                        </div>
                    </div>

                    <div v-if="uploadForm.mode === 'file'" class="form-item">
                        <label>上传文件（仅 .txt）</label>
                        <label class="upload-box">
                            <input
                                type="file"
                                accept=".txt,text/plain"
                                class="file-input"
                                @change="handleFileChange"
                            />
                            <div v-if="uploadForm.fileName" class="file-info">
                                <span class="name">{{ uploadForm.fileName }}</span>
                                <span class="size">{{ uploadForm.fileSize }}</span>
                            </div>
                            <div v-else class="placeholder">点击选择或拖拽 TXT 文件</div>
                        </label>
                    </div>

                    <div v-if="uploadForm.mode === 'git'" class="form-item">
                        <label>Git 仓库地址</label>
                        <input
                            v-model="uploadForm.repoUrl"
                            type="text"
                            class="tag-input"
                            placeholder="https://github.com/xxx/xxx.git"
                        />
                    </div>

                    <div v-if="uploadForm.mode === 'git'" class="form-item">
                        <label>Git 用户名</label>
                        <input
                            v-model="uploadForm.repoUsername"
                            type="text"
                            class="tag-input"
                            placeholder="请输入用户名"
                        />
                    </div>

                    <div v-if="uploadForm.mode === 'git'" class="form-item">
                        <label>Git 密码</label>
                        <input
                            v-model="uploadForm.repoPassword"
                            type="password"
                            class="tag-input"
                            placeholder="请输入密码或 token"
                        />
                    </div>

                    <div v-if="uploadForm.error" class="error-tip dense">
                        {{ uploadForm.error }}
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-ghost" type="button" @click="closeUpload">取消</button>
                    <button
                        class="btn btn-primary"
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
.chat {
    display: grid;
    grid-template-rows: var(--header-height) 1fr auto var(--footer-height);
    height: 100vh;
    background: var(--bg-page);
}

.chat-header {
    height: var(--header-height);
    position: sticky;
    top: 0;
    z-index: 10;
    border-bottom: 1px solid rgba(15, 23, 42, 0.06);
    background: #eef1f6;
    backdrop-filter: blur(6px);
}

.header-inner {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.select-group {
    display: flex;
    align-items: center;
    gap: 10px;
}

.model-select {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 4px;
    font-weight: 600;
    position: relative;
}

.select-label {
    font-size: 16px;
    color: var(--text-secondary);
    min-width: 30px;
}

.model-trigger {
    min-width: 200px;
    padding: 8px 12px;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    background: #fff;
    display: inline-flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    cursor: pointer;
    box-shadow: var(--shadow-soft);
    min-height: 36px;
}

.model-select.compact .model-trigger {
    min-width: 160px;
}

.model-text {
    font-weight: 700;
    color: var(--text-primary);
}

.arrow {
    transition: transform 0.2s ease;
    color: var(--text-secondary);
}

.arrow.open {
    transform: rotate(180deg);
}

.model-dropdown {
    position: absolute;
    top: calc(100% + 6px);
    left: 0;
    width: 100%;
    background: #fff;
    border: 1px solid var(--border-color);
    border-radius: 12px;
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
    padding: 6px;
    z-index: 15;
}

.model-dropdown.scrollable {
    max-height: 240px;
    overflow-y: auto;
}

.model-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    color: var(--text-primary);
    transition: background 0.15s ease;
}

.model-option:hover {
    background: #f5f7fb;
}

.model-option.active {
    background: #e8f1ff;
    color: var(--accent-color);
    font-weight: 700;
}

.model-option .check {
    font-size: 13px;
}

.header-actions {
    display: flex;
    gap: 10px;
}

.header-actions .danger {
    border-color: #f1c0c0;
    color: #d14343;
    background: #fff5f5;
}

.header-actions .danger:hover {
    background: #ffecec;
}

.message-area {
    overflow: hidden;
    background: var(--bg-page);
}

.message-scroll {
    height: 100%;
    overflow-y: auto;
    padding: 16px 0;
    scroll-behavior: smooth;
    background: var(--bg-page);
    scrollbar-gutter: auto;
}

.messages {
    display: flex;
    flex-direction: column;
    gap: 14px;
    width: 100%;
}

.message-row {
    display: flex;
    width: 100%;
    justify-content: flex-start;
}

.message-row.user {
    justify-content: flex-end;
}

.bubble {
    max-width: 100%;
    width: fit-content;
    background: #fff;
    border-radius: 14px;
    padding: 12px 14px;
    border: 1px solid var(--border-color);
    position: relative;
}

.bubble.pending {
    border-style: dashed;
}

.message-row.user .bubble {
    background: linear-gradient(135deg, #e5f4ff, #eaf4ff);
    border-color: #c5e2ff;
}

.think {
    font-size: 12px;
    color: #7b8190;
    margin-bottom: 6px;
    background: #f3f4f6;
    border-radius: 8px;
    padding: 8px 10px;
    border: 1px dashed var(--border-color);
}

.text {
    white-space: pre-wrap;
    line-height: 1.6;
}

.text.error {
    color: #d14343;
}

.error-tip {
    margin-top: 6px;
    padding: 8px 10px;
    border-radius: 8px;
    background: #fff3f3;
    color: #d14343;
    font-size: 13px;
}

.error-tip.dense {
    margin-top: 0;
}

.typing {
    display: inline-flex;
    gap: 4px;
    align-items: center;
}

.typing span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #7b8190;
    animation: blink 1.2s infinite ease-in-out;
}

.typing span:nth-child(2) {
    animation-delay: 0.2s;
}

.typing span:nth-child(3) {
    animation-delay: 0.4s;
}

.pending-block {
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.pending-text {
    font-size: 13px;
    color: var(--text-secondary);
}

.welcome-state {
    display: flex;
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    padding: 60px 0;
    color: var(--text-primary);
}

.welcome-line {
    font-size: 20px;
    font-weight: 800;
    letter-spacing: 0.3px;
    line-height: 1.4;
    background: linear-gradient(120deg, rgba(47, 124, 246, 0.68), rgba(15, 23, 42, 0.76));
    background-clip: text;
    color: transparent;
    opacity: 0.92;
}

.cursor {
    color: var(--accent-color);
    animation: caretBlink 1s steps(1) infinite;
}

@keyframes blink {
    0%,
    80%,
    100% {
        opacity: 0.3;
    }
    40% {
        opacity: 1;
    }
}

.input-area {
    background: var(--bg-page);
}

.input-inner {
    display: flex;
    flex-direction: column;
    gap: 0;
    padding: 12px 0;
}

.input-card {
    background: #fff;
    border-radius: 16px;
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-soft);
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.input-textarea {
    width: 100%;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    padding: 12px 14px;
    font-size: 14px;
    resize: none;
    background: #fff;
    box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.06);
}

.input-textarea:disabled {
    background: #f4f6fb;
}

.input-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.input-actions .btn {
    height: 36px;
}

.chat-footer {
    height: var(--footer-height);
    border-top: 1px solid rgba(15, 23, 42, 0.06);
    background: #eef1f6;
    backdrop-filter: blur(6px);
}

.footer-inner {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    color: var(--text-secondary);
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
    width: min(520px, 100%);
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 50px rgba(15, 23, 42, 0.2);
    border: 1px solid var(--border-color);
}

.modal.large {
    width: min(640px, 100%);
}

.modal.small {
    width: min(380px, 100%);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 18px 10px;
    border-bottom: 1px solid var(--border-color);
}

.modal-header .title {
    font-weight: 700;
    font-size: 18px;
}

.close {
    border: none;
    background: transparent;
    font-size: 22px;
    cursor: pointer;
    color: var(--text-secondary);
}

.modal-body {
    padding: 14px 18px;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.form-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.form-item label {
    font-weight: 600;
    color: var(--text-primary);
}

.form-item input[type='number'],
.form-item input[type='text'] {
    padding: 10px 12px;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    font-size: 14px;
}

.options {
    display: flex;
    gap: 12px;
}

.option {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 12px;
    border: 1px solid var(--border-color);
    border-radius: 12px;
    background: #f7f9fc;
    cursor: pointer;
}

.modal-footer {
    padding: 12px 18px 16px;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    border-top: 1px solid var(--border-color);
}

.btn {
    padding: 9px 14px;
    border-radius: 12px;
    font-weight: 700;
    cursor: pointer;
    border: 1px solid transparent;
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    line-height: 1.1;
}

.btn:disabled {
    cursor: not-allowed;
    opacity: 0.7;
}

.btn-primary {
    background: var(--accent-color);
    color: #fff;
    border-color: var(--accent-color);
}

.btn-primary:hover:not(:disabled) {
    filter: brightness(0.95);
}

.btn-ghost {
    background: #fff;
    border-color: var(--border-color);
    color: var(--text-primary);
}

.btn-ghost:hover:not(:disabled) {
    background: #f7f9fc;
}

.btn-primary.danger {
    background: #d14343;
    border-color: #d14343;
    box-shadow: 0 14px 30px rgba(209, 67, 67, 0.35);
}

.confirm-text {
    font-size: 15px;
    color: var(--text-primary);
}

.tag-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

.upload-mode {
    display: flex;
    gap: 10px;
}

.mode-btn {
    padding: 8px 12px;
    border-radius: 10px;
    border: 1px solid var(--border-color);
    background: #f7f9fc;
    color: var(--text-secondary);
    cursor: pointer;
    font-weight: 600;
}

.mode-btn.active {
    background: #e8f1ff;
    color: var(--accent-color);
    border-color: #c7dcff;
}

.tag-input {
    flex: 1;
    padding: 10px 12px;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    font-size: 14px;
}

.upload-box {
    border: 1px dashed var(--border-color);
    border-radius: 12px;
    padding: 14px 12px;
    background: #f8fafc;
    cursor: pointer;
    display: block;
}

.file-input {
    display: none;
}

.file-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
    color: var(--text-primary);
}

.file-info .size {
    color: var(--text-secondary);
    font-size: 13px;
}

.placeholder {
    color: var(--text-secondary);
    font-size: 14px;
}

@keyframes caretBlink {
    0%,
    50% {
        opacity: 1;
    }
    50.1%,
    100% {
        opacity: 0;
    }
}

@media (max-width: 720px) {
    .chat {
        grid-template-rows: auto 1fr auto auto;
    }

    .header-inner,
    .footer-inner {
        padding: 0 8px;
    }

    .header-actions {
        gap: 6px;
        flex-wrap: wrap;
    }

    .select-group {
        flex-direction: column;
        align-items: flex-start;
    }

    .model-trigger {
        min-width: 0;
        width: 100%;
    }
}
</style>
