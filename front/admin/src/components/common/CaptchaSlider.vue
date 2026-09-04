<template>
  <div ref="wrapRef" class="cap-wrap">
    <div v-if="error" class="cap-error" @click="loadCaptcha">验证码加载失败，点击重试</div>
    <template v-else>
      <div class="cap-stage" @pointerdown="onStart" :style="{ width: dispW + 'px', height: dispH + 'px' }">
        <img v-if="bg" :src="bg" class="cap-bg" :style="{ width: dispW + 'px', height: dispH + 'px' }" />
        <img
          v-if="jig"
          :src="jig"
          class="cap-jigsaw"
          :style="{ left: offset + 'px', top: 0, width: jigW + 'px', height: dispH + 'px' }"
        />
        <span class="cap-tip">{{ verified ? '验证通过' : '按住滑块，拖至缺口处' }}</span>
      </div>
      <div v-if="!verified" class="cap-bar" @pointerdown="onStart" :style="{ width: dispW + 'px' }">
        <div class="cap-track-fill" :style="{ width: offset + 'px' }" />
        <div class="cap-thumb" :style="{ transform: `translateX(${offset}px)` }" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { fetchCaptcha, checkCaptcha } from '@/utils/captcha'

const emit = defineEmits<{ (e: 'verified'): void }>()

const wrapRef = ref<HTMLDivElement | null>(null)
const bg = ref('')
const jig = ref('')
const error = ref(false)
const verified = ref(false)
const checking = ref(false)

const dispW = ref(300)
const dispH = ref(150)
const offset = ref(0)
// 滑块图（jigsaw）为 47×155 的模板画布（透明底 + 抠出的小图），显示宽度按背景等比缩放
const jigW = ref(0)

let scale = 1
let maxOffset = 0

let token = ''
let secretKey = ''
let active = false
let startX = 0
let startOffset = 0

async function loadCaptcha() {
  error.value = false
  verified.value = false
  offset.value = 0
  try {
    const data = await fetchCaptcha()
    token = data.token
    secretKey = data.secretKey
    bg.value = data.originalImageBase64
    jig.value = data.jigsawImageBase64
    await measure()
  } catch {
    error.value = true
  }
}

async function measure() {
  // 背景图为原始尺寸（310×155）；滑块图（jigsaw）为等高的模板画布，其内部抠图块
  // 已位于自然 y 位置（top 0 平铺即可对齐）。整体水平拖动即可与缺口对齐。
  const [bgImg, jigImg] = await Promise.all([loadImage(bg.value), loadImage(jig.value)])
  const bgW = bgImg.naturalWidth
  const bgH = bgImg.naturalHeight
  const jigWNatural = jigImg.naturalWidth

  // 响应式：画布宽度取「容器可用宽度」与原始宽度中的较小者，上限 300px
  const avail = wrapRef.value ? wrapRef.value.clientWidth : bgW
  dispW.value = Math.max(Math.min(bgW, avail || bgW, 300), 220)
  scale = bgW / dispW.value
  dispH.value = Math.round(bgH / scale)
  // 滑块图显示宽度按背景等比缩放；高度覆盖整幅画布（其内部抠图块自带正确 y 位置）
  jigW.value = Math.round(jigWNatural / scale)
  // 拖动范围：从画布最左到最右，让缺口块能到达背景任意 x
  maxOffset = (bgW - jigWNatural) / scale
  // 宽度变化后钳制当前位移
  offset.value = clamp(offset.value, 0, maxOffset)
}

function onResize() {
  if (error.value || checking.value) return
  measure()
}

function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = src
  })
}

function onStart(e: PointerEvent) {
  if (verified.value || checking.value || active) return
  active = true
  startX = e.clientX
  startOffset = offset.value
  window.addEventListener('pointermove', onMove)
  window.addEventListener('pointerup', onEnd)
  try {
    e.preventDefault()
    ;(e.currentTarget as Element).setPointerCapture?.(e.pointerId)
  } catch {
    /* 忽略捕获失败，window 级监听仍可接收 move/up */
  }
}

function onMove(e: PointerEvent) {
  if (!active) return
  offset.value = clamp(startOffset + (e.clientX - startX), 0, maxOffset)
}

function onEnd() {
  window.removeEventListener('pointermove', onMove)
  window.removeEventListener('pointerup', onEnd)
  if (!active) return
  active = false
  verify()
}

async function verify() {
  checking.value = true
  try {
    // x = 缺口块左边缘在背景图中的自然像素坐标（=拖动距离*scale）；
    // y 固定为 5（anji 后端对 blockPuzzle 的参考值，与模板高度=背景高度相关）
    const x = Math.round(offset.value * scale)
    await checkCaptcha(token, x, 5, secretKey)
    verified.value = true
    emit('verified')
  } catch {
    // 校验失败：更换新验证码并重置滑块
    offset.value = 0
    loadCaptcha()
  } finally {
    checking.value = false
  }
}

function clamp(v: number, min: number, max: number) {
  return Math.min(Math.max(v, min), max)
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  loadCaptcha()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('pointermove', onMove)
  window.removeEventListener('pointerup', onEnd)
})
</script>

<style scoped lang="less">
.cap-wrap {
  :deep(*) {
    box-sizing: border-box;
  }
}
.cap-stage {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  user-select: none;
  touch-action: none;
  background: #f5f5f5;
  cursor: pointer;
}
.cap-bg {
  display: block;
}
.cap-jigsaw {
  position: absolute;
  top: 0;
  pointer-events: none;
}
.cap-tip {
  position: absolute;
  left: 50%;
  top: 8px;
  transform: translateX(-50%);
  color: #fff;
  font-size: 12px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.6);
  pointer-events: none;
}
.cap-bar {
  position: relative;
  height: 36px;
  margin-top: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f7f8fa;
  overflow: hidden;
  cursor: grab;
  touch-action: none;
}
.cap-track-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: rgba(16, 185, 129, 0.15);
}
.cap-thumb {
  position: absolute;
  left: 0;
  top: 0;
  width: 36px;
  height: 36px;
  background: #10b981;
  border-radius: 8px;
  cursor: grab;
  touch-action: none;
}
.cap-error {
  padding: 16px;
  text-align: center;
  color: #ef4444;
  font-size: 13px;
  cursor: pointer;
}
</style>
