export const adminMenuGroups = [
    {
        name: 'workflow',
        label: '工作流管理',
        items: [
            { key: 'flow', label: 'FLOW', path: '/admin/flow' },
            { key: 'config', label: 'CONFIG', path: '/admin/config' },
            { key: 'task', label: 'TASK', path: '/admin/task' }
        ]
    },
    {
        name: 'model',
        label: '模型管理',
        items: [
            { key: 'agent', label: 'AGENT', path: '/admin/agent' },
            { key: 'client', label: 'CLIENT', path: '/admin/client' }
        ]
    },
    {
        name: 'base',
        label: '服务管理',
        items: [
            { key: 'model', label: 'MODEL', path: '/admin/model' },
            { key: 'api', label: 'API', path: '/admin/api' },
            { key: 'mcp', label: 'MCP', path: '/admin/mcp' },
            { key: 'prompt', label: 'PROMPT', path: '/admin/prompt' },
            { key: 'advisor', label: 'ADVISOR', path: '/admin/advisor' }
        ]
    },
    {
        name: 'user',
        label: '用户管理',
        items: [
            { key: 'user', label: 'USER', path: '/admin/user' },
            { key: 'session', label: 'SESSION', path: '/admin/session' }
        ]
    }
];
