# 电商前端 API 接口文档

## 1. 接口概览

| 接口名称 | 方法 | 功能描述 |
|---------|------|----------|
| 获取所有商品 | GET | 获取所有商品列表 |
| 根据ID获取商品 | GET | 根据商品ID获取商品详情 |
| 搜索商品 | GET | 根据关键词搜索商品 |
| 分页查询商品 | GET | 分页查询商品，支持分类筛选和关键词搜索 |
| 获取所有分类 | GET | 获取所有商品分类 |

## 2. 接口详情

### 2.1 获取所有商品

**接口路径**: `/api/products`
**请求方法**: GET
**响应数据**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "description": "A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络",
      "price": 7999,
      "image": "https://picsum.photos/id/1/300/300",
      "category": "手机",
      "brand": "Apple",
      "stock": 50
    },
    // 更多商品...
  ]
}
```

### 2.2 根据ID获取商品

**接口路径**: `/api/products/{id}`
**请求方法**: GET
**响应数据**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络",
    "price": 7999,
    "image": "https://picsum.photos/id/1/300/300",
    "category": "手机",
    "brand": "Apple",
    "stock": 50
  }
}
```

### 2.3 搜索商品

**接口路径**: `/api/products/search`
**请求方法**: GET
**请求参数**:
| 参数名 | 类型 | 描述 | 必选 |
|-------|------|------|------|
| keyword | string | 搜索关键词 | 是 |

**响应数据**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "description": "A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络",
      "price": 7999,
      "image": "https://picsum.photos/id/1/300/300",
      "category": "手机",
      "brand": "Apple",
      "stock": 50
    },
    // 更多匹配的商品...
  ]
}
```

### 2.4 分页查询商品

**接口路径**: `/api/products/page`
**请求方法**: GET
**请求参数**:
| 参数名 | 类型 | 描述 | 必选 |
|-------|------|------|------|
| page | number | 当前页码 | 是 |
| pageSize | number | 每页条数 | 是 |
| category | string | 商品分类（可选） | 否 |
| keyword | string | 搜索关键词（可选） | 否 |

**响应数据**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "description": "A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络",
        "price": 7999,
        "image": "https://picsum.photos/id/1/300/300",
        "category": "手机",
        "brand": "Apple",
        "stock": 50
      },
      // 更多商品...
    ],
    "total": 8,
    "page": 1,
    "pageSize": 4
  }
}
```

### 2.5 获取所有分类

**接口路径**: `/api/categories`
**请求方法**: GET
**响应数据**:
```json
{
  "success": true,
  "data": ["手机", "笔记本电脑", "平板电脑", "耳机"]
}
```

## 3. 实现说明

### 3.1 请求工具封装

使用 `axios` 封装了独立的请求工具 `request.js`，包含：
- 基础配置（baseURL、超时时间等）
- 请求拦截器（可添加认证信息）
- 响应拦截器（统一错误处理）

### 3.2 Mock 数据实现

由于是前端项目，使用了 Mock 数据模拟 API 响应：
- 商品数据存储在 `products.js` 中
- API 接口通过 Promise 模拟异步请求
- 支持分页、分类筛选和关键词搜索功能

### 3.3 前端实现

在 `ProductList.vue` 中实现了：
- 分类下拉选择器
- 关键词搜索框
- 商品列表展示
- 分页组件
- 加载状态和错误提示

## 4. 使用示例

### 4.1 分页查询示例

```javascript
// 导入 API
import { productAPI } from './api/api.js'

// 分页查询商品
const fetchProducts = async () => {
  try {
    const response = await productAPI.getProductsByPage({
      page: 1,
      pageSize: 4,
      category: '手机', // 可选
      keyword: 'iPhone' // 可选
    })
    if (response.success) {
      console.log('商品列表:', response.data.items)
      console.log('总商品数:', response.data.total)
    }
  } catch (error) {
    console.error('获取商品失败:', error)
  }
}
```

### 4.2 获取分类示例

```javascript
// 导入 API
import { productAPI } from './api/api.js'

// 获取所有分类
const fetchCategories = async () => {
  try {
    const response = await productAPI.getCategories()
    if (response.success) {
      console.log('分类列表:', response.data)
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}
```