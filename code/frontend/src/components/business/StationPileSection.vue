<template>
  <section id="section-piles" class="detail-section pile-section">
    <div class="detail-section__head">
      <h4>充电桩管理</h4>
      <el-button class="action-button" type="primary" @click="openCreateDialog">新增充电桩</el-button>
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
          <el-button link type="primary" @click="openEditDialog(pile)">编辑</el-button>
          <el-button link type="primary" @click="openStatusDialog(pile)">改状态</el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      width="520px"
      class="pile-create-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <div>
            <h3>{{ dialogMode === 'create' ? '新增充电桩' : '编辑充电桩' }}</h3>
            <p>所属站点ID：{{ stationId }}</p>
          </div>
          <el-button text @click="closePileDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>桩编号</label>
          <el-input :model-value="nextPileCode" disabled />
          <p class="field-hint">桩编号由系统自动生成，保存后以实际编号为准。</p>
        </div>

        <div class="form-field">
          <label>充电类型</label>
          <el-select v-model="pileForm.chargeType" placeholder="请选择充电类型">
            <el-option
              v-for="option in chargeTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <p v-if="pileFormError" class="field-error">{{ pileFormError }}</p>
        </div>

        <div class="form-field">
          <label>充电枪数量</label>
          <el-input-number
            v-model="pileForm.gunCount"
            :min="1"
            :max="64"
            :step="1"
            step-strictly
            controls-position="right"
          />
          <p class="field-hint">
            {{ dialogMode === 'create' ? '新增后默认状态为运营中。' : '桩状态不在编辑中修改，请使用状态切换操作。' }}
          </p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button class="action-button action-button--secondary" @click="closePileDialog">取消</el-button>
          <el-button class="action-button" type="primary" :loading="dialogSaving" @click="submitPileForm">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="statusDialogVisible"
      width="520px"
      class="pile-status-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <div>
            <h3>修改充电桩状态</h3>
            <p>{{ statusTargetPile?.pileCode }} · 当前状态：{{ statusTargetPile?.pileStatusName || '--' }}</p>
          </div>
          <el-button text @click="closeStatusDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>新状态</label>
          <el-select v-model="statusForm.pileStatusItemId" placeholder="请选择新状态">
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
            placeholder="请输入状态变更备注，最多500字符"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button class="action-button action-button--secondary" @click="closeStatusDialog">取消</el-button>
          <el-button class="action-button" type="primary" :loading="statusSaving" @click="submitStatusForm">
            保存状态
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'

import {
  createStationPile,
  getStationNextPileCode,
  getStationPileStatusOptions,
  getStationPiles,
  summarizePiles,
  updateStationPile,
  updateStationPileStatus,
  type PileListItem,
  type PileStatusOption,
} from '@/api/pile'
import { CHARGE_TYPE_OPTIONS } from '@/constants/enums/charge-type'

const props = defineProps<{ stationId: number }>()

const emit = defineEmits<{ loaded: [summary: { pileCount: number; gunCount: number }] }>()

const loading = ref(true)
const errorMessage = ref('')
const piles = ref<PileListItem[]>([])

const dialogVisible = ref(false)
const dialogSaving = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingPile = ref<PileListItem | null>(null)
const nextPileCode = ref('--')
const pileFormError = ref('')
const pileForm = reactive({
  chargeType: 'FAST' as string,
  gunCount: 2 as number | null,
})
const chargeTypeOptions = CHARGE_TYPE_OPTIONS

const statusDialogVisible = ref(false)
const statusSaving = ref(false)
const statusOptions = ref<PileStatusOption[]>([])
const statusTargetPile = ref<PileListItem | null>(null)
const statusFormError = ref('')
const statusForm = reactive({
  pileStatusItemId: null as number | null,
  remark: '',
})

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

async function openCreateDialog() {
  dialogMode.value = 'create'
  editingPile.value = null
  pileForm.chargeType = 'FAST'
  pileForm.gunCount = 2
  pileFormError.value = ''
  nextPileCode.value = '--'
  dialogVisible.value = true

  // 预览编号仅作展示引导，加载失败不阻塞录入，实际编号以保存时生成为准
  try {
    nextPileCode.value = await getStationNextPileCode(props.stationId)
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '桩编号预览加载失败，不影响保存'))
  }
}

function openEditDialog(pile: PileListItem) {
  dialogMode.value = 'edit'
  editingPile.value = pile
  pileForm.chargeType = pile.chargeType
  pileForm.gunCount = pile.gunCount
  pileFormError.value = ''
  nextPileCode.value = pile.pileCode
  dialogVisible.value = true
}

function closePileDialog() {
  if (dialogSaving.value) {
    return
  }
  dialogVisible.value = false
}

async function submitPileForm() {
  if (!pileForm.chargeType) {
    pileFormError.value = '请选择充电类型'
    return
  }
  if (pileForm.gunCount == null || pileForm.gunCount < 1) {
    pileFormError.value = '充电枪数量至少为1'
    return
  }

  dialogSaving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createStationPile(props.stationId, {
        chargeType: pileForm.chargeType,
        gunCount: pileForm.gunCount,
      })
      ElMessage.success('新增充电桩成功')
    } else if (editingPile.value) {
      await updateStationPile(props.stationId, editingPile.value.id, {
        chargeType: pileForm.chargeType,
        gunCount: pileForm.gunCount,
      })
      ElMessage.success('更新充电桩成功')
    }
    dialogVisible.value = false
    await refresh()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, dialogMode.value === 'create' ? '新增充电桩失败，请稍后重试' : '更新充电桩失败，请稍后重试'))
  } finally {
    dialogSaving.value = false
  }
}

async function openStatusDialog(pile: PileListItem) {
  statusTargetPile.value = pile
  statusFormError.value = ''
  statusForm.remark = ''
  // 默认预选当前状态：列表行只有状态名称，按名称匹配选项，匹配不到则留空由用户选择
  statusForm.pileStatusItemId =
    statusOptions.value.find(option => option.itemName === pile.pileStatusName)?.itemId ?? null
  statusDialogVisible.value = true

  if (statusOptions.value.length > 0) {
    return
  }
  try {
    statusOptions.value = await getStationPileStatusOptions(props.stationId)
  } catch (error) {
    // 选项加载失败时表单不可用，参照站点状态弹窗口径直接关闭并提示
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
  if (!statusForm.pileStatusItemId) {
    statusFormError.value = '请选择新状态'
    return
  }
  if (!statusTargetPile.value) {
    return
  }

  statusSaving.value = true
  try {
    await updateStationPileStatus(props.stationId, statusTargetPile.value.id, {
      pileStatusItemId: statusForm.pileStatusItemId,
      remark: statusForm.remark.trim(),
    })
    ElMessage.success('充电桩状态切换成功')
    statusDialogVisible.value = false
    await refresh()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '充电桩状态切换失败，请稍后重试'))
  } finally {
    statusSaving.value = false
  }
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

.field-hint {
  margin: 0;
  color: var(--text-muted);
  font-size: 12px;
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
