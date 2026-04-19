<template>
  <view class="care-add-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">{{ taskId ? '完成养护任务' : '添加养护记录' }}</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="add-scroll">
      <!-- 如果是从任务跳转过来，顶部显示任务指导卡片 -->
      <view class="task-info-card" v-if="taskId">
        <view class="task-info-header">
          <text class="task-plant-name">🌱 {{ plantName || '绿植' }}</text>
          <view class="task-type-badge">{{ getCareTypeLabel(formData.careType) }}</view>
        </view>
        <view class="task-info-body">
          <text class="task-label">养护指导：</text>
          <text class="task-detail">{{ taskDetail || '请按照计划完成养护' }}</text>
        </view>
      </view>

      <view class="form-card">
        <!-- 如果是主动添加记录，允许选择养护类型 -->
        <view class="form-item" v-if="!taskId">
          <view class="item-label">选择养护类型</view>
          <view class="type-selector">
            <view
              class="type-chip"
              :class="{ active: formData.careType === item.value }"
              v-for="item in careTypeOptions"
              :key="item.value"
              @click="formData.careType = item.value"
            >
              <text class="type-icon">{{ item.icon }}</text>
              <text class="type-text">{{ item.label }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <view class="item-label">执行情况</view>
          <view class="content-area">
            <textarea
              v-model="formData.description"
              class="content-textarea"
              placeholder="记录一下本次养护的具体情况，如浇水量、状态等..."
              placeholder-class="textarea-placeholder"
              :maxlength="500"
              auto-height
            />
            <view class="char-count">{{ formData.description.length }}/500</view>
          </view>
        </view>
      </view>
      
      <view class="form-card image-section">
        <view class="section-header">
          <text class="section-title">📷 现场图片</text>
          <text class="section-desc">{{ imageList.length }}/9</text>
        </view>
        <view class="image-grid">
          <view class="image-item" v-for="(img, index) in imageList" :key="index">
            <image class="preview-image" :src="img" mode="aspectFill" />
            <view class="delete-btn" @click="removeImage(index)">
              <text class="delete-icon">×</text>
            </view>
          </view>
          <view class="add-image-btn" @click="chooseImage" v-if="imageList.length < 9">
            <text class="add-icon">+</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>

    <view class="submit-bar">
      <view class="submit-btn" :class="{ disabled: submitting }" @click="handleSubmit">
        <text class="submit-text">{{ submitting ? '提交中...' : '确认提交' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { careApi, carePlanApi } from '../../api'
import { uploadFile } from '../../utils/request'

const userStore = useUserStore()
const statusBarHeight = ref(0)
const submitting = ref(false)

const plantId = ref(0)
const plantName = ref('')
const taskId = ref<number | undefined>()
const taskDetail = ref('')

const formData = reactive({
  careType: 'WATER',
  description: ''
})

const imageList = ref<string[]>([])

const careTypeOptions = [
  { label: '浇水', value: 'WATER', icon: '💧' },
  { label: '施肥', value: 'FERTILIZE', icon: '✨' },
  { label: '修剪', value: 'PRUNE', icon: '✂️' },
  { label: '病虫害', value: 'PEST_CONTROL', icon: '🐛' },
  { label: '其他', value: 'OTHER', icon: '📋' }
]

const getCareTypeLabel = (type: string) => {
  const option = careTypeOptions.find(o => o.value === type)
  return option ? `${option.icon} ${option.label}` : type
}

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
  if (!formData.description.trim() && imageList.value.length === 0) {
    uni.showToast({ title: '描述或图片至少填一项', icon: 'none' })
    return
  }
  
  submitting.value = true
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
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

onLoad((options) => {
  plantId.value = Number(options?.plantId)
  if (options?.plantName) {
    plantName.value = decodeURIComponent(options.plantName)
  }
  if (options?.taskId) {
    taskId.value = Number(options.taskId)
  }
  if (options?.taskType) {
    formData.careType = decodeURIComponent(options.taskType).toUpperCase()
  }
  if (options?.taskDetail) {
    taskDetail.value = decodeURIComponent(options.taskDetail)
  }
})

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
})
</script>

<style lang="scss" scoped>
.care-add-page {
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

  .nav-placeholder {
    width: 64rpx;
  }
}

.add-scroll {
  flex: 1;
  height: calc(100vh - 240rpx);
}

.task-info-card {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  margin: 24rpx;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(76, 175, 80, 0.1);

  .task-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    padding-bottom: 20rpx;
    border-bottom: 1rpx dashed rgba(76, 175, 80, 0.3);

    .task-plant-name {
      font-size: 32rpx;
      font-weight: 700;
      color: #2E7D32;
    }

    .task-type-badge {
      background: #FFFFFF;
      color: #388E3C;
      font-size: 24rpx;
      font-weight: 600;
      padding: 6rpx 20rpx;
      border-radius: 30rpx;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
    }
  }

  .task-info-body {
    display: flex;
    flex-direction: column;

    .task-label {
      font-size: 24rpx;
      color: #4CAF50;
      margin-bottom: 8rpx;
    }

    .task-detail {
      font-size: 28rpx;
      color: #1B5E20;
      line-height: 1.6;
    }
  }
}

