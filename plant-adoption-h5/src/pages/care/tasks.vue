<template>
  <view class="care-task-page">
    <view class="filter-bar">
      <view
        v-for="item in filters"
        :key="item.value"
        class="filter-chip"
        :class="{ active: currentStatus === item.value }"
        @click="changeFilter(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <view class="task-list" v-if="tasks.length">
      <view class="task-card" v-for="task in tasks" :key="task.id">
        <view class="task-header">
          <view>
            <text class="task-plant">{{ task.plantName }}</text>
            <text class="task-type">{{ careTypeMap[task.careType?.toUpperCase()] || task.careType }}</text>
          </view>
          <view class="task-status" :class="task.status.toLowerCase()">
            {{ statusMap[task.status] || task.status }}
          </view>
        </view>

        <view class="task-detail">{{ task.careDetail }}</view>
        <view class="task-footer">
          <text class="task-date">应执行：{{ formatDate(task.dueDate) }}</text>
          <view class="task-actions" v-if="task.status === 'PENDING' || task.status === 'OVERDUE'">
            <button class="task-btn ghost" @click="goAdjust(task)">调整</button>
            <button class="task-btn" @click="goExecute(task)">去完成</button>
          </view>
        </view>
      </view>
    </view>

    <up-empty v-else text="暂无养护任务" mode="list" />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { carePlanApi } from '../../api'
import type { CareTask } from '../../api'

const tasks = ref<CareTask[]>([])
const currentStatus = ref('')

const filters = [
  { label: '全部', value: '' },
  { label: '待执行', value: 'PENDING' },
  { label: '已逾期', value: 'OVERDUE' },
  { label: '已完成', value: 'DONE' }
]

const statusMap: Record<string, string> = {
  PENDING: '待执行',
  DONE: '已完成',
  OVERDUE: '已逾期',
  CANCELED: '已取消'
}

const careTypeMap: Record<string, string> = {
  WATER: '浇水',
  FERTILIZE: '施肥',
  PRUNE: '修剪',
  OTHER: '其他'
}

const formatDate = (value: string) => {
  if (!value) return '-'
  return value.split('T')[0]
}

const loadTasks = async () => {
  try {
    tasks.value = await carePlanApi.getMyTasks({
      status: currentStatus.value || undefined
    })
  } catch (e) {
    console.error(e)
  }
}

const changeFilter = (status: string) => {
  currentStatus.value = status
  loadTasks()
}

const goExecute = (task: CareTask) => {
  uni.navigateTo({
    url: `/pages/care/add?plantId=${task.plantId}&plantName=${encodeURIComponent(task.plantName || '')}&taskId=${task.id}&taskType=${encodeURIComponent(task.careType || '')}&taskDetail=${encodeURIComponent(task.careDetail || '')}`
  })
}

const goAdjust = (task: CareTask) => {
  uni.navigateTo({
    url: `/pages/care/adjust?taskId=${task.id}&dueDate=${encodeURIComponent(task.dueDate || '')}&cycleType=${encodeURIComponent(task.cycleTypeOverride || '')}&cycleValue=${encodeURIComponent(String(task.cycleValueOverride || ''))}&careDetail=${encodeURIComponent(task.careDetailOverride || task.careDetail || '')}`
  })
}

onMounted(() => {
  loadTasks()
})

onShow(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.care-task-page {
  min-height: 100vh;
  background: #f5f7f5;
  padding: 24rpx;
}

.filter-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.filter-chip {
  padding: 14rpx 28rpx;
  border-radius: 999rpx;
  background: #ffffff;
  color: #5f6b63;
  font-size: 26rpx;
}

.filter-chip.active {
  background: #3f8f52;
  color: #ffffff;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.task-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 20rpx rgba(63, 143, 82, 0.08);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.task-plant {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #253128;
}

.task-type {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #6d7a70;
}

.task-status {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.task-status.pending,
.task-status.overdue {
  background: #fff3d6;
  color: #9a6a00;
}

.task-status.done {
  background: #e7f6ea;
  color: #2f7d42;
}

.task-detail {
  margin-top: 18rpx;
  font-size: 26rpx;
  color: #4f5d53;
  line-height: 1.6;
}

.task-footer {
  margin-top: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-actions {
  display: flex;
  gap: 16rpx;
}

.task-date {
  font-size: 24rpx;
  color: #8b978e;
}

.task-btn {
  margin: 0;
  padding: 0 26rpx;
  height: 64rpx;
  line-height: 64rpx;
  border: none;
  border-radius: 999rpx;
  background: #3f8f52;
  color: #fff;
  font-size: 24rpx;
}

.task-btn.ghost {
  background: #eef5ef;
  color: #3f8f52;
}
</style>
