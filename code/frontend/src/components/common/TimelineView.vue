<template>
  <div class="timeline-view">
    <div v-if="loading" class="timeline-loading">加载中...</div>
    <div v-else-if="events.length === 0" class="timeline-empty">暂无历史记录</div>
    <div v-else class="timeline-list">
      <div v-for="event in events" :key="event.id" class="timeline-item">
        <div class="timeline-meta">
          <span>{{ formatTime(event.operatedAt) }}</span>
          <span>{{ event.operatorAccount }}</span>
          <span :class="['timeline-tag', event.operationType.toLowerCase()]">
            {{ formatOperationType(event.operationType) }}
          </span>
        </div>
        <div class="timeline-title">{{ resolveTitle(event) }}</div>
        <div v-if="event.remark" class="timeline-desc">备注：{{ event.remark }}</div>
        <div class="timeline-body">
          <template v-if="isEquityAdjust(event)">
            <EquityAdjustChanges :before-state="parseState(event.beforeState)" :after-state="parseState(event.afterState)" />
          </template>
          <template v-else-if="isShareholdingCreate(event)">
            <div class="timeline-change-row">
              <span class="timeline-change-label">{{ getShareholdingLabel() }}：</span>
              <span class="timeline-change-new">{{ formatShareholdingLine(parseState(event.afterState)) }}</span>
            </div>
          </template>
          <template v-else-if="isShareholdingExit(event)">
            <div class="timeline-change-row">
              <span class="timeline-change-label">{{ getShareholdingLabel() }}：</span>
              <span class="timeline-change-old">{{ formatShareholdingLine(parseState(event.beforeState)) }}</span>
              <span class="timeline-change-arrow">→</span>
              <span class="timeline-change-new">已退出</span>
            </div>
          </template>
          <template v-else-if="isStatusChange(event)">
            <div class="timeline-change-row">
              <span class="timeline-change-label">状态：</span>
              <span class="timeline-change-old">{{ formatStatusValue(event, 'before') }}</span>
              <span class="timeline-change-arrow">→</span>
              <span class="timeline-change-new">{{ formatStatusValue(event, 'after') }}</span>
            </div>
          </template>
          <template v-else>
            <div
              v-for="change in diffChanges(event)"
              :key="change.field"
              class="timeline-change-row"
            >
              <span class="timeline-change-label">{{ change.label }}：</span>
              <span class="timeline-change-old">{{ change.oldValue || '无' }}</span>
              <span class="timeline-change-arrow">→</span>
              <span class="timeline-change-new">{{ change.newValue || '无' }}</span>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TimelineEvent } from '@/api/operation-log'
import EquityAdjustChanges from './EquityAdjustChanges.vue'

interface Props {
  events: TimelineEvent[]
  loading: boolean
  viewType?: 'station' | 'shareholder'
}

const props = withDefaults(defineProps<Props>(), {
  viewType: 'station'
})

interface StateRecord {
  [key: string]: unknown
}

interface FieldChange {
  field: string
  label: string
  oldValue: string
  newValue: string
}

const stationFieldLabels: Record<string, string> = {
  stationName: '充电站名称',
  address: '地址',
  contactName: '联系人',
  contactPhone: '联系电话',
  meterAccountNumber: '电表户号',
  platformStationCode: '平台站点编号',
  platformItemId: '所在平台',
  stationStatusItemId: '当前状态',
}

const pileFieldLabels: Record<string, string> = {
  pileCode: '充电桩编号',
  chargeType: '充电类型',
  gunCount: '充电枪数量',
  pileStatusItemId: '充电桩状态',
}

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
    ENABLE: '启用',
    DISABLE: '停用',
    RESET_PASSWORD: '重置密码',
    EQUITY_ADJUST: '股权调整',
    EXIT: '退出',
  }
  return typeMap[type] || type
}

function resolveTitle(event: TimelineEvent): string {
  let title = ''
  
  if (event.targetType === 'SHAREHOLDING') {
    if (event.operationType === 'CREATE') title = props.viewType === 'shareholder' ? '关联充电站' : '关联股东'
    else if (event.operationType === 'EXIT') title = props.viewType === 'shareholder' ? '退出充电站' : '股东退出'
    else if (event.operationType === 'EQUITY_ADJUST') title = '股权调整'
  } else if (event.targetType === 'PILE') {
    if (event.operationType === 'CREATE') title = '新增充电桩'
    else if (event.operationType === 'UPDATE') title = '修改充电桩'
    else if (event.operationType === 'STATUS_CHANGE') title = '修改充电桩状态'
  } else if (event.targetType === 'STATION') {
    if (event.operationType === 'CREATE') title = '新增充电站'
    else if (event.operationType === 'UPDATE') title = '修改基础信息'
    else if (event.operationType === 'STATUS_CHANGE') title = '修改站点状态'
  } else if (event.targetType === 'SHAREHOLDER') {
    if (event.operationType === 'CREATE') title = '新增股东'
    else if (event.operationType === 'UPDATE') title = '修改基础信息'
    else if (event.operationType === 'STATUS_CHANGE') title = '状态变更'
  }
  
  // 在股东视角下，为涉及充电站的操作添加充电站名称前缀
  if (props.viewType === 'shareholder' && event.targetType === 'SHAREHOLDING') {
    const stationName = extractStationName(event)
    if (stationName) {
      title = `【${stationName}】${title}`
    }
  }
  
  return title || event.targetName
}

