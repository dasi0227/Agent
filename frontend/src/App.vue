<script setup>
import { computed, watchEffect } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import { useSettingsStore } from './router/pinia';

const route = useRoute();
const hideSidebar = computed(() => route.meta?.hideSidebar);

const settingsStore = useSettingsStore();

watchEffect(() => {
    const theme = settingsStore.theme === 'dark' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.style.colorScheme = theme;
});
</script>

<template>
    <div
        :class="[
            'grid h-screen bg-[var(--bg-page)] text-[var(--text-primary)]',
            hideSidebar
                ? 'grid-cols-[1fr]'
                : 'grid-cols-[280px_1fr] max-[960px]:grid-cols-[240px_1fr] max-[720px]:grid-cols-[1fr]'
        ]"
    >
        <Sidebar v-if="!hideSidebar" />
        <RouterView />
    </div>
</template>
