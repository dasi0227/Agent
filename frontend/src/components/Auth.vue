<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { login, register } from '../request/api';
import { useAuthStore, useSettingsStore } from '../router/pinia';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

const mode = ref(route.path.includes('register') ? 'register' : 'login');
const loading = ref(false);
const error = ref('');
const target = ref('chat');

const form = reactive({
    username: '',
    password: ''
});

const syncModeFromRoute = () => {
    mode.value = route.path.includes('register') ? 'register' : 'login';
    target.value = 'chat';
};

const switchMode = (next) => {
    if (mode.value === next) return;
    mode.value = next;
    error.value = '';
    const path = next === 'login' ? '/login' : '/register';
    router.replace({ path, query: route.query });
};

syncModeFromRoute();

const submit = async () => {
    if (!form.username || !form.password) {
        error.value = '请填写用户名和密码';
        return;
    }
    loading.value = true;
    error.value = '';
    try {
        const api = mode.value === 'login' ? login : register;
        const resp = await api({ username: form.username.trim(), password: form.password });
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
        authStore.setAuth({ token, user });
        settingsStore.updateSettings({ token });
        router.push('/chat');
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
                <div class="text-[22px] font-semibold text-[#0f172a]">{{ mode === 'login' ? '登录' : '注册' }}</div>
                <div class="flex overflow-hidden rounded-[12px] border border-[rgba(15,23,42,0.08)]">
                    <button
                        class="px-[14px] py-[8px] text-[14px] font-semibold transition"
                        :class="mode === 'login' ? 'bg-[#1d4ed8] text-white' : 'bg-white text-[#0f172a]'"
                        type="button"
                        @click="switchMode('login')"
                    >
                        登录
                    </button>
                    <button
                        class="px-[14px] py-[8px] text-[14px] font-semibold transition"
                        :class="mode === 'register' ? 'bg-[#1d4ed8] text-white' : 'bg-white text-[#0f172a]'"
                        type="button"
                        @click="switchMode('register')"
                    >
                        注册
                    </button>
                </div>
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
                    {{ loading ? '提交中...' : mode === 'login' ? '登录' : '注册并登录' }}
                </button>
                <div class="text-center text-[12px] text-[#64748b]">
                    {{ mode === 'login' ? '没有账号？点击上方切换注册' : '已有账号？点击上方切换登录' }}
                </div>
            </div>
        </div>
    </div>
</template>
