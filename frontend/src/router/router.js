import { createRouter, createWebHistory } from 'vue-router';
import Chat from '../components/Chat.vue';
import Work from '../components/Work.vue';
import Auth from '../components/Auth.vue';
import AuthAdmin from '../components/AuthAdmin.vue';
import AdminTable from '../components/AdminTable.vue';
import AdminConfig from '../components/AdminConfig.vue';
import AdminFlow from '../components/AdminFlow.vue';
import AdminCanvas from '../components/AdminCanvas.vue';
import AdminSession from '../components/AdminSession.vue';
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
        path: '/admin/login',
        name: 'admin-login',
        component: AuthAdmin,
        meta: {
            hideSidebar: true
        }
    },
    {
        path: '/admin',
        redirect: '/admin/flow'
    },
    {
        path: '/admin/config',
        name: 'admin-config',
        component: AdminConfig,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/flow',
        name: 'admin-flow',
        component: AdminFlow,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/canvas',
        name: 'admin-canvas',
        component: AdminCanvas,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/session',
        name: 'admin-session',
        component: AdminSession,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/:module',
        name: 'admin',
        component: AdminTable,
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
    history: createWebHistory('/agent/'),
    routes
});

router.beforeEach((to, from, next) => {
    const auth = getStoredAuth();
    const isAdminRoute = to.path.startsWith('/admin');
    document.title = isAdminRoute ? 'Dasi AI 后台管理' : 'Dasi AI';
    if (!auth.token && to.meta?.requiresAdmin) {
        next({ path: '/admin/login', replace: true });
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
    next();
});

export default router;
