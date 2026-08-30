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
})
