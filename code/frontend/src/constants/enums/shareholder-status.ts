export const SHAREHOLDER_STATUS = {
  ENABLED: 'ENABLED',
  DISABLED: 'DISABLED',
} as const

export type ShareholderStatus = (typeof SHAREHOLDER_STATUS)[keyof typeof SHAREHOLDER_STATUS]

export function getShareholderStatusLabel(status: string) {
  return status === SHAREHOLDER_STATUS.DISABLED ? '停用' : '启用'
}

export const SHAREHOLDER_STATUS_OPTIONS = [
  { value: SHAREHOLDER_STATUS.ENABLED, label: '启用' },
  { value: SHAREHOLDER_STATUS.DISABLED, label: '停用' },
]