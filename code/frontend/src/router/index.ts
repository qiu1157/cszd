import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { USER_ROLES, type UserRole } from '@/constants/enums/user-role'
import AppLayout from '@/layout/AppLayout.vue'
import pinia from '@/stores'
import { useAuthStore } from '@/stores/auth'
import {
  getDefaultRouteByRole,
  getForbiddenFallbackRouteByRole,
  LEGACY_PLATFORMS_PATH,
  STATION_CREATE_PATH,
  STATION_DETAIL_PATH,
  STATION_EDIT_PATH,
  SYSTEM_DICTIONARY_PATH,
} from './navigation'

type AppRouteMeta = {
  title: string
  requiresAuth?: boolean
  publicOnly?: boolean
  roles?: UserRole[]
}

const routes: Array<RouteRecordRaw & { meta: AppRouteMeta }> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', publicOnly: true },
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    meta: { title: '工作台', requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '首页', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: '/stations',
        name: 'StationList',
        component: () => import('@/views/station/StationListView.vue'),
        meta: { title: '充电站管理', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: STATION_CREATE_PATH,
        name: 'StationCreate',
        component: () => import('@/views/station/StationCreateView.vue'),
        meta: { title: '新增充电站', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: STATION_DETAIL_PATH,
        name: 'StationDetail',
        component: () => import('@/views/station/StationDetailView.vue'),
        meta: { title: '充电站详情', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: STATION_EDIT_PATH,
        name: 'StationEdit',
        component: () => import('@/views/station/StationEditView.vue'),
        meta: { title: '编辑充电站', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: '/shareholders',
        name: 'ShareholderList',
        component: () => import('@/views/shareholder/ShareholderListView.vue'),
        meta: { title: '股东管理', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: '/system-dictionaries',
        name: 'SystemDictionaryList',
        component: () => import('@/views/system-dictionary/SystemDictionaryListView.vue'),
        meta: { title: '系统字典', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN] },
      },
      {
        path: LEGACY_PLATFORMS_PATH,
        redirect: SYSTEM_DICTIONARY_PATH,
        meta: { title: '系统字典', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN] },
      },
      {
        path: '/logs',
        name: 'Logs',
        component: () => import('@/views/logs/LogsView.vue'),
        meta: { title: '操作日志', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
      {
        path: '/users',
        name: 'UserList',
        component: () => import('@/views/user/UserListView.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN] },
      },
      {
        path: '/account',
        name: 'MyAccount',
        component: () => import('@/views/account/AccountView.vue'),
        meta: { title: '我的账号', requiresAuth: true, roles: [USER_ROLES.SYSTEM_ADMIN, USER_ROLES.BUSINESS_ADMIN] },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)
  authStore.hydrateFromStorage()

  const targetMeta = to.meta as AppRouteMeta

  if (targetMeta.publicOnly && authStore.isAuthenticated) {
    return authStore.user ? getDefaultRouteByRole(authStore.user.role) : true
  }

  if (targetMeta.requiresAuth && !authStore.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (targetMeta.requiresAuth && authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.refreshCurrentUser()
    } catch {
      authStore.signOut()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  if (targetMeta.roles?.length && authStore.user && !targetMeta.roles.includes(authStore.user.role)) {
    return getForbiddenFallbackRouteByRole(authStore.user.role)
  }

  return true
})

export default router
