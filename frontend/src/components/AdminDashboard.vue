<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import SidebarAdmin from './SidebarAdmin.vue';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { fetchAdminDashboard } from '../request/api';
import { normalizeError } from '../request/request';
import { useSettingsStore } from '../router/pinia';

const router = useRouter();
const settingsStore = useSettingsStore();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const currentKey = ref('dashboard');
const menuGroups = adminMenuGroups;

const loading = ref(false);
const errorMessage = ref('');
const dashboard = ref({ countInfo: {}, graphInfo: {} });
const rangeMode = ref('7d');
const usageModeSummary = ref('work'); // work | chat (for summary bar chart)
const usageModeTop = ref('work'); // work | chat (for top chart)
const usageKey = ref('api'); // api | model | client | agent | prompt | advisor | mcp

const lineChartRef = ref(null);
const barChartRef = ref(null);
const topBarChartRef = ref(null);
const pieChartRef = ref(null);
let lineChart;
let barChart;
let topBarChart;
let pieChart;

const pickData = (resp, message = '获取数据失败') => {
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

const fetchDashboard = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
        const resp = await fetchAdminDashboard();
        dashboard.value = pickData(resp, '获取 Dashboard 失败') || { countInfo: {}, graphInfo: {} };
    } catch (error) {
        errorMessage.value = normalizeError(error).message || '获取 Dashboard 失败';
    } finally {
        loading.value = false;
    }
};

const statCardRows = computed(() => {
    const countInfo = dashboard.value?.countInfo || {};
    const cards = {
        agent: { key: 'agent', label: 'AGENT', value: countInfo.agentCount ?? 0, path: '/admin/agent' },
        client: { key: 'client', label: 'CLIENT', value: countInfo.clientCount ?? 0, path: '/admin/client' },
        flow: { key: 'flow', label: 'FLOW', value: countInfo.flowCount ?? 0, path: '/admin/flow' },
        config: { key: 'config', label: 'CONFIG', value: countInfo.configCount ?? 0, path: '/admin/config' },
        task: { key: 'task', label: 'TASK', value: countInfo.taskCount ?? 0, path: '/admin/task' },
        session: { key: 'session', label: 'SESSION', value: countInfo.sessionCount ?? 0, path: '/admin/session' },

        model: { key: 'model', label: 'MODEL', value: countInfo.modelCount ?? 0, path: '/admin/model' },
        api: { key: 'api', label: 'API', value: countInfo.apiCount ?? 0, path: '/admin/api' },
        mcp: { key: 'mcp', label: 'MCP', value: countInfo.mcpCount ?? 0, path: '/admin/mcp' },
        prompt: { key: 'prompt', label: 'PROMPT', value: countInfo.promptCount ?? 0, path: '/admin/prompt' },
        advisor: { key: 'advisor', label: 'ADVISOR', value: countInfo.advisorCount ?? 0, path: '/admin/advisor' },

        user: { key: 'user', label: 'USER', value: countInfo.userCount ?? 0, path: '/admin/user' }
    };

    return [
        [cards.agent, cards.client, cards.flow, cards.config, cards.task, cards.session],
        [cards.model, cards.api, cards.mcp, cards.prompt, cards.advisor, cards.user]
    ];
});

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) router.push(target.path);
};

const goPath = (path) => {
    if (path) {
        router.push(path);
    }
};

const getChartTheme = () => {
    const style = getComputedStyle(document.documentElement);
    return {
        textPrimary: style.getPropertyValue('--text-primary').trim() || '#1b2437',
        textSecondary: style.getPropertyValue('--text-secondary').trim() || '#5b6780',
        border: style.getPropertyValue('--border-color').trim() || '#e3e8f0',
        accent: style.getPropertyValue('--accent-color').trim() || '#2f7cf6',
        surface: style.getPropertyValue('--surface-2').trim() || '#f8fafc'
    };
};

const getMessageSeries = () => {
    const graphInfo = dashboard.value?.graphInfo || {};
    const raw = rangeMode.value === '30d' ? graphInfo.messageLastMonth : graphInfo.messageLastWeek;
    const list = Array.isArray(raw) ? raw : [];
    return {
        labels: list.map((item) => item?.date || ''),
        values: list.map((item) => item?.count ?? 0)
    };
};

