<template>
  <div class="page">
    <glass-card padding="0">
      <van-cell-group inset>
        <van-field v-model="form.oldPassword" type="password" label="原密码" placeholder="请输入原密码" />
        <van-field
          v-model="form.newPassword"
          type="password"
          label="新密码"
          placeholder="至少6位"
          :rules="[{ required: true, message: '请输入新密码' }]"
        />
        <van-field
          v-model="form.confirm"
          type="password"
          label="确认新密码"
          placeholder="再次输入新密码"
          :rules="[{ validator: isValid, message: '两次输入不一致' }]"
        />
      </van-cell-group>
    </glass-card>

    <van-button round block type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
      确认修改
    </van-button>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { showSuccessToast, showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import GlassCard from '@/components/GlassCard.vue'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })

function isValid() {
  return form.newPassword === form.confirm
}

async function handleSubmit() {
  if (!form.oldPassword) {
    showToast('请输入原密码')
    return
  }
  if (form.newPassword !== form.confirm) {
    showToast('两次输入的新密码不一致')
    return
  }
  loading.value = true
  try {
    await authApi.changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    // 后端已清除该账号 token，前端登出回登录页
    await userStore.logoutWithApi()
    showSuccessToast('密码修改成功，请重新登录')
  } catch {
    // 原密码错误等，Interceptor 已弹具体错误
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.submit-btn {
  margin-top: 8px;
}
</style>