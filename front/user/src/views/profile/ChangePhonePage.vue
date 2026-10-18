<template>
  <div class="page">
    <van-nav-bar title="更换手机号" left-arrow @click-left="$router.back()" />

    <div class="content">
      <div class="phone-card surface-card">
        <van-field label="当前手机号" :model-value="maskPhone(currentPhone) || '-'" readonly />
        <van-field
          v-model="form.oldPhoneCode"
          type="digit"
          maxlength="6"
          label="旧号验证码"
          placeholder="请输入旧手机号验证码"
          :rules="[{ required: true, message: '请输入旧手机号验证码' }]"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              plain
              :disabled="countdown.old > 0 || sending.old"
              @click="sendCode('old')"
            >
              {{ countdown.old > 0 ? `${countdown.old}s后重发` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
        <van-field
          v-model="form.newPhone"
          type="tel"
          maxlength="11"
          label="新手机号"
          placeholder="请输入新手机号"
          :rules="[
            { required: true, message: '请输入新手机号' },
            { validator: isPhone, message: '新手机号格式不正确' }
          ]"
        />
        <van-field
          v-model="form.newPhoneCode"
          type="digit"
          maxlength="6"
          label="新号验证码"
          placeholder="请输入新手机号验证码"
          :rules="[{ required: true, message: '请输入新手机号验证码' }]"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              plain
              :disabled="countdown.new > 0 || sending.new"
              @click="sendCode('new')"
            >
              {{ countdown.new > 0 ? `${countdown.new}s后重发` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
      </div>

      <van-button round block type="primary" :loading="submitting" @click="handleSubmit">
        确认换绑
      </van-button>
      <div class="tip">换绑成功后需要重新登录</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { showSuccessToast, showToast } from 'vant'
import { changePhone, sendPhoneChangeCode } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { isPhone } from '@/utils/validator'
import { maskPhone } from '@/utils/format'

type PhoneScene = 'old' | 'new'

const userStore = useUserStore()
const currentPhone = ref('')
const submitting = ref(false)
const sending = reactive<Record<PhoneScene, boolean>>({ old: false, new: false })
const countdown = reactive<Record<PhoneScene, number>>({ old: 0, new: 0 })
const timers: Record<PhoneScene, number | null> = { old: null, new: null }
const form = reactive({
  oldPhoneCode: '',
  newPhone: '',
  newPhoneCode: ''
})

onMounted(async () => {
  try {
    await userStore.ensureProfileCompleted()
    currentPhone.value = userStore.profile?.phone || ''
  } catch {
    showToast('手机号加载失败')
  }
})

onUnmounted(() => {
  clearTimer('old')
  clearTimer('new')
})

function clearTimer(scene: PhoneScene) {
  if (timers[scene]) {
    clearInterval(timers[scene]!)
    timers[scene] = null
  }
}

function startCountdown(scene: PhoneScene) {
  clearTimer(scene)
  countdown[scene] = 60
  timers[scene] = window.setInterval(() => {
    countdown[scene] -= 1
    if (countdown[scene] <= 0) clearTimer(scene)
  }, 1000)
}

async function sendCode(scene: PhoneScene) {
  const phone = scene === 'old' ? currentPhone.value : form.newPhone
  if (scene === 'old' && !phone) {
    showToast('当前手机号加载失败')
    return
  }
  if (scene === 'new' && !isPhone(phone)) {
    showToast('请输入正确的新手机号')
    return
  }
  sending[scene] = true
  try {
    await sendPhoneChangeCode({ phone, scene })
    showToast('验证码已发送')
    startCountdown(scene)
  } catch {
    // 拦截器展示后端错误
  } finally {
    sending[scene] = false
  }
}

async function handleSubmit() {
  if (!currentPhone.value) {
    showToast('当前手机号加载失败')
    return
  }
  if (!/^\d{6}$/.test(form.oldPhoneCode)) {
    showToast('请输入6位旧号验证码')
    return
  }
  if (!isPhone(form.newPhone)) {
    showToast('请输入正确的新手机号')
    return
  }
  if (form.newPhone === currentPhone.value) {
    showToast('新手机号不能与当前手机号相同')
    return
  }
  if (!/^\d{6}$/.test(form.newPhoneCode)) {
    showToast('请输入6位新号验证码')
    return
  }

  submitting.value = true
  try {
    const res = await changePhone({
      oldPhoneCode: form.oldPhoneCode,
      newPhone: form.newPhone,
      newPhoneCode: form.newPhoneCode
    })
    showSuccessToast(res.data || '换绑成功，请重新登录')
    userStore.logout()
  } catch {
    // 拦截器展示后端错误
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100dvh;
  background: $color-bg-page;
}

.content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.phone-card {
  overflow: hidden;
}

.tip {
  margin-top: -12px;
  text-align: center;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}
</style>