const sumUsageMap = (usageMap) => {
    const keys = ['api', 'model', 'client', 'agent', 'prompt', 'advisor', 'mcp'];
    const map = usageMap && typeof usageMap === 'object' ? usageMap : {};
    return keys.map((key) => {
        const list = Array.isArray(map[key]) ? map[key] : [];
        const value = list.reduce((acc, item) => acc + (item?.value ?? 0), 0);
        return { name: key, value };
    });
};

const getUsageBars = () => {
    const graphInfo = dashboard.value?.graphInfo || {};
    const usageMap = usageModeSummary.value === 'chat' ? graphInfo.chatUsage : graphInfo.workUsage;
    return sumUsageMap(usageMap);
};

const getUsageTopBars = () => {
    const graphInfo = dashboard.value?.graphInfo || {};
    const usageMap = usageModeTop.value === 'chat' ? graphInfo.chatUsage : graphInfo.workUsage;
    const map = usageMap && typeof usageMap === 'object' ? usageMap : {};
    const list = Array.isArray(map[usageKey.value]) ? map[usageKey.value] : [];
    return list
        .filter((item) => item && item.id)
        .slice(0, 10)
        .map((item) => ({
            id: item.id,
            value: item.value ?? 0
        }));
};

const getSessionPie = () => {
    const graphInfo = dashboard.value?.graphInfo || {};
    const pie = graphInfo.sessionWorkVsChat || {};
    return [
        { name: 'chat', value: pie.chatCount ?? 0 },
        { name: 'work', value: pie.workCount ?? 0 }
    ];
};

const updateLineChart = () => {
    if (!lineChart) return;
    const theme = getChartTheme();
    const series = getMessageSeries();
    lineChart.setOption({
        grid: { left: 32, right: 24, top: 32, bottom: 24, containLabel: true },
        tooltip: { trigger: 'axis' },
        xAxis: {
            type: 'category',
            data: series.labels,
            axisLine: { lineStyle: { color: theme.border } },
            axisLabel: { color: theme.textSecondary }
        },
        yAxis: {
            type: 'value',
            minInterval: 1,
            axisLine: { lineStyle: { color: theme.border } },
            axisLabel: { color: theme.textSecondary },
            splitLine: { lineStyle: { color: theme.border } }
        },
        series: [
            {
                type: 'line',
                data: series.values,
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { color: theme.accent, width: 2 },
                itemStyle: { color: theme.accent },
                areaStyle: { color: theme.accent, opacity: 0.12 }
            }
        ]
    });
};

const updateBarChart = () => {
    if (!barChart) return;
    const theme = getChartTheme();
    const bars = getUsageBars();
    const labels = bars.map((item) => (item.name || '').toUpperCase());
    const values = bars.map((item) => item.value ?? 0);
    barChart.setOption({
        grid: { left: 32, right: 24, top: 32, bottom: 24, containLabel: true },
        tooltip: { trigger: 'axis' },
        xAxis: {
            type: 'category',
            data: labels,
            axisLine: { lineStyle: { color: theme.border } },
            axisLabel: { color: theme.textSecondary }
        },
        yAxis: {
            type: 'value',
            minInterval: 1,
            axisLine: { lineStyle: { color: theme.border } },
            axisLabel: { color: theme.textSecondary },
            splitLine: { lineStyle: { color: theme.border } }
        },
        series: [
            {
                type: 'bar',
                data: values,
                barWidth: 26,
                itemStyle: {
                    color: theme.accent,
                    borderRadius: [8, 8, 0, 0]
                }
            }
        ]
    });
};

