<template>
  <div class="page">
    <van-nav-bar title="个人信息" left-arrow @click-left="$router.back()" />
    <glass-card class="card">
      <van-form @submit="onSubmit">
        <van-field
          v-model="form.username"
          label="姓名"
          placeholder="请输入姓名"
          :rules="[{ required: true, message: '请输入姓名' }]"
        />
        <van-field
          v-model="form.idCard"
          label="身份证号"
          placeholder="请输入身份证号"
          :rules="idCardRules"
        />
        <van-field name="sex" label="性别" :rules="sexRules">
          <template #input>
            <van-radio-group v-model="form.sex" direction="horizontal">
              <van-radio :name="0">男</van-radio>
              <van-radio :name="1">女</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
          v-model="form.address"
          label="家庭住址"
          type="textarea"
          rows="2"
          autosize
          maxlength="255"
          show-word-limit
          placeholder="请输入家庭住址（选填）"
        />
        <div class="action">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            保存
          </van-button>
        </div>
      </van-form>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import GlassCard from '@/components/GlassCard.vue'
import { getUserProfile, updateUserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { isIdCard } from '@/utils/validator'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)

const idCardRules = [
  { required: true, message: '请输入身份证号' },
  { validator: isIdCard, message: '身份证号格式不正确' }
]
const sexRules = [{ required: true, message: '请选择性别' }]

const form = reactive<{
  username: string
  idCard: string
  sex: number | null
  address: string
}>({
  username: '',
  idCard: '',
  sex: null,
  address: ''
})

onMounted(loadProfile)

async function loadProfile() {
  try {
    const res = await getUserProfile()
    form.username = res.data?.username || ''
    form.idCard = res.data?.idCard || ''
    form.sex = res.data?.sex ?? null
    form.address = res.data?.address || ''
  } catch {
    showToast('个人信息加载失败')
  }
}

async function onSubmit() {
  if (form.sex === null) return
  submitting.value = true
  try {
    const res = await updateUserProfile({
      username: form.username,
      idCard: form.idCard,
      sex: form.sex,
      address: form.address
    })
    userStore.applyProfile(res.data)
    showToast('个人信息已保存')
    await router.replace('/profile')
  } catch (e: any) {
    showToast(e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $color-bg-page;
}

.card {
  margin: 16px;
}

.action {
  margin-top: 24px;
}
</style>
