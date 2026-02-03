<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../request/api';
import { useAuthStore, useSettingsStore } from '../router/pinia';

const router = useRouter();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

const loading = ref(false);
const error = ref('');

const form = reactive({
    username: '',
    password: ''
});

const submit = async () => {
    if (!form.username || !form.password) {
        error.value = '请填写用户名和密码';
        return;
    }
    loading.value = true;
    error.value = '';
    try {
        const resp = await login({ username: form.username.trim(), password: form.password });
        const payload = resp?.data || resp?.result || resp;
        const token = payload?.token || payload?.data?.token;
        const user =
            payload?.user ||
            payload?.data?.user ||
            (payload && (payload.username || payload.role || payload.id)
                ? {
                      id: payload.id ?? payload.data?.id,
                      username: payload.username ?? payload.data?.username,
                      role: payload.role ?? payload.data?.role,
                      userStatus: payload.userStatus ?? payload.data?.userStatus
                  }
                : null);
        if (!token || !user) {
            throw new Error('登录信息异常');
        }
        if (user.userStatus === 0) {
            throw new Error('账号已被禁用');
        }
        if (user.role !== 'admin') {
            throw new Error('仅管理员可以进入后台');
        }
        authStore.setAuth({ token, user });
        settingsStore.updateSettings({ token });
        router.push('/admin');
    } catch (e) {
        error.value = e?.message || '操作失败，请稍后重试';
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="flex h-screen items-center justify-center bg-[var(--auth-bg)] p-[16px]">
        <div
            class="w-full max-w-[520px] rounded-[18px] border border-[rgba(15,23,42,0.08)] bg-white/90 p-[24px] shadow-[0_20px_60px_rgba(15,23,42,0.12)] backdrop-blur"
        >
            <div class="mb-[16px] flex items-center justify-between">
                <div class="text-[22px] font-semibold text-[#0f172a]">后台管理</div>
            </div>

            <div class="space-y-[14px]">
                <div>
                    <div class="mb-[6px] text-[13px] text-[#475569]">用户名</div>
                    <input
                        v-model="form.username"
                        class="w-full rounded-[12px] border border-[#e2e8f0] px-[12px] py-[12px] text-[14px] text-[#0f172a] outline-none transition focus:border-[#1d4ed8] focus:ring-2 focus:ring-[#bfdbfe]"
                        placeholder="请输入用户名"
                        @keydown.enter.prevent="submit"
                    />
                </div>
                <div>
                    <div class="mb-[6px] text-[13px] text-[#475569]">密码</div>
                    <input
                        v-model="form.password"
                        type="password"
                        class="w-full rounded-[12px] border border-[#e2e8f0] px-[12px] py-[12px] text-[14px] text-[#0f172a] outline-none transition focus:border-[#1d4ed8] focus:ring-2 focus:ring-[#bfdbfe]"
                        placeholder="请输入密码"
                        @keydown.enter.prevent="submit"
                    />
                </div>
                <div v-if="error" class="text-[13px] text-[#dc2626]">{{ error }}</div>
                <button
                    class="mt-[4px] flex w-full items-center justify-center rounded-[12px] bg-[#1d4ed8] px-[14px] py-[12px] text-[15px] font-semibold text-white shadow-[0_10px_30px_rgba(37,99,235,0.35)] transition hover:bg-[#1e40af] disabled:cursor-not-allowed disabled:bg-[#94a3b8]"
                    type="button"
                    :disabled="loading"
                    @click="submit"
                >
                    {{ loading ? '提交中...' : '登录' }}
                </button>
                <div class="text-center text-[12px] text-[#64748b]">注册管理员请联系开发者</div>
            </div>
        </div>
    </div>
</template>
