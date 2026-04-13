import { get, post, put } from '@/utils/request'
import type { CarePlanTemplate, CareTask, CareTaskStats, PageResult } from './types'

export interface CarePlanTemplatePayload {
  templateName: string
  plantCategory?: string
  plantSpecies?: string
  description?: string
  items: Array<{
    careType: string
    cycleType: string
    cycleValue: number
    careDetail: string
    careNote?: string
    remindAdvance?: number
    sort?: number
  }>
}

export const getCarePlanTemplateList = (params?: {
  plantCategory?: string
  status?: number
}) => {
  return get<CarePlanTemplate[]>('/care-plan/template/list', params)
}

export const createCarePlanTemplate = (data: CarePlanTemplatePayload) => {
  return post<CarePlanTemplate>('/care-plan/template', data)
}

export const getCareTaskList = (params?: {
  current?: number
  size?: number
  status?: string
  keyword?: string
}) => {
  return get<PageResult<CareTask>>('/care-plan/task/list', params)
}

export const getCareTaskStats = () => {
  return get<CareTaskStats>('/care-plan/task/stats')
}

export const adjustCareTask = (taskId: number, data: {
  dueDate?: string
  cycleType?: string
  cycleValue?: number
  careDetail?: string
  adjustNote?: string
}) => {
  return put<CareTask>(`/care-plan/task/${taskId}/adjust`, data)
}
