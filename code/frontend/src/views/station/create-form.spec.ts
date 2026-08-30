import { describe, expect, it } from 'vitest'

import { createInitialStationCreateForm, toCreateStationPayload } from './create-form'

describe('station create form helpers', () => {
  it('应生成初始表单并在提交前去除首尾空格', () => {
    const form = createInitialStationCreateForm()
    form.stationName = '  雨花城际快充港  '
    form.address = '  长沙市雨花区城际大道南段  '

    expect(toCreateStationPayload(form)).toEqual({
      stationName: '雨花城际快充港',
      platformItemId: null,
      platformStationCode: '',
      address: '长沙市雨花区城际大道南段',
      contactName: '',
      contactPhone: '',
    })
  })
})
