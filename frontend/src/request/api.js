import http, { streamFetch } from './request';
import { trimStrings } from '../utils/StringUtil';

const AI_BASE_PATH = '/api/v1/ai';
const CHAT_COMPLETE_PATH = `${AI_BASE_PATH}/chat/complete`;
const CHAT_STREAM_PATH = `${AI_BASE_PATH}/chat/stream`;
const AGENT_EXECUTE_PATH = `${AI_BASE_PATH}/work/execute`;
const CLIENT_ARMORY_PATH = `${AI_BASE_PATH}/armory`;
const RAG_UPLOAD_PATH = `${AI_BASE_PATH}/rag/file`;
const RAG_GIT_PATH = `${AI_BASE_PATH}/rag/git`;

const QUERY_BASE_PATH = '/api/v1/query';
const CHAT_CLIENTS_PATH = `${QUERY_BASE_PATH}/chat-client-list`;
const CHAT_MCP_PATH = `${QUERY_BASE_PATH}/chat-mcp-list`;
const AGENT_LIST_PATH = `${QUERY_BASE_PATH}/agent-list`;
const RAG_TAGS_PATH = `${QUERY_BASE_PATH}/chat-rag-list`;

const AUTH_BASE_PATH = '/api/v1/auth';
const LOGIN_PATH = `${AUTH_BASE_PATH}/login`;
const REGISTER_PATH = `${AUTH_BASE_PATH}/register`;
const PROFILE_PATH = `${AUTH_BASE_PATH}/profile`;
const PASSWORD_PATH = `${AUTH_BASE_PATH}/password`;

const ADMIN_BASE_PATH = '/api/v1/admin';
const ADMIN_STATUS_PARAM = {
    api: 'apiStatus',
    model: 'modelStatus',
    mcp: 'mcpStatus',
    advisor: 'advisorStatus',
    prompt: 'promptStatus',
    client: 'clientStatus',
    agent: 'agentStatus',
    config: 'configStatus'
};

export const fetchComplete = async ({
    clientId,
    userMessage,
    ragTag,
    mcpIdList,
    temperature,
    presencePenalty,
    maxCompletionTokens,
    sessionId,
    signal
}) => {
    return http.post(
        CHAT_COMPLETE_PATH,
        {
            clientId,
            userMessage,
            temperature,
            presencePenalty,
            maxCompletionTokens,
            mcpIdList,
            sessionId,
            ragTag
        },
        { signal }
    );
};

export const fetchStream = async ({
    clientId,
    userMessage,
    ragTag,
    mcpIdList,
    temperature,
    presencePenalty,
    maxCompletionTokens,
    sessionId,
    onData,
    onError,
    onDone,
    signal
}) => {
    const url = `${http.defaults.baseURL}${CHAT_STREAM_PATH}`;
    return streamFetch(
        url,
        {
            clientId,
            userMessage,
            temperature,
            presencePenalty,
            maxCompletionTokens,
            mcpIdList,
            sessionId,
            ragTag
        },
        onData,
        onError,
        onDone,
        signal
    );
};

export const pickContentFromResult = (result) => {
    if (!result) {
        return '';
    }
    if (typeof result === 'string') {
        return result;
    }
    return (
        result?.output?.content ||
        result?.output?.text ||
        result?.data ||
        result?.result?.output?.content ||
        result?.result?.output?.text ||
        result?.result ||
        ''
    );
};

export const queryRagTags = async () => http.get(RAG_TAGS_PATH);

export const queryChatModels = async () => http.get(CHAT_CLIENTS_PATH);

export const queryChatMcps = async () => http.get(CHAT_MCP_PATH);

export const queryAgentList = async () => http.get(AGENT_LIST_PATH);

export const dispatchArmory = async ({ armoryType, armoryId }) => {
    return http.post(
        CLIENT_ARMORY_PATH,
        {
            armoryType,
            armoryId
        }
    );
};

export const executeAgentStream = async ({
    aiAgentId,
    userMessage,
    sessionId,
    maxRound,
    maxRetry,
    onData,
    onError,
    onDone,
    signal
}) => {
    const url = `${http.defaults.baseURL}${AGENT_EXECUTE_PATH}`;
    return streamFetch(
        url,
        {
            aiAgentId,
            userMessage,
            sessionId,
            maxRound,
            maxRetry
        },
        onData,
        onError,
        onDone,
        signal
    );
};

export const uploadRagFile = async ({ ragTag, file }) => {
    const formData = new FormData();
    formData.append('ragTag', ragTag);
    formData.append('fileList', file);
    return http.post(RAG_UPLOAD_PATH, formData);
};

export const uploadRagGit = async ({ repo, username, password }) => {
    return http.post(RAG_GIT_PATH, {
        repo,
        username,
        password
    });
};

// Auth
export const login = async ({ username, password }) =>
    http.post(LOGIN_PATH, trimStrings({ username, password }));
export const register = async ({ username, password }) =>
    http.post(REGISTER_PATH, trimStrings({ username, password }));
export const fetchProfile = async () => http.post(PROFILE_PATH);
export const updatePassword = async ({ id, username, oldPassword, newPassword }) =>
    http.post(PASSWORD_PATH, trimStrings({ id, username, oldPassword, newPassword }));

// -------------------- Admin --------------------
const buildAdminPath = (moduleKey, action) => `${ADMIN_BASE_PATH}/${moduleKey}/${action}`;

export const adminPage = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'page'), trimStrings(payload));

export const adminInsert = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'insert'), trimStrings(payload));

export const adminUpdate = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'update'), trimStrings(payload));

export const adminDelete = async (moduleKey, id) =>
    http.post(buildAdminPath(moduleKey, 'delete'), null, { params: { id } });

export const adminToggle = async (moduleKey, id, status) => {
    const statusKey = ADMIN_STATUS_PARAM[moduleKey];
    return http.post(buildAdminPath(moduleKey, 'toggle'), null, { params: { id, [statusKey]: status } });
};

// config 专用（非分页 Map）
export const configList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/list`, trimStrings(payload));
export const configInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/insert`, trimStrings(payload));
export const configUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/update`, trimStrings(payload));
export const configDelete = async (id) => http.post(`${ADMIN_BASE_PATH}/config/delete`, null, { params: { id } });
export const configToggle = async (id, status) =>
    http.post(`${ADMIN_BASE_PATH}/config/toggle`, null, { params: { id, configStatus: status } });

// ---- admin option lists ----
const ADMIN_LIST_BASE = `${ADMIN_BASE_PATH}/list`;
export const listClientType = async () => http.get(`${ADMIN_LIST_BASE}/clientType`);
export const listAgentType = async () => http.get(`${ADMIN_LIST_BASE}/agentType`);
export const listConfigType = async () => http.get(`${ADMIN_LIST_BASE}/configType`);
export const listUserRole = async () => http.get(`${ADMIN_LIST_BASE}/userRole`);
export const listApiId = async () => http.get(`${ADMIN_LIST_BASE}/apiId`);
export const listModelId = async () => http.get(`${ADMIN_LIST_BASE}/modelId`);
export const listClientRole = async () => http.get(`${ADMIN_LIST_BASE}/clientRole`);
