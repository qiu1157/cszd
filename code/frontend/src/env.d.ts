/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // 为什么：为 TypeScript 提供 .vue 模块类型声明，避免 TS 无法识别组件导入
  const component: DefineComponent<{}, {}, any>
  export default component
}

