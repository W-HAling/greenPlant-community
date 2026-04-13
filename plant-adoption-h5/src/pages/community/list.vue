<template>
  <view class="community-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <view class="header-section">
      <text class="header-title">社区</text>
      <view class="header-actions">
        <view class="action-btn" @click="goPublish">
          <text class="action-icon">✏️</text>
          <text class="action-text">发布</text>
        </view>
      </view>
    </view>
    
    <view class="filter-section">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view class="filter-tabs">
          <view 
            class="tab-item" 
            :class="{ active: currentTab === index }"
            v-for="(tab, index) in postTabs"
            :key="index"
            @click="handleTabChange(index)"
          >
            <text class="tab-text">{{ tab.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <scroll-view scroll-y class="post-scroll" @scrolltolower="loadMore">
      <view class="post-list">
        <view
          class="post-card"
          v-for="(post, index) in posts"
          :key="post.id"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="goDetail(post.id)"
        >
          <view class="post-header">
            <view class="user-avatar">
              <text class="avatar-text">{{ post.userName?.charAt(0) }}</text>
            </view>
            <view class="user-info">
              <text class="user-name">{{ post.userName }}</text>
              <text class="post-time">{{ formatTime(post.createTime) }}</text>
            </view>
            <view class="post-type-tag" :class="post.postType?.toLowerCase()">
              {{ postTypeMap[post.postType] }}
            </view>
          </view>
          
          <view class="post-content">
            <text class="post-title">{{ post.title }}</text>
            <text class="post-text">{{ post.content }}</text>
            <view class="post-images" v-if="post.images">
              <image
                v-for="(img, imgIndex) in parseImages(post.images).slice(0, 3)"
                :key="imgIndex"
                class="post-image"
                :src="img"
                mode="aspectFill"
              />
            </view>
          </view>
          
          <view class="post-footer">
            <view class="stat-item">
              <text class="stat-icon">👁</text>
              <text class="stat-value">{{ post.viewCount || 0 }}</text>
            </view>
            <view class="stat-item" :class="{ liked: post.isLiked }" @click.stop="handleLike(post)">
              <text class="stat-icon">{{ post.isLiked ? '❤️' : '🤍' }}</text>
              <text class="stat-value">{{ post.likeCount || 0 }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">💬</text>
              <text class="stat-value">{{ post.commentCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="loadmore-section" v-if="posts.length > 0">
        <view class="loadmore-loading" v-if="loadStatus === 'loading'">
          <view class="loading-spinner"></view>
          <text class="loadmore-text">加载中...</text>
        </view>
        <view class="loadmore-end" v-else-if="loadStatus === 'nomore'">
          <view class="end-line"></view>
          <text class="end-text">已经到底啦</text>
          <view class="end-line"></view>
        </view>
      </view>
      
      <view class="empty-state" v-if="posts.length === 0 && loadStatus !== 'loading'">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">📝</text>
        </view>
        <text class="empty-title">暂无帖子</text>
        <text class="empty-desc">快来发布第一篇帖子吧</text>
        <view class="empty-btn" @click="goPublish">
          <text class="empty-btn-text">发布帖子</text>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
    
    <view class="publish-fab" @click="goPublish">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { communityApi } from '../../api'

const userStore = useUserStore()

const statusBarHeight = ref(0)
const currentTab = ref(0)
const posts = ref<any[]>([])
const current = ref(1)
const loadStatus = ref<'loadmore' | 'loading' | 'nomore'>('loadmore')
const myPostsFilterUserId = ref<number | null>(null)

const postTabs = [
  { name: '全部', type: '' },
  { name: '分享', type: 'SHARE' },
  { name: '问答', type: 'QUESTION' },
  { name: '经验', type: 'EXPERIENCE' }
]

const postTypeMap: Record<string, string> = {
  SHARE: '分享',
  QUESTION: '问答',
  EXPERIENCE: '经验'
}

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

const parseImages = (images: string) => {
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const loadPosts = async (refresh = false) => {
  if (refresh) {
    current.value = 1
    posts.value = []
    loadStatus.value = 'loadmore'
  }
  
  if (loadStatus.value === 'nomore') return
  
  loadStatus.value = 'loading'
  
  try {
    const postType = postTabs[currentTab.value].type
    const params: Record<string, any> = { current: current.value, size: 10 }
    if (postType) params.postType = postType
    if (myPostsFilterUserId.value) params.userId = myPostsFilterUserId.value
    const data = await communityApi.getPosts(params)
    
    if (refresh) {
      posts.value = data.records
    } else {
      posts.value.push(...data.records)
    }
    
    if (posts.value.length >= data.total) {
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
  loadPosts()
}

const handleTabChange = (index: number) => {
  if (currentTab.value === index) return
  currentTab.value = index
  loadPosts(true)
}

const handleLike = async (post: any) => {
  if (!userStore.requireLogin()) return
  
  try {
    if (post.isLiked) {
      await communityApi.unlikePost(post.id)
      post.isLiked = false
      post.likeCount--
    } else {
      await communityApi.likePost(post.id)
      post.isLiked = true
      post.likeCount++
    }
  } catch (e) {
    console.error(e)
  }
}

const goDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
}

const goPublish = () => {
  if (!userStore.requireLogin()) return
  uni.navigateTo({ url: '/pages/community/publish' })
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20

  const myPostsUserId = uni.getStorageSync('myPostsUserId')
  if (myPostsUserId) {
    uni.removeStorageSync('myPostsUserId')
    currentTab.value = 0
    myPostsFilterUserId = myPostsUserId
  }
  
  loadPosts(true)
})

onShow(() => {
  loadPosts(true)
})
</script>

<style lang="scss" scoped>
.community-page {
  min-height: 100vh;
  background: #F5F7F5;
  display: flex;
  flex-direction: column;
}

.status-bar {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  
  .header-title {
    font-size: 40rpx;
    font-weight: 700;
    color: #FFFFFF;
  }
  
  .header-actions {
    display: flex;
    gap: 16rpx;
  }
  
  .action-btn {
    display: flex;
    align-items: center;
    padding: 12rpx 24rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 32rpx;
    
    .action-icon {
      font-size: 24rpx;
      margin-right: 8rpx;
    }
    
    .action-text {
      font-size: 26rpx;
      font-weight: 500;
      color: #FFFFFF;
    }
  }
}

.filter-section {
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E5E5;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-tabs {
  display: inline-flex;
  padding: 16rpx 24rpx;
  gap: 16rpx;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 32rpx;
  background: #F5F7F5;
  border-radius: 32rpx;
  transition: all 0.25s ease;
  
  &.active {
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  }
  
  .tab-text {
    font-size: 26rpx;
    font-weight: 500;
    color: #666;
    transition: color 0.25s ease;
  }
  
  &.active .tab-text {
    color: #FFFFFF;
  }
}

.post-scroll {
  flex: 1;
  height: calc(100vh - 200rpx);
}

.post-list {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.post-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  animation: cardFadeIn 0.4s ease-out both;
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.99);
  }
  
  .post-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .user-avatar {
      width: 72rpx;
      height: 72rpx;
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
        font-weight: 600;
        color: #1A1A1A;
      }
      
      .post-time {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
    
    .post-type-tag {
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
      font-size: 22rpx;
      font-weight: 500;
      
      &.share {
        background: #E3F2FD;
        color: #1565C0;
      }
      
      &.question {
        background: #FFF8E1;
        color: #F57C00;
      }
      
      &.experience {
        background: #E8F5E9;
        color: #2E7D32;
      }
    }
  }
  
  .post-content {
    .post-title {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #1A1A1A;
      margin-bottom: 12rpx;
      line-height: 1.4;
    }
    
    .post-text {
      display: block;
      font-size: 28rpx;
      color: #666;
      line-height: 1.6;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }
    
    .post-images {
      display: flex;
      gap: 12rpx;
      margin-top: 16rpx;
      
      .post-image {
        width: 200rpx;
        height: 200rpx;
        border-radius: 12rpx;
      }
    }
  }
  
  .post-footer {
    display: flex;
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #F5F5F5;
    
    .stat-item {
      display: flex;
      align-items: center;
      margin-right: 40rpx;
      
      .stat-icon {
        font-size: 28rpx;
        margin-right: 8rpx;
      }
      
      .stat-value {
        font-size: 24rpx;
        color: #999;
      }
      
      &.liked {
        .stat-value {
          color: #F44336;
        }
      }
    }
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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
    margin-bottom: 32rpx;
  }
  
  .empty-btn {
    padding: 20rpx 48rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    border-radius: 40rpx;
    box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.35);
    
    .empty-btn-text {
      font-size: 28rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }
}

.bottom-space {
  height: 160rpx;
}

.publish-fab {
  position: fixed;
  right: 32rpx;
  bottom: 180rpx;
  width: 112rpx;
  height: 112rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.4);
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.95);
  }
  
  .fab-icon {
    font-size: 56rpx;
    font-weight: 300;
    color: #FFFFFF;
  }
}
</style>
