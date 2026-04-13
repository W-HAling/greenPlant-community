<template>
  <view class="my-bottle-page">
    <view class="tab-bar">
      <view
        v-for="item in tabs"
        :key="item.value"
        class="tab-item"
        :class="{ active: currentTab === item.value }"
        @click="changeTab(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <view class="bottle-list" v-if="bottles.length">
      <view class="bottle-card" v-for="item in bottles" :key="item.id">
        <view class="bottle-head">
          <text class="bottle-status">{{ statusMap[item.status] || item.status }}</text>
          <text class="bottle-time">{{ formatTime(item.createTime) }}</text>
        </view>
        <text class="bottle-content">{{ item.content }}</text>
        <text class="bottle-reply" v-if="item.replyContent">回复：{{ item.replyContent }}</text>
      </view>
    </view>

    <up-empty v-else text="暂无漂流瓶记录" mode="list" />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { driftBottleApi } from '../../api'
import type { DriftBottle } from '../../api'

const currentTab = ref('send')
const bottles = ref<DriftBottle[]>([])

const tabs = [
  { label: '我发送的', value: 'send' },
  { label: '我收到的', value: 'receive' }
]

const statusMap: Record<string, string> = {
  FLOATING: '漂浮中',
  PICKED: '已被拾取',
  REPLIED: '已回复',
  CLOSED: '已关闭'
}

const formatTime = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadData = async () => {
  try {
    bottles.value = await driftBottleApi.getMyBottles({ type: currentTab.value })
  } catch (e) {
    console.error(e)
  }
}

const changeTab = (tab: string) => {
  currentTab.value = tab
  loadData()
}

onMounted(() => {
  loadData()
})

onShow(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.my-bottle-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 24rpx;
}

.tab-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  padding: 16rpx 28rpx;
  border-radius: 999rpx;
  background: #fff;
  font-size: 26rpx;
  color: #66756d;
}

.tab-item.active {
  background: #3d8bff;
  color: #fff;
}

.bottle-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.bottle-card {
  background: #fff;
  border-radius: 22rpx;
  padding: 24rpx;
}

.bottle-head {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.bottle-status {
  font-size: 24rpx;
  color: #4178de;
}

.bottle-time {
  font-size: 22rpx;
  color: #95a197;
}

.bottle-content {
  display: block;
  margin-top: 18rpx;
  font-size: 28rpx;
  line-height: 1.8;
  color: #36443c;
}

.bottle-reply {
  display: block;
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  background: #f4f8ff;
  font-size: 25rpx;
  color: #5271a6;
}
</style>
