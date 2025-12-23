import axios from 'axios';

const BASE_URL = 'http://localhost:8001';
const REQUEST_TIMEOUT = 600000;
const SETTINGS_KEY = 'chat_settings';

const http = axios.create({
    baseURL: BASE_URL,
    timeout: REQUEST_TIMEOUT
});

const getDuration = (config) => {
    if (!config?.metadata?.startTime) {
        return null;
    }
    const duration = Date.now() - config.metadata.startTime;
    return `${duration}ms`;
};

export const normalizeError = (error) => {
    if (error?.name === 'AbortError') {
        return {
            message: '请求已取消',
            status: null,
            isNetworkError: false,
            raw: error
        };
    }
    if (axios.isCancel(error)) {
        return {
            message: '请求已取消',
            status: null,
            isNetworkError: false,
            raw: error
        };
    }
    const isNetworkError = !error.response;
    const status = error.response?.status ?? null;
    const data = error.response?.data ?? {};
    const message = data.message || data.error || error.message || '请求失败，请稍后再试';
    return {
        message,
        status,
        isNetworkError,
        raw: error
    };
};

http.interceptors.request.use(
    (config) => {
        const storedSettings = localStorage.getItem(SETTINGS_KEY);
        const settings = storedSettings ? JSON.parse(storedSettings) : {};
        config.headers = {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            ...(config.headers || {})
        };
        if (settings.token) {
            config.headers.Authorization = `Bearer ${settings.token}`;
        }
        config.metadata = { startTime: Date.now() };
        console.log(
            `[request] ${config.method?.toUpperCase() || 'GET'} ${config.baseURL || ''}${config.url} start`
        );
        return config;
    },
    (error) => Promise.reject(normalizeError(error))
);

http.interceptors.response.use(
    (response) => {
        const duration = getDuration(response.config);
        if (duration) {
            console.log(`[response] ${response.config.url} done in ${duration}`);
        }
        return response.data;
    },
    (error) => {
        const duration = getDuration(error.config);
        if (duration) {
            console.warn(`[response error] ${error.config?.url || ''} failed in ${duration}`);
        }
        return Promise.reject(normalizeError(error));
    }
);

export async function streamFetch(url, onData, onError, onDone, signal) {
    const controller = new AbortController();
    const activeSignal = signal || controller.signal;
    const headers = { Accept: 'text/event-stream' };
    try {
        console.log(`[stream] GET ${url}`);
        const response = await fetch(url, { headers, signal: activeSignal });
        if (!response.ok || !response.body) {
            throw new Error(`流式请求失败: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        while (true) {
            const { done, value } = await reader.read();
            if (done) {
                break;
            }
            buffer += decoder.decode(value, { stream: true });
            let boundary = buffer.indexOf('\n\n');
            while (boundary !== -1) {
                const chunk = buffer.slice(0, boundary).trim();
                buffer = buffer.slice(boundary + 2);
                if (chunk) {
                    processEventChunk(chunk, onData, onError);
                }
                boundary = buffer.indexOf('\n\n');
            }
        }
        if (buffer.trim()) {
            processEventChunk(buffer.trim(), onData, onError);
        }
        onDone && onDone();
    } catch (error) {
        if (error.name === 'AbortError') {
            onError && onError(normalizeError(error));
            return;
        }
        onError && onError(normalizeError(error));
        throw error;
    }
}

const processEventChunk = (chunk, onData, onError) => {
    const lines = chunk.split('\n');
    const dataLine = lines.find((line) => line.startsWith('data:'));
    if (!dataLine) {
        return;
    }
    const payload = dataLine.replace(/^data:\s*/, '');
    if (!payload) {
        return;
    }
    try {
        const json = JSON.parse(payload);
        onData && onData(json);
    } catch (error) {
        onError && onError(normalizeError(error));
    }
};

export default http;
