export type ResetBusinessAdminForm = {
  newPassword: string
}

export type ResetBusinessAdminFormErrors = Partial<Record<keyof ResetBusinessAdminForm, string>>

const RESET_PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)\S{6,20}$/

export function validateResetBusinessAdminForm(form: ResetBusinessAdminForm): ResetBusinessAdminFormErrors {
  const errors: ResetBusinessAdminFormErrors = {}
  const normalizedPassword = form.newPassword.trim()

  if (!normalizedPassword) {
    errors.newPassword = '请输入新密码'
  } else if (!RESET_PASSWORD_PATTERN.test(normalizedPassword)) {
    errors.newPassword = '请输入6-20位且包含字母和数字的新密码'
  }

  return errors
}

export function buildResetBusinessAdminPayload(form: ResetBusinessAdminForm) {
  return {
    newPassword: form.newPassword.trim(),
  }
}
