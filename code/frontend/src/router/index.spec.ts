import { describe, expect, it } from 'vitest'

import source from './index.ts?raw'

describe('router source', () => {
  it('系统管理员应可访问充电站列表路由', () => {
    expect(source).toContain("path: '/stations'")
    expect(source).toContain("roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN]")
  })

  it('应提供新增充电站页面路由', () => {
    expect(source).toContain("path: STATION_CREATE_PATH")
    expect(source).toContain("name: 'StationCreate'")
  })

  it('应提供编辑充电站页面路由', () => {
    expect(source).toContain('path: STATION_EDIT_PATH')
    expect(source).toContain("name: 'StationEdit'")
  })

  it('应提供充电站详情页面路由', () => {
    expect(source).toContain('path: STATION_DETAIL_PATH')
    expect(source).toContain("name: 'StationDetail'")
  })

  it('应提供股东详情页面路由', () => {
    expect(source).toContain('path: SHAREHOLDER_DETAIL_PATH')
    expect(source).toContain("name: 'ShareholderDetail'")
  })
})
