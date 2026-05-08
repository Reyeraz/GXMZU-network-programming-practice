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
}

// API 响应类型
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}
