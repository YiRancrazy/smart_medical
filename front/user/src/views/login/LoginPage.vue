<template>
  <div class="login-page">
    <div class="login-logo">智慧医疗</div>
    <glass-card class="login-card">
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
          v-model="form.password"
          type="password"
          label="密码"
          :placeholder="isRegister ? '请设置密码' : '请输入密码'"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <div class="login-actions">
          <div class="login-captcha">
            <CaptchaSlider :key="capKey" @verified="captchaVerified = true" />
          </div>
          <van-button round block type="primary" native-type="submit" :loading="loading" :disabled="!captchaVerified">
            {{ isRegister ? '注册' : '登录' }}
          </van-button>
        </div>
        <div class="switch-mode" @click="toggleMode">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </div>
      </van-form>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import GlassCard from '@/components/GlassCard.vue'
import CaptchaSlider from '@/components/CaptchaSlider.vue'
import { isPhone } from '@/utils/validator'

const userStore = useUserStore()
const loading = ref(false)
const isRegister = ref(false)
const captchaVerified = ref(false)
// 切换登录/注册时重建滑块组件以强制重新验证
const capKey = ref(0)
const form = reactive({ phone: '', password: '' })

function toggleMode() {
  isRegister.value = !isRegister.value
  captchaVerified.value = false
  capKey.value++
}

async function handleSubmit() {
  loading.value = true
  try {
    if (isRegister.value) {
      await userStore.register(form.phone, form.password)
      showToast('注册成功')
    } else {
      await userStore.login(form.phone, form.password)
    }
  } catch (e) {
    // U04: 透传后端错误文案，让用户知道是密码错还是账号不存在
    showToast((e as Error)?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
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

.login-actions {
  margin-top: 32px;
}

.login-captcha {
  margin-bottom: 24px;
}

.switch-mode {
  margin-top: 16px;
  text-align: center;
  color: $color-primary;
  font-size: 14px;
  cursor: pointer;
}
</style>
