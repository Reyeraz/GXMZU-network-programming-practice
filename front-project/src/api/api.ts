import request from './request.js'
import type { Product, ApiResponse, RegisterRequest, LoginRequest, LoginResponse } from '../types'

// 商品相关API
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

// 用户相关API
export const userAPI = {
  register: (data: RegisterRequest): Promise<ApiResponse<string>> => {
    return request.post('/user/register', data)
  },

  login: (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    return request.post('/user/login', data)
  }
}

export default request
