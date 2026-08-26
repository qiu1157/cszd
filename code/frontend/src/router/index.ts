import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import AppLayout from '@/layout/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '首页' } },
      { path: '/stations', name: 'StationList', component: () => import('@/views/station/StationListView.vue'), meta: { title: '充电站管理' } },
      { path: '/shareholders', name: 'ShareholderList', component: () => import('@/views/shareholder/ShareholderListView.vue'), meta: { title: '股东管理' } },
      { path: '/platforms', name: 'PlatformList', component: () => import('@/views/platform/PlatformListView.vue'), meta: { title: '平台管理' } },
      { path: '/logs', name: 'Logs', component: () => import('@/views/logs/LogsView.vue'), meta: { title: '操作日志' } },
      { path: '/users', name: 'UserList', component: () => import('@/views/user/UserListView.vue'), meta: { title: '用户管理' } },
      { path: '/account', name: 'MyAccount', component: () => import('@/views/account/AccountView.vue'), meta: { title: '我的账号' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router

