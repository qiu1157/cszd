import { describe, expect, it } from 'vitest'

import { mapBusinessAdminToTableRow } from './adapters'

describe('mapBusinessAdminToTableRow', () => {
  it('应把最后登录时间映射为可展示文本', () => {
    expect(
      mapBusinessAdminToTableRow({
        account: '13800138000',
        name: '李丹',
        role: 'BUSINESS_ADMIN',
        status: 'ENABLED',
        lastLoginAt: '2026-08-26T09:18:00',
      }),
    ).toEqual({
      account: '13800138000',
      subtitle: '业务管理员 · 李丹',
      roleLabel: '业务管理员',
      statusLabel: '启用',
      statusType: 'success',
      lastLoginText: '2026-08-26 09:18',
    })
  })

  it('应在最后登录时间为空时显示未登录', () => {
    expect(
      mapBusinessAdminToTableRow({
        account: '13900139000',
        name: '唐凯',
        role: 'BUSINESS_ADMIN',
        status: 'DISABLED',
        lastLoginAt: null,
      }).lastLoginText,
    ).toBe('未登录')
  })
})
