const THINK_START = '<think>';
const THINK_END = '</think>';

export const parseThinkText = (text) => {
    if (!text) {
        return {
            think: '',
            answer: ''
        };
    }
    const match = text.match(/<think>([\s\S]*?)<\/think>/);
    if (!match) {
        return {
            think: '',
            answer: text
        };
    }
    const think = match[1] || '';
    const answer = text.replace(match[0], '');
    return {
        think: think.trim(),
        answer: answer.trim()
    };
};

export const createStreamAccumulator = () => ({
    inThink: false,
    think: '',
    answer: ''
});

export const applyStreamToken = (accumulator, token) => {
    if (!token) {
        return accumulator;
    }
    let remaining = token;
    while (remaining.length > 0) {
        if (accumulator.inThink) {
            const endIndex = remaining.indexOf(THINK_END);
            if (endIndex === -1) {
                accumulator.think += remaining;
                remaining = '';
            } else {
                accumulator.think += remaining.slice(0, endIndex);
                accumulator.inThink = false;
                remaining = remaining.slice(endIndex + THINK_END.length);
            }
        } else {
            const startIndex = remaining.indexOf(THINK_START);
            if (startIndex === -1) {
                accumulator.answer += remaining;
                remaining = '';
            } else {
                accumulator.answer += remaining.slice(0, startIndex);
                accumulator.inThink = true;
                remaining = remaining.slice(startIndex + THINK_START.length);
            }
        }
    }
    return accumulator;
};
