<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="280px">
      <div class="brand">
        <div class="brand-title">充电站管理系统</div>
        <div class="brand-sub">第一期原型基准</div>
      </div>

      <el-menu class="menu" router :default-active="activePath">
        <el-menu-item v-for="item in appMenuItems" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header" height="72px">
        <div class="header-left">
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-desc">页面空壳已就绪，后续按 tasks.md 逐步补齐业务能力。</div>
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
import { RouterView } from 'vue-router'

import { appMenuItems } from './menu'

const route = useRoute()

const activePath = computed(() => route.path)
const pageTitle = computed(() => (route.meta?.title as string) || '页面')
</script>

<style scoped lang="scss">
.app-shell {
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
}

.app-sidebar {
  border-right: 1px solid var(--line-soft);
  background: var(--bg-panel-strong);
  padding: 16px 12px;
}

.brand {
  padding: 10px 12px 16px;
}

.brand-title {
  font-size: 16px;
  letter-spacing: 0.06em;
}

.brand-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-muted);
}

.menu {
  border-right: none;
  background: transparent;
}

.app-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--line-soft);
  background: var(--bg-panel);
}

.header-left {
  display: grid;
  gap: 6px;
}

.page-title {
  font-size: 18px;
}

.page-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.app-main {
  padding: 20px;
}
</style>