function extractStationName(event: TimelineEvent): string {
  try {
    const afterState = parseState(event.afterState) as StateRecord
    const beforeState = parseState(event.beforeState) as StateRecord
    const afterRecord = Array.isArray(afterState) ? afterState[0] : afterState
    const beforeRecord = Array.isArray(beforeState) ? beforeState[0] : beforeState
    const state = (afterRecord && Object.keys(afterRecord).length > 0) ? afterRecord : beforeRecord
    return String(state?.stationName || '')
  } catch {
    return ''
  }
}

function parseState(state: string | null): StateRecord | StateRecord[] {
  if (!state) return {}
  try {
    return JSON.parse(state)
  } catch {
    return {}
  }
}

function formatValue(field: string, value: unknown): string {
  if (value === null || value === undefined) return ''
  if (field === 'chargeType') {
    const map: Record<string, string> = { DC: '直流', AC: '交流' }
    return map[String(value)] || String(value)
  }
  return String(value)
}

function isStatusChange(event: TimelineEvent): boolean {
  return event.operationType === 'STATUS_CHANGE'
}

function isEquityAdjust(event: TimelineEvent): boolean {
  return event.targetType === 'SHAREHOLDING' && event.operationType === 'EQUITY_ADJUST'
}

function isShareholdingCreate(event: TimelineEvent): boolean {
  return event.targetType === 'SHAREHOLDING' && event.operationType === 'CREATE'
}

function isShareholdingExit(event: TimelineEvent): boolean {
  return event.targetType === 'SHAREHOLDING' && event.operationType === 'EXIT'
}

function getFieldLabels(event: TimelineEvent): Record<string, string> {
  if (event.targetType === 'PILE') return pileFieldLabels
  return stationFieldLabels
}

function diffChanges(event: TimelineEvent): FieldChange[] {
  const labels = getFieldLabels(event)
  const before = parseState(event.beforeState) as StateRecord
  const after = parseState(event.afterState) as StateRecord
  const changes: FieldChange[] = []

  for (const [field, label] of Object.entries(labels)) {
    const oldValue = formatValue(field, before[field])
    const newValue = formatValue(field, after[field])
    if (oldValue !== newValue) {
      changes.push({ field, label, oldValue, newValue })
    }
  }

  return changes
}

function formatShareholdingLine(state: StateRecord | StateRecord[]): string {
  const record = Array.isArray(state) ? state[0] : state
  if (!record) return '--'
  
  if (props.viewType === 'shareholder') {
    const stationName = String(record.stationName || record.stationId || '--')
    const percentage = record.ownershipPercentage ?? '--'
    return `${stationName} 占股 ${percentage}%`
  } else {
    const shareholderName = String(record.shareholderName || record.shareholderId || '--')
    const percentage = record.ownershipPercentage ?? '--'
    return `${shareholderName} 占股 ${percentage}%`
  }
}

function getShareholdingLabel(): string {
  return props.viewType === 'shareholder' ? '充电站' : '股东'
}

function formatStatusValue(event: TimelineEvent, direction: 'before' | 'after'): string {
  const state = parseState(direction === 'before' ? event.beforeState : event.afterState) as StateRecord
  if (!state) return '--'
  const field = event.targetType === 'PILE' ? 'pileStatusItemId' : 'stationStatusItemId'
  return String(state[field] ?? '--')
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
  gap: 20px;
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
  bottom: -22px;
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
  background: #409eff;
  box-shadow: 0 0 0 6px rgba(64, 158, 255, 0.08);
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

.timeline-tag {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.08);
}

.timeline-tag.create {
  background: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}

.timeline-tag.update {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.timeline-tag.status_change {
  background: rgba(230, 162, 60, 0.12);
  color: #e6a23c;
}

.timeline-tag.equity_adjust {
  background: rgba(156, 39, 176, 0.12);
  color: #9c27b0;
}

.timeline-tag.exit,
.timeline-tag.delete {
  background: rgba(245, 108, 108, 0.12);
  color: #f56c6c;
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
  margin-bottom: 8px;
}

.timeline-body {
  display: grid;
  gap: 6px;
}

.timeline-change-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
  line-height: 1.6;
}

.timeline-change-text {
  color: var(--text-primary);
  white-space: nowrap;
}

.timeline-change-label {
  color: var(--text-secondary);
  white-space: nowrap;
}

.timeline-change-old {
  color: var(--text-muted);
  text-decoration: line-through;
}

.timeline-change-arrow {
  color: var(--text-muted);
}

.timeline-change-new {
  color: #409eff;
  font-weight: 400;
}
</style>
