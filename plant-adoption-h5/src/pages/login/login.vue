<template>
  <view class="login-page">
    <view class="bg-decoration">
      <view class="bg-circle circle-1"></view>
      <view class="bg-circle circle-2"></view>
      <view class="bg-circle circle-3"></view>
      <view class="bg-leaf leaf-1">🌿</view>
      <view class="bg-leaf leaf-2">🌱</view>
      <view class="bg-leaf leaf-3">🍃</view>
    </view>
    
    <view class="login-content">
      <view class="brand-section">
        <view class="brand-icon">
          <text class="icon-text">🌱</text>
        </view>
        <text class="brand-title">绿植领养</text>
        <text class="brand-subtitle">让办公室充满生机</text>
      </view>
      
      <view class="form-card">
        <text class="form-title">欢迎登录</text>
        
        <view class="form-group">
          <text class="form-label">手机号</text>
          <view class="input-wrapper" :class="{ 'input-focus': focusField === 'phone', 'input-error': errors.phone }">
            <text class="input-icon">📱</text>
            <input
              v-model="formData.phone"
              class="form-input"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              @focus="focusField = 'phone'"
              @blur="handlePhoneBlur"
              @input="clearError('phone')"
            />
            <view class="input-clear" v-if="formData.phone" @click="formData.phone = ''">
              <text class="clear-icon">×</text>
            </view>
          </view>
          <text class="error-text" v-if="errors.phone">{{ errors.phone }}</text>
        </view>
        
        <view class="form-group">
          <text class="form-label">验证码</text>
          <view class="input-wrapper code-wrapper" :class="{ 'input-focus': focusField === 'code', 'input-error': errors.code }">
            <text class="input-icon">🔐</text>
            <input
              v-model="formData.code"
              class="form-input code-input"
              placeholder="请输入验证码"
              type="number"
              maxlength="6"
              @focus="focusField = 'code'"
              @blur="handleCodeBlur"
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
        
        <view class="form-options">
          <view class="remember-me" @click="toggleRemember">
            <view class="checkbox" :class="{ checked: formData.remember }">
              <text class="check-icon" v-if="formData.remember">✓</text>
            </view>
            <text class="remember-text">记住手机号</text>
          </view>
        </view>
        
        <view class="submit-btn" :class="{ disabled: !canSubmit, loading: isLoading }" @click="handleLogin">
          <text class="submit-btn-text" v-if="!isLoading">登 录</text>
          <view class="loading-wrapper" v-else>
            <view class="loading-spinner"></view>
            <text class="loading-text">登录中...</text>
          </view>
        </view>
        
        <view class="divider-section">
          <view class="divider-line"></view>
          <text class="divider-text">或</text>
          <view class="divider-line"></view>
        </view>
        
        <view class="register-link" @click="goRegister">
          <text class="link-text">还没有账号？</text>
          <text class="link-btn">立即注册</text>
        </view>
      </view>
      
      <view class="demo-hint">
        <text class="hint-text">演示账号：13800138000</text>
        <text class="hint-text">验证码：123456</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { authApi } from '../../api'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const formData = ref({
  phone: '',
  code: '',
  remember: false
})

const errors = ref({
  phone: '',
  code: ''
})

const focusField = ref('')
const countdown = ref(0)
const isLoading = ref(false)

const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(formData.value.phone)
})

const canSubmit = computed(() => {
  return isPhoneValid.value && 
         formData.value.code.length === 6 && 
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

const clearError = (field: keyof typeof errors.value) => {
  errors.value[field] = ''
}

const handlePhoneBlur = () => {
  focusField.value = ''
  if (formData.value.phone) {
    validatePhone()
  }
}

const handleCodeBlur = () => {
  focusField.value = ''
  if (formData.value.code) {
    validateCode()
  }
}

const toggleRemember = () => {
  formData.value.remember = !formData.value.remember
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

const handleLogin = async () => {
  if (!canSubmit.value) return
  
  if (!validatePhone() || !validateCode()) return
  
  isLoading.value = true
  
  try {
    const user = await authApi.login(formData.value.phone, formData.value.code)
    
    if (formData.value.remember) {
      uni.setStorageSync('rememberedPhone', formData.value.phone)
    } else {
      uni.removeStorageSync('rememberedPhone')
    }
    
    userStore.setUserInfo(user)
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (e: any) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}

const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' })
}

onMounted(() => {
  const rememberedPhone = uni.getStorageSync('rememberedPhone')
  if (rememberedPhone) {
    formData.value.phone = rememberedPhone
    formData.value.remember = true
  }
})
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F5E9 0%, #F5F7F5 40%, #FFFFFF 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
  
  &.circle-1 {
    width: 400rpx;
    height: 400rpx;
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.2) 0%, rgba(129, 199, 132, 0.1) 100%);
    top: -100rpx;
    right: -100rpx;
    animation: float 8s ease-in-out infinite;
  }
  
  &.circle-2 {
    width: 300rpx;
    height: 300rpx;
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.15) 0%, rgba(129, 199, 132, 0.08) 100%);
    top: 200rpx;
    left: -80rpx;
    animation: float 6s ease-in-out infinite reverse;
  }
  
  &.circle-3 {
    width: 200rpx;
    height: 200rpx;
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(129, 199, 132, 0.05) 100%);
    bottom: 300rpx;
    right: 60rpx;
    animation: float 10s ease-in-out infinite;
  }
}

