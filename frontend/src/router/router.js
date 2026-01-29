import { createRouter, createWebHistory } from 'vue-router';
import Chat from '../components/Chat.vue';
import Agent from '../components/Agent.vue';

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
        name: 'agent',
        component: Agent
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
