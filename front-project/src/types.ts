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

// API 响应类型
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}
