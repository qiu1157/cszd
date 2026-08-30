import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type StationPlatformOption = {
  itemId: number
  itemCode: string
  itemName: string
}

export async function getStationPlatformOptions() {
  const response = await request.get<ApiResponse<StationPlatformOption[]>>('/api/stations/platform-options')
  return response.data.data
}
