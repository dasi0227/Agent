<script setup>
import { adminErrorToasts, dismissAdminErrorToast } from '../utils/adminErrorToast';
</script>

<template>
    <div class="pointer-events-none fixed right-[14px] top-[14px] z-[1200] flex w-[min(380px,calc(100vw-28px))] flex-col gap-[10px]">
        <TransitionGroup name="admin-error-toast">
            <div
                v-for="toast in adminErrorToasts"
                :key="toast.id"
                class="pointer-events-auto relative overflow-hidden rounded-[10px] border border-[rgba(239,68,68,0.25)] bg-white shadow-[0_10px_26px_rgba(15,23,42,0.2)]"
            >
                <button
                    class="absolute right-[8px] top-[8px] flex h-[22px] w-[22px] items-center justify-center rounded-[6px] text-[18px] leading-none text-[#9ca3af] transition hover:bg-[#f3f4f6] hover:text-[#6b7280]"
                    type="button"
                    aria-label="关闭"
                    @click="dismissAdminErrorToast(toast.id)"
                >
                    ×
                </button>
                <div class="flex items-start gap-[10px] px-[12px] pb-[12px] pt-[12px] pr-[36px]">
                    <div class="mt-[1px] grid h-[30px] w-[30px] shrink-0 place-items-center rounded-full bg-[#ef4444] text-[20px] font-bold leading-none text-white">
                        !
                    </div>
                    <div class="min-w-0 flex-1">
                        <div class="text-[15px] font-semibold leading-[1.45] text-[#111827]">
                            错误：{{ toast.message }}
                        </div>
                        <div class="mt-[6px] flex flex-wrap gap-[6px] text-[11px] leading-[1.4] text-[#64748b]">
                            <span v-if="toast.operation" class="rounded-[999px] border border-[#dbe2eb] bg-[#f8fafc] px-[6px] py-[2px]">
                                操作：{{ toast.operation }}
                            </span>
                            <span
                                v-if="toast.requestPath"
                                class="max-w-full rounded-[6px] border border-[#dbe2eb] bg-[#f8fafc] px-[6px] py-[2px] font-mono break-all [overflow-wrap:anywhere]"
                            >
                                {{ toast.requestPath }}
                            </span>
                        </div>
                    </div>
                </div>
                <div
                    class="h-[3px] bg-[#ef4444] animate-admin-error-progress"
                    :style="{ animationDuration: `${toast.duration}ms` }"
                    @animationend="dismissAdminErrorToast(toast.id)"
                ></div>
            </div>
        </TransitionGroup>
    </div>
</template>

<style scoped>
@keyframes admin-error-progress {
    from {
        width: 100%;
    }
    to {
        width: 0%;
    }
}

.animate-admin-error-progress {
    animation-name: admin-error-progress;
    animation-timing-function: linear;
    animation-fill-mode: forwards;
}

.admin-error-toast-enter-active,
.admin-error-toast-leave-active {
    transition: all 0.22s ease;
}

.admin-error-toast-enter-from,
.admin-error-toast-leave-to {
    opacity: 0;
    transform: translateY(-8px) translateX(10px);
}
</style>
