<template>
  <section class="page-section">
    <el-card class="user-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>业务管理员列表</h3>
            <p>系统管理员可查看业务管理员账号状态与最后登录时间；新增、启停、重置密码将在后续任务中实现。</p>
          </div>
          <el-button type="primary" disabled>新增业务管理员</el-button>
        </div>
      </template>

      <div class="summary-row">
        <div class="summary-chip">当前仅展示业务管理员，不显示 `admin`</div>
        <div class="summary-chip">最后登录时间在登录成功后自动更新</div>
      </div>

      <el-table v-loading="loading" :data="tableRows" class="user-table" empty-text="暂无业务管理员，后续任务将支持新增业务管理员。">
        <el-table-column label="账号信息" min-width="260">
          <template #default="{ row }">
            <div class="title-cell">
              <strong>{{ row.account }}</strong>
              <span class="subtitle">{{ row.subtitle }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            {{ row.roleLabel }}
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最后登录" min-width="180">
          <template #default="{ row }">
            {{ row.lastLoginText }}
          </template>
        </el-table-column>

        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default>
            <div class="action-group">
              <el-button link type="primary" disabled>重置密码</el-button>
              <el-button link disabled>启用</el-button>
              <el-button link disabled>停用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

import { getBusinessAdmins } from '@/api/user'

import { mapBusinessAdminToTableRow } from './adapters'

const loading = ref(false)
const businessAdmins = ref<Awaited<ReturnType<typeof getBusinessAdmins>>>([])

const tableRows = computed(() => businessAdmins.value.map(mapBusinessAdminToTableRow))

async function loadBusinessAdmins() {
  loading.value = true
  try {
    businessAdmins.value = await getBusinessAdmins()
  } catch {
    ElMessage.error('业务管理员列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadBusinessAdmins()
})
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.user-page-card {
  border: 1px solid var(--line-soft);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0)),
    var(--bg-panel);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.section-head p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.summary-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.summary-chip {
  border: 1px solid rgba(57, 200, 255, 0.14);
  border-radius: 999px;
  padding: 8px 12px;
  color: var(--text-secondary);
  font-size: 13px;
  background: rgba(57, 200, 255, 0.06);
}

.user-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(57, 200, 255, 0.05);
  --el-table-border-color: rgba(150, 206, 255, 0.1);
  --el-table-row-hover-bg-color: rgba(57, 200, 255, 0.04);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

.title-cell {
  display: grid;
  gap: 6px;
}

.title-cell strong {
  font-size: 15px;
  font-weight: 600;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 13px;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 960px) {
  .section-head {
    flex-direction: column;
  }
}
</style>
