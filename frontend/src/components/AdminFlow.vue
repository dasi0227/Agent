<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import SidebarAdmin from './SidebarAdmin.vue';
import arrowIcon from '../assets/arrow.svg';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { useAuthStore } from '../router/pinia';
import { adminAgentList, flowAgent, flowClients, flowDelete, flowInsert } from '../request/api';
import { normalizeError } from '../request/request';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const currentKey = ref('flow');
const menuGroups = adminMenuGroups;

const ROLE_MAP = {
    step: ['INSPECTOR', 'PLANNER', 'RUNNER', 'REPLIER'],
    loop: ['ANALYZER', 'PERFORMER', 'SUPERVISOR', 'SUMMARIZER']
};

const loading = reactive({
    agents: false,
    clients: false,
    flows: false,
    replacing: false
});

const agents = ref([]);
const allClients = ref([]);
const selectedAgent = ref(null);
const agentFlows = ref([]);
const activeSlot = ref(null);

const clientDetailMap = computed(() => {
    const map = new Map();
    (allClients.value || []).forEach((c) => map.set(c.clientId, c));
    return map;
});

const errorDialog = reactive({ visible: false, message: '' });
const confirmDialog = reactive({ visible: false, roleLabel: '', fromClient: '', toClient: '', onConfirm: null });
const promptDialog = reactive({ visible: false, title: '', content: '' });

const pickData = (resp, msg = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || msg);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const agentStatusDot = (status) => (status === 1 ? 'bg-emerald-500' : 'bg-rose-500');

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) router.push(target.path);
};

const showError = (msg) => {
    errorDialog.visible = true;
    errorDialog.message = msg || '操作失败';
};
const closeError = () => {
    errorDialog.visible = false;
    errorDialog.message = '';
};

const openConfirm = (payload) => Object.assign(confirmDialog, { ...payload, visible: true });
const closeConfirm = () => {
    confirmDialog.visible = false;
    confirmDialog.onConfirm = null;
};
const openPromptDialog = (slot) => {
    const prompt = (slot?.flow?.flowPrompt || '').trim();
    if (!prompt) return;
    promptDialog.visible = true;
    promptDialog.title = `${slot.roleLabel} · Flow Prompt`;
    promptDialog.content = prompt;
};
const closePromptDialog = () => {
    promptDialog.visible = false;
    promptDialog.title = '';
    promptDialog.content = '';
};

const loadClients = async () => {
    loading.clients = true;
    try {
        const res = await flowClients();
        allClients.value = pickData(res, '获取 Client 失败') || [];
    } catch (err) {
        showError(normalizeError(err).message);
        allClients.value = [];
    } finally {
        loading.clients = false;
    }
};

const loadAgents = async () => {
    loading.agents = true;
    try {
        const res = await adminAgentList({});
        agents.value = pickData(res, '获取 Agent 失败') || [];
    } catch (err) {
        showError(normalizeError(err).message);
        agents.value = [];
    } finally {
        loading.agents = false;
    }
};

const loadFlows = async (agentId) => {
    loading.flows = true;
    try {
        const res = await flowAgent(agentId);
        agentFlows.value = pickData(res, '获取 Flow 失败') || [];
    } catch (err) {
        showError(normalizeError(err).message);
        agentFlows.value = [];
    } finally {
        loading.flows = false;
    }
};

const roleOrder = computed(() => ROLE_MAP[selectedAgent.value?.agentType || 'step'] || ROLE_MAP.step);

const slotList = computed(() =>
    roleOrder.value.map((roleLabel, idx) => {
        const seq = idx + 1;
        const matched =
            agentFlows.value.find((f) => f.flowSeq === seq) ||
            agentFlows.value.find((f) => (f.clientRole || '').toUpperCase() === roleLabel);
        return { seq, roleLabel, flow: matched || null };
    })
);

