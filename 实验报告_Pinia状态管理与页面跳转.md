/# 网络编程实践 - 实验报告

## 一、实验名称

Vue Router 页面跳转与 Pinia 全局状态管理实践

## 二、实验目的

1. 掌握 Vue Router 的 404 页面捕获与自动跳转机制
2. 掌握下单成功后的页面跳转及订单号传递
3. 掌握 Pinia 状态管理库的安装与配置
4. 掌握使用 Pinia 定义全局购物车状态，实现添加、删除、清空、总价计算等功能
5. 理解 Vue3 组合式 API（Composition API）与 Pinia Store 的协作模式

## 三、实验环境

| 项目   | 环境/版本        |
| ------ | ---------------- |
| 操作系统 | Windows 11       |
| 前端框架 | Vue 3.5.13       |
| 构建工具 | Vite 6.0.5       |
| UI 组件库 | Element Plus 2.13.6 |
| 状态管理 | Pinia（新增）     |
| 路由    | Vue Router 4.4.5 |
| 包管理器 | npm              |
| 后端    | Spring Boot 3.4.4（无需修改） |

## 四、实验内容

### 任务一：404 页面跳转和下单成功跳转

#### （1）实现思路

- **404 页面**：路由配置中已有 `/:pathMatch(.*)*` 通配规则匹配 NotFound 组件。在此基础上增加 5 秒倒计时自动跳转回首页，并增加"返回上一页"按钮。
- **下单成功页面**：从路由 query 获取 `orderNo`，若无订单号则直接跳转首页；若有则显示订单号，并启动 5 秒倒计时自动返回首页。
- **路由控制**：利用 `useRouter` 的 `push()` 实现编程式导航，`onUnmounted` 时清理定时器防止内存泄漏。

#### （2）核心代码

**NotFound.vue** — 关键变更：

```javascript
// 5秒倒计时自动跳转
const countdown = ref(5)
let timer = null

onMounted(() => {
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
```

**OrderSuccess.vue** — 关键变更：

```javascript
// 若无订单号则跳转首页，否则5秒后自动返回
onMounted(() => {
  orderNo.value = route.query.orderNo || ''
  if (!orderNo.value) {
    router.push('/')
    return
  }
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      router.push('/')
    }
  }, 1000)
})
```

### 任务二：Pinia 全局购物车状态管理

#### （1）实现思路

1. **安装 Pinia**：`npm install pinia`
2. **创建 Store**：在 `src/stores/cart.ts` 中使用 Composition API 风格定义 `useCartStore`
   - **state**：`items`（商品数组），持久化至 localStorage
   - **getters**：`totalQuantity`（总数量）、`totalPrice`（总价）
   - **actions**：`addToCart`、`removeFromCart`、`updateQuantity`、`clearCart`、`loadCart`
3. **注册 Pinia**：在 `main.ts` 中 `app.use(createPinia())`
4. **改造所有消费组件**：App.vue、ProductList.vue、ProductDetail.vue、Cart.vue、Checkout.vue 统一使用 store 替代零散的 localStorage 操作
5. **Cart.vue 新增"清空购物车"按钮**：调用 `cartStore.clearCart()`

#### （2）核心代码

**stores/cart.ts** — Pinia Store 定义：

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalQuantity = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  function addToCart(product, quantity = 1) {
    const existing = items.value.find(item => item.id === product.id)
    if (existing) {
      existing.quantity += quantity
    } else {
      items.value.push({ ...product, quantity })
    }
    saveCart()  // 持久化到 localStorage
  }

  function removeFromCart(id: number) { /* ... */ }
  function updateQuantity(id: number, quantity: number) { /* ... */ }
  function clearCart() {
    items.value = []
    saveCart()
  }

  return { items, totalQuantity, totalPrice, loadCart, addToCart, removeFromCart, updateQuantity, clearCart }
})
```

**Cart.vue** — 使用 store（新增清空购物车按钮）：

```html
<script setup>
import { useCartStore } from '../stores/cart'
const cartStore = useCartStore()
const cartItems = computed(() => cartStore.items)
const totalQuantity = computed(() => cartStore.totalQuantity)
const totalPrice = computed(() => cartStore.totalPrice)
const clearCart = () => cartStore.clearCart()
</script>

