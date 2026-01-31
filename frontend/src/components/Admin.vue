<script setup>
import { computed, reactive, ref, watch, onMounted } from 'vue';
import SideMenu from './SideMenu.vue';
import { fetchAdminList, createAdminItem, updateAdminItem, deleteAdminItem, switchAdminStatus } from '../request/api';
import { normalizeError } from '../request/request';
import { useAuthStore } from '../router/pinia';

const authStore = useAuthStore();

const moduleDefs = [
    {
        key: 'agent',
        label: 'AGENT',
        group: 'model',
        title: 'AGENT 管理',
        statusField: 'agentStatus',
        search: ['keyword', 'status', 'type'],
        query: { keyword: '', status: '', type: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            agentId: '',
            agentName: '',
            agentType: 'loop',
            agentDesc: '',
            agentStatus: 1
        }),
        fields: [
            { prop: 'agentId', label: 'Agent ID', placeholder: '可不填自动生成' },
            { prop: 'agentName', label: '名称', required: true },
            { prop: 'agentType', label: '类型', required: true },
            { prop: 'agentDesc', label: '描述', type: 'textarea' },
            { prop: 'agentStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'agentId', label: 'ID' },
            { prop: 'agentName', label: '名称' },
            { prop: 'agentType', label: '类型' },
            { prop: 'agentDesc', label: '描述' }
        ]
    },
    {
        key: 'client',
        label: 'CLIENT',
        group: 'model',
        title: 'CLIENT 管理',
        statusField: 'clientStatus',
        search: ['keyword', 'modelId', 'type', 'status'],
        query: { keyword: '', modelId: '', type: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            clientId: '',
            clientType: '',
            modelId: '',
            modelName: '',
            clientName: '',
            clientDesc: '',
            clientStatus: 1
        }),
        fields: [
            { prop: 'clientId', label: 'Client ID', placeholder: '可不填自动生成' },
            { prop: 'clientName', label: '名称', required: true },
            { prop: 'clientType', label: '类型', required: true },
            { prop: 'modelId', label: '模型', type: 'select', optionsKey: 'models', required: true },
            { prop: 'modelName', label: '模型名称', placeholder: '可选' },
            { prop: 'clientDesc', label: '描述', type: 'textarea' },
            { prop: 'clientStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'clientId', label: 'ID' },
            { prop: 'clientName', label: '名称' },
            { prop: 'clientType', label: '类型' },
            { prop: 'modelId', label: '模型ID' }
        ]
    },
    {
        key: 'flow',
        label: 'FLOW',
        group: 'model',
        title: 'FLOW 管理',
        statusField: 'flowStatus',
        search: ['agentId', 'clientId', 'status'],
        query: { agentId: '', clientId: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            agentId: '',
            clientId: '',
            clientType: '',
            flowPrompt: '',
            flowSeq: 1,
            flowStatus: 1
        }),
        fields: [
            { prop: 'agentId', label: 'Agent', type: 'select', optionsKey: 'agents', required: true },
            { prop: 'clientId', label: 'Client', type: 'select', optionsKey: 'clients', required: true },
            { prop: 'clientType', label: '客户端类型', placeholder: '可选' },
            { prop: 'flowSeq', label: '顺序', type: 'number', required: true },
            { prop: 'flowPrompt', label: '提示词', type: 'textarea' },
            { prop: 'flowStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'agentId', label: 'Agent' },
            { prop: 'clientId', label: 'Client' },
            { prop: 'clientType', label: '类型' },
            { prop: 'flowSeq', label: '顺序' }
        ]
    },
    {
        key: 'api',
        label: 'API',
        group: 'base',
        title: 'API 管理',
        statusField: 'apiStatus',
        search: ['keyword', 'status'],
        query: { keyword: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            apiId: '',
            apiBaseUrl: '',
            apiKey: '',
            apiCompletionsPath: '',
            apiEmbeddingsPath: '',
            apiStatus: 1
        }),
        fields: [
            { prop: 'apiId', label: 'API ID', placeholder: '可不填自动生成' },
            { prop: 'apiBaseUrl', label: 'Base URL', required: true },
            { prop: 'apiKey', label: 'Key', required: true },
            { prop: 'apiCompletionsPath', label: '对话路径', required: true },
            { prop: 'apiEmbeddingsPath', label: 'Embedding 路径', required: true },
            { prop: 'apiStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'apiId', label: 'ID' },
            { prop: 'apiBaseUrl', label: 'Base URL' },
            { prop: 'apiCompletionsPath', label: '对话路径' }
        ]
    },
    {
        key: 'model',
        label: 'MODEL',
        group: 'base',
        title: 'MODEL 管理',
        statusField: 'modelStatus',
        search: ['keyword', 'apiId', 'status'],
        query: { keyword: '', apiId: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            modelId: '',
            apiId: '',
            modelName: '',
            modelType: '',
            modelStatus: 1
        }),
        fields: [
            { prop: 'modelId', label: 'Model ID', placeholder: '可不填自动生成' },
            { prop: 'modelName', label: '名称', required: true },
            { prop: 'modelType', label: '类型', placeholder: '如 GPT' },
            { prop: 'apiId', label: 'API', type: 'select', optionsKey: 'apis', required: true },
            { prop: 'modelStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'modelId', label: 'ID' },
            { prop: 'modelName', label: '名称' },
            { prop: 'apiId', label: 'API' }
        ]
    },
    {
        key: 'mcp',
        label: 'MCP',
        group: 'base',
        title: 'MCP 管理',
        statusField: 'mcpStatus',
        search: ['keyword', 'type', 'status'],
        query: { keyword: '', type: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            mcpId: '',
            mcpName: '',
            mcpType: '',
            mcpConfig: '',
            mcpDesc: '',
            mcpTimeout: 180,
            mcpChat: 0,
            mcpStatus: 1
        }),
        fields: [
            { prop: 'mcpId', label: 'MCP ID', placeholder: '可不填自动生成' },
            { prop: 'mcpName', label: '名称', required: true },
            { prop: 'mcpType', label: '类型', required: true },
            { prop: 'mcpConfig', label: '配置', type: 'textarea', required: true },
            { prop: 'mcpDesc', label: '描述', type: 'textarea' },
            { prop: 'mcpTimeout', label: '超时时间', type: 'number' },
            { prop: 'mcpChat', label: '聊天可用', type: 'switch' },
            { prop: 'mcpStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'mcpId', label: 'ID' },
            { prop: 'mcpName', label: '名称' },
            { prop: 'mcpType', label: '类型' }
        ]
    },
    {
        key: 'advisor',
        label: 'ADVISOR',
        group: 'base',
        title: 'ADVISOR 管理',
        statusField: 'advisorStatus',
        search: ['keyword', 'type', 'status'],
        query: { keyword: '', type: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            advisorId: '',
            advisorName: '',
            advisorType: '',
            advisorDesc: '',
            advisorOrder: 0,
            advisorParam: '',
            advisorStatus: 1
        }),
        fields: [
            { prop: 'advisorId', label: 'Advisor ID', placeholder: '可不填自动生成' },
            { prop: 'advisorName', label: '名称', required: true },
            { prop: 'advisorType', label: '类型', required: true },
            { prop: 'advisorOrder', label: '顺序', type: 'number' },
            { prop: 'advisorDesc', label: '描述', type: 'textarea' },
            { prop: 'advisorParam', label: '参数', type: 'textarea' },
            { prop: 'advisorStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'advisorId', label: 'ID' },
            { prop: 'advisorName', label: '名称' },
            { prop: 'advisorType', label: '类型' }
        ]
    },
    {
        key: 'prompt',
        label: 'PROMPT',
        group: 'base',
        title: 'PROMPT 管理',
        statusField: 'promptStatus',
        search: ['keyword', 'status'],
        query: { keyword: '', status: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            promptId: '',
            promptName: '',
            promptContent: '',
            promptDesc: '',
            promptStatus: 1
        }),
        fields: [
            { prop: 'promptId', label: 'Prompt ID', placeholder: '可不填自动生成' },
            { prop: 'promptName', label: '名称', required: true },
            { prop: 'promptContent', label: '内容', type: 'textarea', required: true },
            { prop: 'promptDesc', label: '描述', type: 'textarea' },
            { prop: 'promptStatus', label: '状态', type: 'switch' }
        ],
        columns: [
            { prop: 'promptId', label: 'ID' },
            { prop: 'promptName', label: '名称' }
        ]
    },
    {
        key: 'user',
        label: 'USER',
        group: 'user',
        title: 'USER 管理',
        statusField: null,
        search: ['username', 'role'],
        query: { username: '', role: '', page: 1, size: 10 },
        formDefaults: () => ({
            id: null,
            username: '',
            password: '',
            role: 'account'
        }),
        fields: [
            { prop: 'username', label: '用户名', required: true },
            { prop: 'password', label: '密码', type: 'password', requiredOnCreate: true },
            {
                prop: 'role',
                label: '角色',
                type: 'select',
                options: [
                    { label: 'admin', value: 'admin' },
                    { label: 'account', value: 'account' }
                ]
            }
        ],
        columns: [
            { prop: 'username', label: '用户名' },
            { prop: 'role', label: '角色' }
        ]
    }
];

