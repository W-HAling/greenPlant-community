<template>
  <view class="throw-page">
    <view class="form-card">
      <text class="form-title">扔出一个漂流瓶</text>
      <textarea
        v-model="content"
        class="content-input"
        maxlength="500"
        placeholder="写下今天的心情、养护小发现，或者一句想被陌生同事看到的话..."
      />

      <view class="image-list">
        <view class="image-item" v-for="(img, index) in imageList" :key="index">
          <image class="preview-image" :src="img" mode="aspectFill" />
          <view class="delete-btn" @click="removeImage(index)">×</view>
        </view>
        <view class="image-add" v-if="imageList.length < 3" @click="chooseImage">
          <text class="add-plus">+</text>
          <text class="add-text">添加图片</text>
        </view>
      </view>

      <view class="submit-btn" @click="submitBottle">发送漂流瓶</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { driftBottleApi } from '../../api'
import { uploadFile } from '../../utils/request'

const content = ref('')
const imageList = ref<string[]>([])

const chooseImage = () => {
  uni.chooseImage({
    count: 3 - imageList.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      for (const path of res.tempFilePaths) {
        try {
          const url = await uploadFile(path)
          imageList.value.push(url)
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const removeImage = (index: number) => {
  imageList.value.splice(index, 1)
}

const submitBottle = async () => {
  if (!content.value.trim()) {
    uni.showToast({ title: '请输入漂流瓶内容', icon: 'none' })
    return
  }
  try {
    await driftBottleApi.throwBottle({
      content: content.value,
      imageUrls: imageList.value.length ? imageList.value : undefined
    })
    uni.showToast({ title: '漂流瓶已出发', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/drift-bottle/my' })
    }, 1200)
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.throw-page {
  min-height: 100vh;
  background: #f6f8fb;
  padding: 28rpx;
}

.form-card {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 12rpx 30rpx rgba(25, 42, 31, 0.06);
}

.form-title {
  display: block;
  font-size: 38rpx;
  font-weight: 700;
  color: #223028;
}

.content-input {
  width: 100%;
  min-height: 260rpx;
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #f7f9fb;
  box-sizing: border-box;
  font-size: 28rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 24rpx;
}

.image-item,
.image-add {
  width: 200rpx;
  height: 200rpx;
  border-radius: 18rpx;
  overflow: hidden;
  position: relative;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  text-align: center;
  line-height: 40rpx;
}

.image-add {
  background: #f1f4f7;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #7d8c84;
}

.add-plus {
  font-size: 46rpx;
}

.add-text {
  margin-top: 8rpx;
  font-size: 24rpx;
}

.submit-btn {
  margin-top: 36rpx;
  padding: 24rpx 0;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #3d8bff 0%, #6b9dff 100%);
  color: #fff;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
}
</style>
