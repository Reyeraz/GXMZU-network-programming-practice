<template>
  <div class="app">
    <!-- 导航栏 -->
    <el-header height="60px" class="navbar">
      <div class="nav-container">
        <h1 class="logo"><a href="/">数码购物平台</a></h1>
        <el-menu :default-active="activeIndex" mode="horizontal" background-color="#1a1a1a" text-color="#fff" active-text-color="#4a90e2">
          <el-menu-item index="1">
            <router-link to="/">首页</router-link>
          </el-menu-item>
          <el-menu-item index="2">
            <router-link to="/coupons">优惠券中心</router-link>
          </el-menu-item>
          <el-menu-item index="3">
            <router-link to="/cart" class="cart-link">
              购物车
              <el-badge v-if="cartCount > 0" :value="cartCount" type="danger" class="cart-count" />
            </router-link>
          </el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <!-- 路由出口 -->
    <el-main class="main-content">
      <router-view></router-view>
    </el-main>

    <!-- 优惠券弹窗 -->
    <CouponPopup 
      :visible="couponPopupVisible" 
      :coupon="coupon" 
      @close="closeCouponPopup" 
      @claim="claimCoupon" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import CouponPopup from './components/CouponPopup.vue'
import { useCartStore } from './stores/cart'

const route = useRoute()
const cartStore = useCartStore()

// 购物车商品数量（从 Pinia store 获取）
const cartCount = computed(() => cartStore.totalQuantity)

// 优惠券弹窗状态
const couponPopupVisible = ref(false)

// 优惠券数据
const coupon = ref({
  value: 50,
  minSpend: 200,
  expiryDate: '2026-12-31'
})

// 计算当前激活的导航项
const activeIndex = computed(() => {
  const path = route.path
  if (path === '/') return '1'
  if (path === '/coupons') return '2'
  if (path === '/cart') return '3'
  return '1'
})

// 关闭优惠券弹窗
const closeCouponPopup = () => {
  couponPopupVisible.value = false
  // 记录用户已关闭弹窗，避免重复显示
  localStorage.setItem('couponPopupClosed', 'true')
}

// 领取优惠券
const claimCoupon = () => {
  // 模拟领取优惠券的逻辑
  console.log('领取优惠券:', coupon.value)
  // 可以将优惠券添加到用户的优惠券列表中
  const userCoupons = JSON.parse(localStorage.getItem('userCoupons') || '[]')
  userCoupons.push({
    ...coupon.value,
    claimedAt: new Date().toISOString()
  })
  localStorage.setItem('userCoupons', JSON.stringify(userCoupons))
  // 显示领取成功提示
  alert('优惠券领取成功！')
  // 记录用户已领取，避免重复显示
  localStorage.setItem('couponClaimed', 'true')
}

// 监听路由变化，更新购物车数量
onMounted(() => {
  cartStore.loadCart()

  // 检查是否需要显示优惠券弹窗
  const hasClosed = localStorage.getItem('couponPopupClosed') === 'true'
  const hasClaimed = localStorage.getItem('couponClaimed') === 'true'
  
  if (!hasClosed && !hasClaimed) {
    // 延迟显示弹窗，让页面先加载完成
    setTimeout(() => {
      couponPopupVisible.value = true
    }, 1000)
  }
})
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 16px;
  line-height: 1.5;
  color: #333;
  background-color: #f5f5f5;
}

/* 应用容器 */
.app {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  min-height: 100vh;
}

/* 导航栏样式 */
.navbar {
  background-color: #1a1a1a;
  color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo a {
  color: #fff;
  text-decoration: none;
  font-size: 1.5rem;
  font-weight: bold;
}

/* 购物车链接样式 */
.cart-link {
  position: relative;
  color: #fff;
  text-decoration: none;
}

.cart-count {
  position: absolute;
  top: -8px;
  right: -12px;
}

/* 主内容区域 */
.main-content {
  padding: 2rem 1rem;
}

/* 容器样式 */
.container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 标题样式 */
h1, h2, h3, h4, h5, h6 {
  margin-bottom: 1rem;
  color: #2c3e50;
}
</style>