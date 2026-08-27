import type { UserRole } from '@/constants/enums/user-role'

export type AuthUserStatus = 'ENABLED' | 'DISABLED'

export type AuthUser = {
  account: string
  name: string
  phone: string | null
  role: UserRole
  status: AuthUserStatus
  lastLoginAt: string | null
}

export type AuthSession = {
  token: string
  user: AuthUser
}

const AUTH_STORAGE_KEY = 'cszd-auth-session'

export function readAuthSession() {
  const rawValue = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!rawValue) {
    return null
  }

  try {
    return JSON.parse(rawValue) as AuthSession
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

export function writeAuthSession(session: AuthSession) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}
