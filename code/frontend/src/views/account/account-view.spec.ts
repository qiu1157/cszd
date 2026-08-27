import { describe, expect, it } from 'vitest'

import { USER_ROLES } from '@/constants/enums/user-role'
import type { AuthUser } from '@/utils/auth-storage'

import { buildAccountSections } from './account-view'

describe('buildAccountSections', () => {
  it('应生成我的账号页面的核心信息与正式安全设置说明', () => {
    const user: AuthUser = {
      account: '13800138000',
      name: '李丹',
      phone: '13800138000',
      role: USER_ROLES.BUSINESS_ADMIN,
      status: 'ENABLED',
      lastLoginAt: '2026-08-27T21:08:00',
    }

    expect(buildAccountSections(user)).toEqual([
      {
        title: '账号概览',
        items: [
          { label: '姓名', value: '李丹' },
          { label: '登录账号', value: '13800138000' },
          { label: '手机号', value: '13800138000' },
          { label: '角色', value: '业务管理员' },
          { label: '账号状态', value: '启用' },
        ],
      },
      {
        title: '登录信息',
        items: [{ label: '最后登录时间', value: '2026-08-27 21:08' }],
      },
      {
        title: '安全设置',
        items: [{ label: '修改本人密码', value: '支持在线修改，成功后需重新登录' }],
      },
    ])
  })

  it('未登录时间为空时应显示未登录，并保留安全设置说明', () => {
    const user: AuthUser = {
      account: 'admin',
      name: '系统管理员',
      phone: null,
      role: USER_ROLES.SYSTEM_ADMIN,
      status: 'ENABLED',
      lastLoginAt: null,
    }

    expect(buildAccountSections(user)[2]).toEqual({
      title: '安全设置',
      items: [{ label: '修改本人密码', value: '支持在线修改，成功后需重新登录' }],
    })
  })
})
