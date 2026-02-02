<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import SidebarAdmin from './SidebarAdmin.vue';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { useAuthStore } from '../router/pinia';

const authStore = useAuthStore();
const router = useRouter();
const menuGroups = adminMenuGroups;
const currentKey = ref('flow');

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) {
        router.push(target.path);
    }
};
</script>

<template>
    <div class="flex h-screen bg-[#f8fafc]">
        <SidebarAdmin :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">FLOW 管理</div>
                <div class="flex items-center gap-3">
                    <div class="text-right">
                        <div class="text-[14px] font-semibold text-[#0f172a]">{{ authStore.user?.username || '-' }}</div>
                        <div class="text-[12px] text-[#94a3b8]">角色：{{ authStore.user?.role || '-' }}</div>
                    </div>
                    <div class="grid h-[36px] w-[36px] place-items-center rounded-full bg-[#e2e8f0] text-[14px] font-bold text-[#0f172a]">
                        {{ (authStore.user?.username || '?').slice(0, 1).toUpperCase() }}
                    </div>
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f8fafc]"
                        type="button"
                        @click="authStore.logout()"
                    >
                        退出
                    </button>
                </div>
            </header>

            <div class="flex-1 p-8 text-center text-[15px] text-[#475569]">
                <div class="mx-auto max-w-xl rounded-[16px] border border-dashed border-[#cbd5e1] bg-white px-6 py-10 shadow-sm">
                    <div class="mb-2 text-[18px] font-semibold text-[#0f172a]">工作流功能开发中</div>
                    <div class="text-[13px] text-[#64748b]">敬请期待后续版本更新。</div>
                </div>
            </div>
        </div>
    </div>
</template>
