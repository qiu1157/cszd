<template>
  <div class="equity-adjust-changes">
    <div
      v-for="item in mergedItems"
      :key="item.shareholderId"
      class="timeline-change-row"
    >
      <span class="timeline-change-text">股东 {{ item.shareholderName }}</span>
      <span class="timeline-change-label">：</span>
      <span class="timeline-change-old">{{ item.beforePercentage !== null ? item.beforePercentage + '%' : '--' }}</span>
      <span class="timeline-change-arrow">→</span>
      <span class="timeline-change-new">{{ item.afterPercentage !== null ? item.afterPercentage + '%' : '已退出' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  beforeState: unknown
  afterState: unknown
}

interface SnapshotItem {
  shareholdingId?: number
  shareholderId: number
  shareholderName: string
  ownershipPercentage: number | null
}

const props = defineProps<Props>()

function parseSnapshot(state: unknown): SnapshotItem[] {
  if (!state) return []
  if (!Array.isArray(state)) return []
  return state.map((item: unknown) => ({
    shareholdingId: (item as SnapshotItem).shareholdingId,
    shareholderId: (item as SnapshotItem).shareholderId || 0,
    shareholderName: String((item as SnapshotItem).shareholderName || (item as SnapshotItem).shareholderId || '--'),
    ownershipPercentage: (item as SnapshotItem).ownershipPercentage ?? null,
  }))
}

const mergedItems = computed(() => {
  const beforeItems = parseSnapshot(props.beforeState)
  const afterItems = parseSnapshot(props.afterState)

  const beforeMap = new Map<number, SnapshotItem>()
  beforeItems.forEach(item => beforeMap.set(item.shareholderId, item))

  const afterMap = new Map<number, SnapshotItem>()
  afterItems.forEach(item => afterMap.set(item.shareholderId, item))

  // 合并所有股东，按调整后名单优先，保持调整前不在调整后列表中的也展示
  const allIds = new Set([...beforeMap.keys(), ...afterMap.keys()])
  return Array.from(allIds).map(id => {
    const before = beforeMap.get(id)
    const after = afterMap.get(id)
    return {
      shareholderId: id,
      shareholderName: after?.shareholderName || before?.shareholderName || String(id),
      beforePercentage: before?.ownershipPercentage ?? null,
      afterPercentage: after?.ownershipPercentage ?? null,
    }
  })
})
</script>

<style scoped lang="scss">
.equity-adjust-changes {
  display: grid;
  gap: 6px;
}
</style>
