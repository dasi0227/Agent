<script setup>
import { computed } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import AgentSidebar from './components/AgentSidebar.vue';

const route = useRoute();
const isAgentRoute = computed(() => route.path.startsWith('/agent'));
</script>

<template>
    <div
        class="grid h-screen grid-cols-[280px_1fr] bg-[var(--bg-page)] text-[var(--text-primary)] max-[960px]:grid-cols-[240px_1fr] max-[720px]:grid-cols-[1fr]"
    >
        <Sidebar v-if="!isAgentRoute" />
        <AgentSidebar v-else />
        <RouterView />
    </div>
</template>
