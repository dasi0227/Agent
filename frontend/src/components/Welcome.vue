<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { queryAgentList, queryChatMcps, queryChatModels } from '../request/api';
import { normalizeError } from '../request/request';
import { useWelcomeLaunchStore } from '../router/pinia';
import { createTypewriter, DEFAULT_TYPEWRITER_SEGMENTS } from '../utils/TypeWriter';

const router = useRouter();
const welcomeLaunchStore = useWelcomeLaunchStore();

const sending = ref(false);
const actionError = ref('');

const typewriterState = reactive({
    lines: [],
    lineIndex: 0,
    playing: false
});

const chatActions = [
    {
        key: 'chat-self-intro',
        label: '你是什么模型，介绍一下你自己',
        prompt: '你是什么模型，介绍一下你自己'
    },
    {
        key: 'chat-java',
        label: '如何利用 java 编写 helloworld 代码',
        prompt: '如何利用 java 编写 helloworld 代码'
    },
    {
        key: 'chat-student',
        label: 'Dasi 是什么大学的学生',
        prompt: 'Dasi 是什么大学的学生'
    },
    {
        key: 'chat-fire-notice',
        label: '发送消防演练通知到企业微信',
        prompt:
            '请你起草一则消防演练通知，并发送到企业微信（文本类型）。通知内容需要包含：演练时间与地点、参与范围、集合与疏散路线、演练纪律、应急联系人与注意事项。'
    }
];

const workActions = [
    {
        key: 'work-weather',
        label: '请你查询广州今日天气，生成一份穿衣提醒，发送邮件到邮箱 1740929297@qq.com，并且使用 HTML 进行美化。',
        prompt: '请你查询广州今日天气，生成一份穿衣提醒，发送邮件到邮箱 1740929297@qq.com，并且使用 HTML 进行美化。',
        agentId: 'agent_weather'
    },
    {
        key: 'work-nba',
        label: '联网搜索最近最新的 NBA 交易新闻，筛选 2 条并各写 1-2 句摘要，然后发送到企业微信（文本类型）。',
        prompt: '联网搜索最近最新的 NBA 交易新闻，筛选 2 条并各写 1-2 句摘要，然后发送到企业微信（文本类型）。',
        agentId: 'agent_web'
    },
    {
        key: 'work-springai',
        label: '请你写一篇关于 SpringAI 的趣味小故事，先发布到 CSDN 上，然后将这篇文章通告到企业微信（文本卡片类型）。',
        prompt: '请你写一篇关于 SpringAI 的趣味小故事，先发布到 CSDN 上，然后将这篇文章通告到企业微信（文本卡片类型）。',
        agentId: 'agent_article'
    }
];

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

const normalizeOptions = (list, idKey, nameKey) => {
    const normalized = (Array.isArray(list) ? list : [])
        .map((item) => {
            if (!item || typeof item !== 'object') return null;
            const value = item[idKey] || item.id || '';
            const label = item[nameKey] || item.name || value;
            if (!value) return null;
            return { value, label };
        })
        .filter(Boolean);

    const seen = new Set();
    return normalized.filter((item) => {
        if (seen.has(item.value)) return false;
        seen.add(item.value);
        return true;
    });
};

const buildSessionTitle = (prompt) => (prompt || '新会话').slice(0, 20) || '新会话';

const pickModelByKeyword = (models, keyword) => {
    if (!keyword) return models[0] || null;
    const lower = keyword.toLowerCase();
    return (
        models.find((item) => (item.value || '').toLowerCase().includes(lower)) ||
        models.find((item) => (item.label || '').toLowerCase().includes(lower)) ||
        null
    );
};

const pickMcpByKeywords = (mcps, keywords = []) => {
    if (!keywords.length) return null;
    const lowers = keywords.map((k) => k.toLowerCase());
    return (
        mcps.find((item) =>
            lowers.some((kw) => (item.value || '').toLowerCase().includes(kw) || (item.label || '').toLowerCase().includes(kw))
        ) || null
    );
};

