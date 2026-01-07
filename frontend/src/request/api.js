import http, { streamFetch } from './request';

const CHAT_COMPLETE_PATH = '/api/v1/chat/complete';
const CHAT_STREAM_PATH = '/api/v1/chat/stream';
const RAG_TAGS_PATH = '/api/v1/query/tags';
const CHAT_MODELS_PATH = '/api/v1/query/models';
const RAG_UPLOAD_PATH = '/api/v1/rag/file';
const RAG_GIT_PATH = '/api/v1/rag/git';

export const fetchComplete = async ({ model, message, ragTag, signal }) => {
    return http.get(CHAT_COMPLETE_PATH, {
        params: {
            model,
            message,
            ragTag
        },
        signal
    });
};

export const fetchStream = async ({ model, message, ragTag, onData, onError, onDone, signal }) => {
    const url = `${http.defaults.baseURL}${CHAT_STREAM_PATH}?model=${encodeURIComponent(model)}&message=${encodeURIComponent(message)}&ragTag=${encodeURIComponent(ragTag || '')}`;
    return streamFetch(url, onData, onError, onDone, signal);
};

export const pickContentFromResult = (result) => {
    if (!result) {
        return '';
    }
    return (
        result?.output?.content ||
        result?.output?.text ||
        result?.result?.output?.content ||
        result?.result?.output?.text ||
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
