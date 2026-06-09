import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 白名单路径（不需要token）
const WHITE_LIST = [
  '/user/register',
  '/user/login',
  '/products/list',
  '/products/',
  '/categories/list',
  '/categories'
]

// 判断是否需要认证
const needAuth = (url) => {
  return !WHITE_LIST.some(path => url.includes(path))
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    // 只有需要认证的接口才添加token
    if (token && needAuth(config.url)) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // Token过期或未登录
          ElMessage.error('登录已过期，请重新登录')
          // 清除本地存储的用户信息
          localStorage.removeItem('token')
          localStorage.removeItem('userId')
          localStorage.removeItem('username')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          // 无权限访问
          ElMessage.error('没有权限执行此操作')
          break
        case 404:
          // 资源不存在
          ElMessage.error(data?.message || '请求的资源不存在')
          break
        case 500:
          // 服务器错误
          ElMessage.error('服务器错误，请稍后重试')
          break
        default:
          // 其他错误
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      // 网络错误
      ElMessage.error('网络请求失败，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
