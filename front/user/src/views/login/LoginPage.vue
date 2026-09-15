<template>
  <div class="login-page">
    <div class="login-logo">智慧医疗</div>
    <glass-card class="login-card">
      <van-form @submit="handleSubmit">
        <van-tabs v-model:active="mode" class="login-tabs">
          <van-tab title="密码登录" name="password" />
          <van-tab title="验证码登录" name="code" />
        </van-tabs>
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
          v-if="mode === 'password'"
          v-model="form.password"
          type="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <van-field
          v-else
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
        <div class="login-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            {{ submitText }}
          </van-button>
        </div>
        <div class="switch-mode" @click="$router.push('/register')">
          没有账号？去注册
        </div>
        <div class="forgot-link" v-if="mode === 'password'" @click="$router.push('/forgot-password')">忘记密码？</div>
      </van-form>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import GlassCard from '@/components/GlassCard.vue'
import { isPhone } from '@/utils/validator'
import { authApi } from '@/api/auth'

const userStore = useUserStore()
const loading = ref(false)
// 登录模式：password 密码登录 / code 验证码登录（注册已独立为 /register 页面）
const mode = ref<'password' | 'code'>('password')
const form = reactive({ phone: '', password: '', code: '' })

// 验证码重发倒计时（60s）
const countdown = ref(0)
const sending = ref(false)
let timer: number | null = null

const submitText = computed(() => {
  return mode.value === 'code' ? '验证码登录' : '登录'
})

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
    // 验证码登录场景（scene=login），后端校验账号已存在才可发
    await authApi.sendSmsCode(form.phone, 'login')
    showToast('验证码已发送')
    startCountdown()
  } catch (e) {
    // 发送失败/冷却中/账号不存在，透传后端错误文案
    showToast((e as Error)?.message || '验证码发送失败')
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  loading.value = true
  try {
    if (mode.value === 'code') {
      await userStore.loginByCode(form.phone, form.code)
    } else {
      await userStore.login(form.phone, form.password)
    }
  } catch (e) {
    // 透传后端错误文案（验证码错误/过期/账号不存在等）
    showToast((e as Error)?.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100dvh;
  padding: 80px 24px 24px;
  background: $gradient-primary;
}

.login-logo {
  text-align: center;
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $color-primary;
  margin-bottom: 48px;
}

.login-tabs {
  margin-bottom: 16px;
}

.login-actions {
  margin-top: 32px;
}

.switch-mode {
  margin-top: 16px;
  text-align: center;
  color: $color-primary;
  font-size: 14px;
  cursor: pointer;
}

.forgot-link {
  margin-top: 12px;
  text-align: center;
  color: $color-text-secondary;
  font-size: 13px;
  cursor: pointer;
}
</style>
