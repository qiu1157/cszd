<template>
  <el-card class="placeholder">
    <div class="title">{{ title }}</div>
    <div class="desc">{{ desc }}</div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  desc: string
}>()
</script>

<style scoped lang="scss">
.placeholder {
  border: 1px solid var(--line-soft);
  background: var(--bg-panel);
}

.title {
  font-size: 16px;
  margin-bottom: 8px;
}

.desc {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>

