<template>
  <section class="page-section">
    <el-card class="shareholder-page-card" shadow="never">
      <template #header>
        <div class="section-head section-head--stacked">
          <div class="section-head__top">
            <h3>股东列表页</h3>
            <el-button class="action-button" type="primary" @click="handleCreate">新增股东</el-button>
          </div>
          <div class="toolbar-row">
            <el-input
              v-model="searchForm.shareholderName"
              class="toolbar-input"
              placeholder="请输入股东名称"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchForm.shareholderStatus"
              class="toolbar-input"
              placeholder="请选择状态"
              clearable
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button class="action-button" type="primary" @click="handleSearch">查询</el-button>
            <el-button class="action-button" @click="handleResetFilters">重置筛选</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableRows" class="shareholder-table" empty-text="暂无股东，请先新增股东。">
        <el-table-column label="股东名称" min-width="220">
          <template #default="{ row }">
            <div class="title-cell">
              <strong>{{ row.shareholderName }}</strong>
              <span class="title-cell__subtitle">{{ row.shareholderSubtitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contactText" label="联系人" min-width="180" />
        <el-table-column label="关联站点" min-width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleShowStationDetail(row)">
              {{ row.relatedStationCount }} 个站点
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAtText" label="最近变化" min-width="180" />
      </el-table>
    </el-card>

    <el-dialog
      v-model="stationDialogVisible"
      :close-on-click-modal="false"
      :title="stationDialogTitle"
      width="520px"
    >
      <div class="station-summary">共 {{ activeShareholder?.relatedStationCount ?? 0 }} 个关联站点</div>
      <el-empty description="暂无关联站点" :image-size="80" />
      <template #footer>
        <el-button @click="stationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

import { getShareholders, type ShareholderListItem } from '@/api/shareholder'
import {
  SHAREHOLDER_STATUS_OPTIONS,
  type ShareholderStatus,
} from '@/constants/enums/shareholder-status'

import { mapShareholderToTableRow, type ShareholderTableRow } from './adapters'

const loading = ref(false)
const shareholders = ref<ShareholderListItem[]>([])
const statusOptions = SHAREHOLDER_STATUS_OPTIONS
const searchForm = ref({
  shareholderName: '',
  shareholderStatus: null as ShareholderStatus | null,
})

const tableRows = computed(() => shareholders.value.map(mapShareholderToTableRow))

async function loadShareholders() {
  loading.value = true
  try {
    shareholders.value = await getShareholders(
      searchForm.value.shareholderName,
      searchForm.value.shareholderStatus,
    )
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '股东列表加载失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  void loadShareholders()
}

function handleResetFilters() {
  searchForm.value.shareholderName = ''
  searchForm.value.shareholderStatus = null
  void loadShareholders()
}

const stationDialogVisible = ref(false)
const activeShareholder = ref<ShareholderTableRow | null>(null)

const stationDialogTitle = computed(() =>
  activeShareholder.value ? `${activeShareholder.value.shareholderName} · 关联站点` : '关联站点',
)

// 站点明细数据依赖里程碑 F 的持股关系表，当前先呈现数量与空态占位
function handleShowStationDetail(row: ShareholderTableRow) {
  activeShareholder.value = row
  stationDialogVisible.value = true
}

// 新增表单在 E-3 任务实现，先给出明确提示避免死按钮
function handleCreate() {
  ElMessage.info('新增股东功能开发中，将在股东新增任务中开放')
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
  void loadShareholders()
})
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.shareholder-page-card {
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

.shareholder-table {
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

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-cell__subtitle {
  font-size: 12px;
  color: var(--text-secondary);
}

.station-summary {
  margin-bottom: 8px;
  color: var(--text-secondary);
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