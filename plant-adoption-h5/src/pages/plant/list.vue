<template>
  <view class="plant-list-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索绿植名称/位置"
          placeholder-class="search-placeholder"
          @confirm="handleSearch"
        />
        <view class="search-clear" v-if="keyword" @click="keyword = ''">
          <text class="clear-icon">×</text>
        </view>
      </view>
    </view>
    
    <view class="filter-section">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view class="filter-tabs">
          <view 
            class="tab-item" 
            :class="{ active: currentTab === index }"
            v-for="(tab, index) in statusTabs"
            :key="index"
            @click="handleTabChange(index)"
          >
            <text class="tab-text">{{ tab.name }}</text>
            <view class="tab-count" v-if="tab.count">{{ tab.count }}</view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <scroll-view
      scroll-y
      class="plant-scroll"
      @scrolltolower="loadMore"
    >
      <view class="plant-grid">
        <view
          class="plant-card"
          v-for="(plant, index) in plants"
          :key="plant.id"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="goDetail(plant.id)"
        >
          <view class="plant-image-wrapper">
            <image
              class="plant-image"
              :src="plant.imageUrl?.split(',')[0] || '/static/images/default-plant.png'"
              mode="aspectFill"
            />
            <view class="status-tag" :class="plant.status.toLowerCase()">
              {{ statusMap[plant.status] }}
            </view>
            <view class="image-overlay"></view>
          </view>
          <view class="plant-info">
            <text class="plant-name">{{ plant.name }}</text>
            <text class="plant-variety">{{ plant.variety || '绿植' }}</text>
            <view class="plant-meta">
              <text class="meta-icon">📍</text>
              <text class="meta-text">{{ plant.location }}</text>
            </view>
            <view class="plant-footer" v-if="plant.careCount > 0">
              <view class="care-badge">
                <text class="care-icon">💧</text>
                <text class="care-text">已养护{{ plant.careCount }}次</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="loadmore-section" v-if="plants.length > 0">
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
      
      <view class="empty-state" v-if="plants.length === 0 && loadStatus !== 'loading'">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">🌱</text>
        </view>
        <text class="empty-title">暂无绿植</text>
        <text class="empty-desc">当前筛选条件下没有找到绿植</text>
      </view>
      
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { plantApi } from '../../api'
import type { Plant } from '../../api'

