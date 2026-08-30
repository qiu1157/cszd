import { describe, expect, it } from 'vitest'

import { mapStationToTableRow } from './adapters'

describe('mapStationToTableRow', () => {
  it('应将运营中站点映射为绿色状态标签', () => {
    expect(
      mapStationToTableRow({
        id: 1001,
        stationCode: 'ST-202608-0018',
        stationName: '星城南服务区超级站',
        platformName: '星络平台',
        platformStationCode: 'XL-4301-889',
        contactName: '张海峰',
        contactPhone: '13800138000',
        stationStatusName: '运营中',
        updatedAt: '2026-08-29T10:26:00',
      }),
    ).toMatchObject({
      stationCode: 'ST-202608-0018',
      stationName: '星城南服务区超级站',
      platformName: '星络平台',
      statusLabel: '运营中',
      statusType: 'success',
      updatedAtText: '2026-08-29 10:26',
    })
  })

  it('应将签合同站点映射为琥珀状态标签，并处理空平台编号', () => {
    expect(
      mapStationToTableRow({
        id: 1002,
        stationCode: 'ST-202608-0001',
        stationName: '雨花测试站',
        platformName: '云快充',
        platformStationCode: null,
        contactName: '李丹',
        contactPhone: '13800138001',
        stationStatusName: '签合同',
        updatedAt: '2026-08-29T09:12:00',
      }),
    ).toMatchObject({
      statusLabel: '签合同',
      statusType: 'warning',
      platformStationCodeText: '--',
    })
  })
})
