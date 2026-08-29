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

export async function getStations() {
  const response = await request.get<ApiResponse<StationListItem[]>>('/api/stations')
  return response.data.data
}
