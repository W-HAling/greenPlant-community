const BASE_URL = '/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
}

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export const request = <T = any>(options: RequestOptions): Promise<T> => {
  const { url, method = 'GET', data, header = {}, showLoading = true, showError = true } = options
  
  const token = uni.getStorageSync('token')
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }
  header['Content-Type'] = 'application/json'
  
  if (showLoading) {
    uni.showLoading({
      title: '加载中...',
      mask: true
    })
  }
  
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      success: (res: any) => {
        if (showLoading) {
          uni.hideLoading()
        }
        
        const response = res.data as ApiResponse<T>
        
        if (response.code === 200) {
          resolve(response.data)
        } else if (response.code === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({
            title: '请重新登录',
            icon: 'none'
          })
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
          reject(response)
        } else {
          if (showError) {
            uni.showToast({
              title: response.message || '请求失败',
              icon: 'none'
            })
          }
          reject(response)
        }
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        if (showError) {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
        }
        reject(err)
      }
    })
  })
}

export const get = <T = any>(url: string, params?: any, options?: Partial<RequestOptions>) => {
  const filteredParams = params
    ? Object.fromEntries(
        Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
      )
    : {}
  const queryStr = Object.keys(filteredParams).length > 0
    ? '?' + Object.entries(filteredParams).map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')
    : ''
  return request<T>({
    url: url + queryStr,
    method: 'GET',
    ...options
  })
}

export const post = <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) => {
  return request<T>({
    url,
    method: 'POST',
    data,
    ...options
  })
}

export const put = <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) => {
  return request<T>({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

export const del = <T = any>(url: string, options?: Partial<RequestOptions>) => {
  return request<T>({
    url,
    method: 'DELETE',
    ...options
  })
}

export const uploadFile = (filePath: string): Promise<string> => {
  const token = uni.getStorageSync('token')
  
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/upload/image',
      filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({
            title: data.message || '上传失败',
            icon: 'none'
          })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}
