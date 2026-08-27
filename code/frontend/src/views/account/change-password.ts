export type ChangePasswordForm = {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export type ChangePasswordFormErrors = Partial<Record<keyof ChangePasswordForm, string>>

const CHANGE_PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)\S{6,20}$/

export function validateChangePasswordForm(
  form: ChangePasswordForm,
): ChangePasswordFormErrors {
  const errors: ChangePasswordFormErrors = {}
  const currentPassword = form.currentPassword.trim()
  const newPassword = form.newPassword.trim()
  const confirmPassword = form.confirmPassword.trim()

  if (!currentPassword) {
    errors.currentPassword = '请输入旧密码'
  }

  if (!newPassword) {
    errors.newPassword = '请输入新密码'
  } else if (!CHANGE_PASSWORD_PATTERN.test(newPassword)) {
    errors.newPassword = '请输入6-20位且包含字母和数字的新密码'
  } else if (currentPassword && newPassword === currentPassword) {
    errors.newPassword = '新密码不能与旧密码相同'
  }

  if (!confirmPassword) {
    errors.confirmPassword = '请输入确认新密码'
  } else if (newPassword && confirmPassword !== newPassword) {
    errors.confirmPassword = '确认密码与新密码不一致'
  }

  return errors
}

export function buildChangePasswordPayload(form: ChangePasswordForm) {
  return {
    currentPassword: form.currentPassword.trim(),
    newPassword: form.newPassword.trim(),
    confirmPassword: form.confirmPassword.trim(),
  }
}
