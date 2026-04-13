<template>
  <view class="plant-detail-page">
    <view class="image-section">
      <image
        class="plant-image"
        :src="plant.imageUrl || '/static/images/default-plant.png'"
        mode="aspectFill"
      />
      <view class="image-overlay"></view>
      
      <view class="nav-bar">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">←</text>
        </view>
        <view class="nav-placeholder"></view>
      </view>
      
      <view class="status-badge" :class="plant.status?.toLowerCase()">
        <text class="status-icon">{{ statusIcon[plant.status] }}</text>
        <text class="status-text">{{ statusMap[plant.status] }}</text>
      </view>
    </view>
    
    <scroll-view scroll-y class="content-scroll" :style="{ bottom: showBottomBar ? '140rpx' : '0' }">
    <view class="content-section">
      <view class="plant-header">
        <text class="plant-name">{{ plant.name }}</text>
        <text class="plant-variety">{{ plant.variety || '绿植' }}</text>
      </view>
      
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-icon">📍</text>
          <text class="stat-value">{{ plant.location || '-' }}</text>
          <text class="stat-label">位置</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-icon">💧</text>
          <text class="stat-value">{{ plant.careCount || 0 }}</text>
          <text class="stat-label">养护次数</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-icon">📅</text>
          <text class="stat-value">{{ formatTime(plant.lastCareTime) || '-' }}</text>
          <text class="stat-label">最后养护</text>
        </view>
      </view>
      
      <view class="info-section" v-if="plant.description">
        <view class="section-header">
          <text class="section-icon">🌿</text>
          <text class="section-title">简介</text>
        </view>
        <text class="section-content">{{ plant.description }}</text>
      </view>
      
      <view class="info-section" v-if="plant.careTips">
        <view class="section-header">
          <text class="section-icon">💡</text>
          <text class="section-title">养护提示</text>
        </view>
        <text class="section-content">{{ plant.careTips }}</text>
      </view>
      
      <view class="info-section" v-if="plant.status === 'ADOPTED' && plant.adopterName">
        <view class="section-header">
          <text class="section-icon">👤</text>
          <text class="section-title">领养人</text>
        </view>
        <view class="adopter-card">
          <view class="adopter-avatar">
            <text class="avatar-text">{{ plant.adopterName?.charAt(0) }}</text>
          </view>
          <view class="adopter-info">
            <text class="adopter-name">{{ plant.adopterName }}</text>
            <text class="adopter-time">领养于 {{ formatTime(plant.adoptionTime) }}</text>
          </view>
        </view>
      </view>
      
      <view class="info-section">
        <view class="section-header">
          <text class="section-icon">📝</text>
          <text class="section-title">养护记录</text>
          <text class="section-more" @click="goCareList">查看全部 ›</text>
        </view>
        
        <view class="care-list" v-if="careLogs.length > 0">
          <view 
            class="care-item" 
            v-for="log in careLogs" 
            :key="log.id"
          >
            <view class="care-icon" :class="log.careType.toLowerCase()">
              <text class="icon-text">{{ careEmojiMap[log.careType] }}</text>
            </view>
            <view class="care-content">
              <view class="care-header">
                <text class="care-type">{{ careTypeMap[log.careType] }}</text>
                <text class="care-time">{{ formatTime(log.careTime) }}</text>
              </view>
              <text class="care-desc" v-if="log.description">{{ log.description }}</text>
            </view>
          </view>
        </view>
        
        <view class="empty-care" v-else>
          <text class="empty-icon">💧</text>
          <text class="empty-text">暂无养护记录</text>
        </view>
      </view>
      
      <view class="bottom-space"></view>
    </view>
    </scroll-view>
    
    <view class="bottom-bar" v-if="plant.status === 'AVAILABLE' || isMyPlant">
      <view 
        class="action-btn secondary" 
        v-if="plant.status === 'ADOPTED' && isMyPlant"
        @click="handleReturn"
      >
        <text class="btn-icon">↩️</text>
        <text class="btn-text">归还</text>
      </view>
      <view 
        class="action-btn primary" 
        v-if="plant.status === 'AVAILABLE' && canAdopt"
        @click="handleAdopt"
      >
        <text class="btn-icon">🌱</text>
        <text class="btn-text">申请领养</text>
      </view>
      <view 
        class="action-btn primary" 
        v-if="plant.status === 'ADOPTED' && isMyPlant"
        @click="goAddCare"
      >
        <text class="btn-icon">💧</text>
        <text class="btn-text">记录养护</text>
      </view>
      <view 
        class="action-btn disabled" 
        v-if="!canAdopt && plant.status === 'AVAILABLE'"
      >
        <text class="btn-text">领养数量已达上限</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { plantApi, careApi, adoptionApi } from '../../api'
