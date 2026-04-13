<template>
  <view class="care-add-page">
    <view class="form-section">
      <up-form :model="formData" ref="formRef">
        <up-form-item label="养护类型" prop="careType" required>
          <up-radio-group v-model="formData.careType">
            <view class="radio-grid">
              <up-radio label="浇水" name="WATER" />
              <up-radio label="施肥" name="FERTILIZE" />
              <up-radio label="修剪" name="PRUNE" />
              <up-radio label="病虫害防治" name="PEST_CONTROL" />
              <up-radio label="其他" name="OTHER" />
            </view>
          </up-radio-group>
        </up-form-item>
        
        <up-form-item label="养护描述" prop="description">
          <up-textarea
            v-model="formData.description"
            placeholder="记录这次养护的详情..."
            :maxlength="500"
            count
          />
        </up-form-item>
      </up-form>
    </view>
    
    <view class="image-section">
      <view class="section-title">添加图片</view>
      <view class="image-list">
        <view class="image-item" v-for="(img, index) in imageList" :key="index">
          <image class="preview-image" :src="img" mode="aspectFill" />
          <view class="delete-btn" @click="removeImage(index)">
            <up-icon name="close" size="12" color="#fff" />
          </view>
        </view>
        <view class="add-image" @click="chooseImage" v-if="imageList.length < 9">
          <up-icon name="plus" size="24" color="#999" />
          <text class="add-text">添加图片</text>
        </view>
      </view>
    </view>
    
    <view class="submit-btn">
      <up-button type="primary" text="提交记录" @click="handleSubmit" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { careApi, carePlanApi } from '../../api'
import { uploadFile } from '../../utils/request'

const userStore = useUserStore()

const plantId = ref(0)
const taskId = ref<number | undefined>()
const formData = reactive({
  careType: 'WATER',
  description: ''
})

const imageList = ref<string[]>([])
const formRef = ref()

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - imageList.value.length,
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

const handleSubmit = async () => {
  if (!formData.careType) {
    uni.showToast({ title: '请选择养护类型', icon: 'none' })
    return
  }
  
  try {
    const payload = {
      description: formData.description,
      images: imageList.value.length > 0 ? JSON.stringify(imageList.value) : undefined
    }

    if (taskId.value) {
      await carePlanApi.executeTask(taskId.value, payload)
    } else {
      await careApi.record({
        plantId: plantId.value,
        careType: formData.careType,
        description: formData.description,
        images: payload.images
      })
    }
    
    uni.showToast({ title: '记录成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error(e)
  }
}

onLoad((options) => {
  plantId.value = Number(options?.plantId)
  if (options?.taskId) {
    taskId.value = Number(options.taskId)
  }
  if (options?.taskType) {
    formData.careType = decodeURIComponent(options.taskType).toUpperCase()
  }
  if (options?.taskDetail && !formData.description) {
    formData.description = decodeURIComponent(options.taskDetail)
  }
})
</script>

<style lang="scss" scoped>
.care-add-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.form-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.radio-grid {
  display: flex;
  flex-wrap: wrap;
}

.image-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
  
  .preview-image {
    width: 100%;
    height: 100%;
    border-radius: 8rpx;
  }
  
  .delete-btn {
    position: absolute;
    top: -10rpx;
    right: -10rpx;
    width: 40rpx;
    height: 40rpx;
    background: #fa3534;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.add-image {
  width: 200rpx;
  height: 200rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .add-text {
    font-size: 24rpx;
    color: #999;
    margin-top: 8rpx;
  }
}

.submit-btn {
  margin-top: 40rpx;
}
</style>
