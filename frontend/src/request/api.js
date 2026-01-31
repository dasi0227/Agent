import http, { streamFetch } from './request';


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
const RAG_TAGS_PATH = `${QUERY_BASE_PATH}/rag-tag-list`;

const AUTH_BASE_PATH = '/api/v1';
const LOGIN_PATH = `${AUTH_BASE_PATH}/login`;
const REGISTER_PATH = `${AUTH_BASE_PATH}/register`;
const ME_PATH = `${AUTH_BASE_PATH}/me`;

const ADMIN_BASE_PATH = '/api/v1/admin';
const ADMIN_PATHS = {
    api: 'apis',
    model: 'models',
    mcp: 'mcps',
    advisor: 'advisors',
    prompt: 'prompts',
    client: 'clients',
    flow: 'flows',
    agent: 'agents',
    user: 'users'
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

export const queryRagTags = async () => {
    return http.get(RAG_TAGS_PATH);
};

export const queryChatModels = async () => {
    return http.get(CHAT_CLIENTS_PATH);
};

export const queryChatMcps = async () => {
    return http.get(CHAT_MCP_PATH);
};

export const queryAgentList = async () => {
    return http.get(AGENT_LIST_PATH);
};

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
export const login = async ({ username, password }) => http.post(LOGIN_PATH, { username, password });
export const register = async ({ username, password }) => http.post(REGISTER_PATH, { username, password });
export const fetchProfile = async () => http.get(ME_PATH);
export const updateProfile = async ({ username, oldPassword, newPassword }) =>
    http.put(ME_PATH, { username, oldPassword, newPassword });

const buildAdminPath = (moduleKey) => `${ADMIN_BASE_PATH}/${ADMIN_PATHS[moduleKey]}`;

export const fetchAdminList = async (moduleKey, params = {}) => http.get(buildAdminPath(moduleKey), { params });
export const createAdminItem = async (moduleKey, payload) => http.post(buildAdminPath(moduleKey), payload);
export const updateAdminItem = async (moduleKey, id, payload) => http.put(`${buildAdminPath(moduleKey)}/${id}`, payload);
export const deleteAdminItem = async (moduleKey, id) => http.delete(`${buildAdminPath(moduleKey)}/${id}`);
export const switchAdminStatus = async (moduleKey, id, status) =>
    http.put(`${buildAdminPath(moduleKey)}/${id}/status`, { status });

// Legacy aliases (kept for compatibility)
export const fetchAdminAgents = (params = {}) => fetchAdminList('agent', params);
export const createAdminAgent = (payload) => createAdminItem('agent', payload);
export const updateAdminAgent = (id, payload) => updateAdminItem('agent', id, payload);
export const deleteAdminAgent = (id) => deleteAdminItem('agent', id);

export const fetchAdminUsers = (params = {}) => fetchAdminList('user', params);
export const createAdminUser = (payload) => createAdminItem('user', payload);
export const updateAdminUser = (id, payload) => updateAdminItem('user', id, payload);
export const deleteAdminUser = (id) => deleteAdminItem('user', id);
