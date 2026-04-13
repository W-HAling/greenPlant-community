import { get, post } from '@/utils/request'
import type { AdoptionRecord, PageResult } from './types'

export const getAdoptionList = (params: {
  current?: number
  size?: number
  userId?: number
  status?: string
}) => {
  return get<PageResult<AdoptionRecord>>('/adoption/list', params)
}

export const getAdoptionDetail = (id: number) => {
  return get<AdoptionRecord>(`/adoption/${id}`)
}

export const approveAdoption = (recordId: number, approved: boolean, remark?: string) => {
  return post<AdoptionRecord>('/adoption/approve', { recordId, approved, remark })
}
