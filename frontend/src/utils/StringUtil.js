export const trimStrings = (input) => {
    if (input === null || input === undefined) return input;
    if (typeof input === 'string') return input.trim();
    if (Array.isArray(input)) return input.map((item) => trimStrings(item));
    if (typeof input === 'object') {
        return Object.fromEntries(Object.entries(input).map(([k, v]) => [k, trimStrings(v)]));
    }
    return input;
};

const THINK_START = '<think>';
const THINK_END = '</think>';

export const parseThinkText = (text) => {
    if (!text) {
        return {
            think: '',
            answer: ''
        };
    }
    const answer = text.replace(/<think>[\s\S]*?<\/think>/g, '');
    return {
        think: '',
        answer
    };
};

export const createStreamAccumulator = () => ({
    inThink: false,
    answer: '',
    carry: ''
});

const findPartialSuffix = (text, tag) => {
    const max = Math.min(tag.length - 1, text.length);
    for (let len = max; len > 0; len -= 1) {
        if (tag.startsWith(text.slice(-len))) {
            return len;
        }
    }
    return 0;
};

export const applyStreamToken = (accumulator, token) => {
    if (!token) {
        return accumulator;
    }
    let remaining = `${accumulator.carry || ''}${token}`;
    accumulator.carry = '';
    while (remaining.length > 0) {
        if (accumulator.inThink) {
            const endIndex = remaining.indexOf(THINK_END);
            if (endIndex === -1) {
                const partial = findPartialSuffix(remaining, THINK_END);
                if (partial > 0) {
                    accumulator.carry = remaining.slice(-partial);
                } else {
                    accumulator.carry = '';
                }
                remaining = '';
            } else {
                accumulator.inThink = false;
                remaining = remaining.slice(endIndex + THINK_END.length);
            }
        } else {
            const startIndex = remaining.indexOf(THINK_START);
            if (startIndex === -1) {
                const partial = findPartialSuffix(remaining, THINK_START);
                if (partial > 0) {
                    accumulator.answer += remaining.slice(0, -partial);
                    accumulator.carry = remaining.slice(-partial);
                } else {
                    accumulator.answer += remaining;
                }
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

const isLikelyJson = (text) => {
    if (!text || typeof text !== 'string') return false;
    const trimmed = text.trim();
    if (!trimmed.startsWith('{') || !trimmed.endsWith('}')) return false;
    try {
        JSON.parse(trimmed);
        return true;
    } catch (error) {
        return false;
    }
};

const safeParseNestedJson = (value) => {
    if (typeof value !== 'string') return value;
    const trimmed = value.trim();
    if (!(trimmed.startsWith('{') && trimmed.endsWith('}'))) return value;
    try {
        return JSON.parse(trimmed);
    } catch (error) {
        return value;
    }
};

export const formatMcpJson = (content) => {
    if (!isLikelyJson(content)) return content;
    try {
        const parsed = JSON.parse(content.trim());
        if (parsed && typeof parsed === 'object') {
            if (Object.prototype.hasOwnProperty.call(parsed, 'mcp_parameters')) {
                parsed.mcp_parameters = safeParseNestedJson(parsed.mcp_parameters);
            }
        }
        return JSON.stringify(parsed, null, 2);
    } catch (error) {
        return content;
    }
};
