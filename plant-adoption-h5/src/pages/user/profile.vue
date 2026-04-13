<template>
  <view class="profile-page">
    <view class="header-section">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="user-info" @click="handleAvatarClick">
          <view class="avatar-wrapper">
            <view class="user-avatar">
              <text class="avatar-text">{{ userStore.userInfo?.nickname?.charAt(0) || '?' }}</text>
            </view>
            <view class="avatar-badge" v-if="userStore.isLoggedIn">
              <text class="badge-icon">📷</text>
            </view>
          </view>
          <view class="user-text">
            <text class="user-name">{{ userStore.userInfo?.nickname || '未登录' }}</text>
            <text class="user-phone">{{ formatPhone(userStore.userInfo?.phone) }}</text>
          </view>
        </view>
        
        <view class="settings-btn" @click="goSettings">
          <text class="settings-icon">⚙️</text>
        </view>
      </view>
    </view>
    
    <view class="stats-section">
      <view class="stats-card">
        <view class="stat-item" @click="goMyAdoption">
          <text class="stat-value">{{ adoptedCount }}</text>
          <text class="stat-label">我的领养</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="goMyCare">
          <text class="stat-value">{{ careCount }}</text>
          <text class="stat-label">养护记录</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="goMyPosts">
          <text class="stat-value">{{ postCount }}</text>
          <text class="stat-label">我的帖子</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-group">
        <view class="menu-item" @click="goNotification">
          <view class="menu-icon notification">
            <text class="icon-emoji">🔔</text>
          </view>
          <text class="menu-title">消息通知</text>
          <view class="menu-badge" v-if="unreadCount > 0">
            <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
      
      <view class="menu-group">
        <view class="group-title">我的绿植</view>
        
        <view class="menu-item" @click="goMyAdoption">
          <view class="menu-icon plant">
            <text class="icon-emoji">🌱</text>
          </view>
          <text class="menu-title">我的领养</text>
          <text class="menu-desc">{{ adoptedCount }}株</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="goMyCare">
          <view class="menu-icon care">
            <text class="icon-emoji">💧</text>
          </view>
          <text class="menu-title">养护记录</text>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="goMyTasks">
          <view class="menu-icon task">
            <text class="icon-emoji">🗓️</text>
          </view>
          <text class="menu-title">养护任务</text>
          <view class="menu-badge task-badge" v-if="activeTaskCount > 0">
            <text class="badge-text">{{ activeTaskCount > 99 ? '99+' : activeTaskCount }}</text>
          </view>
          <text class="menu-desc" v-else>查看待办</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
      
      <view class="menu-group">
        <view class="group-title">社区互动</view>
        
        <view class="menu-item" @click="goMyPosts">
          <view class="menu-icon post">
            <text class="icon-emoji">📝</text>
          </view>
          <text class="menu-title">我的帖子</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="goMyLikes">
          <view class="menu-icon like">
            <text class="icon-emoji">❤️</text>
          </view>
          <text class="menu-title">我的点赞</text>
          <text class="menu-arrow">›</text>
        </view>

        <view class="menu-item" @click="goDriftBottle">
          <view class="menu-icon bottle">
            <text class="icon-emoji">🍾</text>
          </view>
          <text class="menu-title">漂流瓶</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
      
      <view class="menu-group">
        <view class="menu-item" @click="goAbout">
          <view class="menu-icon about">
            <text class="icon-emoji">ℹ️</text>
          </view>
          <text class="menu-title">关于我们</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="goFeedback">
          <view class="menu-icon feedback">
            <text class="icon-emoji">💬</text>
          </view>
          <text class="menu-title">意见反馈</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <view class="action-section" v-if="userStore.isLoggedIn">
      <view class="logout-btn" @click="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>
    
    <view class="action-section" v-else>
      <view class="login-btn" @click="goLogin">
        <text class="login-text">去登录</text>
      </view>
    </view>
    
    <view class="bottom-space"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { carePlanApi, notificationApi, userApi } from '../../api'

const userStore = useUserStore()

const adoptedCount = ref(0)
const careCount = ref(0)
const postCount = ref(0)
const unreadCount = ref(0)
const activeTaskCount = ref(0)

