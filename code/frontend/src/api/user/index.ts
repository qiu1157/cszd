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

export type CreateBusinessAdminPayload = {
  phone: string
  name: string
  initialPassword: string
}

export type ResetBusinessAdminPasswordPayload = {
  newPassword: string
}

export type UpdateBusinessAdminStatusAction = 'enable' | 'disable'

export async function getBusinessAdmins() {
  const response = await request.get<ApiResponse<BusinessAdminListItem[]>>('/api/users/business-admins')
  return response.data.data
}

export async function createBusinessAdmin(payload: CreateBusinessAdminPayload) {
  const response = await request.post<ApiResponse<null>>('/api/users/business-admins', payload)
  return response.data
}

export async function updateBusinessAdminStatus(account: string, action: UpdateBusinessAdminStatusAction) {
  const response = await request.post<ApiResponse<null>>(`/api/users/business-admins/${account}/${action}`)
  return response.data
}

export async function resetBusinessAdminPassword(account: string, payload: ResetBusinessAdminPasswordPayload) {
  const response = await request.post<ApiResponse<null>>(`/api/users/business-admins/${account}/reset-password`, payload)
  return response.data
}
