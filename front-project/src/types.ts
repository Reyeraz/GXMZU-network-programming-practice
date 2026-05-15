// 促销信息类型
export interface Promotion {
  type: 'flash_sale' | 'new_arrival' | 'hot_sale'
  tag: string
  discountRate: number
  endTime: string
}

// 产品类型定义
export interface Product {
  id: number
  name: string
  description: string
  price: number
  image: string
  category: string
  brand: string
  stock: number
  promotion?: Promotion
}

// 用户类型
export interface UserInfo {
  userId: number
  username: string
  phone?: string
  email?: string
  address?: string
}

// 注册请求
export interface RegisterRequest {
  username: string
  password: string
  phone?: string
  email?: string
  address?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  token: string
}

// API 响应类型
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}