const moduleGroups = [
    {
        name: 'model',
        label: '模型管理',
        items: moduleDefs.filter((m) => m.group === 'model')
    },
    {
        name: 'base',
        label: '基础管理',
        items: moduleDefs.filter((m) => m.group === 'base')
    },
    {
        name: 'user',
        label: '用户管理',
        items: moduleDefs.filter((m) => m.group === 'user')
    }
];

const modulesMap = Object.fromEntries(moduleDefs.map((m) => [m.key, m]));

const currentKey = ref('agent');
const modalVisible = ref(false);
const editingId = ref(null);
const modalError = ref('');
const currentForm = reactive({});

const errorDialog = reactive({
    visible: false,
    message: ''
});

const unwrapResult = (resp, defaultMsg = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || defaultMsg);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const stateMap = reactive(
    Object.fromEntries(
        moduleDefs.map((m) => [
            m.key,
            {
                list: [],
                total: 0,
                loading: false,
                query: { ...m.query },
                error: ''
            }
        ])
    )
);

const options = reactive({
    apis: [],
    models: [],
    agents: [],
    clients: []
});

const currentModule = computed(() => modulesMap[currentKey.value]);

const pageCount = computed(() => {
    const st = stateMap[currentKey.value];
    if (!st || !st.total) return 1;
    return Math.max(1, Math.ceil(st.total / (st.query.size || 10)));
});

