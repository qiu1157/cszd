export type AppMenuItem = {
  path: string
  title: string
}

// 为什么：第一期先落地与原型一致的菜单结构，后续再叠加角色权限过滤（系统管理员/业务管理员）
export const appMenuItems: AppMenuItem[] = [
  { path: '/dashboard', title: '首页' },
  { path: '/stations', title: '充电站管理' },
  { path: '/shareholders', title: '股东管理' },
  { path: '/platforms', title: '平台管理' },
  { path: '/logs', title: '操作日志' },
  { path: '/users', title: '用户管理' },
  { path: '/account', title: '我的账号' },
]

