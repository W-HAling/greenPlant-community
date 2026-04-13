import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo } from '@/api/auth'
import type { UserInfo } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  
  const isLoggedIn = computed(() => !!token.value)
  
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  const loginAction = async (phone: string, code: string) => {
    const res = await login(phone, code)
    setToken(res.token || '')
    setUserInfo(res)
    return res
  }
  
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const info = await getUserInfo()
      setUserInfo(info)
    } catch {
      logout()
    }
  }
  
  const initUser = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch {
        logout()
      }
    }
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    loginAction,
    logout,
    fetchUserInfo,
    initUser
  }
})
