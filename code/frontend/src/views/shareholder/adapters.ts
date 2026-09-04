import type { ShareholderListItem } from '@/api/shareholder'
import { getShareholderStatusLabel, SHAREHOLDER_STATUS } from '@/constants/enums/shareholder-status'

export type ShareholderTableRow = {
  id: number
  shareholderName: string
  shareholderSubtitle: string
  contactText: string
  relatedStationCount: number
  statusLabel: string
  statusType: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  updatedAtText: string
}

export function mapShareholderToTableRow(item: ShareholderListItem): ShareholderTableRow {
  return {
    id: item.id,
    shareholderName: item.shareholderName,
    shareholderSubtitle: '独立主档实体',
    contactText: buildContactText(item.contactName, item.contactPhone),
    // 为什么写死 0：关联站点数依赖里程碑 F 的持股关系表，当前后端尚未返回该字段
    relatedStationCount: 0,
    statusLabel: getShareholderStatusLabel(item.shareholderStatus),
    statusType: resolveStatusType(item.shareholderStatus),
    updatedAtText: formatUpdatedAt(item.updatedAt),
  }
}

function buildContactText(name: string | null, phone: string | null) {
  const parts = [name?.trim() || '', phone?.trim() || ''].filter(Boolean)
  return parts.length > 0 ? parts.join(' / ') : '--'
}

function resolveStatusType(status: string) {
  return status === SHAREHOLDER_STATUS.ENABLED ? 'success' : 'info'
}

function formatUpdatedAt(value: string) {
  if (!value) {
    return '--'
  }

  return value.slice(0, 16).replace('T', ' ')
}