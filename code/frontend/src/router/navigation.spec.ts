import { describe, expect, it } from 'vitest'

import { USER_ROLES } from '@/constants/enums/user-role'

import { getDefaultRouteByRole, getForbiddenFallbackRouteByRole, SYSTEM_DICTIONARY_PATH } from './navigation'

describe('router navigation helpers', () => {
  it('系统管理员登录后的默认落点应为首页概览', () => {
    expect(getDefaultRouteByRole(USER_ROLES.SYSTEM_ADMIN)).toBe('/dashboard')
  })

  it('系统管理员无权限回跳也应回到首页概览', () => {
    expect(getForbiddenFallbackRouteByRole(USER_ROLES.SYSTEM_ADMIN)).toBe('/dashboard')
  })

  it('旧平台路由应兼容跳转到系统字典正式路由', () => {
    expect(SYSTEM_DICTIONARY_PATH).toBe('/system-dictionaries')
  })
})
