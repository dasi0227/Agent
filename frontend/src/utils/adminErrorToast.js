import { reactive } from 'vue';

let seed = 0;
const DEFAULT_DURATION = 3000;

export const adminErrorToasts = reactive([]);

const removeToastById = (id) => {
    const idx = adminErrorToasts.findIndex((item) => item.id === id);
    if (idx >= 0) {
        adminErrorToasts.splice(idx, 1);
    }
};

export const dismissAdminErrorToast = (id) => {
    if (!id) return;
    removeToastById(id);
};

export const pushAdminErrorToast = (payload, options = {}) => {
    const source = typeof payload === 'string' ? { message: payload } : payload || {};
    const text = String(source.message || '').trim();
    if (!text) return '';
    const id = `admin_error_${Date.now()}_${seed++}`;
    const duration = Number(source.duration) > 0
        ? Number(source.duration)
        : Number(options.duration) > 0
            ? Number(options.duration)
            : DEFAULT_DURATION;

    adminErrorToasts.push({
        id,
        message: text,
        operation: String(source.operation || '').trim(),
        requestPath: String(source.requestPath || '').trim(),
        duration
    });
    return id;
};
