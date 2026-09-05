<template>
  <div class="register-page">
    <div class="register-logo">智慧医疗</div>
    <glass-card class="register-card">
      <van-form @submit="handleSubmit">
        <van-field
          v-model="form.phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[
            { required: true, message: '请输入手机号' },
            { validator: isPhone, message: '请输入正确的手机号' }
          ]"
        />
        <van-field
          v-model="form.code"
          type="digit"
          label="验证码"
          placeholder="请输入6位验证码"
          maxlength="6"
          :rules="[
            { required: true, message: '请输入验证码' },
            { validator: (v: string) => /^\d{6}$/.test(v), message: '请输入6位数字验证码' }
          ]"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              plain
              :disabled="countdown > 0 || sending"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
        <div class="register-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            注册
          </van-button>
        </div>
        <div class="back-login" @click="$router.push('/login')">已有账号？去登录</div>
      </van-form>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import GlassCard from '@/components/GlassCard.vue'
import { isPhone } from '@/utils/validator'
import { authApi } from '@/api/auth'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ phone: '', code: '' })

// 验证码重发倒计时（60s）
const countdown = ref(0)
const sending = ref(false)
let timer: number | null = null

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function startCountdown() {
  countdown.value = 60
  if (timer) clearInterval(timer)
  timer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && timer) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function handleSendCode() {
  if (!isPhone(form.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  sending.value = true
  try {
    // 注册场景（默认 scene=register），后端校验账号不存在才可发
    await authApi.sendSmsCode(form.phone)
    showToast('验证码已发送')
    startCountdown()
  } catch (e) {
    // 发送失败/冷却中/账号已存在，透传后端错误文案
    showToast((e as Error)?.message || '验证码发送失败')
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  loading.value = true
  try {
    await userStore.register(form.phone, form.code)
    showToast('注册成功')
  } catch (e) {
    // 透传后端错误文案（验证码错误/过期/账号已存在等）
    showToast((e as Error)?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  padding: 80px 24px 24px;
  background: $gradient-primary;
}

.register-logo {
  text-align: center;
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $color-primary;
  margin-bottom: 48px;
}

.register-actions {
  margin-top: 32px;
}

.back-login {
  margin-top: 16px;
  text-align: center;
  color: $color-primary;
  font-size: 14px;
  cursor: pointer;
}
</style>
