<template>
  <section class="page-section">
    <el-card class="account-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>我的账号</h3>
            <p>查看当前账号信息，并在安全设置中完成本人密码修改。</p>
          </div>
        </div>
      </template>

      <div v-for="section in sections" :key="section.title" class="account-section">
        <div class="account-section__head">
          <h4>{{ section.title }}</h4>
        </div>

        <div v-if="section.title !== '安全设置'" class="account-grid">
          <div v-for="item in section.items" :key="item.label" class="account-field">
            <span class="account-field__label">{{ item.label }}</span>
            <strong class="account-field__value">{{ item.value }}</strong>
          </div>
        </div>

        <div v-else class="account-security-card">
          <div>
            <div class="account-field__label">{{ section.items[0].label }}</div>
            <strong class="account-field__value">{{ section.items[0].value }}</strong>
          </div>
          <el-button type="primary" @click="openDialog">修改本人密码</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      width="520px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
      align-center
      @closed="resetDialogState"
    >
      <template #header>
        <div class="account-dialog-head">
          <h3>修改本人密码</h3>
          <p>请输入旧密码，并设置 6-20 位且同时包含字母和数字的新密码。</p>
        </div>
      </template>

      <div class="account-dialog-form">
        <div class="account-dialog-field">
          <label for="current-password">旧密码</label>
          <el-input
            id="current-password"
            v-model="form.currentPassword"
            type="password"
            show-password
            autocomplete="current-password"
            placeholder="请输入旧密码"
          />
          <p v-if="errors.currentPassword" class="field-error">{{ errors.currentPassword }}</p>
        </div>

        <div class="account-dialog-field">
          <label for="new-password">新密码</label>
          <el-input
            id="new-password"
            v-model="form.newPassword"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请输入新密码"
          />
          <p v-if="errors.newPassword" class="field-error">{{ errors.newPassword }}</p>
        </div>

        <div class="account-dialog-field">
          <label for="confirm-password">确认新密码</label>
          <el-input
            id="confirm-password"
            v-model="form.confirmPassword"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请再次输入新密码"
          />
          <p v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button :disabled="submitting" @click="closeDialog">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitChangePassword">
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

import { buildAccountSections } from './account-view'
import {
  buildChangePasswordPayload,
  type ChangePasswordForm,
  type ChangePasswordFormErrors,
  validateChangePasswordForm,
} from './change-password'

const router = useRouter()
const authStore = useAuthStore()
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref<ChangePasswordForm>(createInitialForm())
const errors = ref<ChangePasswordFormErrors>({})

onMounted(async () => {
  await authStore.refreshCurrentUser()
})

const sections = computed(() => (authStore.user ? buildAccountSections(authStore.user) : []))

function openDialog() {
  resetDialogState()
  dialogVisible.value = true
}

function closeDialog() {
  if (submitting.value) {
    return
  }
  dialogVisible.value = false
}

function resetDialogState() {
  form.value = createInitialForm()
  errors.value = {}
}

async function submitChangePassword() {
  errors.value = validateChangePasswordForm(form.value)
  if (Object.keys(errors.value).length > 0) {
    return
  }

  submitting.value = true
  try {
    await changePassword(buildChangePasswordPayload(form.value))
    dialogVisible.value = false
    ElMessage.success('密码修改成功，请重新登录')
    authStore.signOut()
    await router.push('/login')
  } catch (error) {
    ElMessage.error(resolveApiErrorMessage(error, '密码修改失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}

function createInitialForm(): ChangePasswordForm {
  return {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
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

.account-page-card {
  border: 1px solid rgba(123, 231, 255, 0.12);
  background: linear-gradient(180deg, rgba(8, 15, 28, 0.94), rgba(5, 11, 22, 0.96));

  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(123, 231, 255, 0.12);
    padding: 24px 28px 18px;
  }

  :deep(.el-card__body) {
    display: grid;
    gap: 20px;
    padding: 24px 28px 28px;
  }
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;

  h3 {
    margin: 0;
    font-size: 22px;
    color: #f4f7fb;
  }

  p {
    margin: 8px 0 0;
    color: rgba(214, 225, 240, 0.72);
    font-size: 14px;
    line-height: 1.6;
  }
}

.account-section {
  padding: 22px 24px;
  border: 1px solid rgba(123, 231, 255, 0.12);
  border-radius: 18px;
  background: rgba(8, 16, 30, 0.72);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

.account-section__head {
  margin-bottom: 18px;

  h4 {
    margin: 0;
    font-size: 16px;
    color: #f4f7fb;
  }
}

.account-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.account-field {
  display: grid;
  gap: 8px;
  min-height: 76px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(10, 19, 35, 0.9);
  border: 1px solid rgba(123, 231, 255, 0.1);
}

.account-field__label {
  font-size: 12px;
  color: rgba(169, 184, 201, 0.72);
}

.account-field__value {
  color: #f4f7fb;
  font-size: 15px;
  line-height: 1.5;
}

.account-security-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: rgba(10, 19, 35, 0.9);
  border: 1px solid rgba(123, 231, 255, 0.1);
}

.account-dialog-head {
  display: grid;
  gap: 8px;

  h3 {
    margin: 0;
    font-size: 20px;
    color: #f4f7fb;
  }

  p {
    margin: 0;
    font-size: 14px;
    line-height: 1.6;
    color: rgba(214, 225, 240, 0.72);
  }
}

.account-dialog-form {
  display: grid;
  gap: 18px;
}

.account-dialog-field {
  display: grid;
  gap: 8px;

  label {
    font-size: 13px;
    color: rgba(214, 225, 240, 0.82);
  }
}

.field-error {
  margin: 0;
  font-size: 12px;
  color: #ff8f8f;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .account-page-card {
    :deep(.el-card__header) {
      padding: 20px 18px 14px;
    }

    :deep(.el-card__body) {
      padding: 18px;
    }
  }

  .account-section {
    padding: 18px;
  }

  .account-security-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .dialog-actions {
    width: 100%;
    justify-content: stretch;

    :deep(.el-button) {
      flex: 1;
    }
  }
}
</style>