import type { Plant } from '../../api'

const userStore = useUserStore()

const plantId = ref(0)
const plant = ref<Plant>({} as Plant)
const careLogs = ref<any[]>([])
const canAdopt = ref(true)

const statusMap: Record<string, string> = {
  AVAILABLE: '可领养',
  ADOPTED: '已领养',
  MAINTENANCE: '维护中'
}

const statusIcon: Record<string, string> = {
  AVAILABLE: '🌱',
  ADOPTED: '💚',
  MAINTENANCE: '🔧'
}

const careTypeMap: Record<string, string> = {
  WATER: '浇水',
  FERTILIZE: '施肥',
  PRUNE: '修剪',
  PEST_CONTROL: '病虫害防治',
  OTHER: '其他'
}

const careEmojiMap: Record<string, string> = {
  WATER: '💧',
  FERTILIZE: '🌱',
  PRUNE: '✂️',
  PEST_CONTROL: '🐛',
  OTHER: '📝'
}

const isMyPlant = computed(() => {
  return plant.value.adopterId === userStore.userInfo?.id
})

const showBottomBar = computed(() => {
  return plant.value.status === 'AVAILABLE' || isMyPlant.value
})

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const loadPlant = async () => {
  try {
    plant.value = await plantApi.getDetail(plantId.value)
  } catch (e) {
    console.error(e)
  }
}

const loadCareLogs = async () => {
  try {
    const data = await careApi.getList({ plantId: plantId.value, current: 1, size: 5 })
    careLogs.value = data.records
  } catch (e) {
    console.error(e)
  }
}

