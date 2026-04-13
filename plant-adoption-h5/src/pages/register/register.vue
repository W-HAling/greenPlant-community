<template>
  <view class="register-page">
    <view class="register-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">注册账号</text>
      <view class="placeholder"></view>
    </view>
    
    <view class="register-content">
      <view class="brand-section">
        <view class="brand-icon">
          <text class="icon-text">🌱</text>
        </view>
        <text class="brand-title">加入绿植领养</text>
        <text class="brand-desc">让办公室充满生机，从领养一株绿植开始</text>
      </view>
      
      <view class="form-section">
        <view class="form-group">
          <text class="form-label">手机号</text>
          <view class="input-wrapper" :class="{ 'input-error': errors.phone }">
            <text class="input-icon">📱</text>
            <input
              v-model="formData.phone"
              class="form-input"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              @blur="validatePhone"
              @input="clearError('phone')"
            />
          </view>
          <text class="error-text" v-if="errors.phone">{{ errors.phone }}</text>
        </view>
        
        <view class="form-group">
          <text class="form-label">验证码</text>
          <view class="input-wrapper code-wrapper" :class="{ 'input-error': errors.code }">
            <text class="input-icon">🔐</text>
            <input
              v-model="formData.code"
              class="form-input code-input"
              placeholder="请输入验证码"
              type="number"
              maxlength="6"
              @blur="validateCode"
              @input="clearError('code')"
            />
            <view 
              class="code-btn" 
              :class="{ disabled: countdown > 0 || !isPhoneValid }"
              @click="sendCode"
            >
              <text class="code-btn-text">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</text>
            </view>
          </view>
          <text class="error-text" v-if="errors.code">{{ errors.code }}</text>
        </view>
        
        <view class="form-group">
          <text class="form-label">昵称</text>
          <view class="input-wrapper" :class="{ 'input-error': errors.nickname }">
            <text class="input-icon">👤</text>
            <input
              v-model="formData.nickname"
              class="form-input"
              placeholder="请输入昵称（2-12个字符）"
              maxlength="12"
              @blur="validateNickname"
              @input="clearError('nickname')"
            />
          </view>
          <text class="error-text" v-if="errors.nickname">{{ errors.nickname }}</text>
        </view>
        
        <view class="form-group">
          <text class="form-label">部门</text>
          <view class="input-wrapper" @click="showDepartmentPicker = true">
            <text class="input-icon">🏢</text>
            <text class="form-input picker-input" :class="{ placeholder: !selectedDepartment }">
              {{ selectedDepartment ? selectedDepartment.name : '请选择部门（选填）' }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <view class="agreement-section">
          <view class="checkbox-wrapper" @click="toggleAgreement">
            <view class="checkbox" :class="{ checked: isAgreed }">
              <text class="check-icon" v-if="isAgreed">✓</text>
            </view>
          </view>
          <text class="agreement-text">
            我已阅读并同意
            <text class="agreement-link" @click.stop="showAgreement('user')">《用户协议》</text>
            和
            <text class="agreement-link" @click.stop="showAgreement('privacy')">《隐私政策》</text>
          </text>
        </view>
        
        <view class="submit-btn" :class="{ disabled: !canSubmit }" @click="handleRegister">
          <text class="submit-btn-text" v-if="!isLoading">立即注册</text>
          <view class="loading-wrapper" v-else>
            <view class="loading-spinner"></view>
            <text class="loading-text">注册中...</text>
          </view>
        </view>
        
        <view class="login-link">
          <text class="link-text">已有账号？</text>
          <text class="link-btn" @click="goLogin">立即登录</text>
        </view>
      </view>
    </view>
    
    <view class="department-picker" v-if="showDepartmentPicker" @click.self="showDepartmentPicker = false">
      <view class="picker-content" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择部门</text>
          <view class="picker-close" @click="showDepartmentPicker = false">
            <text class="close-icon">×</text>
          </view>
        </view>
        <scroll-view scroll-y class="picker-list">
          <view 
            class="picker-item" 
            :class="{ selected: selectedDepartment?.id === dept.id }"
            v-for="dept in departments" 
            :key="dept.id"
            @click="selectDepartment(dept)"
          >
            <text class="dept-name">{{ dept.name }}</text>
            <text class="check-icon" v-if="selectedDepartment?.id === dept.id">✓</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { authApi } from '../../api'
import { useUserStore } from '../../stores/user'

interface Department {
  id: number
  name: string
}

const userStore = useUserStore()

const formData = ref({
  phone: '',
  code: '',
  nickname: '',
  departmentId: undefined as number | undefined
})

const errors = ref({
  phone: '',
  code: '',
  nickname: ''
})

const countdown = ref(0)
const isLoading = ref(false)
const isAgreed = ref(false)
const showDepartmentPicker = ref(false)
const selectedDepartment = ref<Department | null>(null)
const departments = ref<Department[]>([])

const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(formData.value.phone)
})

