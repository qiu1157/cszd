<template>
  <section class="page-section">
    <el-card class="station-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>充电站详情</h3>
            <p class="section-desc">基础信息、充电桩、股东与时间线采用单页纵向展示。</p>
          </div>
          <div class="action-group">
            <el-button class="action-button action-button--secondary" @click="goBack">返回列表</el-button>
            <el-button class="action-button action-button--secondary" @click="goToEdit">编辑基础信息</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-state">加载中...</div>

      <div v-else class="detail-layout">
        <section class="detail-section station-summary">
          <div class="detail-section__head">
            <h4>概览</h4>
          </div>
          <p class="summary-text">
            {{ detail.stationName || '--' }} · {{ detail.stationStatusName || '--' }} ·
            {{ resolvePlatformSummary() }}
          </p>
          <div class="info-grid info-grid--summary">
            <div class="info-card">
              <small>系统ID</small>
              <strong>{{ detail.stationCode || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>平台</small>
              <strong>{{ resolvePlatformSummary() }}</strong>
            </div>
            <div class="info-card">
              <small>联系人</small>
              <strong>{{ resolveContactSummary() }}</strong>
            </div>
            <div class="info-card">
              <small>地址</small>
              <strong>{{ detail.address || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>充电桩</small>
              <strong>{{ pileSummaryText }}</strong>
            </div>
            <div class="info-card">
              <small>持股股东</small>
              <strong :class="{ 'summary-warning': shareholderSumIncomplete }">{{ shareholderSummaryText }}</strong>
              <p v-if="shareholderSumIncomplete" class="info-card__hint">
                持股中合计不等于 100%，请确认股权结构是否符合预期。
              </p>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <div class="detail-section__head">
            <h4>分区导航</h4>
          </div>
          <div class="section-anchor">
            <el-button class="anchor-button" @click="handleAnchorClick('section-basic-info')">基础信息</el-button>
            <el-button class="anchor-button" @click="handleAnchorClick('section-piles')">充电桩管理</el-button>
            <el-button class="anchor-button" @click="handleAnchorClick('section-shareholders')">股东信息</el-button>
            <el-button class="anchor-button" @click="handleAnchorClick('section-timeline')">历史变化</el-button>
          </div>
        </section>

        <section id="section-basic-info" class="detail-section">
          <div class="detail-section__head">
            <h4>基础信息分区</h4>
            <div class="action-group">
              <el-button class="action-button action-button--secondary" @click="openStatusDialog">修改状态</el-button>
              <el-button class="action-button action-button--secondary" @click="goToEdit">编辑基础信息</el-button>
            </div>
          </div>
          <div class="kpi-strip">
            <div class="kpi-item">
              <span>生命周期</span>
              <strong>{{ detail.stationStatusName || '--' }}</strong>
            </div>
            <div class="kpi-item">
              <span>平台归属</span>
              <strong>{{ detail.platformName || '--' }}</strong>
            </div>
            <div class="kpi-item">
              <span>平台站点编号</span>
              <strong>{{ detail.platformStationCode || '--' }}</strong>
            </div>
            <div class="kpi-item">
              <span>页面形态</span>
              <strong>单页分区</strong>
            </div>
          </div>
          <div class="info-grid">
            <div class="info-card">
              <small>充电站编号</small>
              <strong>{{ detail.stationCode || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>当前状态</small>
              <strong>{{ detail.stationStatusName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>充电站名称</small>
              <strong>{{ detail.stationName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>所在平台</small>
              <strong>{{ detail.platformName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>平台站点编号</small>
              <strong>{{ detail.platformStationCode || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>联系人</small>
              <strong>{{ detail.contactName || '--' }}</strong>
            </div>
            <div class="info-card">
              <small>联系电话</small>
              <strong>{{ detail.contactPhone || '--' }}</strong>
            </div>
            <div class="info-card info-card--wide">
              <small>地址</small>
              <strong>{{ detail.address || '--' }}</strong>
            </div>
          </div>
        </section>

        <StationPileSection
          v-if="resolvedStationId"
          :station-id="resolvedStationId"
          @loaded="applyPileSummary"
        />
        <section v-else id="section-piles" class="detail-section">
          <div class="detail-section__head">
            <h4>充电桩管理</h4>
          </div>
          <div class="placeholder-card">充电站不存在，无法维护充电桩。</div>
        </section>

        <StationShareholderSection
          v-if="resolvedStationId"
          :station-id="resolvedStationId"
          @loaded="applyShareholderSummary"
        />
        <section v-else id="section-shareholders" class="detail-section">
          <div class="detail-section__head">
            <h4>股东信息</h4>
          </div>
          <div class="placeholder-card">充电站不存在，无法维护股东关系。</div>
        </section>

        <section id="section-timeline" class="detail-section">
          <div class="detail-section__head">
            <h4>历史变化时间线</h4>
          </div>
          <TimelineView :events="timelineEvents" :loading="timelineLoading" />
        </section>
      </div>
    </el-card>

    <el-dialog
      v-model="statusDialogVisible"
      width="520px"
      class="station-status-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <div>
            <h3>修改站点状态</h3>
            <p>当前状态：{{ detail.stationStatusName || '--' }}</p>
          </div>
          <el-button text @click="closeStatusDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>新状态</label>
          <el-select v-model="statusForm.targetStatusItemId" placeholder="请选择新状态">
            <el-option
              v-for="option in statusOptions"
              :key="option.itemId"
              :label="option.itemName"
              :value="option.itemId"
            />
          </el-select>
          <p v-if="statusFormError" class="field-error">{{ statusFormError }}</p>
        </div>

        <div class="form-field">
          <label>备注（可选）</label>
          <el-input
            v-model="statusForm.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注，最多500字符"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button class="action-button action-button--secondary" @click="closeStatusDialog">取消</el-button>
          <el-button class="action-button" type="primary" :loading="statusSaving" @click="submitStatusForm">保存状态</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getStationTimeline, type TimelineEvent } from '@/api/operation-log'
import { getStationDetail, updateStationStatus, type StationDetail } from '@/api/station'
import { getStationStatusOptions, type StationStatusOption } from '@/api/station/form-options'
import StationPileSection from '@/components/business/StationPileSection.vue'
import StationShareholderSection from '@/components/business/StationShareholderSection.vue'
import TimelineView from '@/components/common/TimelineView.vue'
import { buildStationEditPath, STATION_LIST_PATH } from '@/router/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const resolvedStationId = resolveStationIdAtSetup()
const detail = reactive<StationDetail>({
  id: 0,
  stationCode: '',
  stationName: '',
  platformItemId: null,
  platformName: '',
  platformStationCode: '',
  address: '',
  contactName: '',
  contactPhone: '',
  stationStatusItemId: null,
  stationStatusName: '',
})

const statusDialogVisible = ref(false)
const statusSaving = ref(false)
const statusOptions = ref<StationStatusOption[]>([])
const statusFormError = ref('')
const statusForm = reactive({
  targetStatusItemId: null as number | null,
  remark: '',
})

const timelineEvents = ref<TimelineEvent[]>([])
const timelineLoading = ref(false)

function resolveStationId() {
  const rawId = Number(route.params.id)
  return Number.isInteger(rawId) && rawId > 0 ? rawId : null
}

function resolveStationIdAtSetup() {
  return resolveStationId()
}

const pileSummary = ref<{ pileCount: number; gunCount: number } | null>(null)

const pileSummaryText = computed(() => {
  if (!pileSummary.value) {
    return '--'
  }
  return `${pileSummary.value.pileCount} 根 / ${pileSummary.value.gunCount} 把枪`
})

const shareholderSummary = ref<{ activeShareholderCount: number; activePercentageSum: number } | null>(null)

const shareholderSummaryText = computed(() => {
  if (!shareholderSummary.value) {
    return '--'
  }
  return `${shareholderSummary.value.activeShareholderCount} 位 / 合计 ${shareholderSummary.value.activePercentageSum}%`
})

// 与股东分区的警示口径一致：仅当“存在持股中股东且合计≠100%”时提示；
// 摘要未加载（null）或无持股中股东时保持安静
const shareholderSumIncomplete = computed(() => {
  if (!shareholderSummary.value) {
    return false
  }
  return (
    shareholderSummary.value.activeShareholderCount > 0
    && shareholderSummary.value.activePercentageSum !== 100
  )
})

function applyPileSummary(summary: { pileCount: number; gunCount: number }) {
  pileSummary.value = summary
}

// 股东分区加载完成时回填概览摘要：与桩分区同构，由子组件 emit loaded 驱动
function applyShareholderSummary(summary: { activeShareholderCount: number; activePercentageSum: number }) {
  shareholderSummary.value = summary
}

function applyDetail(data: StationDetail) {
  detail.id = data.id
  detail.stationCode = data.stationCode
  detail.stationName = data.stationName
  detail.platformItemId = data.platformItemId
  detail.platformName = data.platformName
  detail.platformStationCode = data.platformStationCode
  detail.address = data.address
  detail.contactName = data.contactName
  detail.contactPhone = data.contactPhone
  detail.stationStatusItemId = data.stationStatusItemId
  detail.stationStatusName = data.stationStatusName
}

function goBack() {
  void router.push(STATION_LIST_PATH)
}

function goToEdit() {
  const stationId = resolveStationId()
  if (!stationId) {
    void router.push(STATION_LIST_PATH)
    return
  }
  void router.push(buildStationEditPath(stationId))
}

function handleAnchorClick(sectionId: string) {
  const element = document.getElementById(sectionId)
  element?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function openStatusDialog() {
  statusForm.targetStatusItemId = detail.stationStatusItemId
  statusForm.remark = ''
  statusFormError.value = ''
  statusDialogVisible.value = true

  if (statusOptions.value.length > 0) {
    return
  }

  try {
    statusOptions.value = await getStationStatusOptions()
  } catch (error) {
    statusDialogVisible.value = false
    ElMessage.error(resolveApiErrorMessage(error, '状态选项加载失败，请稍后重试'))
  }
}

function closeStatusDialog() {
  if (statusSaving.value) {
    return
  }
  statusDialogVisible.value = false
}

async function submitStatusForm() {
  if (!statusForm.targetStatusItemId) {
    statusFormError.value = '请选择新状态'
    return
  }

  const stationId = resolveStationId()
  if (!stationId) {
    return
  }

  statusSaving.value = true
  try {
    await updateStationStatus(stationId, {
      stationStatusItemId: statusForm.targetStatusItemId,
      remark: statusForm.remark.trim(),
    })
    ElMessage.success('状态切换成功')
    statusDialogVisible.value = false
    applyDetail(await getStationDetail(stationId))
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '状态切换失败，请稍后重试'))
  } finally {
    statusSaving.value = false
  }
}

function resolvePlatformSummary() {
  const platformName = detail.platformName?.trim() || '--'
  const platformStationCode = detail.platformStationCode?.trim()
  return platformStationCode ? `${platformName} / ${platformStationCode}` : platformName
}

function resolveContactSummary() {
  const contactName = detail.contactName?.trim() || '--'
  const contactPhone = detail.contactPhone?.trim()
  return contactPhone ? `${contactName} / ${contactPhone}` : contactName
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

async function loadTimeline(stationId: number) {
  timelineLoading.value = true
  try {
    timelineEvents.value = await getStationTimeline(stationId)
  } catch (error) {
    console.error('时间线加载失败:', error)
    timelineEvents.value = []
  } finally {
    timelineLoading.value = false
  }
}

onMounted(() => {
  const stationId = resolveStationId()
  if (!stationId) {
    ElMessage.error('充电站不存在')
    void router.push(STATION_LIST_PATH)
    return
  }

  void (async () => {
    try {
      const data = await getStationDetail(stationId)
      applyDetail(data)
      await loadTimeline(stationId)
    } catch (error) {
      ElMessage.error(resolveApiErrorMessage(error, '充电站详情加载失败，请稍后重试'))
      await router.push(STATION_LIST_PATH)
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

.info-card strong.summary-warning {
  color: var(--warning);
}

.info-card__hint {
  margin: 0;
  color: var(--warning);
  font-size: 12px;
}

.info-card--wide {
  grid-column: 1 / -1;
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

.dialog-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.dialog-head h3 {
  margin: 0;
  font-size: 20px;
}

.dialog-head p {
  margin: 8px 0 0;
  color: var(--text-secondary);
}

.dialog-body {
  display: grid;
  gap: 16px;
}

.form-field {
  display: grid;
  gap: 8px;
}

.form-field label {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.field-error {
  margin: 0;
  color: #ff8f8f;
  font-size: 13px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
