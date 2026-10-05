import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * 全局 axios 实例：统一基础路径、超时时间与令牌注入，并把响应错误转换为界面提示。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
/**
 * 请求拦截器：为每个请求附加 Authorization 请求头（令牌取自 localStorage）
 */
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
/**
 * 响应拦截器：业务码非 200 时提示并中断后续处理；
 * HTTP 错误按状态码区分处理，401 时清除本地登录信息并跳转登录页。
 */
request.interceptors.response.use(
  response => {
    const res = response.data
    // 兼容 PageResult 和 Result 格式
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 500:
          ElMessage.error(data?.message || '服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
