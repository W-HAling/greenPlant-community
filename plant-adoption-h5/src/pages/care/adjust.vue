<template>
  <view class="care-adjust-page">
    <view class="form-card">
      <view class="field">
        <text class="label">截止日期</text>
        <picker mode="date" :value="formData.dueDate" @change="handleDateChange">
          <view class="picker-value">{{ formData.dueDate || '请选择日期' }}</view>
        </picker>
      </view>

      <view class="field">
        <text class="label">周期类型</text>
        <picker :range="cycleOptions" range-key="label" @change="handleCycleTypeChange">
          <view class="picker-value">{{ currentCycleLabel }}</view>
        </picker>
      </view>

      <view class="field">
        <text class="label">周期数值</text>
        <input v-model="cycleValueInput" class="input" type="number" placeholder="请输入周期数值" />
      </view>

      <view class="field">
        <text class="label">任务内容</text>
        <textarea v-model="formData.careDetail" class="textarea" placeholder="请输入新的养护内容" />
      </view>

      <view class="field">
        <text class="label">调整说明</text>
        <textarea v-model="formData.adjustNote" class="textarea" placeholder="例如：天气变热，缩短浇水周期" />
      </view>
    </view>

    <view class="submit-btn">
      <up-button type="primary" text="保存调整" @click="handleSubmit" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { carePlanApi } from '../../api'

const taskId = ref<number>()
const cycleValueInput = ref('')

const cycleOptions = [
  { label: '沿用当前周期', value: '' },
  { label: '每日', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' }
]

const formData = reactive({
  dueDate: '',
  cycleType: '',
  careDetail: '',
  adjustNote: ''
})

const currentCycleLabel = computed(() => {
  return cycleOptions.find(item => item.value === formData.cycleType)?.label || '沿用当前周期'
})

const handleDateChange = (event: any) => {
  formData.dueDate = event.detail.value
}

const handleCycleTypeChange = (event: any) => {
  const option = cycleOptions[event.detail.value]
  formData.cycleType = option?.value || ''
}

const handleSubmit = async () => {
  if (!taskId.value) return

  try {
    await carePlanApi.adjustTask(taskId.value, {
      dueDate: formData.dueDate || undefined,
      cycleType: formData.cycleType || undefined,
      cycleValue: cycleValueInput.value ? Number(cycleValueInput.value) : undefined,
      careDetail: formData.careDetail || undefined,
      adjustNote: formData.adjustNote || undefined
    })
    uni.showToast({ title: '调整成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1200)
  } catch (error) {
    console.error(error)
  }
}

onLoad((options) => {
  taskId.value = Number(options?.taskId)
  formData.dueDate = options?.dueDate ? decodeURIComponent(options.dueDate) : ''
  formData.cycleType = options?.cycleType ? decodeURIComponent(options.cycleType) : ''
  cycleValueInput.value = options?.cycleValue ? decodeURIComponent(options.cycleValue) : ''
  formData.careDetail = options?.careDetail ? decodeURIComponent(options.careDetail) : ''
})
</script>

<style lang="scss" scoped>
.care-adjust-page {
  min-height: 100vh;
  background: #f5f7f5;
  padding: 24rpx;
}

.form-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}

.field {
  margin-bottom: 24rpx;
}

.label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: #5f6b63;
}

.picker-value,
.input,
.textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f6f8f6;
  font-size: 28rpx;
  color: #253128;
}

.textarea {
  min-height: 180rpx;
}

.submit-btn {
  margin-top: 32rpx;
}
</style>
