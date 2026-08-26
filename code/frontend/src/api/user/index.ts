import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type BusinessAdminListItem = {
  account: string
  name: string
  role: 'BUSINESS_ADMIN'
  status: 'ENABLED' | 'DISABLED'
  lastLoginAt: string | null
}

export async function getBusinessAdmins() {
  const response = await request.get<ApiResponse<BusinessAdminListItem[]>>('/api/users/business-admins')
  return response.data.data
}
