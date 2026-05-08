<template>
  <div v-if="error" class="error-container">
    <el-alert :title="error" type="error" show-icon />
    <el-button type="primary" @click="router.push('/')" style="margin-top: 1rem;">
      返回商品列表
    </el-button>
  </div>
  <div v-else-if="loading" class="loading">
    <el-skeleton :rows="10" animated />
  </div>
  <div v-else-if="product" class="product-detail">
    <el-row :gutter="20" class="product-detail-container">
      <!-- 商品图片 -->
      <el-col :span="12">
        <el-card :body-style="{ padding: '2rem', textAlign: 'center' }">
          <el-image
            :src="product.image"
            :alt="product.name"
            fit="contain"
            style="max-height: 500px;"
          />
        </el-card>
      </el-col>
      
      <!-- 商品详情 -->
      <el-col :span="12">
        <div class="product-detail-info">
          <h1>{{ product.name }}</h1>
          <div class="product-detail-meta">
            <el-tag size="medium">{{ product.category }}</el-tag>
            <el-tag size="medium" type="info">{{ product.brand }}</el-tag>
            <el-tag 
              size="medium" 
              :type="product.stock > 0 ? (product.stock < 10 ? 'warning' : 'success') : 'danger'"
            >
              {{ product.stock > 0 ? `库存: ${product.stock}` : '缺货' }}
            </el-tag>
          </div>
          <div class="product-detail-price">¥{{ product.price.toFixed(2) }}</div>
          <div class="product-detail-description">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>
          <div class="product-detail-actions">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              :step="1"
              size="large"
              style="width: 120px;"
            />
            <el-button 
              type="primary" 
              size="large" 
              @click="addToCart" 
              :disabled="product.stock <= 0"
              style="margin-left: 1rem;"
            >
              加入购物车
            </el-button>
            <router-link to="/">
              <el-button size="large" style="margin-left: 1rem;">
                返回列表
              </el-button>
            </router-link>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productAPI } from '../api/api.js'

// 接收路由参数
const props = defineProps(['id'])
const router = useRouter()

// 商品数据
const product = ref(null)

// 加载状态
const loading = ref(true)

// 错误信息
const error = ref('')

// 购买数量
const quantity = ref(1)

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await productAPI.getProductById(props.id)
    if (response.success) {
      product.value = response.data
    }
  } catch (err) {
    error.value = '获取商品详情失败，请稍后重试'
    console.error('Error fetching product detail:', err)
  } finally {
    loading.value = false
  }
}

// 添加到购物车
const addToCart = () => {
  if (!product.value || product.value.stock <= 0) return
  
  // 从localStorage获取购物车数据
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  
  // 检查商品是否已在购物车中
  const existingItemIndex = cart.findIndex(item => item.id === product.value.id)
  
  if (existingItemIndex !== -1) {
    // 已存在，增加数量
    cart[existingItemIndex].quantity += quantity.value
  } else {
    // 不存在，添加新商品
    cart.push({
      id: product.value.id,
      name: product.value.name,
      price: product.value.price,
      image: product.value.image,
      quantity: quantity.value
    })
  }
  
  // 保存到localStorage
  localStorage.setItem('cart', JSON.stringify(cart))
  
  // 触发storage事件，更新App组件中的购物车数量
  window.dispatchEvent(new Event('storage'))
  
  // 提示用户
  alert('商品已加入购物车')
  
  // 重置数量
  quantity.value = 1
}

// 页面加载时获取商品详情
onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.product-detail-container {
  margin-bottom: 2rem;
}

.product-detail-info {
  padding: 1rem 0;
}

.product-detail-meta {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.product-detail-price {
  font-size: 2.5rem;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 2rem;
}

.product-detail-description {
  margin-bottom: 2rem;
}

.product-detail-description h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #2c3e50;
}

.product-detail-description p {
  line-height: 1.6;
  color: #333;
}

.product-detail-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.loading {
  padding: 3rem;
}

.error-container {
  padding: 3rem;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .product-detail-price {
    font-size: 2rem;
  }
}

@media (max-width: 768px) {
  .product-detail-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .product-detail-actions .el-button {
    margin-left: 0 !important;
    margin-top: 1rem;
  }
  
  .product-detail-actions .el-input-number {
    align-self: center;
  }
}
</style>