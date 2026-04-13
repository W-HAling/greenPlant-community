import { get, post, put } from '../utils/request'

export interface Plant {
  id: number
  name: string
  variety: string
  location: string
  imageUrl: string
  qrCodeUrl: string
  status: string
  adopterId: number
  adopterName: string
  adopterPhone: string
  adopterAvatar: string
  adoptionTime: string
  lastCareTime: string
  careCount: number
  description: string
  careTips: string
  carePlanTemplateId?: number
  createTime: string
}

export interface CareTask {
  id: number
  plantId: number
  plantName: string
  dueDate: string
  status: string
  careType: string
  careDetail: string
  cycleTypeOverride?: string
  cycleValueOverride?: number
  careDetailOverride?: string
}

export interface DriftBottle {
  id: number
  senderId: number
  receiverId?: number
  content: string
  imageUrls?: string
  status: string
  pickTime?: string
  replyContent?: string
  replyTime?: string
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const plantApi = {
  getList: (params: { current?: number; size?: number; status?: string; keyword?: string; location?: string }) => {
    return get<PageResult<Plant>>('/plant/list', params)
  },
  
  getDetail: (id: number) => {
    return get<Plant>(`/plant/${id}`)
  },
  
  getStats: () => {
    return get<{ total: number; available: number; adopted: number; maintenance: number }>('/plant/stats')
  }
}

export const authApi = {
  sendCode: (phone: string) => {
    return post('/auth/code', null, { url: `/auth/code?phone=${phone}` })
  },
  
  login: (phone: string, code: string) => {
    return post('/auth/login', { phone, code })
  },
  
  register: (data: { phone: string; code: string; nickname: string; departmentId?: number }) => {
    return post('/auth/register', data)
  }
}

export const adoptionApi = {
  apply: (plantId: number, remark?: string) => {
    return post('/adoption/apply', { plantId, remark })
  },
  
  cancel: (recordId: number) => {
    return post(`/adoption/cancel/${recordId}`)
  },
  
  returnPlant: (recordId: number, reason?: string) => {
    return post(`/adoption/return/${recordId}${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`)
  },
  
  getMyRecords: (params: { current?: number; size?: number; status?: string }) => {
    return get('/adoption/my', params)
  },
  
  canAdopt: () => {
    return get<boolean>('/adoption/can-adopt')
  }
}

export const careApi = {
  record: (data: { plantId: number; careType: string; description?: string; images?: string }) => {
    return post('/care', data)
  },
  
  getMyLogs: (params: { current?: number; size?: number; plantId?: number; careType?: string }) => {
    return get('/care/my', params)
  },
  
  getList: (params: { current?: number; size?: number; plantId?: number; careType?: string }) => {
    return get('/care/list', params)
  }
}

export const carePlanApi = {
  getMyTasks: (params?: { status?: string }) => {
    return get<CareTask[]>('/care-plan/task/my', params)
  },

  executeTask: (taskId: number, data?: { description?: string; images?: string }) => {
    return put(`/care-plan/task/${taskId}/execute`, data)
  },

  adjustTask: (taskId: number, data?: {
    dueDate?: string
    cycleType?: string
    cycleValue?: number
    careDetail?: string
    adjustNote?: string
  }) => {
    return put(`/care-plan/task/${taskId}/adjust`, data)
  }
}

export const communityApi = {
  getPosts: (params: { current?: number; size?: number; postType?: string }) => {
    return get('/community/post/list', params)
  },
  
  getPostDetail: (postId: number) => {
    return get(`/community/post/${postId}`)
  },
  
  publishPost: (data: { title: string; content: string; postType?: string; images?: string }) => {
    return post('/community/post', data)
  },
  
  toggleLike: (postId: number) => {
    return post(`/community/post/${postId}/like`)
  },
  
  getComments: (params: { current?: number; size?: number; postId: number }) => {
    return get('/community/comment/list', params)
  },
  
  addComment: (data: { postId: number; content: string; parentId?: number; replyUserId?: number }) => {
    return post('/community/comment', data)
  }
}

export const notificationApi = {
  getList: (params: { current?: number; size?: number }) => {
    return get('/notification/list', params)
  },
  
  getUnreadCount: () => {
    return get<number>('/notification/unread-count')
  },
  
  markAsRead: (id: number) => {
    return put(`/notification/${id}/read`)
  },
  
  markAllAsRead: () => {
    return put('/notification/read-all')
  }
}

export const userApi = {
  getInfo: () => {
    return get('/user/info')
  },
  
  updateInfo: (data: { nickname?: string; avatar?: string; departmentId?: number }) => {
    return put('/user/info', data)
  },
  
  getAdoptionStats: () => {
    return get<number>('/user/adoption/stats')
  }
}

export const driftBottleApi = {
  throwBottle: (data: { content: string; imageUrls?: string[] }) => {
    return post<DriftBottle>('/drift-bottle', data)
  },

  pickBottle: () => {
    return post<DriftBottle>('/drift-bottle/pick')
  },

  replyBottle: (id: number, data: { replyContent: string }) => {
    return post<DriftBottle>(`/drift-bottle/${id}/reply`, data)
  },

  releaseBottle: (id: number) => {
    return post(`/drift-bottle/${id}/release`)
  },

  getMyBottles: (params?: { type?: string }) => {
    return get<DriftBottle[]>('/drift-bottle/my', params)
  }
}
