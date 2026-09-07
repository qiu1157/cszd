<template>
  <section id="section-shareholders" class="detail-section shareholder-section">
    <div class="detail-section__head">
      <h4>股东信息</h4>
      <div class="action-group">
        <el-button class="action-button action-button--secondary" @click="goToEquityAdjustment">股权调整</el-button>
        <el-button class="action-button" type="primary" @click="openLinkDialog">关联股东</el-button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="3" animated />

    <div v-else-if="errorMessage" class="shareholder-error">
      <p>{{ errorMessage }}</p>
      <el-button class="action-button action-button--secondary" @click="refresh">重试</el-button>
    </div>

    <div v-else-if="shareholdings.length === 0" class="empty-card">
      暂无持股中股东，点击右上角“关联股东”开始维护。
    </div>

    <div v-else class="mini-list">
      <div v-if="ownershipSumIncomplete" class="ownership-sum-warning">
        当前持股中合计 {{ activeSummary.activePercentageSum }}%，不等于 100%，请确认股权结构是否符合预期。
      </div>
      <div v-for="holding in shareholdings" :key="holding.id" class="mini-item">
        <div class="mini-item__info">
          <strong>{{ holding.shareholderName }}</strong>
          <p>
            联系人：{{ holding.contactName || '--' }} · 电话：{{ holding.contactPhone || '--' }} ·
            当前占比：{{ holding.ownershipPercentage }}%
          </p>
        </div>
        <div class="mini-item__side">
          <el-tag :type="resolveHoldingStatusType(holding.holdingStatus)" effect="dark" round>
            {{ holding.holdingStatusName }}
          </el-tag>
          <!-- 退股入口只对持股中关系开放：已退出是历史痕迹，再次操作无业务含义 -->
          <el-button
            v-if="holding.holdingStatus === 'ACTIVE'"
            class="action-button action-button--secondary"
            size="small"
            :loading="exitingId === holding.id"
            :disabled="exitingId != null"
            @click="requestExit(holding)"
          >
            退出
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="linkDialogVisible"
      width="520px"
      class="shareholder-link-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <div>
            <h3>关联股东</h3>
            <p>所属站点ID：{{ stationId }}</p>
          </div>
          <el-button text @click="closeLinkDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>选择股东（主档）</label>
          <el-select
            v-model="linkForm.shareholderId"
            placeholder="请选择股东"
            filterable
          >
            <el-option
              v-for="option in shareholderOptions"
              :key="option.id"
              :label="formatShareholderOption(option)"
              :value="option.id"
            />
          </el-select>
          <p class="field-hint">仅展示启用状态的股东，停用股东不能新建持股关系。</p>
        </div>

        <div class="form-field">
          <label>占股比例（%）</label>
          <el-input-number
            v-model="linkForm.ownershipPercentage"
            :min="0"
            :max="100"
            :step="1"
            :precision="2"
            controls-position="right"
          />
          <p class="field-hint">持股中占比合计不等于 100% 时，保存前会提示确认。</p>
          <p v-if="linkFormError" class="field-error">{{ linkFormError }}</p>
        </div>

        <div class="form-field">
          <label>关系状态</label>
          <el-select v-model="linkForm.holdingStatus" placeholder="请选择关系状态">
            <el-option
              v-for="option in holdingStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <p class="field-hint">补录历史关系可选“已退出”，不计入占比合计。</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button class="action-button action-button--secondary" @click="closeLinkDialog">取消</el-button>
          <el-button class="action-button" type="primary" :loading="linkSaving" @click="submitLinkForm">
            确认关联
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getShareholderLinkOptions, type ShareholderLinkOption } from '@/api/shareholder'
import { buildStationEquityAdjustmentPath } from '@/router/navigation'
import {
  createStationShareholding,
  exitStationShareholding,
  getStationShareholdings,
  summarizeShareholdings,
  type StationShareholding,
} from '@/api/shareholding'

const props = defineProps<{ stationId: number }>()

const emit = defineEmits<{
  loaded: [summary: { activeShareholderCount: number; activePercentageSum: number }]
}>()

const router = useRouter()

const loading = ref(true)
const errorMessage = ref('')
const shareholdings = ref<StationShareholding[]>([])

const linkDialogVisible = ref(false)
const linkSaving = ref(false)
const linkFormError = ref('')
const shareholderOptions = ref<ShareholderLinkOption[]>([])
const linkForm = reactive({
  shareholderId: null as number | null,
  ownershipPercentage: null as number | null,
  holdingStatus: 'ACTIVE' as 'ACTIVE' | 'EXITED',
})

// 关系状态是应用层枚举口径（HoldingStatus），与充电桩的字典状态选项来源不同，前端直接维护常量
const holdingStatusOptions = [
  { value: 'ACTIVE' as const, label: '持股中' },
  { value: 'EXITED' as const, label: '已退出' },
]

// 列表警示与父组件摘要共用一份口径，数据变化时只需重算一次
const activeSummary = computed(() => summarizeShareholdings(shareholdings.value))

// spec 7.5 警示条件：仅当“存在持股中股东且合计≠100%”时警示；
// 站点无股东时走既有空态引导，不产生警示噪音
const ownershipSumIncomplete = computed(
  () =>
    activeSummary.value.activeShareholderCount > 0
    && activeSummary.value.activePercentageSum !== 100,
)

