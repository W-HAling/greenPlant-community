export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar: string
  departmentId: number
  departmentName: string
  role: string
  token?: string
}

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

export interface CarePlanItem {
  id?: number
  careType: string
  cycleType: string
  cycleValue: number
  careDetail: string
  careNote?: string
  remindAdvance?: number
  sort?: number
}

export interface CarePlanTemplate {
  id: number
  templateName: string
  plantCategory?: string
  plantSpecies?: string
  description?: string
  status: number
  items: CarePlanItem[]
  createTime: string
}

export interface CareTask {
  id: number
  plantId: number
  plantName?: string
  adopterId: number
  adopterName?: string
  dueDate: string
  status: string
  careType?: string
  careDetail?: string
  lastRemindTime?: string
  completedTime?: string
  createTime: string
  cycleTypeOverride?: string
  cycleValueOverride?: number
  careDetailOverride?: string
}

export interface CareTaskStats {
  total: number
  pending: number
  overdue: number
  done: number
}

export interface AdoptionRecord {
  id: number
  plantId: number
  plantName: string
  userId: number
  userName: string
  userPhone: string
  userAvatar: string
  status: string
  applyTime: string
  approveTime: string
  approverId: number
  approverName: string
  approveRemark: string
  returnTime: string
  returnReason: string
  plantImageUrl: string
  plantLocation: string
}

export interface CareLog {
  id: number
  plantId: number
  plantName: string
  userId: number
  userName: string
  userAvatar: string
  careType: string
  careTime: string
  description: string
  images: string
  plantImageUrl: string
  plantLocation: string
}

export interface DriftBottle {
  id: number
  senderId: number
  receiverId?: number
  senderName?: string
  receiverName?: string
  content: string
  imageUrls?: string
  status: string
  pickTime?: string
  replyContent?: string
  replyTime?: string
  pickExpireTime?: string
  createTime: string
}

export interface CommunityPost {
  id: number
  userId: number
  userName: string
  userAvatar: string
  title: string
  content: string
  images: string
  postType: string
  viewCount: number
  likeCount: number
  commentCount: number
  isTop: number
  isEssence: number
  status: number
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface Department {
  id: number
  name: string
  parentId: number
  level: number
  sort: number
  leader: string
  phone: string
  status: number
  createTime: string
}

export interface SysConfig {
  id: number
  configKey: string
  configValue: string
  configType: string
  description: string
  createTime: string
}
