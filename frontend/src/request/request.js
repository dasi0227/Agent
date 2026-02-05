import axios from 'axios';
import router from '../router/router';
import { useAuthStore } from '../router/pinia';

const BASE_URL = 'http://localhost:8066';
const REQUEST_TIMEOUT = 600000;
const SETTINGS_KEY = 'chat_settings';
const AUTH_KEY = 'auth_info';
const APP_BASE = (import.meta.env.BASE_URL || '/').replace(/\/$/, '');
const LOGIN_PATH = `${APP_BASE}/login`;

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
    const message = data.message || data.error || data.info || error.message || '请求失败，请稍后再试';
    return {
        message,
        status,
        isNetworkError,
        raw: error
    };
};

http.interceptors.request.use(
    (config) => {
        let authStore;
        try {
            authStore = useAuthStore();
        } catch (e) {
            authStore = null;
        }
        const storedSettings = localStorage.getItem(SETTINGS_KEY);
        const settings = storedSettings ? JSON.parse(storedSettings) : {};
        const storedAuth = localStorage.getItem(AUTH_KEY);
        const auth = storedAuth ? JSON.parse(storedAuth) : {};
        const isFormData = config.data instanceof FormData;
        config.headers = {
            ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
            Accept: 'application/json',
            ...(config.headers || {})
        };
        const token = authStore?.token || auth.token || settings.token;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        config.metadata = { startTime: Date.now() };
        return config;
    },
    (error) => Promise.reject(normalizeError(error))
);

http.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        const status = error?.response?.status;
        let authStore;
        try {
            authStore = useAuthStore();
        } catch (e) {
            authStore = null;
        }
        if (status === 401) {
            authStore?.clear();
            try {
                const settingsRaw = localStorage.getItem(SETTINGS_KEY);
                if (settingsRaw) {
                    const parsed = JSON.parse(settingsRaw);
                    delete parsed.token;
                    localStorage.setItem(SETTINGS_KEY, JSON.stringify(parsed));
                }
            } catch (_) {
                // ignore
            }
            if (router.currentRoute.value.path !== '/login') {
                router.replace('/login');
            }
        } else if (status === 403) {
            window.alert('无权限访问该资源');
        }
        return Promise.reject(normalizeError(error));
    }
);

export async function streamFetch(url, body, onData, onError, onDone, signal) {
    const controller = new AbortController();
    const activeSignal = signal || controller.signal;
    const headers = { Accept: 'text/event-stream', 'Content-Type': 'application/json' };
    try {
        const storedAuth = localStorage.getItem(AUTH_KEY);
        const auth = storedAuth ? JSON.parse(storedAuth) : {};
        const storedSettings = localStorage.getItem(SETTINGS_KEY);
        const settings = storedSettings ? JSON.parse(storedSettings) : {};
        const token = auth.token || settings.token;
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
    } catch (_) {
        // ignore
    }
    try {
        console.log(`[stream] POST ${url}`);
        const response = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(body || {}),
            signal: activeSignal
        });
        if (response.status === 401) {
            localStorage.removeItem(AUTH_KEY);
            if (window.location.pathname !== LOGIN_PATH) {
                window.location.href = LOGIN_PATH;
            }
            throw new Error('未登录或登录已过期');
        }
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
                const chunk = buffer.slice(0, boundary);
                buffer = buffer.slice(boundary + 2);
                if (chunk) {
                    processEventChunk(chunk, onData, onError);
                }
                boundary = buffer.indexOf('\n\n');
            }
        }
        if (buffer) {
            processEventChunk(buffer, onData, onError);
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
    const lines = chunk.split('\n').map((line) => line.replace(/\r$/, ''));
    const dataLines = lines.filter((line) => line.startsWith('data:'));
    if (!dataLines.length) {
        return;
    }
    const payload = dataLines.map((line) => line.replace(/^data:\s*/, '')).join('\n');
    if (!payload) {
        return;
    }
    try {
        const json = JSON.parse(payload);
        onData && onData(json);
        return;
    } catch (error) {
        if (onData) {
            onData(payload);
            return;
        }
        onError && onError(normalizeError(error));
    }
};

export default http;