const canSubmit = computed(() => {
  return isPhoneValid.value && 
         formData.value.code.length === 6 && 
         formData.value.nickname.length >= 2 &&
         isAgreed.value &&
         !isLoading.value
})

const validatePhone = () => {
  if (!formData.value.phone) {
    errors.value.phone = '请输入手机号'
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    errors.value.phone = '请输入正确的手机号'
    return false
  }
  errors.value.phone = ''
  return true
}

const validateCode = () => {
  if (!formData.value.code) {
    errors.value.code = '请输入验证码'
    return false
  }
  if (formData.value.code.length !== 6) {
    errors.value.code = '验证码为6位数字'
    return false
  }
  errors.value.code = ''
  return true
}

const validateNickname = () => {
  if (!formData.value.nickname) {
    errors.value.nickname = '请输入昵称'
    return false
  }
  if (formData.value.nickname.length < 2) {
    errors.value.nickname = '昵称至少2个字符'
    return false
  }
  errors.value.nickname = ''
  return true
}

const clearError = (field: keyof typeof errors.value) => {
  errors.value[field] = ''
}

const sendCode = async () => {
  if (countdown.value > 0 || !isPhoneValid.value) return
  
  try {
    await authApi.sendCode(formData.value.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    console.error(e)
  }
}

const toggleAgreement = () => {
  isAgreed.value = !isAgreed.value
}

const showAgreement = (type: string) => {
  uni.showToast({ title: `${type === 'user' ? '用户协议' : '隐私政策'}页面开发中`, icon: 'none' })
}

const selectDepartment = (dept: Department) => {
  selectedDepartment.value = dept
  formData.value.departmentId = dept.id
  showDepartmentPicker.value = false
}

const handleRegister = async () => {
  if (!canSubmit.value) return
  
  if (!validatePhone() || !validateCode() || !validateNickname()) return
  
  isLoading.value = true
  
  try {
    const user = await authApi.register({
      phone: formData.value.phone,
      code: formData.value.code,
      nickname: formData.value.nickname,
      departmentId: formData.value.departmentId
    })
    
    userStore.setUserInfo(user)
    uni.showToast({ title: '注册成功', icon: 'success' })
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (e: any) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

const goLogin = () => {
  uni.redirectTo({ url: '/pages/login/login' })
}

onMounted(() => {
  departments.value = [
    { id: 1, name: '技术部' },
    { id: 2, name: '产品部' },
    { id: 3, name: '设计部' },
    { id: 4, name: '运营部' },
    { id: 5, name: '市场部' },
    { id: 6, name: '人事部' },
    { id: 7, name: '财务部' },
    { id: 8, name: '行政部' }
  ]
})
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F5E9 0%, #F5F7F5 30%, #FFFFFF 100%);
}

.register-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  padding-top: calc(24rpx + env(safe-area-inset-top));
  
  .back-btn {
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 50%;
    
    .back-icon {
      font-size: 36rpx;
      color: #333;
    }
  }
  
  .header-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #1A1A1A;
  }
  
  .placeholder {
    width: 72rpx;
  }
}

.register-content {
  padding: 32rpx;
}

