<template>
  <div class="order-success">
    <el-card class="order-success-card">
      <div class="order-success-content">
        <div class="success-icon">
          <el-icon class="success-icon-inner"><Check /></el-icon>
        </div>
        <h1 class="success-title">下单成功！</h1>
        <p class="order-info">订单号为：<span class="order-no">{{ orderNo }}</span></p>
        <p class="redirect-hint">{{ countdown }} 秒后自动返回首页</p>
        <div class="success-actions">
          <router-link to="/" class="action-link">
            <el-button type="primary" size="large">返回首页</el-button>
          </router-link>
          <router-link to="/cart" class="action-link">
            <el-button size="large">查看购物车</el-button>
          </router-link>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const orderNo = ref('')
const countdown = ref(5)
let timer = null

onMounted(() => {
  orderNo.value = route.query.orderNo || ''

  if (!orderNo.value) {
    router.push('/')
    return
  }

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
.order-success {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 2rem;
}

.order-success-card {
  max-width: 500px;
  width: 100%;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-success-content {
  padding: 3rem 2rem;
}

.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #f0f9eb;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 2rem;
}

.success-icon-inner {
  font-size: 40px;
  color: #67c23a;
}

.success-title {
  font-size: 1.75rem;
  color: #303133;
  margin: 0 0 1.5rem;
}

.order-info {
  font-size: 1.1rem;
  color: #606266;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.redirect-hint {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 2rem;
}

.order-no {
  font-weight: bold;
  color: #409eff;
  margin-left: 0.5rem;
}

.success-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.action-link {
  display: inline-block;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .success-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>