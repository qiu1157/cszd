import { describe, expect, it } from 'vitest'

import source from './StationDetailView.vue?raw'

describe('StationDetailView source', () => {
  it('应渲染详情页四分区骨架，并提供编辑入口', () => {
    expect(source).toContain('充电站详情')
    expect(source).toContain('station-summary')
    expect(source).toContain('概览')
    expect(source).toContain('分区导航')
    expect(source).toContain('section-anchor')
    expect(source).toContain('handleAnchorClick')
    expect(source).toContain('基础信息分区')
    expect(source).toContain('基础信息')
    expect(source).toContain('充电桩管理')
    expect(source).toContain('股东信息')
    expect(source).toContain('历史变化时间线')
    expect(source).toContain('kpi-strip')
    expect(source).toContain('修改状态')
    expect(source).toContain('编辑基础信息')
    expect(source).toContain('goToEdit')
    expect(source).toContain('getStationDetail')
    expect(source).toContain('后续任务实现')
  })

  it('应接入充电桩分区组件，并在概览卡展示桩/枪规模统计', () => {
    expect(source).toContain('StationPileSection')
    expect(source).toContain(':station-id="resolvedStationId"')
    expect(source).toContain('@loaded="applyPileSummary"')
    expect(source).toContain('pileSummaryText')
    expect(source).toContain('根 / ')
    expect(source).toContain('把枪')
  })

  it('应提供可点击的修改状态入口，且弹窗禁止遮罩与ESC关闭', () => {
    expect(source).not.toContain(':disabled="true">修改状态')
    expect(source).toContain('openStatusDialog')
    expect(source).toContain('statusDialogVisible')
    expect(source).toContain('修改站点状态')
    expect(source).toContain(':show-close="false"')
    expect(source).toContain(':close-on-click-modal="false"')
    expect(source).toContain(':close-on-press-escape="false"')
    expect(source).toContain('取消')
    expect(source).toContain('保存状态')
  })

  it('应在新状态选择与备注输入上约束数据，并在保存后刷新详情', () => {
    expect(source).toContain('statusForm.targetStatusItemId')
    expect(source).toContain('statusForm.remark')
    expect(source).toContain('maxlength="500"')
    expect(source).toContain('show-word-limit')
    expect(source).toContain('getStationStatusOptions')
    expect(source).toContain('updateStationStatus')
    expect(source).toContain('submitStatusForm')
    expect(source).toContain('状态切换成功')
  })
})
