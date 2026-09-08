<template>
  <section class="page-section">
    <el-card class="shareholder-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>股东详情</h3>
            <p class="section-desc">基础信息、关联充电站与历史时间线采用单页纵向展示。</p>
          </div>
          <div class="action-group">
            <el-button class="action-button action-button--secondary" @click="goBack">返回列表</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-state">加载中...</div>

      <div v-else class="detail-layout">
        <section class="detail-section shareholder-summary">
          <div class="detail-section__head">
            <h4>概览</h4>
          </div>
          <p class="summary-text">
            {{ detail.shareholderName || '--' }} · {{ statusLabel }} · {{ contactSummaryText }}
          </p>
          <div class="info-grid info-grid--summary">
            <div class="info-card">
              <small>股东ID</small>
              <strong>{{ detail.id || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>主档状态</small>
              <strong>{{ statusLabel }}</strong>
            </div>
            <div class="info-card">
              <small>联系人</small>
              <strong>{{ detail.contactName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>联系电话</small>
              <strong>{{ detail.contactPhone || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>关联站点</small>
              <strong>{{ stationCountText }}</strong>
            </div>
            <div class="info-card">
              <small>最近更新</small>
              <strong>{{ formatDateTimeText(detail.updatedAt) }}</strong>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <div class="detail-section__head">
            <h4>分区导航</h4>
          </div>
          <div class="section-anchor">
            <el-button class="anchor-button" @click="handleAnchorClick('section-basic-info')">基础信息</el-button>
            <el-button class="anchor-button" @click="handleAnchorClick('section-stations')">关联充电站</el-button>
            <el-button class="anchor-button" @click="handleAnchorClick('section-timeline')">历史变化</el-button>
          </div>
        </section>

        <section id="section-basic-info" class="detail-section">
          <div class="detail-section__head">
            <h4>基础信息分区</h4>
          </div>
          <div class="kpi-strip">
            <div class="kpi-item">
              <span>主档状态</span>
              <strong>{{ statusLabel }}</strong>
            </div>
            <div class="kpi-item">
              <span>关联站点数</span>
              <strong>{{ stationCountText }}</strong>
            </div>
            <div class="kpi-item">
              <span>创建时间</span>
              <strong>{{ formatDateTimeText(detail.createdAt) }}</strong>
            </div>
            <div class="kpi-item">
              <span>更新时间</span>
              <strong>{{ formatDateTimeText(detail.updatedAt) }}</strong>
            </div>
          </div>
          <div class="info-grid">
            <div class="info-card">
              <small>股东编号</small>
              <strong>{{ detail.id || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>股东名称</small>
              <strong>{{ detail.shareholderName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>主档状态</small>
              <strong>{{ statusLabel }}</strong>
            </div>
            <div class="info-card">
              <small>联系人</small>
              <strong>{{ detail.contactName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>联系电话</small>
              <strong>{{ detail.contactPhone || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>创建时间</small>
              <strong>{{ formatDateTimeText(detail.createdAt) }}</strong>
            </div>
            <div class="info-card">
              <small>更新时间</small>
              <strong>{{ formatDateTimeText(detail.updatedAt) }}</strong>
            </div>
          </div>
        </section>

        <section id="section-stations" class="detail-section">
          <div class="detail-section__head">
            <h4>关联充电站</h4>
          </div>
          <el-empty v-if="stationsError" description="关联充电站加载失败，请稍后重试">
            <el-button class="action-button action-button--secondary" @click="reloadShareholderStations">
              重新加载
            </el-button>
          </el-empty>
          <el-table
            v-else
            v-loading="stationsLoading"
            :data="stationList"
            class="shareholder-station-table"
            empty-text="暂无关联站点"
          >
            <el-table-column prop="stationCode" label="站点编号" min-width="150" />
            <el-table-column prop="stationName" label="站点名称" min-width="180" />
            <el-table-column label="站点状态" min-width="110">
              <template #default="{ row }">{{ row.stationStatusName || '--' }}</template>
            </el-table-column>
            <el-table-column label="持股占比" min-width="110">
              <template #default="{ row }">{{ formatOwnershipText(row.ownershipPercentage) }}</template>
            </el-table-column>
            <el-table-column label="生效时间" min-width="160">
              <template #default="{ row }">{{ formatDateTimeText(row.effectiveAt || '') }}</template>
            </el-table-column>
          </el-table>
        </section>

        <section id="section-timeline" class="detail-section">
          <div class="detail-section__head">
            <h4>历史变化时间线</h4>
            <el-select
              v-model="selectedStationIdForTimeline"
              placeholder="全部站点"
              clearable
              style="width: 240px"
              @change="handleTimelineStationFilterChange"
            >
              <el-option label="全部站点" :value="null" />
              <el-option
                v-for="station in stationList"
                :key="station.stationId"
                :label="station.stationName"
                :value="station.stationId"
              />
            </el-select>
          </div>
          <TimelineView :events="timelineEvents" :loading="timelineLoading" />
        </section>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getShareholderTimeline, type TimelineEvent } from '@/api/operation-log'
import {
  getShareholderDetail,
  getShareholderStations,
  type ShareholderDetail,
  type ShareholderStationListItem,
} from '@/api/shareholder'
import TimelineView from '@/components/common/TimelineView.vue'
import { SHAREHOLDER_LIST_PATH } from '@/router/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const detail = reactive<ShareholderDetail>({
  id: 0,
  shareholderName: '',
  contactName: null,
  contactPhone: null,
  shareholderStatus: '',
  createdAt: '',
  updatedAt: '',
})

const stationList = ref<ShareholderStationListItem[]>([])
const stationsLoading = ref(true)
const stationsError = ref(false)

const timelineEvents = ref<TimelineEvent[]>([])
const timelineLoading = ref(false)
const selectedStationIdForTimeline = ref<number | null>(null)

const statusLabel = computed(() => {
  if (detail.shareholderStatus === 'ENABLED') {
    return '启用'
  }
  if (detail.shareholderStatus === 'DISABLED') {
    return '停用'
  }
  return '--'
})

const contactSummaryText = computed(() => {
  const contactName = detail.contactName?.trim() || '--'
  const contactPhone = detail.contactPhone?.trim()
  return contactPhone ? `${contactName} / ${contactPhone}` : contactName
})

// 关联站点数独立于详情主体加载：加载中/失败时先给占位文案，避免误展示 0 个
const stationCountText = computed(() => {
  if (stationsLoading.value) {
    return '加载中...'
  }
  if (stationsError.value) {
    return '--'
  }
  return `${stationList.value.length} 个`
})

function formatDateTimeText(value: string) {
  return value ? value.slice(0, 16).replace('T', ' ') : '--'
}

function formatOwnershipText(value: number) {
  return `${value}%`
}

// 关联站点独立加载：失败仅影响本分区（空态+重试），不回退整页，避免详情主体已可见时被强制跳走
function loadShareholderStations(shareholderId: number) {
  stationsLoading.value = true
  stationsError.value = false
  void (async () => {
    try {
      stationList.value = await getShareholderStations(shareholderId)
    } catch (error) {
      stationsError.value = true
      ElMessage.error(resolveApiErrorMessage(error, '关联充电站加载失败，请稍后重试'))
    } finally {
      stationsLoading.value = false
    }
  })()
}

function reloadShareholderStations() {
  const shareholderId = resolveShareholderId()
  if (shareholderId) {
    loadShareholderStations(shareholderId)
  }
}

function resolveShareholderId() {
  const rawId = Number(route.params.id)
  return Number.isInteger(rawId) && rawId > 0 ? rawId : null
}

function goBack() {
  void router.push(SHAREHOLDER_LIST_PATH)
}

function handleAnchorClick(sectionId: string) {
  const element = document.getElementById(sectionId)
  element?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function loadTimeline(shareholderId: number, stationId?: number) {
  timelineLoading.value = true
  try {
    timelineEvents.value = await getShareholderTimeline(shareholderId, stationId)
  } catch (error) {
    console.error('时间线加载失败:', error)
    timelineEvents.value = []
  } finally {
    timelineLoading.value = false
  }
}

function handleTimelineStationFilterChange() {
  const shareholderId = resolveShareholderId()
  if (!shareholderId) return
  
  const stationId = selectedStationIdForTimeline.value ?? undefined
  void loadTimeline(shareholderId, stationId)
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
  const shareholderId = resolveShareholderId()
  if (!shareholderId) {
    ElMessage.error('股东不存在')
    void router.push(SHAREHOLDER_LIST_PATH)
    return
  }

  // 站点列表与详情并行加载：站点分区有独立的加载/失败态，不阻塞详情主体渲染
  loadShareholderStations(shareholderId)
  
  // 加载时间线
  void loadTimeline(shareholderId)

  void (async () => {
    try {
      const data = await getShareholderDetail(shareholderId)
      detail.id = data.id
      detail.shareholderName = data.shareholderName
      detail.contactName = data.contactName
      detail.contactPhone = data.contactPhone
      detail.shareholderStatus = data.shareholderStatus
      detail.createdAt = data.createdAt
      detail.updatedAt = data.updatedAt
    } catch (error) {
      ElMessage.error(resolveApiErrorMessage(error, '股东详情加载失败，请稍后重试'))
      await router.push(SHAREHOLDER_LIST_PATH)
    } finally {
      loading.value = false
    }
  })()
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

.section-head h3,
.detail-section__head h4 {
  margin: 0;
}

.section-desc {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  color: var(--text-secondary);
}

.detail-layout {
  display: grid;
  gap: 16px;
}

.detail-section {
  display: grid;
  gap: 12px;
}

.summary-text {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

.detail-section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.info-grid--summary {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.info-card,
.placeholder-card {
  border: 1px solid rgba(150, 206, 255, 0.12);
  border-radius: 16px;
  padding: 16px;
  background: rgba(8, 15, 30, 0.38);
}

.info-card {
  display: grid;
  gap: 6px;
}

.info-card small {
  color: var(--text-secondary);
}

.info-card strong {
  line-height: 1.5;
  word-break: break-all;
}

.placeholder-card {
  color: var(--text-secondary);
}

.section-anchor {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.anchor-button {
  border-radius: 999px;
}

.kpi-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(150, 206, 255, 0.12);
  background: rgba(8, 15, 30, 0.32);
}

.kpi-item span {
  color: var(--text-secondary);
  font-size: 12px;
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 暗色主题适配：Element Plus 表格默认白色背景，通过 CSS 变量对齐详情页暗色风格（与股东列表页同口径） */
.shareholder-station-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(57, 200, 255, 0.05);
  --el-table-border-color: rgba(150, 206, 255, 0.1);
  --el-table-row-hover-bg-color: rgba(57, 200, 255, 0.04);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

@media (max-width: 768px) {
  .section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .detail-section__head {
    flex-direction: column;
    align-items: stretch;
  }

  .info-grid--summary,
  .kpi-strip,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>