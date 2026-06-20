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
        <!-- 全选/批量操作 -->
        <el-card class="cart-toolbar" :body-style="{ padding: '0.75rem 1rem' }">
          <el-row align="middle">
            <el-col :span="12">
              <el-checkbox
                v-model="isAllSelected"
                :indeterminate="isIndeterminate"
                @change="handleSelectAll"
              >
                全选
              </el-checkbox>
            </el-col>
            <el-col :span="12" style="text-align: right;">
              <el-button
                type="danger"
                size="small"
                :disabled="selectedCount === 0"
                @click="handleDeleteSelected"
              >
                删除已选 ({{ selectedCount }})
              </el-button>
            </el-col>
          </el-row>
        </el-card>

        <!-- 商品列表 -->
        <el-card
          v-for="item in cartItems"
          :key="item.cartId"
          class="cart-item"
          :body-style="{ padding: '1rem' }"
        >
          <el-row :gutter="20" align="middle">
            <el-col :span="1">
              <el-checkbox
                :model-value="item.selected === 1"
                @change="(val: boolean) => handleToggleSelected(item, val)"
              />
            </el-col>
            <el-col :span="3">
              <div class="cart-item-image">
                <el-image
                  :src="placeholderImage"
                  :alt="item.name"
                  fit="cover"
                  style="width: 80px; height: 80px;"
                />
              </div>
            </el-col>
            <el-col :span="10">
              <div class="cart-item-info">
                <h3 class="cart-item-name">{{ item.name }}</h3>
                <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>
                <div v-if="item.isAvailable === 0" class="cart-item-warning">
                  <el-tag type="danger" size="small">已下架</el-tag>
                </div>
                <div v-else-if="item.stock < item.quantity" class="cart-item-warning">
                  <el-tag type="warning" size="small">库存不足 (剩余{{ item.stock }})</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="5">
              <el-input-number
                :model-value="item.quantity"
                :min="1"
                :max="item.stock"
                :step="1"
                size="small"
                style="width: 120px;"
                @change="(val: number) => handleUpdateQuantity(item, val)"
              />
            </el-col>
            <el-col :span="3" class="cart-item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </el-col>
            <el-col :span="2" style="text-align: center;">
              <el-button
                type="danger"
                size="small"
                circle
                @click="handleRemoveItem(item)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
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
          <span>已选商品：</span>
          <span>{{ selectedCount }} 件</span>
        </div>
        <div class="summary-item">
          <span>已选总价：</span>
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
          <el-button
            type="primary"
            size="large"
            style="width: 100%; margin-bottom: 0.75rem;"
            :disabled="selectedCount === 0"
            @click="goCheckout"
          >
            去结算 ({{ selectedCount }})
          </el-button>
          <el-button
            type="danger"
            size="large"
            style="width: 100%;"
            @click="handleDeleteSelected"
            :disabled="selectedCount === 0"
          >
            清空已选商品
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
// 购物车后端不返回图片，使用占位图
const placeholderImage = '/placeholder.png'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 从 store 获取购物车数据
const cartItems = computed(() => cartStore.items)
const totalQuantity = computed(() => cartStore.totalQuantity)
const totalPrice = computed(() => cartStore.totalPrice)
const selectedCount = computed(() => cartStore.selectedCount)

// 是否全选
const isAllSelected = computed(() =>
  cartItems.value.length > 0 && cartItems.value.every(item => item.selected === 1)
)

// 是否半选
const isIndeterminate = computed(() => {
  const selectedItems = cartItems.value.filter(item => item.selected === 1)
  return selectedItems.length > 0 && selectedItems.length < cartItems.value.length
})

// 全选/全不选
const handleSelectAll = async (val) => {
  await cartStore.batchUpdateSelected(val ? 1 : 0)
}

// 切换单个商品勾选状态
const handleToggleSelected = async (item, val) => {
  await cartStore.updateSelected(item.cartId, val ? 1 : 0)
}

// 更新商品数量
const handleUpdateQuantity = async (item, quantity) => {
  if (quantity === null || quantity === undefined) return
  await cartStore.updateQuantity(item.cartId, quantity)
}

// 删除单个商品
const handleRemoveItem = async (item) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${item.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeCartItem(item.cartId)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}

// 删除已选商品
const handleDeleteSelected = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除已选的${selectedCount.value}件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.deleteSelected()
    ElMessage.success('已清空已选商品')
  } catch {
    // 用户取消
  }
}

// 去结算
const goCheckout = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (selectedCount.value === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }
  router.push('/checkout')
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后查看购物车')
    router.push('/login')
    return
  }
  cartStore.loadCart()
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

.cart-toolbar {
  margin-bottom: 0.5rem;
}

.cart-item {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cart-item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.cart-item-info {
  flex: 1;
}

.cart-item-name {
  font-size: 1rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.cart-item-price {
  font-size: 1.1rem;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 0.25rem;
}

.cart-item-warning {
  margin-top: 0.25rem;
}

.cart-item-subtotal {
  font-size: 1.1rem;
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
}
</style>
