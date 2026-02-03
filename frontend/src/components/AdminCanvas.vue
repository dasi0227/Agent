<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import SidebarAdmin from './SidebarAdmin.vue';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { useAuthStore } from '../router/pinia';
import {
    adminAgentList,
    adminInsert,
    adminUpdate,
    configDelete,
    configInsert,
    configList,
    configToggle,
    configUpdate,
    flowAgent,
    flowClients,
    flowDelete,
    flowInsert,
    flowUpdate,
    listAgentType,
    listApiId,
    listClientRole,
    listClientType,
    listConfigType,
    listModelId
} from '../request/api';
import { normalizeError } from '../request/request';
import { VueFlow, useVueFlow, MarkerType, Handle, Position } from '@vue-flow/core';
import { Background } from '@vue-flow/background';
import { Controls } from '@vue-flow/controls';
import { MiniMap } from '@vue-flow/minimap';
import '@vue-flow/core/dist/style.css';
import '@vue-flow/controls/dist/style.css';
import '@vue-flow/minimap/dist/style.css';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const menuGroups = adminMenuGroups;
const currentKey = ref('flow');

const agentId = computed(() => String(route.query.agentId || '').trim());

const loading = reactive({
    page: false,
    save: false
});

const agentInfo = ref(null);
const flowList = ref([]);
const clientDetails = ref([]);
const configMap = ref({});
const configTypes = ref([]);

const nodes = ref([]);
const edges = ref([]);

const options = reactive({
    clientTypes: [],
    clientRoles: [],
    modelIds: [],
    apiIds: [],
    agentTypes: []
});

const errorDialog = reactive({ visible: false, message: '' });
const showError = (msg) => {
    errorDialog.visible = true;
    errorDialog.message = msg || '操作失败';
};
const closeError = () => {
    errorDialog.visible = false;
    errorDialog.message = '';
};

const unwrapResult = (resp, msg = '操作失败') => {
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

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) router.push(target.path);
};

const { fitView } = useVueFlow();

const refreshOptions = async () => {
    try {
        const [clientTypes, clientRoles, modelIds, apiIds, agentTypes] = await Promise.all([
            listClientType(),
            listClientRole(),
            listModelId(),
            listApiId(),
            listAgentType()
        ]);
        options.clientTypes = unwrapResult(clientTypes, '获取 clientType 失败') || [];
        options.clientRoles = unwrapResult(clientRoles, '获取 clientRole 失败') || [];
        options.modelIds = unwrapResult(modelIds, '获取 modelId 失败') || [];
        options.apiIds = unwrapResult(apiIds, '获取 apiId 失败') || [];
        options.agentTypes = unwrapResult(agentTypes, '获取 agentType 失败') || [];
    } catch (err) {
        options.clientTypes = [];
        options.clientRoles = [];
        options.modelIds = [];
        options.apiIds = [];
        options.agentTypes = [];
        showError(normalizeError(err).message || '加载选项失败');
    }
};

const fetchData = async () => {
    if (!agentId.value) {
        showError('缺少 agentId');
        return;
    }
    loading.page = true;
    try {
        const [agentResp, flowResp, clientResp, configResp, configTypeResp] = await Promise.all([
            adminAgentList({ idKeyword: agentId.value }),
            flowAgent(agentId.value),
            flowClients(),
            configList({}),
            listConfigType()
        ]);
        const agentList = unwrapResult(agentResp, '获取 Agent 失败') || [];
        agentInfo.value = agentList.find((item) => item.agentId === agentId.value) || null;
        flowList.value = unwrapResult(flowResp, '获取 Flow 失败') || [];
        clientDetails.value = unwrapResult(clientResp, '获取 Client 失败') || [];
        configMap.value = unwrapResult(configResp, '获取 Config 失败') || {};
        configTypes.value = unwrapResult(configTypeResp, '获取 ConfigType 失败') || [];
        buildGraph();
        await nextTick();
        fitView({ padding: 0.2 });
    } catch (err) {
        showError(normalizeError(err).message || '加载画布失败');
    } finally {
        loading.page = false;
    }
};

const clientDetailMap = computed(() => {
    const map = new Map();
    (clientDetails.value || []).forEach((item) => map.set(item.clientId, item));
    return map;
});

const flowMapByClient = computed(() => {
    const map = new Map();
    (flowList.value || []).forEach((item) => map.set(item.clientId, item));
    return map;
});

const configMapByClient = computed(() => configMap.value || {});

const CLIENT_X_GAP = 160;
const DEP_X_GAP = 240;
const NODE_WIDTH = 220;
const CLIENT_Y = 0;
const AGENT_Y = -220;
const DEP_Y = 200;
const API_Y = 360;