const statusBarHeight = ref(0)
const keyword = ref('')
const currentTab = ref(0)
const plants = ref<Plant[]>([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const loadStatus = ref<'loadmore' | 'loading' | 'nomore'>('loadmore')

const statusTabs = reactive([
  { name: '全部', status: '', count: 0 },
  { name: '可领养', status: 'AVAILABLE', count: 0 },
  { name: '已领养', status: 'ADOPTED', count: 0 },
  { name: '维护中', status: 'MAINTENANCE', count: 0 }
])

const statusMap: Record<string, string> = {
  AVAILABLE: '可领养',
  ADOPTED: '已领养',
  MAINTENANCE: '维护中'
}

const loadPlants = async (refresh = false) => {
  if (refresh) {
    current.value = 1
    plants.value = []
    loadStatus.value = 'loadmore'
  }
  
  if (loadStatus.value === 'nomore') return
  
  loadStatus.value = 'loading'
  
  try {
    const status = statusTabs[currentTab.value].status
    const params: Record<string, any> = { current: current.value, size: size.value }
    if (status) params.status = status
    if (keyword.value) params.keyword = keyword.value
    const data = await plantApi.getList(params)
    
    if (refresh) {
      plants.value = data.records
    } else {
      plants.value.push(...data.records)
    }
    
    total.value = data.total
    
    if (plants.value.length >= total.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (e) {
    loadStatus.value = 'loadmore'
    console.error(e)
  }
}

const loadStats = async () => {
  try {
    const stats = await plantApi.getStats()
    statusTabs[0].count = stats.total
    statusTabs[1].count = stats.available
    statusTabs[2].count = stats.adopted
    statusTabs[3].count = stats.maintenance
  } catch (e) {
    console.error(e)
  }
}

const loadMore = () => {
  if (loadStatus.value !== 'loadmore') return
  current.value++
  loadPlants()
}

const handleSearch = () => {
  loadPlants(true)
}

const handleTabChange = (index: number) => {
  if (currentTab.value === index) return
  currentTab.value = index
  loadPlants(true)
}

const goDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/plant/detail?id=${id}` })
}

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  if (options?.status) {
    const index = statusTabs.findIndex(t => t.status === options.status)
    if (index > -1) {
      currentTab.value = index
    }
  }
})

onMounted(() => {
  loadPlants(true)
  loadStats()
})

onShow(() => {
  loadPlants(true)
  loadStats()
})
</script>

<style lang="scss" scoped>
.plant-list-page {
  min-height: 100vh;
  background: #F5F7F5;
  display: flex;
  flex-direction: column;
}

.status-bar {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
}

.search-section {
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
}

.search-bar {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 40rpx;
  padding: 0 24rpx;
  height: 80rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  
  .search-icon {
    font-size: 32rpx;
    margin-right: 12rpx;
  }
  
  .search-input {
    flex: 1;
    height: 80rpx;
    font-size: 28rpx;
    color: #1A1A1A;
  }
  
  .search-placeholder {
    color: #999;
  }
  
  .search-clear {
    width: 36rpx;
    height: 36rpx;
    background: #CCCCCC;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .clear-icon {
      font-size: 24rpx;
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
  padding: 16rpx 28rpx;
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
  
  .tab-count {
    margin-left: 8rpx;
    padding: 2rpx 10rpx;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 16rpx;
    font-size: 22rpx;
    color: #666;
  }
  
  &.active .tab-count {
    background: rgba(255, 255, 255, 0.3);
    color: #FFFFFF;
  }
}

.plant-scroll {
  flex: 1;
  height: calc(100vh - 200rpx);
}

.plant-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  gap: 20rpx;
}

.plant-card {
  width: calc(50% - 10rpx);
  background: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  animation: cardFadeIn 0.4s ease-out both;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  .plant-image-wrapper {
    position: relative;
    width: 100%;
    height: 280rpx;
    background: #F5F7F5;
    
    .plant-image {
      width: 100%;
      height: 100%;
    }
    
    .image-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 60rpx;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.1));
    }
  }
  
  .status-tag {
    position: absolute;
    top: 16rpx;
    left: 16rpx;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    font-size: 22rpx;
    font-weight: 500;
    backdrop-filter: blur(4px);
    
    &.available {
      background: rgba(76, 175, 80, 0.95);
      color: #FFFFFF;
    }
    
    &.adopted {
      background: rgba(255, 152, 0, 0.95);
      color: #FFFFFF;
    }
    
    &.maintenance {
      background: rgba(158, 158, 158, 0.95);
      color: #FFFFFF;
    }
  }
  
  .plant-info {
    padding: 20rpx;
    
    .plant-name {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: #1A1A1A;
      margin-bottom: 6rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .plant-variety {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-bottom: 10rpx;
    }
    
    .plant-meta {
      display: flex;
      align-items: center;
      
      .meta-icon {
        font-size: 22rpx;
        margin-right: 4rpx;
      }
      
      .meta-text {
        font-size: 22rpx;
        color: #666;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .plant-footer {
      margin-top: 12rpx;
      
      .care-badge {
        display: inline-flex;
        align-items: center;
        padding: 6rpx 12rpx;
        background: #E8F5E9;
        border-radius: 8rpx;
        
        .care-icon {
          font-size: 20rpx;
          margin-right: 4rpx;
        }
        
        .care-text {
          font-size: 20rpx;
          color: #4CAF50;
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
  }
}

.bottom-space {
  height: 120rpx;
}
</style>