.form-card {
  background: #FFFFFF;
  margin: 20rpx 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  &:first-child {
    margin-top: 20rpx;
  }
}

.form-item {
  & + .form-item {
    margin-top: 32rpx;
    padding-top: 32rpx;
    border-top: 1rpx solid #F5F5F5;
  }

  .item-label {
    font-size: 30rpx;
    font-weight: 600;
    color: #1A1A1A;
    margin-bottom: 20rpx;
  }
}

.type-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .type-chip {
    display: flex;
    align-items: center;
    padding: 14rpx 28rpx;
    background: #F5F7F5;
    border-radius: 32rpx;
    border: 2rpx solid transparent;
    transition: all 0.2s ease;

    &.active {
      background: #E8F5E9;
      border-color: #4CAF50;

      .type-text {
        color: #4CAF50;
        font-weight: 600;
      }
    }

    .type-icon {
      font-size: 28rpx;
      margin-right: 8rpx;
    }

    .type-text {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.content-area {
  position: relative;
  background: #F9FAF9;
  border-radius: 16rpx;
  padding: 20rpx;
  border: 1rpx solid #EEEEEE;

  .content-textarea {
    width: 100%;
    min-height: 200rpx;
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
  }

  .textarea-placeholder {
    color: #BBBBBB;
  }

  .char-count {
    text-align: right;
    font-size: 22rpx;
    color: #CCCCCC;
    margin-top: 12rpx;
  }
}

.image-section {
  .section-header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #1A1A1A;
    }

    .section-desc {
      font-size: 22rpx;
      color: #999;
    }
  }

  .image-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }

  .image-item {
    position: relative;
    width: 200rpx;
    height: 200rpx;

    .preview-image {
      width: 100%;
      height: 100%;
      border-radius: 12rpx;
    }

    .delete-btn {
      position: absolute;
      top: -12rpx;
      right: -12rpx;
      width: 44rpx;
      height: 44rpx;
      background: #F44336;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4rpx 8rpx rgba(244, 67, 54, 0.3);

      .delete-icon {
        color: #FFFFFF;
        font-size: 28rpx;
        line-height: 1;
      }
    }
  }

  .add-image-btn {
    width: 200rpx;
    height: 200rpx;
    background: #FAFAFA;
    border: 2rpx dashed #DDDDDD;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;

    &:active {
      background: #F0F0F0;
      border-color: #4CAF50;
    }

    .add-icon {
      font-size: 48rpx;
      color: #CCCCCC;
      line-height: 1;
    }

    .add-text {
      font-size: 22rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
}

.bottom-space {
  height: 140rpx;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  z-index: 100;

  .submit-btn {
    height: 96rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.35);
    transition: opacity 0.2s ease;

    &.disabled {
      opacity: 0.6;
      pointer-events: none;
    }

    &:active {
      transform: scale(0.98);
    }

    .submit-text {
      font-size: 32rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }
}
</style>
