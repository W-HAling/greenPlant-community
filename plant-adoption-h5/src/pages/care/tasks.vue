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
      <view 
        class="task-card-mix" 
        v-for="task in tasks" 
        :key="task.id"
        :class="[getCareClass(task.careType), task.status.toLowerCase()]"
      >
        <!-- Watermark -->
        <view class="watermark">{{ careEmojiMap[task.careType?.toUpperCase()] || '✨' }}</view>
        
        <view class="top">
          <!-- Avatar -->
          <view class="avatar">{{ plantAvatarMap[task.careType?.toUpperCase()] || '🌵' }}</view>
          <div class="info">
            <view class="title">{{ task.plantName }}</view>
            <view class="subtitle">{{ careTypeMap[task.careType?.toUpperCase()] || task.careType }} <template v-if="task.careDetail">· {{ task.careDetail }}</template></view>
          </div>
          <!-- Status -->
          <view class="status" :class="task.status.toLowerCase()">
            {{ statusMap[task.status] || task.status }}
          </view>
        </view>
        
        <view class="bottom">
          <view class="time">应执行: {{ formatDate(task.dueDate) }}</view>
          <view class="btn-group" v-if="task.status === 'PENDING' || task.status === 'OVERDUE'">
            <button class="btn ghost" @click.stop="goAdjust(task)">调整</button>
            <button class="btn primary" @click.stop="goExecute(task)">去完成</button>
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

const careEmojiMap: Record<string, string> = {
  WATER: '💧',
  FERTILIZE: '🌱',
  PRUNE: '✂️',
  OTHER: '✨'
}

const plantAvatarMap: Record<string, string> = {
  WATER: '🪴',
  FERTILIZE: '🍀',
  PRUNE: '🌿',
  OTHER: '🌵'
}

const getCareClass = (careType: string | undefined) => {
  if (!careType) return 'other'
  return careType.toLowerCase()
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
    url: `/pages/care/add?plantId=${task.plantId}&taskId=${task.id}&taskType=${encodeURIComponent(task.careType || '')}&taskDetail=${encodeURIComponent(task.careDetail || '')}`
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

.task-card-mix {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(63, 143, 82, 0.08);
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:active {
    transform: scale(0.98);
  }

  /* Watermark */
  .watermark {
    position: absolute;
    right: -24rpx;
    bottom: -30rpx;
    font-size: 180rpx;
    line-height: 1;
    opacity: 0.06;
    transform: rotate(-15deg);
    pointer-events: none;
    z-index: 0;
  }

  &.water .watermark { opacity: 0.08; filter: grayscale(50%) brightness(1.2); }
  &.prune .watermark { opacity: 0.06; filter: grayscale(30%); }
  &.fertilize .watermark { opacity: 0.08; filter: grayscale(20%); }

  /* Ensure content is above watermark */
  .top, .bottom {
    position: relative;
    z-index: 1;
  }

  .top {
    display: flex;
    gap: 24rpx;
    align-items: center;
  }

  /* Avatar */
  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: #eef5ef;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    flex-shrink: 0;
  }

  &.water .avatar { background: #eff6ff; }
  &.prune .avatar { background: #fff7ed; }
  &.fertilize .avatar { background: #ecfdf5; }

  .info {
    flex: 1;
  }

  .title {
    font-size: 32rpx;
    font-weight: 600;
    color: #253128;
    margin-bottom: 8rpx;
  }

  .subtitle {
    font-size: 26rpx;
    color: #6d7a70;
    /* Limit lines for long details */
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
  }

  /* Status Tag */
  .status {
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    font-size: 24rpx;
    font-weight: 500;
    white-space: nowrap;
    
    &.overdue { background: #fee2e2; color: #dc2626; }
    &.pending { background: #fef3c7; color: #d97706; }
    &.done { background: #f3f4f6; color: #6b7280; }
  }

  /* Bottom Actions */
  .bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 2rpx dashed #f3f4f6;
    padding-top: 24rpx;
  }

  .time {
    font-size: 24rpx;
    color: #9ca3af;
  }

  .btn-group {
    display: flex;
    gap: 16rpx;
  }

  .btn {
    margin: 0;
    padding: 0 32rpx;
    height: 60rpx;
    line-height: 60rpx;
    border-radius: 40rpx;
    font-size: 26rpx;
    font-weight: 500;
    
    &::after { border: none; }

    &.primary {
      background: #3f8f52;
      color: #ffffff;
      box-shadow: 0 4rpx 12rpx rgba(63, 143, 82, 0.2);
    }

    &.ghost {
      background: #f3f4f6;
      color: #4b5563;
    }
  }

  /* Done state dimming */
  &.done {
    opacity: 0.6;
    box-shadow: none;
    border: 2rpx solid #f3f4f6;
  }
}
</style>
