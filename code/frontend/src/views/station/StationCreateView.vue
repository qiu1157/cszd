<template>
  <section class="page-section">
    <el-card class="station-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <h3>新增充电站</h3>
          <div class="action-group">
            <el-button class="action-button action-button--secondary" @click="goBack">取消</el-button>
            <el-button class="action-button" type="primary" :loading="saving" @click="handleSubmit">保存并返回列表</el-button>
          </div>
        </div>
      </template>

      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="充电站名称" required>
              <el-input v-model="form.stationName" placeholder="例如：雨花城际快充港" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所在平台" required>
              <el-select v-model="form.platformItemId" class="full-width" placeholder="请选择平台" clearable>
                <el-option
                  v-for="item in platformOptions"
                  :key="item.itemId"
                  :label="item.itemName"
                  :value="item.itemId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="平台站点编号">
              <el-input v-model="form.platformStationCode" placeholder="例如：XL-4301-889" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="例如：13800138000" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="电表户号">
              <el-input v-model="form.meterAccountNumber" placeholder="例如：0101234567890" />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="例如：长沙市雨花区城际大道南段" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" placeholder="例如：张海峰" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="初始状态">
              <el-input value="签合同（默认）" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { createStation } from '@/api/station'
import { type StationPlatformOption, getStationPlatformOptions } from '@/api/station/form-options'
import { STATION_LIST_PATH } from '@/router/navigation'

import { createInitialStationCreateForm, toCreateStationPayload } from './create-form'

const router = useRouter()
const form = reactive(createInitialStationCreateForm())
const platformOptions = ref<StationPlatformOption[]>([])
const saving = ref(false)

async function loadPlatformOptions() {
  try {
    platformOptions.value = await getStationPlatformOptions()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '平台选项加载失败，请稍后重试'))
  }
}

function goBack() {
  void router.push(STATION_LIST_PATH)
}

async function handleSubmit() {
  if (!form.stationName.trim()) {
    ElMessage.warning('请输入充电站名称')
    return
  }
  if (!form.platformItemId) {
    ElMessage.warning('请选择所在平台')
    return
  }

  saving.value = true
  try {
    await createStation(toCreateStationPayload(form))
    ElMessage.success('新增充电站成功')
    await router.push(STATION_LIST_PATH)
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '新增充电站失败，请稍后重试'))
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
  void loadPlatformOptions()
})
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.station-page-card {
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

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.full-width {
  width: 100%;
}
</style>
