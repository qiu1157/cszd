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
      buildRelation({ id: 3, ownershipPercentage: 15, holdingStatusName: '已退出' }),
    ])

    expect(summary.activeShareholderCount).toBe(2)
    expect(summary.activePercentageSum).toBe(85)
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