<!-- 模板新增按钮 -->
<el-button type="danger" size="large" @click="clearCart">清空购物车</el-button>
```

## 五、文件变更清单

| 操作   | 文件路径                                        | 说明                             |
| ------ | ----------------------------------------------- | -------------------------------- |
| 修改   | `src/views/NotFound.vue`                        | 添加 5 秒倒计时自动跳转 + 返回上一页按钮 |
| 修改   | `src/views/OrderSuccess.vue`                    | 添加 5 秒倒计时自动跳转              |
| 新增   | `src/stores/cart.ts`                            | Pinia 购物车 Store                |
| 修改   | `src/main.ts`                                   | 注册 Pinia 插件                   |
| 修改   | `src/App.vue`                                   | 改用 store 获取购物车数量          |
| 修改   | `src/views/ProductList.vue`                     | 改用 store.addToCart()            |
| 修改   | `src/views/ProductDetail.vue`                   | 改用 store.addToCart()            |
| 修改   | `src/views/Cart.vue`                            | 改用 store + 添加清空购物车按钮    |
| 修改   | `src/views/Checkout.vue`                        | 改用 store.clearCart()            |
| 新增   | `package.json`（依赖）                          | 新增 pinia 依赖                   |

## 六、功能测试

### 测试用例

| 编号 | 测试场景                       | 预期结果                                       | 实际结果 |
| ---- | ------------------------------ | ---------------------------------------------- | -------- |
| 1    | 访问不存在的路由 `/xyz`         | 显示 404 页面，5 秒后自动跳转首页               | 通过     |
| 2    | 访问 404 页面后点击"返回上一页" | 返回浏览器历史上一页                            | 通过     |
| 3    | 提交订单后跳转 `/order/success` | 显示订单号，5 秒后自动返回首页                  | 通过     |
| 4    | 直接访问 `/order/success` 无参  | 自动跳转到首页                                  | 通过     |
| 5    | 商品列表页添加商品到购物车      | 购物车数量 +1，商品存储在 localStorage + store  | 通过     |
| 6    | 商品详情页添加多件商品到购物车  | 购物车中对应商品数量正确增加                    | 通过     |
| 7    | 购物车页面删除商品              | 商品移除，总价正确更新                          | 通过     |
| 8    | 购物车页面修改商品数量          | 小计和总价正确更新                              | 通过     |
| 9    | 购物车页面点击"清空购物车"      | 所有商品移除，显示空购物车提示                  | 通过     |
| 10   | 下单成功后购物车自动清空        | 下单成功页面显示订单号，购物车为空              | 通过     |

### 运行截图说明

- 访问 `http://localhost:5174/product/1` — 商品详情页，有促销标签和倒计时
- 点击"加入购物车" → 顶部购物车徽标数字变化
- 访问 `http://localhost:5174/cart` — 购物车页面，可见"清空购物车"按钮
- 点击"去结算" → 填写信息 → 提交订单 → 跳转下单成功页，5 秒后自动返回首页
- 访问 `http://localhost:5174/nonexistent` → 404 页面，5 秒后自动返回首页

## 七、问题与解决

| 问题                           | 原因                             | 解决方案                                |
| ------------------------------ | -------------------------------- | --------------------------------------- |
| Cart.vue 中 `@change` 参数传递 | `@change="updateCart"` 未传 item | 改为 `@change="(val) => updateCart(item, val)"` |
| 多组件各自维护 localStorage   | 增删改逻辑分散，数据不一致风险   | 统一收敛到 Pinia Store 中管理           |
| App.vue 中 cartCount 需响应式  | `ref` 需手动从 localStorage 刷新 | 改为 `computed(() => cartStore.totalQuantity)` |

## 八、实验总结

通过本次实验，完成了以下工作：

1. **404 与下单成功跳转**：利用 Vue Router 编程式导航 + `setInterval` 倒计时实现页面自动跳转，并在组件卸载时清理定时器防止内存泄漏。
2. **Pinia 状态管理**：成功将购物车数据从零散的 localStorage 操作重构为 Pinia 全局 Store，所有页面的购物车操作（添加、删除、修改数量、清空、总价计算）统一收敛到 `useCartStore` 中管理，提高了代码可维护性和数据一致性。
3. 整合同步了 **5 个组件**（App、ProductList、ProductDetail、Cart、Checkout）的数据操作逻辑，消除了重复代码。
