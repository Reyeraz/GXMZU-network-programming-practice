/**
 * 图片URL处理工具
 * 将相对路径转换为完整的图片URL
 */

const BASE_URL = import.meta.env.VITE_STATIC_BASE_URL || 'http://localhost:8080'

/**
 * 获取完整的图片URL
 * 如果已经是完整的http/https链接则直接返回
 * 否则拼接BASE_URL
 */
export const getFullImageUrl = (url: string): string => {
  if (!url) return ''

  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  // 确保路径以/开头
  const path = url.startsWith('/') ? url : `/${url}`
  return `${BASE_URL}${path}`
}

/**
 * 获取商品图片URL
 * @param image 商品图片路径
 * @param placeholder 占位图URL
 */
export const getProductImageUrl = (image: string, placeholder: string = '/placeholder.png'): string => {
  if (!image) return placeholder
  return getFullImageUrl(image)
}
