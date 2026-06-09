import request from './request.js'
import type { Product, ApiResponse, RegisterRequest, LoginRequest, LoginResponse } from '../types'

// ==================== 商品相关API ====================
export const productAPI = {
  // 获取所有商品
  getAllProducts: (): Promise<ApiResponse<Product[]>> => {
    return request.get('/products')
  },

  // 根据ID获取商品
  getProductById: (id: string | number): Promise<ApiResponse<Product>> => {
    return request.get(`/products/${id}`)
  },

  // 搜索商品
  searchProducts: (keyword: string): Promise<ApiResponse<Product[]>> => {
    return request.get('/products/search', { params: { keyword } })
  },

  // 分页查询商品
  getProductsByPage: (params: {
    page: number;
    pageSize: number;
    category?: string;
    keyword?: string;
  }): Promise<ApiResponse<{
    items: Product[];
    total: number;
    page: number;
    pageSize: number;
  }>> => {
    return request.get('/products/page', { params })
  },

  // 获取所有分类
  getCategories: (): Promise<ApiResponse<string[]>> => {
    return request.get('/categories')
  }
}

// ==================== 用户相关API ====================
export const userAPI = {
  // 用户注册
  register: (data: RegisterRequest): Promise<ApiResponse<string>> => {
    return request.post('/user/register', data)
  },

  // 用户登录
  login: (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    return request.post('/user/login', data)
  }
}

// ==================== 购物车相关API ====================
export const cartAPI = {
  // 获取购物车列表
  getCartList: (): Promise<ApiResponse<any[]>> => {
    return request.get('/cart')
  },

  // 添加商品到购物车
  addToCart: (productId: number, quantity: number): Promise<ApiResponse<any>> => {
    return request.post('/cart', { productId, quantity })
  },

  // 更新购物车商品数量
  updateQuantity: (cartId: number, quantity: number): Promise<ApiResponse<any>> => {
    return request.put(`/cart/${cartId}`, { quantity })
  },

  // 更新勾选状态
  updateSelected: (cartId: number, selected: number): Promise<ApiResponse<any>> => {
    return request.put(`/cart/${cartId}/selected`, { selected })
  },

  // 批量更新勾选状态（全选/全不选）
  batchUpdateSelected: (selected: number): Promise<ApiResponse<any>> => {
    return request.put('/cart/selected/batch', { selected })
  },

  // 删除单个购物车商品
  deleteCartItem: (cartId: number): Promise<ApiResponse<any>> => {
    return request.delete(`/cart/${cartId}`)
  },

  // 批量删除已勾选商品
  deleteSelected: (): Promise<ApiResponse<any>> => {
    return request.delete('/cart/selected')
  },

  // 获取购物车商品总数
  getCartCount: (): Promise<ApiResponse<number>> => {
    return request.get('/cart/count')
  },

  // 获取已勾选商品数
  getSelectedCount: (): Promise<ApiResponse<number>> => {
    return request.get('/cart/selected/count')
  }
}

// ==================== 订单相关API ====================
export const orderAPI = {
  // 创建订单
  createOrder: (data: {
    consignee: string;
    telephone: string;
    city: string;
    address: string;
  }): Promise<ApiResponse<any>> => {
    return request.post('/orders/create', data)
  },

  // 获取订单详情
  getOrderDetail: (orderId: number): Promise<ApiResponse<any>> => {
    return request.get(`/orders/${orderId}`)
  },

  // 下载订单发票
  downloadInvoice: (orderId: number): Promise<any> => {
    return request.get(`/invoice/download/${orderId}`, { responseType: 'blob' })
  }
}

export default request
