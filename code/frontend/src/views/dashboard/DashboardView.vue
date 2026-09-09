<template>
  <div class="dashboard-view">
    <div class="metrics-grid">
      <div class="metric-card">
        <small>充电站总数</small>
        <div class="metric-value">
          <strong>{{ stationStats?.totalCount ?? '—' }}</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStationStatistics, type StationStatistics } from '@/api/station'

const stationStats = ref<StationStatistics | null>(null)

const loadStatistics = async () => {
  try {
    stationStats.value = await getStationStatistics()
  } catch (error) {
    console.error('加载充电站统计失败:', error)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.dashboard-view {
  padding: 0;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.metric-card {
  border: 1px solid var(--line-soft);
  border-radius: 24px;
  padding: 22px;
  background: var(--bg-panel);
  position: relative;
  overflow: hidden;
}

.metric-card::after {
  content: '';
  position: absolute;
  width: 140px;
  height: 140px;
  right: -30px;
  top: -30px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(57, 200, 255, 0.18), transparent 68%);
}

.metric-card small {
  display: block;
  margin-bottom: 12px;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 12px;
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.metric-value strong {
  font-size: 40px;
  letter-spacing: -0.06em;
  line-height: 1;
  color: var(--text-primary);
}

.metric-value span {
  color: var(--text-secondary);
  font-size: 13px;
}

.metric-footer {
  margin-top: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  position: relative;
  z-index: 1;
}
</style>

