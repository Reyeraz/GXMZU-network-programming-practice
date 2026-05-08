<template>
  <div class="coupon-center">
    <h1>个人优惠券中心</h1>
    
    <!-- 优惠券筛选标签 -->
    <el-tabs v-model="activeTab" class="coupon-tabs">
      <el-tab-pane 
        v-for="tab in tabs" 
        :key="tab.value"
        :label="tab.label"
        :name="tab.value"
      >
      </el-tab-pane>
    </el-tabs>
    
    <!-- 优惠券列表 -->
    <div class="coupon-list">
      <el-card 
        v-for="coupon in filteredCoupons" 
        :key="coupon.id"
        class="coupon-card"
        :class="{ 'used': coupon.status === 'used', 'expired': coupon.status === 'expired' }"
        :body-style="{ padding: '0' }"
      >
        <el-row :gutter="0" type="flex" class="coupon-content">
          <!-- 优惠券面额 -->
          <el-col :span="8" class="coupon-value">
            <span class="value-label">¥</span>
            <span class="value-amount">{{ coupon.amount }}</span>
            <span class="value-condition">满{{ coupon.minSpend }}可用</span>
          </el-col>
          
          <!-- 优惠券信息 -->
          <el-col :span="16" class="coupon-info">
            <h3 class="coupon-title">{{ coupon.title }}</h3>
            <p class="coupon-description">{{ coupon.description }}</p>
            <div class="coupon-meta">
              <span class="coupon-validity">有效期至：{{ coupon.endDate }}</span>
              <el-tag size="small">{{ coupon.category }}</el-tag>
            </div>
            
            <!-- 优惠券状态/操作 -->
            <div class="coupon-actions">
              <el-button 
                v-if="coupon.status === 'available'"
                type="primary"
                @click="claimCoupon(coupon)"
              >
                立即领取
              </el-button>
              <el-button 
                v-else-if="coupon.status === 'claimed'"
                type="default"
                @click="useCoupon(coupon)"
              >
                立即使用
              </el-button>
              <el-tag v-else-if="coupon.status === 'used'" type="info" effect="plain">
                已使用
              </el-tag>
              <el-tag v-else-if="coupon.status === 'expired'" type="warning" effect="plain">
                已过期
              </el-tag>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    
    <!-- 无优惠券提示 -->
    <el-empty v-if="filteredCoupons.length === 0" description="暂无可用优惠券" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 路由实例
const router = useRouter()

// 激活的标签页
const activeTab = ref('all')

// 优惠券标签
const tabs = [
  { label: '全部', value: 'all' },
  { label: '未使用', value: 'available' },
  { label: '已使用', value: 'used' },
  { label: '已过期', value: 'expired' }
]

// 模拟优惠券数据
const coupons = ref([
  {
    id: 1,
    title: '新人专享优惠券',
    amount: 20,
    minSpend: 100,
    description: '新用户注册即可领取，全场通用',
    category: '全场通用',
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    status: 'available',
    claimed: false
  },
  {
    id: 2,
    title: '数码产品专享券',
    amount: 50,
    minSpend: 300,
    description: '购买数码产品满300元可用',
    category: '数码产品',
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    status: 'claimed',
    claimed: true
  },
  {
    id: 3,
    title: '限时秒杀优惠券',
    amount: 30,
    minSpend: 200,
    description: '限时秒杀活动专享优惠券',
    category: '限时活动',
    startDate: '2024-01-01',
    endDate: '2024-06-30',
    status: 'expired',
    claimed: true
  },
  {
    id: 4,
    title: '满减优惠券',
    amount: 100,
    minSpend: 500,
    description: '全场满500元减100元',
    category: '全场通用',
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    status: 'available',
    claimed: false
  },
  {
    id: 5,
    title: '生日专属优惠券',
    amount: 88,
    minSpend: 200,
    description: '生日当月专享优惠券',
    category: '个人专属',
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    status: 'used',
    claimed: true
  }
])

// 根据标签筛选优惠券
const filteredCoupons = computed(() => {
  if (activeTab.value === 'all') {
    return coupons.value
  }
  return coupons.value.filter(coupon => coupon.status === activeTab.value)
})

// 领取优惠券
const claimCoupon = (coupon) => {
  // 更新优惠券状态
  coupon.status = 'claimed'
  coupon.claimed = true
  
  // 保存到localStorage（模拟）
  saveCouponsToLocal()
  
  // 提示用户
  alert('优惠券领取成功！')
}

// 使用优惠券
const useCoupon = (coupon) => {
  // 更新优惠券状态
  coupon.status = 'used'
  
  // 保存到localStorage（模拟）
  saveCouponsToLocal()
  
  // 提示用户
  alert('优惠券使用成功！')
  
  // 跳转到商品列表页
  router.push('/')
}

// 保存优惠券到localStorage
const saveCouponsToLocal = () => {
  localStorage.setItem('coupons', JSON.stringify(coupons.value))
}

// 从localStorage获取优惠券
const loadCouponsFromLocal = () => {
  const savedCoupons = localStorage.getItem('coupons')
  if (savedCoupons) {
    coupons.value = JSON.parse(savedCoupons)
  }
}

// 初始化时加载优惠券数据
loadCouponsFromLocal()
</script>

<style scoped>
.coupon-center {
  max-width: 1200px;
  margin: 0 auto;
}

/* 标签页样式 */
.coupon-tabs {
  margin-bottom: 2rem;
}

/* 优惠券列表 */
.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

/* 优惠券卡片 */
.coupon-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}

.coupon-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

/* 已使用和已过期样式 */
.coupon-card.used,
.coupon-card.expired {
  opacity: 0.7;
}

.coupon-card.used::before,
.coupon-card.expired::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(0, 0, 0, 0.05) 10px, rgba(0, 0, 0, 0.05) 20px);
  pointer-events: none;
}

/* 优惠券面额区域 */
.coupon-value {
  background: linear-gradient(135deg, #4a90e2 0%, #357abd 100%);
  color: white;
  padding: 2rem 1.5rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.value-amount {
  font-size: 2.5rem;
  font-weight: bold;
  line-height: 1;
}

.value-label {
  font-size: 1.25rem;
  font-weight: bold;
  margin-right: 0.25rem;
}

.value-condition {
  font-size: 0.85rem;
  margin-top: 0.5rem;
  opacity: 0.9;
}

/* 优惠券信息区域 */
.coupon-info {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.coupon-title {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.coupon-description {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.coupon-meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  font-size: 0.85rem;
  color: #999;
}

.coupon-validity {
  display: block;
}

/* 优惠券操作 */
.coupon-actions {
  margin-top: auto;
}

/* 无优惠券提示 */
.no-coupons {
  padding: 3rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .coupon-list {
    grid-template-columns: 1fr;
  }
  
  .coupon-content {
    flex-direction: column;
  }
  
  .coupon-value {
    width: 100%;
    padding: 1.5rem;
  }
  
  .coupon-info {
    width: 100%;
  }
}
</style>