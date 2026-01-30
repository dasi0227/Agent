<script setup>
import { RouterView } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
</script>

<template>
    <div
        class="grid h-screen grid-cols-[280px_1fr] bg-[var(--bg-page)] text-[var(--text-primary)] max-[960px]:grid-cols-[240px_1fr] max-[720px]:grid-cols-[1fr]"
    >
        <Sidebar />
        <RouterView />
    </div>
</template>
