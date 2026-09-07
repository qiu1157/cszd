import { describe, expect, it } from 'vitest'

import { summarizeShareholdings, type StationShareholding } from './index'

function buildRelation(overrides: Partial<StationShareholding> = {}): StationShareholding {
  return {
    id: 1,
    shareholderId: 1,
    shareholderName: '测试股东',
    contactName: null,
    contactPhone: null,
    ownershipPercentage: 0,
    holdingStatus: 'ACTIVE',
    holdingStatusName: '持股中',
    effectiveAt: null,
    ...overrides,
  }
}

describe('summarizeShareholdings', () => {
  it('应只统计持股中关系的股东数与占比合计，已退出关系不参与口径', () => {
    const summary = summarizeShareholdings([
      buildRelation({ id: 1, ownershipPercentage: 60 }),
      buildRelation({ id: 2, ownershipPercentage: 25 }),
      buildRelation({ id: 3, ownershipPercentage: 15, holdingStatus: 'EXITED', holdingStatusName: '已退出' }),
    ])

    expect(summary.activeShareholderCount).toBe(2)
    expect(summary.activePercentageSum).toBe(85)
  })

  it('摘要口径应以状态码为准，不依赖中文状态名文案', () => {
    // 为什么：状态名是展示层文案（未来可能调整措辞或国际化），
    // 若按中文匹配，文案一变摘要口径就会静默失真
    const summary = summarizeShareholdings([
      buildRelation({ id: 1, ownershipPercentage: 60, holdingStatusName: '--' }),
      buildRelation({ id: 2, ownershipPercentage: 40, holdingStatus: 'EXITED', holdingStatusName: '持股中' }),
    ])

    expect(summary.activeShareholderCount).toBe(1)
    expect(summary.activePercentageSum).toBe(60)
  })

  it('占比合计应按两位小数精度取整，避免浮点尾差造成误报', () => {
    const summary = summarizeShareholdings([
      buildRelation({ id: 1, ownershipPercentage: 33.33 }),
      buildRelation({ id: 2, ownershipPercentage: 33.33 }),
      buildRelation({ id: 3, ownershipPercentage: 33.34 }),
    ])

    expect(summary.activeShareholderCount).toBe(3)
    expect(summary.activePercentageSum).toBe(100)
  })

  it('无持股中关系时应返回零值摘要，不产生警示噪音', () => {
    const summary = summarizeShareholdings([])

    expect(summary.activeShareholderCount).toBe(0)
    expect(summary.activePercentageSum).toBe(0)
  })
})