<template>
  <div class="dashboard-view">
    <div class="metrics-grid">
      <div class="metric-card">
        <small>充电站总数</small>
        <div class="metric-value">
          <strong>{{ stationStats?.totalCount ?? '—' }}</strong>
        </div>
      </div>

      <div class="metric-card">
        <small>充电桩总数</small>
        <div class="metric-value">
          <strong>{{ pileStats?.totalCount ?? '—' }}</strong>
          <span>快充 {{ pileStats?.fastCount ?? 0 }} / 慢充 {{ pileStats?.slowCount ?? 0 }}</span>
        </div>
      </div>

      <div class="metric-card">
        <small>股东主档</small>
        <div class="metric-value">
          <strong>{{ shareholderStats?.totalCount ?? '—' }}</strong>
          <span>启用 {{ shareholderStats?.enabledCount ?? 0 }} / 停用 {{ shareholderStats?.disabledCount ?? 0 }}</span>
        </div>
      </div>
    </div>

    <div class="dual-grid">
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
              <span :style="getStationBarStyle(statusCount)"></span>
            </div>
            <strong>{{ statusCount.count }}</strong>
          </div>
          <div v-if="!stationStats || stationStats.statusCounts.length === 0" class="empty-hint">
            暂无状态数据
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-head">
          <h3>待办事项</h3>
        </div>
        <div class="empty-hint">
          待开发
        </div>
      </div>
    </div>

    <div class="triple-grid">
      <div class="table-wrap">
        <div class="section-head">
          <h3>最近新增站点</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>站点</th>
              <th>平台</th>
              <th>状态</th>
              <th>联系人</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="station in recentStations" :key="station.id" @click="navigateTo(`/stations/${station.id}`)">
              <td>
                <div class="station-cell">
                  <strong>{{ station.stationName }}</strong>
                  <small>{{ station.stationCode }}</small>
                </div>
              </td>
              <td>{{ station.platformName || '—' }}</td>
              <td>{{ station.statusName || '—' }}</td>
              <td>{{ station.contactName || '—' }}</td>
            </tr>
            <tr v-if="recentStations.length === 0">
              <td colspan="4" class="empty-hint">暂无新增站点</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="timeline-card">
        <div class="section-head">
          <h3>最近关键操作</h3>
        </div>
        <div class="timeline-list">
          <div v-for="event in recentLogs" :key="event.id" class="timeline-item">
            <div class="timeline-time">{{ formatTime(event.operatedAt) }}</div>
            <div class="timeline-content">
              <strong>{{ getOperationDescription(event) }}</strong>
            </div>
          </div>
          <div v-if="recentLogs.length === 0" class="empty-hint">
            暂无操作记录
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-head">
          <h3>快捷入口</h3>
        </div>
        <div class="mini-list">
          <div class="mini-item" @click="navigateTo('/stations?action=create')">
            <div>
              <strong>新增充电站</strong>
              <p>新建站点基础信息并进入详情继续维护。</p>
            </div>
            <span class="tag tag-info">主流程</span>
          </div>
          <div class="mini-item" @click="navigateTo('/logs')">
            <div>
              <strong>查看日志中心</strong>
              <p>按时间、操作人、对象类型快速过滤审计事件。</p>
            </div>
            <span class="tag tag-success">审计</span>
          </div>
          <div class="mini-item" @click="navigateTo('/dictionary')">
            <div>
              <strong>系统字典</strong>
              <p>统一维护平台、充电站状态、充电桩状态等基础字典。</p>
            </div>
            <span class="tag tag-neutral">系统</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStationStatistics, getRecentStations, type StationStatistics, type RecentStation } from '@/api/station'
import { getPileStatistics, type PileStatistics } from '@/api/pile'
import { getShareholderStatistics, type ShareholderStatistics } from '@/api/shareholder'
import { queryOperationLogs, type TimelineEvent } from '@/api/operation-log'

const router = useRouter()

const stationStats = ref<StationStatistics | null>(null)
const pileStats = ref<PileStatistics | null>(null)
const shareholderStats = ref<ShareholderStatistics | null>(null)
const recentLogs = ref<TimelineEvent[]>([])
const recentStations = ref<RecentStation[]>([])

