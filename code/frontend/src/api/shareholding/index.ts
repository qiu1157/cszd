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
  // 状态码是逻辑判断的唯一依据；holdingStatusName 仅为展示文案，不参与任何口径计算
  holdingStatus: 'ACTIVE' | 'EXITED'
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

// 退股是状态流转（spec 8.2）：服务端将关系置为已退出并刷新生效时间，前端不做本地状态改写
export async function exitStationShareholding(stationId: number, shareholdingId: number) {
  const response = await request.put<ApiResponse<number>>(
    `/api/stations/${stationId}/shareholdings/${shareholdingId}/exit`,
  )
  return response.data.data
}

// 站点详情概览卡摘要：只统计持股中关系的股东数与占比合计，
// 已退出关系是历史痕迹，不参与当前股权结构口径；
// 过滤依据为状态码而非中文状态名，避免展示文案变化导致口径静默失真
export function summarizeShareholdings(shareholdings: StationShareholding[]) {
  const activeRelations = shareholdings.filter(item => item.holdingStatus === 'ACTIVE')
  // 占比域精度为 2 位小数（后端 DECIMAL(5,2)）：JS 浮点直加会产生尾差，
  // 若不按域精度取整会把 33.33+33.33+33.34 误判为“合计≠100%”
  const activePercentageSum = Number(
    activeRelations.reduce(
      (total, item) => total + (item.ownershipPercentage || 0),
      0,
    ).toFixed(2),
  )
  return {
    activeShareholderCount: activeRelations.length,
    activePercentageSum,
  }
}