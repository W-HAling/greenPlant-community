<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <img src="/vite.svg" alt="Logo" class="logo" />
        <h1>绿植领养管理系统</h1>
        <p>后台管理登录</p>
      </div>
      
      <el-form ref="formRef" :model="formData" :rules="rules" class="login-form">
        <el-form-item prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            maxlength="11"
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入验证码"
            prefix-icon="Lock"
            maxlength="6"
          >
            <template #append>
              <el-button
                :disabled="countdown > 0"
                @click="handleSendCode"
                :loading="sendingCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tip">
        <p>管理员账号：13800138000</p>
        <p>验证码：123456（模拟）</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { sendCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const formData = reactive({
  phone: '',
  code: ''
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  if (!formData.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    ElMessage.warning('手机号格式不正确')
    return
  }
  
  sendingCode.value = true
  try {
    await sendCode(formData.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error(error)
  } finally {
    sendingCode.value = false
  }
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.loginAction(formData.phone, formData.code)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  .logo {
    width: 60px;
    height: 60px;
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: #909399;
  }
}

.login-form {
  .login-btn {
    width: 100%;
    height: 42px;
  }
}

.login-tip {
  margin-top: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  
  p {
    font-size: 12px;
    color: #909399;
    line-height: 1.8;
  }
}
</style>
