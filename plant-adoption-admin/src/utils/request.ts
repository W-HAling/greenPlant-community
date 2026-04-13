import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const BASE_URL = '/api'

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

const instance: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    
    if (res.code === 200) {
      return res.data
    }
    
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error(res.message))
    }
    
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return instance.request<any, T>(config)
}

export const get = <T = any>(url: string, params?: any): Promise<T> => {
  return request<T>({ method: 'GET', url, params })
}

export const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ method: 'POST', url, data, ...config })
}

export const put = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ method: 'PUT', url, data, ...config })
}

export const del = <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>({ method: 'DELETE', url, ...config })
}

export default instance
