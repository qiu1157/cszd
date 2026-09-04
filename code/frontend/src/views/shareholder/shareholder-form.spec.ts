import { describe, expect, it } from 'vitest'

import {
  buildShareholderCreatePayload,
  buildShareholderUpdatePayload,
  validateShareholderEditForm,
  validateShareholderForm,
  type ShareholderForm,
} from './shareholder-form'

describe('shareholder form helpers', () => {
  it('应校验股东名称与状态必填，联系人与电话允许为空', () => {
    const errors = validateShareholderForm({
      shareholderName: '   ',
      contactName: '',
      contactPhone: '',
      shareholderStatus: '',
    })

    expect(errors).toEqual({
      shareholderName: '请输入股东名称',
      shareholderStatus: '请选择股东状态',
    })
  })

  it('应拒绝超出数据库字段长度的名称、联系人与电话', () => {
    const errors = validateShareholderForm(createForm({
      shareholderName: '股'.repeat(129),
      contactName: '名'.repeat(65),
      contactPhone: '1'.repeat(21),
    }))

    expect(errors.shareholderName).toBe('股东名称不能超过128个字符')
    expect(errors.contactName).toBe('联系人不能超过64个字符')
    expect(errors.contactPhone).toBe('联系电话不能超过20个字符')
  })

  it('应裁剪字段首尾空格后构建新增请求', () => {
    const payload = buildShareholderCreatePayload(createForm({
      shareholderName: '  长沙城新能源合伙企业  ',
      contactName: ' 刘可 ',
      contactPhone: ' 13977882211 ',
    }))

    expect(payload).toEqual({
      shareholderName: '长沙城新能源合伙企业',
      contactName: '刘可',
      contactPhone: '13977882211',
      shareholderStatus: 'ENABLED',
    })
  })

  it('应校验编辑表单名称必填，且不校验股东状态', () => {
    const errors = validateShareholderEditForm({
      shareholderName: '   ',
      contactName: '',
      contactPhone: '',
    })

    expect(errors).toEqual({
      shareholderName: '请输入股东名称',
    })
  })

  it('应拒绝超出数据库字段长度的编辑字段', () => {
    const errors = validateShareholderEditForm({
      shareholderName: '股'.repeat(129),
      contactName: '名'.repeat(65),
      contactPhone: '1'.repeat(21),
    })

    expect(errors.shareholderName).toBe('股东名称不能超过128个字符')
    expect(errors.contactName).toBe('联系人不能超过64个字符')
    expect(errors.contactPhone).toBe('联系电话不能超过20个字符')
  })

  it('应裁剪字段首尾空格后构建更新请求', () => {
    const payload = buildShareholderUpdatePayload({
      shareholderName: '  长沙城新能源合伙企业（更新）  ',
      contactName: ' 刘可 ',
      contactPhone: ' 13977882211 ',
    })

    expect(payload).toEqual({
      shareholderName: '长沙城新能源合伙企业（更新）',
      contactName: '刘可',
      contactPhone: '13977882211',
    })
  })
})

function createForm(overrides: Partial<ShareholderForm> = {}): ShareholderForm {
  return {
    shareholderName: '长沙城新能源合伙企业',
    contactName: '刘可',
    contactPhone: '13977882211',
    shareholderStatus: 'ENABLED',
    ...overrides,
  }
}