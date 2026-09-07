import { USER_ROLES, type UserRole } from '@/constants/enums/user-role'

export const DASHBOARD_PATH = '/dashboard'
export const STATION_LIST_PATH = '/stations'
export const STATION_CREATE_PATH = '/stations/create'
export const STATION_DETAIL_PATH = '/stations/:id'
export const STATION_EDIT_PATH = '/stations/:id/edit'
export const STATION_EQUITY_ADJUSTMENT_PATH = '/stations/:id/equity-adjustment'
export const SHAREHOLDER_LIST_PATH = '/shareholders'
export const SHAREHOLDER_DETAIL_PATH = '/shareholders/:id'
export const SYSTEM_DICTIONARY_PATH = '/system-dictionaries'
export const LEGACY_PLATFORMS_PATH = '/platforms'

export function buildStationDetailPath(id: number | string) {
  return `/stations/${id}`
}

export function buildStationEditPath(id: number | string) {
  return `/stations/${id}/edit`
}

export function buildStationEquityAdjustmentPath(id: number | string) {
  return `/stations/${id}/equity-adjustment`
}

export function buildShareholderDetailPath(id: number | string) {
  return `/shareholders/${id}`
}

export function getDefaultRouteByRole(role: UserRole) {
  return role === USER_ROLES.SYSTEM_ADMIN ? DASHBOARD_PATH : STATION_LIST_PATH
}

export function getForbiddenFallbackRouteByRole(role: UserRole) {
  return role === USER_ROLES.SYSTEM_ADMIN ? DASHBOARD_PATH : STATION_LIST_PATH
}
