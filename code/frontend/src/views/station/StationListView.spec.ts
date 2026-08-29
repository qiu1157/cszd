import { describe, expect, it } from 'vitest'

import source from './StationListView.vue?raw'

describe('StationListView source', () => {
  it('应渲染基础列表结构，并将新增按钮保持禁用态', () => {
    expect(source).toContain('充电站管理')
    expect(source).toContain('新增充电站')
    expect(source).toContain(':disabled="true"')
    expect(source).toContain('充电站编号')
    expect(source).toContain('所在平台')
    expect(source).toContain('当前状态')
    expect(source).toContain('查看详情')
    expect(source).not.toContain('按名称搜索')
    expect(source).not.toContain('平台筛选')
    expect(source).not.toContain('状态筛选')
  })
})
