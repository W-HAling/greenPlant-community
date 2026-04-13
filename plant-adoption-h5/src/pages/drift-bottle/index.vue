<template>
  <view class="bottle-page">
    <view class="hero-card">
      <text class="hero-title">漂流瓶</text>
      <text class="hero-desc">扔出一段心情，或者捡起别人留下的小情绪。</text>

      <view class="hero-actions">
        <view class="hero-btn primary" @click="goThrow">
          <text>扔瓶子</text>
        </view>
        <view class="hero-btn secondary" @click="pickBottle">
          <text>捡瓶子</text>
        </view>
      </view>

      <view class="hero-link" @click="goMyBottles">查看我的漂流瓶</view>
    </view>

    <view class="picked-card" v-if="pickedBottle">
      <view class="picked-header">
        <text class="picked-title">捡到一个漂流瓶</text>
        <text class="picked-time">{{ formatTime(pickedBottle.createTime) }}</text>
      </view>

      <text class="picked-content">{{ pickedBottle.content }}</text>

      <view class="picked-images" v-if="parseImages(pickedBottle.imageUrls).length">
        <image
          v-for="(img, index) in parseImages(pickedBottle.imageUrls)"
          :key="index"
          class="picked-image"
          :src="img"
          mode="aspectFill"
        />
      </view>

      <textarea
        v-model="replyContent"
        class="reply-input"
        maxlength="300"
        placeholder="写下你的回复..."
      />

      <view class="picked-actions">
        <view class="action ghost" @click="releaseBottle">先放回去</view>
        <view class="action solid" @click="replyBottle">发送回复</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { driftBottleApi } from '../../api'
import type { DriftBottle } from '../../api'

const pickedBottle = ref<DriftBottle | null>(null)
const replyContent = ref('')

const parseImages = (images?: string) => {
  if (!images) return []
  if (images.startsWith('[')) {
    try {
      return JSON.parse(images)
    } catch {
      return []
    }
  }
  return images.split(',').filter(Boolean)
}

const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const goThrow = () => {
  uni.navigateTo({ url: '/pages/drift-bottle/throw' })
}

const goMyBottles = () => {
  uni.navigateTo({ url: '/pages/drift-bottle/my' })
}

const pickBottle = async () => {
  try {
    pickedBottle.value = await driftBottleApi.pickBottle()
    replyContent.value = ''
  } catch (e) {
    console.error(e)
  }
}

const releaseBottle = async () => {
  if (!pickedBottle.value) return
  try {
    await driftBottleApi.releaseBottle(pickedBottle.value.id)
    pickedBottle.value = null
    replyContent.value = ''
    uni.showToast({ title: '已放回漂流瓶', icon: 'success' })
  } catch (e) {
    console.error(e)
  }
}

const replyBottle = async () => {
  if (!pickedBottle.value) return
  if (!replyContent.value.trim()) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }
  try {
    await driftBottleApi.replyBottle(pickedBottle.value.id, {
      replyContent: replyContent.value
    })
    uni.showToast({ title: '回复成功', icon: 'success' })
    pickedBottle.value = null
    replyContent.value = ''
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.bottle-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f8ff 0%, #eef7f0 100%);
  padding: 28rpx;
}

.hero-card,
.picked-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 16rpx 36rpx rgba(43, 73, 57, 0.08);
}

.hero-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #223028;
}

.hero-desc {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #5f6f66;
}

.hero-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 28rpx;
}

.hero-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 999rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
}

.hero-btn.primary {
  background: #3d8bff;
  color: #fff;
}

.hero-btn.secondary {
  background: #e7f0ff;
  color: #2f65c8;
}

.hero-link {
  margin-top: 24rpx;
  text-align: center;
  color: #4f8a60;
  font-size: 26rpx;
}

.picked-card {
  margin-top: 24rpx;
}

.picked-header {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.picked-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #26342b;
}

.picked-time {
  font-size: 22rpx;
  color: #8a968e;
}

.picked-content {
  display: block;
  margin-top: 20rpx;
  font-size: 28rpx;
  line-height: 1.8;
  color: #405046;
}

.picked-images {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;
  flex-wrap: wrap;
}

.picked-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
}

.reply-input {
  width: 100%;
  min-height: 180rpx;
  margin-top: 24rpx;
  padding: 22rpx;
  border-radius: 20rpx;
  background: #f5f8f6;
  font-size: 28rpx;
  box-sizing: border-box;
}

.picked-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.action {
  flex: 1;
  text-align: center;
  padding: 22rpx 0;
  border-radius: 999rpx;
  font-size: 26rpx;
}

.action.ghost {
  background: #edf1ee;
  color: #5c6c62;
}

.action.solid {
  background: #4f8a60;
  color: #fff;
}
</style>
