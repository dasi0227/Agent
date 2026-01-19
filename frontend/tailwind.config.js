/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    theme: {
        extend: {
            fontFamily: {
                sans: ['"Space Grotesk"', '"Noto Sans SC"', 'Segoe UI', 'system-ui', '-apple-system', 'sans-serif']
            },
            keyframes: {
                blink: {
                    '0%, 80%, 100%': { opacity: '0.3' },
                    '40%': { opacity: '1' }
                },
                caretBlink: {
                    '0%, 50%': { opacity: '1' },
                    '50.1%, 100%': { opacity: '0' }
                }
            },
            animation: {
                blink: 'blink 1.2s infinite ease-in-out',
                caret: 'caretBlink 1s steps(1) infinite'
            }
        }
    },
    plugins: []
};
