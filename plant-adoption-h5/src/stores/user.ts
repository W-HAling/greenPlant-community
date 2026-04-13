import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface User {
  id: number
  phone: string
  nickname: string
  avatar: string
  departmentId: number
  departmentName: string
  role: string
  token: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<User | null>(null)
  const token = ref<string>('')
  
  const isLoggedIn = computed(() => !!token.value)
  
  const setUserInfo = (user: User) => {
    userInfo.value = user
    token.value = user.token
    uni.setStorageSync('userInfo', JSON.stringify(user))
    uni.setStorageSync('token', user.token)
  }
  
  const logout = () => {
    userInfo.value = null
    token.value = ''
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('token')
    uni.reLaunch({
      url: '/pages/login/login'
    })
  }
  
  const checkLogin = () => {
    const savedToken = uni.getStorageSync('token')
    const savedUserInfo = uni.getStorageSync('userInfo')
    
    if (savedToken && savedUserInfo) {
      token.value = savedToken
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (e) {
        logout()
      }
    }
  }
  
  const requireLogin = () => {
    if (!isLoggedIn.value) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/login'
        })
      }, 1500)
      return false
    }
    return true
  }
  
  return {
    userInfo,
    token,
    isLoggedIn,
    setUserInfo,
    logout,
    checkLogin,
    requireLogin
  }
})
