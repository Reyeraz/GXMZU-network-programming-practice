<template>
  <div class="product-list">
    <h1>商品列表</h1>
    
    <!-- 图片轮播 -->
    <ImageCarousel />
    
    <!-- 筛选和搜索区域 -->
    <div class="filter-container">
      <div class="category-filter">
        <el-select v-model="selectedCategory" placeholder="选择分类" @change="handleFilterChange">
          <el-option label="全部" value="" />
          <el-option 
            v-for="category in categories" 
            :key="category" 
            :label="category" 
            :value="category" 
          />
        </el-select>
      </div>
      <div class="search-container">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索商品名称、描述、品牌..." 
          @input="handleSearch"
          prefix-icon="el-icon-search"
          size="large"
          style="max-width: 400px;"
        />
      </div>
    </div>
    
    <!-- 跳转按钮 -->
    <div class="demo-button-container" style="margin: 1.5rem 0; text-align: center;">
      <el-button 
        type="primary" 
        @click="router.push('/first-demo')"
      >
        进入FirstDemo
      </el-button>
    </div>
    
    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="4" animated style="margin: 2rem 0;" />
    
    <!-- 错误提示 -->
    <el-alert v-else-if="error" :title="error" type="error" show-icon style="margin: 2rem 0;" />
    
    <!-- 商品网格 -->
    <div v-else class="products-grid">
      <el-card 
        v-for="product in products" 
        :key="product.id" 
        class="product-card"
        :body-style="{ padding: '0' }"
      >
        <div class="product-image">
          <img :src="getProductImageUrl(product.image)" :alt="product.name" />
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-description">{{ product.description }}</p>
          <div class="product-meta">
            <el-tag size="small">{{ product.category }}</el-tag>
            <el-tag size="small" type="info">{{ product.brand }}</el-tag>
          </div>
          <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
          <div class="product-actions">
            <router-link :to="'/product/' + product.id">
              <el-button type="default" plain>
                查看详情
              </el-button>
            </router-link>
            <el-button 
              type="primary" 
              @click="addToCart(product)"
              :disabled="product.stock <= 0"
            >
              {{ product.stock <= 0 ? '缺货' : '加入购物车' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 无结果提示 -->
    <el-empty v-if="!loading && !error && products.length === 0" description="未找到匹配的商品" />
    
    <!-- 分页组件 -->
    <div v-if="!loading && !error && total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[4, 8, 16]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productAPI } from '../api/api.js'
import ImageCarousel from '../components/ImageCarousel.vue'
import { useCartStore } from '../stores/cart'
import { getProductImageUrl } from '../utils/image'

// 路由实例
const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 选中的分类
const selectedCategory = ref('')

// 分类列表
const categories = ref([])

// 商品数据
const products = ref([])

// 总商品数
const total = ref(0)

// 当前页码
const currentPage = ref(1)

// 每页条数
const pageSize = ref(4)

// 加载状态
const loading = ref(false)

// 错误信息
const error = ref('')

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await productAPI.getCategories()
    if (response.success) {
      categories.value = response.data
    }
  } catch (err) {
    console.error('Error fetching categories:', err)
  }
}

// 获取商品列表（带分页和筛选）
const fetchProducts = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await productAPI.getProductsByPage({
      page: currentPage.value,
      pageSize: pageSize.value,
      category: selectedCategory.value || undefined,
      keyword: searchKeyword.value || undefined
    })
    if (response.success) {
      products.value = response.data.items
      total.value = response.data.total
    }
  } catch (err) {
    error.value = '获取商品数据失败，请稍后重试'
    console.error('Error fetching products:', err)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchProducts()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

const cartStore = useCartStore()

// 添加到购物车
const addToCart = (product) => {
  cartStore.addToCart({
    id: product.id,
    name: product.name,
    price: product.price,
    image: product.image
  })
  alert('商品已加入购物车')
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.product-list {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.category-filter {
  flex: 1;
  min-width: 200px;
}

.search-container {
  flex: 2;
  min-width: 300px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.product-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.product-image {
  height: 200px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.product-description {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.product-meta {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.product-price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 1rem;
}

.product-actions {
  display: flex;
  gap: 0.75rem;
}

.product-actions .el-button {
  flex: 1;
}

.pagination-container {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .category-filter,
  .search-container {
    flex: none;
    width: 100%;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1rem;
  }
  
  .product-info {
    padding: 1rem;
  }
  
  .product-actions {
    flex-direction: column;
  }
}
</style>