import { describe, expect, it } from 'vitest'

import { mapBusinessAdminToTableRow } from './adapters'

describe('mapBusinessAdminToTableRow', () => {
  it('应为启用账号开放停用按钮并禁用启用按钮', () => {
    expect(
      mapBusinessAdminToTableRow({
        account: '13800138000',
        name: '李丹',
        role: 'BUSINESS_ADMIN',
        status: 'ENABLED',
        lastLoginAt: '2026-08-27T18:00:00',
      }),
    ).toMatchObject({
      statusLabel: '启用',
      canEnable: false,
      canDisable: true,
    })
  })

  it('应为停用账号开放启用按钮并禁用停用按钮', () => {
    expect(
      mapBusinessAdminToTableRow({
        account: '13900139000',
        name: '唐凯',
        role: 'BUSINESS_ADMIN',
        status: 'DISABLED',
        lastLoginAt: null,
      }),
    ).toMatchObject({
      statusLabel: '停用',
      canEnable: true,
      canDisable: false,
    })
  })
})
