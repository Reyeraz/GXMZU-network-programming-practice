<template>
  <div class="checkout">
    <h1>确认订单</h1>
    
    <!-- 购物车为空时 -->
    <div v-if="cartItems.length === 0" class="checkout-empty">
      <el-empty description="您的购物车是空的，无法结算">
        <router-link to="/">
          <el-button type="primary">去购物</el-button>
        </router-link>
      </el-empty>
    </div>
    
    <!-- 购物车有商品时 -->
    <div v-else class="checkout-container">
      <!-- 收货信息表单 -->
      <el-card class="checkout-section">
        <template #header>
          <div class="card-header">
            <h2>收货信息</h2>
          </div>
        </template>
        <el-form :model="shippingInfo" label-width="80px" class="shipping-form">
          <el-form-item label="姓名" prop="name" :rules="[{ required: true, message: '请输入您的姓名' }]">
            <el-input v-model="shippingInfo.name" placeholder="请输入您的姓名" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phone" :rules="[{ required: true, message: '请输入您的手机号码' }]">
            <el-input v-model="shippingInfo.phone" placeholder="请输入您的手机号码" />
          </el-form-item>
          <el-form-item label="详细地址" prop="address" :rules="[{ required: true, message: '请输入您的详细地址' }]">
            <el-input v-model="shippingInfo.address" placeholder="请输入您的详细地址" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="城市" prop="city" :rules="[{ required: true, message: '请输入城市' }]">
                <el-input v-model="shippingInfo.city" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮编" prop="zipcode" :rules="[{ required: true, message: '请输入邮编' }]">
                <el-input v-model="shippingInfo.zipcode" placeholder="请输入邮编" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
      
      <!-- 订单商品列表 -->
      <el-card class="checkout-section">
        <template #header>
          <div class="card-header">
            <h2>订单商品</h2>
          </div>
        </template>
        <div class="order-items">
          <el-divider v-for="(item, index) in cartItems" :key="item.id" :content-position="'center'" v-if="index > 0" />
          <div 
            v-for="item in cartItems" 
            :key="item.id" 
            class="order-item"
          >
            <el-row :gutter="20" type="flex" align="middle">
              <el-col :span="18">
                <div class="order-item-info">
                  <div class="order-item-name">{{ item.name }}</div>
                  <div class="order-item-quantity">x{{ item.quantity }}</div>
                </div>
              </el-col>
              <el-col :span="6" class="order-item-price">
                ¥{{ (item.price * item.quantity).toFixed(2) }}
              </el-col>
            </el-row>
          </div>
        </div>
      </el-card>
      
      <!-- 订单金额 -->
      <el-card class="checkout-section">
        <template #header>
          <div class="card-header">
            <h2>订单金额</h2>
          </div>
        </template>
        <div class="order-summary">
          <div class="summary-item">
            <span>商品总价：</span>
            <span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>运费：</span>
            <span>{{ shippingFee > 0 ? '¥' + shippingFee.toFixed(2) : '免运费' }}</span>
          </div>
          <el-divider content-position="'center'" />
          <div class="summary-item summary-total">
            <span>应付金额：</span>
            <span>¥{{ (totalPrice + shippingFee).toFixed(2) }}</span>
          </div>
        </div>
      </el-card>
      
      <!-- 结算按钮 -->
      <div class="checkout-actions">
        <router-link to="/cart">
          <el-button size="large">返回购物车</el-button>
        </router-link>
        <el-button type="primary" size="large" @click="placeOrder">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()

// 购物车商品（从 Pinia store 获取）
const cartItems = computed(() => cartStore.items)

// 收货信息
const shippingInfo = ref({
  name: '',
  phone: '',
  address: '',
  city: '',
  zipcode: ''
})

// 运费
const shippingFee = ref(0)

// 计算商品总价（从 Pinia store 获取）
const totalPrice = computed(() => cartStore.totalPrice)

// 计算运费
const calculateShippingFee = () => {
  // 简单的运费计算：订单金额超过100免运费，否则10元运费
  shippingFee.value = totalPrice.value >= 100 ? 0 : 10
}

// 提交订单
const placeOrder = () => {
  // 验证收货信息
  if (!shippingInfo.value.name || !shippingInfo.value.phone || !shippingInfo.value.address || !shippingInfo.value.city || !shippingInfo.value.zipcode) {
    alert('请填写完整的收货信息')
    return
  }
  
  // 生成订单号
  const orderNo = `ORD-${Date.now()}`
  
  // 创建订单数据
  const order = {
    id: orderNo,
    shippingInfo: { ...shippingInfo.value },
    items: [...cartItems.value],
    totalPrice: totalPrice.value,
    shippingFee: shippingFee.value,
    finalPrice: totalPrice.value + shippingFee.value,
    orderTime: new Date().toISOString()
  }
  
  // 保存订单到localStorage（实际项目中会发送到服务器）
  const orders = JSON.parse(localStorage.getItem('orders') || '[]')
  orders.push(order)
  localStorage.setItem('orders', JSON.stringify(orders))

  // 使用 Pinia store 清空购物车
  cartStore.clearCart()
  
  // 跳转到订单成功页
  router.push({
    path: '/order/success',
    query: { orderNo: orderNo }
  })
}

// 页面加载时加载购物车数据并计算运费
onMounted(() => {
  cartStore.loadCart()
  calculateShippingFee()
})
</script>

<style scoped>
.checkout {
  max-width: 1200px;
  margin: 0 auto;
}

.checkout-empty {
  padding: 4rem;
  margin-top: 2rem;
}

.checkout-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  margin-top: 2rem;
}

.checkout-section {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.checkout-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header h2 {
  font-size: 1.25rem;
  margin: 0;
  color: #2c3e50;
}

.order-items {
  display: flex;
  flex-direction: column;
}

.order-item {
  padding: 0.75rem 0;
}

.order-item-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-item-name {
  font-weight: 500;
  color: #2c3e50;
}

.order-item-quantity {
  font-size: 0.9rem;
  color: #666;
}

.order-item-price {
  font-weight: bold;
  color: #e74c3c;
  text-align: right;
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 1rem;
}

.summary-total {
  font-size: 1.25rem;
  font-weight: bold;
  color: #e74c3c;
}

.checkout-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkout-actions {
    flex-direction: column;
  }
  
  .checkout-actions .el-button {
    width: 100%;
  }
}
</style>