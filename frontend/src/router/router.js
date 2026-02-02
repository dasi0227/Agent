import { createRouter, createWebHistory } from 'vue-router';
import Chat from '../components/Chat.vue';
import Work from '../components/Work.vue';
import Auth from '../components/Auth.vue';
import Admin from '../components/Admin.vue';
import NotFound from '../components/NotFound.vue';
import { getStoredAuth } from './pinia';

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
    },
    {
        path: '/login',
        name: 'login',
        component: Auth,
        meta: {
            hideSidebar: true
        }
    },
    {
        path: '/register',
        name: 'register',
        component: Auth,
        meta: {
            hideSidebar: true
        }
    },
    {
        path: '/admin',
        name: 'admin',
        component: Admin,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'not-found',
        component: NotFound,
        meta: {
            hideSidebar: true
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const auth = getStoredAuth();
    if (!auth.token && to.path !== '/login' && to.path !== '/register') {
        next({ path: '/login', replace: true });
        return;
    }
    if (to.meta?.requiresAuth && !auth.token) {
        next({ path: '/login', replace: true });
        return;
    }
    if (to.meta?.requiresAdmin && auth?.user?.role !== 'admin') {
        next({ path: '/login', replace: true });
        return;
    }
    if (to.path === '/login' && auth.token) {
        const target = auth?.user?.role === 'admin' ? '/admin' : '/chat';
        next({ path: target, replace: true });
        return;
    }
    next();
});

export default router;
