import { USER_ROLES, type UserRole } from '@/constants/enums/user-role'

export const DASHBOARD_PATH = '/dashboard'
export const STATION_LIST_PATH = '/stations'
export const STATION_CREATE_PATH = '/stations/create'
export const STATION_EDIT_PATH = '/stations/:id/edit'
export const SYSTEM_DICTIONARY_PATH = '/system-dictionaries'
export const LEGACY_PLATFORMS_PATH = '/platforms'

export function buildStationEditPath(id: number | string) {
  return `/stations/${id}/edit`
}

export function getDefaultRouteByRole(role: UserRole) {
  return role === USER_ROLES.SYSTEM_ADMIN ? DASHBOARD_PATH : STATION_LIST_PATH
}

export function getForbiddenFallbackRouteByRole(role: UserRole) {
  return role === USER_ROLES.SYSTEM_ADMIN ? DASHBOARD_PATH : STATION_LIST_PATH
}
