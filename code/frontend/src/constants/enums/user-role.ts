export const USER_ROLES = {
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  BUSINESS_ADMIN: 'BUSINESS_ADMIN',
} as const

export type UserRole = (typeof USER_ROLES)[keyof typeof USER_ROLES]

export function getUserRoleLabel(role: UserRole) {
  return role === USER_ROLES.SYSTEM_ADMIN ? '系统管理员' : '业务管理员'
}
