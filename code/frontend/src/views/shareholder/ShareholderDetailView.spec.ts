import { describe, expect, it } from 'vitest'

import source from './ShareholderDetailView.vue?raw'

describe('ShareholderDetailView source', () => {
  it('应渲染详情页分区骨架，并提供返回列表入口', () => {
    expect(source).toContain('股东详情')
    expect(source).toContain('shareholder-summary')
    expect(source).toContain('概览')
    expect(source).toContain('分区导航')
    expect(source).toContain('section-anchor')
    expect(source).toContain('handleAnchorClick')
    expect(source).toContain('基础信息分区')
    expect(source).toContain('关联充电站')
    expect(source).toContain('历史变化时间线')
    expect(source).toContain('kpi-strip')
    expect(source).toContain('返回列表')
    expect(source).toContain('getShareholderDetail')
    expect(source).toContain('后续任务实现')
  })

  it('应按路由参数加载详情，非法或缺失时回退列表页', () => {
    expect(source).toContain('resolveShareholderId')
    expect(source).toContain('route.params.id')
    expect(source).toContain('SHAREHOLDER_LIST_PATH')
    expect(source).toContain('股东不存在')
    expect(source).toContain('股东详情加载失败')
    expect(source).toContain('formatDateTimeText')
  })

  it('应渲染关联充电站分区，支持空态、失败重试与占比展示', () => {
    expect(source).toContain('getShareholderStations')
    expect(source).toContain('stationList')
    expect(source).toContain('shareholder-station-table')
    expect(source).toContain('站点编号')
    expect(source).toContain('站点名称')
    expect(source).toContain('持股占比')
    expect(source).toContain('生效时间')
    expect(source).toContain('暂无关联站点')
    expect(source).toContain('关联充电站加载失败')
    expect(source).toContain('重新加载')
    expect(source).toContain('reloadShareholderStations')
    expect(source).toContain('stationCountText')
    expect(source).toContain('formatOwnershipText')
  })
})