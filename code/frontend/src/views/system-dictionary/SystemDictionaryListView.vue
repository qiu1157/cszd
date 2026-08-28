<template>
  <section class="page-section">
    <el-card class="system-dictionary-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>系统字典</h3>
            <p>系统管理员可统一查看平台、充电站状态和充电桩状态字典；新增、编辑和启停将在后续任务中开放。</p>
          </div>
          <el-button type="primary" disabled>新增字典项</el-button>
        </div>
      </template>

      <div class="summary-row">
        <div class="summary-chip">左侧固定展示平台、充电站状态、充电桩状态 3 类字典</div>
        <div class="summary-chip">字典类型按排序值优先展示，相同排序再按编码兜底</div>
        <div class="summary-chip">字典项按排序值优先展示，相同排序再按编码兜底</div>
        <div class="summary-chip">新增、编辑、启用、停用入口当前任务仅展示禁用态</div>
      </div>

      <div class="dictionary-layout">
        <aside class="dictionary-nav">
          <button
            v-for="item in navItems"
            :key="item.typeCode"
            type="button"
            class="dictionary-nav__item"
            :class="{ 'dictionary-nav__item--active': item.typeCode === activeTypeCode }"
            @click="selectType(item.typeCode)"
          >
            <strong>{{ item.title }}</strong>
            <span>{{ item.statusLabel }}</span>
          </button>
        </aside>

        <div class="dictionary-content">
          <div class="dictionary-content__head">
            <div>
              <h4>{{ activeTypeTitle }}</h4>
              <p>当前仅提供列表查看能力，后续任务将继续开放字典项维护操作。</p>
            </div>
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
        </div>
      </div>
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

import { mapDictItemToTableRow, mapDictTypeToNavItem } from './adapters'

type DictTypeCode = 'PLATFORM' | 'STATION_STATUS' | 'PILE_STATUS'

const loading = ref(false)
const activeTypeCode = ref<DictTypeCode>('PLATFORM')
const types = ref<SystemDictTypeListItem[]>([])
const items = ref<SystemDictItemListItem[]>([])

const navItems = computed(() => types.value.map(mapDictTypeToNavItem))
const tableRows = computed(() => items.value.map(mapDictItemToTableRow))
const activeTypeTitle = computed(() => navItems.value.find((item) => item.typeCode === activeTypeCode.value)?.title ?? '系统字典')

async function loadTypes() {
  types.value = await getSystemDictionaryTypes()
  if (types.value.some((item) => item.typeCode === 'PLATFORM')) {
    activeTypeCode.value = 'PLATFORM'
    return
  }
  if (types.value[0]) {
    activeTypeCode.value = types.value[0].typeCode
  }
}

async function loadItems() {
  loading.value = true
  try {
    items.value = await getSystemDictionaryItems(activeTypeCode.value)
  } catch {
    ElMessage.error('系统字典列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function selectType(typeCode: DictTypeCode) {
  if (activeTypeCode.value === typeCode) {
    return
  }
  activeTypeCode.value = typeCode
  await loadItems()
}

onMounted(async () => {
  try {
    await loadTypes()
    await loadItems()
  } catch {
    ElMessage.error('系统字典类型加载失败，请稍后重试')
  }
})
</script>

<style scoped>
.system-dictionary-card {
  border-radius: 24px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #0f172a;
}

.section-head p {
  margin: 0;
  line-height: 1.6;
  color: #64748b;
}

.summary-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.summary-chip {
  padding: 8px 14px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
}

.dictionary-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.dictionary-nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dictionary-nav__item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.dictionary-nav__item strong {
  color: #0f172a;
}

.dictionary-nav__item span {
  font-size: 13px;
  color: #64748b;
}

.dictionary-nav__item:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.dictionary-nav__item--active {
  border-color: #2563eb;
  background: #eff6ff;
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.12);
}

.dictionary-content {
  min-width: 0;
}

.dictionary-content__head {
  margin-bottom: 16px;
}

.dictionary-content__head h4 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #0f172a;
}

.dictionary-content__head p {
  margin: 0;
  color: #64748b;
}

.action-group {
  display: flex;
  gap: 8px;
}

.action-button {
  min-width: 64px;
}

.action-button--muted {
  color: #94a3b8;
  border-color: #e2e8f0;
  background: #f8fafc;
}

@media (max-width: 960px) {
  .dictionary-layout {
    grid-template-columns: 1fr;
  }

  .dictionary-nav {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
