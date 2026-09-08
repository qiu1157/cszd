<template>
  <div class="timeline-view">
    <div v-if="loading" class="timeline-loading">加载中...</div>
    <div v-else-if="events.length === 0" class="timeline-empty">暂无历史记录</div>
    <div v-else class="timeline-list">
      <div v-for="event in events" :key="event.id" class="timeline-item">
        <div class="timeline-meta">
          <span>{{ formatTime(event.operatedAt) }}</span>
          <span>{{ event.operatorAccount }}</span>
          <span>{{ formatOperationType(event.operationType) }}</span>
        </div>
        <div class="timeline-title">{{ event.targetName }}</div>
        <div v-if="event.remark" class="timeline-desc">{{ event.remark }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TimelineEvent } from '@/api/operation-log'

defineProps<{
  events: TimelineEvent[]
  loading: boolean
}>()

function formatTime(time: string): string {
  if (!time) return '--'
  try {
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch {
    return time
  }
}

function formatOperationType(type: string): string {
  const typeMap: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    STATUS_CHANGE: '状态变更',
    EQUITY_ADJUSTMENT: '股权调整',
  }
  return typeMap[type] || type
}
</script>

<style scoped lang="scss">
.timeline-view {
  min-height: 120px;
}

.timeline-loading,
.timeline-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--text-secondary);
  font-size: 14px;
}

.timeline-list {
  display: grid;
  gap: 16px;
}

.timeline-item {
  position: relative;
  padding: 0 0 0 26px;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 5px;
  bottom: -18px;
  width: 1px;
  background: rgba(255, 255, 255, 0.08);
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-item::after {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--brand-accent);
  box-shadow: 0 0 0 6px rgba(31, 242, 178, 0.08);
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-muted);
  font-size: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.timeline-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.timeline-desc {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 13px;
}
</style>
