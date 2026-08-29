<template>
  <section class="page-section">
    <el-card class="system-dictionary-card" shadow="never">
      <template #header>
        <div class="section-head">
          <h3>系统字典</h3>
        </div>
      </template>

      <div class="toolbar dictionary-toolbar">
        <div class="toolbar-field">
          <label>字典类型</label>
          <el-select
            v-model="activeTypeCode"
            class="dictionary-type-select"
            placeholder="请选择字典类型"
            @change="handleTypeChange"
          >
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="toolbar-spacer"></div>
        <el-button class="create-button" type="primary" :disabled="!activeTypeCode || saving" @click="openCreateDialog">
          新增字典项
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableRows" class="dictionary-table" empty-text="当前尚未维护字典项。">
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="code" label="编码" min-width="180" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" min-width="100" />
        <el-table-column prop="updatedAtText" label="更新时间" min-width="180" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button class="action-button action-button--secondary" :disabled="!row.canEdit || saving" @click="openEditDialog(row)">
                编辑
              </el-button>
              <el-button class="action-button action-button--muted" :disabled="row.actionDisabled">启用</el-button>
              <el-button class="action-button action-button--muted" :disabled="row.actionDisabled">停用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      width="520px"
      class="system-dictionary-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <div>
            <h3 v-if="dialogMode === 'create'">新增字典项</h3>
            <h3 v-else>编辑字典项</h3>
            <p>当前字典类型：{{ activeTypeName || '--' }}</p>
          </div>
          <el-button text @click="closeDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>名称</label>
          <el-input v-model="form.itemName" maxlength="64" placeholder="请输入名称" @blur="validateField('itemName')" />
          <p v-if="formErrors.itemName" class="field-error">{{ formErrors.itemName }}</p>
        </div>

        <div class="form-field">
          <label>编码</label>
          <el-input v-model="form.itemCode" maxlength="64" placeholder="请输入编码" @blur="validateField('itemCode')" />
          <p v-if="formErrors.itemCode" class="field-error">{{ formErrors.itemCode }}</p>
        </div>

        <div class="form-field">
          <label>状态</label>
          <el-select v-model="form.status" placeholder="请选择状态" @change="validateField('status')">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
          <p v-if="formErrors.status" class="field-error">{{ formErrors.status }}</p>
        </div>

        <div class="form-field">
          <label>排序</label>
          <el-input v-model="form.sortOrder" maxlength="9" placeholder="不填默认按0保存" @blur="validateField('sortOrder')" />
          <p v-if="formErrors.sortOrder" class="field-error">{{ formErrors.sortOrder }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

import {
  createSystemDictionaryItem,
  getSystemDictionaryItems,
  getSystemDictionaryTypes,
  updateSystemDictionaryItem,
  type SystemDictItemListItem,
  type SystemDictTypeListItem,
} from '@/api/system-dictionary'

import { mapDictItemToTableRow } from './adapters'
import {
  buildSystemDictItemPayload,
  validateSystemDictItemForm,
  type SystemDictItemForm,
  type SystemDictItemFormErrors,
} from './system-dictionary-form'

type DictTypeCode = SystemDictTypeListItem['typeCode']
type DictTableRow = ReturnType<typeof mapDictItemToTableRow>
type DialogMode = 'create' | 'edit'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<DialogMode>('create')
const editingItemId = ref<number | null>(null)
const activeTypeCode = ref<DictTypeCode | ''>('PLATFORM')
const types = ref<SystemDictTypeListItem[]>([])
const items = ref<SystemDictItemListItem[]>([])
const form = ref<SystemDictItemForm>(createInitialForm())
const formErrors = ref<SystemDictItemFormErrors>({})

const tableRows = computed(() => items.value.map(mapDictItemToTableRow))
const typeOptions = computed(() =>
  types.value.map((item) => ({
    value: item.typeCode,
    label: item.typeName,
  })),
)
const activeTypeName = computed(() => types.value.find((item) => item.typeCode === activeTypeCode.value)?.typeName ?? '')

async function loadTypes() {
  types.value = await getSystemDictionaryTypes()
  activeTypeCode.value = resolveInitialTypeCode(types.value)
}

async function loadItems(typeCode: DictTypeCode | '' = activeTypeCode.value) {
  if (!typeCode) {
    items.value = []
    return
  }

  loading.value = true
  try {
    items.value = await getSystemDictionaryItems(typeCode)
  } catch {
    ElMessage.error('系统字典列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleTypeChange(typeCode: DictTypeCode | '') {
  activeTypeCode.value = typeCode
  await loadItems(typeCode)
}

function openCreateDialog() {
  dialogMode.value = 'create'
  editingItemId.value = null
  form.value = createInitialForm()
  formErrors.value = {}
  dialogVisible.value = true
}

function openEditDialog(row: DictTableRow) {
  dialogMode.value = 'edit'
  editingItemId.value = row.id
  form.value = {
    itemName: row.name,
    itemCode: row.code,
    status: row.statusType === 'success' ? 'ENABLED' : 'DISABLED',
    sortOrder: String(row.sortOrder),
  }
  formErrors.value = {}
  dialogVisible.value = true
}

function closeDialog() {
  if (saving.value) {
    return
  }
  dialogVisible.value = false
}

function validateField(field: keyof SystemDictItemForm) {
  const nextErrors = validateSystemDictItemForm(form.value)
  formErrors.value = {
    ...formErrors.value,
    [field]: nextErrors[field],
  }
}

async function submitForm() {
  formErrors.value = validateSystemDictItemForm(form.value)
  if (Object.values(formErrors.value).some(Boolean) || !activeTypeCode.value) {
    return
  }

  saving.value = true
  try {
    const payload = buildSystemDictItemPayload(form.value)
    if (dialogMode.value === 'create') {
      await createSystemDictionaryItem(activeTypeCode.value, payload)
      ElMessage.success('字典项新增成功')
    } else if (editingItemId.value !== null) {
      await updateSystemDictionaryItem(activeTypeCode.value, editingItemId.value, payload)
      ElMessage.success('字典项更新成功')
    }

    dialogVisible.value = false
    editingItemId.value = null
    form.value = createInitialForm()
    await loadItems(activeTypeCode.value)
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, dialogMode.value === 'create' ? '字典项新增失败，请稍后重试' : '字典项更新失败，请稍后重试'))
  } finally {
    saving.value = false
  }
}

function resolveInitialTypeCode(dictTypes: SystemDictTypeListItem[]): DictTypeCode | '' {
  if (dictTypes.some((item) => item.typeCode === 'PLATFORM')) {
    return 'PLATFORM'
  }

  return dictTypes[0]?.typeCode ?? ''
}

function createInitialForm(): SystemDictItemForm {
  return {
    itemName: '',
    itemCode: '',
    status: 'ENABLED',
    sortOrder: '',
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

onMounted(async () => {
  try {
    await loadTypes()
    await loadItems(activeTypeCode.value)
  } catch {
    ElMessage.error('系统字典类型加载失败，请稍后重试')
  }
})
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.system-dictionary-card {
  border: 1px solid var(--line-soft);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0)),
    var(--bg-panel);
}

.section-head {
  display: flex;
  align-items: center;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.dictionary-toolbar {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  margin-bottom: 20px;
}

.toolbar-field {
  display: grid;
  gap: 8px;
}

.toolbar-field label {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.dictionary-type-select {
  width: 100%;
}

.toolbar-spacer {
  min-height: 1px;
}

.dictionary-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(57, 200, 255, 0.05);
  --el-table-border-color: rgba(150, 206, 255, 0.1);
  --el-table-row-hover-bg-color: rgba(57, 200, 255, 0.04);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.action-button {
  min-width: 68px;
  margin: 0;
  border-radius: 12px;
  padding: 8px 14px;
  font-weight: 600;
}

.action-button--secondary {
  border-color: rgba(57, 200, 255, 0.24);
  color: #dff9ff;
  background: rgba(57, 200, 255, 0.1);
}

.action-button--muted {
  border-color: rgba(148, 163, 184, 0.18);
  color: rgba(206, 216, 230, 0.58);
  background: rgba(148, 163, 184, 0.06);
  box-shadow: none;
}

.action-button--muted.is-disabled,
.action-button--muted[disabled] {
  border-color: rgba(148, 163, 184, 0.14);
  color: rgba(206, 216, 230, 0.42);
  background: rgba(148, 163, 184, 0.04);
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

@media (max-width: 960px) {
  .dictionary-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
