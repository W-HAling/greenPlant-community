<template>
  <view class="publish-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">发布帖子</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="publish-scroll">
      <view class="form-card">
        <view class="form-item">
          <input
            v-model="formData.title"
            class="title-input"
            placeholder="输入标题（必填）"
            placeholder-class="input-placeholder"
            maxlength="50"
          />
        </view>

        <view class="form-item type-selector">
          <scroll-view scroll-x class="type-scroll" show-scrollbar="false">
            <view class="type-list">
              <view
                class="type-item"
                :class="{ active: formData.postType === item.value }"
                v-for="item in postTypeOptions"
                :key="item.value"
                @click="formData.postType = item.value"
              >
                <text class="type-icon">{{ item.icon }}</text>
                <text class="type-name">{{ item.label }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view class="form-item content-area">
          <textarea
            v-model="formData.content"
            class="content-textarea"
            placeholder="分享你的绿植故事、养护经验或提问..."
            placeholder-class="textarea-placeholder"
            :maxlength="2000"
            auto-height
          />
          <view class="char-count">{{ formData.content.length }}/2000</view>
        </view>
      </view>

      <view class="form-card plant-ref-section" v-if="adoptedPlants.length > 0">
        <view class="section-header">
          <text class="section-title">🌱 关联绿植</text>
          <text class="section-desc">选择你正在养护的绿植</text>
        </view>
        <scroll-view scroll-x class="plant-scroll" show-scrollbar="false">
          <view class="plant-list">
            <view
              class="plant-chip"
              :class="{ selected: selectedPlantId === p.id }"
              v-for="p in adoptedPlants"
              :key="p.id"
              @click="togglePlantSelect(p.id)"
            >
              <text class="chip-name">{{ p.name }}</text>
            </view>
            <view
              class="plant-chip deselect"
              :class="{ selected: selectedPlantId === null }"
              @click="selectedPlantId = null"
            >
              <text class="chip-name">不关联</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="form-card image-section">
        <view class="section-header">
          <text class="section-title">📷 添加图片</text>
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
        <text class="submit-text">{{ submitting ? '发布中...' : '发布帖子' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { communityApi, adoptionApi } from '../../api'
import { uploadFile } from '../../utils/request'

const userStore = useUserStore()
const statusBarHeight = ref(0)
const submitting = ref(false)
const selectedPlantId = ref<number | null>(null)
const adoptedPlants = ref<any[]>([])

const formData = reactive({
  title: '',
  content: '',
  postType: 'SHARE'
})

const imageList = ref<string[]>([])

const postTypeOptions = [
  { label: '分享', value: 'SHARE', icon: '✨' },
  { label: '问答', value: 'QUESTION', icon: '❓' },
  { label: '经验', value: 'EXPERIENCE', icon: '💡' }
]

const loadAdoptedPlants = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const data = await adoptionApi.getMyRecords({ current: 1, size: 20, status: 'APPROVED' })
    adoptedPlants.value = data.records.filter((r: any) => r.status === 'APPROVED').map((r: any) => ({
      id: r.plantId,
      name: r.plantName
    }))
  } catch (e) {
    console.error(e)
  }
}

const togglePlantSelect = (id: number) => {
  selectedPlantId.value = selectedPlantId.value === id ? null : id
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
  if (!userStore.requireLogin()) return

  if (!formData.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (!formData.content.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return
  }

  submitting.value = true
  
  try {
    const postData: Record<string, any> = {
      title: formData.title.trim(),
      content: formData.content.trim(),
      postType: formData.postType
    }

    if (imageList.value.length > 0) {
      postData.images = JSON.stringify(imageList.value)
    }

    await communityApi.publishPost(postData)

    uni.showToast({ title: '发布成功', icon: 'success' })
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

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  loadAdoptedPlants()
})
</script>

<style lang="scss" scoped>
.publish-page {
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

.publish-scroll {
  flex: 1;
  height: calc(100vh - 240rpx);
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
    margin-top: 24rpx;
    padding-top: 24rpx;
    border-top: 1rpx solid #F5F5F5;
  }
}

.title-input {
  height: 80rpx;
  font-size: 34rpx;
  font-weight: 600;
  color: #1A1A1A;
}

.input-placeholder {
  color: #BBBBBB;
  font-size: 34rpx;
}

.type-selector {
  padding-top: 16rpx;
}

.type-scroll {
  white-space: nowrap;
}

.type-list {
  display: inline-flex;
  gap: 16rpx;
}

.type-item {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 28rpx;
  background: #F5F7F5;
  border-radius: 32rpx;
  transition: all 0.25s ease;
  border: 2rpx solid transparent;

  &.active {
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    border-color: transparent;
    box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);

    .type-name {
      color: #FFFFFF;
    }
  }

  .type-icon {
    font-size: 28rpx;
    margin-right: 8rpx;
  }

  .type-name {
    font-size: 26rpx;
    font-weight: 500;
    color: #666;
  }
}

.content-area {
  position: relative;

  .content-textarea {
    width: 100%;
    min-height: 300rpx;
    font-size: 28rpx;
    color: #333;
    line-height: 1.8;
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

.plant-ref-section {
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

  .plant-scroll {
    white-space: nowrap;
  }

  .plant-list {
    display: inline-flex;
    gap: 16rpx;
  }

  .plant-chip {
    display: inline-flex;
    align-items: center;
    padding: 14rpx 24rpx;
    background: #F5F7F5;
    border-radius: 28rpx;
    border: 2rpx solid transparent;
    transition: all 0.2s ease;

    &.selected {
      background: #E8F5E9;
      border-color: #4CAF50;

      .chip-name {
        color: #4CAF50;
        font-weight: 600;
      }
    }

    &.deselect.selected {
      background: #F5F5F5;
      border-color: #CCCCCC;

      .chip-name {
        color: #666;
      }
    }

    .chip-name {
      font-size: 24rpx;
      color: #666;
      white-space: nowrap;
    }
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
