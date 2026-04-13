<template>
  <view class="community-detail-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">帖子详情</text>
      <view class="nav-placeholder"></view>
    </view>
    
    <scroll-view scroll-y class="detail-scroll">
      <view class="post-card">
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
        
        <view class="post-body">
          <text class="post-title">{{ post.title }}</text>
          <text class="post-text">{{ post.content }}</text>
          <view class="post-images" v-if="post.images">
            <image
              v-for="(img, index) in parseImages(post.images)"
              :key="index"
              class="post-image"
              :src="img"
              mode="aspectFill"
              @click="previewImage(index)"
            />
          </view>
        </view>
        
        <view class="post-footer">
          <view class="stat-item" @click="handleLike">
            <text class="stat-icon">{{ post.isLiked ? '❤️' : '🤍' }}</text>
            <text class="stat-value" :class="{ liked: post.isLiked }">{{ post.likeCount || 0 }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-icon">💬</text>
            <text class="stat-value">{{ post.commentCount || 0 }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-icon">👁</text>
            <text class="stat-value">{{ post.viewCount || 0 }}</text>
          </view>
        </view>
      </view>
      
      <view class="comment-section">
        <view class="section-header">
          <text class="section-icon">💬</text>
          <text class="section-title">评论</text>
          <text class="section-count">{{ post.commentCount || 0 }}</text>
        </view>
        
        <view class="comment-list" v-if="comments.length > 0">
          <view class="comment-item" v-for="comment in comments" :key="comment.id">
            <view class="comment-avatar">
              <text class="avatar-text">{{ comment.userName?.charAt(0) }}</text>
            </view>
            <view class="comment-content">
              <view class="comment-header">
                <text class="comment-name">{{ comment.userName }}</text>
                <text class="comment-time">{{ formatTime(comment.createTime) }}</text>
              </view>
              <text class="comment-text">{{ comment.content }}</text>
            </view>
          </view>
        </view>
        
        <view class="empty-comments" v-else>
          <text class="empty-icon">💭</text>
          <text class="empty-text">暂无评论，快来抢沙发吧</text>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
    
    <view class="comment-input-bar">
      <view class="input-wrapper">
        <input
          v-model="commentText"
          class="comment-input"
          placeholder="说点什么..."
          placeholder-class="input-placeholder"
        />
      </view>
      <view class="send-btn" :class="{ active: commentText.trim() }" @click="submitComment">
        <text class="send-text">发送</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { communityApi } from '../../api'

const userStore = useUserStore()

const postId = ref(0)
const post = ref<any>({})
const comments = ref<any[]>([])
const commentText = ref('')

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
  
  return `${date.getMonth() + 1}-${date.getDate()}`
}

const parseImages = (images: string) => {
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const loadPost = async () => {
  try {
    post.value = await communityApi.getPostDetail(postId.value)
  } catch (e) {
    console.error(e)
  }
}

const loadComments = async () => {
  try {
    const data = await communityApi.getComments({ postId: postId.value, current: 1, size: 100 })
    comments.value = data.records
  } catch (e) {
    console.error(e)
  }
}

const handleLike = async () => {
  if (!userStore.requireLogin()) return
  
  try {
    const isLiked = await communityApi.toggleLike(postId.value)
    post.value.isLiked = isLiked
    post.value.likeCount += isLiked ? 1 : -1
  } catch (e) {
    console.error(e)
  }
}

const previewImage = (index: number) => {
  const images = parseImages(post.value.images)
  uni.previewImage({
    urls: images,
    current: index
  })
}

const submitComment = async () => {
  if (!userStore.requireLogin()) return
  if (!commentText.value.trim()) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  
  try {
    await communityApi.addComment({
      postId: postId.value,
      content: commentText.value
    })
    commentText.value = ''
    uni.showToast({ title: '评论成功', icon: 'success' })
    loadComments()
    post.value.commentCount++
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  uni.navigateBack()
}

onLoad((options) => {
  postId.value = Number(options?.id)
})

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style lang="scss" scoped>
.community-detail-page {
  min-height: 100vh;
  background: #F5F7F5;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  padding-top: calc(24rpx + env(safe-area-inset-top));
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E5E5;
  
  .back-btn {
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #F5F7F5;
    border-radius: 50%;
    
    .back-icon {
      font-size: 36rpx;
      color: #333;
    }
  }
  
  .nav-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #1A1A1A;
  }
  
  .nav-placeholder {
    width: 72rpx;
  }
}

.detail-scroll {
  flex: 1;
  height: calc(100vh - 180rpx);
}

.post-card {
  background: #FFFFFF;
  margin: 24rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  
  .post-header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    
    .user-avatar {
      width: 80rpx;
      height: 80rpx;
      background: linear-gradient(135deg, #4CAF50 0%, #81C784 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16rpx;
      
      .avatar-text {
        font-size: 32rpx;
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
  
  .post-body {
    .post-title {
      display: block;
      font-size: 36rpx;
      font-weight: 700;
      color: #1A1A1A;
      margin-bottom: 16rpx;
      line-height: 1.4;
    }
    
    .post-text {
      display: block;
      font-size: 30rpx;
      color: #333;
      line-height: 1.8;
      margin-bottom: 20rpx;
    }
    
    .post-images {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;
      
      .post-image {
        width: 200rpx;
        height: 200rpx;
        border-radius: 12rpx;
      }
    }
  }
  
  .post-footer {
    display: flex;
    margin-top: 24rpx;
    padding-top: 24rpx;
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
        font-size: 26rpx;
        color: #999;
        
        &.liked {
          color: #F44336;
        }
      }
    }
  }
}

.comment-section {
  background: #FFFFFF;
  margin: 0 24rpx 24rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  
  .section-icon {
    font-size: 28rpx;
    margin-right: 12rpx;
  }
  
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1A1A1A;
  }
  
  .section-count {
    margin-left: 8rpx;
    padding: 2rpx 12rpx;
    background: #E8F5E9;
    border-radius: 12rpx;
    font-size: 22rpx;
    color: #4CAF50;
  }
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.comment-item {
  display: flex;
  
  .comment-avatar {
    width: 64rpx;
    height: 64rpx;
    background: linear-gradient(135deg, #81C784 0%, #A5D6A7 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
    flex-shrink: 0;
    
    .avatar-text {
      font-size: 26rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }
  
  .comment-content {
    flex: 1;
    background: #F5F7F5;
    border-radius: 16rpx;
    padding: 16rpx 20rpx;
    
    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8rpx;
      
      .comment-name {
        font-size: 26rpx;
        font-weight: 600;
        color: #1A1A1A;
      }
      
      .comment-time {
        font-size: 22rpx;
        color: #999;
      }
    }
    
    .comment-text {
      font-size: 28rpx;
      color: #333;
      line-height: 1.5;
    }
  }
}

.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx;
  
  .empty-icon {
    font-size: 64rpx;
    margin-bottom: 16rpx;
    opacity: 0.5;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.bottom-space {
  height: 40rpx;
}

.comment-input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 1rpx solid #E5E5E5;
  gap: 16rpx;
}

.input-wrapper {
  flex: 1;
  background: #F5F7F5;
  border-radius: 40rpx;
  padding: 0 24rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
}

.comment-input {
  width: 100%;
  height: 80rpx;
  font-size: 28rpx;
  color: #1A1A1A;
}

.input-placeholder {
  color: #999;
}

.send-btn {
  width: 120rpx;
  height: 80rpx;
  background: #E5E5E5;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  
  &.active {
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  }
  
  .send-text {
    font-size: 28rpx;
    font-weight: 600;
    color: #999;
  }
  
  &.active .send-text {
    color: #FFFFFF;
  }
}
</style>