const checkCanAdopt = async () => {
  if (!userStore.isLoggedIn) {
    canAdopt.value = false
    return
  }
  try {
    canAdopt.value = await adoptionApi.canAdopt()
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  uni.navigateBack()
}

const handleAdopt = () => {
  if (!userStore.requireLogin()) return
  
  uni.showModal({
    title: '确认领养',
    content: `确定要领养【${plant.value.name}】吗？`,
    confirmColor: '#4CAF50',
    success: async (res) => {
      if (res.confirm) {
        try {
          await adoptionApi.apply(plantId.value)
          uni.showToast({ title: '申请成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const handleReturn = () => {
  uni.showModal({
    title: '确认归还',
    content: '确定要归还这株绿植吗？',
    confirmColor: '#FF9800',
    success: async (res) => {
      if (res.confirm) {
        try {
          await adoptionApi.returnPlant(plantId.value)
          uni.showToast({ title: '归还成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const goCareList = () => {
  uni.navigateTo({ url: `/pages/care/list?plantId=${plantId.value}` })
}

const goAddCare = () => {
  uni.navigateTo({ url: `/pages/care/add?plantId=${plantId.value}` })
}

onLoad((options) => {
  plantId.value = Number(options?.id)
})

onMounted(() => {
  loadPlant()
  loadCareLogs()
  checkCanAdopt()
})
</script>

<style lang="scss" scoped>
.plant-detail-page {
  min-height: 100vh;
  background: #F5F7F5;
  display: flex;
  flex-direction: column;
}

.image-section {
  position: relative;
  width: 100%;
  height: 500rpx;
  
  .plant-image {
    width: 100%;
    height: 100%;
  }
  
  .image-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 120rpx;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.3));
  }
  
  .nav-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    padding: 24rpx 32rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
  }
  
  .back-btn {
    width: 72rpx;
    height: 72rpx;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
    
    .back-icon {
      color: #333;
      font-size: 36rpx;
    }
  }
  
  .nav-placeholder {
    width: 72rpx;
  }
  
  .status-badge {
    position: absolute;
    bottom: 24rpx;
    right: 32rpx;
    display: flex;
    align-items: center;
    padding: 12rpx 24rpx;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
    
    .status-icon {
      font-size: 28rpx;
      margin-right: 8rpx;
    }
    
    .status-text {
      font-size: 26rpx;
      font-weight: 600;
    }
    
    &.available {
      .status-text { color: #4CAF50; }
    }
    
    &.adopted {
      .status-text { color: #FF9800; }
    }
    
    &.maintenance {
      .status-text { color: #9E9E9E; }
    }
  }
}

.content-scroll {
  flex: 1;
  height: calc(100vh - 500rpx);
}

.content-section {
  margin-top: -40rpx;
  position: relative;
  //z-index: 10;
}

.plant-header {
  background: #FFFFFF;
  padding: 32rpx;
  border-radius: 40rpx 40rpx 0 0;
  
  .plant-name {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 8rpx;
  }
  
  .plant-variety {
    font-size: 28rpx;
    color: #999;
  }
}

.stats-card {
  display: flex;
  background: #FFFFFF;
  padding: 32rpx 16rpx;
  margin: 0 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  margin-top: -20rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
  
  .stat-icon {
    display: block;
    font-size: 32rpx;
    margin-bottom: 8rpx;
  }
  
  .stat-value {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #1A1A1A;
    margin-bottom: 4rpx;
  }
  
  .stat-label {
    font-size: 22rpx;
    color: #999;
  }
}

.stat-divider {
  width: 1rpx;
  background: #E5E5E5;
  margin: 16rpx 0;
}

.info-section {
  background: #FFFFFF;
  margin: 24rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  
  .section-icon {
    font-size: 28rpx;
    margin-right: 12rpx;
  }
  
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1A1A1A;
    flex: 1;
  }
  
  .section-more {
    font-size: 26rpx;
    color: #4CAF50;
  }
}

.section-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

.adopter-card {
  display: flex;
  align-items: center;
  background: #F5F7F5;
  border-radius: 16rpx;
  padding: 20rpx;
  
  .adopter-avatar {
    width: 72rpx;
    height: 72rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #81C784 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    
    .avatar-text {
      font-size: 28rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }
  
  .adopter-info {
    .adopter-name {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #1A1A1A;
    }
    
    .adopter-time {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }
}

.care-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.care-item {
  display: flex;
  align-items: flex-start;
  background: #F5F7F5;
  border-radius: 16rpx;
  padding: 20rpx;
  
  .care-icon {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14rpx;
    margin-right: 16rpx;
    
    &.water { background: #E3F2FD; }
    &.fertilize { background: #E8F5E9; }
    &.prune { background: #FFF8E1; }
    &.pest_control { background: #FFEBEE; }
    &.other { background: #F5F5F5; }
    
    .icon-text {
      font-size: 28rpx;
    }
  }
  
  .care-content {
    flex: 1;
    
    .care-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6rpx;
      
      .care-type {
        font-size: 28rpx;
        font-weight: 600;
        color: #1A1A1A;
      }
      
      .care-time {
        font-size: 22rpx;
        color: #999;
      }
    }
    
    .care-desc {
      font-size: 24rpx;
      color: #666;
      line-height: 1.5;
    }
  }
}

.empty-care {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx;
  background: #F5F7F5;
  border-radius: 16rpx;
  
  .empty-icon {
    font-size: 48rpx;
    margin-bottom: 12rpx;
    opacity: 0.5;
  }
  
  .empty-text {
    font-size: 26rpx;
    color: #999;
  }
}

.bottom-space {
  height: 40rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.action-btn {
  flex: 1;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 48rpx;
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  .btn-icon {
    font-size: 28rpx;
    margin-right: 8rpx;
  }
  
  .btn-text {
    font-size: 30rpx;
    font-weight: 600;
  }
  
  &.primary {
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.35);
    
    .btn-text {
      color: #FFFFFF;
    }
  }
  
  &.secondary {
    background: #F5F7F5;
    border: 2rpx solid #E5E5E5;
    
    .btn-text {
      color: #666;
    }
  }
  
  &.disabled {
    background: #E5E5E5;
    pointer-events: none;
    
    .btn-text {
      color: #999;
    }
  }
}
</style>
