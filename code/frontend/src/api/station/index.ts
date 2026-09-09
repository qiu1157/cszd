import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type StationListItem = {
  id: number
  stationCode: string
  stationName: string
  platformName: string | null
  platformStationCode: string | null
  contactName: string | null
  contactPhone: string | null
  stationStatusName: string | null
  updatedAt: string
}

export type StationBasePayload = {
  stationName: string
  platformItemId: number | null
  platformStationCode: string
  address: string
  contactName: string
  contactPhone: string
}

export type StationCreatePayload = StationBasePayload
export type StationUpdatePayload = StationBasePayload

export type StationStatusUpdatePayload = {
  stationStatusItemId: number | null
  remark: string
}

export type StationDetail = {
  id: number
  stationCode: string
  stationName: string
  platformItemId: number | null
  platformName: string | null
  platformStationCode: string | null
  address: string | null
  contactName: string | null
  contactPhone: string | null
  meterAccountNumber: string | null
  stationStatusItemId: number | null
  stationStatusName: string | null
}

export async function getStations(
  stationName?: string,
  platformItemId?: number | null,
  stationStatusItemId?: number | null,
) {
  const response = await request.get<ApiResponse<StationListItem[]>>('/api/stations', {
    params: {
      stationName: stationName?.trim() || undefined,
      platformItemId: platformItemId ?? undefined,
      stationStatusItemId: stationStatusItemId ?? undefined,
    },
  })
  return response.data.data
}

export async function createStation(payload: StationCreatePayload) {
  const response = await request.post<ApiResponse<number>>('/api/stations', payload)
  return response.data.data
}

export async function getStationDetail(id: number) {
  const response = await request.get<ApiResponse<StationDetail>>(`/api/stations/${id}`)
  return response.data.data
}

export async function updateStation(id: number, payload: StationUpdatePayload) {
  const response = await request.put<ApiResponse<number>>(`/api/stations/${id}`, payload)
  return response.data.data
}

export async function updateStationStatus(id: number, payload: StationStatusUpdatePayload) {
  const response = await request.put<ApiResponse<number>>(`/api/stations/${id}/status`, payload)
  return response.data.data
}

export type StationStatistics = {
  totalCount: number
  statusCounts: Array<{
    statusItemId: number
    statusItemCode: string
    statusItemName: string
    count: number
  }>
}

export async function getStationStatistics() {
  const response = await request.get<ApiResponse<StationStatistics>>('/api/stations/statistics')
  return response.data.data
}

export type RecentStation = {
  id: number
  stationCode: string
  stationName: string
  platformName: string | null
  statusName: string | null
  contactName: string | null
  contactPhone: string | null
  createdAt: string
}

export async function getRecentStations() {
  const response = await request.get<ApiResponse<RecentStation[]>>('/api/stations/recent')
  return response.data.data
}
