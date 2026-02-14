<script setup>
import { useRouter } from 'vue-router';
import AppFooter from './AppFooter.vue';

const router = useRouter();

const goHome = () => router.push('/chat');
const goBack = () => router.back();
</script>

<template>
    <div class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)] text-[#0f172a]">
        <div class="flex items-center justify-center p-[16px]">
            <div class="w-[680px] max-w-[95vw] rounded-[22px] border-2 border-[#e2e8f0] bg-white px-[60px] py-[60px] shadow-[0_30px_80px_rgba(15,23,42,0.18)]">
                <div class="text-center text-[72px] font-extrabold text-[#0f172a] tracking-[4px]">404</div>
                <div class="mt-[12px] text-center text-[20px] text-[#475569]">页面不存在</div>
                <div class="mt-[32px] flex items-center justify-center gap-[16px]">
                    <button
                        class="rounded-[12px] bg-[#22c55e] px-[18px] py-[12px] text-[15px] font-semibold text-white shadow-[0_8px_20px_rgba(34,197,94,0.25)] transition hover:bg-[#16a34a]"
                        type="button"
                        @click="goBack"
                    >
                        返回上页
                    </button>
                    <button
                        class="rounded-[12px] bg-[#1d4ed8] px-[18px] py-[12px] text-[15px] font-semibold text-white shadow-[0_8px_20px_rgba(37,99,235,0.3)] transition hover:bg-[#1e40af]"
                        type="button"
                        @click="goHome"
                    >
                        前往首页
                    </button>
                </div>
            </div>
        </div>
        <AppFooter inner-class="px-[16px]" />
    </div>
</template>
