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