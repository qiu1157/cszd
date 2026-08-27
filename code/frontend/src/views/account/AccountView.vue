<template>
  <section class="page-section">
    <el-card class="account-page-card" shadow="never">
      <template #header>
        <div class="section-head">
          <div>
            <h3>我的账号</h3>
            <p>查看当前账号信息；修改本人密码将在下一个任务接入。</p>
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
          <el-button disabled>修改本人密码</el-button>
        </div>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import { useAuthStore } from '@/stores/auth'

import { buildAccountSections } from './account-view'

const authStore = useAuthStore()

onMounted(async () => {
  await authStore.refreshCurrentUser()
})

const sections = computed(() => (authStore.user ? buildAccountSections(authStore.user) : []))
</script>

<style scoped lang="scss">
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
}
</style>
