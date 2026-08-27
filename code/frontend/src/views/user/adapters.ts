import type { BusinessAdminListItem } from '@/api/user'

type UserTableRow = {
  account: string
  subtitle: string
  roleLabel: string
  statusLabel: string
  statusType: 'success' | 'info'
  canEnable: boolean
  canDisable: boolean
  lastLoginText: string
}

export function mapBusinessAdminToTableRow(item: BusinessAdminListItem): UserTableRow {
  const isEnabled = item.status === 'ENABLED'

  return {
    account: item.account,
    subtitle: `业务管理员 · ${item.name}`,
    roleLabel: '业务管理员',
    statusLabel: isEnabled ? '启用' : '停用',
    statusType: isEnabled ? 'success' : 'info',
    canEnable: !isEnabled,
    canDisable: isEnabled,
    lastLoginText: formatLastLoginAt(item.lastLoginAt),
  }
}

function formatLastLoginAt(value: string | null) {
  if (!value) {
    return '未登录'
  }

  return value.slice(0, 16).replace('T', ' ')
}
