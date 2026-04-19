<template>
  <view class="index-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <view class="header">
      <view class="header-content">
        <view class="welcome">
          <text class="greeting">{{ greeting }}</text>
          <text class="username">{{ userStore.userInfo?.nickname || '访客' }}</text>
        </view>
        <view class="header-actions">
          <view class="notification-btn" @click="goNotification">
            <text class="bell-icon">🔔</text>
            <view class="badge" v-if="unreadCount > 0">
              <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <scroll-view scroll-y class="main-scroll" @scrolltolower="loadMorePosts">
      <view class="stats-section">
        <view class="stats-card">
          <view class="stat-item" @click="goPlantList('AVAILABLE')">
            <text class="stat-value">{{ stats.available }}</text>
            <text class="stat-label">可领养</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goPlantList('ADOPTED')">
            <text class="stat-value">{{ stats.adopted }}</text>
            <text class="stat-label">已领养</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ myAdoptedCount }}</text>
            <text class="stat-label">我的领养</text>
          </view>
        </view>
      </view>
      
      <view class="quick-entry-section" v-if="userStore.isLoggedIn">
        <view class="care-focus-card" @click="goCareTasks">
          <view class="care-focus-content">
            <view class="care-focus-copy">
              <text class="care-focus-title">养护任务</text>
              <text class="care-focus-desc">
                {{ activeTaskCount > 0 ? `当前有 ${activeTaskCount} 项待处理任务` : '查看今日养护安排与执行记录' }}
              </text>
            </view>
            <view class="care-focus-side">
              <view class="care-focus-badge" v-if="activeTaskCount > 0">
                <text class="care-focus-badge-text">{{ activeTaskCount > 99 ? '99+' : activeTaskCount }}</text>
              </view>
              <text class="care-focus-action">{{ activeTaskCount > 0 ? '立即处理' : '查看任务' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <view class="section-title-group">
            <text class="section-title">热门绿植</text>
            <text class="section-subtitle">发现适合你的绿植伙伴</text>
          </view>
          <text class="section-more" @click="goPlantList()">查看全部 ›</text>
        </view>
        
        <scroll-view scroll-x class="plant-scroll" show-scrollbar="false">
          <view class="plant-list">
            <view
              class="plant-card"
              v-for="plant in hotPlants"
              :key="plant.id"
              @click="goPlantDetail(plant.id)"
            >
              <view class="plant-image-wrapper">
                <image 
                  class="plant-image" 
                  :src="plant.imageUrl?.split(',')[0] || '/static/images/default-plant.png'" 
                  mode="aspectFill" 
                />
                <view class="status-tag" :class="plant.status.toLowerCase()">
                  {{ statusMap[plant.status] }}
                </view>
              </view>
              <view class="plant-info">
                <text class="plant-name">{{ plant.name }}</text>
                <text class="plant-variety">{{ plant.variety || '绿植' }}</text>
                <view class="plant-location">
                  <text class="location-icon">📍</text>
                  <text class="location-text">{{ plant.location }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
      
      <view class="section">
        <view class="section-header">
          <view class="section-title-group">
            <text class="section-title">社区动态</text>
            <text class="section-subtitle">分享养护心得</text>
          </view>
          <text class="section-more" @click="goCommunity">查看全部 ›</text>
        </view>
        
        <view class="post-list">
          <view
            class="post-card"
            v-for="post in latestPosts"
            :key="post.id"
            @click="goPostDetail(post.id)"
          >
            <view class="post-header">
              <view class="user-avatar">
                <text class="avatar-text">{{ post.userName?.charAt(0) }}</text>
              </view>
              <view class="user-info">
                <text class="user-name">{{ post.userName }}</text>
                <text class="post-time">{{ formatTime(post.createTime) }}</text>
              </view>
            </view>
            <view class="post-content">
              <text class="post-title">{{ post.title }}</text>
              <text class="post-desc">{{ post.content }}</text>
            </view>
            <view class="post-footer">
              <view class="stat-item">
                <text class="stat-icon">👁</text>
                <text class="stat-value">{{ post.viewCount || 0 }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-icon">❤️</text>
                <text class="stat-value">{{ post.likeCount || 0 }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-icon">💬</text>
                <text class="stat-value">{{ post.commentCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="empty-state" v-if="latestPosts.length === 0 && !isLoading">
          <text class="empty-icon">📝</text>
          <text class="empty-text">暂无动态</text>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { carePlanApi, plantApi, communityApi, notificationApi, userApi } from '../../api'
import type { Plant } from '../../api'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const stats = ref({ total: 0, available: 0, adopted: 0, maintenance: 0 })
const hotPlants = ref<Plant[]>([])
const latestPosts = ref<any[]>([])
const unreadCount = ref(0)
const myAdoptedCount = ref(0)
const activeTaskCount = ref(0)
const isLoading = ref(false)

const statusMap: Record<string, string> = {
  AVAILABLE: '可领养',
  ADOPTED: '已领养',
  MAINTENANCE: '维护中'
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}-${date.getDate()}`
}

const loadData = async () => {
  try {
    const [statsData, plantsData] = await Promise.all([
      plantApi.getStats(),
      plantApi.getList({ current: 1, size: 6, status: 'AVAILABLE' })
    ])
    
    stats.value = statsData
    hotPlants.value = plantsData.records
  } catch (e) {
    console.error(e)
  }
}

const loadPosts = async () => {
  if (isLoading.value) return
  isLoading.value = true
  
  try {
    const data = await communityApi.getPosts({ current: 1, size: 5 })
    latestPosts.value = data.records
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
}

const loadMorePosts = () => {}

const loadNotificationCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    unreadCount.value = await notificationApi.getUnreadCount()
  } catch (e) {
    console.error(e)
  }
}

const loadMyAdoptedCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    myAdoptedCount.value = await userApi.getAdoptionStats()
  } catch (e) {
    console.error(e)
  }
}

const loadCareTaskCount = async () => {
  if (!userStore.isLoggedIn) {
    activeTaskCount.value = 0
    return
  }
  try {
    const tasks = await carePlanApi.getMyTasks()
    activeTaskCount.value = tasks.filter(task => task.status === 'PENDING' || task.status === 'OVERDUE').length
  } catch (e) {
    console.error(e)
  }
}

const goNotification = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/notification/list' })
}

const goCareTasks = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/care/tasks' })
}

const goPlantList = (status?: string) => {
  let url = '/pages/plant/list'
  if (status) {
    url += `?status=${status}`
  }
  uni.switchTab({ url })
}

const goPlantDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/plant/detail?id=${id}` })
}

const goCommunity = () => {
  uni.switchTab({ url: '/pages/community/list' })
}

const goPostDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  loadData()
  loadPosts()
})

onShow(() => {
  loadNotificationCount()
  loadMyAdoptedCount()
  loadCareTaskCount()
})
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background: #F5F7F5;
  display: flex;
  flex-direction: column;
}

.status-bar {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
}

.header {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  padding-bottom: 32rpx;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx;
  }
}

