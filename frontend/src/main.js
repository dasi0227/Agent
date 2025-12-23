import { createApp } from 'vue';
import { createPinia } from 'pinia';
import router from './router/router';
import './style.css';
import App from './App.vue';

const setScrollbarWidth = () => {
    const width = window.innerWidth - document.documentElement.clientWidth;
    document.documentElement.style.setProperty('--scrollbar-w', `${width}px`);
};

setScrollbarWidth();
window.addEventListener('resize', setScrollbarWidth);

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.mount('#app');
