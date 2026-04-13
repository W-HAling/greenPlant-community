import { del, get } from '@/utils/request'
import type { DriftBottle, PageResult } from './types'

export interface DriftBottleQuery {
  current?: number
  size?: number
  status?: string
  keyword?: string
}

export const getDriftBottleList = (params: DriftBottleQuery) => {
  return get<PageResult<DriftBottle>>('/drift-bottle/list', params)
}

export const deleteDriftBottle = (id: number) => {
  return del(`/drift-bottle/${id}`)
}
