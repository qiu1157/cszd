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
            <el-button class="action-button action-button--muted" @click="goBack">返回列表</el-button>
            <el-button class="action-button" type="primary" @click="goToEdit">编辑基础信息</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-state">加载中...</div>

      <div v-else class="detail-layout">
        <section class="detail-section">
          <div class="detail-section__head">
            <h4>基础信息</h4>
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

        <section class="detail-section">
          <div class="detail-section__head">
            <h4>充电桩管理</h4>
          </div>
          <div class="placeholder-card">后续任务实现：在此分区维护充电桩列表与新增入口。</div>
        </section>

        <section class="detail-section">
          <div class="detail-section__head">
            <h4>股东信息</h4>
          </div>
          <div class="placeholder-card">后续任务实现：在此分区维护股东关系与占比信息。</div>
        </section>

        <section class="detail-section">
          <div class="detail-section__head">
            <h4>历史变化时间线</h4>
          </div>
          <div class="placeholder-card">后续任务实现：在此分区展示站点历史变化时间线。</div>
        </section>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getStationDetail, type StationDetail } from '@/api/station'
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
  stationStatusItemId: null,
  stationStatusName: '',
})

function resolveStationId() {
  const rawId = Number(route.params.id)
  return Number.isInteger(rawId) && rawId > 0 ? rawId : null
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

.info-card--wide {
  grid-column: 1 / -1;
}

.placeholder-card {
  color: var(--text-secondary);
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-button {
  min-width: 110px;
  margin: 0;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 600;
}

.action-button--muted {
  border-color: rgba(148, 163, 184, 0.18);
  color: rgba(206, 216, 230, 0.58);
  background: rgba(148, 163, 184, 0.06);
  box-shadow: none;
}

@media (max-width: 768px) {
  .section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
