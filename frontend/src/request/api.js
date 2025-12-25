import http, { streamFetch } from './request';

const COMPLETE_PATH = '/api/v1/ollama/complete';
const STREAM_PATH = '/api/v1/ollama/stream';
const RAG_TAGS_PATH = '/api/v1/rag/query/tags';
const RAG_UPLOAD_PATH = '/api/v1/rag/upload';

export const fetchComplete = async ({ model, message, signal }) => {
    return http.get(COMPLETE_PATH, {
        params: {
            model,
            message
        },
        signal
    });
};

export const fetchStream = async ({ model, message, onData, onError, onDone, signal }) => {
    const url = `${http.defaults.baseURL}${STREAM_PATH}?model=${encodeURIComponent(model)}&message=${encodeURIComponent(message)}`;
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

export const uploadRagFile = async ({ ragTag, file }) => {
    const formData = new FormData();
    formData.append('ragTag', ragTag);
    formData.append('fileList', file);
    return http.post(RAG_UPLOAD_PATH, formData);
};
