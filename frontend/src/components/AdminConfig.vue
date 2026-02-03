<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import SidebarAdmin from './SidebarAdmin.vue';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { configList, configInsert, configUpdate, configDelete, configToggle, listConfigType } from '../request/api';
import { normalizeError } from '../request/request';
import { useAuthStore } from '../router/pinia';

const authStore = useAuthStore();
const router = useRouter();

const menuGroups = adminMenuGroups;
const currentKey = ref('config');

const query = reactive({
    idKeyword: '',
    valueKeyword: '',
    configType: ''
});

const options = reactive({
    configTypes: []
});

const cards = ref([]);
const loading = ref(false);
const modalVisible = ref(false);
const modalError = ref('');
const editingId = ref(null);
const currentForm = reactive({
    id: null,
    clientId: '',
    configType: '',
    configValue: '',
    configParam: '',
    configStatus: 1
});

const errorDialog = reactive({
    visible: false,
    message: ''
});

const showErrorDialog = (msg) => {
    errorDialog.visible = true;
    errorDialog.message = msg || '操作失败';
};

const unwrapResult = (resp, defaultMsg = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || defaultMsg);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const loadConfigTypes = async () => {
    try {
        const res = await listConfigType();
        const payload = res?.data ?? res?.result ?? res;
        options.configTypes = payload || [];
    } catch {
        options.configTypes = [];
    }
};

const fetchList = async () => {
    loading.value = true;
    try {
        const res = await configList({ ...query });
        const payload = unwrapResult(res, '查询失败') || {};
        cards.value = Object.entries(payload).map(([clientId, list]) => ({
            clientId,
            list: Array.isArray(list) ? list : []
        }));
    } catch (err) {
        showErrorDialog(normalizeError(err).message || '查询失败');
        cards.value = [];
    } finally {
        loading.value = false;
    }
};

const openCreate = (clientId = '') => {
    editingId.value = null;
    modalError.value = '';
    Object.assign(currentForm, {
        id: null,
        clientId: clientId || '',
        configType: '',
        configValue: '',
        configParam: '',
        configStatus: 1
    });
    modalVisible.value = true;
};

const openEdit = (row) => {
    editingId.value = row.id;
    modalError.value = '';
    Object.assign(currentForm, row);
    modalVisible.value = true;
};

const saveForm = async () => {
    const requiredFields = [
        ['clientId', 'Client ID'],
        ['configType', '配置类型'],
        ['configValue', '配置值']
    ];
    for (const [key, label] of requiredFields) {
        if (!currentForm[key]) {
            modalError.value = `${label} 不能为空`;
            return;
        }
    }
    try {
        if (editingId.value) {
            const res = await configUpdate(currentForm);
            unwrapResult(res, '更新失败');
        } else {
            const res = await configInsert(currentForm);
            unwrapResult(res, '创建失败');
        }
        modalVisible.value = false;
        await fetchList();
    } catch (err) {
        modalError.value = normalizeError(err).message || '保存失败';
    }
};

const handleDelete = async (row) => {
    if (!window.confirm('确认删除该配置？')) return;
    try {
        const res = await configDelete(row.id);
        unwrapResult(res, '删除失败');
        await fetchList();
    } catch (err) {
        showErrorDialog(normalizeError(err).message || '删除失败');
    }
};

const switchStatus = async (row, val) => {
    const old = row.configStatus;
    row.configStatus = val;
    try {
        const res = await configToggle(row.id, val);
        unwrapResult(res, '更新状态失败');
    } catch (err) {
        row.configStatus = old;
        showErrorDialog(normalizeError(err).message || '更新状态失败');
    }
};

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) {
        router.push(target.path);
    }
};

onMounted(async () => {
    await loadConfigTypes();
    await fetchList();
});
</script>