const buildGraph = () => {
    const builtNodes = [];
    const builtEdges = [];

    if (!agentInfo.value) {
        nodes.value = [];
        edges.value = [];
        return;
    }

    const flows = [...(flowList.value || [])].sort((a, b) => (a.flowSeq || 0) - (b.flowSeq || 0));
    const flowClientIds = flows.map((f) => f.clientId).filter(Boolean);

    const clientNodes = [];
    const dependencyNodes = [];
    const apiNodes = [];

    const agentNodeId = `agent:${agentInfo.value.agentId}`;
    let currentX = 0;
    const clientXs = [];
    flowClientIds.forEach((clientId, idx) => {
        const detail = clientDetailMap.value.get(clientId) || { clientId };
        const flow = flowMapByClient.value.get(clientId) || null;
        const clientNodeId = `client:${clientId}`;
        const clientData = {
            clientId,
            flow,
            flowSeq: flow?.flowSeq ?? '-',
            client: detail.client || {},
            clientRole: detail.clientRole || flow?.clientRole || detail.client?.clientRole || '-'
        };
        const dependencyList = [];
        const modelId = detail.model?.modelId || detail.client?.modelId;
        if (modelId) {
            dependencyList.push({
                type: 'model',
                id: `model:${clientId}:${modelId}`,
                data: detail.model || { modelId, apiId: detail.model?.apiId || detail.api?.apiId }
            });
        }
        const mcpList = [...(detail.mcpList || [])];
        mcpList.forEach((mcp) => {
            if (!mcp?.mcpId) return;
            dependencyList.unshift({
                type: 'mcp',
                id: `mcp:${clientId}:${mcp.mcpId}`,
                data: mcp
            });
        });
        (detail.promptList || []).forEach((prompt) => {
            if (!prompt?.promptId) return;
            dependencyList.push({
                type: 'prompt',
                id: `prompt:${clientId}:${prompt.promptId}`,
                data: prompt
            });
        });
        (detail.advisorList || []).forEach((advisor) => {
            if (!advisor?.advisorId) return;
            dependencyList.push({
                type: 'advisor',
                id: `advisor:${clientId}:${advisor.advisorId}`,
                data: advisor
            });
        });

        const depCount = Math.max(1, dependencyList.length);
        const depSpan = (depCount - 1) * DEP_X_GAP;
        const blockWidth = Math.max(NODE_WIDTH, depSpan + NODE_WIDTH);
        const baseX = currentX + blockWidth / 2;
        clientXs.push(baseX);
        clientNodes.push({
            id: clientNodeId,
            type: 'client',
            position: { x: baseX, y: CLIENT_Y },
            data: clientData
        });

        const startX = baseX - depSpan / 2;
        dependencyList.forEach((dep, depIndex) => {
            const depX = startX + depIndex * DEP_X_GAP;
            dependencyNodes.push({
                id: dep.id,
                type: dep.type,
                position: { x: depX, y: DEP_Y },
                data: dep.data
            });

            if (dep.type === 'model') {
                const apiId = detail.api?.apiId || dep.data?.apiId || detail.model?.apiId;
                if (apiId) {
                    apiNodes.push({
                        id: `api:${clientId}:${apiId}`,
                        type: 'api',
                        position: { x: depX, y: API_Y },
                        data: detail.api || { apiId, apiBaseUrl: '-', apiCompletionsPath: '-' }
                    });
                }
            }
        });
        currentX += blockWidth + CLIENT_X_GAP;
    });

    const agentX = clientXs.length ? (Math.min(...clientXs) + Math.max(...clientXs)) / 2 : 0;
    builtNodes.push({
        id: agentNodeId,
        type: 'agent',
        position: { x: agentX, y: AGENT_Y },
        data: { ...agentInfo.value }
    });
    builtNodes.push(...clientNodes, ...dependencyNodes, ...apiNodes);

    const edgeStyle = { stroke: '#94a3b8', strokeWidth: 1.5 };

    const firstFlow = flows.find((f) => f.flowSeq === 1);
    if (firstFlow?.clientId) {
        builtEdges.push({
            id: `edge:${agentNodeId}->client:${firstFlow.clientId}`,
            source: agentNodeId,
            target: `client:${firstFlow.clientId}`,
            markerEnd: MarkerType.ArrowClosed,
            style: edgeStyle
        });
    }

    for (let i = 0; i < flows.length - 1; i += 1) {
        const current = flows[i];
        const next = flows[i + 1];
        if (current?.clientId && next?.clientId) {
            builtEdges.push({
                id: `edge:client:${current.clientId}->client:${next.clientId}`,
                source: `client:${current.clientId}`,
                target: `client:${next.clientId}`,
                markerEnd: MarkerType.ArrowClosed,
                style: edgeStyle
            });
        }
    }

    flowClientIds.forEach((clientId) => {
        const detail = clientDetailMap.value.get(clientId) || {};
        const modelId = detail.model?.modelId || detail.client?.modelId;
        const clientPrefix = `client:${clientId}`;
        const modelNodeId = modelId ? `model:${clientId}:${modelId}` : null;
        if (modelNodeId) {
            builtEdges.push({
                id: `edge:${clientPrefix}->${modelNodeId}`,
                source: clientPrefix,
                target: modelNodeId,
                markerEnd: MarkerType.ArrowClosed,
                style: edgeStyle
            });
        }

        const apiId = detail.api?.apiId || detail.model?.apiId;
        if (modelNodeId && apiId) {
            builtEdges.push({
                id: `edge:${modelNodeId}->api:${clientId}:${apiId}`,
                source: modelNodeId,
                target: `api:${clientId}:${apiId}`,
                markerEnd: MarkerType.ArrowClosed,
                style: edgeStyle
            });
        }

        (detail.promptList || []).forEach((prompt) => {
            if (!prompt?.promptId) return;
            builtEdges.push({
                id: `edge:client:${clientId}->prompt:${clientId}:${prompt.promptId}`,
                source: `client:${clientId}`,
                target: `prompt:${clientId}:${prompt.promptId}`,
                markerEnd: MarkerType.ArrowClosed,
                style: edgeStyle
            });
        });
        (detail.advisorList || []).forEach((advisor) => {
            if (!advisor?.advisorId) return;
            builtEdges.push({
                id: `edge:client:${clientId}->advisor:${clientId}:${advisor.advisorId}`,
                source: `client:${clientId}`,
                target: `advisor:${clientId}:${advisor.advisorId}`,
                markerEnd: MarkerType.ArrowClosed,
                style: edgeStyle
            });
        });
        (detail.mcpList || []).forEach((mcp) => {
            if (!mcp?.mcpId) return;
            builtEdges.push({
                id: `edge:client:${clientId}->mcp:${clientId}:${mcp.mcpId}`,
                source: `client:${clientId}`,
                target: `mcp:${clientId}:${mcp.mcpId}`,
                markerEnd: MarkerType.ArrowClosed,
                style: edgeStyle
            });
        });
    });

    nodes.value = builtNodes;
    edges.value = builtEdges;
};

const createModalVisible = ref(false);
const modalVisible = ref(false);
const modalType = ref('');
const modalTitle = ref('');
const modalError = ref('');
const editingId = ref(null);
const currentForm = reactive({});
const flowForm = reactive({
    id: null,
    flowSeq: 1,
    flowPrompt: 'auto',
    clientRole: ''
});

const activeClientId = ref('');
const configEditMap = reactive({});
const configNewMap = reactive({
    prompt: { configValue: '', configParam: '', configStatus: 1, configType: '' },
    advisor: { configValue: '', configParam: '', configStatus: 1, configType: '' },
    mcp: { configValue: '', configParam: '', configStatus: 1, configType: '' }
});
const pendingCreate = reactive({
    draftId: '',
    draftType: '',
    relationType: '',
    sourceId: '',
    configType: ''
});

