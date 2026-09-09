// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import type { TimelineEvent } from '@/api/operation-log'

import TimelineView from './TimelineView.vue'
import source from './TimelineView.vue?raw'

describe('TimelineView source', () => {
  it('应支持充电站和股东两种视角的 viewType 属性', () => {
    expect(source).toContain("viewType?: 'station' | 'shareholder'")
    expect(source).toContain("viewType: 'station'")
  })

  it('应渲染加载中、空态和列表三种展示状态', () => {
    expect(source).toContain('timeline-loading')
    expect(source).toContain('加载中...')
    expect(source).toContain('timeline-empty')
    expect(source).toContain('暂无历史记录')
    expect(source).toContain('timeline-list')
  })

  it('时间线元信息应包含操作时间、操作人账号和操作类型标签', () => {
    expect(source).toContain('timeline-meta')
    expect(source).toContain('formatTime(event.operatedAt)')
    expect(source).toContain('event.operatorAccount')
    expect(source).toContain('formatOperationType(event.operationType)')
  })

  it('应按操作类型映射标签颜色：CREATE绿色、UPDATE蓝色、STATUS_CHANGE橙色、EQUITY_ADJUST紫色、EXIT红色', () => {
    expect(source).toContain('.timeline-tag.create')
    expect(source).toContain('#67c23a')
    expect(source).toContain('.timeline-tag.update')
    expect(source).toContain('#409eff')
    expect(source).toContain('.timeline-tag.status_change')
    expect(source).toContain('#e6a23c')
    expect(source).toContain('.timeline-tag.equity_adjust')
    expect(source).toContain('#9c27b0')
    expect(source).toContain('.timeline-tag.exit')
    expect(source).toContain('#f56c6c')
  })

  it('应为股权调整操作类型使用 EquityAdjustChanges 组件渲染', () => {
    expect(source).toContain('EquityAdjustChanges')
    expect(source).toContain('isEquityAdjust(event)')
    expect(source).toContain(':before-state="parseState(event.beforeState)"')
    expect(source).toContain(':after-state="parseState(event.afterState)"')
  })

  it('应为持股关系新增操作显示股东或充电站关联信息', () => {
    expect(source).toContain('isShareholdingCreate(event)')
    expect(source).toContain('getShareholdingLabel()')
    expect(source).toContain('formatShareholdingLine')
  })

  it('应为持股关系退出操作显示股东或充电站退出信息', () => {
    expect(source).toContain('isShareholdingExit(event)')
    expect(source).toContain('已退出')
  })

  it('应为状态变更操作显示前后状态对比', () => {
    expect(source).toContain('isStatusChange(event)')
    expect(source).toContain('formatStatusValue(event, \'before\')')
    expect(source).toContain('formatStatusValue(event, \'after\')')
  })

  it('应为普通修改操作显示字段级变化明细', () => {
    expect(source).toContain('diffChanges(event)')
    expect(source).toContain('change.label')
    expect(source).toContain('change.oldValue')
    expect(source).toContain('change.newValue')
  })

  it('在股东视角下持股关系标题应显示充电站名称前缀', () => {
    expect(source).toContain("props.viewType === 'shareholder'")
    expect(source).toContain('extractStationName(event)')
    expect(source).toContain('【${stationName}】')
  })

  it('应根据视角切换持股关系的标签文本：充电站视角显示股东，股东视角显示充电站', () => {
    expect(source).toContain('getShareholdingLabel()')
    expect(source).toContain("props.viewType === 'shareholder' ? '充电站' : '股东'")
  })

  it('变化行样式应统一：主文本深色、标签次深色、旧值浅色删除线、新值蓝色', () => {
    expect(source).toContain('.timeline-change-text')
    expect(source).toContain('var(--text-primary)')
    expect(source).toContain('.timeline-change-label')
    expect(source).toContain('var(--text-secondary)')
    expect(source).toContain('.timeline-change-old')
    expect(source).toContain('text-decoration: line-through')
    expect(source).toContain('.timeline-change-new')
    expect(source).toContain('color: #409eff')
  })
})

