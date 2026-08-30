<template>
  <section class="page-section">
    <el-card class="station-page-card" shadow="never">
      <template #header>
        <div class="section-head section-head--stacked">
          <div class="section-head__top">
            <h3>充电站管理</h3>
            <el-button class="action-button" type="primary" @click="goToCreate">新增充电站</el-button>
          </div>
          <div class="toolbar-row">
            <el-input
              v-model="searchForm.stationName"
              class="toolbar-input"
              placeholder="请输入充电站名称"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-button class="action-button" type="primary" @click="handleSearch">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableRows" class="station-table" empty-text="暂无充电站，请先新增充电站。">
        <el-table-column prop="stationCode" label="充电站编号" min-width="160" />
        <el-table-column prop="stationName" label="充电站名称" min-width="220" />
        <el-table-column prop="platformName" label="所在平台" min-width="140" />
        <el-table-column prop="platformStationCodeText" label="平台站点编号" min-width="160" />
        <el-table-column prop="contactName" label="联系人" min-width="120" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="140" />
        <el-table-column label="当前状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAtText" label="更新时间" min-width="180" />
        <el-table-column label="操作" min-width="140" fixed="right">
          <template #default>
            <div class="action-group">
              <el-button class="action-button action-button--muted" :disabled="true">查看详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getStations, type StationListItem } from '@/api/station'
import { STATION_CREATE_PATH } from '@/router/navigation'

import { mapStationToTableRow } from './adapters'

const router = useRouter()
const loading = ref(false)
const stations = ref<StationListItem[]>([])
const searchForm = ref({
  stationName: '',
})

const tableRows = computed(() => stations.value.map(mapStationToTableRow))

async function loadStations() {
  loading.value = true
  try {
    stations.value = await getStations(searchForm.value.stationName)
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '充电站列表加载失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}

function goToCreate() {
  void router.push(STATION_CREATE_PATH)
}

function handleSearch() {
  void loadStations()
}

function resolveApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }

  return fallbackMessage
}

onMounted(() => {
  void loadStations()
})
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.station-page-card {
  border: 1px solid var(--line-soft);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0)),
    var(--bg-panel);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head--stacked {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.section-head__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.station-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(57, 200, 255, 0.05);
  --el-table-border-color: rgba(150, 206, 255, 0.1);
  --el-table-row-hover-bg-color: rgba(57, 200, 255, 0.04);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

.toolbar-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-input {
  width: min(320px, 100%);
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-button {
  min-width: 88px;
  margin: 0;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 600;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    color 0.2s ease;
}

.action-button--muted {
  border-color: rgba(148, 163, 184, 0.18);
  color: rgba(206, 216, 230, 0.58);
  background: rgba(148, 163, 184, 0.06);
  box-shadow: none;
}

.action-button--muted.is-disabled,
.action-button--muted[disabled] {
  border-color: rgba(148, 163, 184, 0.14);
  color: rgba(206, 216, 230, 0.42);
  background: rgba(148, 163, 184, 0.04);
}

@media (max-width: 768px) {
  .section-head__top,
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-input {
    width: 100%;
  }
}
</style>