const formSchemas = {
    agent: {
        moduleKey: 'agent',
        title: 'Agent',
        fields: [
            { prop: 'agentId', label: 'Agent ID', required: true },
            { prop: 'agentName', label: '名称', required: true },
            { prop: 'agentType', label: '类型', type: 'select', optionsKey: 'agentTypes', required: true },
            { prop: 'agentDesc', label: '描述', type: 'textarea' },
            { prop: 'agentStatus', label: '状态', type: 'switch' }
        ],
        defaults: () => ({
            id: null,
            agentId: '',
            agentName: '',
            agentType: '',
            agentDesc: '',
            agentStatus: 1
        })
    },
    client: {
        moduleKey: 'client',
        title: 'Client',
        fields: [
            { prop: 'clientId', label: 'Client ID', required: true },
            { prop: 'clientName', label: '名称', required: true },
            { prop: 'clientType', label: '类型', type: 'select', optionsKey: 'clientTypes', required: true },
            { prop: 'clientRole', label: '角色', type: 'select', optionsKey: 'clientRoles', required: true },
            { prop: 'modelId', label: '模型 ID', type: 'select', optionsKey: 'modelIds', required: true },
            { prop: 'clientDesc', label: '描述', type: 'textarea' },
            { prop: 'clientStatus', label: '状态', type: 'switch' }
        ],
        defaults: () => ({
            id: null,
            clientId: '',
            clientName: '',
            clientType: '',
            clientRole: '',
            modelId: '',
            clientDesc: '',
            clientStatus: 1
        })
    },
    model: {
        moduleKey: 'model',
        title: 'Model',
        fields: [
            { prop: 'modelId', label: 'Model ID', required: true },
            { prop: 'modelName', label: '名称', required: true },
            { prop: 'modelType', label: '类型', required: true },
            { prop: 'apiId', label: 'API', type: 'select', optionsKey: 'apiIds', required: true }
        ],
        defaults: () => ({
            id: null,
            modelId: '',
            modelName: '',
            modelType: '',
            apiId: ''
        })
    },
    api: {
        moduleKey: 'api',
        title: 'API',
        fields: [
            { prop: 'apiId', label: 'API ID', required: true },
            { prop: 'apiBaseUrl', label: 'Base URL', required: true },
            { prop: 'apiKey', label: 'Key', required: true },
            { prop: 'apiCompletionsPath', label: '对话路径', required: true },
            { prop: 'apiEmbeddingsPath', label: 'Embedding 路径', required: true }
        ],
        defaults: () => ({
            id: null,
            apiId: '',
            apiBaseUrl: '',
            apiKey: '',
            apiCompletionsPath: '',
            apiEmbeddingsPath: ''
        })
    },
    prompt: {
        moduleKey: 'prompt',
        title: 'Prompt',
        fields: [
            { prop: 'promptId', label: 'Prompt ID', required: true },
            { prop: 'promptName', label: '名称', required: true },
            { prop: 'promptContent', label: '内容', type: 'textarea', required: true },
            { prop: 'promptDesc', label: '描述', type: 'textarea' }
        ],
        defaults: () => ({
            id: null,
            promptId: '',
            promptName: '',
            promptContent: '',
            promptDesc: ''
        })
    },
    advisor: {
        moduleKey: 'advisor',
        title: 'Advisor',
        fields: [
            { prop: 'advisorId', label: 'Advisor ID', required: true },
            { prop: 'advisorName', label: '名称', required: true },
            { prop: 'advisorType', label: '类型', required: true },
            { prop: 'advisorOrder', label: '顺序', type: 'number' },
            { prop: 'advisorDesc', label: '描述', type: 'textarea' },
            { prop: 'advisorParam', label: '参数', type: 'textarea' }
        ],
        defaults: () => ({
            id: null,
            advisorId: '',
            advisorName: '',
            advisorType: '',
            advisorOrder: 0,
            advisorDesc: '',
            advisorParam: ''
        })
    },
    mcp: {
        moduleKey: 'mcp',
        title: 'MCP',
        fields: [
            { prop: 'mcpId', label: 'MCP ID', required: true },
            { prop: 'mcpName', label: '名称', required: true },
            { prop: 'mcpType', label: '类型', required: true },
            { prop: 'mcpConfig', label: '配置', type: 'textarea', required: true },
            { prop: 'mcpDesc', label: '描述', type: 'textarea' },
            { prop: 'mcpTimeout', label: '超时时间', type: 'number' },
            { prop: 'mcpChat', label: '聊天可用', type: 'switch' }
        ],
        defaults: () => ({
            id: null,
            mcpId: '',
            mcpName: '',
            mcpType: '',
            mcpConfig: '',
            mcpDesc: '',
            mcpTimeout: 180,
            mcpChat: 0
        })
    }
};

const currentSchema = computed(() => formSchemas[modalType.value]);

const resetForm = (schema) => {
    const defaults = schema?.defaults ? schema.defaults() : {};
    Object.keys(currentForm).forEach((key) => delete currentForm[key]);
    Object.assign(currentForm, defaults);
};

const openCreateModal = (type) => {
    modalType.value = type;
    modalTitle.value = `新增 ${formSchemas[type]?.title || ''}`;
    editingId.value = null;
    modalError.value = '';
    resetForm(formSchemas[type]);
    activeClientId.value = '';
    modalVisible.value = true;
};

const openNodeModal = (node) => {
    if (!node?.type) return;
    if (node.data?.draft) {
        showError('请先建立连接后再编辑');
        return;
    }
    modalType.value = node.type;
    modalTitle.value = `编辑 ${formSchemas[node.type]?.title || ''}`;
    modalError.value = '';
    const schema = formSchemas[node.type];
    resetForm(schema);

    if (node.type === 'agent') {
        const payload = node.data || {};
        editingId.value = payload.id || null;
        Object.assign(currentForm, payload);
        currentForm.agentStatus = payload.agentStatus ?? 1;
    }
    if (node.type === 'client') {
        const detail = clientDetailMap.value.get(node.data.clientId) || {};
        const client = detail.client || {};
        editingId.value = client.id || null;
        Object.assign(currentForm, {
            id: client.id || null,
            clientId: client.clientId || node.data.clientId,
            clientName: client.clientName || '',
            clientType: client.clientType || '',
            clientRole: client.clientRole || detail.clientRole || node.data.clientRole || '',
            modelId: client.modelId || detail.model?.modelId || '',
            clientDesc: client.clientDesc || '',
            clientStatus: client.clientStatus ?? 1
        });
        activeClientId.value = currentForm.clientId;
        const flow = flowMapByClient.value.get(currentForm.clientId);
        Object.assign(flowForm, {
            id: flow?.id || null,
            flowSeq: flow?.flowSeq || 1,
            flowPrompt: flow?.flowPrompt || 'auto',
            clientRole: flow?.clientRole || currentForm.clientRole || ''
        });
        prepareConfigForms();
    }
    if (node.type === 'model') {
        const payload = node.data || {};
        editingId.value = payload.id || null;
        Object.assign(currentForm, payload);
    }
    if (node.type === 'api') {
        const payload = node.data || {};
        editingId.value = payload.id || null;
        Object.assign(currentForm, payload);
    }
    if (node.type === 'prompt') {
        const payload = node.data || {};
        editingId.value = payload.id || null;
        Object.assign(currentForm, payload);
    }
    if (node.type === 'advisor') {
        const payload = node.data || {};
        editingId.value = payload.id || null;
        Object.assign(currentForm, payload);
    }
    if (node.type === 'mcp') {
        const payload = node.data || {};
        editingId.value = payload.id || null;
        Object.assign(currentForm, payload);
        currentForm.mcpChat = payload.mcpChat ?? 0;
    }
    modalVisible.value = true;
};