const formatStatus = (val) => (val === 1 ? '启用' : '禁用');

const showErrorDialog = (msg) => {
    errorDialog.visible = true;
    errorDialog.message = msg || '操作失败';
};

const loadRefs = async () => {
    const load = async (key, moduleKey, idField, nameField) => {
        try {
            const res = await fetchAdminList(moduleKey, { page: 1, size: 200 });
            const payload = res?.data ?? res?.result ?? res;
            options[key] =
                payload?.list?.map((item) => ({
                    value: item[idField] || item[Object.keys(item).find((k) => k.endsWith('Id'))],
                    label: item[nameField] || item[Object.keys(item).find((k) => k.endsWith('Name'))] || item.username || ''
                })) || [];
        } catch (_) {
            options[key] = [];
        }
    };
    await Promise.all([
        load('apis', 'api', 'apiId', 'apiId'),
        load('models', 'model', 'modelId', 'modelName'),
        load('agents', 'agent', 'agentId', 'agentName'),
        load('clients', 'client', 'clientId', 'clientName')
    ]);
};

const fetchList = async (key = currentKey.value) => {
    const module = modulesMap[key];
    const state = stateMap[key];
    state.loading = true;
    state.error = '';
    try {
        const params = { ...state.query };
        Object.keys(params).forEach((k) => {
            if (params[k] === '' || params[k] === null || params[k] === undefined) {
                delete params[k];
            }
        });
        const res = await fetchAdminList(key, params);
        const payload = unwrapResult(res, '查询失败');
        state.list = payload?.list || [];
        state.total = payload?.total || 0;
    } catch (err) {
        state.error = normalizeError(err).message;
    } finally {
        state.loading = false;
    }
};

const openCreate = () => {
    editingId.value = null;
    modalError.value = '';
    Object.assign(currentForm, currentModule.value.formDefaults());
    modalVisible.value = true;
};

const openEdit = (row) => {
    editingId.value = row.id;
    modalError.value = '';
    Object.assign(currentForm, currentModule.value.formDefaults(), row);
    modalVisible.value = true;
};

const handleDelete = async (row) => {
    if (!window.confirm('确认删除该记录？')) return;
    try {
        const res = await deleteAdminItem(currentKey.value, row.id);
        unwrapResult(res, '删除失败');
        await fetchList();
    } catch (err) {
        const e = normalizeError(err);
        showErrorDialog(e.message || '删除失败');
    }
};

