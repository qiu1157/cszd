export type CreateBusinessAdminForm = {
  phone: string
  name: string
  initialPassword: string
}

export type CreateBusinessAdminFormErrors = Partial<Record<keyof CreateBusinessAdminForm, string>>

const MAINLAND_CHINA_PHONE_PATTERN = /^1[3-9]\d{9}$/

export function validateCreateBusinessAdminForm(form: CreateBusinessAdminForm): CreateBusinessAdminFormErrors {
  const errors: CreateBusinessAdminFormErrors = {}
  const normalizedPhone = form.phone.trim()

  if (!normalizedPhone) {
    errors.phone = '请输入手机号'
  } else if (!MAINLAND_CHINA_PHONE_PATTERN.test(normalizedPhone)) {
    errors.phone = '请输入正确的手机号'
  }

  if (!form.name.trim()) {
    errors.name = '请输入姓名'
  }

  if (!form.initialPassword.trim()) {
    errors.initialPassword = '请输入初始密码'
  }

  return errors
}

export function buildCreateBusinessAdminPayload(form: CreateBusinessAdminForm) {
  return {
    phone: form.phone.trim(),
    name: form.name.trim(),
    initialPassword: form.initialPassword.trim(),
  }
}