const validateSchema = () => {
    const schema = currentSchema.value;
    if (!schema) return false;
    for (const field of schema.fields) {
        if (field.required && !currentForm[field.prop]) {
            modalError.value = `${field.label} 不能为空`;
            return false;
        }
    }
    modalError.value = '';
    return true;
};

const saveEntity = async () => {
    if (!validateSchema()) return;
    loading.save = true;
    try {
        if (editingId.value) {
            await unwrapResult(adminUpdate(currentSchema.value.moduleKey, currentForm), '更新失败');
        } else {
            await unwrapResult(adminInsert(currentSchema.value.moduleKey, currentForm), '创建失败');
            if (pendingCreate.draftId) {
                await handlePendingRelationAfterCreate();
            }
        }
        modalVisible.value = false;
        await fetchData();
    } catch (err) {
        modalError.value = normalizeError(err).message || '保存失败';
    } finally {
        loading.save = false;
    }
};

const saveFlow = async () => {
    if (!activeClientId.value) {
        modalError.value = '缺少 Client ID';
        return;
    }
    if (!flowForm.flowSeq || flowForm.flowSeq < 1) {
        modalError.value = 'FlowSeq 不能为空';
        return;
    }
    if (!flowForm.flowPrompt) {
        modalError.value = 'FlowPrompt 不能为空';
        return;
    }
    if (!flowForm.clientRole) {
        modalError.value = 'ClientRole 不能为空';
        return;
    }
    loading.save = true;
    try {
        const payload = {
            id: flowForm.id || null,
            agentId: agentId.value,
            clientId: activeClientId.value,
            clientRole: flowForm.clientRole,
            flowPrompt: flowForm.flowPrompt,
            flowSeq: flowForm.flowSeq
        };
        if (flowForm.id) {
            await unwrapResult(flowUpdate(payload), '更新 Flow 失败');
        } else {
            await unwrapResult(flowInsert(payload), '新增 Flow 失败');
        }
        modalVisible.value = false;
        await fetchData();
    } catch (err) {
        modalError.value = normalizeError(err).message || '保存 Flow 失败';
    } finally {
        loading.save = false;
    }
};

const deleteFlow = async () => {
    if (!flowForm.id) return;
    loading.save = true;
    try {
        await unwrapResult(flowDelete(flowForm.id), '删除 Flow 失败');
        modalVisible.value = false;
        await fetchData();
    } catch (err) {
        modalError.value = normalizeError(err).message || '删除 Flow 失败';
    } finally {
        loading.save = false;
    }
};

const resolveConfigType = (keyword) => {
    const lower = keyword.toLowerCase();
    return configTypes.value.find((item) => String(item || '').toLowerCase().includes(lower)) || '';
};

const prepareConfigForms = () => {
    Object.keys(configEditMap).forEach((key) => delete configEditMap[key]);
    Object.assign(configNewMap, {
        prompt: { configValue: '', configParam: '', configStatus: 1, configType: resolveConfigType('prompt') },
        advisor: { configValue: '', configParam: '', configStatus: 1, configType: resolveConfigType('advisor') },
        mcp: { configValue: '', configParam: '', configStatus: 1, configType: resolveConfigType('mcp') }
    });

    const list = configMapByClient.value[activeClientId.value] || [];
    list.forEach((item) => {
        configEditMap[item.id] = {
            configValue: item.configValue,
            configParam: item.configParam,
            configStatus: item.configStatus ?? 1
        };
    });
};

const configGroups = computed(() => {
    const list = configMapByClient.value[activeClientId.value] || [];
    const groups = { prompt: [], advisor: [], mcp: [] };
    list.forEach((item) => {
        const type = String(item.configType || '').toLowerCase();
        if (type.includes('prompt')) groups.prompt.push(item);
        else if (type.includes('advisor')) groups.advisor.push(item);
        else if (type.includes('mcp')) groups.mcp.push(item);
    });
    return groups;
});

const saveConfigItem = async (item) => {
    if (!item?.id) return;
    const draft = configEditMap[item.id];
    if (!draft?.configValue) {
        modalError.value = 'ConfigValue 不能为空';
        return;
    }
    loading.save = true;
    try {
        await unwrapResult(
            configUpdate({
                id: item.id,
                clientId: item.clientId,
                configType: item.configType,
                configValue: draft.configValue,
                configParam: draft.configParam,
                configStatus: draft.configStatus ?? 1
            }),
            '更新配置失败'
        );
        modalVisible.value = false;
        await fetchData();
    } catch (err) {
        modalError.value = normalizeError(err).message || '更新配置失败';
    } finally {
        loading.save = false;
    }
};

const deleteConfigItem = async (item) => {
    if (!item?.id) return;
    loading.save = true;
    try {
        await unwrapResult(configDelete(item.id), '删除配置失败');
        modalVisible.value = false;
        await fetchData();
    } catch (err) {
        modalError.value = normalizeError(err).message || '删除配置失败';
    } finally {
        loading.save = false;
    }
};

const addConfigItem = async (groupKey) => {
    if (!activeClientId.value) {
        modalError.value = '缺少 Client ID';
        return;
    }
    const draft = configNewMap[groupKey];
    if (!draft?.configValue) {
        modalError.value = 'ConfigValue 不能为空';
        return;
    }
    const configType = draft.configType || resolveConfigType(groupKey);
    if (!configType) {
        modalError.value = '请选择 ConfigType';
        return;
    }
    loading.save = true;
    try {
        await unwrapResult(
            configInsert({
                clientId: activeClientId.value,
                configType,
                configValue: draft.configValue,
                configParam: draft.configParam,
                configStatus: draft.configStatus ?? 1
            }),
            '新增配置失败'
        );
        modalVisible.value = false;
        await fetchData();
    } catch (err) {
        modalError.value = normalizeError(err).message || '新增配置失败';
    } finally {
        loading.save = false;
    }
};

const toggleConfigStatus = async (item) => {
    if (!item?.id) return;
    const draft = configEditMap[item.id];
    const next = draft.configStatus === 1 ? 0 : 1;
    draft.configStatus = next;
    try {
        await unwrapResult(configToggle(item.id, next), '更新配置状态失败');
    } catch (err) {
        draft.configStatus = next === 1 ? 0 : 1;
        modalError.value = normalizeError(err).message || '更新配置状态失败';
    }
};

