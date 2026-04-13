<template>
  <view class="adoption-my-page">
    <view class="status-tabs">
      <up-tabs :list="statusTabs" @change="handleTabChange" :current="currentTab" />
    </view>
    
    <scroll-view scroll-y class="record-scroll" @scrolltolower="loadMore">
      <view class="record-list">
        <view
          class="record-card"
          v-for="record in records"
          :key="record.id"
          @click="goPlantDetail(record.plantId)"
        >
          <view class="record-header">
            <text class="plant-name">{{ record.plantName }}</text>
            <up-tag :text="statusMap[record.status]" :type="statusTypeMap[record.status]" size="mini" />
          </view>
          
          <view class="record-info">
            <view class="info-row">
              <text class="info-label">申请时间</text>
              <text class="info-value">{{ formatTime(record.applyTime) }}</text>
            </view>
            <view class="info-row" v-if="record.approveTime">
              <text class="info-label">审批时间</text>
              <text class="info-value">{{ formatTime(record.approveTime) }}</text>
            </view>
            <view class="info-row" v-if="record.approveRemark">
              <text class="info-label">审批备注</text>
              <text class="info-value">{{ record.approveRemark }}</text>
            </view>
          </view>
          
          <view class="record-actions" v-if="record.status === 'PENDING'">
            <up-button size="small" type="error" text="取消申请" @click.stop="handleCancel(record.id)" />
          </view>
          
          <view class="record-actions" v-if="record.status === 'APPROVED'">
            <up-button size="small" type="primary" text="记录养护" @click.stop="goAddCare(record.plantId)" />
            <up-button size="small" type="warning" text="归还" @click.stop="handleReturn(record.id)" customStyle="margin-left: 20rpx;" />
          </view>
        </view>
      </view>
      
      <up-empty v-if="records.length === 0" text="暂无领养记录" mode="list" />
      <up-loadmore v-else :status="loadStatus" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { adoptionApi } from '../../api'

const currentTab = ref(0)
const records = ref<any[]>([])
const current = ref(1)
const loadStatus = ref<'loadmore' | 'loading' | 'nomore'>('loadmore')

const statusTabs = [
  { name: '全部', status: '' },
  { name: '待审批', status: 'PENDING' },
  { name: '已通过', status: 'APPROVED' },
  { name: '已拒绝', status: 'REJECTED' }
]

const statusMap: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  CANCELLED: '已取消',
  RETURNED: '已归还'
}

const statusTypeMap: Record<string, string> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CANCELLED: 'info',
  RETURNED: 'info'
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const loadRecords = async (refresh = false) => {
  if (refresh) {
    current.value = 1
    records.value = []
    loadStatus.value = 'loadmore'
  }
  
  if (loadStatus.value === 'nomore') return
  
  loadStatus.value = 'loading'
  
  try {
    const status = statusTabs[currentTab.value].status
    const params: Record<string, any> = { current: current.value, size: 10 }
    if (status) params.status = status
    const data = await adoptionApi.getMyRecords(params)
    
    if (refresh) {
      records.value = data.records
    } else {
      records.value.push(...data.records)
    }
    
    if (records.value.length >= data.total) {
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
  loadRecords()
}

const handleTabChange = (index: number) => {
  currentTab.value = index
  loadRecords(true)
}

const handleCancel = (recordId: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消这个领养申请吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await adoptionApi.cancel(recordId)
          uni.showToast({ title: '已取消', icon: 'success' })
          loadRecords(true)
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const handleReturn = (recordId: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要归还这株绿植吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await adoptionApi.returnPlant(recordId)
          uni.showToast({ title: '已归还', icon: 'success' })
          loadRecords(true)
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const goPlantDetail = (plantId: number) => {
  uni.navigateTo({ url: `/pages/plant/detail?id=${plantId}` })
}

const goAddCare = (plantId: number) => {
  uni.navigateTo({ url: `/pages/care/add?plantId=${plantId}` })
}

onMounted(() => {
  loadRecords(true)
})

onShow(() => {
  loadRecords(true)
})
</script>

<style lang="scss" scoped>
.adoption-my-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-tabs {
  background: #fff;
}

.record-scroll {
  height: calc(100vh - 88rpx);
}

.record-list {
  padding: 20rpx;
}

.record-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .record-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .plant-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
  
  .record-info {
    .info-row {
      display: flex;
      margin-bottom: 12rpx;
      
      .info-label {
        width: 160rpx;
        font-size: 26rpx;
        color: #999;
      }
      
      .info-value {
        flex: 1;
        font-size: 26rpx;
        color: #333;
      }
    }
  }
  
  .record-actions {
    display: flex;
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f5f5f5;
  }
}
</style>
