<template>
  <div class="not-found">
    <el-card class="not-found-card">
      <div class="not-found-content">
        <h1 class="not-found-title">404</h1>
        <h2 class="not-found-subtitle">页面不存在</h2>
        <p class="not-found-message">抱歉，您访问的页面不存在或已被删除</p>
        <p class="redirect-hint">{{ countdown }} 秒后自动返回首页</p>
        <div class="not-found-actions">
          <router-link to="/" class="not-found-link">
            <el-button type="primary" size="large">返回首页</el-button>
          </router-link>
          <el-button size="large" @click="router.back()">返回上一页</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const countdown = ref(5)
let timer = null

onMounted(() => {
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      router.push('/')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 2rem;
}

.not-found-card {
  max-width: 500px;
  width: 100%;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.not-found-content {
  padding: 3rem 2rem;
}

.not-found-title {
  font-size: 6rem;
  font-weight: bold;
  color: #409eff;
  margin: 0;
  line-height: 1;
}

.not-found-subtitle {
  font-size: 1.5rem;
  color: #303133;
  margin: 1rem 0;
}

.not-found-message {
  color: #606266;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.redirect-hint {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 2rem;
}

.not-found-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.not-found-link {
  display: inline-block;
}
</style>