const openCreate = () => {
    createModalVisible.value = true;
};

const lastClickState = reactive({ id: '', ts: 0 });
const handleNodeClick = (event, node) => {
    const targetNode = node || event?.node;
    if (!targetNode?.id) return;
    const now = Date.now();
    if (lastClickState.id === targetNode.id && now - lastClickState.ts < 320) {
        lastClickState.id = '';
        lastClickState.ts = 0;
        openNodeModal(targetNode);
        return;
    }
    lastClickState.id = targetNode.id;
    lastClickState.ts = now;
};

const isDraftNode = (node) => Boolean(node?.data?.draft);

const removeDraftNode = (draftId) => {
    if (!draftId) return;
    nodes.value = (nodes.value || []).filter((item) => item.id !== draftId);
};

const createDraftNode = (type) => {
    const draftId = `draft:${type}:${Date.now()}`;
    nodes.value = [
        ...(nodes.value || []),
        {
            id: draftId,
            type,
            position: { x: 0, y: DEP_Y },
            data: { draft: true }
        }
    ];
    return draftId;
};

const resetPendingCreate = () => {
    pendingCreate.draftId = '';
    pendingCreate.draftType = '';
    pendingCreate.relationType = '';
    pendingCreate.sourceId = '';
    pendingCreate.configType = '';
};

const handlePendingRelationAfterCreate = async () => {
    const draftType = pendingCreate.draftType;
    const draftId = pendingCreate.draftId;
    if (!draftType || !draftId) return;

    if (draftType === 'client') {
        const role = flowForm.clientRole || currentForm.clientRole;
        await unwrapResult(
            flowInsert({
                agentId: agentId.value,
                clientId: currentForm.clientId,
                clientRole: role,
                flowPrompt: flowForm.flowPrompt,
                flowSeq: flowForm.flowSeq
            }),
            '新增 Flow 失败'
        );
    }

    if (draftType === 'model') {
        const clientDetail = clientDetailMap.value.get(pendingCreate.sourceId)?.client;
        if (!clientDetail) throw new Error('缺少 Client 信息，无法绑定 Model');
        await unwrapResult(
            adminUpdate('client', {
                id: clientDetail.id,
                clientId: clientDetail.clientId,
                clientName: clientDetail.clientName,
                clientType: clientDetail.clientType,
                clientRole: clientDetail.clientRole,
                modelId: currentForm.modelId,
                clientDesc: clientDetail.clientDesc,
                clientStatus: clientDetail.clientStatus ?? 1
            }),
            '更新 Client 失败'
        );
    }

    if (draftType === 'api') {
        const modelDetail = nodes.value.find((item) => item.id === pendingCreate.sourceId)?.data;
        if (!modelDetail) throw new Error('缺少 Model 信息，无法绑定 API');
        await unwrapResult(
            adminUpdate('model', {
                id: modelDetail.id,
                modelId: modelDetail.modelId,
                modelName: modelDetail.modelName,
                modelType: modelDetail.modelType,
                apiId: currentForm.apiId
            }),
            '更新 Model 失败'
        );
    }

    if (draftType === 'prompt' || draftType === 'advisor' || draftType === 'mcp') {
        const configType = pendingCreate.configType || resolveConfigType(draftType);
        if (!configType) throw new Error('请选择 ConfigType');
        const idField = `${draftType}Id`;
        await unwrapResult(
            configInsert({
                clientId: pendingCreate.sourceId,
                configType,
                configValue: currentForm[idField],
                configParam: '',
                configStatus: 1
            }),
            '新增配置失败'
        );
    }

    removeDraftNode(draftId);
    resetPendingCreate();
};

const validateConnection = (sourceNode, targetNode) => {
    if (!sourceNode || !targetNode) return false;
    const sType = sourceNode.type;
    const tType = targetNode.type;
    if (sType === 'agent' && tType === 'client') return true;
    if (sType === 'client' && tType === 'client') return true;
    if (sType === 'client' && ['model', 'prompt', 'advisor', 'mcp'].includes(tType)) return true;
    if (sType === 'model' && tType === 'api') return true;
    return false;
};

const handleConnect = ({ source, target }) => {
    const sourceNode = nodes.value.find((item) => item.id === source);
    const targetNode = nodes.value.find((item) => item.id === target);
    if (!sourceNode || !targetNode) return;

    const draftNode = isDraftNode(sourceNode) ? sourceNode : isDraftNode(targetNode) ? targetNode : null;
    if (!draftNode) {
        showError('请连接到新增节点');
        return;
    }
    const existingNode = draftNode.id === sourceNode.id ? targetNode : sourceNode;
    if (draftNode.id !== target) {
        showError('请从已有节点拖拽到新增节点');
        return;
    }
    if (!validateConnection(existingNode, draftNode)) {
        showError('连接规则不允许');
        return;
    }

    if (draftNode.type === 'model') {
        const clientId = existingNode.data?.clientId || existingNode.id?.split(':')[1];
        const detail = clientDetailMap.value.get(clientId);
        if (detail?.model?.modelId || detail?.client?.modelId) {
            showError('一个 Client 只能绑定一个 Model');
            return;
        }
    }
    if (draftNode.type === 'api') {
        const modelApi = existingNode.data?.apiId;
        if (modelApi) {
            showError('一个 Model 只能绑定一个 API');
            return;
        }
    }

    resetPendingCreate();
    pendingCreate.draftId = draftNode.id;
    pendingCreate.draftType = draftNode.type;

    if (draftNode.type === 'client') {
        const maxSeq = Math.max(0, ...flowList.value.map((item) => item.flowSeq || 0));
        flowForm.flowSeq = maxSeq + 1;
        flowForm.flowPrompt = 'auto';
        flowForm.clientRole = '';
    }

    if (['prompt', 'advisor', 'mcp', 'model'].includes(draftNode.type)) {
        pendingCreate.sourceId = existingNode.data?.clientId || existingNode.id?.split(':')[1] || '';
    }
    if (draftNode.type === 'api') {
        pendingCreate.sourceId = existingNode.id;
    }
    if (['prompt', 'advisor', 'mcp'].includes(draftNode.type)) {
        pendingCreate.relationType = 'config';
        pendingCreate.configType = resolveConfigType(draftNode.type);
    }

    openCreateModal(draftNode.type);
};

const handleModalCancel = () => {
    if (pendingCreate.draftId) {
        removeDraftNode(pendingCreate.draftId);
        resetPendingCreate();
    }
    modalVisible.value = false;
};

onMounted(async () => {
    await refreshOptions();
    await fetchData();
});
</script>