.welcome {
  .greeting {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: 4rpx;
  }
  
  .username {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #FFFFFF;
  }
}

.header-actions {
  display: flex;
  align-items: center;
}

.notification-btn {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  
  .bell-icon {
    font-size: 36rpx;
  }
  
  .badge {
    position: absolute;
    top: 4rpx;
    right: 4rpx;
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background: #FF5252;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .badge-text {
      font-size: 20rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }
}

.main-scroll {
  flex: 1;
  height: calc(100vh - 180rpx);
}

.stats-section {
  margin-top: -24rpx;
  padding: 0 24rpx;
  position: relative;
  z-index: 10;
}

.quick-entry-section {
  padding: 24rpx 24rpx 0;
}

.care-focus-card {
  border-radius: 24rpx;
  background: linear-gradient(135deg, #fff8ee 0%, #fff2db 100%);
  box-shadow: 0 12rpx 26rpx rgba(255, 145, 0, 0.12);
  padding: 28rpx;
}

.care-focus-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.care-focus-copy {
  flex: 1;
}

.care-focus-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #6f4200;
}

.care-focus-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: #9a6400;
}

.care-focus-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
}

.care-focus-badge {
  min-width: 48rpx;
  height: 48rpx;
  padding: 0 12rpx;
  border-radius: 24rpx;
  background: #ff7a00;
  display: flex;
  align-items: center;
  justify-content: center;
}