const updateTopBarChart = () => {
    if (!topBarChart) return;
    const theme = getChartTheme();
    const bars = getUsageTopBars();
    const ids = bars.map((item) => item.id);
    const values = bars.map((item) => item.value ?? 0);
    topBarChart.setOption({
        grid: { left: 16, right: 24, top: 16, bottom: 16, containLabel: true },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: {
            type: 'value',
            minInterval: 1,
            axisLine: { lineStyle: { color: theme.border } },
            axisLabel: { color: theme.textSecondary },
            splitLine: { lineStyle: { color: theme.border } }
        },
        yAxis: {
            type: 'category',
            data: ids,
            axisLine: { lineStyle: { color: theme.border } },
            axisLabel: {
                color: theme.textSecondary,
                width: 180,
                overflow: 'truncate',
                interval: 0
            }
        },
        series: [
            {
                type: 'bar',
                data: values,
                barWidth: 18,
                itemStyle: {
                    color: theme.accent,
                    borderRadius: [0, 8, 8, 0]
                }
            }
        ]
    });
};

const updatePieChart = () => {
    if (!pieChart) return;
    const theme = getChartTheme();
    const data = getSessionPie();
    pieChart.setOption({
        tooltip: { trigger: 'item' },
        legend: {
            bottom: 0,
            textStyle: { color: theme.textSecondary }
        },
        series: [
            {
                type: 'pie',
                radius: ['45%', '70%'],
                center: ['50%', '45%'],
                itemStyle: { borderColor: theme.surface, borderWidth: 2 },
                label: { color: theme.textSecondary },
                data: data
            }
        ]
    });
};

const initCharts = () => {
    if (lineChartRef.value && !lineChart) {
        lineChart = echarts.init(lineChartRef.value);
    }
    if (barChartRef.value && !barChart) {
        barChart = echarts.init(barChartRef.value);
    }
    if (topBarChartRef.value && !topBarChart) {
        topBarChart = echarts.init(topBarChartRef.value);
    }
    if (pieChartRef.value && !pieChart) {
        pieChart = echarts.init(pieChartRef.value);
    }
    updateCharts();
};

const updateCharts = () => {
    updateLineChart();
    updateBarChart();
    updateTopBarChart();
    updatePieChart();
};

const handleResize = () => {
    lineChart?.resize();
    barChart?.resize();
    topBarChart?.resize();
    pieChart?.resize();
};

onMounted(async () => {
    await fetchDashboard();
    await nextTick();
    initCharts();
    window.addEventListener('resize', handleResize);
});

watch(
    () => [
        dashboard.value,
        rangeMode.value,
        usageModeSummary.value,
        usageModeTop.value,
        usageKey.value,
        isDarkTheme.value
    ],
    async () => {
        await nextTick();
        initCharts();
    },
    { deep: true }
);

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    lineChart?.dispose();
    barChart?.dispose();
    topBarChart?.dispose();
    pieChart?.dispose();
    lineChart = null;
    barChart = null;
    topBarChart = null;
    pieChart = null;
});
</script>

