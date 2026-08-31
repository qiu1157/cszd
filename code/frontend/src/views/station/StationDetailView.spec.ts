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
})