.bg-leaf {
  position: absolute;
  font-size: 48rpx;
  opacity: 0.6;
  
  &.leaf-1 {
    top: 120rpx;
    right: 80rpx;
    animation: sway 4s ease-in-out infinite;
  }
  
  &.leaf-2 {
    top: 300rpx;
    left: 60rpx;
    animation: sway 5s ease-in-out infinite reverse;
  }
  
  &.leaf-3 {
    bottom: 400rpx;
    right: 100rpx;
    animation: sway 6s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20rpx) rotate(5deg); }
}

@keyframes sway {
  0%, 100% { transform: rotate(-10deg); }
  50% { transform: rotate(10deg); }
}

.login-content {
  position: relative;
  z-index: 1;
  padding: 120rpx 40rpx 40rpx;
  padding-top: calc(120rpx + env(safe-area-inset-top));
}

.brand-section {
  text-align: center;
  margin-bottom: 48rpx;
  animation: slideDown 0.6s ease-out;
  
  .brand-icon {
    width: 140rpx;
    height: 140rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #81C784 100%);
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24rpx;
    box-shadow: 0 12rpx 32rpx rgba(76, 175, 80, 0.35);
    
    .icon-text {
      font-size: 64rpx;
    }
  }
  
  .brand-title {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: #1A1A1A;
    margin-bottom: 12rpx;
    letter-spacing: 4rpx;
  }
  
  .brand-subtitle {
    font-size: 28rpx;
    color: #666;
    letter-spacing: 2rpx;
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 48rpx 36rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08);
  animation: slideUp 0.6s ease-out 0.2s both;
  
  .form-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #1A1A1A;
    margin-bottom: 36rpx;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-group {
  margin-bottom: 28rpx;
  
  .form-label {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #1A1A1A;
    margin-bottom: 14rpx;
  }
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #F5F7F5;
  border: 2rpx solid transparent;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 100rpx;
  transition: all 0.25s ease;
  
  &.input-focus {
    background: #FFFFFF;
    border-color: #4CAF50;
    box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.12);
  }
  
  &.input-error {
    border-color: #F44336;
    background: #FFEBEE;
  }
  
  .input-icon {
    font-size: 36rpx;
    margin-right: 16rpx;
  }
  
  .form-input {
    flex: 1;
    height: 100rpx;
    font-size: 32rpx;
    color: #1A1A1A;
  }
  
  .input-clear {
    width: 40rpx;
    height: 40rpx;
    background: #CCCCCC;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .clear-icon {
      font-size: 28rpx;
      color: #FFFFFF;
    }
  }
}

.code-wrapper {
  .code-input {
    flex: 1;
  }
  
  .code-btn {
    flex-shrink: 0;
    padding: 18rpx 28rpx;
    background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
    border-radius: 12rpx;
    transition: all 0.2s ease;
    
    &:active {
      transform: scale(0.96);
    }
    
    &.disabled {
      background: #E5E5E5;
      pointer-events: none;
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
  margin-top: 10rpx;
  padding-left: 8rpx;
  animation: shake 0.3s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8rpx); }
  75% { transform: translateX(8rpx); }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24rpx 0;
  
  .remember-me {
    display: flex;
    align-items: center;
    
    .checkbox {
      width: 40rpx;
      height: 40rpx;
      border: 2rpx solid #CCCCCC;
      border-radius: 10rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12rpx;
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
    
    .remember-text {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.submit-btn {
  height: 100rpx;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 36rpx;
  box-shadow: 0 8rpx 24rpx rgba(76, 175, 80, 0.35);
  transition: all 0.25s ease;
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.35);
  }
  
  &.disabled {
    background: #CCCCCC;
    box-shadow: none;
    pointer-events: none;
  }
  
  .submit-btn-text {
    font-size: 34rpx;
    font-weight: 600;
    color: #FFFFFF;
    letter-spacing: 8rpx;
  }
  
  .loading-wrapper {
    display: flex;
    align-items: center;
    
    .loading-spinner {
      width: 36rpx;
      height: 36rpx;
      border: 3rpx solid rgba(255, 255, 255, 0.3);
      border-top-color: #FFFFFF;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-right: 16rpx;
    }
    
    .loading-text {
      font-size: 30rpx;
      color: #FFFFFF;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.divider-section {
  display: flex;
  align-items: center;
  margin: 36rpx 0;
  
  .divider-line {
    flex: 1;
    height: 1rpx;
    background: #E5E5E5;
  }
  
  .divider-text {
    padding: 0 24rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.register-link {
  text-align: center;
  
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

.demo-hint {
  text-align: center;
  margin-top: 48rpx;
  padding: 24rpx;
  background: rgba(76, 175, 80, 0.08);
  border-radius: 16rpx;
  animation: fadeIn 0.6s ease-out 0.4s both;
  
  .hint-text {
    display: block;
    font-size: 24rpx;
    color: #4CAF50;
    line-height: 1.8;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