<template>
    <div class="admin-font flex h-screen bg-[#f8fafc]">
        <SidebarAdmin :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">
                    CONFIG CANVAS
                    <span v-if="agentId" class="ml-2 text-[14px] font-normal text-[#64748b]">/ {{ agentId }}</span>
                </div>
            </header>

            <div class="flex items-center justify-between gap-3 border-b border-[#e2e8f0] bg-white px-6 py-3">
                <div class="text-[14px] text-[#475569]">
                    {{ agentInfo?.agentName || agentId || '未选择 Agent' }}
                </div>
                <div class="flex items-center gap-2">
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f8fafc]"
                        type="button"
                        @click="openCreate"
                    >
                        新增节点
                    </button>
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f8fafc]"
                        type="button"
                        @click="router.push(`/admin/flow?agentId=${agentId}`)"
                    >
                        返回 FLOW
                    </button>
                </div>
            </div>

            <div class="flex-1 overflow-hidden p-4">
                <div v-if="loading.page" class="flex h-full items-center justify-center text-[13px] text-[#94a3b8]">加载中...</div>
                <div v-else class="h-full rounded-[14px] border border-[#e2e8f0] bg-white">
                    <VueFlow
                        class="h-full"
                        v-model:nodes="nodes"
                        v-model:edges="edges"
                        :nodes-draggable="true"
                        :nodes-connectable="true"
                        :zoom-on-scroll="true"
                        :pan-on-drag="true"
                        :fit-view-on-init="true"
                        @node-click="handleNodeClick"
                        @connect="handleConnect"
                    >
                        <Background :gap="18" color="#e2e8f0" />
                        <Controls />
                        <MiniMap />

                        <template #node-agent="{ data }">
                            <div :class="['canvas-card', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="source" :position="Position.Bottom" class="canvas-handle" />
                                <div class="canvas-id">{{ data.agentId }}</div>
                                <div class="canvas-text">名称：{{ data.agentName || '-' }}</div>
                                <div class="canvas-text">类型：{{ data.agentType || '-' }}</div>
                                <div class="canvas-desc">概述：{{ data.agentDesc || '-' }}</div>
                            </div>
                        </template>

                        <template #node-client="{ data }">
                            <div :class="['canvas-card', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="target" :position="Position.Top" class="canvas-handle" />
                                <Handle type="source" :position="Position.Bottom" class="canvas-handle" />
                                <div class="canvas-id">{{ data.clientId }}</div>
                                <div class="canvas-text">名称：{{ data.client?.clientName || '-' }}</div>
                                <div class="canvas-text">类型：{{ data.client?.clientType || '-' }}</div>
                                <div class="canvas-text">角色：{{ data.client?.clientRole || data.clientRole || '-' }}</div>
                                <div class="canvas-desc">概述：{{ data.client?.clientDesc || '-' }}</div>
                            </div>
                        </template>

                        <template #node-model="{ data }">
                            <div :class="['canvas-card', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="target" :position="Position.Top" class="canvas-handle" />
                                <Handle type="source" :position="Position.Bottom" class="canvas-handle" />
                                <div class="canvas-id">{{ data.modelId }}</div>
                                <div class="canvas-text">名称：{{ data.modelName || '-' }}</div>
                                <div class="canvas-text">类型：{{ data.modelType || '-' }}</div>
                                <div class="canvas-desc">概述：{{ data.modelDesc || '-' }}</div>
                            </div>
                        </template>

                        <template #node-api="{ data }">
                            <div :class="['canvas-card', 'canvas-card-wide', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="target" :position="Position.Top" class="canvas-handle" />
                                <div class="canvas-id">{{ data.apiId }}</div>
                                <div class="canvas-text line-clamp-2">基础路径：{{ data.apiBaseUrl || '-' }}</div>
                                <div class="canvas-text">补全路径：{{ data.apiCompletionsPath || '-' }}</div>
                                <div class="canvas-text">嵌入路径：{{ data.apiEmbeddingsPath || '-' }}</div>
                            </div>
                        </template>

                        <template #node-prompt="{ data }">
                            <div :class="['canvas-card', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="target" :position="Position.Top" class="canvas-handle" />
                                <div class="canvas-id">{{ data.promptId }}</div>
                                <div class="canvas-text">名称：{{ data.promptName || '-' }}</div>
                                <div class="canvas-desc">概述：{{ data.promptDesc || '-' }}</div>
                            </div>
                        </template>

                        <template #node-advisor="{ data }">
                            <div :class="['canvas-card', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="target" :position="Position.Top" class="canvas-handle" />
                                <div class="canvas-id">{{ data.advisorId }}</div>
                                <div class="canvas-text">名称：{{ data.advisorName || '-' }}</div>
                                <div class="canvas-text">类型：{{ data.advisorType || '-' }}</div>
                                <div class="canvas-desc">概述：{{ data.advisorDesc || '-' }}</div>
                            </div>
                        </template>

                        <template #node-mcp="{ data }">
                            <div :class="['canvas-card', data?.draft ? 'canvas-card-draft' : '']">
                                <Handle type="target" :position="Position.Top" class="canvas-handle" />
                                <div class="canvas-id">{{ data.mcpId }}</div>
                                <div class="canvas-text">名称：{{ data.mcpName || '-' }}</div>
                                <div class="canvas-text">类型：{{ data.mcpType || '-' }}</div>
                                <div class="canvas-desc">概述：{{ data.mcpDesc || '-' }}</div>
                            </div>
                        </template>
                    </VueFlow>
                </div>
            </div>
        </div>

        <!-- 新增节点选择弹窗 -->
        <div v-if="createModalVisible" class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.35)] px-4">
            <div class="w-full max-w-[520px] rounded-[14px] bg-white p-6 shadow-lg">
                <div class="mb-4 flex items-center justify-between">
                    <div class="text-[16px] font-semibold text-[#0f172a]">新增节点</div>
                    <button class="text-[14px] text-[#94a3b8]" type="button" @click="createModalVisible = false">✕</button>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <button class="canvas-btn" type="button" @click="createModalVisible = false; createDraftNode('client')">Client</button>
                    <button class="canvas-btn" type="button" @click="createModalVisible = false; createDraftNode('model')">Model</button>
                    <button class="canvas-btn" type="button" @click="createModalVisible = false; createDraftNode('api')">API</button>
                    <button class="canvas-btn" type="button" @click="createModalVisible = false; createDraftNode('prompt')">Prompt</button>
                    <button class="canvas-btn" type="button" @click="createModalVisible = false; createDraftNode('advisor')">Advisor</button>
                    <button class="canvas-btn" type="button" @click="createModalVisible = false; createDraftNode('mcp')">MCP</button>
                </div>
            </div>
        </div>

        <!-- 编辑弹窗 -->
        <div v-if="modalVisible" class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.35)] px-4">
            <div class="w-full max-w-[720px] rounded-[14px] bg-white p-6 shadow-lg max-h-[90vh] overflow-auto">
                <div class="mb-4 flex items-center justify-between">
                    <div class="text-[16px] font-semibold text-[#0f172a]">{{ modalTitle }}</div>
                    <button class="text-[14px] text-[#94a3b8]" type="button" @click="handleModalCancel">✕</button>
                </div>

                <div class="flex flex-col gap-3">
                    <div v-for="field in currentSchema?.fields || []" :key="field.prop" class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">
                            {{ field.label }}
                            <span v-if="field.required" class="text-[#dc2626]">*</span>
                        </label>
                        <template v-if="field.type === 'textarea'">
                            <textarea
                                v-model="currentForm[field.prop]"
                                rows="3"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                            />
                        </template>
                        <template v-else-if="field.type === 'select'">
                            <select
                                v-model="currentForm[field.prop]"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                            >
                                <option value="">请选择</option>
                                <option
                                    v-for="opt in options[field.optionsKey || ''] || []"
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
                            />
                        </template>
                        <template v-else-if="field.type === 'switch'">
                            <button
                                class="relative h-[24px] w-[54px] rounded-full text-[10px] font-semibold uppercase tracking-[0.5px] transition"
                                :class="currentForm[field.prop] === 1 ? 'bg-[#1d4ed8] text-white' : 'bg-[#cbd5e1] text-[#0f172a]'"
                                type="button"
                                @click="currentForm[field.prop] = currentForm[field.prop] === 1 ? 0 : 1"
                            >
                                <span
                                    class="absolute left-[3px] top-[3px] h-[18px] w-[18px] rounded-full bg-white transition"
                                    :class="currentForm[field.prop] === 1 ? 'translate-x-[28px]' : ''"
                                />
                                <span
                                    class="absolute inset-0 flex items-center px-[6px] transition"
                                    :class="currentForm[field.prop] === 1 ? 'justify-start' : 'justify-end'"
                                >
                                    <span>{{ currentForm[field.prop] === 1 ? 'on' : 'off' }}</span>
                                </span>
                            </button>
                        </template>
                        <template v-else>
                            <input
                                v-model="currentForm[field.prop]"
                                class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                            />
                        </template>
                    </div>
                </div>

                <div v-if="modalType === 'client'" class="mt-6 border-t border-[#e2e8f0] pt-5">
                    <div class="text-[14px] font-semibold text-[#0f172a]">Flow 配置（当前 Agent）</div>
                    <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
                        <div class="flex flex-col gap-1">
                            <label class="text-[13px] font-semibold text-[#0f172a]">Flow Seq</label>
                            <input v-model.number="flowForm.flowSeq" type="number" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]" />
                        </div>
                        <div class="flex flex-col gap-1">
                            <label class="text-[13px] font-semibold text-[#0f172a]">Client Role</label>
                            <select v-model="flowForm.clientRole" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]">
                                <option value="">请选择</option>
                                <option v-for="opt in options.clientRoles" :key="opt" :value="opt">{{ opt }}</option>
                            </select>
                        </div>
                        <div class="flex flex-col gap-1 sm:col-span-2">
                            <label class="text-[13px] font-semibold text-[#0f172a]">Flow Prompt</label>
                            <textarea v-model="flowForm.flowPrompt" rows="2" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]" />
                        </div>
                    </div>
                    <div class="mt-4 flex gap-3">
                        <button class="rounded-[10px] bg-[#1d4ed8] px-4 py-2 text-[13px] font-semibold text-white" type="button" @click="saveFlow">保存 Flow</button>
                        <button
                            v-if="flowForm.id"
                            class="rounded-[10px] border border-[#e2e8f0] px-4 py-2 text-[13px] font-semibold text-[#0f172a]"
                            type="button"
                            @click="deleteFlow"
                        >
                            删除 Flow
                        </button>
                    </div>
                </div>

                <div v-if="modalType === 'client'" class="mt-6 border-t border-[#e2e8f0] pt-5">
                    <div class="text-[14px] font-semibold text-[#0f172a]">依赖配置</div>

                    <div class="mt-4 space-y-4">
                        <div>
                            <div class="text-[13px] font-semibold text-[#0f172a]">Prompt</div>
                            <div class="mt-2 space-y-2">
                                <div v-for="item in configGroups.prompt" :key="item.id" class="rounded-[10px] border border-[#e2e8f0] p-3">
                                    <div class="grid gap-2 sm:grid-cols-2">
                                        <input v-model="configEditMap[item.id].configValue" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="Prompt ID" />
                                        <input v-model="configEditMap[item.id].configParam" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="参数" />
                                    </div>
                                    <div class="mt-2 flex items-center justify-between">
                                        <button class="text-[12px] text-[#0f172a]" type="button" @click="toggleConfigStatus(item)">
                                            状态：{{ configEditMap[item.id].configStatus === 1 ? '启用' : '禁用' }}
                                        </button>
                                        <div class="flex gap-2">
                                            <button class="text-[12px] text-[#1d4ed8]" type="button" @click="saveConfigItem(item)">保存</button>
                                            <button class="text-[12px] text-[#dc2626]" type="button" @click="deleteConfigItem(item)">删除</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="rounded-[10px] border border-dashed border-[#e2e8f0] p-3">
                                    <div class="grid gap-2 sm:grid-cols-2">
                                        <input v-model="configNewMap.prompt.configValue" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="Prompt ID" />
                                        <input v-model="configNewMap.prompt.configParam" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="参数" />
                                    </div>
                                    <div v-if="!configNewMap.prompt.configType" class="mt-2">
                                        <select v-model="configNewMap.prompt.configType" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]">
                                            <option value="">选择 ConfigType</option>
                                            <option v-for="opt in configTypes" :key="opt" :value="opt">{{ opt }}</option>
                                        </select>
                                    </div>
                                    <div class="mt-2 flex justify-end">
                                        <button class="text-[12px] text-[#1d4ed8]" type="button" @click="addConfigItem('prompt')">新增 Prompt</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div>
                            <div class="text-[13px] font-semibold text-[#0f172a]">Advisor</div>
                            <div class="mt-2 space-y-2">
                                <div v-for="item in configGroups.advisor" :key="item.id" class="rounded-[10px] border border-[#e2e8f0] p-3">
                                    <div class="grid gap-2 sm:grid-cols-2">
                                        <input v-model="configEditMap[item.id].configValue" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="Advisor ID" />
                                        <input v-model="configEditMap[item.id].configParam" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="参数" />
                                    </div>
                                    <div class="mt-2 flex items-center justify-between">
                                        <button class="text-[12px] text-[#0f172a]" type="button" @click="toggleConfigStatus(item)">
                                            状态：{{ configEditMap[item.id].configStatus === 1 ? '启用' : '禁用' }}
                                        </button>
                                        <div class="flex gap-2">
                                            <button class="text-[12px] text-[#1d4ed8]" type="button" @click="saveConfigItem(item)">保存</button>
                                            <button class="text-[12px] text-[#dc2626]" type="button" @click="deleteConfigItem(item)">删除</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="rounded-[10px] border border-dashed border-[#e2e8f0] p-3">
                                    <div class="grid gap-2 sm:grid-cols-2">
                                        <input v-model="configNewMap.advisor.configValue" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="Advisor ID" />
                                        <input v-model="configNewMap.advisor.configParam" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="参数" />
                                    </div>
                                    <div v-if="!configNewMap.advisor.configType" class="mt-2">
                                        <select v-model="configNewMap.advisor.configType" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]">
                                            <option value="">选择 ConfigType</option>
                                            <option v-for="opt in configTypes" :key="opt" :value="opt">{{ opt }}</option>
                                        </select>
                                    </div>
                                    <div class="mt-2 flex justify-end">
                                        <button class="text-[12px] text-[#1d4ed8]" type="button" @click="addConfigItem('advisor')">新增 Advisor</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div>
                            <div class="text-[13px] font-semibold text-[#0f172a]">MCP</div>
                            <div class="mt-2 space-y-2">
                                <div v-for="item in configGroups.mcp" :key="item.id" class="rounded-[10px] border border-[#e2e8f0] p-3">
                                    <div class="grid gap-2 sm:grid-cols-2">
                                        <input v-model="configEditMap[item.id].configValue" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="MCP ID" />
                                        <input v-model="configEditMap[item.id].configParam" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="参数" />
                                    </div>
                                    <div class="mt-2 flex items-center justify-between">
                                        <button class="text-[12px] text-[#0f172a]" type="button" @click="toggleConfigStatus(item)">
                                            状态：{{ configEditMap[item.id].configStatus === 1 ? '启用' : '禁用' }}
                                        </button>
                                        <div class="flex gap-2">
                                            <button class="text-[12px] text-[#1d4ed8]" type="button" @click="saveConfigItem(item)">保存</button>
                                            <button class="text-[12px] text-[#dc2626]" type="button" @click="deleteConfigItem(item)">删除</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="rounded-[10px] border border-dashed border-[#e2e8f0] p-3">
                                    <div class="grid gap-2 sm:grid-cols-2">
                                        <input v-model="configNewMap.mcp.configValue" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="MCP ID" />
                                        <input v-model="configNewMap.mcp.configParam" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]" placeholder="参数" />
                                    </div>
                                    <div v-if="!configNewMap.mcp.configType" class="mt-2">
                                        <select v-model="configNewMap.mcp.configType" class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]">
                                            <option value="">选择 ConfigType</option>
                                            <option v-for="opt in configTypes" :key="opt" :value="opt">{{ opt }}</option>
                                        </select>
                                    </div>
                                    <div class="mt-2 flex justify-end">
                                        <button class="text-[12px] text-[#1d4ed8]" type="button" @click="addConfigItem('mcp')">新增 MCP</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="modalError" class="mt-4 rounded-[10px] bg-[#fef2f2] px-3 py-2 text-[12px] text-[#dc2626]">
                    {{ modalError }}
                </div>

                <div v-if="pendingCreate.relationType === 'config'" class="mt-4 rounded-[10px] border border-[#e2e8f0] bg-[#f8fafc] p-3">
                    <div class="text-[13px] font-semibold text-[#0f172a]">ConfigType</div>
                    <select v-model="pendingCreate.configType" class="mt-2 rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px]">
                        <option value="">请选择</option>
                        <option v-for="opt in configTypes" :key="opt" :value="opt">{{ opt }}</option>
                    </select>
                </div>

                <div class="mt-5 flex justify-end gap-3">
                    <button class="rounded-[10px] border border-[#e2e8f0] px-4 py-2 text-[13px] font-semibold text-[#0f172a]" type="button" @click="handleModalCancel">
                        取消
                    </button>
                    <button class="rounded-[10px] bg-[#1d4ed8] px-4 py-2 text-[13px] font-semibold text-white" type="button" :disabled="loading.save" @click="saveEntity">
                        {{ loading.save ? '保存中...' : '保存' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 错误弹窗 -->
        <div v-if="errorDialog.visible" class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.45)] px-4" @click="closeError">
            <div class="absolute inset-0 bg-[rgba(220,38,38,0.08)]"></div>
            <div class="relative w-full max-w-[420px] rounded-[14px] bg-white p-6 shadow-lg shadow-[0_20px_60px_rgba(220,38,38,0.25)] border border-[#fecdd3]" @click.stop>
                <div class="mb-2 text-[16px] font-semibold text-[#0f172a]">操作失败</div>
                <div class="text-[13px] text-[#475569]">{{ errorDialog.message }}</div>
                <div class="mt-4 flex justify-end">
                    <button class="rounded-[10px] bg-[#ef4444] px-4 py-2 text-[13px] font-semibold text-white" type="button" @click="closeError">
                        确认
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

.canvas-card {
    width: 220px;
    padding: 10px 12px;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    background: #ffffff;
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.06);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.canvas-card-wide {
    width: 280px;
}

.canvas-handle {
    width: 10px;
    height: 10px;
    background: #94a3b8;
    border: 2px solid #ffffff;
    border-radius: 999px;
}

.vue-flow__node:hover .canvas-card {
    border-color: #0ea5e9;
    box-shadow: 0 6px 16px rgba(14, 165, 233, 0.18);
}

.canvas-card-draft {
    border-style: dashed;
    background: #f8fafc;
}

.canvas-id {
    text-align: center;
    font-size: 14px;
    font-weight: 700;
    color: #0f172a;
}

.canvas-text {
    margin-top: 6px;
    font-size: 12px;
    color: #64748b;
}

.canvas-desc {
    margin-top: 6px;
    font-size: 12px;
    color: #64748b;
    display: -webkit-box;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.canvas-btn {
    border-radius: 10px;
    border: 1px solid #e2e8f0;
    padding: 10px 12px;
    font-size: 13px;
    font-weight: 600;
    color: #0f172a;
    transition: background 0.2s ease;
}

.canvas-btn:hover {
    background: #f8fafc;
}
</style>
