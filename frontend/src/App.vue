<script setup>
import { RouterView } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
</script>

<template>
    <div class="app-shell">
        <Sidebar />
        <RouterView />
    </div>
</template>

<style scoped>
.app-shell {
    display: grid;
    grid-template-columns: 280px 1fr;
    height: 100vh;
    background: var(--bg-page);
    color: var(--text-primary);
}

@media (max-width: 960px) {
    .app-shell {
        grid-template-columns: 240px 1fr;
    }
}

@media (max-width: 720px) {
    .app-shell {
        grid-template-columns: 1fr;
    }
}
</style>