<template>
    <div class="admin-font flex h-screen bg-[var(--bg-page)] text-[var(--text-primary)]">
        <SidebarAdmin :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[var(--border-color)] bg-[var(--surface-1)] px-6 py-4 shadow-[var(--shadow-soft)]">
                <div class="text-[18px] font-semibold">DASHBOARD</div>
            </header>

            <div class="flex-1 overflow-auto p-6">
                <div v-if="errorMessage" class="mb-4 rounded-[12px] border border-[var(--border-color)] bg-[var(--surface-2)] px-4 py-3 text-[13px] text-[var(--text-secondary)]">
                    {{ errorMessage }}
                </div>

                <div class="mb-6 flex items-center justify-center text-center">
                    <div class="text-[26px] font-extrabold tracking-[0.02em] text-[var(--text-primary)]">
                        Dasi AI 目前已经处理了
                        <span class="mx-2 text-[42px] font-black text-[var(--accent-strong)] tabular-nums">
                            {{ dashboard?.countInfo?.messageCount ?? 0 }}
                        </span>
                        条消息
                    </div>
                </div>

                <div class="space-y-4">
                    <div v-for="(row, idx) in statCardRows" :key="`stat-row-${idx}`" class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-6">
                        <button
                            v-for="card in row"
                            :key="card.key"
                            class="group rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] px-4 py-3 text-left shadow-[var(--shadow-soft)] transition hover:-translate-y-1 hover:bg-[var(--surface-2)]"
                            type="button"
                            @click="goPath(card.path)"
                        >
                            <div class="flex items-center justify-between gap-3">
                                <div class="text-[12px] font-semibold tracking-[0.08em] text-[var(--text-secondary)]">
                                    {{ card.label }}
                                </div>
                                <div class="text-[30px] font-bold tabular-nums text-[var(--text-primary)]">
                                    {{ card.value }}
                                </div>
                            </div>
                        </button>
                    </div>
                </div>

                <div class="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-3">
                    <div class="lg:col-span-2 rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                        <div class="mb-4 flex items-center justify-between">
                            <div class="text-[15px] font-semibold">Message 趋势</div>
                            <div class="flex items-center gap-2">
                                <button
                                    class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                    :class="rangeMode === '7d' ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                    type="button"
                                    @click="rangeMode = '7d'"
                                >
                                    近 7 天
                                </button>
                                <button
                                    class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                    :class="rangeMode === '30d' ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                    type="button"
                                    @click="rangeMode = '30d'"
                                >
                                    近 30 天
                                </button>
                            </div>
                        </div>
                        <div ref="lineChartRef" class="h-[260px] w-full"></div>
                        <div v-if="loading" class="mt-3 text-center text-[12px] text-[var(--text-secondary)]">加载中...</div>
                    </div>

                    <div class="rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                        <div class="mb-4 text-[15px] font-semibold">Chat / Work 比例</div>
                        <div ref="pieChartRef" class="h-[260px] w-full"></div>
                    </div>
                </div>

                <div class="mt-6 flex flex-col gap-4">
                    <div class="rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                        <div class="mb-4 flex items-center justify-between">
                            <div class="text-[15px] font-semibold">使用次数分布</div>
                            <div class="flex items-center gap-2">
                                <button
                                    class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                    :class="usageModeSummary === 'work' ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                    type="button"
                                    @click="usageModeSummary = 'work'"
                                >
                                    WORK
                                </button>
                                <button
                                    class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                    :class="usageModeSummary === 'chat' ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                    type="button"
                                    @click="usageModeSummary = 'chat'"
                                >
                                    CHAT
                                </button>
                            </div>
                        </div>
                        <div ref="barChartRef" class="h-[280px] w-full"></div>
                    </div>

                    <div class="rounded-[16px] border border-[var(--border-color)] bg-[var(--surface-1)] p-4 shadow-[var(--shadow-soft)]">
                        <div class="mb-3 flex flex-wrap items-center justify-between gap-3">
                            <div class="text-[14px] font-semibold text-[var(--text-primary)]">
                                TOP 10 ({{ usageModeTop.toUpperCase() }}) - {{ usageKey.toUpperCase() }}
                            </div>
                            <div class="flex flex-wrap items-center gap-2">
                                <button
                                    class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                    :class="usageModeTop === 'work' ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                    type="button"
                                    @click="usageModeTop = 'work'"
                                >
                                    WORK
                                </button>
                                <button
                                    class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                    :class="usageModeTop === 'chat' ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                    type="button"
                                    @click="usageModeTop = 'chat'"
                                >
                                    CHAT
                                </button>
                            </div>
                        </div>

                        <div ref="topBarChartRef" class="h-[340px] w-full"></div>
                        <div v-if="getUsageTopBars().length === 0" class="mt-3 text-center text-[12px] text-[var(--text-secondary)]">
                            暂无数据
                        </div>

                        <div class="mt-4 flex flex-wrap items-center gap-2">
                            <button
                                v-for="key in ['api', 'model', 'client', 'agent', 'prompt', 'advisor', 'mcp']"
                                :key="key"
                                class="rounded-full border px-3 py-1 text-[12px] font-medium transition"
                                :class="usageKey === key ? 'border-[var(--accent-color)] bg-[var(--accent-soft)] text-[var(--accent-strong)]' : 'border-[var(--border-color)] text-[var(--text-secondary)]'"
                                type="button"
                                @click="usageKey = key"
                            >
                                {{ key.toUpperCase() }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