const saveForm = async () => {
    const module = currentModule.value;
    modalError.value = '';
    for (const field of module.fields) {
        if (field.required && !currentForm[field.prop]) {
            modalError.value = `${field.label} 不能为空`;
            return;
        }
        if (field.requiredOnCreate && !editingId.value && !currentForm[field.prop]) {
            modalError.value = `${field.label} 不能为空`;
            return;
        }
    }
    try {
        if (editingId.value) {
            const res = await updateAdminItem(module.key, editingId.value, currentForm);
            unwrapResult(res, '更新失败');
        } else {
            const res = await createAdminItem(module.key, currentForm);
            unwrapResult(res, '创建失败');
        }
        modalVisible.value = false;
        await Promise.all([fetchList(), loadRefs()]);
    } catch (err) {
        const e = normalizeError(err);
        showErrorDialog(e.message || '保存失败');
    }
};

const switchStatus = async (row, val) => {
    const oldVal = row[currentModule.value.statusField];
    row[currentModule.value.statusField] = val;
    try {
        const res = await switchAdminStatus(currentKey.value, row.id, val);
        unwrapResult(res, '更新状态失败');
    } catch (err) {
        const e = normalizeError(err);
        row[currentModule.value.statusField] = oldVal;
        showErrorDialog(e.message || '更新状态失败');
    }
};

const changePage = (step) => {
    const st = stateMap[currentKey.value];
    const next = Math.min(Math.max(1, st.query.page + step), pageCount.value);
    st.query.page = next;
    fetchList();
};

watch(
    () => currentKey.value,
    async () => {
        await fetchList();
    }
);

onMounted(async () => {
    await loadRefs();
    await fetchList();
});

const logout = () => {
    authStore.clear();
    window.location.href = '/login';
};
</script>