const triggerChatAction = async (action) => {
    const modelResp = await queryChatModels();
    const rawModels = Array.isArray(modelResp?.result)
        ? modelResp.result
        : Array.isArray(modelResp)
          ? modelResp
          : Array.isArray(modelResp?.data)
            ? modelResp.data
            : [];

    const models = rawModels
        .map((item) => {
            if (typeof item === 'string') {
                return { value: item, label: item };
            }
            if (!item || typeof item !== 'object') return null;
            const value = item.clientId || item.modelId || item.id || '';
            const label = item.modelName || item.name || value;
            if (!value) return null;
            return { value, label };
        })
        .filter(Boolean);

    if (!models.length) {
        throw new Error('暂无可用 CLIENT，请先在后台配置 client');
    }

    let selectedModel = models[0];
    let selectedMcpIds = [];
    let ragTag = '';

    if (action?.key === 'chat-self-intro') {
        const target = pickModelByKeyword(models, 'doubao-seed-1.8');
        if (!target) {
            throw new Error('未找到 doubao-seed-1.8 模型，请先在后台配置对应 client');
        }
        selectedModel = target;
    } else if (action?.key === 'chat-java') {
        const target = pickModelByKeyword(models, 'deepseek-chat');
        if (!target) {
            throw new Error('未找到 deepseek-chat 模型，请先在后台配置对应 client');
        }
        selectedModel = target;
    } else if (action?.key === 'chat-student') {
        const target = pickModelByKeyword(models, 'qwen-plus');
        if (!target) {
            throw new Error('未找到 qwen-plus 模型，请先在后台配置对应 client');
        }
        selectedModel = target;
        ragTag = 'dasi-info';
    } else if (action?.key === 'chat-fire-notice') {
        const target = pickModelByKeyword(models, 'glm-5');
        if (!target) {
            throw new Error('未找到 glm-5 模型，请先在后台配置 glm-5 对应 client');
        }
        selectedModel = target;

        const mcpResp = await queryChatMcps();
        const mcpList = Array.isArray(mcpResp?.result)
            ? mcpResp.result
            : Array.isArray(mcpResp)
              ? mcpResp
              : Array.isArray(mcpResp?.data)
                ? mcpResp.data
                : [];
        const mcps = normalizeOptions(mcpList, 'mcpId', 'mcpName');
        const wecomMcp = pickMcpByKeywords(mcps, ['企业微信', 'wecom', 'wxwork']);
        if (!wecomMcp) {
            throw new Error('未找到企业微信 MCP，请先在后台配置企业微信相关 MCP');
        }
        selectedMcpIds = [wecomMcp.value];
    }

    welcomeLaunchStore.setTask({
        type: 'chat',
        prompt: action.prompt,
        sessionTitle: buildSessionTitle(action.prompt),
        clientId: selectedModel.value,
        mcpIdList: selectedMcpIds,
        ragTag
    });

    router.push('/chat');
};

const triggerWorkAction = async (action) => {
    const agentResp = await queryAgentList();
    const agentList = Array.isArray(agentResp?.result)
        ? agentResp.result
        : Array.isArray(agentResp)
          ? agentResp
          : Array.isArray(agentResp?.data)
            ? agentResp.data
            : [];

    const agents = normalizeOptions(agentList, 'agentId', 'agentName');
    if (!agents.length) {
        throw new Error('暂无可用 AGENT，请先在后台配置 agent');
    }
    const requestedAgentId = action?.agentId || '';
    const matchedAgent =
        (requestedAgentId && agents.find((item) => item.value === requestedAgentId)) || agents[0] || null;
    if (!matchedAgent?.value) {
        throw new Error('未找到可用 AGENT');
    }
    if (requestedAgentId && matchedAgent.value !== requestedAgentId) {
        throw new Error(`未找到指定 AGENT：${requestedAgentId}`);
    }

    welcomeLaunchStore.setTask({
        type: 'work',
        prompt: action.prompt,
        sessionTitle: buildSessionTitle(action.prompt),
        agentId: matchedAgent.value
    });

    router.push('/work');
};

const runAction = async (kind, action) => {
    if (!action || sending.value) return;
    sending.value = true;
    actionError.value = '';
    try {
        if (kind === 'chat') {
            await triggerChatAction(action);
        } else {
            await triggerWorkAction(action);
        }
    } catch (error) {
        const friendly = normalizeError(error);
        const rawMessage = friendly.message || (error instanceof Error ? error.message : '执行失败');
        actionError.value = rawMessage;
    } finally {
        sending.value = false;
    }
};

onMounted(() => {
    typewriterController.start();
});