describe('TimelineView 渲染', () => {
  it('loading 状态应显示加载提示', () => {
    const wrapper = mount(TimelineView, {
      props: {
        events: [],
        loading: true,
      },
    })

    expect(wrapper.find('.timeline-loading').exists()).toBe(true)
    expect(wrapper.find('.timeline-loading').text()).toBe('加载中...')
  })

  it('空数据应显示空态提示', () => {
    const wrapper = mount(TimelineView, {
      props: {
        events: [],
        loading: false,
      },
    })

    expect(wrapper.find('.timeline-empty').exists()).toBe(true)
    expect(wrapper.find('.timeline-empty').text()).toBe('暂无历史记录')
  })

  it('应渲染充电站新增操作', () => {
    const events: TimelineEvent[] = [
      {
        id: 1,
        operatedAt: '2026-09-09T10:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'STATION',
        targetId: '1',
        targetName: 'ST-001',
        operationType: 'CREATE',
        remark: '',
        beforeState: null,
        afterState: JSON.stringify({ stationName: '测试充电站', address: '测试地址' }),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('新增充电站')
    expect(wrapper.find('.timeline-tag.create').exists()).toBe(true)
  })

  it('应渲染充电站修改操作并显示字段变化', () => {
    const events: TimelineEvent[] = [
      {
        id: 2,
        operatedAt: '2026-09-09T11:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'STATION',
        targetId: '1',
        targetName: 'ST-001',
        operationType: 'UPDATE',
        remark: '',
        beforeState: JSON.stringify({ stationName: '旧名称', address: '旧地址' }),
        afterState: JSON.stringify({ stationName: '新名称', address: '新地址' }),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('修改基础信息')
    expect(wrapper.find('.timeline-tag.update').exists()).toBe(true)
    expect(wrapper.text()).toContain('充电站名称')
    expect(wrapper.text()).toContain('旧名称')
    expect(wrapper.text()).toContain('新名称')
  })

  it('应渲染充电站状态变更操作', () => {
    const events: TimelineEvent[] = [
      {
        id: 3,
        operatedAt: '2026-09-09T12:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'STATION',
        targetId: '1',
        targetName: 'ST-001',
        operationType: 'STATUS_CHANGE',
        remark: '',
        beforeState: JSON.stringify({ stationStatusItemId: 'ACTIVE' }),
        afterState: JSON.stringify({ stationStatusItemId: 'INACTIVE' }),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('修改站点状态')
    expect(wrapper.find('.timeline-tag.status_change').exists()).toBe(true)
  })

  it('应渲染充电桩新增操作', () => {
    const events: TimelineEvent[] = [
      {
        id: 4,
        operatedAt: '2026-09-09T13:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'PILE',
        targetId: '1',
        targetName: 'PILE-001',
        operationType: 'CREATE',
        remark: '',
        beforeState: null,
        afterState: JSON.stringify({ pileCode: 'PILE-001', chargeType: 'DC', gunCount: 2 }),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('新增充电桩')
    expect(wrapper.find('.timeline-tag.create').exists()).toBe(true)
  })

  it('应在充电站视角渲染股东关联操作', () => {
    const events: TimelineEvent[] = [
      {
        id: 5,
        operatedAt: '2026-09-09T14:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'SHAREHOLDING',
        targetId: '1',
        targetName: 'SH-001',
        operationType: 'CREATE',
        remark: '',
        beforeState: null,
        afterState: JSON.stringify({ shareholderId: 1, shareholderName: '张三', ownershipPercentage: 60 }),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
        viewType: 'station',
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('关联股东')
    expect(wrapper.text()).toContain('股东')
    expect(wrapper.text()).toContain('张三 占股 60%')
  })

  it('应在股东视角渲染充电站关联操作并显示充电站名称前缀', () => {
    const events: TimelineEvent[] = [
      {
        id: 6,
        operatedAt: '2026-09-09T15:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'SHAREHOLDING',
        targetId: '1',
        targetName: 'SH-001',
        operationType: 'CREATE',
        remark: '',
        beforeState: null,
        afterState: JSON.stringify({ stationId: 1, stationName: '测试充电站', ownershipPercentage: 60 }),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
        viewType: 'shareholder',
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('【测试充电站】关联充电站')
    expect(wrapper.text()).toContain('充电站')
    expect(wrapper.text()).toContain('测试充电站 占股 60%')
  })

  it('应在充电站视角渲染股东退出操作', () => {
    const events: TimelineEvent[] = [
      {
        id: 7,
        operatedAt: '2026-09-09T16:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'SHAREHOLDING',
        targetId: '1',
        targetName: 'SH-001',
        operationType: 'EXIT',
        remark: '',
        beforeState: JSON.stringify({ shareholderId: 1, shareholderName: '张三', ownershipPercentage: 60 }),
        afterState: null,
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
        viewType: 'station',
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('股东退出')
    expect(wrapper.find('.timeline-tag.exit').exists()).toBe(true)
    expect(wrapper.text()).toContain('股东')
    expect(wrapper.text()).toContain('张三 占股 60%')
    expect(wrapper.text()).toContain('已退出')
  })

  it('应在股东视角渲染充电站退出操作', () => {
    const events: TimelineEvent[] = [
      {
        id: 8,
        operatedAt: '2026-09-09T17:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'SHAREHOLDING',
        targetId: '1',
        targetName: 'SH-001',
        operationType: 'EXIT',
        remark: '',
        beforeState: JSON.stringify({ stationId: 1, stationName: '测试充电站', ownershipPercentage: 60 }),
        afterState: null,
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
        viewType: 'shareholder',
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('【测试充电站】退出充电站')
    expect(wrapper.text()).toContain('充电站')
    expect(wrapper.text()).toContain('测试充电站 占股 60%')
    expect(wrapper.text()).toContain('已退出')
  })

  it('应渲染股权调整操作并使用 EquityAdjustChanges 组件', () => {
    const events: TimelineEvent[] = [
      {
        id: 9,
        operatedAt: '2026-09-09T18:00:00',
        operatorAccount: 'admin',
        operatorRole: 'PLATFORM_ADMIN',
        targetType: 'SHAREHOLDING',
        targetId: '1',
        targetName: 'SH-001',
        operationType: 'EQUITY_ADJUST',
        remark: '调整股权比例',
        beforeState: JSON.stringify([
          { shareholderId: 1, shareholderName: '张三', ownershipPercentage: 60 },
          { shareholderId: 2, shareholderName: '李四', ownershipPercentage: 40 },
        ]),
        afterState: JSON.stringify([
          { shareholderId: 1, shareholderName: '张三', ownershipPercentage: 50 },
          { shareholderId: 2, shareholderName: '李四', ownershipPercentage: 50 },
        ]),
      },
    ]

    const wrapper = mount(TimelineView, {
      props: {
        events,
        loading: false,
      },
    })

    expect(wrapper.find('.timeline-title').text()).toBe('股权调整')
    expect(wrapper.find('.timeline-tag.equity_adjust').exists()).toBe(true)
    expect(wrapper.text()).toContain('备注：调整股权比例')
    expect(wrapper.text()).toContain('股东 张三')
    expect(wrapper.text()).toContain('60%')
    expect(wrapper.text()).toContain('50%')
  })
})
