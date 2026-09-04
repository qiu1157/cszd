import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type StationShareholding = {
  id: number
  shareholderId: number
  shareholderName: string
  contactName: string | null
  contactPhone: string | null
  ownershipPercentage: number
  holdingStatusName: string
  effectiveAt: string | null
}

export async function getStationShareholdings(stationId: number) {
  const response = await request.get<ApiResponse<StationShareholding[]>>(
    `/api/stations/${stationId}/shareholdings`,
  )
  return response.data.data
}

export type ShareholdingCreatePayload = {
  shareholderId: number
  ownershipPercentage: number
  holdingStatus: 'ACTIVE' | 'EXITED'
}

export async function createStationShareholding(stationId: number, payload: ShareholdingCreatePayload) {
  const response = await request.post<ApiResponse<number>>(
    `/api/stations/${stationId}/shareholdings`,
    payload,
  )
  return response.data.data
}

// 站点详情概览卡摘要：只统计持股中关系的股东数与占比合计，
// 已退出关系是历史痕迹，不参与当前股权结构口径
export function summarizeShareholdings(shareholdings: StationShareholding[]) {
  const activeRelations = shareholdings.filter(item => item.holdingStatusName === '持股中')
  const activePercentageSum = activeRelations.reduce(
    (total, item) => total + (item.ownershipPercentage || 0),
    0,
  )
  return {
    activeShareholderCount: activeRelations.length,
    activePercentageSum,
  }
}