<template>
  <view class="bottle-page theme-starry">
    <!-- 背景 Canvas 容器 -->
    <view id="bg-canvas-container" class="bg-canvas"></view>

    <!-- 返回与我的瓶子按钮 -->
    <view class="top-bar">
      <view class="icon-btn" @click="goBack"><i class="fa-solid fa-chevron-left"></i></view>
      <view class="icon-btn" @click="goMyBottles"><i class="fa-solid fa-bottle-water"></i> 我的瓶子</view>
    </view>

    <!-- 主界面控件 -->
    <view class="hint" :class="{ hide: !hintOn }">拾取漂流瓶，看看大海带来了什么心愿</view>
    
    <view class="pick-btn" :class="{ active: pickMode }" @click="togglePickMode" :style="{ opacity: hasPickable ? 1 : 0.35 }">
      <i class="fa-solid" :class="pickMode ? 'fa-xmark' : 'fa-hand-pointer'" style="margin-right:6px;font-size:12px"></i>
      {{ pickMode ? '取消拾取' : '拾取漂流瓶' }}
    </view>

    <view class="wish-btn" @click="showPanel">
      <i class="fa-solid fa-feather" style="margin-right:6px;font-size:12px"></i>写下心愿
    </view>

    <view class="sound-btn" :class="{ on: audioOn }" @click="toggleAudio">
      <i class="fa-solid" :class="audioOn ? 'fa-volume-high' : 'fa-volume-xmark'"></i>
    </view>

    <!-- 写心愿面板 -->
    <view class="panel-overlay" :class="{ show: isPanelShow }">
      <view class="panel-bg" @click="hidePanel"></view>
      <view class="panel-card">
        <h3>写下你的心愿</h3>
        <textarea v-model="throwContent" placeholder="在这里写下你想对大海说的话..." maxlength="300"></textarea>
        <view class="panel-btns">
          <button class="btn-send" @click="doThrowBottle">投入大海</button>
          <button class="btn-cancel" @click="hidePanel">取消</button>
        </view>
      </view>
    </view>

    <!-- 捡到的瓶子纸卷 -->
    <view class="paper" :class="{ show: !!pickedBottle, hide: isHidingPaper }">
      <view class="cap cap-t"></view>
      <view class="paper-body">
        <view class="paper-header">
          <text class="paper-title">来自远方的瓶子</text>
          <text class="paper-time" v-if="pickedBottle">{{ formatTime(pickedBottle.createTime) }}</text>
        </view>
        <p class="paper-content">{{ pickedBottle?.content || paperMessage }}</p>
        
        <!-- 真实接口逻辑：回复或放回 -->
        <template v-if="pickedBottle && pickedBottle.id">
          <textarea v-model="replyContent" class="reply-input" placeholder="写下你的回复..." maxlength="300"></textarea>
          <view class="paper-actions">
            <button class="action-btn ghost" @click="releaseBottle">先放回去</button>
            <button class="action-btn solid" @click="replyBottle">发送回复</button>
          </view>
        </template>
        <template v-else>
          <view class="paper-actions single">
            <button class="action-btn ghost" @click="closePaper">关闭</button>
          </view>
        </template>
      </view>
      <view class="cap cap-b"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { driftBottleApi } from '../../api'
import type { DriftBottle } from '../../api'
import { createStarryEngine } from './starry'

const engine = ref<any>(null)
const pickMode = ref(false)
const hasPickable = ref(true)
const hintOn = ref(true)
const audioOn = ref(false)

const isPanelShow = ref(false)
const throwContent = ref('')

const pickedBottle = ref<DriftBottle | null>(null)
const paperMessage = ref('')
const replyContent = ref('')
const isHidingPaper = ref(false)

const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const goBack = () => {
  uni.navigateBack()
}

const goMyBottles = () => {
  uni.navigateTo({ url: '/pages/drift-bottle/my' })
}

const toggleAudio = () => {
  if (engine.value) {
    audioOn.value = engine.value.toggleAudio()
  }
}

