import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { clearAuth, getToken } from './auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const result = response.data

    if (result && result.code === 1) {
      return result.data
    }

    ElMessage.error(result?.msg || '请求处理失败，请稍后重试')
    return Promise.reject(new Error(result?.msg || 'Request failed'))
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth()
      ElMessage.error('登录状态已失效，请重新登录')
      router.replace('/login')
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常，请稍后重试')
    }

    return Promise.reject(error)
  }
)

export default service
