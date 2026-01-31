<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '../router/pinia';

const router = useRouter();
const authStore = useAuthStore();

const goHome = () => router.push('/chat');
const goBack = () => router.back();
const goAdmin = () => router.push('/admin');
</script>

<template>
    <div class="flex h-screen flex-col items-center justify-center bg-[var(--bg-page)] text-[#0f172a]">
        <div class="w-[520px] max-w-[90vw] rounded-[22px] border-2 border-[#e2e8f0] bg-white px-[60px] py-[50px] shadow-[0_30px_80px_rgba(15,23,42,0.18)]">
            <div class="text-center text-[64px] font-extrabold text-[#0f172a] tracking-[4px]">404</div>
            <div class="mt-[12px] text-center text-[18px] text-[#475569]">页面不存在，您可以返回继续浏览</div>
            <div class="mt-[28px] flex items-center justify-center gap-[14px]">
                <button
                    class="rounded-[12px] border border-[#e2e8f0] px-[18px] py-[12px] text-[15px] font-semibold text-[#0f172a] transition hover:bg-[#f8fafc]"
                    type="button"
                    @click="goBack"
                >
                    返回上一页
                </button>
                <button
                    class="rounded-[12px] bg-[#1d4ed8] px-[18px] py-[12px] text-[15px] font-semibold text-white shadow-[0_8px_20px_rgba(37,99,235,0.3)] transition hover:bg-[#1e40af]"
                    type="button"
                    @click="goHome"
                >
                    返回首页
                </button>
                <button
                    v-if="authStore.isAdmin"
                    class="rounded-[12px] border border-[#1d4ed8] px-[18px] py-[12px] text-[15px] font-semibold text-[#1d4ed8] transition hover:bg-[#e0f2fe]"
                    type="button"
                    @click="goAdmin"
                >
                    前往后台
                </button>
            </div>
        </div>
    </div>
</template>
