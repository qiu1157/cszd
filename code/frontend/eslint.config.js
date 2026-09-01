import js from '@eslint/js'
import vue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'
import prettier from 'eslint-config-prettier'
import vueParser from 'vue-eslint-parser'

// 为什么：统一代码风格，避免格式/基础质量规则分散到各页面；第一期先落地基础 lint/format
export default [
  {
    ignores: ['dist/**', 'node_modules/**'],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...vue.configs['flat/recommended'],
  prettier,
  {
    files: ['**/*.{ts,tsx,vue}'],
    languageOptions: {
      // 为什么：`.vue` 文件需要先由 vue-eslint-parser 解析，再交给 TS parser 处理 script 部分
      parser: vueParser,
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
      },
    },
    rules: {
      // 项目偏好：可读性优先，避免隐式 any 与杂乱的 console
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-empty-object-type': 'off',
      // 为什么：构建已由 tsc 做真实的未定义符号检查，js.configs.recommended 的 no-undef
      // 对 .vue 不会识别浏览器全局变量（document 等）而产生误报，故按 typescript-eslint 官方建议关闭
      'no-undef': 'off',
      'no-console': ['warn', { allow: ['warn', 'error'] }],
    },
  },
]
