import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type ShareholderListItem = {
  id: number
  shareholderName: string
  contactName: string | null
  contactPhone: string | null
  shareholderStatus: string
  updatedAt: string
  stationCount: number
}

export async function getShareholders(shareholderName?: string, shareholderStatus?: string | null) {
  const response = await request.get<ApiResponse<ShareholderListItem[]>>('/api/shareholders', {
    params: {
      shareholderName: shareholderName?.trim() || undefined,
      shareholderStatus: shareholderStatus || undefined,
    },
  })
  return response.data.data
}

export type ShareholderDetail = {
  id: number
  shareholderName: string
  contactName: string | null
  contactPhone: string | null
  shareholderStatus: string
  createdAt: string
  updatedAt: string
}

export async function getShareholderDetail(shareholderId: number) {
  const response = await request.get<ApiResponse<ShareholderDetail>>(`/api/shareholders/${shareholderId}`)
  return response.data.data
}

export type ShareholderCreatePayload = {
  shareholderName: string
  contactName: string
  contactPhone: string
  shareholderStatus: string
}

export async function createShareholder(payload: ShareholderCreatePayload) {
  const response = await request.post<ApiResponse<number>>('/api/shareholders', payload)
  return response.data.data
}

export type ShareholderUpdatePayload = {
  shareholderName: string
  contactName: string
  contactPhone: string
}

export async function updateShareholder(shareholderId: number, payload: ShareholderUpdatePayload) {
  const response = await request.put<ApiResponse<number>>(`/api/shareholders/${shareholderId}`, payload)
  return response.data.data
}

export type ShareholderStatusAction = 'enable' | 'disable'

export async function updateShareholderStatus(shareholderId: number, action: ShareholderStatusAction) {
  const response = await request.post<ApiResponse<null>>(`/api/shareholders/${shareholderId}/${action}`)
  return response.data.data
}

export type ShareholderStationListItem = {
  stationId: number
  stationCode: string
  stationName: string
  stationStatusName: string | null
  ownershipPercentage: number
  effectiveAt: string | null
}

export async function getShareholderStations(shareholderId: number) {
  const response = await request.get<ApiResponse<ShareholderStationListItem[]>>(
    `/api/shareholders/${shareholderId}/stations`,
  )
  return response.data.data
}

export type ShareholderLinkOption = {
  id: number
  shareholderName: string
  contactName: string | null
  contactPhone: string | null
}

export type ShareholderStatistics = {
  totalCount: number
  enabledCount: number
  disabledCount: number
}

export async function getShareholderLinkOptions() {
  const response = await request.get<ApiResponse<ShareholderLinkOption[]>>('/api/shareholders/link-options')
  return response.data.data
}

export async function getShareholderStatistics() {
  const response = await request.get<ApiResponse<ShareholderStatistics>>('/api/shareholders/statistics')
  return response.data.data
}

export type ShareholderImportError = {
  rowNumber: number
  message: string
}

export type ShareholderImportResult = {
  totalCount: number
  successCount: number
  errors: ShareholderImportError[]
}

export async function importShareholders(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post<ApiResponse<ShareholderImportResult>>('/api/shareholders/import', formData)
  return response.data.data
}

// 模板下载：后端返回 .xlsx 二进制流，前端用临时链接触发浏览器下载
export async function downloadShareholderImportTemplate() {
  const response = await request.get<Blob>('/api/shareholders/import-template', {
    responseType: 'blob',
  })
  const blob = response.data
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '股东导入模板.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
