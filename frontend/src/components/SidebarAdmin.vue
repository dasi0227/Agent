<script setup>
import { computed, ref, watch } from 'vue';
import { useAuthStore, useSettingsStore } from '../router/pinia';

const props = defineProps({
    groups: { type: Array, required: true },
    current: { type: String, required: true }
});

const emit = defineEmits(['select']);

const openGroups = ref(new Set(props.groups.map((g) => g.name)));
const authStore = useAuthStore();
const settingsStore = useSettingsStore();
const currentUser = computed(() => authStore.user || { username: '访客' });
const avatarChar = computed(() => (currentUser.value.username || '?').slice(0, 1).toUpperCase());
const isDarkTheme = computed(() => settingsStore.theme === 'dark');

watch(
    () => props.groups,
    (val) => {
        if (val && val.length) {
            openGroups.value = new Set(val.map((g) => g.name));
        }
    }
);

const toggle = (name) => {
    const next = new Set(openGroups.value);
    if (next.has(name)) {
        next.delete(name);
    } else {
        next.add(name);
    }
    openGroups.value = next;
};

const handleSelect = (key) => emit('select', key);
const handleLogout = () => authStore.logout('/admin/login');
const toggleTheme = () => settingsStore.updateSettings({ theme: isDarkTheme.value ? 'light' : 'dark' });
</script>

<template>
    <aside class="admin-font flex h-full w-[240px] shrink-0 flex-col border-r border-[#e2e8f0] bg-[#f4f6fb] shadow-sm">
        <div class="flex items-center justify-between px-4 py-4 text-[24px] font-semibold text-[#0f172a]">
            <span>管理菜单</span>
            <button
                class="grid h-[30px] w-[30px] place-items-center rounded-[10px] border border-[#e2e8f0] bg-white text-[#64748b] transition hover:border-[#c7d2fe] hover:text-[#1d4ed8]"
                type="button"
                :title="isDarkTheme ? '切换到白天' : '切换到黑天'"
                @click="toggleTheme"
            >
                <svg v-if="isDarkTheme" viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                    <path
                        d="M12 3.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5A.75.75 0 0112 3.75zm6.22 2.53a.75.75 0 011.06 1.06l-1.06 1.06a.75.75 0 11-1.06-1.06l1.06-1.06zM20.25 11.25a.75.75 0 010 1.5h-1.5a.75.75 0 010-1.5h1.5zm-2.47 6.72a.75.75 0 011.06-1.06l1.06 1.06a.75.75 0 11-1.06 1.06l-1.06-1.06zM12 18.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zm-6.22-.78a.75.75 0 011.06 0l1.06 1.06a.75.75 0 11-1.06 1.06l-1.06-1.06a.75.75 0 010-1.06zM3.75 12a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5A.75.75 0 013.75 12zm2.47-6.72a.75.75 0 011.06 0l1.06 1.06a.75.75 0 11-1.06 1.06L6.22 6.34a.75.75 0 010-1.06zM12 7.5a4.5 4.5 0 100 9 4.5 4.5 0 000-9z"
                    />
                </svg>
                <svg v-else viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                    <path
                        d="M21.752 15.002A9.718 9.718 0 0112 21.75 9.75 9.75 0 0112 2.25c.33 0 .658.016.983.048a.75.75 0 01.34 1.38 7.5 7.5 0 009.098 11.072.75.75 0 011.33.252z"
                    />
                </svg>
            </button>
        </div>
        <div class="flex-1 overflow-auto">
            <div v-for="group in groups" :key="group.name" class="border-t border-[#e2e8f0]">
                <button
                    class="flex w-full items-center justify-between px-4 py-3 text-left text-[14px] font-semibold text-[#0f172a]"
                    type="button"
                    @click="toggle(group.name)"
                >
                    <span>{{ group.label }}</span>
                    <span
                        class="caret transition-transform duration-150"
                        :class="openGroups.has(group.name) ? 'caret-open' : 'caret-closed'"
                    />
                </button>
                <transition name="fade">
                    <div v-show="openGroups.has(group.name)" class="pb-2">
                        <button
                            v-for="item in group.items"
                            :key="item.key"
                            class="mx-3 mb-2 flex w-[calc(100%-24px)] items-center gap-2 rounded-[10px] px-3 py-2 text-left text-[13px] transition"
                            :class="item.key === current ? 'bg-[#e0e7ff] text-[#1d4ed8]' : 'text-[#0f172a] hover:bg-white/70'"
                            type="button"
                            @click="handleSelect(item.key)"
                        >
                            <span class="h-[6px] w-[6px] rounded-full" :class="item.key === current ? 'bg-[#1d4ed8]' : 'bg-[#cbd5e1]'" />
                            <span>{{ item.label }}</span>
                        </button>
                    </div>
                </transition>
            </div>
        </div>
        <div class="p-4">
            <div class="flex items-center justify-between rounded-[14px] border border-[#e2e8f0] bg-[#f8fafc] px-4 py-3 text-[#0f172a]">
                <div class="flex items-center gap-3">
                    <div class="grid h-[40px] w-[40px] place-items-center rounded-[12px] border border-[#dbeafe] bg-[#e8f1ff] text-[14px] font-bold text-[#1d4ed8]">
                        {{ avatarChar }}
                    </div>
                    <div class="font-semibold">{{ currentUser.username || '访客' }}</div>
                </div>
                <div class="flex items-center gap-2">
                    <button class="admin-logout rounded-[8px] px-2 py-1 text-[12px] text-[#64748b] transition hover:text-[#1d4ed8]" type="button" @click="handleLogout">
                        退出登录
                    </button>
                </div>
            </div>
        </div>
    </aside>
</template>

<style scoped>
.admin-font {
    font-size: 15px;
}
.admin-font .text-\[12px\] {
    font-size: 13px !important;
}
.admin-font .text-\[13px\] {
    font-size: 14px !important;
}
.fade-enter-active,
.fade-leave-active {
    transition: all 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}

.caret {
    display: inline-block;
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 7px solid #94a3b8;
    transform-origin: center;
}

.caret-open {
    transform: rotate(0deg);
}

.caret-closed {
    transform: rotate(-90deg);
}
</style>
