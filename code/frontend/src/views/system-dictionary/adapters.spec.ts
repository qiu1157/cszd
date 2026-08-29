import { describe, expect, it } from 'vitest'

import { mapDictItemToTableRow, mapDictTypeToNavItem } from './adapters'

describe('system dictionary adapters', () => {
  it('应将平台类型映射为默认首屏导航项', () => {
    expect(
      mapDictTypeToNavItem({
        typeCode: 'PLATFORM',
        typeName: '平台',
        status: 'ENABLED',
        sortOrder: 10,
      }),
    ).toEqual({
      typeCode: 'PLATFORM',
      title: '平台',
      statusLabel: '启用',
      isDisabled: false,
    })
  })

  it('应将停用字典项映射为禁用态展示', () => {
    expect(
      mapDictItemToTableRow({
        id: 5,
        itemCode: 'DISABLED',
        itemName: '停用',
        status: 'DISABLED',
        sortOrder: 50,
        updatedAt: '2026-08-27T20:30:00',
      }),
    ).toMatchObject({
      id: 5,
      code: 'DISABLED',
      name: '停用',
      statusLabel: '停用',
      statusType: 'info',
      canEdit: true,
      canEnable: true,
      canDisable: false,
      enableActionTone: 'primary',
      disableActionTone: 'muted',
    })
  })

  it('应将启用字典项映射为停用动作高亮展示', () => {
    expect(
      mapDictItemToTableRow({
        id: 9,
        itemCode: 'STATE_GRID',
        itemName: '国网',
        status: 'ENABLED',
        sortOrder: 10,
        updatedAt: '2026-08-29T10:00:00',
      }),
    ).toMatchObject({
      id: 9,
      code: 'STATE_GRID',
      name: '国网',
      statusLabel: '启用',
      statusType: 'success',
      canEdit: true,
      canEnable: false,
      canDisable: true,
      enableActionTone: 'muted',
      disableActionTone: 'primary',
    })
  })
})
