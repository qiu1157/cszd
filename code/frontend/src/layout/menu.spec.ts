import { describe, expect, it } from 'vitest'

import { appMenuItems } from './menu'

type MenuRole = 'SYSTEM_ADMIN' | 'BUSINESS_ADMIN'

function getTitlesByRole(role: MenuRole) {
  return (appMenuItems as Array<{ title: string; roles?: MenuRole[] }>)
    .filter((item) => item.roles?.includes(role))
    .map((item) => item.title)
}

describe('appMenuItems', () => {
  it('系统管理员应看到充电站与股东管理入口', () => {
    expect(getTitlesByRole('SYSTEM_ADMIN')).toEqual([
      '首页',
      '充电站管理',
      '股东管理',
      '系统字典',
      '操作日志',
      '用户管理',
      '我的账号',
    ])
  })

  it('业务管理员只看到业务侧入口', () => {
    expect(getTitlesByRole('BUSINESS_ADMIN')).toEqual([
      '首页',
      '充电站管理',
      '股东管理',
      '操作日志',
      '我的账号',
    ])
  })
})