onBeforeUnmount(() => {
    typewriterController.stop();
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="overflow-y-auto bg-[var(--bg-page)] py-[18px]">
            <div class="mx-auto flex w-full max-w-[1060px] flex-col gap-[20px] pt-[52px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))] max-[720px]:pt-[34px]">
                <div class="flex h-[292px] flex-col items-start gap-[14px] overflow-hidden pt-[16px] text-[var(--text-primary)] max-[720px]:h-[220px] max-[720px]:pt-[10px]">
                    <div
                        v-for="(line, idx) in typewriterState.lines"
                        :key="idx"
                        class="text-[32px] font-extrabold leading-[1.4] tracking-[0.3px] text-transparent opacity-[0.94] bg-clip-text max-[720px]:text-[28px]"
                        :style="{ backgroundImage: 'var(--typewriter-gradient)' }"
                    >
                        {{ line }}
                        <span v-if="typewriterState.playing && idx === typewriterState.lineIndex" class="text-[var(--accent-color)] animate-caret">▍</span>
                    </div>
                </div>

                <div class="grid items-stretch gap-[18px] md:grid-cols-2">
                    <div class="flex h-full flex-col rounded-[18px] border border-[var(--border-color)] bg-[var(--surface-1)] p-[18px] shadow-[var(--shadow-soft)]">
                        <div class="mb-[14px] text-[18px] font-bold text-[var(--text-primary)] max-[720px]:text-[16px]">Chat 类型</div>
                        <div class="flex flex-1 flex-col justify-between gap-[10px]">
                            <button
                                v-for="item in chatActions"
                                :key="item.key"
                                class="flex min-h-[56px] items-center rounded-[14px] border border-[var(--border-color)] bg-white px-[16px] py-[13px] text-left text-[17px] text-[var(--text-primary)] transition-all duration-200 hover:bg-[#f5f7fb] disabled:cursor-not-allowed disabled:opacity-70 max-[720px]:min-h-[48px] max-[720px]:text-[15px]"
                                :disabled="sending"
                                type="button"
                                @click="runAction('chat', item)"
                            >
                                <span class="line-clamp-2">{{ item.label }}</span>
                            </button>
                        </div>
                    </div>

                    <div class="flex h-full flex-col rounded-[18px] border border-[var(--border-color)] bg-[var(--surface-1)] p-[18px] shadow-[var(--shadow-soft)]">
                        <div class="mb-[14px] text-[18px] font-bold text-[var(--text-primary)] max-[720px]:text-[16px]">Work 类型</div>
                        <div class="flex flex-1 flex-col justify-between gap-[10px]">
                            <button
                                v-for="item in workActions"
                                :key="item.key"
                                class="flex min-h-[56px] items-center rounded-[14px] border border-[var(--border-color)] bg-white px-[16px] py-[13px] text-left text-[17px] text-[var(--text-primary)] transition-all duration-200 hover:bg-[#f5f7fb] disabled:cursor-not-allowed disabled:opacity-70 max-[720px]:min-h-[48px] max-[720px]:text-[15px]"
                                :disabled="sending"
                                type="button"
                                @click="runAction('work', item)"
                            >
                                <span class="whitespace-normal break-words pr-[8px] leading-[1.45]">{{ item.label }}</span>
                            </button>
                        </div>
                    </div>
                </div>

                <div v-if="actionError" class="rounded-[12px] border border-[rgba(239,68,68,0.35)] bg-[rgba(254,242,242,0.8)] px-[12px] py-[10px] text-[13px] text-[#b91c1c]">
                    {{ actionError }}
                </div>
            </div>
        </div>

        <footer class="h-[var(--footer-height)] border-t border-[rgba(15,23,42,0.06)] bg-[var(--surface-3)] backdrop-blur-[6px]">
            <div
                class="mx-auto flex h-full w-full max-w-[1060px] items-center justify-between pl-[24px] pr-[calc(24px+var(--scrollbar-w))] text-[13px] text-[var(--text-secondary)] max-[720px]:pl-[8px] max-[720px]:pr-[calc(8px+var(--scrollbar-w))]"
            >
                <span>© 2025 Dasi</span>
                <span>内容为 AI 生成，仅供参考，请注意甄别</span>
            </div>
        </footer>
    </section>
</template>