const pickedClientIds = computed(() => slotList.value.map((s) => s.flow?.clientId).filter(Boolean));
const candidateClients = computed(() => {
    if (activeSlot.value == null) return [];
    const expectRole = roleOrder.value[activeSlot.value];
    return (allClients.value || []).filter(
        (c) => (c.clientRole || '').toUpperCase() === expectRole && !pickedClientIds.value.includes(c.clientId)
    );
});

const enterDetail = async (agent) => {
    selectedAgent.value = agent;
    activeSlot.value = null;
    await loadFlows(agent.agentId);
    router.replace({ path: '/admin/flow', query: { agentId: agent.agentId } });
};

const openAgentFromRoute = async () => {
    const targetId = String(route.query.agentId || '').trim();
    if (!targetId) return;
    if (!selectedAgent.value) {
        selectedAgent.value = {
            agentId: targetId,
            agentName: targetId,
            agentType: 'step',
            agentStatus: 1
        };
    }
    const agent = agents.value.find((item) => item.agentId === targetId);
    if (agent) {
        await enterDetail(agent);
        return;
    }
    await loadFlows(targetId);
};

const backToGrid = () => {
    selectedAgent.value = null;
    agentFlows.value = [];
    activeSlot.value = null;
    router.replace({ path: '/admin/flow' });
};

const handleChooseSlot = (idx) => {
    activeSlot.value = idx;
};

const getClientDetail = (clientId) => clientDetailMap.value.get(clientId);
const getIdList = (list, key) => (list || []).map((item) => item?.[key]).filter(Boolean);
const isLongPrompt = (prompt) => (prompt || '').trim().length > 20;

const performReplace = async (slot, newClient) => {
    if (!selectedAgent.value) return;
    loading.replacing = true;
    try {
        const existing = slot.flow;
        if (existing?.id) {
            pickData(await flowDelete(existing.id), '删除旧配置失败');
        }
        const prompt = (existing?.flowPrompt && existing.flowPrompt.trim()) || 'auto';
        pickData(
            await flowInsert({
                agentId: selectedAgent.value.agentId,
                clientId: newClient.clientId,
                clientRole: (newClient.clientRole || '').toLowerCase(),
                flowPrompt: prompt,
                flowSeq: slot.seq
            }),
            '新增配置失败'
        );
        await loadFlows(selectedAgent.value.agentId);
        activeSlot.value = null;
    } catch (err) {
        showError(normalizeError(err).message);
    } finally {
        loading.replacing = false;
        closeConfirm();
    }
};

const openReplace = (slot, client) => {
    openConfirm({
        roleLabel: slot.roleLabel,
        fromClient: slot.flow?.clientId || '未配置',
        toClient: client.clientId,
        onConfirm: () => performReplace(slot, client)
    });
};

onMounted(async () => {
    await openAgentFromRoute();
    await Promise.all([loadClients(), loadAgents()]);
    await openAgentFromRoute();
});
</script>

