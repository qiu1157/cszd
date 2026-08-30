import { describe, expect, it } from 'vitest'

import source from './StationListView.vue?raw'

describe('StationListView source', () => {
  it('应渲染基础列表结构，并提供新增入口', () => {
    expect(source).toContain('充电站管理')
    expect(source).toContain('新增充电站')
    expect(source).toContain('请输入充电站名称')
    expect(source).toContain('请选择平台')
    expect(source).toContain('请选择状态')
    expect(source).toContain('platformOptions')
    expect(source).toContain('statusOptions')
    expect(source).toContain('searchForm.platformItemId')
    expect(source).toContain('searchForm.stationStatusItemId')
    expect(source).toContain('查询')
    expect(source).toContain('@keyup.enter')
    expect(source).toContain('@click="goToCreate"')
    expect(source).toContain('充电站编号')
    expect(source).toContain('所在平台')
    expect(source).toContain('当前状态')
    expect(source).toContain('查看详情')
    expect(source).not.toContain('按名称搜索')
    expect(source).not.toContain('平台筛选')
    expect(source).not.toContain('状态筛选')
  })
})
