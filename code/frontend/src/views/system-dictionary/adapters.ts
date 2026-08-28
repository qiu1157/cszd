import type { SystemDictItemListItem, SystemDictTypeListItem } from '@/api/system-dictionary'

export function mapDictTypeToNavItem(item: SystemDictTypeListItem) {
  return {
    typeCode: item.typeCode,
    title: item.typeName,
    statusLabel: item.status === 'ENABLED' ? '启用' : '停用',
    isDisabled: item.status !== 'ENABLED',
  }
}

export function mapDictItemToTableRow(item: SystemDictItemListItem) {
  return {
    code: item.itemCode,
    name: item.itemName,
    sortOrder: item.sortOrder,
    updatedAtText: item.updatedAt.slice(0, 16).replace('T', ' '),
    statusLabel: item.status === 'ENABLED' ? '启用' : '停用',
    statusType: item.status === 'ENABLED' ? 'success' : 'info',
    actionDisabled: true,
  }
}
