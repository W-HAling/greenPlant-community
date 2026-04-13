<template>
  <view class="care-list-page">
    <scroll-view scroll-y class="care-scroll" @scrolltolower="loadMore">
      <view class="care-list">
        <view
          class="care-card"
          v-for="log in careLogs"
          :key="log.id"
          @click="goPlantDetail(log.plantId)"
        >
          <view class="care-header">
            <view class="care-icon">
              <up-icon :name="careIconMap[log.careType]" size="20" color="#5677FC" />
            </view>
            <view class="care-info">
              <text class="care-type">{{ careTypeMap[log.careType] }}</text>
              <text class="care-plant">{{ log.plantName }}</text>
            </view>
            <text class="care-time">{{ formatTime(log.careTime) }}</text>
          </view>
          
          <view class="care-content" v-if="log.description">
            <text>{{ log.description }}</text>
          </view>
          
          <view class="care-images" v-if="log.images">
            <image
              v-for="(img, index) in parseImages(log.images).slice(0, 3)"
              :key="index"
              class="care-image"
              :src="img"
              mode="aspectFill"
            />
          </view>
        </view>
      </view>
      
      <up-empty v-if="careLogs.length === 0" text="暂无养护记录" mode="list" />
      <up-loadmore v-else :status="loadStatus" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { careApi } from '../../api'

const plantId = ref<number | undefined>()
const careLogs = ref<any[]>([])
const current = ref(1)
const loadStatus = ref<'loadmore' | 'loading' | 'nomore'>('loadmore')

const careTypeMap: Record<string, string> = {
  WATER: '浇水',
  FERTILIZE: '施肥',
  PRUNE: '修剪',
  PEST_CONTROL: '病虫害防治',
  OTHER: '其他'
}

const careIconMap: Record<string, string> = {
  WATER: 'arrow-down',
  FERTILIZE: 'gift',
  PRUNE: 'cut',
  PEST_CONTROL: 'medal',
  OTHER: 'more-dot-fill'
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const parseImages = (images: string) => {
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const loadCareLogs = async (refresh = false) => {
  if (refresh) {
    current.value = 1
    careLogs.value = []
    loadStatus.value = 'loadmore'
  }
  
  if (loadStatus.value === 'nomore') return
  
  loadStatus.value = 'loading'
  
  try {
    const params: Record<string, any> = { current: current.value, size: 10 }
    if (plantId.value) params.plantId = plantId.value
    const data = await careApi.getMyLogs(params)
    
    if (refresh) {
      careLogs.value = data.records
    } else {
      careLogs.value.push(...data.records)
    }
    
    if (careLogs.value.length >= data.total) {
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
  loadCareLogs()
}

const goPlantDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/plant/detail?id=${id}` })
}

onLoad((options) => {
  if (options?.plantId) {
    plantId.value = Number(options.plantId)
  }
})

onMounted(() => {
  loadCareLogs(true)
})

onShow(() => {
  loadCareLogs(true)
})
</script>

<style lang="scss" scoped>
.care-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.care-scroll {
  height: 100vh;
}

.care-list {
  padding: 20rpx;
}

.care-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .care-header {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    .care-icon {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f3ff;
      border-radius: 50%;
      margin-right: 16rpx;
    }
    
    .care-info {
      flex: 1;
      
      .care-type {
        display: block;
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
      }
      
      .care-plant {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
    
    .care-time {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .care-content {
    padding: 16rpx;
    background: #f8f8f8;
    border-radius: 8rpx;
    margin-bottom: 16rpx;
    
    text {
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
    }
  }
  
  .care-images {
    display: flex;
    
    .care-image {
      width: 160rpx;
      height: 160rpx;
      border-radius: 8rpx;
      margin-right: 10rpx;
    }
  }
}
</style>
