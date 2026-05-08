import request from './request.js'
import { products, getProductById, searchProducts } from '../data/products.js'
import type { Product, ApiResponse } from '../types'

// 商品相关API
export const productAPI = {
  // 获取所有商品
  getAllProducts: (): Promise<ApiResponse<Product[]>> => {
    // 模拟API请求
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, data: products })
      }, 300)
    })
  },

  // 根据ID获取商品
  getProductById: (id: string | number): Promise<ApiResponse<Product>> => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const product = getProductById(id)
        if (product) {
          resolve({ success: true, data: product })
        } else {
          reject({ success: false, message: '商品不存在' })
        }
      }, 200)
    })
  },

  // 搜索商品
  searchProducts: (keyword: string): Promise<ApiResponse<Product[]>> => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const results = searchProducts(keyword)
        resolve({ success: true, data: results })
      }, 300)
    })
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
    return new Promise((resolve) => {
      setTimeout(() => {
        let filteredProducts = [...products]
        
        // 分类筛选
        if (params.category) {
          filteredProducts = filteredProducts.filter(product => 
            product.category === params.category
          )
        }
        
        // 关键词搜索
        if (params.keyword) {
          const lowerKeyword = params.keyword.toLowerCase()
          filteredProducts = filteredProducts.filter(product => 
            product.name.toLowerCase().includes(lowerKeyword) ||
            product.description.toLowerCase().includes(lowerKeyword) ||
            product.brand.toLowerCase().includes(lowerKeyword)
          )
        }
        
        // 计算总数
        const total = filteredProducts.length
        
        // 分页
        const start = (params.page - 1) * params.pageSize
        const end = start + params.pageSize
        const paginatedProducts = filteredProducts.slice(start, end)
        
        resolve({
          success: true,
          data: {
            items: paginatedProducts,
            total,
            page: params.page,
            pageSize: params.pageSize
          }
        })
      }, 300)
    })
  },

  // 获取所有分类
  getCategories: (): Promise<ApiResponse<string[]>> => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const categories = [...new Set(products.map(product => product.category))]
        resolve({ success: true, data: categories })
      }, 200)
    })
  }
}

export default request