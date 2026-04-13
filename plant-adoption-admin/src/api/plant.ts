import { get, post, put, del } from '@/utils/request'
import type { Plant, PageResult } from './types'

export const getPlantList = (params: {
  current?: number
  size?: number
  status?: string
  keyword?: string
  location?: string
}) => {
  return get<PageResult<Plant>>('/plant/list', params)
}

export const getPlantDetail = (id: number) => {
  return get<Plant>(`/plant/${id}`)
}

export const createPlant = (data: Partial<Plant>) => {
  return post<Plant>('/plant', data)
}

export const updatePlant = (id: number, data: Partial<Plant>) => {
  return put<Plant>(`/plant/${id}`, data)
}

export const deletePlant = (id: number) => {
  return del(`/plant/${id}`)
}

export const getPlantStats = () => {
  return get<{ total: number; available: number; adopted: number; maintenance: number }>('/plant/stats')
}
