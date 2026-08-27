import { describe, expect, it } from 'vitest'

import { buildResetBusinessAdminPayload, validateResetBusinessAdminForm } from './reset-business-admin'

describe('validateResetBusinessAdminForm', () => {
  it('应校验新密码必填', () => {
    expect(
      validateResetBusinessAdminForm({
        newPassword: ' ',
      }),
    ).toEqual({
      newPassword: '请输入新密码',
    })
  })

  it('应校验新密码为6-20位且包含字母和数字', () => {
    expect(
      validateResetBusinessAdminForm({
        newPassword: '123456',
      }),
    ).toEqual({
      newPassword: '请输入6-20位且包含字母和数字的新密码',
    })
  })
})

describe('buildResetBusinessAdminPayload', () => {
  it('应返回去除首尾空格后的新密码', () => {
    expect(
      buildResetBusinessAdminPayload({
        newPassword: ' Reset123 ',
      }),
    ).toEqual({
      newPassword: 'Reset123',
    })
  })
})
