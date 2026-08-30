import { describe, expect, it } from 'vitest'

import source from './StationEditView.vue?raw'

describe('StationEditView source', () => {
  it('应渲染编辑充电站表单，且当前状态保持只读展示', () => {
    expect(source).toContain('编辑充电站')
    expect(source).toContain('保存并返回列表')
    expect(source).toContain('当前状态')
    expect(source).toContain('disabled')
    expect(source).toContain('加载中')
  })
})
