<template>
  <div class="cart">
    <h1>购物车</h1>
    
    <!-- 购物车为空时 -->
    <div v-if="cartItems.length === 0" class="cart-empty">
      <el-empty description="您的购物车是空的">
        <router-link to="/">
          <el-button type="primary">去购物</el-button>
        </router-link>
      </el-empty>
    </div>
    
    <!-- 购物车有商品时 -->
    <div v-else class="cart-container">
      <!-- 购物车商品列表 -->
      <div class="cart-items">
        <el-card 
          v-for="item in cartItems" 
          :key="item.id" 
          class="cart-item"
          :body-style="{ padding: '1rem' }"
        >
          <el-row :gutter="20" type="flex" align="middle">
            <el-col :span="4">
              <div class="cart-item-image">
                <el-image
                  :src="item.image"
                  :alt="item.name"
                  fit="cover"
                  style="width: 100px; height: 100px;"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="cart-item-info">
                <h3 class="cart-item-name">{{ item.name }}</h3>
                <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>
                <div class="cart-item-actions">
                  <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :step="1"
                    size="small"
                    style="width: 100px;"
                    @change="updateCart"
                  />
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="removeItem(item)"
                    style="margin-left: 1rem;"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </el-col>
            <el-col :span="8" class="cart-item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </el-col>
          </el-row>
        </el-card>
      </div>
      
      <!-- 购物车结算 -->
      <el-card class="cart-summary">
        <h3>订单摘要</h3>
        <div class="summary-item">
          <span>商品总数：</span>
          <span>{{ totalQuantity }} 件</span>
        </div>
        <div class="summary-item">
          <span>商品总价：</span>
          <span>¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-item summary-total">
          <span>应付金额：</span>
          <span>¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-actions">
          <router-link to="/">
            <el-button size="large" style="width: 100%; margin-bottom: 0.75rem;">
              继续购物
            </el-button>
          </router-link>
          <router-link to="/checkout">
            <el-button type="primary" size="large" style="width: 100%;">
              去结算
            </el-button>
          </router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, watchEffect } from 'vue'

// 购物车商品
const cartItems = ref([])

// 从localStorage获取购物车数据
const loadCart = () => {
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  cartItems.value = cart
}

// 保存购物车数据到localStorage
const saveCart = () => {
  localStorage.setItem('cart', JSON.stringify(cartItems.value))
  // 触发storage事件，更新App组件中的购物车数量
  window.dispatchEvent(new Event('storage'))
}

// 更新购物车
const updateCart = () => {
  saveCart()
}

// 删除商品
const removeItem = (item) => {
  const index = cartItems.value.findIndex(cartItem => cartItem.id === item.id)
  if (index !== -1) {
    cartItems.value.splice(index, 1)
    saveCart()
  }
}

// 计算商品总数
const totalQuantity = computed(() => {
  return cartItems.value.reduce((total, item) => total + item.quantity, 0)
})

// 计算商品总价
const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

// 页面加载时获取购物车数据
onMounted(() => {
  loadCart()
})

// 监听购物车数据变化，保存到localStorage
watchEffect(() => {
  saveCart()
})

// 监听总价变化，当超过3000元时弹窗提示
watch(totalPrice, (newValue) => {
  if (newValue > 3000) {
    alert('已满⾜包邮条件')
  }
})
</script>

<style scoped>
.cart {
  max-width: 1200px;
  margin: 0 auto;
}

.cart-empty {
  padding: 4rem;
  margin-top: 2rem;
}

.cart-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  margin-top: 2rem;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cart-item-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.cart-item-info {
  flex: 1;
}

.cart-item-name {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.cart-item-price {
  font-size: 1.2rem;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 1rem;
}

.cart-item-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.cart-item-subtotal {
  font-size: 1.2rem;
  font-weight: bold;
  color: #2c3e50;
  text-align: right;
}

.cart-summary {
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.cart-summary h3 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  font-size: 1rem;
}

.summary-total {
  font-size: 1.25rem;
  font-weight: bold;
  padding-top: 1rem;
  border-top: 1px solid #eee;
  margin-top: 1rem;
  color: #e74c3c;
}

.summary-actions {
  margin-top: 2rem;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .cart-container {
    grid-template-columns: 1fr;
  }
  
  .cart-summary {
    position: static;
  }
}

@media (max-width: 768px) {
  .cart-item-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .cart-item-actions .el-button {
    margin-left: 0 !important;
    margin-top: 0.75rem;
  }
  
  .cart-item-actions .el-input-number {
    align-self: center;
  }
}
</style>