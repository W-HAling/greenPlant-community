<template>
  <view class="notification-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">消息通知</text>
      <view class="mark-all" @click="markAllRead">
        <text class="mark-text">全部已读</text>
      </view>
    </view>
    
    <scroll-view scroll-y class="notification-scroll" @scrolltolower="loadMore">
      <view class="notification-list">
        <view
          class="notification-item"
          :class="{ unread: !item.isRead }"
          v-for="(item, index) in notifications"
          :key="item.id"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="handleClick(item)"
        >
          <view class="notification-icon" :class="item.notificationType?.toLowerCase()">
            <text class="icon-emoji">{{ iconEmojiMap[item.notificationType] }}</text>
          </view>
          <view class="notification-content">
            <view class="content-header">
              <text class="notification-title">{{ item.title }}</text>
              <view class="unread-dot" v-if="!item.isRead"></view>
            </view>
            <text class="notification-text">{{ item.content }}</text>
            <text class="notification-time">{{ formatTime(item.createTime) }}</text>
          </view>
          <view class="notification-arrow">
            <text class="arrow-text">›</text>
          </view>
        </view>
      </view>
      
      <view class="loadmore-section" v-if="notifications.length > 0">
        <view class="loadmore-loading" v-if="loadStatus === 'loading'">
          <view class="loading-spinner"></view>
          <text class="loadmore-text">加载中...</text>
        </view>
        <view class="loadmore-end" v-else-if="loadStatus === 'nomore'">
          <view class="end-line"></view>
          <text class="end-text">没有更多消息了</text>
          <view class="end-line"></view>
        </view>
      </view>
      
      <view class="empty-state" v-if="notifications.length === 0 && loadStatus !== 'loading'">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">🔔</text>
        </view>
        <text class="empty-title">暂无消息</text>
        <text class="empty-desc">您的消息通知将显示在这里</text>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { notificationApi } from '../../api'

const statusBarHeight = ref(0)
const notifications = ref<any[]>([])
const current = ref(1)
const loadStatus = ref<'loadmore' | 'loading' | 'nomore'>('loadmore')

const iconEmojiMap: Record<string, string> = {
  ADOPTION: '🌱',
  CARE: '💧',
  SYSTEM: '🔔',
  LIKE: '❤️',
  COMMENT: '💬'
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadNotifications = async (refresh = false) => {
  if (refresh) {
    current.value = 1
    notifications.value = []
    loadStatus.value = 'loadmore'
  }
  
  if (loadStatus.value === 'nomore') return
  
  loadStatus.value = 'loading'
  
  try {
    const data = await notificationApi.getList({
      current: current.value,
      size: 20
    })
    
    if (refresh) {
      notifications.value = data.records
    } else {
      notifications.value.push(...data.records)
    }
    
    if (notifications.value.length >= data.total) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (e) {
    loadStatus.value = 'loadmore'
    console.error(e)
  }
}

const loadMore = () => {
  if (loadStatus.value !== 'loadmore') return
  current.value++
  loadNotifications()
}

const markAllRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (e) {
    console.error(e)
  }
}

const handleClick = async (item: any) => {
  if (!item.isRead) {
    try {
      await notificationApi.markAsRead(item.id)
      item.isRead = 1
    } catch (e) {
      console.error(e)
    }
  }
  
  if (item.relatedType === 'AdoptionRecord') {
    uni.navigateTo({ url: '/pages/adoption/my' })
  } else if (item.relatedType === 'CommunityPost') {
    uni.navigateTo({ url: `/pages/community/detail?id=${item.relatedId}` })
  } else if (item.relatedType === 'CareTask') {
    uni.navigateTo({ url: '/pages/care/tasks' })
  } else if (item.relatedType === 'DriftBottle') {
    uni.navigateTo({ url: '/pages/drift-bottle/my' })
  }
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  loadNotifications(true)
})

onShow(() => {
  loadNotifications(true)
})
</script>

<style lang="scss" scoped>
.notification-page {
  min-height: 100vh;
  background: #F5F7F5;
  display: flex;
  flex-direction: column;
}

.status-bar {
  background: #FFFFFF;
}

.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E5E5;
  
  .back-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #F5F7F5;
    border-radius: 50%;
    
    .back-icon {
      font-size: 32rpx;
      color: #333;
    }
  }
  
  .nav-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #1A1A1A;
  }
  
  .mark-all {
    padding: 12rpx 20rpx;
    background: #E8F5E9;
    border-radius: 24rpx;
    
    .mark-text {
      font-size: 24rpx;
      color: #4CAF50;
    }
  }
}

.notification-scroll {
  flex: 1;
  height: calc(100vh - 120rpx);
}

.notification-list {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.notification-item {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  animation: slideIn 0.3s ease-out both;
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.99);
  }
  
  &.unread {
    background: linear-gradient(135deg, #FFFFFF 0%, #F1F8E9 100%);
    border-left: 6rpx solid #4CAF50;
  }
  
  .notification-icon {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 20rpx;
    margin-right: 20rpx;
    
    .icon-emoji {
      font-size: 36rpx;
    }
    
    &.adoption {
      background: #E8F5E9;
    }
    
    &.care {
      background: #E3F2FD;
    }
    
    &.system {
      background: #FFF8E1;
    }
    
    &.like {
      background: #FCE4EC;
    }
    
    &.comment {
      background: #F3E5F5;
    }
  }
  
  .notification-content {
    flex: 1;
    min-width: 0;
    
    .content-header {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;
      
      .notification-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #1A1A1A;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .unread-dot {
        width: 16rpx;
        height: 16rpx;
        background: #F44336;
        border-radius: 50%;
        margin-left: 12rpx;
      }
    }
    
    .notification-text {
      display: block;
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 8rpx;
    }
    
    .notification-time {
      display: block;
      font-size: 22rpx;
      color: #999;
    }
  }
  
  .notification-arrow {
    margin-left: 12rpx;
    
    .arrow-text {
      font-size: 32rpx;
      color: #CCCCCC;
    }
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.loadmore-section {
  padding: 32rpx 24rpx;
}

.loadmore-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  
  .loading-spinner {
    width: 32rpx;
    height: 32rpx;
    border: 3rpx solid #E5E5E5;
    border-top-color: #4CAF50;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  
  .loadmore-text {
    font-size: 26rpx;
    color: #999;
  }
}

.loadmore-end {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  
  .end-line {
    width: 60rpx;
    height: 1rpx;
    background: #E5E5E5;
  }
  
  .end-text {
    font-size: 24rpx;
    color: #999;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx;
  
  .empty-icon-wrapper {
    width: 160rpx;
    height: 160rpx;
    background: #E8F5E9;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 32rpx;
    
    .empty-icon {
      font-size: 80rpx;
    }
  }
  
  .empty-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1A1A1A;
    margin-bottom: 12rpx;
  }
  
  .empty-desc {
    font-size: 26rpx;
    color: #999;
  }
}

.bottom-space {
  height: 40rpx;
}
</style>