const togglePickMode = () => {
  if (!hasPickable.value && !pickMode.value) return
  pickMode.value = !pickMode.value
  if (engine.value) {
    engine.value.setPickMode(pickMode.value)
  }
  if (pickMode.value) hintOn.value = false
}

const showPanel = () => {
  isPanelShow.value = true
  throwContent.value = ''
}

const hidePanel = () => {
  isPanelShow.value = false
}

const doThrowBottle = async () => {
  if (!throwContent.value.trim()) {
    uni.showToast({ title: '请输入心愿内容', icon: 'none' })
    return
  }
  try {
    uni.showLoading({ title: '正在扔出...' })
    await driftBottleApi.throwBottle({ content: throwContent.value })
    uni.hideLoading()
    hidePanel()
    
    // 播放抛出动画
    if (engine.value) engine.value.doThrow()
    setTimeout(fetchBottleCount, 1500)
    
    // 提示
    showTemporaryPaper(`"${throwContent.value}" 已随浪远去...`)
    throwContent.value = ''
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}

const releaseBottle = async () => {
  if (!pickedBottle.value) return
  try {
    uni.showLoading({ title: '放回中...' })
    await driftBottleApi.releaseBottle(pickedBottle.value.id)
    uni.hideLoading()
    uni.showToast({ title: '已放回漂流瓶', icon: 'success' })
    closePaper()
    // 给海面增加一个新瓶子（视觉效果）
    if (engine.value) engine.value.addBottles(1)
    setTimeout(fetchBottleCount, 1000)
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}

const replyBottle = async () => {
  if (!pickedBottle.value) return
  if (!replyContent.value.trim()) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }
  try {
    uni.showLoading({ title: '发送中...' })
    await driftBottleApi.replyBottle(pickedBottle.value.id, {
      replyContent: replyContent.value
    })
    uni.hideLoading()
    uni.showToast({ title: '回复成功', icon: 'success' })
    closePaper()
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}

const showTemporaryPaper = (text: string) => {
  paperMessage.value = text
  pickedBottle.value = {} as DriftBottle // 占位显示
  setTimeout(() => {
    if (paperMessage.value === text) {
      closePaper()
    }
  }, 4500)
}

const closePaper = () => {
  isHidingPaper.value = true
  setTimeout(() => {
    pickedBottle.value = null
    paperMessage.value = ''
    replyContent.value = ''
    isHidingPaper.value = false
  }, 350)
}

const fetchBottleCount = async () => {
  try {
    const res = await driftBottleApi.getList({ current: 1, size: 1, status: 'FLOATING' })
    if (res && res.total !== undefined) {
      if (engine.value) {
        engine.value.setBottleCount(res.total)
      }
    }
  } catch (e) {
    console.error('获取漂流瓶数量失败', e)
  }
}

onMounted(() => {
  // 注入 FontAwesome
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'
  document.head.appendChild(link)

  // 注入字体
  const fontLink = document.createElement('link')
  fontLink.rel = 'stylesheet'
  fontLink.href = 'https://fonts.googleapis.com/css2?family=ZCOOL+XiaoWei&display=swap'
  document.head.appendChild(fontLink)

  // 初始化引擎
  setTimeout(() => {
    engine.value = createStarryEngine('bg-canvas-container', {
      onPickableChange: (has: boolean) => {
        hasPickable.value = has
        if (!has && pickMode.value) {
          pickMode.value = false
        }
      },
      onPickClick: async (bottleRef: any) => {
        try {
          uni.showLoading({ title: '捞取中...' })
          const res = await driftBottleApi.pickBottle()
          uni.hideLoading()
          
          if (res && res.id) {
            // 成功捞到，播放动画
            pickedBottle.value = res // 暂存数据，等动画结束显示
            engine.value.playPickAnim(bottleRef)
            // 稍后更新剩余数量
            setTimeout(fetchBottleCount, 1000)
          } else {
            uni.showToast({ title: '瓶子溜走了', icon: 'none' })
            engine.value.setPickMode(false)
            pickMode.value = false
            fetchBottleCount()
          }
        } catch (e) {
          uni.hideLoading()
          console.error(e)
          uni.showToast({ title: '捞取失败', icon: 'none' })
          engine.value.setPickMode(false)
          pickMode.value = false
          fetchBottleCount()
        }
      },
      onPickCancel: () => {
        engine.value.setPickMode(false)
        pickMode.value = false
      },
      onPickAnimEnd: () => {
        // 动画结束，纸卷自动展开显示 pickedBottle 内容
        // (由于 pickedBottle.value 已被赋值，模板通过 v-if/class 自动显示)
      }
    })

    if (engine.value) {
      engine.value.mount()
      fetchBottleCount()
    }
  }, 100)
})

onBeforeUnmount(() => {
  if (engine.value) {
    engine.value.unmount()
  }
})
</script>

<style lang="scss" scoped>
.bottle-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.bg-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: auto;
}

.top-bar {
  position: absolute;
  top: 40px;
  left: 20px;
  right: 20px;
  display: flex;
  justify-content: space-between;
  z-index: 20;
  pointer-events: auto;

  .icon-btn {
    background: rgba(15,25,50,0.55);
    border: 1px solid rgba(100,140,200,0.22);
    color: rgba(200,215,240,0.8);
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 14px;
    backdrop-filter: blur(4px);
    cursor: pointer;
    font-family: 'ZCOOL XiaoWei', serif;
  }
}

/* UI 控件样式 */
.hint { position: fixed; bottom: 76px; left: 50%; transform: translateX(-50%); font-family: 'ZCOOL XiaoWei', serif; font-size: 13px; color: rgba(180,200,230,0.4); pointer-events: none; z-index: 10; white-space: nowrap; transition: opacity 1.5s; }
.hint.hide { opacity: 0; }
.pick-btn { position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%); font-family: 'ZCOOL XiaoWei', serif; font-size: 14px; color: rgba(200,215,240,0.7); background: rgba(15,25,50,0.55); border: 1px solid rgba(100,140,200,0.22); border-radius: 20px; padding: 8px 22px; cursor: pointer; z-index: 20; transition: all .3s; backdrop-filter: blur(4px); white-space: nowrap; pointer-events: auto; }
.pick-btn.active { background: rgba(60,120,200,0.25); border-color: rgba(130,180,255,0.4); color: rgba(220,235,255,0.95); box-shadow: 0 0 16px rgba(80,140,230,0.15); }
.wish-btn { position: fixed; bottom: 24px; right: 24px; font-family: 'ZCOOL XiaoWei', serif; font-size: 14px; color: rgba(200,215,240,0.65); background: rgba(15,25,50,0.55); border: 1px solid rgba(100,140,200,0.2); border-radius: 20px; padding: 8px 18px; cursor: pointer; z-index: 20; transition: all .3s; backdrop-filter: blur(4px); white-space: nowrap; pointer-events: auto; }
.sound-btn { position: fixed; bottom: 24px; left: 24px; width: 36px; height: 36px; border-radius: 50%; background: rgba(15,25,50,0.55); border: 1px solid rgba(100,140,200,0.2); color: rgba(180,200,230,0.55); font-size: 14px; cursor: pointer; z-index: 20; display: flex; align-items: center; justify-content: center; transition: all .3s; backdrop-filter: blur(4px); pointer-events: auto; }
.sound-btn.on { color: rgba(140,200,255,0.9); border-color: rgba(100,180,255,0.35); }

