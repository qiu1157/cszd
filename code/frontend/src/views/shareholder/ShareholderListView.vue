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
        <el-table-column label="关联站点" min-width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleShowStationDetail(row)">
              {{ row.relatedStationCount }} 个站点
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAtText" label="最近变化" min-width="180" />
      </el-table>
    </el-card>

    <el-dialog
      v-model="stationDialogVisible"
      :close-on-click-modal="false"
      :title="stationDialogTitle"
      width="520px"
    >
      <div class="station-summary">共 {{ activeShareholder?.relatedStationCount ?? 0 }} 个关联站点</div>
      <el-empty description="暂无关联站点" :image-size="80" />
      <template #footer>
        <el-button @click="stationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="createDialogVisible"
      class="shareholder-create-dialog"
      width="520px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <h3>新增股东</h3>
          <el-button text @click="closeCreateDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>股东名称</label>
          <el-input
            v-model="createForm.shareholderName"
            maxlength="128"
            placeholder="请输入股东名称"
            @blur="validateCreateField('shareholderName')"
          />
          <p v-if="createErrors.shareholderName" class="field-error">{{ createErrors.shareholderName }}</p>
        </div>

        <div class="form-field">
          <label>联系人</label>
          <el-input
            v-model="createForm.contactName"
            maxlength="64"
            placeholder="请输入联系人（选填）"
            @blur="validateCreateField('contactName')"
          />
          <p v-if="createErrors.contactName" class="field-error">{{ createErrors.contactName }}</p>
        </div>

        <div class="form-field">
          <label>联系电话</label>
          <el-input
            v-model="createForm.contactPhone"
            maxlength="20"
            placeholder="请输入联系电话（选填）"
            @blur="validateCreateField('contactPhone')"
          />
          <p v-if="createErrors.contactPhone" class="field-error">{{ createErrors.contactPhone }}</p>
        </div>

        <div class="form-field">
          <label>股东状态</label>
          <el-select
            v-model="createForm.shareholderStatus"
            placeholder="请选择股东状态"
            @change="validateCreateField('shareholderStatus')"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <p v-if="createErrors.shareholderStatus" class="field-error">{{ createErrors.shareholderStatus }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="closeCreateDialog">取消</el-button>
          <el-button type="primary" :loading="creating" @click="submitCreateForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

import { createShareholder, getShareholders, type ShareholderListItem } from '@/api/shareholder'
import {
  SHAREHOLDER_STATUS_OPTIONS,
  type ShareholderStatus,
} from '@/constants/enums/shareholder-status'

import { mapShareholderToTableRow, type ShareholderTableRow } from './adapters'
import {
  buildShareholderCreatePayload,
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

const stationDialogVisible = ref(false)
const activeShareholder = ref<ShareholderTableRow | null>(null)

const stationDialogTitle = computed(() =>
  activeShareholder.value ? `${activeShareholder.value.shareholderName} · 关联站点` : '关联站点',
)

// 站点明细数据依赖里程碑 F 的持股关系表，当前先呈现数量与空态占位
function handleShowStationDetail(row: ShareholderTableRow) {
  activeShareholder.value = row
  stationDialogVisible.value = true
}

const createDialogVisible = ref(false)
const creating = ref(false)
const createForm = ref<ShareholderForm>(createInitialForm())
const createErrors = ref<ShareholderFormErrors>({})

function createInitialForm(): ShareholderForm {
  return {
    shareholderName: '',
    contactName: '',
    contactPhone: '',
    shareholderStatus: '',
  }
}

function openCreateDialog() {
  createForm.value = createInitialForm()
  createErrors.value = {}
  createDialogVisible.value = true
}

// 保存请求进行中禁止关闭，避免半途提交造成数据不一致
function closeCreateDialog() {
  if (creating.value) {
    return
  }
  createDialogVisible.value = false
}

function validateCreateField(field: keyof ShareholderForm) {
  const nextErrors = validateShareholderForm(createForm.value)
  createErrors.value = {
    ...createErrors.value,
    [field]: nextErrors[field],
  }
}

async function submitCreateForm() {
  createErrors.value = validateShareholderForm(createForm.value)
  if (Object.values(createErrors.value).some(Boolean)) {
    return
  }

  creating.value = true
  try {
    await createShareholder(buildShareholderCreatePayload(createForm.value))
    ElMessage.success('股东新增成功')
    createDialogVisible.value = false
    createForm.value = createInitialForm()
    await loadShareholders()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '股东新增失败，请稍后重试'))
  } finally {
    creating.value = false
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

.station-summary {
  margin-bottom: 8px;
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