<script setup>
import { computed } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';

const route = useRoute();
const hideSidebar = computed(() => route.meta?.hideSidebar);
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