.brand-section {
  text-align: center;
  margin-bottom: 48rpx;
  
  .brand-icon {
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #81C784 100%);
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.3);
    
    .icon-text {
      font-size: 56rpx;
    }
  }
  
  .brand-title {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 12rpx;
  }
  
  .brand-desc {
    font-size: 28rpx;
    color: #666;
    line-height: 1.5;
  }
}

.form-section {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.form-group {
  margin-bottom: 32rpx;
  
  .form-label {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #1A1A1A;
    margin-bottom: 16rpx;
  }
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #F5F7F5;
  border: 2rpx solid transparent;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 96rpx;
  transition: all 0.2s ease;
  
  &:focus-within {
    background: #FFFFFF;
    border-color: #4CAF50;
    box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
  }
  
  &.input-error {
    border-color: #F44336;
    background: #FFEBEE;
  }
  
  .input-icon {
    font-size: 32rpx;
    margin-right: 16rpx;
  }
  
  .form-input {
    flex: 1;
    height: 96rpx;
    font-size: 30rpx;
    color: #1A1A1A;
    
    &.picker-input {
      display: flex;
      align-items: center;
      
      &.placeholder {
        color: #999;
      }
    }
  }
  
  .picker-arrow {
    font-size: 32rpx;
    color: #999;
    margin-left: 8rpx;
  }
}

.code-wrapper {
  .code-input {
    flex: 1;
  }
  
  .code-btn {
    flex-shrink: 0;
    padding: 16rpx 24rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    border-radius: 12rpx;
    
    &.disabled {
      background: #E5E5E5;
    }
    
    .code-btn-text {
      font-size: 26rpx;
      font-weight: 500;
      color: #FFFFFF;
    }
    
    &.disabled .code-btn-text {
      color: #999;
    }
  }
}

.error-text {
  display: block;
  font-size: 24rpx;
  color: #F44336;
  margin-top: 8rpx;
  padding-left: 8rpx;
}

.agreement-section {
  display: flex;
  align-items: flex-start;
  margin: 32rpx 0;
  
  .checkbox-wrapper {
    margin-right: 16rpx;
    padding-top: 4rpx;
  }
  
  .checkbox {
    width: 40rpx;
    height: 40rpx;
    border: 2rpx solid #CCCCCC;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    
    &.checked {
      background: #4CAF50;
      border-color: #4CAF50;
    }
    
    .check-icon {
      font-size: 24rpx;
      color: #FFFFFF;
      font-weight: 700;
    }
  }
  
  .agreement-text {
    flex: 1;
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }
  
  .agreement-link {
    color: #4CAF50;
  }
}

.submit-btn {
  height: 96rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.3);
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  }
  
  &.disabled {
    background: #CCCCCC;
    box-shadow: none;
  }
  
  .submit-btn-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #FFFFFF;
  }
  
  .loading-wrapper {
    display: flex;
    align-items: center;
    
    .loading-spinner {
      width: 32rpx;
      height: 32rpx;
      border: 3rpx solid rgba(255, 255, 255, 0.3);
      border-top-color: #FFFFFF;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-right: 12rpx;
    }
    
    .loading-text {
      font-size: 30rpx;
      color: #FFFFFF;
    }
  }
}

.login-link {
  text-align: center;
  margin-top: 32rpx;
  
  .link-text {
    font-size: 28rpx;
    color: #666;
  }
  
  .link-btn {
    font-size: 28rpx;
    color: #4CAF50;
    font-weight: 500;
  }
}

.department-picker {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.picker-content {
  width: 100%;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 70vh;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 2rpx solid #F5F5F5;
  
  .picker-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #1A1A1A;
  }
  
  .picker-close {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .close-icon {
      font-size: 48rpx;
      color: #999;
    }
  }
}

.picker-list {
  max-height: 60vh;
  padding-bottom: env(safe-area-inset-bottom);
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 2rpx solid #F5F5F5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.selected {
    background: #E8F5E9;
    
    .dept-name {
      color: #4CAF50;
      font-weight: 500;
    }
  }
  
  .dept-name {
    font-size: 30rpx;
    color: #1A1A1A;
  }
  
  .check-icon {
    font-size: 32rpx;
    color: #4CAF50;
    font-weight: 700;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
