import { get, post } from '@/utils/request'
import type { UserInfo } from './types'

export const sendCode = (phone: string) => {
  return post(`/auth/code?phone=${phone}`)
}

export const login = (phone: string, code: string) => {
  return post<UserInfo>('/auth/login', { phone, code })
}

export const getUserInfo = () => {
  return get<UserInfo>('/user/info')
}
