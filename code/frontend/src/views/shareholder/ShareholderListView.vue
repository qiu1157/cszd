<template>
  <section class="page-section">
    <el-card class="shareholder-page-card" shadow="never">
      <template #header>
        <div class="section-head section-head--stacked">
          <div class="section-head__top">
            <h3>股东列表页</h3>
            <div class="section-head__actions">
              <el-button class="action-button" @click="handleDownloadTemplate">下载股东模板</el-button>
              <el-button v-if="canImport" class="action-button" type="primary" @click="openImportDialog">
                导入股东
              </el-button>
              <el-button class="action-button" type="primary" @click="openCreateDialog">新增股东</el-button>
            </div>
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

    <el-dialog
      v-model="importDialogVisible"
      class="shareholder-import-dialog"
      width="560px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <h3>导入股东</h3>
          <el-button text @click="closeImportDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <el-upload
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx"
          :on-change="handleImportFileChange"
          :on-remove="handleImportFileRemove"
        >
          <div class="upload-trigger">将 Excel 文件拖到此处，或<em>点击选择文件</em></div>
          <template #tip>
            <div class="upload-hint">仅支持 .xlsx；请先下载并使用系统模板，导入仅新增、股东名称不可重复。</div>
          </template>
        </el-upload>

        <div v-if="importErrors.length" class="import-result">
          <p class="import-result__title">导入失败：以下数据未通过校验，本次未导入任何数据。</p>
          <el-table :data="importErrors" max-height="240" empty-text="--">
            <el-table-column prop="rowNumber" label="行号" width="80" />
            <el-table-column prop="message" label="原因" min-width="320" />
          </el-table>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="closeImportDialog">取消</el-button>
          <el-button type="primary" :loading="importing" :disabled="!importFile" @click="submitImport">
            导入
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage, ElMessageBox, type UploadFile } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import {
  createShareholder,
  downloadShareholderImportTemplate,
  getShareholders,
  importShareholders,
  updateShareholder,
  updateShareholderStatus,
  type ShareholderImportError,
  type ShareholderListItem,
  type ShareholderStatusAction,
} from '@/api/shareholder'
import {
  SHAREHOLDER_STATUS_OPTIONS,
  type ShareholderStatus,
} from '@/constants/enums/shareholder-status'
import { USER_ROLES } from '@/constants/enums/user-role'
import { buildShareholderDetailPath } from '@/router/navigation'
import { useAuthStore } from '@/stores/auth'

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

const authStore = useAuthStore()
// 模板下载对两类角色可见；导入仅系统管理员可见（前端显隐，后端仍强制校验）
const canImport = computed(() => authStore.user?.role === USER_ROLES.SYSTEM_ADMIN)

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

const importDialogVisible = ref(false)
const importFile = ref<File | null>(null)
const importing = ref(false)
const importErrors = ref<ShareholderImportError[]>([])

function openImportDialog() {
  importFile.value = null
  importErrors.value = []
  importDialogVisible.value = true
}

function closeImportDialog() {
  if (importing.value) {
    return
  }
  importDialogVisible.value = false
  importFile.value = null
  importErrors.value = []
}

function handleImportFileChange(uploadFile: UploadFile) {
  importFile.value = uploadFile.raw ?? null
  importErrors.value = []
}

function handleImportFileRemove() {
  importFile.value = null
  importErrors.value = []
}

async function handleDownloadTemplate() {
  try {
    await downloadShareholderImportTemplate()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '模板下载失败，请稍后重试'))
  }
}

async function submitImport() {
  const file = importFile.value
  if (!file) {
    return
  }

  importing.value = true
  importErrors.value = []
  try {
    const result = await importShareholders(file)
    if (result.errors.length > 0) {
      importErrors.value = result.errors
      return
    }
    ElMessage.success(`导入成功，新增 ${result.successCount} 条股东`)
    importDialogVisible.value = false
    importFile.value = null
    await loadShareholders()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '导入失败，请稍后重试'))
  } finally {
    importing.value = false
  }
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

.section-head__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
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

.upload-trigger {
  color: var(--text-secondary);
}

.upload-trigger em {
  color: var(--accent, #39c8ff);
  font-style: normal;
}

.upload-hint {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 8px;
}

.import-result {
  margin-top: 16px;
}

.import-result__title {
  margin: 0 0 8px;
  color: #ff8f8f;
  font-size: 13px;
}

@media (max-width: 768px) {
  .section-head__top,
  .section-head__actions,
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-input {
    width: 100%;
  }
}
</style>