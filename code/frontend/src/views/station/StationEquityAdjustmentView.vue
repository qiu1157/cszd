<template>
  <section class="page-section">
    <el-card class="station-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>股权调整</h3>
            <p class="section-desc">调整当前充电站持股中股东的占股比例。</p>
          </div>
          <div class="action-group">
            <el-button class="action-button action-button--secondary" @click="goBack">返回详情</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-state">加载中...</div>

      <div v-else-if="!resolvedStationId" class="placeholder-card">充电站不存在，无法调整股权。</div>

      <div v-else class="detail-layout">
        <section class="detail-section">
          <div class="detail-section__head">
            <h4>持股股东</h4>
            <p class="section-desc">仅展示持股中股东，已退出股东不在此页面调整。</p>
          </div>

          <div v-if="activeShareholdings.length === 0" class="placeholder-card">
            当前站点没有持股中股东，无需调整股权。
          </div>

          <div v-else class="equity-list">
            <div
              v-for="holding in activeShareholdings"
              :key="holding.id"
              class="equity-item"
            >
              <div class="equity-item__info">
                <strong>{{ holding.shareholderName }}</strong>
                <span class="equity-item__contact">{{ resolveContactSummary(holding) }}</span>
              </div>
              <div class="equity-item__input">
                <el-input
                  v-model.number="holding.ownershipPercentage"
                  type="number"
                  :min="0"
                  :max="100"
                  placeholder="占股比例"
                  disabled
                >
                  <template #append>%</template>
                </el-input>
              </div>
            </div>
          </div>

          <div v-if="activeShareholdings.length > 0" class="equity-summary">
            <span>持股中合计</span>
            <strong :class="{ 'summary-warning': percentageSum !== 100 }">{{ percentageSum }}%</strong>
            <p v-if="percentageSum !== 100" class="info-card__hint">
              合计不等于 100%，保存时将二次确认。
            </p>
          </div>
        </section>

        <section class="detail-section">
          <div class="detail-section__head">
            <h4>调整说明</h4>
          </div>
          <ul class="equity-notes">
            <li>股权调整立即生效，不额外指定未来生效时间。</li>
            <li>保存前若合计不等于 100%，将弹窗提示确认。</li>
            <li>保存成功后生成一条股权调整事件并写入时间线。</li>
          </ul>
        </section>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getStationShareholdings, type StationShareholding } from '@/api/shareholding'
import { buildStationDetailPath, STATION_LIST_PATH } from '@/router/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const resolvedStationId = resolveStationIdAtSetup()
const shareholdings = ref<StationShareholding[]>([])

const activeShareholdings = computed(() =>
  shareholdings.value.filter(holding => holding.holdingStatus === 'ACTIVE')
)

const percentageSum = computed(() =>
  activeShareholdings.value.reduce((sum, holding) => sum + Number(holding.ownershipPercentage), 0)
)

function resolveStationId() {
  const rawId = Number(route.params.id)
  return Number.isInteger(rawId) && rawId > 0 ? rawId : null
}

function resolveStationIdAtSetup() {
  return resolveStationId()
}

function resolveContactSummary(holding: StationShareholding) {
  const contactName = holding.contactName?.trim() || '--'
  const contactPhone = holding.contactPhone?.trim()
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

function goBack() {
  const stationId = resolveStationId()
  if (stationId) {
    void router.push(buildStationDetailPath(stationId))
    return
  }
  void router.push(STATION_LIST_PATH)
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
      shareholdings.value = await getStationShareholdings(stationId)
    } catch (error) {
      ElMessage.error(resolveApiErrorMessage(error, '持股关系加载失败，请稍后重试'))
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

.detail-section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.placeholder-card {
  border: 1px solid rgba(150, 206, 255, 0.12);
  border-radius: 16px;
  padding: 16px;
  background: rgba(8, 15, 30, 0.38);
  color: var(--text-secondary);
}

.equity-list {
  display: grid;
  gap: 12px;
}

.equity-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid rgba(150, 206, 255, 0.12);
  border-radius: 16px;
  background: rgba(8, 15, 30, 0.38);
}

.equity-item__info {
  display: grid;
  gap: 4px;
}

.equity-item__contact {
  color: var(--text-secondary);
  font-size: 13px;
}

.equity-item__input {
  width: 160px;
}

.equity-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid rgba(150, 206, 255, 0.12);
  background: rgba(8, 15, 30, 0.32);
}

.equity-summary strong {
  font-size: 18px;
}

.equity-summary strong.summary-warning {
  color: var(--warning);
}

.info-card__hint {
  margin: 0;
  color: var(--warning);
  font-size: 12px;
}

.equity-notes {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

@media (max-width: 768px) {
  .section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .equity-item {
    flex-direction: column;
    align-items: stretch;
  }

  .equity-item__input {
    width: 100%;
  }
}
</style>