const formatPhone = (phone?: string) => {
  if (!phone) return '点击登录'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const loadData = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const [adopted, unread, tasks] = await Promise.all([
      userApi.getAdoptionStats(),
      notificationApi.getUnreadCount(),
      carePlanApi.getMyTasks()
    ])
    
    adoptedCount.value = adopted
    unreadCount.value = unread
    activeTaskCount.value = tasks.filter(task => task.status === 'PENDING' || task.status === 'OVERDUE').length
  } catch (e) {
    console.error(e)
  }
}

const handleAvatarClick = () => {
  if (!userStore.requireLogin()) return
  
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
    }
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#F44336',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        adoptedCount.value = 0
        unreadCount.value = 0
        activeTaskCount.value = 0
      }
    }
  })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

const goNotification = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/notification/list' })
}

const goMyAdoption = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/adoption/my' })
}

const goMyCare = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/care/list' })
}

const goMyTasks = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/care/tasks' })
}

const goMyPosts = () => {
  if (!userStore.requireLogin()) return
  const userId = userStore.userInfo?.id
  if (userId) {
    uni.setStorageSync('myPostsUserId', userId)
  }
  uni.switchTab({ url: '/pages/community/list' })
}

const goMyLikes = () => {
  if (!userStore.requireLogin()) return
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const goDriftBottle = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/drift-bottle/index' })
}

const goSettings = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const goAbout = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const goFeedback = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

onMounted(() => {
  loadData()
})

onShow(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: #F5F7F5;
}

.header-section {
  position: relative;
  padding-bottom: 60rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 280rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 32rpx;
  padding-top: calc(80rpx + env(safe-area-inset-top));
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-right: 24rpx;
}

.user-avatar {
  width: 140rpx;
  height: 140rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  
  .avatar-text {
    font-size: 56rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #4CAF50 0%, #81C784 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.avatar-badge {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 44rpx;
  height: 44rpx;
  background: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  
  .badge-icon {
    font-size: 20rpx;
  }
}

.user-text {
  .user-name {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 8rpx;
  }
  
  .user-phone {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.85);
  }
}

.settings-btn {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .settings-icon {
    font-size: 32rpx;
  }
}

.stats-section {
  padding: 0 24rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 10;
}

.stats-card {
  display: flex;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx 0;
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
  }
  
  .stat-label {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-top: 8rpx;
  }
}

.stat-divider {
  width: 1rpx;
  background: #E5E5E5;
  margin: 10rpx 0;
}

.menu-section {
  padding: 24rpx;
}

.menu-group {
  background: #FFFFFF;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.group-title {
  padding: 24rpx 28rpx 16rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #999;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx;
  border-bottom: 1rpx solid #F5F5F5;
  transition: background 0.2s ease;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: #F5F7F5;
  }
  
  .menu-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    
    .icon-emoji {
      font-size: 28rpx;
    }
    
    &.notification { background: #FFF3E0; }
    &.plant { background: #E8F5E9; }
    &.care { background: #E3F2FD; }
    &.post { background: #F3E5F5; }
    &.like { background: #FFEBEE; }
    &.about { background: #E0F7FA; }
    &.feedback { background: #F5F5F5; }
  }
  
  .menu-title {
    flex: 1;
    font-size: 30rpx;
    color: #1A1A1A;
  }
  
  .menu-desc {
    font-size: 26rpx;
    color: #999;
    margin-right: 12rpx;
  }
  
  .menu-badge {
    padding: 4rpx 16rpx;
    background: #F44336;
    border-radius: 20rpx;
    margin-right: 12rpx;
    
    .badge-text {
      font-size: 22rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }

  .task-badge {
    background: #FF7A00;
  }
  
  .menu-arrow {
    font-size: 32rpx;
    color: #CCCCCC;
  }
}

.action-section {
  padding: 24rpx;
}

.logout-btn {
  height: 96rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  
  .logout-text {
    font-size: 30rpx;
    color: #F44336;
  }
}

.login-btn {
  height: 96rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.35);
  
  .login-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #FFFFFF;
  }
}

.bottom-space {
  height: 120rpx;
}
</style>
