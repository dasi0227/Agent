import { createRouter, createWebHistory } from 'vue-router';
import Chat from '../components/Chat.vue';
import Work from '../components/Work.vue';

const routes = [
    {
        path: '/',
        redirect: '/chat'
    },
    {
        path: '/chat',
        name: 'chat',
        component: Chat
    },
    {
        path: '/agent',
        redirect: '/work'
    },
    {
        path: '/work',
        name: 'work',
        component: Work
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
