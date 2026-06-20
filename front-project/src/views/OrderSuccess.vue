<template>
  <div class="order-success">
    <el-card class="order-success-card">
      <div class="order-success-content">
        <div class="success-icon">
          <el-icon class="success-icon-inner"><Check /></el-icon>
        </div>
        <h1 class="success-title">下单成功！</h1>

        <div v-if="orderInfo" class="order-detail">
          <p class="order-info">订单号：<span class="order-no">{{ orderInfo.orderId }}</span></p>
          <p class="order-info">订单状态：<el-tag :type="statusType" size="small">{{ statusText }}</el-tag></p>
          <p class="order-info">应付金额：<span class="order-amount">¥{{ orderInfo.payAmount }}</span></p>
          <p class="order-info">下单时间：{{ formatTime(orderInfo.orderDate) }}</p>
        </div>

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { orderAPI } from '../api/api'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const orderInfo = ref(null)
const countdown = ref(5)
let timer = null

// 订单状态映射
const statusText = computed(() => {
  const map = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[orderInfo.value?.status] || '未知'
})

const statusType = computed(() => {
  const map = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'success', 4: 'info' }
  return map[orderInfo.value?.status] || 'info'
})

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 获取订单详情
const fetchOrderDetail = async (orderId) => {
  try {
    const res = await orderAPI.getOrderDetail(orderId)
    if (res.success) {
      orderInfo.value = res.data
    }
  } catch (err) {
    console.error('获取订单详情失败:', err)
  }
}

onMounted(() => {
  const orderId = route.query.orderId

  if (!orderId) {
    router.push('/')
    return
  }

  // 如果有 payAmount 参数（从 Checkout 跳转过来），直接显示
  const payAmount = route.query.payAmount
  if (payAmount) {
    orderInfo.value = {
      orderId: Number(orderId),
      payAmount: payAmount,
      status: 0,
      orderDate: new Date().toISOString()
    }
  }

  // 从后端获取完整订单详情
  fetchOrderDetail(Number(orderId))

  // 重新加载购物车（下单后购物车已清空已选商品）
  cartStore.loadCart()

  // 倒计时
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

.order-detail {
  margin-bottom: 1.5rem;
}

.order-info {
  font-size: 1rem;
  color: #606266;
  margin-bottom: 0.5rem;
  line-height: 1.8;
}

.order-no {
  font-weight: bold;
  color: #409eff;
  margin-left: 0.5rem;
}

.order-amount {
  font-weight: bold;
  color: #e74c3c;
  font-size: 1.1rem;
}

.redirect-hint {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 2rem;
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
