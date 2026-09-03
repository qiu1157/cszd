import { describe, expect, it } from 'vitest'

import source from './ShareholderListView.vue?raw'

describe('ShareholderListView source', () => {
  it('应渲染股东列表结构，并提供新增入口', () => {
    expect(source).toContain('股东列表页')
    expect(source).toContain('新增股东')
    expect(source).toContain('@click="handleCreate"')
    expect(source).toContain('请输入股东名称')
    expect(source).toContain('请选择状态')
    expect(source).toContain('SHAREHOLDER_STATUS_OPTIONS')
    expect(source).toContain('searchForm.shareholderStatus')
    expect(source).toContain('查询')
    expect(source).toContain('重置筛选')
    expect(source).toContain('handleResetFilters')
    expect(source).toContain('@keyup.enter')
    expect(source).toContain('股东主档')
    expect(source).toContain('row.shareholderSubtitle')
    expect(source).toContain('title-cell__subtitle')
    expect(source).toContain('关联站点')
    expect(source).toContain('个站点')
    expect(source).toContain('最近变化')
    expect(source).toContain('getShareholders')
    expect(source).toContain('handleSearch')
  })

  it('不应渲染原型中未要求或后续任务实现的入口', () => {
    expect(source).not.toContain('导出列表')
    expect(source).not.toContain('排序')
    expect(source).not.toContain('查看详情')
    expect(source).not.toContain('label="操作"')
  })
})