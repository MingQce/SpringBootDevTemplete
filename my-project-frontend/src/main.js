import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";

const app = createApp(App)

app.use(router) //调用router

app.mount('#app')   //挂载
