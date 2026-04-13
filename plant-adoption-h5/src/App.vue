<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from './stores/user'

onLaunch(() => {
  console.log('App Launch')
  const userStore = useUserStore()
  userStore.checkLogin()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.container {
  padding: 20rpx;
}

.text-primary {
  color: #5677FC;
}

.text-success {
  color: #19be6b;
}

.text-warning {
  color: #ff9900;
}

.text-error {
  color: #fa3534;
}

.bg-primary {
  background-color: #5677FC;
}

.bg-success {
  background-color: #19be6b;
}

.bg-warning {
  background-color: #ff9900;
}

.bg-error {
  background-color: #fa3534;
}

.mt-10 {
  margin-top: 10rpx;
}

.mt-20 {
  margin-top: 20rpx;
}

.mb-10 {
  margin-bottom: 10rpx;
}

.mb-20 {
  margin-bottom: 20rpx;
}

.p-20 {
  padding: 20rpx;
}

.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-1 {
  flex: 1;
}
</style>
