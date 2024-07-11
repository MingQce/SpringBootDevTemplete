import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import axios from "axios";
import 'element-plus/theme-chalk/dark/css-vars.css';//暗黑模式

axios.defaults.baseURL = 'http://localhost:8080'
const app = createApp(App)

app.use(router) //调用router

app.mount('#app')   //挂载
