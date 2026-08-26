import type { BusinessAdminListItem } from '@/api/user'

type UserTableRow = {
  account: string
  subtitle: string
  roleLabel: string
  statusLabel: string
  statusType: 'success' | 'info'
  lastLoginText: string
}

export function mapBusinessAdminToTableRow(item: BusinessAdminListItem): UserTableRow {
  return {
    account: item.account,
    subtitle: `业务管理员 · ${item.name}`,
    roleLabel: '业务管理员',
    statusLabel: item.status === 'ENABLED' ? '启用' : '停用',
    statusType: item.status === 'ENABLED' ? 'success' : 'info',
    lastLoginText: formatLastLoginAt(item.lastLoginAt),
  }
}

function formatLastLoginAt(value: string | null) {
  if (!value) {
    return '未登录'
  }

  return value.slice(0, 16).replace('T', ' ')
}
