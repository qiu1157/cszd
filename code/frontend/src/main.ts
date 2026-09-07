import 'element-plus/dist/index.css'
// 为什么：应用是深色主题，弹窗/下拉浮层按浅色渲染会出现白底浅字不可读，需在浅色样式之后引入官方暗色变量覆盖
import 'element-plus/theme-chalk/dark/css-vars.css'
import ElementPlus from 'element-plus'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import pinia from './stores'
import './styles/global.scss'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
