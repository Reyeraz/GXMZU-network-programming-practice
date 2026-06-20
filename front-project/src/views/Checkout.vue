<template>
  <div class="checkout">
    <h1>确认订单</h1>

    <!-- 无已选商品时 -->
    <div v-if="selectedItems.length === 0" class="checkout-empty">
      <el-empty description="没有已选中的商品，无法结算">
        <router-link to="/cart">
          <el-button type="primary">返回购物车</el-button>
        </router-link>
      </el-empty>
    </div>

    <!-- 有已选商品时 -->
    <div v-else class="checkout-container">
      <!-- 收货信息表单 -->
      <el-card class="checkout-section">
        <template #header>
          <div class="card-header">
            <h2>收货信息</h2>
          </div>
        </template>
        <el-form
          ref="formRef"
          :model="shippingInfo"
          :rules="formRules"
          label-width="80px"
          class="shipping-form"
        >
          <el-form-item label="收货人" prop="consignee">
            <el-input v-model="shippingInfo.consignee" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号码" prop="telephone">
            <el-input v-model="shippingInfo.telephone" placeholder="请输入手机号码" maxlength="11" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="城市" prop="city">
                <el-input v-model="shippingInfo.city" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="shippingInfo.address" placeholder="请输入详细地址" type="textarea" :rows="2" />
          </el-form-item>
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
          <div
            v-for="item in selectedItems"
            :key="item.cartId"
            class="order-item"
          >
            <el-row :gutter="20" align="middle">
              <el-col :span="2">
                <el-image
                  :src="placeholderImage"
                  :alt="item.name"
                  fit="cover"
                  style="width: 50px; height: 50px; border-radius: 4px;"
                />
              </el-col>
              <el-col :span="14">
                <div class="order-item-info">
                  <div class="order-item-name">{{ item.name }}</div>
                  <div class="order-item-quantity">x{{ item.quantity }}</div>
                </div>
              </el-col>
              <el-col :span="8" class="order-item-price">
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
          <el-divider />
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
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="placeOrder"
        >
          提交订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { orderAPI } from '../api/api'
// 购物车后端不返回图片，使用占位图
const placeholderImage = '/placeholder.png'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 表单引用
const formRef = ref(null)

// 提交中状态
const submitting = ref(false)

// 购物车中已选中的商品
const selectedItems = computed(() =>
  cartStore.items.filter(item => item.selected === 1)
)

// 已选商品总价
const totalPrice = computed(() => cartStore.totalPrice)

// 运费
const shippingFee = computed(() => totalPrice.value >= 100 ? 0 : 10)

// 收货信息（匹配后端 OrderCreateRequest 字段）
const shippingInfo = ref({
  consignee: '',
  telephone: '',
  city: '',
  address: ''
})

// 表单校验规则
const formRules = {
  consignee: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  telephone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 提交订单
const placeOrder = async () => {
  // 表单校验
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 检查是否有已选商品
  if (selectedItems.value.length === 0) {
    ElMessage.warning('没有已选中的商品')
    return
  }

  submitting.value = true
  try {
    const res = await orderAPI.createOrder({
      consignee: shippingInfo.value.consignee,
      telephone: shippingInfo.value.telephone,
      city: shippingInfo.value.city,
      address: shippingInfo.value.address
    })

    if (res.success) {
      ElMessage.success('下单成功！')
      // 跳转到订单成功页，传递订单ID
      router.push({
        path: '/order/success',
        query: {
          orderId: res.data.orderId,
          payAmount: res.data.payAmount
        }
      })
    }
  } catch (err) {
    console.error('创建订单失败:', err)
  } finally {
    submitting.value = false
  }
}

// 页面加载时
onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  cartStore.loadCart()
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
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
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
