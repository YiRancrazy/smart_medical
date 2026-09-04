<template>
  <div ref="wrapRef" class="cap-wrap">
    <div v-if="error" class="cap-error" @click="loadCaptcha">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
        <line x1="12" y1="9" x2="12" y2="13" />
        <line x1="12" y1="17" x2="12.01" y2="17" />
      </svg>
      验证码加载失败，点击重试
    </div>
    <template v-else>
      <div class="cap-stage" @pointerdown="onStart" :style="{ width: dispW + 'px', height: dispH + 'px' }">
        <img v-if="bg" :src="bg" class="cap-bg" :style="{ width: dispW + 'px', height: dispH + 'px' }" />
        <img
          v-if="jig"
          :src="jig"
          class="cap-jigsaw"
          :style="{ left: offset + 'px', top: 0, width: jigW + 'px', height: dispH + 'px' }"
        />
        <div v-if="loading" class="cap-loading"><span class="cap-spinner" /></div>
        <button
          v-if="!verified && !loading"
          type="button"
          class="cap-refresh"
          title="刷新验证码"
          :disabled="checking"
          @pointerdown.stop
          @click.stop="loadCaptcha"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10" />
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
          </svg>
        </button>
        <span v-if="!loading" class="cap-tip">{{ verified ? '验证通过' : '按住滑块，拖至缺口处' }}</span>
      </div>

      <div v-if="!verified" class="cap-bar" @pointerdown="onStart" :style="{ width: dispW + 'px' }">
        <div class="cap-track-fill" :style="{ width: offset + 'px' }" />
        <span class="cap-bar-hint">{{ checking ? '正在验证…' : '按住滑块，向右拖动完成拼图' }}</span>
        <div class="cap-thumb" :style="{ transform: `translateX(${offset}px)` }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="5" y1="12" x2="19" y2="12" />
            <polyline points="12 5 19 12 12 19" />
          </svg>
        </div>
      </div>
      <div v-else class="cap-success" :style="{ width: dispW + 'px' }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12" />
        </svg>
        验证通过
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
const loading = ref(false)

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
  loading.value = true
  try {
    const data = await fetchCaptcha()
    token = data.token
    secretKey = data.secretKey
    bg.value = data.originalImageBase64
    jig.value = data.jigsawImageBase64
    await measure()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function measure() {
  // 背景图为原始尺寸（310×155）；滑块图（jigsaw）为等高的模板画布，其内部抠图块
  // 已位于自然 y 位置（top 0 平铺即可对齐）。整体水平拖动即可与缺口对齐。
  const [bgImg, jigImg] = await Promise.all([loadImage(bg.value), loadImage(jig.value)])
  const bgW = bgImg.naturalWidth
  const bgH = bgImg.naturalHeight
  const jigWNatural = jigImg.naturalWidth

  // 响应式：画布宽度尽量铺满容器（与表单输入框对齐），上限 360px，下限 200px
  const avail = wrapRef.value ? wrapRef.value.clientWidth : bgW
  dispW.value = Math.max(Math.min(avail || bgW, 360), 200)
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
@import '@/styles/variables.less';

.cap-wrap {
  :deep(*) {
    box-sizing: border-box;
  }
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cap-stage {
  position: relative;
  border-radius: @border-radius-base;
  overflow: hidden;
  user-select: none;
  touch-action: none;
  background: #eef1f4;
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
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: @font-size-xs;
  letter-spacing: 0.5px;
  white-space: nowrap;
  pointer-events: none;
}
.cap-refresh {
  position: absolute;
  right: 8px;
  top: 8px;
  z-index: 2;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: @primary-color-active;
  box-shadow: @shadow-sm;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease;

  svg {
    width: 15px;
    height: 15px;
  }
  &:hover {
    background: #fff;
  }
  &:active {
    transform: scale(0.92);
  }
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
.cap-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
}
.cap-spinner {
  width: 22px;
  height: 22px;
  border: 2px solid rgba(16, 185, 129, 0.25);
  border-top-color: @primary-color;
  border-radius: 50%;
  animation: cap-spin 0.7s linear infinite;
}
@keyframes cap-spin {
  to {
    transform: rotate(360deg);
  }
}
.cap-bar {
  position: relative;
  height: 40px;
  margin-top: 12px;
  border: 1px solid @border-color;
  border-radius: @border-radius-base;
  background: #f7f8fa;
  overflow: hidden;
  cursor: grab;
  touch-action: none;
  user-select: none;
}
.cap-track-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.35), rgba(52, 211, 153, 0.5));
}
.cap-bar-hint {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: @text-color-disabled;
  font-size: @font-size-sm;
  pointer-events: none;
  user-select: none;
}
.cap-thumb {
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, @primary-color, @primary-color-active);
  color: #fff;
  border-radius: @border-radius-sm;
  box-shadow: @shadow-base;
  cursor: grab;
  touch-action: none;

  svg {
    width: 18px;
    height: 18px;
  }
}
.cap-success {
  height: 40px;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid rgba(16, 185, 129, 0.35);
  border-radius: @border-radius-base;
  background: @primary-color-bg;
  color: @primary-color-active;
  font-size: @font-size-sm;
  font-weight: @font-weight-medium;

  svg {
    width: 16px;
    height: 16px;
  }
}
.cap-error {
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px dashed @border-color;
  border-radius: @border-radius-base;
  background: #f7f8fa;
  color: @error-color;
  font-size: @font-size-sm;
  cursor: pointer;

  svg {
    width: 20px;
    height: 20px;
  }
}
</style>
