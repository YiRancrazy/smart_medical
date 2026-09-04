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
import { reactive, ref } from 'vue'
import { showSuccessToast, showToast } from 'vant'
import { useRouter } from 'vue-router'
import { isPhone } from '@/utils/validator'
import { authApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

async function handleSubmit() {
  loading.value = true
  try {
    await authApi.forgotPassword(form)
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