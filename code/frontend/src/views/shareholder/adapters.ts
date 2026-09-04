import type { ShareholderListItem } from '@/api/shareholder'

export type ShareholderTableRow = {
  id: number
  shareholderName: string
  shareholderSubtitle: string
  contactText: string
  relatedStationCount: number
  statusLabel: string
  statusType: 'success' | 'info'
  canEnable: boolean
  canDisable: boolean
  enableActionTone: 'primary' | 'secondary'
  disableActionTone: 'primary' | 'secondary'
  updatedAtText: string
}

export function mapShareholderToTableRow(item: ShareholderListItem): ShareholderTableRow {
  const isEnabled = item.shareholderStatus === 'ENABLED'

  return {
    id: item.id,
    shareholderName: item.shareholderName,
    shareholderSubtitle: '独立主档实体',
    contactText: item.contactName || item.contactPhone
      ? `${item.contactName ?? '--'} / ${item.contactPhone ?? '--'}`
      : '--',
    relatedStationCount: item.stationCount,
    statusLabel: isEnabled ? '启用' : '停用',
    statusType: isEnabled ? 'success' : 'info',
    // 启用/停用互斥：仅当前状态的对立动作可用并高亮，与字典项状态切换交互保持一致
    canEnable: !isEnabled,
    canDisable: isEnabled,
    enableActionTone: isEnabled ? 'secondary' : 'primary',
    disableActionTone: isEnabled ? 'primary' : 'secondary',
    updatedAtText: item.updatedAt.slice(0, 16).replace('T', ' '),
  }
}