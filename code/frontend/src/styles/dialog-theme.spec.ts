import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'
import indexHtml from '../../index.html?raw'
import mainSource from '../main.ts?raw'

// 为什么：vitest 默认将 scss 模块 stub 为空对象，?raw 拿不到源码，改用文件系统直读
const globalScss = readFileSync(new URL('./global.scss', import.meta.url), 'utf-8')

// 为什么：应用是深色工业风，而 Element Plus 默认按浅色主题渲染，
// 导致所有弹窗出现"白底面板 + 近白文字"不可读的问题；
// 此处以源码断言锁定"官方暗色变量 + 原型 modal 皮肤"的修复，防止回归。
describe('弹窗暗色主题（对齐原型 modal 基调）', () => {
  it('index.html 根节点挂载 dark 类，激活 Element Plus 暗色变量', () => {
    expect(indexHtml).toContain('<html lang="zh-CN" class="dark">')
  })

  it('main.ts 在浅色样式之后引入官方暗色变量，保证暗色生效', () => {
    const lightImportIndex = mainSource.indexOf('element-plus/dist/index.css')
    const darkImportIndex = mainSource.indexOf('element-plus/theme-chalk/dark/css-vars.css')
    expect(lightImportIndex).toBeGreaterThan(-1)
    expect(darkImportIndex).toBeGreaterThan(lightImportIndex)
  })

  it('global.scss 按原型 modal 覆盖弹窗遮罩：深色叠加 + 背景模糊', () => {
    expect(globalScss).toContain('.el-overlay')
    expect(globalScss).toContain('rgba(6, 10, 16, 0.72)')
    expect(globalScss).toContain('blur(6px)')
  })

  it('global.scss 按原型 modal-card 覆盖弹窗面板皮肤与标题颜色', () => {
    expect(globalScss).toContain('.el-dialog')
    expect(globalScss).toContain('.el-message-box')
    expect(globalScss).toContain('var(--bg-panel-strong)')
    expect(globalScss).toContain('border-radius: 18px')
    expect(globalScss).toContain('0 30px 80px rgba(0, 0, 0, 0.45)')
    expect(globalScss).toContain('.el-dialog__title')
    expect(globalScss).toContain('.el-message-box__title')
  })
})