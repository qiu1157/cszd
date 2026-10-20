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

export async function getStationShareholdings(
  stationId: number,
  holdingStatus?: 'ACTIVE' | 'EXITED',
) {
  const response = await request.get<ApiResponse<StationShareholding[]>>(
    `/api/stations/${stationId}/shareholdings`,
    {
      params: holdingStatus ? { holdingStatus } : undefined,
    },
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

export type EquityAdjustmentItem = {
  shareholdingId: number
  ownershipPercentage: number
}

// 股权调整是批量更新站点下持股中关系的占比（spec 8.3）
export async function adjustStationShareholdings(stationId: number, items: EquityAdjustmentItem[]) {
  const response = await request.put<ApiResponse<number>>(
    `/api/stations/${stationId}/shareholdings/equity-adjustment`,
    { items },
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

export type ShareholdingImportError = {
  rowNumber: number
  message: string
}

export type ShareholdingImportResult = {
  totalCount: number
  successCount: number
  errors: ShareholdingImportError[]
}

export async function importShareholdings(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post<ApiResponse<ShareholdingImportResult>>(
    '/api/stations/shareholding-import',
    formData,
  )
  return response.data.data
}

// 模板下载：后端返回 .xlsx 二进制流，前端用临时链接触发浏览器下载
export async function downloadShareholdingImportTemplate() {
  const response = await request.get<Blob>('/api/stations/shareholding-import-template', {
    responseType: 'blob',
  })
  const blob = response.data
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '股东关系导入模板.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}