<template>
    <div class="admin-font flex h-screen bg-[#f8fafc]">
        <SidebarAdmin :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">CONFIG 管理</div>
            </header>

            <div class="flex-1 overflow-auto p-5">
                <div class="mb-4 flex flex-wrap items-center gap-3 rounded-[12px] border border-[#e2e8f0] bg-white px-4 py-3">
                    <input
                        v-model="query.idKeyword"
                        class="w-[180px] rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                        placeholder="ID 关键字"
                    />
                    <input
                        v-model="query.valueKeyword"
                        class="w-[180px] rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                        placeholder="值关键字"
                    />
                    <select
                        v-model="query.configType"
                        class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                    >
                        <option value="">选择类型</option>
                        <option v-for="opt in options.configTypes" :key="opt" :value="opt">{{ opt }}</option>
                    </select>
                    <button
                        class="rounded-[10px] bg-[#1d4ed8] px-4 py-2 text-[13px] font-semibold text-white"
                        type="button"
                        @click="fetchList"
                    >
                        查询
                    </button>
                    <button
                        class="rounded-[10px] bg-[#0f172a] px-4 py-2 text-[13px] font-semibold text-white shadow"
                        type="button"
                        @click="openCreate('')"
                    >
                        新增
                    </button>
                </div>

                <div class="rounded-[12px] border border-[#e2e8f0] bg-white p-4 shadow-sm">
                    <div v-if="loading" class="py-10 text-center text-[13px] text-[#94a3b8]">加载中...</div>
                    <div v-else-if="cards.length === 0" class="py-10 text-center text-[13px] text-[#94a3b8]">暂无数据</div>
                    <div v-else class="grid gap-4">
                        <div
                            v-for="card in cards"
                            :key="card.clientId"
                            class="config-card rounded-[10px] border border-[#e2e8f0] bg-[#f8fafc] shadow-inner"
                        >
                            <div class="config-card-header flex items-center justify-between border-b border-[#e2e8f0] px-4 py-3">
                                <div class="text-[20px] font-semibold text-[#0f172a]">{{ card.clientId }}</div>
                                <button
                                    class="rounded-[10px] bg-[#0f172a] px-3 py-2 text-[12px] font-semibold text-white shadow"
                                    type="button"
                                    @click="openCreate(card.clientId)"
                                >
                                    新增
                                </button>
                            </div>
                            <div class="overflow-auto">
                                <table class="w-full border-collapse text-[13px] table-fixed">
                                    <thead class="config-table-head bg-[#eef2ff] text-[#475569]">
                                        <tr>
                                            <th class="px-3 py-2 text-left font-semibold" style="width: 25%">配置类型</th>
                                            <th class="px-3 py-2 text-left font-semibold" style="width: 35%">配置ID</th>
                                            <th class="px-3 py-2 text-left font-semibold" style="width: 20%">参数</th>
                                            <th class="px-3 py-2 text-left font-semibold" style="width: 20%">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-if="card.list.length === 0">
                                            <td colspan="4" class="px-3 py-3 text-center text-[#94a3b8]">暂无配置</td>
                                        </tr>
                                        <tr
                                            v-for="row in card.list"
                                            :key="row.id"
                                            class="config-table-row border-t border-[#e2e8f0] hover:bg-[#f1f5f9]"
                                        >
                                            <td class="px-3 py-2 text-[#0f172a] truncate" :title="row.configType">{{ row.configType }}</td>
                                            <td class="px-3 py-2 text-[#0f172a] truncate" :title="row.configValue">{{ row.configValue }}</td>
                                            <td class="px-3 py-2 text-[#0f172a] truncate" :title="row.configParam">{{ row.configParam || '-' }}</td>
                                            <td class="px-3 py-2">
                                                <div class="admin-actions flex flex-wrap items-center gap-2">
                                                    <button
                                                        class="relative h-[22px] w-[50px] rounded-full text-[10px] font-semibold uppercase tracking-[0.5px] transition"
                                                        :class="row.configStatus === 1 ? 'bg-[#1d4ed8] text-white' : 'bg-[#cbd5e1] text-[#0f172a]'"
                                                        type="button"
                                                        @click="switchStatus(row, row.configStatus === 1 ? 0 : 1)"
                                                    >
                                                        <span
                                                            class="absolute left-[3px] top-[3px] h-[16px] w-[16px] rounded-full bg-white transition"
                                                            :class="row.configStatus === 1 ? 'translate-x-[28px]' : ''"
                                                        />
                                                        <span
                                                            class="absolute inset-0 flex items-center px-[6px] transition"
                                                            :class="row.configStatus === 1 ? 'justify-start' : 'justify-end'"
                                                        >
                                                            <span>{{ row.configStatus === 1 ? 'on' : 'off' }}</span>
                                                        </span>
                                                    </button>
                                                    <button
                                                        class="rounded-[8px] border border-[#22c55e] px-3 py-1 text-[12px] font-semibold text-[#15803d] transition hover:bg-[#dcfce7]"
                                                        type="button"
                                                        @click="openEdit(row)"
                                                    >
                                                        编辑
                                                    </button>
                                                    <button
                                                        class="rounded-[8px] border border-[#fecdd3] px-3 py-1 text-[12px] font-semibold text-[#dc2626]"
                                                        type="button"
                                                        @click="handleDelete(row)"
                                                    >
                                                        删除
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="modalVisible"
            class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.35)] px-4"
        >
            <div class="w-full max-w-[620px] rounded-[14px] bg-white p-6 shadow-lg">
                <div class="mb-4 flex items-center justify-between">
                    <div class="text-[16px] font-semibold text-[#0f172a]">
                        {{ editingId ? '编辑配置' : '新增配置' }}
                    </div>
                    <button class="text-[14px] text-[#94a3b8]" type="button" @click="modalVisible = false">✕</button>
                </div>
                <div class="flex flex-col gap-3">
                    <div class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">Client ID *</label>
                        <input
                            v-model="currentForm.clientId"
                            class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                            placeholder="输入 Client ID"
                        />
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">配置类型 *</label>
                        <select
                            v-model="currentForm.configType"
                            class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                        >
                            <option value="">请选择</option>
                            <option v-for="opt in options.configTypes" :key="opt" :value="opt">{{ opt }}</option>
                        </select>
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">配置值 *</label>
                        <textarea
                            v-model="currentForm.configValue"
                            rows="2"
                            class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                        />
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">配置参数</label>
                        <textarea
                            v-model="currentForm.configParam"
                            rows="2"
                            class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                        />
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-[13px] font-semibold text-[#0f172a]">状态</label>
                        <button
                            class="relative h-[24px] w-[54px] rounded-full text-[10px] font-semibold uppercase tracking-[0.5px] transition"
                            :class="currentForm.configStatus === 1 ? 'bg-[#1d4ed8] text-white' : 'bg-[#cbd5e1] text-[#0f172a]'"
                            type="button"
                            @click="currentForm.configStatus = currentForm.configStatus === 1 ? 0 : 1"
                        >
                            <span
                                class="absolute left-[3px] top-[3px] h-[18px] w-[18px] rounded-full bg-white transition"
                                :class="currentForm.configStatus === 1 ? 'translate-x-[28px]' : ''"
                            />
                            <span
                                class="absolute inset-0 flex items-center px-[6px] transition"
                                :class="currentForm.configStatus === 1 ? 'justify-start' : 'justify-end'"
                            >
                                <span>{{ currentForm.configStatus === 1 ? 'on' : 'off' }}</span>
                            </span>
                        </button>
                    </div>
                    <div v-if="modalError" class="rounded-[10px] bg-[#fef2f2] px-3 py-2 text-[12px] text-[#dc2626]">
                        {{ modalError }}
                    </div>
                </div>
                <div class="mt-5 flex justify-end gap-3">
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-4 py-2 text-[13px] font-semibold text-[#0f172a]"
                        type="button"
                        @click="modalVisible = false"
                    >
                        取消
                    </button>
                    <button
                        class="rounded-[10px] bg-[#1d4ed8] px-4 py-2 text-[13px] font-semibold text-white"
                        type="button"
                        @click="saveForm"
                    >
                        保存
                    </button>
                </div>
            </div>
        </div>

        <div
            v-if="errorDialog.visible"
            class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.45)] px-4"
        >
            <div class="absolute inset-0 bg-[rgba(220,38,38,0.08)]"></div>
            <div class="relative w-full max-w-[420px] rounded-[14px] bg-white p-6 shadow-lg shadow-[0_20px_60px_rgba(220,38,38,0.25)] border border-[#fecdd3]">
                <div class="mb-3 text-[16px] font-semibold text-[#0f172a]">操作失败</div>
                <div class="text-[13px] text-[#475569]">{{ errorDialog.message }}</div>
                <div class="mt-5 flex justify-end gap-2">
                    <button
                        class="rounded-[10px] border border-[#e2e8f0] px-4 py-2 text-[13px] font-semibold text-[#0f172a]"
                        type="button"
                        @click="errorDialog.visible = false"
                    >
                        知道了
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-font {
    font-size: 15px;
}
.admin-font .text-\[12px\] {
    font-size: 13px !important;
}
.admin-font .text-\[13px\] {
    font-size: 14px !important;
}
</style>