<template>
    <div class="flex h-screen bg-[#f8fafc]">
        <SideMenu :groups="moduleGroups" :current="currentKey" @select="(k) => (currentKey = k)" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">{{ currentModule.title }}</div>
                <div class="flex items-center gap-3">
                    <div class="text-right">
                        <div class="text-[14px] font-semibold text-[#0f172a]">{{ authStore.user?.username || '-' }}</div>
                        <div class="text-[12px] text-[#94a3b8]">角色：{{ authStore.user?.role || '-' }}</div>
                    </div>
                    <div class="grid h-[36px] w-[36px] place-items-center rounded-full bg-[#e2e8f0] text-[14px] font-bold text-[#0f172a]">
                        {{ (authStore.user?.username || '?').slice(0, 1).toUpperCase() }}
                    </div>
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f8fafc]"
                        type="button"
                        @click="logout"
                    >
                        退出
                    </button>
                </div>
            </header>
            <div class="flex-1 overflow-auto p-5">
                <div class="mb-4 grid grid-cols-[1fr_auto] items-center gap-3">
                    <div class="flex flex-wrap items-center gap-3 rounded-[12px] border border-[#e2e8f0] bg-white px-4 py-3">
                        <template v-for="field in currentModule.search" :key="field">
                            <template v-if="field === 'status'">
                                <select
                                    v-model="stateMap[currentKey].query.status"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                >
                                    <option value="">全部状态</option>
                                    <option :value="1">启用</option>
                                    <option :value="0">禁用</option>
                                </select>
                            </template>
                            <template v-else-if="field === 'type'">
                                <input
                                    v-model="stateMap[currentKey].query.type"
                                    class="w-[140px] rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    placeholder="类型"
                                />
                            </template>
                            <template v-else-if="field === 'apiId'">
                                <select
                                    v-model="stateMap[currentKey].query.apiId"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                >
                                    <option value="">全部 API</option>
                                    <option v-for="opt in options.apis" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                                </select>
                            </template>
                            <template v-else-if="field === 'modelId'">
                                <select
                                    v-model="stateMap[currentKey].query.modelId"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                >
                                    <option value="">全部模型</option>
                                    <option v-for="opt in options.models" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                                </select>
                            </template>
                            <template v-else-if="field === 'agentId'">
                                <select
                                    v-model="stateMap[currentKey].query.agentId"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                >
                                    <option value="">全部 Agent</option>
                                    <option v-for="opt in options.agents" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                                </select>
                            </template>
                            <template v-else-if="field === 'clientId'">
                                <select
                                    v-model="stateMap[currentKey].query.clientId"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                >
                                    <option value="">全部 Client</option>
                                    <option v-for="opt in options.clients" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                                </select>
                            </template>
                            <template v-else-if="field === 'role'">
                                <select
                                    v-model="stateMap[currentKey].query.role"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                >
                                    <option value="">全部角色</option>
                                    <option value="admin">admin</option>
                                    <option value="account">account</option>
                                </select>
                            </template>
                            <template v-else>
                                <input
                                    v-model="stateMap[currentKey].query[field]"
                                    class="w-[160px] rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    :placeholder="field === 'keyword' ? '关键字' : field"
                                />
                            </template>
                        </template>
                        <button
                            class="rounded-[10px] bg-[#1d4ed8] px-4 py-2 text-[13px] font-semibold text-white"
                            type="button"
                            @click="() => { stateMap[currentKey].query.page = 1; fetchList(); }"
                        >
                            查询
                        </button>
                    </div>
                    <button
                        class="rounded-[10px] bg-[#0f172a] px-4 py-2 text-[13px] font-semibold text-white shadow"
                        type="button"
                        @click="openCreate"
                    >
                        新增
                    </button>
                </div>

                <div class="overflow-hidden rounded-[12px] border border-[#e2e8f0] bg-white">
                    <table class="w-full border-collapse text-[13px]">
                        <thead class="bg-[#f8fafc] text-[#475569]">
                            <tr>
                                <th v-for="col in currentModule.columns" :key="col.prop" class="px-3 py-2 text-left font-semibold">
                                    {{ col.label }}
                                </th>
                                <th class="px-3 py-2 text-left font-semibold">操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-if="stateMap[currentKey].loading">
                                <td :colspan="currentModule.columns.length + 1" class="px-3 py-4 text-center text-[#94a3b8]">加载中...</td>
                            </tr>
                            <tr v-else-if="stateMap[currentKey].list.length === 0">
                                <td :colspan="currentModule.columns.length + 1" class="px-3 py-4 text-center text-[#94a3b8]">暂无数据</td>
                            </tr>
                            <tr
                                v-for="row in stateMap[currentKey].list"
                                :key="row.id"
                                class="border-t border-[#e2e8f0] hover:bg-[#f8fafc]"
                            >
                                <td v-for="col in currentModule.columns" :key="col.prop" class="px-3 py-2 text-[#0f172a]">
                                    {{ row[col.prop] ?? '-' }}
                                </td>
                                <td class="px-3 py-2">
                                    <div class="flex flex-wrap items-center gap-2">
                                        <template v-if="currentModule.statusField">
                                            <button
                                                class="relative h-[22px] w-[50px] rounded-full text-[10px] font-semibold uppercase tracking-[0.5px] transition"
                                                :class="row[currentModule.statusField] === 1 ? 'bg-[#1d4ed8] text-white' : 'bg-[#cbd5e1] text-[#0f172a]'"
                                                type="button"
                                                @click="switchStatus(row, row[currentModule.statusField] === 1 ? 0 : 1)"
                                            >
                                                <span
                                                    class="absolute left-[3px] top-[3px] h-[16px] w-[16px] rounded-full bg-white transition"
                                                    :class="row[currentModule.statusField] === 1 ? 'translate-x-[28px]' : ''"
                                                />
                                                <span
                                                    class="absolute inset-0 flex items-center px-[6px] transition"
                                                    :class="row[currentModule.statusField] === 1 ? 'justify-start' : 'justify-end'"
                                                >
                                                    <span>{{ row[currentModule.statusField] === 1 ? 'on' : 'off' }}</span>
                                                </span>
                                            </button>
                                        </template>
                                        <button
                                            class="rounded-[8px] border border-[#e2e8f0] px-3 py-1 text-[12px] font-semibold text-[#0f172a]"
                                            type="button"
                                            @click="openEdit(row)"
                                        >
                                            编辑
                                        </button>
                                        <button
                                            class="rounded-[8px] border border-[#fecdd3] px-3 py-1 text-[12px] font-semibold text-[#dc2626]"
                                            type="button"
                                            @click="handleDelete(row)"
                                        >
                                            删除
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="mt-3 flex items-center justify-between text-[13px] text-[#475569]">
                    <div>共 {{ stateMap[currentKey].total }} 条 / 第 {{ stateMap[currentKey].query.page }} / {{ pageCount }}</div>
                    <div class="flex gap-2">
                        <button
                            class="rounded-[8px] border border-[#e2e8f0] px-3 py-2"
                            type="button"
                            @click="changePage(-1)"
                        >
                            上一页
                        </button>
                        <button
                            class="rounded-[8px] border border-[#e2e8f0] px-3 py-2"
                            type="button"
                            @click="changePage(1)"
                        >
                            下一页
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="modalVisible"
            class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.35)] px-4"
        >
            <div class="w-full max-w-[620px] rounded-[14px] bg-white p-6 shadow-lg">
                <div class="mb-4 flex items-center justify-between">
                    <div class="text-[16px] font-semibold text-[#0f172a]">
                        {{ editingId ? '编辑' : '新增' }} - {{ currentModule.title }}
                    </div>
                    <button class="text-[14px] text-[#94a3b8]" type="button" @click="modalVisible = false">✕</button>
                </div>
                <div class="flex flex-col gap-3">
                    <div v-for="field in currentModule.fields" :key="field.prop" class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">
                            {{ field.label }}
                            <span v-if="field.required && !field.requiredOnCreate" class="text-[#dc2626]">*</span>
                        </label>
                        <template v-if="field.type === 'textarea'">
                            <textarea
                                v-model="currentForm[field.prop]"
                                rows="3"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                :placeholder="field.placeholder"
                            />
                        </template>
                        <template v-else-if="field.type === 'select'">
                            <select
                                v-model="currentForm[field.prop]"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                            >
                                <option value="">请选择</option>
                                <option
                                    v-for="opt in field.options || options[field.optionsKey || ''] || []"
                                    :key="opt.value || opt"
                                    :value="opt.value || opt"
                                >
                                    {{ opt.label || opt }}
                                </option>
                            </select>
                        </template>
                        <template v-else-if="field.type === 'number'">
                            <input
                                v-model.number="currentForm[field.prop]"
                                type="number"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                :placeholder="field.placeholder"
                            />
                        </template>
                        <template v-else-if="field.type === 'password'">
                            <input
                                v-model="currentForm[field.prop]"
                                type="password"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                :placeholder="field.placeholder"
                            />
                        </template>
                        <template v-else-if="field.type === 'switch'">
                            <label class="flex items-center gap-2 text-[13px] text-[#475569]">
                                <input
                                    type="checkbox"
                                    class="h-4 w-4 accent-[#1d4ed8]"
                                    :checked="currentForm[field.prop] === 1"
                                    @change="(e) => (currentForm[field.prop] = e.target.checked ? 1 : 0)"
                                />
                                <span>{{ currentForm[field.prop] === 1 ? '启用' : '禁用' }}</span>
                            </label>
                        </template>
                        <template v-else>
                            <input
                                v-model="currentForm[field.prop]"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                :placeholder="field.placeholder"
                            />
                        </template>
                    </div>
                    <div v-if="modalError" class="rounded-[10px] bg-[#fef2f2] px-3 py-2 text-[12px] text-[#dc2626]">
                        {{ modalError }}
                    </div>
                </div>
                <div class="mt-5 flex justify-end gap-3">
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-4 py-2 text-[13px] font-semibold text-[#0f172a]"
                        type="button"
                        @click="modalVisible = false"
                    >
                        取消
                    </button>
                    <button
                        class="rounded-[10px] bg-[#1d4ed8] px-4 py-2 text-[13px] font-semibold text-white"
                        type="button"
                        @click="saveForm"
                    >
                        保存
                    </button>
                </div>
            </div>
        </div>

        <div
            v-if="errorDialog.visible"
            class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.45)] px-4"
        >
            <div class="absolute inset-0 bg-[rgba(220,38,38,0.08)]"></div>
            <div class="relative w-full max-w-[420px] rounded-[14px] bg-white p-6 shadow-lg shadow-[0_20px_60px_rgba(220,38,38,0.25)] border border-[#fecdd3]">
                <div class="mb-3 text-[16px] font-semibold text-[#0f172a]">操作失败</div>
                <div class="text-[13px] text-[#475569]">{{ errorDialog.message }}</div>
                <div class="mt-5 flex justify-end gap-2">
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-4 py-2 text-[13px] font-semibold text-[#0f172a]"
                        type="button"
                        @click="errorDialog.visible = false"
                    >
                        知道了
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
