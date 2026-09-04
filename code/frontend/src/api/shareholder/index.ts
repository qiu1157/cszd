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