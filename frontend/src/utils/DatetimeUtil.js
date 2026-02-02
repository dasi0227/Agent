export const formatDateTime = (val) => {
    if (!val) return '-';
    if (val instanceof Date) {
        const y = val.getFullYear();
        const m = String(val.getMonth() + 1).padStart(2, '0');
        const d = String(val.getDate()).padStart(2, '0');
        const hh = String(val.getHours()).padStart(2, '0');
        const mm = String(val.getMinutes()).padStart(2, '0');
        const ss = String(val.getSeconds()).padStart(2, '0');
        return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
    }
    const str = String(val);
    const main = str.replace('T', ' ').replace('Z', '').split('.')[0];
    return main || str;
};
