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
