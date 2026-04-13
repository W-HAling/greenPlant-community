import { get, put } from '@/utils/request'
import type { UserInfo, PageResult, Department } from './types'

export const getUserList = (params: {
  current?: number
  size?: number
  nickname?: string
  phone?: string
  departmentId?: number
  role?: string
}) => {
  return get<PageResult<UserInfo>>('/user/list', params)
}

export const getUserDetail = (id: number) => {
  return get<UserInfo>(`/user/${id}`)
}

export const updateUserStatus = (id: number, status: number) => {
  return put(`/user/${id}/status`, { status })
}

export const getDepartmentList = () => {
  return get<Department[]>('/department/list')
}
