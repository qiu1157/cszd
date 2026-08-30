import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type StationListItem = {
  stationCode: string
  stationName: string
  platformName: string | null
  platformStationCode: string | null
  contactName: string | null
  contactPhone: string | null
  stationStatusName: string | null
  updatedAt: string
}

export type StationCreatePayload = {
  stationName: string
  platformItemId: number | null
  platformStationCode: string
  address: string
  contactName: string
  contactPhone: string
}

export async function getStations(stationName?: string, platformItemId?: number | null) {
  const response = await request.get<ApiResponse<StationListItem[]>>('/api/stations', {
    params: {
      stationName: stationName?.trim() || undefined,
      platformItemId: platformItemId ?? undefined,
    },
  })
  return response.data.data
}

export async function createStation(payload: StationCreatePayload) {
  const response = await request.post<ApiResponse<number>>('/api/stations', payload)
  return response.data.data
}
