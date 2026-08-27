import type { AuthUser } from '@/utils/auth-storage'
import request from '@/utils/request'

export type LoginPayload = {
  account: string
  password: string
}

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

type LoginResponse = {
  token: string
  user: AuthUser
}

export type ChangePasswordPayload = {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export async function login(payload: LoginPayload) {
  const response = await request.post<ApiResponse<LoginResponse>>('/api/auth/login', payload)
  return response.data.data
}

export async function getCurrentUser() {
  const response = await request.get<ApiResponse<AuthUser>>('/api/auth/me')
  return response.data.data
}

export async function changePassword(payload: ChangePasswordPayload) {
  const response = await request.post<ApiResponse<null>>('/api/auth/change-password', payload)
  return response.data
}
