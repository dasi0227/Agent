<script setup>
import beianIcon from '../assets/beian.png';

defineProps({
    wrapperClass: {
        type: String,
        default: ''
    },
    innerClass: {
        type: String,
        default: ''
    }
});

const currentYear = new Date().getFullYear();
</script>

<template>
    <footer
        :class="[
            'h-[var(--footer-height)] border-t border-[rgba(15,23,42,0.06)] bg-[var(--surface-3)] backdrop-blur-[6px]',
            wrapperClass
        ]"
    >
        <div
            :class="[
                'mx-auto grid h-full w-full grid-cols-3 items-center gap-[12px] text-[12px] text-[var(--text-secondary)]',
                innerClass
            ]"
        >
            <a
                class="inline-flex min-w-0 items-center gap-[6px] truncate text-[inherit] no-underline transition hover:text-[var(--accent-color)]"
                href="https://beian.mps.gov.cn/#/query/webSearch?code=44010602015173"
                rel="noreferrer"
                target="_blank"
                title="粤公网安备44010602015173号"
            >
                <img :src="beianIcon" alt="备案图标" class="h-[14px] w-[14px] shrink-0" />
                <span class="truncate">粤公网安备44010602015173号</span>
            </a>
            <span class="min-w-0 truncate text-center" :title="`Copyright © ${currentYear} Dasi. All Rights Reserved.`">
                Copyright © {{ currentYear }} Dasi. All Rights Reserved.
            </span>
            <span class="min-w-0 truncate text-right" title="本网站内容由 AI 生成，仅供参考，请注意甄别。">
                本网站内容由 AI 生成，仅供参考，请注意甄别。
            </span>
        </div>
    </footer>
</template>
