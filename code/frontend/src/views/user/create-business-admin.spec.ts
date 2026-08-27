import { describe, expect, it } from 'vitest'

import { buildCreateBusinessAdminPayload, validateCreateBusinessAdminForm } from './create-business-admin'

describe('validateCreateBusinessAdminForm', () => {
  it('应校验手机号、姓名和初始密码必填', () => {
    expect(
      validateCreateBusinessAdminForm({
        phone: '',
        name: ' ',
        initialPassword: '',
      }),
    ).toEqual({
      phone: '请输入手机号',
      name: '请输入姓名',
      initialPassword: '请输入初始密码',
    })
  })
})

describe('buildCreateBusinessAdminPayload', () => {
  it('应按接口要求返回新增业务管理员提交体', () => {
    expect(
      buildCreateBusinessAdminPayload({
        phone: '13800138000',
        name: '李丹',
        initialPassword: 'Init@123456',
      }),
    ).toEqual({
      phone: '13800138000',
      name: '李丹',
      initialPassword: 'Init@123456',
    })
  })
})
