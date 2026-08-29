import { describe, expect, it } from 'vitest'

import {
  buildSystemDictItemPayload,
  validateSystemDictItemForm,
  type SystemDictItemForm,
} from './system-dictionary-form'

describe('system dictionary form helpers', () => {
  it('应校验名称、编码、状态必填，排序允许为空', () => {
    const errors = validateSystemDictItemForm({
      itemName: '   ',
      itemCode: '',
      status: '',
      sortOrder: '',
    })

    expect(errors).toEqual({
      itemName: '请输入名称',
      itemCode: '请输入编码',
      status: '请选择状态',
    })
  })

  it('应将排序空值转为0，并裁剪名称和编码首尾空格', () => {
    const payload = buildSystemDictItemPayload(createForm({
      itemName: '  国网  ',
      itemCode: '  STATE_GRID  ',
      status: 'ENABLED',
      sortOrder: '',
    }))

    expect(payload).toEqual({
      itemName: '国网',
      itemCode: 'STATE_GRID',
      status: 'ENABLED',
      sortOrder: 0,
    })
  })

  it('应拒绝非法排序输入', () => {
    const errors = validateSystemDictItemForm(createForm({ sortOrder: '-1' }))

    expect(errors.sortOrder).toBe('排序不能小于0')
  })
})

function createForm(overrides: Partial<SystemDictItemForm> = {}): SystemDictItemForm {
  return {
    itemName: '国网',
    itemCode: 'STATE_GRID',
    status: 'ENABLED',
    sortOrder: '10',
    ...overrides,
  }
}
