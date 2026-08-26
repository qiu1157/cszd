<template>
  <section class="login-screen">
    <div class="login-card">
      <div class="hero-panel">
        <div class="energy-badge">Energy Ops Console</div>
        <h1 class="hero-title">充电站管理系统</h1>
        <p class="hero-desc">
          统一运营后台（PC 优先，手机自适应）。支持站点管理、桩管理、股东与持股关系、股权调整、平台字典与日志审计。
        </p>

        <div class="hero-grid">
          <div class="hero-metric">
            <small>站点总数</small>
            <strong>28</strong>
          </div>
          <div class="hero-metric">
            <small>充电桩总数</small>
            <strong>412</strong>
          </div>
          <div class="hero-metric">
            <small>股东主档</small>
            <strong>36</strong>
          </div>
        </div>

        <div class="hero-stage">
          <div class="stage-line">
            <span>01. 账号密码登录</span>
            <strong>已覆盖</strong>
          </div>
          <div class="stage-line">
            <span>02. 站点与股权双主线管理</span>
            <strong>开发中</strong>
          </div>
          <div class="stage-line">
            <span>03. 日志时间线与审计中心</span>
            <strong>开发中</strong>
          </div>
        </div>
      </div>

      <div class="login-panel">
        <div class="panel-kicker">账号密码登录</div>
        <h2 class="panel-title">登录进入系统</h2>
        <p class="panel-desc">输入账号与密码登录。系统管理员默认账号为 admin。</p>

        <el-form
          ref="formRef"
          class="login-form"
          :model="formModel"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item label="账号 / 手机号" prop="account">
            <el-input v-model="formModel.account" placeholder="请输入账号或手机号" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="formModel.password" show-password placeholder="请输入密码" type="password" />
          </el-form-item>

          <el-button class="submit-btn" type="primary" :loading="submitting" @click="handleSubmit">进入系统</el-button>

          <div class="quick-actions">
            <el-button @click="fillAdminAccount">填充 admin 账号</el-button>
            <el-button @click="fillBusinessAccount">填充业务管理员</el-button>
          </div>
        </el-form>

        <div class="mock-account">
          <strong>演示账号</strong><br />
          系统管理员：`admin / 123456`<br />
          业务管理员：`13800138000 / 123456`
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const formModel = reactive({
  account: 'admin',
  password: '123456',
})

const rules: FormRules<typeof formModel> = {
  account: [{ required: true, message: '请输入账号或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const fallbackPath = await authStore.signIn({
      account: formModel.account.trim(),
      password: formModel.password,
    })
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : fallbackPath
    router.push(redirect)
  } catch {
    ElMessage.error('登录失败，请检查账号和密码')
  } finally {
    submitting.value = false
  }
}

function fillAdminAccount() {
  formModel.account = 'admin'
  formModel.password = '123456'
}

function fillBusinessAccount() {
  formModel.account = '13800138000'
  formModel.password = '123456'
}
</script>

<style scoped lang="scss">
.login-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(31, 242, 178, 0.12), transparent 30%),
    radial-gradient(circle at top right, rgba(57, 200, 255, 0.16), transparent 34%),
    var(--bg-page);
}

.login-card {
  width: min(1180px, 100%);
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  border: 1px solid rgba(94, 139, 179, 0.18);
  border-radius: 28px;
  overflow: hidden;
  background: rgba(8, 18, 28, 0.88);
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.32);
}

.hero-panel,
.login-panel {
  padding: 40px;
}

.hero-panel {
  background:
    linear-gradient(180deg, rgba(13, 30, 46, 0.92), rgba(7, 18, 29, 0.98)),
    var(--bg-panel);
}

.energy-badge {
  display: inline-flex;
  border: 1px solid rgba(57, 200, 255, 0.24);
  border-radius: 999px;
  padding: 8px 12px;
  color: var(--brand-primary);
  background: rgba(57, 200, 255, 0.08);
  font-size: 13px;
}

.hero-title {
  margin: 18px 0 10px;
  font-size: 38px;
  line-height: 1.2;
}

.hero-desc,
.panel-desc {
  color: var(--text-secondary);
  line-height: 1.7;
}

.hero-grid {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.hero-metric {
  border: 1px solid rgba(57, 200, 255, 0.14);
  border-radius: 20px;
  padding: 20px;
  background: rgba(57, 200, 255, 0.06);
}

.hero-metric small {
  display: block;
  margin-bottom: 10px;
  color: var(--text-secondary);
}

.hero-metric strong {
  font-size: 32px;
}

.hero-stage {
  margin-top: 28px;
  display: grid;
  gap: 12px;
}

.stage-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid rgba(57, 200, 255, 0.1);
  padding-bottom: 12px;
  color: var(--text-secondary);
}

.stage-line strong {
  color: var(--brand-accent);
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(11, 22, 34, 0.96);
}

.panel-kicker {
  color: var(--brand-primary);
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.panel-title {
  margin: 14px 0 10px;
  font-size: 32px;
}

.login-form {
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.quick-actions {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mock-account {
  margin-top: 20px;
  border: 1px dashed rgba(57, 200, 255, 0.16);
  border-radius: 20px;
  padding: 16px;
  color: var(--text-secondary);
  line-height: 1.7;
  background: rgba(57, 200, 255, 0.04);
}

@media (max-width: 960px) {
  .login-screen {
    padding: 16px;
  }

  .login-card {
    grid-template-columns: 1fr;
  }

  .hero-panel,
  .login-panel {
    padding: 24px;
  }

  .hero-title,
  .panel-title {
    font-size: 28px;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
