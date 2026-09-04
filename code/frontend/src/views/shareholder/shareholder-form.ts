import type { ShareholderCreatePayload } from '@/api/shareholder'
import type { ShareholderStatus } from '@/constants/enums/shareholder-status'

export type ShareholderForm = {
  shareholderName: string
  contactName: string
  contactPhone: string
  shareholderStatus: '' | ShareholderStatus
}

export type ShareholderFormErrors = Partial<Record<keyof ShareholderForm, string>>

export function validateShareholderForm(form: ShareholderForm): ShareholderFormErrors {
  const errors: ShareholderFormErrors = {}

  if (!form.shareholderName.trim()) {
    errors.shareholderName = '请输入股东名称'
  } else if (form.shareholderName.trim().length > 128) {
    errors.shareholderName = '股东名称不能超过128个字符'
  }

  if (form.contactName.trim().length > 64) {
    errors.contactName = '联系人不能超过64个字符'
  }

  if (form.contactPhone.trim().length > 20) {
    errors.contactPhone = '联系电话不能超过20个字符'
  }

  if (!form.shareholderStatus) {
    errors.shareholderStatus = '请选择股东状态'
  }

  return errors
}

export function buildShareholderCreatePayload(form: ShareholderForm): ShareholderCreatePayload {
  return {
    shareholderName: form.shareholderName.trim(),
    contactName: form.contactName.trim(),
    contactPhone: form.contactPhone.trim(),
    shareholderStatus: form.shareholderStatus,
  }
}