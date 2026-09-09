<template>
  <section class="page-section">
    <div class="section-stack">
      <div v-if="loading" class="loading-state">加载中...</div>

      <template v-else>
        <div class="detail-hero">
          <div class="hero-info">
            <div class="crumb">
              <button type="button" @click="goBack">充电站管理</button>
              <span>/</span>
              <span>站点详情</span>
            </div>
            <div class="section-head" style="margin-bottom: 10px">
              <div>
                <h3>{{ detail.stationName || '--' }}</h3>
              </div>
              <div class="actions-row">
                <span
                  v-if="detail.stationStatusName"
                  :class="['tag', resolveStatusClass()]"
                >
                  {{ detail.stationStatusName }}
                </span>
                <el-button class="action-button action-button--secondary" @click="openStatusDialog">
                  修改状态
                </el-button>
                <el-button class="action-button action-button--primary" @click="goToEdit">
                  编辑基础信息
                </el-button>
              </div>
            </div>
            <p>{{ resolvePlatformSummary() }}</p>
            <div class="info-grid">
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
                <small>电表户号</small>
                <strong>{{ detail.meterAccountNumber || '--' }}</strong>
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
                <strong :class="{ 'summary-warning': shareholderSumIncomplete }">
                  {{ shareholderSummaryText }}
                </strong>
              </div>
            </div>
          </div>

          <section id="section-piles" class="section-card">
            <div class="section-head">
              <div>
                <h3>充电桩管理</h3>
              </div>
            </div>
            <StationPileSection
              :station-id="detail.id"
              @loaded="applyPileSummary"
            />
          </section>
        </div>

        <section id="section-shareholders" class="section-card">
          <div class="section-head">
            <div>
              <h3>股东信息</h3>
            </div>
          </div>
          <StationShareholderSection
            :station-id="detail.id"
            @loaded="applyShareholderSummary"
          />
        </section>

        <section id="section-timeline" class="timeline-card">
          <div class="section-head">
            <div>
              <h3>历史变化时间线</h3>
            </div>
          </div>
          <TimelineView
            v-if="!timelineLoading && timelineEvents.length > 0"
            :events="timelineEvents"
            entity-type="station"
          />
          <div v-else-if="timelineLoading" class="timeline-placeholder">加载历史变化...</div>
          <div v-else class="timeline-placeholder">暂无历史变化记录</div>
        </section>
      </template>
    </div>

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
  meterAccountNumber: '',
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

const pileSummary = ref<{ pileCount: number; gunCount: number } | null>(null)

const pileSummaryText = computed(() => {
  if (!pileSummary.value) {
    return '--'
  }
  return `${pileSummary.value.pileCount} 台 / ${pileSummary.value.gunCount} 把枪`
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
  detail.meterAccountNumber = data.meterAccountNumber
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

function resolveStatusClass() {
  const statusName = detail.stationStatusName?.toLowerCase() || ''
  if (statusName.includes('运营') || statusName.includes('正常')) {
    return 'tag-success'
  }
  if (statusName.includes('停用') || statusName.includes('维护')) {
    return 'tag-warning'
  }
  return 'tag-info'
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
  padding: 20px;
}

.section-stack {
  display: grid;
  gap: 16px;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  color: var(--text-secondary);
}

.crumb {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}

.crumb button {
  background: none;
  border: none;
  color: var(--brand-primary);
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}

.crumb button:hover {
  text-decoration: underline;
}

.detail-hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}

.hero-info {
  padding: 24px;
  border-radius: 24px;
  border: 1px solid rgba(57, 200, 255, 0.12);
  background: linear-gradient(135deg, rgba(57, 200, 255, 0.12), rgba(255, 255, 255, 0.02));
}

.hero-info h3 {
  margin: 0;
  font-size: 30px;
  letter-spacing: -0.04em;
}

.hero-info p {
  margin: 0 0 22px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.tag {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.tag-success {
  background: rgba(45, 222, 157, 0.15);
  color: var(--success);
}

.tag-warning {
  background: rgba(255, 191, 102, 0.15);
  color: var(--warning);
}

.tag-info {
  background: rgba(100, 169, 255, 0.15);
  color: var(--info);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 22px;
}

.info-card {
  border-radius: 18px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.info-card small {
  display: block;
  color: var(--text-muted);
  margin-bottom: 8px;
  font-size: 12px;
}

.info-card strong {
  display: block;
  color: var(--text-primary);
  font-size: 15px;
  word-break: break-word;
  line-height: 1.5;
}

.summary-warning {
  color: var(--warning);
}

.section-card {
  padding: 24px;
  border-radius: 24px;
  border: 1px solid var(--line-soft);
  background: var(--bg-panel);
}

.actions-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.timeline-card {
  padding: 24px;
  border-radius: 24px;
  border: 1px solid var(--line-soft);
  background: var(--bg-panel);
}

.placeholder-card {
  padding: 40px;
  text-align: center;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.03);
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

@media (max-width: 1280px) {
  .detail-hero {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions-row {
    width: 100%;
    flex-direction: column;
  }

  .actions-row .action-button {
    width: 100%;
  }
}
</style>
