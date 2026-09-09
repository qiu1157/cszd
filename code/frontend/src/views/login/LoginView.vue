<template>
  <section class="login-screen">
    <div class="login-card">
      <div class="hero-panel">
        <div class="energy-badge">
          <span class="pulse-dot"></span>
          Energy Ops Console
        </div>
        <h1 class="hero-title">长沙众电<br /><span class="hero-title-sub">充电站运营系统</span></h1>
        <p class="hero-desc">
          长沙众电运营管理后台，支持充电站的桩、物、人、运营、票据统一管理与审计
        </p>

        <div class="hero-poster">
          <img src="/poster-growth.jpg" alt="充电站运营业绩增长海报" />
        </div>

        <div class="hero-grid">
          <div class="hero-metric">
            <small>站点总数</small>
            <strong>{{ statistics.stationCount }}</strong>
          </div>
          <div class="hero-metric">
            <small>充电桩总数</small>
            <strong>{{ statistics.pileCount }}</strong>
          </div>
          <div class="hero-metric">
            <small>客户数</small>
            <strong>{{ statistics.shareholderCount }}</strong>
          </div>
        </div>
      </div>

      <div class="login-panel">
        <div class="panel-kicker">账号密码登录</div>
        <h2 class="panel-title">登录进入系统</h2>

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
        </el-form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getStationStatistics } from '@/api/station'
import { getPileStatistics } from '@/api/pile'
import { getShareholderStatistics } from '@/api/shareholder'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const formModel = reactive({
  account: 'admin',
  password: '',
})

const statistics = reactive({
  stationCount: 0,
  pileCount: 0,
  shareholderCount: 0,
})

const rules: FormRules<typeof formModel> = {
  account: [{ required: true, message: '请输入账号或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function loadStatistics() {
  try {
    const [stationStats, pileStats, shareholderStats] = await Promise.all([
      getStationStatistics(),
      getPileStatistics(),
      getShareholderStatistics(),
    ])
    statistics.stationCount = stationStats.totalCount
    statistics.pileCount = pileStats.totalCount
    statistics.shareholderCount = shareholderStats.totalCount
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
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

onMounted(() => {
  loadStatistics()
})
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
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(57, 200, 255, 0.24);
  border-radius: 999px;
  padding: 8px 12px;
  color: var(--brand-primary);
  background: rgba(57, 200, 255, 0.08);
  font-size: 13px;
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--brand-primary);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

.hero-title {
  margin: 18px 0 10px;
  font-size: 38px;
  line-height: 1.2;
}

.hero-title-sub {
  font-size: 28px;
  color: var(--text-secondary);
  font-weight: normal;
}

.hero-desc {
  color: var(--text-secondary);
  line-height: 1.7;
}

.hero-poster {
  margin: 24px 0;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(57, 200, 255, 0.06);
  border: 1px solid rgba(57, 200, 255, 0.14);
}

.hero-poster img {
  width: 100%;
  height: auto;
  display: block;
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
}
</style>
