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

    <div class="status-distribution-card">
      <div class="section-head">
        <h3>站点状态分布</h3>
      </div>
      <div class="status-rings">
        <div
          v-for="statusCount in stationStats?.statusCounts ?? []"
          :key="statusCount.statusItemId"
          class="status-line"
        >
          <span>{{ statusCount.statusItemName }}</span>
          <div class="bar">
            <span :style="getBarStyle(statusCount)"></span>
          </div>
          <strong>{{ statusCount.count }}</strong>
        </div>
        <div v-if="!stationStats || stationStats.statusCounts.length === 0" class="empty-hint">
          暂无状态数据
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getStationStatistics, type StationStatistics } from '@/api/station'

const stationStats = ref<StationStatistics | null>(null)

const loadStatistics = async () => {
  try {
    stationStats.value = await getStationStatistics()
  } catch (error) {
    console.error('加载充电站统计失败:', error)
  }
}

const getBarStyle = (statusCount: StationStatistics['statusCounts'][0]) => {
  const total = stationStats.value?.totalCount ?? 0
  const percentage = total > 0 ? (statusCount.count / total) * 100 : 0
  const color = getStatusColor(statusCount.statusItemName)
  return {
    width: `${percentage}%`,
    background: color,
  }
}

const getStatusColor = (statusName: string): string => {
  const colorMap: Record<string, string> = {
    '运营中': 'linear-gradient(90deg, #39c8ff, #50e3c2)',
    '建设中': 'linear-gradient(90deg, #64a9ff, #39c8ff)',
    '签合同': 'linear-gradient(90deg, #ffbf66, #ffd98a)',
    '运营异常': 'linear-gradient(90deg, #ff7d7d, #ffb26b)',
    '停用': 'linear-gradient(90deg, #9aa9b8, #c3d0da)',
  }
  return colorMap[statusName] || 'linear-gradient(90deg, #64a9ff, #39c8ff)'
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

.status-distribution-card {
  border: 1px solid var(--line-soft);
  border-radius: 24px;
  padding: 24px;
  background: var(--bg-panel);
}

.section-head {
  margin-bottom: 20px;
}

.section-head h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.status-rings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-line {
  display: grid;
  grid-template-columns: 90px 1fr 60px;
  align-items: center;
  gap: 14px;
}

.status-line span:first-child {
  font-size: 14px;
  color: var(--text-secondary);
}

.bar {
  height: 28px;
  background: var(--bg-soft);
  border-radius: 14px;
  overflow: hidden;
  position: relative;
}

.bar span {
  display: block;
  height: 100%;
  border-radius: 14px;
  background: linear-gradient(90deg, #39c8ff, #50e3c2);
  transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.status-line strong {
  font-size: 18px;
  text-align: right;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.empty-hint {
  padding: 24px;
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
}
</style>

