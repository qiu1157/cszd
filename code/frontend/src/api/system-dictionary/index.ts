import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type SystemDictTypeListItem = {
  typeCode: 'PLATFORM' | 'STATION_STATUS' | 'PILE_STATUS'
  typeName: string
  status: 'ENABLED' | 'DISABLED'
  sortOrder: number
}

export type SystemDictItemListItem = {
  itemCode: string
  itemName: string
  status: 'ENABLED' | 'DISABLED'
  sortOrder: number
  updatedAt: string
}

export async function getSystemDictionaryTypes() {
  const response = await request.get<ApiResponse<SystemDictTypeListItem[]>>('/api/system-dictionaries/types')
  return response.data.data
}

export async function getSystemDictionaryItems(typeCode: string) {
  const response = await request.get<ApiResponse<SystemDictItemListItem[]>>(`/api/system-dictionaries/${typeCode}/items`)
  return response.data.data
}
