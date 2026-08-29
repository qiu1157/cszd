import type { StationListItem } from '@/api/station'

type StationTableRow = {
  stationCode: string
  stationName: string
  platformName: string
  platformStationCodeText: string
  contactName: string
  contactPhone: string
  statusLabel: string
  statusType: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  updatedAtText: string
}

export function mapStationToTableRow(item: StationListItem): StationTableRow {
  return {
    stationCode: item.stationCode,
    stationName: item.stationName,
    platformName: item.platformName?.trim() || '--',
    platformStationCodeText: item.platformStationCode?.trim() || '--',
    contactName: item.contactName?.trim() || '--',
    contactPhone: item.contactPhone?.trim() || '--',
    statusLabel: item.stationStatusName?.trim() || '--',
    statusType: resolveStatusType(item.stationStatusName),
    updatedAtText: formatUpdatedAt(item.updatedAt),
  }
}

function resolveStatusType(statusName: string | null) {
  switch (statusName) {
    case '签合同':
      return 'warning'
    case '建设中':
      return 'primary'
    case '运营中':
      return 'success'
    case '运营异常':
      return 'danger'
    case '停用':
      return 'info'
    default:
      return 'info'
  }
}

function formatUpdatedAt(value: string) {
  if (!value) {
    return '--'
  }

  return value.slice(0, 16).replace('T', ' ')
}
