export const OPERATION_TYPES = {
  CREATE: 'CREATE',
  UPDATE: 'UPDATE',
  STATUS_CHANGE: 'STATUS_CHANGE',
  ENABLE: 'ENABLE',
  DISABLE: 'DISABLE',
  RESET_PASSWORD: 'RESET_PASSWORD',
  EQUITY_ADJUST: 'EQUITY_ADJUST',
  EXIT: 'EXIT',
} as const

export type OperationType = (typeof OPERATION_TYPES)[keyof typeof OPERATION_TYPES]

export function getOperationTypeLabel(type: string) {
  switch (type) {
    case OPERATION_TYPES.CREATE:
      return '新增'
    case OPERATION_TYPES.UPDATE:
      return '修改'
    case OPERATION_TYPES.STATUS_CHANGE:
      return '状态变更'
    case OPERATION_TYPES.ENABLE:
      return '启用'
    case OPERATION_TYPES.DISABLE:
      return '停用'
    case OPERATION_TYPES.RESET_PASSWORD:
      return '重置密码'
    case OPERATION_TYPES.EQUITY_ADJUST:
      return '股权调整'
    case OPERATION_TYPES.EXIT:
      return '退出'
    default:
      return type
  }
}
