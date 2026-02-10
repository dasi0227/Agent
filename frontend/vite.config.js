import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vite.dev/config/
export default defineConfig({
    // Serve the frontend under /agent/ on nginx.
    base: '/agent/',
    plugins: [vue()]
});
