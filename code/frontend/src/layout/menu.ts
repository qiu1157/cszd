import { USER_ROLES, type UserRole } from '@/constants/enums/user-role'

export type AppMenuItem = {
  path: string
  title: string
  roles: UserRole[]
}

const BOTH_ROLES = [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] as UserRole[]

export const appMenuItems: AppMenuItem[] = [
  { path: '/dashboard', title: '首页', roles: BOTH_ROLES },
  { path: '/stations', title: '充电站管理', roles: BOTH_ROLES },
  { path: '/shareholders', title: '股东管理', roles: BOTH_ROLES },
  { path: '/system-dictionaries', title: '系统字典', roles: [USER_ROLES.SYSTEM_ADMIN] },
  { path: '/logs', title: '操作日志', roles: BOTH_ROLES },
  { path: '/users', title: '用户管理', roles: [USER_ROLES.SYSTEM_ADMIN] },
  { path: '/account', title: '我的账号', roles: BOTH_ROLES },
]

export function getMenuItemsByRole(role: UserRole) {
  return appMenuItems.filter((item) => item.roles.includes(role))
}