const loadStatistics = async () => {
  try {
    const [station, pile, shareholder] = await Promise.all([
      getStationStatistics(),
      getPileStatistics(),
      getShareholderStatistics(),
    ])
    stationStats.value = station
    pileStats.value = pile
    shareholderStats.value = shareholder
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentLogs = async () => {
  try {
    const result = await queryOperationLogs({ page: 1, size: 10 })
    recentLogs.value = result.records
  } catch (error) {
    console.error('加载最近操作记录失败:', error)
  }
}

const loadRecentStations = async () => {
  try {
    recentStations.value = await getRecentStations()
  } catch (error) {
    console.error('加载最近新增站点失败:', error)
  }
}

const getStationBarStyle = (statusCount: StationStatistics['statusCounts'][0]) => {
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

const navigateTo = (path: string) => {
  router.push(path)
}

const formatTime = (isoTime: string) => {
  const date = new Date(isoTime)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 7) return `${days} 天前`
  return date.toLocaleDateString('zh-CN')
}

const getOperationDescription = (event: TimelineEvent) => {
  const typeMap: Record<string, (e: TimelineEvent) => string> = {
    CREATE_STATION: (e) => `${e.operatorAccount} 新增了充电站「${e.targetName}」`,
    UPDATE_STATION: (e) => `${e.operatorAccount} 更新了充电站「${e.targetName}」`,
    CHANGE_STATION_STATUS: (e) => `${e.operatorAccount} 变更了充电站「${e.targetName}」的状态`,
    CREATE_PILE: (e) => `${e.operatorAccount} 在「${e.targetName}」新增了充电桩`,
    UPDATE_PILE: (e) => `${e.operatorAccount} 更新了「${e.targetName}」的充电桩`,
    CHANGE_PILE_STATUS: (e) => `${e.operatorAccount} 变更了「${e.targetName}」充电桩的状态`,
    CREATE_SHAREHOLDER: (e) => `${e.operatorAccount} 新增了股东「${e.targetName}」`,
    UPDATE_SHAREHOLDER: (e) => `${e.operatorAccount} 更新了股东「${e.targetName}」`,
    CHANGE_SHAREHOLDER_STATUS: (e) => `${e.operatorAccount} 变更了股东「${e.targetName}」的状态`,
    LINK_SHAREHOLDER: (e) => `${e.operatorAccount} 在「${e.targetName}」关联了股东`,
    UNLINK_SHAREHOLDER: (e) => `${e.operatorAccount} 在「${e.targetName}」解除了股东关联`,
    ADJUST_EQUITY: (e) => `${e.operatorAccount} 调整了「${e.targetName}」的股权占比`,
  }
  
  const formatter = typeMap[event.operationType]
  return formatter ? formatter(event) : `${event.operatorAccount} 操作了 ${event.targetName}`
}

onMounted(() => {
  loadStatistics()
  loadRecentLogs()
  loadRecentStations()
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

.dual-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.triple-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 18px;
}

.status-distribution-card,
.section-card,
.timeline-card,
.table-wrap {
  border: 1px solid var(--line-soft);
  border-radius: 24px;
  padding: 24px;
  background: var(--bg-panel);
}

.section-head {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.mini-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-soft);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mini-item:hover {
  background: var(--bg-hover);
  transform: translateX(2px);
}

.mini-item strong {
  display: block;
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.mini-item p {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

.tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.tag-info {
  background: rgba(57, 200, 255, 0.15);
  color: #39c8ff;
}

.tag-success {
  background: rgba(80, 227, 194, 0.15);
  color: #50e3c2;
}

.tag-neutral {
  background: var(--bg-soft);
  color: var(--text-secondary);
}

.table-wrap table {
  width: 100%;
  border-collapse: collapse;
}

.table-wrap thead th {
  text-align: left;
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--line-soft);
}

.table-wrap tbody tr {
  cursor: pointer;
  transition: background 0.2s ease;
}

.table-wrap tbody tr:hover {
  background: var(--bg-soft);
}

.table-wrap tbody td {
  padding: 14px 16px;
  font-size: 13px;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(150, 206, 255, 0.08);
}

.table-wrap tbody tr:last-child td {
  border-bottom: none;
}

.station-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.station-cell strong {
  font-size: 14px;
  color: var(--text-primary);
}

.station-cell small {
  font-size: 12px;
  color: var(--text-muted);
}

.table-wrap .empty-hint {
  text-align: center;
  padding: 24px;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line-soft);
}

.timeline-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.timeline-time {
  flex-shrink: 0;
  width: 80px;
  font-size: 12px;
  color: var(--text-muted);
  padding-top: 2px;
}

.timeline-content {
  flex: 1;
}

.timeline-content strong {
  display: block;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
}

.empty-hint {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
}
</style>