async function refresh() {
  loading.value = true
  errorMessage.value = ''
  try {
    // 前端防御性过滤：即使接口返回已退出记录也不应出现在详情分区
    shareholdings.value = (await getStationShareholdings(props.stationId, 'ACTIVE')).filter(
      holding => holding.holdingStatus === 'ACTIVE',
    )
    // 概览卡需要持股中股东数与占比合计，列表加载成功后同步给父组件
    emit('loaded', activeSummary.value)
  } catch (error) {
    errorMessage.value = resolveApiErrorMessage(error, '股东关系列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function openLinkDialog() {
  linkForm.shareholderId = null
  linkForm.ownershipPercentage = null
  linkForm.holdingStatus = 'ACTIVE'
  linkFormError.value = ''
  linkDialogVisible.value = true

  try {
    shareholderOptions.value = await getShareholderLinkOptions()
  } catch (error) {
    // 选项加载失败时表单不可用，参照充电桩状态弹窗口径关闭并提示
    linkDialogVisible.value = false
    ElMessage.error(resolveApiErrorMessage(error, '股东选项加载失败，请稍后重试'))
  }
}

function closeLinkDialog() {
  if (linkSaving.value) {
    return
  }
  linkDialogVisible.value = false
}

// 股权调整是独立页面的入口：从详情分区跳转后，用户可在独立页面集中维护多个股东占比
function goToEquityAdjustment() {
  void router.push(buildStationEquityAdjustmentPath(props.stationId))
}

// 退出中标记：既驱动行内按钮 loading，也让其他行的退出入口同时禁用，避免并发重复提交
const exitingId = ref<number | null>(null)

async function requestExit(holding: StationShareholding) {
  try {
    await ElMessageBox.confirm(
      `确认将「${holding.shareholderName}」标记为已退出吗？退出后不再计入占比合计，其他股东占比将按当前比例自动调整，历史关系将保留。`,
      '退股确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        confirmButtonClass: 'action-button action-button--primary',
        cancelButtonClass: 'action-button action-button--secondary',
        type: 'warning',
        closeOnClickModal: false,
        closeOnPressEscape: false,
      },
    )
  } catch {
    return
  }

  exitingId.value = holding.id
  try {
    await exitStationShareholding(props.stationId, holding.id)
    ElMessage.success('退股成功')
    // 列表与概览摘要都依赖后端新口径，统一走 refresh 而非本地改写状态
    await refresh()
  } catch (error) {
    // 幂等拦截、关系不存在等服务端口径直接透出，用户可据此理解后处理
    ElMessage.error(resolveApiErrorMessage(error, '退股失败，请稍后重试'))
  } finally {
    exitingId.value = null
  }
}

async function submitLinkForm() {
  if (!linkForm.shareholderId) {
    linkFormError.value = '请选择股东'
    return
  }
  if (linkForm.ownershipPercentage == null) {
    linkFormError.value = '请填写占股比例'
    return
  }

  // 变更口径：合计≠100% 仅软提示——预演提交后的持股中口径（复用取整逻辑），
  // 与列表警示同条件才弹确认；取消则直接中止，不进入保存态
  const projectedSummary = summarizeShareholdings([
    ...shareholdings.value,
    {
      id: 0,
      shareholderId: linkForm.shareholderId ?? 0,
      shareholderName: '',
      contactName: null,
      contactPhone: null,
      ownershipPercentage: linkForm.ownershipPercentage ?? 0,
      holdingStatus: linkForm.holdingStatus,
      holdingStatusName: linkForm.holdingStatus === 'ACTIVE' ? '持股中' : '已退出',
      effectiveAt: null,
    },
  ])
  if (projectedSummary.activeShareholderCount > 0 && projectedSummary.activePercentageSum !== 100) {
    try {
      await ElMessageBox.confirm('充电站持股数合计不等于100%，是否确认。', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        // 与 el-dialog 底部按钮同为胶囊体系，保持弹窗家族视觉一致
        confirmButtonClass: 'action-button action-button--primary',
        cancelButtonClass: 'action-button action-button--secondary',
        type: 'warning',
        closeOnClickModal: false,
        closeOnPressEscape: false,
      })
    } catch {
      return
    }
  }

  linkSaving.value = true
  try {
    await createStationShareholding(props.stationId, {
      shareholderId: linkForm.shareholderId,
      ownershipPercentage: linkForm.ownershipPercentage,
      holdingStatus: linkForm.holdingStatus,
    })
    ElMessage.success('关联股东成功')
    linkDialogVisible.value = false
    await refresh()
  } catch (error) {
    // 服务端校验口径（重复关联、单条占比越界）直接透出在表单内，便于用户修正后重试
    linkFormError.value = resolveApiErrorMessage(error, '关联股东失败，请稍后重试')
  } finally {
    linkSaving.value = false
  }
}

function formatShareholderOption(option: ShareholderLinkOption) {
  return `${option.shareholderName}（${option.contactName || '—'} / ${option.contactPhone || '—'}）`
}

// 标签颜色按状态码映射：状态名是展示文案，若按文案判断会在措辞调整时静默失效
function resolveHoldingStatusType(status: StationShareholding['holdingStatus']) {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'EXITED':
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
.shareholder-section {
  display: grid;
  gap: 12px;
}

.shareholder-section .detail-section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mini-list {
  display: grid;
  gap: 10px;
}

.ownership-sum-warning {
  padding: 12px 16px;
  border-radius: 14px;
  border: 1px solid rgba(248, 184, 78, 0.35);
  background: rgba(248, 184, 78, 0.08);
  color: var(--warning);
  font-size: 13px;
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

.shareholder-error {
  display: grid;
  justify-items: start;
  gap: 10px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid rgba(255, 125, 125, 0.25);
  background: rgba(40, 12, 16, 0.3);
}

.shareholder-error p {
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
  .shareholder-section .detail-section__head {
    flex-direction: column;
    align-items: stretch;
  }

  .mini-item {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>