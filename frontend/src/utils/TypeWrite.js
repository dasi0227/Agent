export const DEFAULT_TYPEWRITER_SEGMENTS = [
    [
        '你好，我是 Dasi。',
        '擅长陪你思考、创作与推演。',
        '随时输入第一条消息开始体验～'
    ],
    [
        '你可以问我：学习路线 / 代码调试 / 论文写作 / 面试准备。',
        '支持完整回答与流式输出，可在右上角自定义偏好。',
        '描述越清晰，答案越精准。'
    ],
    [
        '你知道如何用不同的编程语言打出 "Hello World" 的吗？',
        '告诉我你想要的语言列表，我一次给你整理好。'
    ]
];

export const createTypewriter = ({
    segments = DEFAULT_TYPEWRITER_SEGMENTS,
    charDelay = 60,
    segmentPause = 3000,
    loop = true,
    onUpdate
} = {}) => {
    let typingTimer = null;
    let pauseTimer = null;
    let running = false;
    let segmentIndex = 0;
    let lineIndex = 0;
    let charIndex = 0;
    let displayLines = [];

    const emit = (playing) => {
        onUpdate &&
            onUpdate({
                lines: [...displayLines],
                segmentIndex,
                lineIndex,
                playing
            });
    };

    const clearTimers = () => {
        if (typingTimer) {
            clearTimeout(typingTimer);
            typingTimer = null;
        }
        if (pauseTimer) {
            clearTimeout(pauseTimer);
            pauseTimer = null;
        }
    };

    const stop = () => {
        if (!running) {
            clearTimers();
            return;
        }
        running = false;
        clearTimers();
        emit(false);
    };

    const resetState = () => {
        segmentIndex = 0;
        lineIndex = 0;
        charIndex = 0;
        displayLines = [];
    };

    const prepareSegment = () => {
        const segment = segments[segmentIndex] || [];
        displayLines = segment.map(() => '');
        lineIndex = 0;
        charIndex = 0;
        emit(true);
    };

    const tick = () => {
        if (!running) {
            return;
        }
        const segment = segments[segmentIndex];
        if (!segment || segment.length === 0) {
            stop();
            return;
        }
        const line = segment[lineIndex] || '';
        if (charIndex < line.length) {
            displayLines[lineIndex] = (displayLines[lineIndex] || '') + line[charIndex];
            charIndex += 1;
            emit(true);
            typingTimer = setTimeout(tick, charDelay);
            return;
        }

        lineIndex += 1;
        charIndex = 0;
        if (lineIndex < segment.length) {
            emit(true);
            typingTimer = setTimeout(tick, charDelay);
            return;
        }

        pauseTimer = setTimeout(() => {
            if (!running) {
                return;
            }
            advanceSegment();
        }, segmentPause);
    };

    const advanceSegment = () => {
        segmentIndex += 1;
        if (segmentIndex >= segments.length) {
            if (!loop) {
                stop();
                return;
            }
            segmentIndex = 0;
        }
        prepareSegment();
        typingTimer = setTimeout(tick, charDelay);
    };

    const start = () => {
        stop();
        resetState();
        if (!segments || segments.length === 0) {
            emit(false);
            return;
        }
        running = true;
        prepareSegment();
        typingTimer = setTimeout(tick, charDelay);
    };

    const reset = () => {
        stop();
        resetState();
        emit(false);
    };

    return {
        start,
        stop,
        reset
    };
};
