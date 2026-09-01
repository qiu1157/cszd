<template>
  <section id="section-piles" class="detail-section pile-section">
    <div class="detail-section__head">
      <h4>充电桩管理</h4>
      <el-button class="action-button" type="primary" @click="handleCreateClick">新增充电桩</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="3" animated />

    <div v-else-if="errorMessage" class="pile-error">
      <p>{{ errorMessage }}</p>
      <el-button class="action-button action-button--secondary" @click="refresh">重试</el-button>
    </div>

    <div v-else-if="piles.length === 0" class="empty-card">
      暂无充电桩，点击右上角“新增充电桩”开始维护。
    </div>

    <div v-else class="mini-list">
      <div v-for="pile in piles" :key="pile.id" class="mini-item">
        <div class="mini-item__info">
          <strong>{{ pile.pileCode }}</strong>
          <p>{{ pile.chargeTypeName || '--' }} · {{ pile.gunCount }} 把枪</p>
        </div>
        <div class="mini-item__side">
          <el-tag :type="resolvePileStatusType(pile.pileStatusName)" effect="dark" round>
            {{ pile.pileStatusName || '--' }}
          </el-tag>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'

import { getStationPiles, summarizePiles, type PileListItem } from '@/api/pile'

const props = defineProps<{ stationId: number }>()

const emit = defineEmits<{ loaded: [summary: { pileCount: number; gunCount: number }] }>()

const loading = ref(true)
const errorMessage = ref('')
const piles = ref<PileListItem[]>([])

async function refresh() {
  loading.value = true
  errorMessage.value = ''
  try {
    piles.value = await getStationPiles(props.stationId)
    // 概览卡需要桩/枪规模统计，列表加载成功后同步给父组件
    emit('loaded', summarizePiles(piles.value))
  } catch (error) {
    errorMessage.value = resolveApiErrorMessage(error, '充电桩列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleCreateClick() {
  // 新增弹窗在下一个任务交付，先给出明确引导避免死按钮
  ElMessage.info('新增充电桩功能将在下一个任务交付')
}

function resolvePileStatusType(statusName: string | null) {
  switch (statusName) {
    case '运营中':
      return 'success'
    case '运营异常':
      return 'danger'
    case '停用':
      return 'info'
    default:
      return 'info'
  }
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
  void refresh()
})

defineExpose({ refresh })
</script>

<style scoped lang="scss">
.pile-section {
  display: grid;
  gap: 12px;
}

.pile-section .detail-section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mini-list {
  display: grid;
  gap: 10px;
}

.mini-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(150, 206, 255, 0.12);
  background: rgba(8, 15, 30, 0.32);
}

.mini-item__info strong {
  word-break: break-all;
}

.mini-item__info p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.empty-card {
  display: grid;
  place-items: center;
  min-height: 96px;
  padding: 16px;
  border-radius: 14px;
  border: 1px dashed rgba(150, 206, 255, 0.18);
  color: var(--text-secondary);
}

.pile-error {
  display: grid;
  justify-items: start;
  gap: 10px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid rgba(255, 125, 125, 0.25);
  background: rgba(40, 12, 16, 0.3);
}

.pile-error p {
  margin: 0;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .pile-section .detail-section__head {
    flex-direction: column;
    align-items: stretch;
  }

  .mini-item {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
