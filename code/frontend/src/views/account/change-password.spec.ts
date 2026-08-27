import { describe, expect, it } from 'vitest'

import {
  buildChangePasswordPayload,
  validateChangePasswordForm,
} from './change-password'

describe('validateChangePasswordForm', () => {
  it('应校验旧密码、新密码、确认新密码', () => {
    expect(
      validateChangePasswordForm({
        currentPassword: ' ',
        newPassword: '123456',
        confirmPassword: '654321',
      }),
    ).toEqual({
      currentPassword: '请输入旧密码',
      newPassword: '请输入6-20位且包含字母和数字的新密码',
      confirmPassword: '确认密码与新密码不一致',
    })
  })

  it('应禁止新旧密码相同', () => {
    expect(
      validateChangePasswordForm({
        currentPassword: 'Reset123',
        newPassword: 'Reset123',
        confirmPassword: 'Reset123',
      }),
    ).toEqual({
      newPassword: '新密码不能与旧密码相同',
    })
  })
})

describe('buildChangePasswordPayload', () => {
  it('应返回去除首尾空格后的请求体', () => {
    expect(
      buildChangePasswordPayload({
        currentPassword: ' old123 ',
        newPassword: ' New123 ',
        confirmPassword: ' New123 ',
      }),
    ).toEqual({
      currentPassword: 'old123',
      newPassword: 'New123',
      confirmPassword: 'New123',
    })
  })
})