.care-focus-badge-text {
  font-size: 24rpx;
  font-weight: 700;
  color: #ffffff;
}

.care-focus-action {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
  color: #c66b00;
}

.stats-card {
  display: flex;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.stat-item {
  flex: 1;
  text-align: center;
  
  .stat-value {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: #1A1A1A;
    line-height: 1.2;
  }
  
  .stat-label {
    display: block;
    font-size: 24rpx;
    color: #666;
    margin-top: 8rpx;
  }
}

.stat-divider {
  width: 1rpx;
  background: #E5E5E5;
  margin: 8rpx 0;
}

.section {
  margin-top: 32rpx;
  padding: 0 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.section-title-group {
  .section-title {
    display: block;
    font-size: 34rpx;
    font-weight: 600;
    color: #1A1A1A;
    line-height: 1.3;
  }
  
  .section-subtitle {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-top: 4rpx;
  }
}

.section-more {
  font-size: 26rpx;
  color: #4CAF50;
  padding: 8rpx 0;
}

.plant-scroll {
  white-space: nowrap;
  margin: 0 -24rpx;
  padding: 0 24rpx;
}

.plant-list {
  display: inline-flex;
  gap: 20rpx;
  padding-right: 24rpx;
}

.plant-card {
  width: 280rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  .plant-image-wrapper {
    position: relative;
    width: 100%;
    height: 280rpx;
    background: #F5F7F5;
  }
  
  .plant-image {
    width: 100%;
    height: 100%;
  }
  
  .status-tag {
    position: absolute;
    top: 16rpx;
    left: 16rpx;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    font-size: 22rpx;
    font-weight: 500;
    
    &.available {
      background: #E8F5E9;
      color: #2E7D32;
    }
    
    &.adopted {
      background: #FFF3E0;
      color: #E65100;
    }
    
    &.maintenance {
      background: #ECEFF1;
      color: #546E7A;
    }
  }
  
  .plant-info {
    padding: 20rpx;
    
    .plant-name {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: #1A1A1A;
      margin-bottom: 6rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .plant-variety {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-bottom: 10rpx;
    }
    
    .plant-location {
      display: flex;
      align-items: center;
      
      .location-icon {
        font-size: 22rpx;
        margin-right: 4rpx;
      }
      
      .location-text {
        font-size: 22rpx;
        color: #666;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.post-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  
  .post-header {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    .user-avatar {
      width: 64rpx;
      height: 64rpx;
      background: linear-gradient(135deg, #4CAF50 0%, #81C784 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16rpx;
      
      .avatar-text {
        font-size: 28rpx;
        font-weight: 600;
        color: #FFFFFF;
      }
    }
    
    .user-info {
      flex: 1;
      
      .user-name {
        display: block;
        font-size: 28rpx;
        font-weight: 500;
        color: #1A1A1A;
      }
      
      .post-time {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
  }
  
  .post-content {
    .post-title {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: #1A1A1A;
      margin-bottom: 8rpx;
      line-height: 1.4;
    }
    
    .post-desc {
      display: block;
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
  
  .post-footer {
    display: flex;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid #F5F5F5;
    
    .stat-item {
      display: flex;
      align-items: center;
      margin-right: 32rpx;
      
      .stat-icon {
        font-size: 24rpx;
        margin-right: 6rpx;
      }
      
      .stat-value {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64rpx 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 16rpx;
    opacity: 0.5;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.bottom-space {
  height: 120rpx;
}
</style>
