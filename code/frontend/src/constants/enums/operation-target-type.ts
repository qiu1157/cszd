export const OPERATION_TARGET_TYPES = {
  STATION: 'STATION',
  PILE: 'PILE',
  SHAREHOLDER: 'SHAREHOLDER',
  SHAREHOLDING: 'SHAREHOLDING',
  SYSTEM_DICT: 'SYSTEM_DICT',
  USER: 'USER',
} as const

export type OperationTargetType = (typeof OPERATION_TARGET_TYPES)[keyof typeof OPERATION_TARGET_TYPES]

export function getOperationTargetTypeLabel(type: string) {
  switch (type) {
    case OPERATION_TARGET_TYPES.STATION:
      return '充电站'
    case OPERATION_TARGET_TYPES.PILE:
      return '充电桩'
    case OPERATION_TARGET_TYPES.SHAREHOLDER:
      return '股东'
    case OPERATION_TARGET_TYPES.SHAREHOLDING:
      return '持股关系'
    case OPERATION_TARGET_TYPES.SYSTEM_DICT:
      return '系统字典'
    case OPERATION_TARGET_TYPES.USER:
      return '用户'
    default:
      return type
  }
}

export const OPERATION_TARGET_TYPE_OPTIONS = [
  { value: OPERATION_TARGET_TYPES.STATION, label: '充电站' },
  { value: OPERATION_TARGET_TYPES.PILE, label: '充电桩' },
  { value: OPERATION_TARGET_TYPES.SHAREHOLDER, label: '股东' },
  { value: OPERATION_TARGET_TYPES.SHAREHOLDING, label: '持股关系' },
  { value: OPERATION_TARGET_TYPES.SYSTEM_DICT, label: '系统字典' },
  { value: OPERATION_TARGET_TYPES.USER, label: '用户' },
]
