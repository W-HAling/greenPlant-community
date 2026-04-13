import { get, put } from '@/utils/request'
import type { SysConfig } from './types'

export const getConfigList = () => {
  return get<SysConfig[]>('/config/list')
}

export const getConfigValue = (key: string) => {
  return get<string>(`/config/${key}`)
}

export const setConfigValue = (key: string, value: string) => {
  return put(`/config/${key}`, value, {
    headers: { 'Content-Type': 'text/plain' }
  })
}
