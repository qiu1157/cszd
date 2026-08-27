export type CreateBusinessAdminForm = {
  phone: string
  name: string
  initialPassword: string
}

export type CreateBusinessAdminFormErrors = Partial<Record<keyof CreateBusinessAdminForm, string>>

export function validateCreateBusinessAdminForm(form: CreateBusinessAdminForm): CreateBusinessAdminFormErrors {
  const errors: CreateBusinessAdminFormErrors = {}

  if (!form.phone.trim()) {
    errors.phone = '请输入手机号'
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
