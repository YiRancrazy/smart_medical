<template>
  <div class="forgot-page">
    <div class="forgot-logo">智慧医疗</div>
    <glass-card class="forgot-card">
      <van-form @submit="handleSubmit">
        <van-field
          v-model="form.phone"
          label="手机号"
          placeholder="请输入注册手机号"
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
        <van-field
          v-model="form.password"
          type="password"
          label="新密码"
          placeholder="请设置新密码（至少6位）"
          :rules="[{ required: true, message: '请输入新密码' }]"
        />
        <div class="forgot-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            重置密码
          </van-button>
        </div>
        <div class="back-login" @click="$router.push('/login')">返回登录</div>
      </van-form>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue'
import { showSuccessToast, showToast } from 'vant'
import { useRouter } from 'vue-router'
import { isPhone } from '@/utils/validator'
import { authApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
let timer: number | null = null
const form = reactive({ phone: '', code: '', password: '' })

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
    // 忘记密码场景（scene=forgot），后端校验账号已存在才可发
    await authApi.sendSmsCode(form.phone, 'forgot')
    showToast('验证码已发送')
    startCountdown()
  } catch (e) {
    showToast((e as Error)?.message || '验证码发送失败')
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  loading.value = true
  try {
    await authApi.forgotPassword({ phone: form.phone, code: form.code, password: form.password })
    showSuccessToast('密码已重置，请重新登录')
    router.replace('/login')
  } catch (e) {
    showToast((e as Error)?.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.forgot-page {
  min-height: 100vh;
  padding: 80px 24px 24px;
  background: $gradient-primary;
}

.forgot-logo {
  text-align: center;
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $color-primary;
  margin-bottom: 48px;
}

.forgot-actions {
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