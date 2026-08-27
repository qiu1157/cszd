<template>
  <section class="page-section">
    <el-card class="user-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>业务管理员列表</h3>
            <p>系统管理员可新增、启用、停用业务管理员，并查看账号状态与最后登录时间；重置密码将在后续任务中实现。</p>
          </div>
          <el-button type="primary" @click="openCreateDialog">新增业务管理员</el-button>
        </div>
      </template>

      <div class="summary-row">
        <div class="summary-chip">当前仅展示业务管理员，不显示 `admin`</div>
        <div class="summary-chip">最后登录时间在登录成功后自动更新</div>
        <div class="summary-chip">新增时会校验中国大陆 11 位手机号格式</div>
        <div class="summary-chip">当前可执行动作会高亮显示，不可执行动作仅作弱提示</div>
      </div>

      <el-table v-loading="loading" :data="tableRows" class="user-table" empty-text="暂无业务管理员，可点击右上角新增业务管理员。">
        <el-table-column label="账号信息" min-width="260">
          <template #default="{ row }">
            <div class="title-cell">
              <strong>{{ row.account }}</strong>
              <span class="subtitle">{{ row.subtitle }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            {{ row.roleLabel }}
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType" effect="dark" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最后登录" min-width="180">
          <template #default="{ row }">
            {{ row.lastLoginText }}
          </template>
        </el-table-column>

        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button class="action-button action-button--muted" plain disabled>重置密码</el-button>
              <el-button
                class="action-button"
                :class="`action-button--${row.enableActionTone}`"
                :disabled="!row.canEnable || statusSubmitting"
                :loading="statusSubmitting && statusSubmittingAccount === row.account && statusSubmittingAction === 'enable'"
                @click="submitBusinessAdminStatus(row.account, 'enable')"
              >
                启用
              </el-button>
              <el-button
                class="action-button"
                :class="`action-button--${row.disableActionTone}`"
                :disabled="!row.canDisable || statusSubmitting"
                :loading="statusSubmitting && statusSubmittingAccount === row.account && statusSubmittingAction === 'disable'"
                @click="submitBusinessAdminStatus(row.account, 'disable')"
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
      width="520px"
      class="create-user-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-head">
          <div>
            <h3>新增业务管理员</h3>
            <p>手机号将作为登录账号，保存成功后立即出现在业务管理员列表中。</p>
          </div>
          <el-button text @click="closeCreateDialog">关闭</el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-field">
          <label>手机号</label>
          <el-input v-model="createForm.phone" maxlength="20" placeholder="请输入手机号" @blur="validateField('phone')" />
          <p v-if="formErrors.phone" class="field-error">{{ formErrors.phone }}</p>
        </div>

        <div class="form-field">
          <label>姓名</label>
          <el-input v-model="createForm.name" maxlength="20" placeholder="请输入姓名" @blur="validateField('name')" />
          <p v-if="formErrors.name" class="field-error">{{ formErrors.name }}</p>
        </div>

        <div class="form-field">
          <label>初始密码</label>
          <el-input
            v-model="createForm.initialPassword"
            maxlength="32"
            type="password"
            show-password
            placeholder="请输入初始密码"
            @blur="validateField('initialPassword')"
          />
          <p v-if="formErrors.initialPassword" class="field-error">{{ formErrors.initialPassword }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="closeCreateDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitCreateBusinessAdmin">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

import { createBusinessAdmin, getBusinessAdmins, updateBusinessAdminStatus, type UpdateBusinessAdminStatusAction } from '@/api/user'

import { mapBusinessAdminToTableRow } from './adapters'
import {
  buildCreateBusinessAdminPayload,
  type CreateBusinessAdminForm,
  type CreateBusinessAdminFormErrors,
  validateCreateBusinessAdminForm,
} from './create-business-admin'

const loading = ref(false)
const saving = ref(false)
const statusSubmitting = ref(false)
const statusSubmittingAccount = ref('')
const statusSubmittingAction = ref<UpdateBusinessAdminStatusAction | ''>('')
const dialogVisible = ref(false)
const businessAdmins = ref<Awaited<ReturnType<typeof getBusinessAdmins>>>([])
const createForm = ref<CreateBusinessAdminForm>(createInitialForm())
const formErrors = ref<CreateBusinessAdminFormErrors>({})

const tableRows = computed(() => businessAdmins.value.map(mapBusinessAdminToTableRow))

async function loadBusinessAdmins() {
  loading.value = true
  try {
    businessAdmins.value = await getBusinessAdmins()
  } catch {
    ElMessage.error('业务管理员列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadBusinessAdmins()
})

function openCreateDialog() {
  dialogVisible.value = true
  createForm.value = createInitialForm()
  formErrors.value = {}
}

function closeCreateDialog() {
  if (saving.value) {
    return
  }
  dialogVisible.value = false
}

function validateField(field: keyof CreateBusinessAdminForm) {
  const nextErrors = validateCreateBusinessAdminForm(createForm.value)
  formErrors.value = {
    ...formErrors.value,
    [field]: nextErrors[field],
  }
}

async function submitCreateBusinessAdmin() {
  formErrors.value = validateCreateBusinessAdminForm(createForm.value)
  if (Object.values(formErrors.value).some(Boolean)) {
    return
  }

  saving.value = true
  try {
    await createBusinessAdmin(buildCreateBusinessAdminPayload(createForm.value))
    ElMessage.success('业务管理员新增成功')
    dialogVisible.value = false
    createForm.value = createInitialForm()
    await loadBusinessAdmins()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '业务管理员新增失败，请稍后重试'))
  } finally {
    saving.value = false
  }
}

async function submitBusinessAdminStatus(account: string, action: UpdateBusinessAdminStatusAction) {
  const actionText = action === 'enable' ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(`确认${actionText}业务管理员 ${account} 吗？`, `${actionText}业务管理员`, {
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
  statusSubmittingAccount.value = account
  statusSubmittingAction.value = action

  try {
    await updateBusinessAdminStatus(account, action)
    ElMessage.success(`业务管理员已${action === 'enable' ? '启用' : '停用'}`)
    await loadBusinessAdmins()
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, `业务管理员${actionText}失败，请稍后重试`))
  } finally {
    statusSubmitting.value = false
    statusSubmittingAccount.value = ''
    statusSubmittingAction.value = ''
  }
}

function createInitialForm(): CreateBusinessAdminForm {
  return {
    phone: '',
    name: '',
    initialPassword: '',
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
</script>

<style scoped lang="scss">
.page-section {
  display: grid;
}

.user-page-card {
  border: 1px solid var(--line-soft);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0)),
    var(--bg-panel);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.section-head p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.summary-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.summary-chip {
  border: 1px solid rgba(57, 200, 255, 0.14);
  border-radius: 999px;
  padding: 8px 12px;
  color: var(--text-secondary);
  font-size: 13px;
  background: rgba(57, 200, 255, 0.06);
}

.user-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(57, 200, 255, 0.05);
  --el-table-border-color: rgba(150, 206, 255, 0.1);
  --el-table-row-hover-bg-color: rgba(57, 200, 255, 0.04);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

.title-cell {
  display: grid;
  gap: 6px;
}

.title-cell strong {
  font-size: 15px;
  font-weight: 600;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 13px;
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
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 600;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    color 0.2s ease;
}

.action-button:not(.is-disabled):hover {
  transform: translateY(-1px);
}

.action-button--primary {
  border-color: rgba(57, 200, 255, 0.45);
  color: #06131d;
  background: linear-gradient(135deg, #39c8ff, #7be7ff);
  box-shadow: 0 10px 24px rgba(57, 200, 255, 0.22);
}

.action-button--primary:not(.is-disabled):hover {
  border-color: rgba(123, 231, 255, 0.85);
  box-shadow: 0 14px 28px rgba(57, 200, 255, 0.28);
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

.action-button.is-disabled.action-button--primary,
.action-button--primary.is-disabled,
.action-button--primary[disabled] {
  border-color: rgba(148, 163, 184, 0.14);
  color: rgba(206, 216, 230, 0.42);
  background: rgba(148, 163, 184, 0.04);
  box-shadow: none;
}

.dialog-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.dialog-head h3 {
  margin: 0;
  font-size: 18px;
}

.dialog-head p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
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
  color: var(--danger);
  font-size: 12px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 960px) {
  .section-head {
    flex-direction: column;
  }

  .dialog-head {
    flex-direction: column;
  }
}
</style>