<template>
    <div class="admin-font flex h-screen bg-[#f8fafc]">
        <SidebarAdmin :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">
                    FLOW 管理
                    <span v-if="selectedAgent" class="ml-2 text-[14px] font-normal text-[#64748b]">/ {{ selectedAgent.agentId }}</span>
                </div>
            </header>

            <div class="flex-1 overflow-auto p-6">
                <!-- Agent 网格 -->
                <div v-if="!selectedAgent" class="h-full overflow-auto">
                    <div class="mb-4 text-left text-[14px] text-[#94a3b8]">共 {{ agents.length }} 个 Agent</div>
                    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        <div
                            v-for="agent in agents"
                            :key="agent.agentId"
                            class="agent-card group relative h-[180px] cursor-pointer overflow-hidden rounded-[16px] bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-lg"
                            @click="enterDetail(agent)"
                        >
                            <div class="absolute right-3 top-3 h-3 w-3 rounded-full" :class="agentStatusDot(agent.agentStatus)" />
                            <div class="agent-card-inner">
                                <div class="agent-card-face agent-card-front flex h-full flex-col justify-between p-4">
                                    <div
                                        class="inline-flex w-fit items-center rounded-full px-2.5 py-1 text-[14px] font-semibold uppercase tracking-[0.08em]"
                                        :class="agent.agentType === 'loop' ? 'bg-[#ede9fe] text-[#7c3aed]' : 'bg-[#dbeafe] text-[#2563eb]'"
                                    >
                                        {{ agent.agentType }}
                                    </div>
                                    <div class="text-center">
                                        <div class="text-[28px] font-semibold text-[#0f172a]">{{ agent.agentId }}</div>
                                        <div class="mt-3 text-[16px] text-[#475569]">{{ agent.agentName || '-' }}</div>
                                    </div>
                                    <div class="text-[12px] text-[#94a3b8]">&nbsp;</div>
                                </div>
                                <div class="agent-card-face agent-card-back flex h-full flex-col items-center justify-center p-4 text-center">
                                    <div class="text-[16px] leading-5 text-[#475569]">
                                        {{ agent.agentDesc || '暂无描述' }}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-if="!loading.agents && agents.length === 0" class="col-span-full rounded-[12px] border border-dashed border-[#cbd5e1] bg-white px-4 py-6 text-center text-[#94a3b8]">
                            暂无数据
                        </div>
                    </div>
                    <div v-if="loading.agents" class="mt-6 text-center text-[13px] text-[#94a3b8]">加载中...</div>
                </div>

                <!-- 详情视图 -->
                <div v-else class="flex h-full flex-col gap-4 overflow-visible">
                    <div class="flex flex-wrap items-center justify-between gap-3">
                        <div class="flex flex-wrap items-center gap-3">
                            <div class="text-[16px] font-semibold text-[#0f172a]">{{ selectedAgent.agentName || selectedAgent.agentId }}</div>
                            <span class="inline-flex items-center rounded-full bg-[#dbeafe] px-3 py-1 text-[12px] font-medium text-[#2563eb]">
                                类型：{{ selectedAgent.agentType }}
                            </span>
                            <span
                                class="inline-flex items-center rounded-full px-3 py-1 text-[12px] font-medium"
                                :class="selectedAgent.agentStatus === 1 ? 'bg-[#ecfdf3] text-[#16a34a]' : 'bg-[#fef2f2] text-[#dc2626]'"
                            >
                                状态：{{ selectedAgent.agentStatus === 1 ? '启用' : '禁用' }}
                            </span>
                        </div>
                        <div class="flex items-center gap-2">
                            <button
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f1f5f9]"
                                type="button"
                                @click="selectedAgent && router.push(`/admin/canvas?agentId=${selectedAgent.agentId}`)"
                            >
                                查看配置图
                            </button>
                            <button
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f1f5f9]"
                                type="button"
                                @click="backToGrid"
                            >
                                返回列表
                            </button>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 gap-4 overflow-visible lg:grid-cols-4 lg:gap-x-10">
                        <div
                            v-for="(slot, idx) in slotList"
                            :key="slot.roleLabel"
                            class="relative z-0 overflow-visible rounded-[14px] bg-white p-4 shadow-sm transition hover:border-[#0ea5e9]"
                            :class="activeSlot === idx ? 'border-2 border-[#0ea5e9] shadow-md' : 'border border-[#e2e8f0]'"
                            @click="handleChooseSlot(idx)"
                        >
                            <div class="mb-3 text-center">
                                <div class="text-[24px] font-semibold text-[#0f172a]">{{ slot.roleLabel }}</div>
                            </div>
                            <div v-if="slot.flow" class="flex min-h-[240px] flex-col gap-2 rounded-[12px] border border-[#e2e8f0] bg-[#f8fafc] p-3 text-left">
                                <div class="flex items-center justify-between">
                                    <div class="text-[16px] font-semibold text-[#0f172a]">{{ slot.flow.clientId }}</div>
                                </div>
                                <div class="mt-1 flex flex-col gap-1 text-[12px] text-[#475569]">
                                    <div class="flex items-start gap-2">
                                        <span class="shrink-0 text-[#94a3b8]">API：</span>
                                        <span
                                            v-if="getClientDetail(slot.flow.clientId)?.api?.apiId"
                                            class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                        >
                                            {{ getClientDetail(slot.flow.clientId)?.api?.apiId }}
                                        </span>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-2">
                                        <span class="shrink-0 text-[#94a3b8]">Model：</span>
                                        <span
                                            v-if="getClientDetail(slot.flow.clientId)?.model?.modelId"
                                            class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                        >
                                            {{ getClientDetail(slot.flow.clientId)?.model?.modelId }}
                                        </span>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-2">
                                        <span class="shrink-0 text-[#94a3b8]">MCP：</span>
                                        <div
                                            v-if="getIdList(getClientDetail(slot.flow.clientId)?.mcpList, 'mcpId').length"
                                            class="flex flex-wrap gap-1"
                                        >
                                            <span
                                                v-for="item in getIdList(getClientDetail(slot.flow.clientId)?.mcpList, 'mcpId')"
                                                :key="item"
                                                class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                            >
                                                {{ item }}
                                            </span>
                                        </div>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-2">
                                        <span class="shrink-0 text-[#94a3b8]">Advisor：</span>
                                        <div
                                            v-if="getIdList(getClientDetail(slot.flow.clientId)?.advisorList, 'advisorId').length"
                                            class="flex flex-wrap gap-1"
                                        >
                                            <span
                                                v-for="item in getIdList(getClientDetail(slot.flow.clientId)?.advisorList, 'advisorId')"
                                                :key="item"
                                                class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                            >
                                                {{ item }}
                                            </span>
                                        </div>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-2">
                                        <span class="shrink-0 text-[#94a3b8]">Prompt：</span>
                                        <div
                                            v-if="getIdList(getClientDetail(slot.flow.clientId)?.promptList, 'promptId').length"
                                            class="flex flex-wrap gap-1"
                                        >
                                            <span
                                                v-for="item in getIdList(getClientDetail(slot.flow.clientId)?.promptList, 'promptId')"
                                                :key="item"
                                                class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                            >
                                                {{ item }}
                                            </span>
                                        </div>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                </div>
                                <div class="mt-auto flex justify-center pt-2">
                                    <button
                                        class="rounded-full border border-[#e2e8f0] bg-white px-4 py-1 text-[12px] font-semibold text-[#475569] shadow-sm transition hover:border-[#0ea5e9] hover:text-[#0ea5e9] disabled:cursor-not-allowed disabled:text-[#cbd5e1]"
                                        type="button"
                                        :disabled="!(slot.flow.flowPrompt && slot.flow.flowPrompt.trim())"
                                        @click.stop="openPromptDialog(slot)"
                                    >
                                        查看设定
                                    </button>
                                </div>
                            </div>
                            <div v-else class="flex h-[240px] items-center justify-center rounded-[10px] border border-dashed border-[#cbd5e1] text-[28px] text-[#cbd5e1]">
                                ➕
                            </div>
                            <div
                                v-if="idx < slotList.length - 1"
                                class="pointer-events-none absolute -right-8 top-1/2 z-20 hidden -translate-y-1/2 items-center justify-center lg:flex"
                            >
                                <img :src="arrowIcon" alt="arrow" class="h-6 w-6 drop-shadow-sm" />
                            </div>
                        </div>
                    </div>

                    <div class="relative z-10 mt-6 flex min-h-[180px] flex-col rounded-[14px] border border-[#e2e8f0] bg-white p-4 shadow-sm">
                        <div v-if="activeSlot === null || activeSlot === undefined" class="flex h-[140px] items-center justify-center text-center text-[48px] font-extrabold text-[#cbd5e1]">
                            点击卡片以更换 Client
                        </div>
                        <template v-else>
                            <div class="mb-3 flex items-center justify-between">
                                <div class="text-[14px] font-semibold text-[#0f172a]">
                                    为 {{ slotList[activeSlot]?.roleLabel }} 选择 Client
                                </div>
                                <button
                                    class="rounded-[8px] border border-[#e2e8f0] px-3 py-1 text-[12px] text-[#475569] hover:bg-[#f8fafc]"
                                    type="button"
                                    @click="activeSlot = null"
                                >
                                    收起
                                </button>
                            </div>
                            <div class="relative z-20 flex gap-3 overflow-x-auto overflow-y-visible pb-2 pt-2">
                                <div
                                    v-for="client in candidateClients"
                                    :key="client.clientId"
                                    class="min-w-[280px] cursor-pointer rounded-[12px] border border-[#e2e8f0] bg-[#f8fafc] p-3 text-left shadow-sm transition hover:-translate-y-0.5 hover:border-[#0ea5e9] hover:shadow-md"
                                    @click.stop="openReplace(slotList[activeSlot], client)"
                                >
                                    <div class="flex items-center justify-between">
                                        <div class="text-[13px] font-semibold text-[#0f172a]">{{ client.clientId }}</div>
                                    </div>
                                    <div class="mt-2 flex flex-col gap-1 text-[12px] text-[#475569]">
                                        <div class="flex items-start gap-2">
                                            <span class="shrink-0 text-[#94a3b8]">API：</span>
                                            <span
                                                v-if="client.api?.apiId"
                                                class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                            >
                                                {{ client.api?.apiId }}
                                            </span>
                                            <span v-else class="text-[#475569]">-</span>
                                        </div>
                                        <div class="flex items-start gap-2">
                                            <span class="shrink-0 text-[#94a3b8]">Model：</span>
                                            <span
                                                v-if="client.model?.modelId"
                                                class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                            >
                                                {{ client.model?.modelId }}
                                            </span>
                                            <span v-else class="text-[#475569]">-</span>
                                        </div>
                                        <div class="flex items-start gap-2">
                                            <span class="shrink-0 text-[#94a3b8]">MCP：</span>
                                            <div v-if="getIdList(client.mcpList, 'mcpId').length" class="flex flex-wrap gap-1">
                                                <span
                                                    v-for="item in getIdList(client.mcpList, 'mcpId')"
                                                    :key="item"
                                                    class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                                >
                                                    {{ item }}
                                                </span>
                                            </div>
                                            <span v-else class="text-[#475569]">-</span>
                                        </div>
                                        <div class="flex items-start gap-2">
                                            <span class="shrink-0 text-[#94a3b8]">Advisor：</span>
                                            <div v-if="getIdList(client.advisorList, 'advisorId').length" class="flex flex-wrap gap-1">
                                                <span
                                                    v-for="item in getIdList(client.advisorList, 'advisorId')"
                                                    :key="item"
                                                    class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                                >
                                                    {{ item }}
                                                </span>
                                            </div>
                                            <span v-else class="text-[#475569]">-</span>
                                        </div>
                                        <div class="flex items-start gap-2">
                                            <span class="shrink-0 text-[#94a3b8]">Prompt：</span>
                                            <div v-if="getIdList(client.promptList, 'promptId').length" class="flex flex-wrap gap-1">
                                                <span
                                                    v-for="item in getIdList(client.promptList, 'promptId')"
                                                    :key="item"
                                                    class="rounded-full border border-[#e2e8f0] bg-white px-2 py-[1px] text-[11px] text-[#475569]"
                                                >
                                                    {{ item }}
                                                </span>
                                            </div>
                                            <span v-else class="text-[#475569]">-</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>

        <!-- 错误弹窗 -->
        <div
            v-if="errorDialog.visible"
            class="fixed inset-0 z-40 flex items-center justify-center bg-[#0f172a]/30 backdrop-blur-[2px]"
            @click="closeError"
        >
            <div class="w-[360px] rounded-[14px] bg-white p-5 shadow-lg" @click.stop>
                <div class="mb-2 text-[16px] font-semibold text-[#0f172a]">操作失败</div>
                <div class="text-[13px] text-[#475569]">{{ errorDialog.message }}</div>
                <div class="mt-4 flex justify-end">
                    <button
                        class="rounded-[10px] bg-[#0ea5e9] px-4 py-2 text-[13px] font-semibold text-white hover:bg-[#0284c7]"
                        type="button"
                        @click="closeError"
                    >
                        确认
                    </button>
                </div>
            </div>
        </div>

        <!-- 确认弹窗 -->
        <div
            v-if="confirmDialog.visible"
            class="fixed inset-0 z-50 flex items-center justify-center bg-[#0f172a]/30 backdrop-blur-[2px]"
            @click="closeConfirm"
        >
            <div class="w-[380px] rounded-[14px] bg-white p-5 shadow-lg" @click.stop>
                <div class="mb-3 text-[16px] font-semibold text-[#0f172a]">确认替换</div>
                <div class="space-y-2 text-[13px] text-[#475569]">
                    <div>槽位：{{ confirmDialog.roleLabel }}</div>
                    <div>当前：{{ confirmDialog.fromClient }}</div>
                    <div>替换为：{{ confirmDialog.toClient }}</div>
                </div>
                <div class="mt-5 flex justify-end gap-3">
                    <button
                        class="rounded-[10px] bg-[#ef4444] px-4 py-2 text-[13px] font-semibold text-white hover:bg-[#dc2626]"
                        type="button"
                        @click="closeConfirm"
                    >
                        取消
                    </button>
                    <button
                        class="rounded-[10px] bg-[#22c55e] px-4 py-2 text-[13px] font-semibold text-white transition hover:bg-[#16a34a] disabled:opacity-60"
                        type="button"
                        :disabled="loading.replacing"
                        @click="confirmDialog.onConfirm && confirmDialog.onConfirm()"
                    >
                        {{ loading.replacing ? '处理中...' : '确认' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- Prompt 详情弹窗 -->
        <div
            v-if="promptDialog.visible"
            class="fixed inset-0 z-50 flex items-center justify-center bg-[#0f172a]/30 backdrop-blur-[2px]"
            @click="closePromptDialog"
        >
            <div class="w-[520px] max-w-[90vw] rounded-[14px] bg-white p-5 shadow-lg" @click.stop>
                <div class="text-[16px] font-semibold text-[#0f172a]">{{ promptDialog.title || 'Prompt 详情' }}</div>
                <div class="mt-3 max-h-[360px] overflow-auto rounded-[10px] border border-[#e2e8f0] bg-[#f8fafc] p-3 text-[13px] text-[#475569] whitespace-pre-wrap">
                    {{ promptDialog.content }}
                </div>
                <div class="mt-4 flex justify-end">
                    <button
                        class="rounded-[10px] bg-[#0ea5e9] px-4 py-2 text-[13px] font-semibold text-white hover:bg-[#0284c7]"
                        type="button"
                        @click="closePromptDialog"
                    >
                        关闭
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-font {
    font-size: 15px;
}
.admin-font .text-\[12px\] {
    font-size: 13px !important;
}
.admin-font .text-\[13px\] {
    font-size: 14px !important;
}

.agent-card {
    perspective: 1000px;
}

.agent-card-inner {
    position: relative;
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
    transition: transform 0.6s;
}

.agent-card:hover .agent-card-inner {
    transform: rotateY(180deg);
}

.agent-card-face {
    position: absolute;
    inset: 0;
    backface-visibility: hidden;
}

.agent-card-back {
    transform: rotateY(180deg);
}
</style>
