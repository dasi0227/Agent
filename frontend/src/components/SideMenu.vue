<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
    groups: { type: Array, required: true },
    current: { type: String, required: true }
});

const emit = defineEmits(['select']);

const openGroups = ref(new Set(props.groups.map((g) => g.name)));

watch(
    () => props.groups,
    (val) => {
        if (val && val.length) {
            openGroups.value = new Set(val.map((g) => g.name));
        }
    }
);

const toggle = (name) => {
    const next = new Set(openGroups.value);
    if (next.has(name)) {
        next.delete(name);
    } else {
        next.add(name);
    }
    openGroups.value = next;
};

const handleSelect = (key) => emit('select', key);
</script>

<template>
    <aside class="h-full w-[240px] shrink-0 border-r border-[#e2e8f0] bg-white shadow-sm">
        <div class="px-4 py-4 text-[18px] font-semibold text-[#0f172a]">管理菜单</div>
        <div class="flex flex-col">
            <div v-for="group in groups" :key="group.name" class="border-t border-[#f1f5f9]">
                <button
                    class="flex w-full items-center justify-between px-4 py-3 text-left text-[14px] font-semibold text-[#0f172a]"
                    type="button"
                    @click="toggle(group.name)"
                >
                    <span>{{ group.label }}</span>
                    <span
                        class="caret transition-transform duration-150"
                        :class="openGroups.has(group.name) ? 'caret-open' : 'caret-closed'"
                    />
                </button>
                <transition name="fade">
                    <div v-show="openGroups.has(group.name)" class="pb-2">
                        <button
                            v-for="item in group.items"
                            :key="item.key"
                            class="mx-3 mb-2 flex w-[calc(100%-24px)] items-center gap-2 rounded-[10px] px-3 py-2 text-left text-[13px] transition"
                            :class="item.key === current ? 'bg-[#e0e7ff] text-[#1d4ed8]' : 'text-[#0f172a] hover:bg-[#f8fafc]'"
                            type="button"
                            @click="handleSelect(item.key)"
                        >
                            <span class="h-[6px] w-[6px] rounded-full" :class="item.key === current ? 'bg-[#1d4ed8]' : 'bg-[#cbd5e1]'" />
                            <span>{{ item.label }}</span>
                        </button>
                    </div>
                </transition>
            </div>
        </div>
    </aside>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: all 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}

.caret {
    display: inline-block;
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 7px solid #94a3b8;
    transform-origin: center;
}

.caret-open {
    transform: rotate(0deg);
}

.caret-closed {
    transform: rotate(-90deg);
}
</style>
