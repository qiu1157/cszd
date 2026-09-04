<template>
  <section class="page-section">
    <el-card class="shareholder-page-card" shadow="never">
      <template #header>
        <div class="section-head section-head--stacked">
          <div class="section-head__top">
            <h3>股东列表页</h3>
            <el-button class="action-button" type="primary" @click="openCreateDialog">新增股东</el-button>
          </div>
          <div class="toolbar-row">
            <el-input
              v-model="searchForm.shareholderName"
              class="toolbar-input"
              placeholder="请输入股东名称"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchForm.shareholderStatus"
              class="toolbar-input"
              placeholder="请选择状态"
              clearable
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button class="action-button" type="primary" @click="handleSearch">查询</el-button>
            <el-button class="action-button" @click="handleResetFilters">重置筛选</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableRows" class="shareholder-table" empty-text="暂无股东，请先新增股东。">
        <el-table-column label="股东名称" min-width="220">
          <template #default="{ row }">
            <div class="title-cell">
              <strong>{{ row.shareholderName }}</strong>
              <span class="title-cell__subtitle">{{ row.shareholderSubtitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contactText" label="联系人" min-width="180" />
        <!-- 关联站点为持股中站点的统计值，纯展示不下钻：明细由详情页"关联充电站"分区承载 -->
        <el-table-column label="关联站点" min-width="120">
          <template #default="{ row }">{{ row.relatedStationCount }} 个</template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAtText" label="最近变化" min-width="180" />
        <el-table-column label="操作" min-width="260">
          <template #default="{ row }">
            <div class="action-group">
              <el-button
                class="action-button action-button--secondary"
                :disabled="saving || statusSubmitting"
                @click="goToShareholderDetail(row.id)"
              >
                详情
              </el-button>
              <el-button
                class="action-button action-button--secondary"
                :disabled="saving || statusSubmitting"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                class="action-button"
                :class="`action-button--${row.enableActionTone}`"
                :disabled="!row.canEnable || saving || statusSubmitting"
                :loading="statusSubmitting && statusSubmittingId === row.id && statusSubmittingAction === 'enable'"
                @click="submitShareholderStatus(row, 'enable')"
              >
                启用
              </el-button>
              <el-button
                class="action-button"
                :class="`action-button--${row.disableActionTone}`"
                :disabled="!row.canDisable || saving || statusSubmitting"
                :loading="statusSubmitting && statusSubmittingId === row.id && statusSubmittingAction === 'disable'"
                @click="submitShareholderStatus(row, 'disable')"
              >
                停用
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      class="shareholder-form-dialog"
      width="520px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <h3 v-if="dialogMode === 'create'">新增股东</h3>
          <h3 v-else>编辑股东</h3>
          <el-button text @click="closeDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>股东名称</label>
          <el-input
            v-model="form.shareholderName"
            maxlength="128"
            placeholder="请输入股东名称"
            @blur="validateField('shareholderName')"
          />
          <p v-if="formErrors.shareholderName" class="field-error">{{ formErrors.shareholderName }}</p>
        </div>

        <div class="form-field">
          <label>联系人</label>
          <el-input
            v-model="form.contactName"
            maxlength="64"
            placeholder="请输入联系人（选填）"
            @blur="validateField('contactName')"
          />
          <p v-if="formErrors.contactName" class="field-error">{{ formErrors.contactName }}</p>
        </div>

        <div class="form-field">
          <label>联系电话</label>
          <el-input
            v-model="form.contactPhone"
            maxlength="20"
            placeholder="请输入联系电话（选填）"
            @blur="validateField('contactPhone')"
          />
          <p v-if="formErrors.contactPhone" class="field-error">{{ formErrors.contactPhone }}</p>
        </div>

        <div v-if="dialogMode === 'create'" class="form-field">
          <label>股东状态</label>
          <el-select
            v-model="form.shareholderStatus"
            placeholder="请选择股东状态"
            @change="validateField('shareholderStatus')"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <p v-if="formErrors.shareholderStatus" class="field-error">{{ formErrors.shareholderStatus }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitShareholderForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import {
  createShareholder,
  getShareholders,
  updateShareholder,
  updateShareholderStatus,
  type ShareholderListItem,
  type ShareholderStatusAction,
} from '@/api/shareholder'
import {
  SHAREHOLDER_STATUS_OPTIONS,
  type ShareholderStatus,
} from '@/constants/enums/shareholder-status'
import { buildShareholderDetailPath } from '@/router/navigation'

import { mapShareholderToTableRow, type ShareholderTableRow } from './adapters'
import {
  buildShareholderCreatePayload,
  buildShareholderUpdatePayload,
  validateShareholderEditForm,
  validateShareholderForm,
  type ShareholderForm,
  type ShareholderFormErrors,
} from './shareholder-form'

const loading = ref(false)
const shareholders = ref<ShareholderListItem[]>([])
const statusOptions = SHAREHOLDER_STATUS_OPTIONS
const searchForm = ref({
  shareholderName: '',
  shareholderStatus: null as ShareholderStatus | null,
})

const tableRows = computed(() => shareholders.value.map(mapShareholderToTableRow))

async function loadShareholders() {
  loading.value = true
  try {
    shareholders.value = await getShareholders(
      searchForm.value.shareholderName,
      searchForm.value.shareholderStatus,
    )
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '股东列表加载失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  void loadShareholders()
}

function handleResetFilters() {
  searchForm.value.shareholderName = ''
  searchForm.value.shareholderStatus = null
  void loadShareholders()
}

const router = useRouter()

// 股东详情承载基础信息与后续分区，列表行通过该入口进入独立路由页
function goToShareholderDetail(shareholderId: number) {
  void router.push(buildShareholderDetailPath(shareholderId))
}

const statusSubmitting = ref(false)
const statusSubmittingId = ref<number | null>(null)
const statusSubmittingAction = ref<'' | ShareholderStatusAction>('')

// 为什么二次确认：状态切换影响后续持股关系的可用性（E-8），误触需有明确的拦截；
// 停用动作附加影响提示，让运营在确认前明确停用后不能再新建持股关系
async function submitShareholderStatus(row: ShareholderTableRow, action: ShareholderStatusAction) {
  const actionText = action === 'enable' ? '启用' : '停用'
  const impactHint = action === 'disable' ? '停用后该股东不能再新建持股关系。' : ''

  try {
    await ElMessageBox.confirm(`确认${actionText}股东 ${row.shareholderName} 吗？${impactHint}`, `${actionText}股东`, {
      confirmButtonText: actionText,
      cancelButtonText: '取消',
      type: 'warning',
      closeOnClickModal: false,
      closeOnPressEscape: false,
    })
  } catch {
    return
  }

  statusSubmitting.value = true
  statusSubmittingId.value = row.id
  statusSubmittingAction.value = action

  try {
    await updateShareholderStatus(row.id, action)
    ElMessage.success(`股东已${actionText}`)
    await loadShareholders()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, `股东${actionText}失败，请稍后重试`))
  } finally {
    statusSubmitting.value = false
    statusSubmittingId.value = null
    statusSubmittingAction.value = ''
  }
}

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingShareholderId = ref<number | null>(null)
const saving = ref(false)
const form = ref<ShareholderForm>(createInitialForm())
const formErrors = ref<ShareholderFormErrors>({})

function createInitialForm(): ShareholderForm {
  return {
    shareholderName: '',
    contactName: '',
    contactPhone: '',
    shareholderStatus: '',
  }
}

function openCreateDialog() {
  dialogMode.value = 'create'
  editingShareholderId.value = null
  form.value = createInitialForm()
  formErrors.value = {}
  dialogVisible.value = true
}

// 表格行上的联系人是名称与电话的合并文案，编辑需要原始字段，因此从原始列表取数
function openEditDialog(row: ShareholderTableRow) {
  const item = shareholders.value.find(shareholder => shareholder.id === row.id)
  if (!item) {
    return
  }
  dialogMode.value = 'edit'
  editingShareholderId.value = item.id
  form.value = {
    shareholderName: item.shareholderName,
    contactName: item.contactName ?? '',
    contactPhone: item.contactPhone ?? '',
    shareholderStatus: '',
  }
  formErrors.value = {}
  dialogVisible.value = true
}

// 保存请求进行中禁止关闭，避免半途提交造成数据不一致
function closeDialog() {
  if (saving.value) {
    return
  }
  dialogVisible.value = false
}

function validateField(field: keyof ShareholderForm) {
  const nextErrors = dialogMode.value === 'create'
    ? validateShareholderForm(form.value)
    : validateShareholderEditForm(form.value)
  formErrors.value = {
    ...formErrors.value,
    [field]: nextErrors[field],
  }
}

async function submitShareholderForm() {
  formErrors.value = dialogMode.value === 'create'
    ? validateShareholderForm(form.value)
    : validateShareholderEditForm(form.value)
  if (Object.values(formErrors.value).some(Boolean)) {
    return
  }

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createShareholder(buildShareholderCreatePayload(form.value))
      ElMessage.success('股东新增成功')
    } else {
      const shareholderId = editingShareholderId.value
      if (shareholderId === null) {
        return
      }
      await updateShareholder(shareholderId, buildShareholderUpdatePayload(form.value))
      ElMessage.success('股东更新成功')
    }
    dialogVisible.value = false
    form.value = createInitialForm()
    await loadShareholders()
  } catch (error) {
    ElMessage.error(
      dialogMode.value === 'create'
        ? resolveApiErrorMessage(error, '股东新增失败，请稍后重试')
        : resolveApiErrorMessage(error, '股东更新失败，请稍后重试'),
    )
  } finally {
    saving.value = false
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
  void loadShareholders()
})
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.shareholder-page-card {
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

.section-head--stacked {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.section-head__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.shareholder-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(57, 200, 255, 0.05);
  --el-table-border-color: rgba(150, 206, 255, 0.1);
  --el-table-row-hover-bg-color: rgba(57, 200, 255, 0.04);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

.toolbar-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-input {
  width: min(320px, 100%);
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-cell__subtitle {
  font-size: 12px;
  color: var(--text-secondary);
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/* 操作列一行 3 个按钮、列宽有限，仅做最小宽度布局微调；按钮视觉样式以 global.scss 为全局唯一来源 */
.action-group .action-button {
  min-width: 68px;
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
  .section-head__top,
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-input {
    width: 100%;
  }
}
</style>