/* 写心愿面板 */
.panel-overlay { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 100; opacity: 0; pointer-events: none; transition: opacity .4s; }
.panel-overlay.show { opacity: 1; pointer-events: auto; }
.panel-bg { position: absolute; inset: 0; background: rgba(0,0,0,0.55); }
.panel-card { position: relative; background: linear-gradient(145deg,#141e38,#0c1428); border: 1px solid rgba(100,140,200,0.2); border-radius: 14px; padding: 28px 26px; width: 360px; max-width: 88vw; box-shadow: 0 8px 40px rgba(0,0,0,0.6); }
.panel-card h3 { font-family: 'ZCOOL XiaoWei', serif; font-size: 18px; color: rgba(200,215,240,0.85); margin-bottom: 14px; text-align: center; font-weight: 400; }
.panel-card textarea { width: 100%; height: 90px; background: rgba(10,18,35,0.7); border: 1px solid rgba(80,120,180,0.25); border-radius: 8px; padding: 12px; color: rgba(220,228,240,0.9); font-family: 'ZCOOL XiaoWei', serif; font-size: 14px; resize: none; outline: none; transition: border-color .3s; box-sizing: border-box; }
.panel-btns { display: flex; gap: 12px; margin-top: 16px; justify-content: center; }
.panel-btns button { font-family: 'ZCOOL XiaoWei', serif; font-size: 14px; padding: 7px 24px; border-radius: 16px; cursor: pointer; transition: all .3s; border: 1px solid; }
.btn-send { background: rgba(60,120,200,0.3); border-color: rgba(100,160,240,0.35)!important; color: rgba(210,225,250,0.9); }
.btn-cancel { background: transparent; border-color: rgba(100,140,200,0.2)!important; color: rgba(180,195,220,0.6); }

/* 信纸卡片 */
.paper { position: fixed; top: 50%; left: 50%; transform: translate(-50%,-50%) scaleY(0); width: 300px; max-width: 85vw; z-index: 90; pointer-events: none; transition: transform .55s cubic-bezier(.34,1.56,.64,1), opacity .4s; opacity: 0; }
.paper.show { transform: translate(-50%,-50%) scaleY(1); opacity: 1; pointer-events: auto; }
.paper.hide { transform: translate(-50%,-50%) scaleY(0.95); opacity: 0; transition: transform .3s, opacity .3s; pointer-events: none; }
.cap { height: 18px; background: radial-gradient(ellipse at center,#c4a882 30%,#8b7355); border-radius: 50%; position: relative; z-index: 1; }
.cap-t { margin-bottom: -4px; }
.cap-b { margin-top: -4px; }
.paper-body { background: linear-gradient(90deg,#d0c098,#e6dac0,#d0c098); padding: 24px 20px; text-align: center; border-left: 2px solid #a09070; border-right: 2px solid #a09070; box-shadow: 0 4px 24px rgba(0,0,0,0.5); }
.paper-header { display: flex; justify-content: space-between; margin-bottom: 12px; border-bottom: 1px dashed rgba(139, 90, 43, 0.3); padding-bottom: 8px; }
.paper-title { font-family: 'ZCOOL XiaoWei', serif; font-size: 16px; color: #5d4037; font-weight: bold; }
.paper-time { font-size: 12px; color: #8d6e63; }
.paper-content { font-family: 'ZCOOL XiaoWei', serif; font-size: 15px; color: #4a3a28; line-height: 1.8; margin-bottom: 16px; text-align: left; }

.reply-input { width: 100%; height: 80px; background: rgba(255,255,255,0.4); border: 1px solid rgba(139, 90, 43, 0.3); border-radius: 8px; padding: 10px; font-family: 'ZCOOL XiaoWei', serif; font-size: 14px; color: #4a3a28; box-sizing: border-box; margin-bottom: 16px; text-align: left; }
.paper-actions { display: flex; gap: 12px; justify-content: center; }
.paper-actions.single { justify-content: center; }
.action-btn { flex: 1; padding: 8px 0; border-radius: 20px; font-family: 'ZCOOL XiaoWei', serif; font-size: 14px; border: none; cursor: pointer; }
.action-btn.ghost { background: rgba(255,255,255,0.5); color: #5d4037; border: 1px solid rgba(139, 90, 43, 0.4); }
.action-btn.solid { background: #8b5a2b; color: #fff; }
</style>
