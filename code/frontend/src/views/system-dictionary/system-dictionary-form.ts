import type { SystemDictItemPayload } from '@/api/system-dictionary'

export type SystemDictItemForm = {
  itemName: string
  itemCode: string
  status: '' | 'ENABLED' | 'DISABLED'
  sortOrder: string
}

export type SystemDictItemFormErrors = Partial<Record<keyof SystemDictItemForm, string>>

export function validateSystemDictItemForm(form: SystemDictItemForm): SystemDictItemFormErrors {
  const errors: SystemDictItemFormErrors = {}

  if (!form.itemName.trim()) {
    errors.itemName = '请输入名称'
  }

  if (!form.itemCode.trim()) {
    errors.itemCode = '请输入编码'
  }

  if (!form.status) {
    errors.status = '请选择状态'
  }

  if (form.sortOrder.trim()) {
    const normalizedSortOrder = Number(form.sortOrder)
    if (!Number.isInteger(normalizedSortOrder)) {
      errors.sortOrder = '排序必须为整数'
    } else if (normalizedSortOrder < 0) {
      errors.sortOrder = '排序不能小于0'
    }
  }

  return errors
}

export function buildSystemDictItemPayload(form: SystemDictItemForm): SystemDictItemPayload {
  return {
    itemName: form.itemName.trim(),
    itemCode: form.itemCode.trim(),
    status: form.status || 'ENABLED',
    sortOrder: form.sortOrder.trim() ? Number(form.sortOrder) : 0,
  }
}
