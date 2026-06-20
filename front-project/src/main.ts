import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import App from './App.vue'
import ProductList from './views/ProductList.vue'
import ProductDetail from './views/ProductDetail.vue'
import Cart from './views/Cart.vue'
import Checkout from './views/Checkout.vue'
import FirstDemo from './views/FirstDemo.vue'
import UserCouponxue from './views/UserCouponxue.vue'
import NotFound from './views/NotFound.vue'
import OrderSuccess from './views/OrderSuccess.vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'

// 导入Element-Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 需要登录才能访问的路由
const AUTH_ROUTES = ['/cart', '/checkout', '/order/success']

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: ProductList },
    { path: '/product/:id', component: ProductDetail, props: true },
    { path: '/cart', component: Cart },
    { path: '/checkout', component: Checkout },
    { path: '/order/success', component: OrderSuccess },
    { path: '/first-demo', name: 'FirstDemo', component: FirstDemo },
    { path: '/coupons', component: UserCouponxue },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound }
  ]
})

// 路由守卫 — 需要登录的页面自动跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (AUTH_ROUTES.includes(to.path) && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

// 创建Vue应用
const app = createApp(App)

// 创建 Pinia 实例
const pinia = createPinia()

// 使用路由
app.use(router)

// 使用 Pinia
app.use(pinia)

// 使用Element-Plus
app.use(ElementPlus)

// 挂载应用
app.mount('#app')