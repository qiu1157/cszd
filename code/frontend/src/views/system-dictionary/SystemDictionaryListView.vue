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
        <el-button class="create-button" type="primary" disabled>新增字典项</el-button>
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
              <el-button class="action-button action-button--muted" :disabled="row.actionDisabled">编辑</el-button>
              <el-button class="action-button action-button--muted" :disabled="row.actionDisabled">启用</el-button>
              <el-button class="action-button action-button--muted" :disabled="row.actionDisabled">停用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

import {
  getSystemDictionaryItems,
  getSystemDictionaryTypes,
  type SystemDictItemListItem,
  type SystemDictTypeListItem,
} from '@/api/system-dictionary'

import { mapDictItemToTableRow } from './adapters'

type DictTypeCode = SystemDictTypeListItem['typeCode']

const loading = ref(false)
const activeTypeCode = ref<DictTypeCode | ''>('PLATFORM')
const types = ref<SystemDictTypeListItem[]>([])
const items = ref<SystemDictItemListItem[]>([])

const tableRows = computed(() => items.value.map(mapDictItemToTableRow))
const typeOptions = computed(() =>
  types.value.map((item) => ({
    value: item.typeCode,
    label: item.typeName,
  })),
)

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

function resolveInitialTypeCode(dictTypes: SystemDictTypeListItem[]): DictTypeCode | '' {
  if (dictTypes.some((item) => item.typeCode === 'PLATFORM')) {
    return 'PLATFORM'
  }

  return dictTypes[0]?.typeCode ?? ''
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

.create-button.is-disabled,
.create-button[disabled] {
  border-color: rgba(148, 163, 184, 0.14);
  color: rgba(206, 216, 230, 0.42);
  background: rgba(148, 163, 184, 0.04);
  box-shadow: none;
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

@media (max-width: 960px) {
  .dictionary-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
