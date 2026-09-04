<template>
  <div class="login-page">
    <glass-card variant="elevated" class="login-card">
      <h1 class="login-title">Smart Medical</h1>
      <p class="login-subtitle">智慧医疗管理后台</p>
      <a-form :model="form" @finish="handleLogin">
        <a-form-item name="role" :rules="[{ required: true, message: '请选择登录角色' }]">
          <a-select v-model:value="form.role" placeholder="选择角色" size="large">
            <a-select-option value="1">系统管理员</a-select-option>
            <a-select-option value="2">医生</a-select-option>
            <a-select-option value="6">药师</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="phone" :rules="[{ required: true, message: '请输入手机号' }]">
          <a-input v-model:value="form.phone" placeholder="手机号" size="large" />
        </a-form-item>
        <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.password" placeholder="密码" size="large" />
        </a-form-item>
        <a-form-item>
          <a-checkbox v-model:checked="form.remember">记住登录</a-checkbox>
        </a-form-item>
        <a-form-item>
          <CaptchaSlider @verified="captchaVerified = true" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block :loading="loading" :disabled="!captchaVerified">
            登录
          </a-button>
        </a-form-item>
      </a-form>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import GlassCard from '@/components/common/GlassCard.vue'
import CaptchaSlider from '@/components/common/CaptchaSlider.vue'
import { message } from 'ant-design-vue'

const authStore = useAuthStore()
const loading = ref(false)
const captchaVerified = ref(false)
const form = reactive({ phone: '', password: '', remember: false, role: '1' })

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(form.phone, form.password, form.remember, parseInt(form.role))
  } catch (err: any) {
    message.error(err?.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: @spacing-base;
}

.login-card {
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.login-title {
  margin: 0 0 @spacing-sm;
  font-size: @font-size-2xl;
  font-weight: @font-weight-semibold;
  color: @primary-color;
}

.login-subtitle {
  margin: 0 0 @spacing-xl;
  color: @text-color-secondary;
}
</style>
