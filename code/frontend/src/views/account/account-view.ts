import { getUserRoleLabel } from '@/constants/enums/user-role'
import type { AuthUser } from '@/utils/auth-storage'

export type AccountSection = {
  title: string
  items: Array<{
    label: string
    value: string
  }>
}

function formatLastLoginAt(value: string | null) {
  if (!value) {
    return '未登录'
  }

  return value.slice(0, 16).replace('T', ' ')
}

function getUserStatusLabel(status: AuthUser['status']) {
  return status === 'ENABLED' ? '启用' : '停用'
}

export function buildAccountSections(user: AuthUser): AccountSection[] {
  return [
    {
      title: '账号概览',
      items: [
        { label: '姓名', value: user.name },
        { label: '登录账号', value: user.account },
        { label: '手机号', value: user.phone ?? '未设置' },
        { label: '角色', value: getUserRoleLabel(user.role) },
        { label: '账号状态', value: getUserStatusLabel(user.status) },
      ],
    },
    {
      title: '登录信息',
      items: [{ label: '最后登录时间', value: formatLastLoginAt(user.lastLoginAt) }],
    },
    {
      title: '安全设置',
      items: [{ label: '修改本人密码', value: '下一任务实现' }],
    },
  ]
}
