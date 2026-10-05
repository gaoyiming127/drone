import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
// 引入即注册全局 axios 拦截器，保证所有接口请求都携带令牌并统一处理错误
import './utils/request'

const app = createApp(App)

app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.mount('#app')
