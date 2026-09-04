import { describe, expect, it } from 'vitest'

import source from './ShareholderListView.vue?raw'

describe('ShareholderListView source', () => {
  it('应渲染股东列表结构，并提供新增入口', () => {
    expect(source).toContain('股东列表页')
    expect(source).toContain('新增股东')
    expect(source).toContain('@click="openCreateDialog"')
    expect(source).toContain('请输入股东名称')
    expect(source).toContain('请选择状态')
    expect(source).toContain('SHAREHOLDER_STATUS_OPTIONS')
    expect(source).toContain('searchForm.shareholderStatus')
    expect(source).toContain('查询')
    expect(source).toContain('重置筛选')
    expect(source).toContain('handleResetFilters')
    expect(source).toContain('@keyup.enter')
    expect(source).toContain('股东名称')
    expect(source).toContain('row.shareholderSubtitle')
    expect(source).toContain('title-cell__subtitle')
    expect(source).toContain('关联站点')
    expect(source).toContain('个站点')
    expect(source).toContain('handleShowStationDetail')
    expect(source).toContain('stationDialogVisible')
    expect(source).toContain(':close-on-click-modal="false"')
    expect(source).toContain('暂无关联站点')
    expect(source).toContain('最近变化')
    expect(source).toContain('getShareholders')
    expect(source).toContain('handleSearch')
  })

  it('应包含新增股东弹窗，禁用遮罩与ESC关闭，保存中防重复提交', () => {
    expect(source).toContain('createDialogVisible')
    expect(source).toContain(':show-close="false"')
    expect(source).toContain(':close-on-press-escape="false"')
    expect(source).toContain('destroy-on-close')
    expect(source).toContain('openCreateDialog')
    expect(source).toContain('closeCreateDialog')
    expect(source).toContain('validateShareholderForm')
    expect(source).toContain('buildShareholderCreatePayload')
    expect(source).toContain('createShareholder')
    expect(source).toContain('submitCreateForm')
    expect(source).toContain(':loading="creating"')
    expect(source).toContain('股东新增成功')
    expect(source).toContain('validateCreateField')
  })

  it('不应渲染原型中未要求或后续任务实现的入口', () => {
    expect(source).not.toContain('导出列表')
    expect(source).not.toContain('排序')
    expect(source).not.toContain('查看详情')
    expect(source).not.toContain('label="操作"')
  })
})