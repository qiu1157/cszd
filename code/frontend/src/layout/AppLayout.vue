<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="280px">
      <div class="brand">
        <div class="brand-badge">Energy Ops Console</div>
        <div class="brand-title">长沙众电 · 能源运营中控台</div>
        <div class="brand-sub">站点、股权、平台与审计统一后台</div>
      </div>

      <el-menu class="menu" router :default-active="activePath">
        <el-menu-item v-for="item in visibleMenuItems" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header" height="72px">
        <div class="header-left">
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-desc">里程碑 A 已接入登录态、鉴权拦截与角色菜单控制。</div>
        </div>

        <div v-if="authStore.user" class="header-right">
          <div class="header-chip">当前角色：{{ roleLabel }}</div>
          <div class="header-chip">当前账号：{{ authStore.user.account }}</div>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView, useRouter } from 'vue-router'

import { getUserRoleLabel } from '@/constants/enums/user-role'
import { useAuthStore } from '@/stores/auth'

import { getMenuItemsByRole } from './menu'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activePath = computed(() => route.path)
const pageTitle = computed(() => (route.meta?.title as string) || '页面')
const visibleMenuItems = computed(() => {
  if (!authStore.user) {
    return []
  }
  return getMenuItemsByRole(authStore.user.role)
})
const roleLabel = computed(() => {
  if (!authStore.user) {
    return ''
  }
  return getUserRoleLabel(authStore.user.role)
})

function handleLogout() {
  authStore.signOut()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.app-shell {
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
}

.app-sidebar {
  border-right: 1px solid var(--line-soft);
  background:
    linear-gradient(180deg, rgba(18, 38, 58, 0.96), rgba(7, 18, 29, 0.96)),
    var(--bg-panel-strong);
  padding: 20px 14px;
}

.brand {
  padding: 8px 12px 20px;
}

.brand-badge {
  display: inline-flex;
  margin-bottom: 12px;
  border: 1px solid rgba(57, 200, 255, 0.2);
  border-radius: 999px;
  padding: 6px 10px;
  color: var(--brand-primary);
  font-size: 12px;
  background: rgba(57, 200, 255, 0.08);
}

.brand-title {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
}

.brand-sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.menu {
  border-right: none;
  background: transparent;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid var(--line-soft);
  background: rgba(8, 18, 28, 0.84);
  backdrop-filter: blur(14px);
}

.header-left {
  display: grid;
  gap: 6px;
}

.header-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.header-chip {
  border: 1px solid rgba(57, 200, 255, 0.12);
  border-radius: 999px;
  padding: 6px 10px;
  color: var(--text-secondary);
  font-size: 12px;
  background: rgba(57, 200, 255, 0.06);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
}

.page-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.app-main {
  padding: 24px;
}

@media (max-width: 960px) {
  .app-sidebar {
    display: none;
  }

  .app-header {
    height: auto;
    padding: 16px;
    align-items: flex-start;
  }

  .header-right {
    justify-content: flex-start;
  }

  .app-main {
    padding: 16px;
  }
}
</style>
