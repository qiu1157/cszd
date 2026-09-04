import { describe, expect, it } from 'vitest'

import { mapShareholderToTableRow } from './adapters'

describe('mapShareholderToTableRow', () => {
  it('应将启用股东映射为绿色状态标签，且停用为唯一可用动作', () => {
    expect(
      mapShareholderToTableRow({
        id: 2001,
        shareholderName: '长沙城新投资有限公司',
        contactName: '刘可',
        contactPhone: '13977882211',
        shareholderStatus: 'ENABLED',
        updatedAt: '2026-09-04T09:40:00',
      }),
    ).toMatchObject({
      shareholderName: '长沙城新投资有限公司',
      shareholderSubtitle: '独立主档实体',
      contactText: '刘可 / 13977882211',
      relatedStationCount: 0,
      statusLabel: '启用',
      statusType: 'success',
      canEnable: false,
      canDisable: true,
      enableActionTone: 'secondary',
      disableActionTone: 'primary',
      updatedAtText: '2026-09-04 09:40',
    })
  })

  it('应将停用股东映射为灰色状态标签与启用高亮动作，并处理缺失联系人', () => {
    expect(
      mapShareholderToTableRow({
        id: 2002,
        shareholderName: '湘江产业基金',
        contactName: null,
        contactPhone: null,
        shareholderStatus: 'DISABLED',
        updatedAt: '2026-08-30T15:20:00',
      }),
    ).toMatchObject({
      contactText: '--',
      statusLabel: '停用',
      statusType: 'info',
      canEnable: true,
      canDisable: false,
      enableActionTone: 'primary',
      disableActionTone: 'secondary',
    })
  })
})