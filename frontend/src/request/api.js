import http, { streamFetch } from './request';

const CHAT_COMPLETE_PATH = '/api/v1/chat/complete';
const CHAT_STREAM_PATH = '/api/v1/chat/stream';
const CHAT_MODELS_PATH = '/api/v1/chat/chat-client-list';
const RAG_TAGS_PATH = '/api/v1/query/tags';
const RAG_UPLOAD_PATH = '/api/v1/rag/file';
const RAG_GIT_PATH = '/api/v1/rag/git';

export const fetchComplete = async ({ clientId, userMessage, signal }) => {
    return http.post(
        CHAT_COMPLETE_PATH,
        {
            clientId,
            userMessage
        },
        { signal }
    );
};

export const fetchStream = async ({ clientId, userMessage, onData, onError, onDone, signal }) => {
    const url = `${http.defaults.baseURL}${CHAT_STREAM_PATH}`;
    return streamFetch(
        url,
        {
            clientId,
            userMessage
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
    return http.get(CHAT_MODELS_PATH);
};

export const uploadRagFile = async ({ ragTag, file }) => {
    const formData = new FormData();
    formData.append('ragTag', ragTag);
    formData.append('fileList', file);
    return http.post(RAG_UPLOAD_PATH, formData);
};

export const uploadRagGit = async ({ repo, username, password }) => {
    const formData = new FormData();
    formData.append('repo', repo);
    formData.append('username', username);
    formData.append('password', password);
    return http.post(RAG_GIT_PATH, formData);
};
