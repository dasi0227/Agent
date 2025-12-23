<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { fetchComplete, fetchStream, pickContentFromResult } from '../request/api';
import { normalizeError } from '../request/request';
import { applyStreamToken, createStreamAccumulator, parseThinkText } from '../utils/parseThink';
import { createTypewriter, DEFAULT_TYPEWRITER_SEGMENTS } from '../utils/typewriter';
import { useChatStore, useSettingsStore } from '../router/pinia';

const chatStore = useChatStore();
const settingsStore = useSettingsStore();

const models = ref([
    { label: 'deepseek-r1:1.5b', value: 'deepseek-r1:1.5b' }
]);

const messageScrollRef = ref(null);
const inputValue = ref('');
const showSettings = ref(false);
const showDeleteConfirm = ref(false);
const isAtBottom = ref(true);
const modelDropdownOpen = ref(false);
const modelSelectRef = ref(null);
const typewriterState = reactive({
    lines: [],
    lineIndex: 0,
    playing: false
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

const messages = computed(() => chatStore.currentMessages);
const sending = computed(() => chatStore.sending);
const hasMessages = computed(() => messages.value.length > 0);

const handleScroll = () => {
    const el = messageScrollRef.value;
    if (!el) {
        return;
    }
    const distance = el.scrollHeight - el.scrollTop - el.clientHeight;
    isAtBottom.value = distance < 80;
};

const scrollToBottom = (smooth = true) => {
    nextTick(() => {
        const el = messageScrollRef.value;
        if (!el) {
            return;
        }
        el.scrollTo({
            top: el.scrollHeight,
            behavior: smooth ? 'smooth' : 'auto'
        });
    });
};

watch(
    messages,
    () => {
        if (isAtBottom.value) {
            scrollToBottom(true);
        }
    },
    { deep: true }
);

watch(
    () => chatStore.currentChatId,
    () => {
        nextTick(() => scrollToBottom(false));
    }
);

const startTypewriter = () => {
    if (hasMessages.value) {
        return;
    }
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

onMounted(() => {
    scrollToBottom(false);
    attachClickOutside();
});

onBeforeUnmount(() => {
    chatStore.stopCurrentRequest();
    detachClickOutside();
    stopTypewriter();
});

const handleKeydown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
};

const sendMessage = async () => {
    const content = inputValue.value.trim();
    if (!content || sending.value) {
        return;
    }
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
    const assistantMessage = chatStore.addAssistantMessage({
        pending: true,
        content: '',
        think: ''
    });
    try {
        const response = await fetchComplete({
            model: currentModel.value,
            message: content,
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
    const assistantMessage = chatStore.addAssistantMessage({
        pending: true,
        content: '',
        think: ''
    });
    let closed = false;

    const finishStream = () => {
        if (closed) {
            return;
        }
        closed = true;
        chatStore.updateAssistantMessage(assistantMessage.id, { pending: false });
        chatStore.setSending(false);
        chatStore.setAbortController(null);
        scrollToBottom(true);
    };

    const handleError = (error) => {
        if (closed) {
            return;
        }
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
                    if (isAtBottom.value) {
                        scrollToBottom(true);
                    }
                }
                if (finishReason === 'stop') {
                    finishStream();
                }
            },
            onError: handleError,
            onDone: finishStream
        });
    } catch (error) {
        handleError(error);
    }
};

const handleStop = () => {
    if (!sending.value) {
        return;
    }
    chatStore.stopCurrentRequest();
    const lastAssistant = [...messages.value]
        .filter((item) => item.role === 'assistant')
        .pop();
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

const confirmDelete = () => {
    showDeleteConfirm.value = true;
};

const handleDelete = () => {
    showDeleteConfirm.value = false;
    chatStore.deleteChat();
};

const toggleModelDropdown = () => {
    modelDropdownOpen.value = !modelDropdownOpen.value;
};

const selectModel = (value) => {
    currentModel.value = value;
    modelDropdownOpen.value = false;
};

const handleClickOutside = (event) => {
    const target = event.target;
    const trigger = modelSelectRef.value;
    if (trigger && !trigger.contains(target)) {
        modelDropdownOpen.value = false;
    }
};

const attachClickOutside = () => {
    document.addEventListener('click', handleClickOutside);
};

const detachClickOutside = () => {
    document.removeEventListener('click', handleClickOutside);
};
</script>

<template>
    <section class="chat">
        <header class="chat-header">
            <div class="content-container header-inner">
                <div class="model-select">
                    <label for="model">模型</label>
                    <div ref="modelSelectRef" class="model-trigger" @click="toggleModelDropdown">
                        <span class="model-text">{{ currentModel }}</span>
                        <span class="arrow" :class="{ open: modelDropdownOpen }">⌄</span>
                    </div>
                    <div v-if="modelDropdownOpen" class="model-dropdown">
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
                <div class="header-actions">
                    <button class="btn btn-ghost danger" type="button" @click="confirmDelete">
                        删除会话
                    </button>
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
                                <div v-if="message.think" class="think">
                                    {{ message.think }}
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
                                    {{ message.content }}
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
    gap: 16px;
}

.model-select {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 600;
    position: relative;
}

.model-trigger {
    min-width: 220px;
    padding: 10px 12px;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    background: #fff;
    display: inline-flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    cursor: pointer;
    box-shadow: var(--shadow-soft);
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
    top: calc(100% + 8px);
    left: 0;
    width: 100%;
    background: #fff;
    border: 1px solid var(--border-color);
    border-radius: 12px;
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
    padding: 6px;
    z-index: 15;
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
    padding: 22px 0;
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
    box-shadow: var(--shadow-soft);
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
    padding: 80px 0;
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
    padding: 16px 28px 16px 18px;
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
    height: 38px;
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

.modal.small {
    width: min(380px, 100%);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 20px 12px;
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
    padding: 16px 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
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

.form-item input[type='number'] {
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
    padding: 14px 20px 18px;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    border-top: 1px solid var(--border-color);
}

.btn {
    padding: 10px 14px;
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
    }

    .model-select select {
        min-width: 160px;
    }
}
</style>
