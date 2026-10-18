<template>
  <div class="page">
    <van-nav-bar title="个人信息" left-arrow @click-left="$router.back()" />
    <div class="card surface-card">
      <van-form @submit="onSubmit">
        <van-field label="头像">
          <template #input>
            <van-uploader
              :after-read="handleAvatarRead"
              :before-read="beforeAvatarRead"
              accept="image/*"
              :max-count="1"
              :preview-image="false"
              :disabled="uploading"
            >
              <van-image v-if="form.avatar" round width="56" height="56" :src="form.avatar" />
              <div v-else class="avatar-upload-placeholder">
                <van-icon name="plus" />
              </div>
            </van-uploader>
          </template>
        </van-field>
        <van-field
          v-model="form.nickname"
          label="昵称"
          placeholder="请输入昵称"
          maxlength="64"
          :rules="[{ required: true, message: '请输入昵称' }]"
        />
        <van-field
          v-model="form.username"
          label="姓名"
          placeholder="请输入姓名"
          :rules="[{ required: true, message: '请输入姓名' }]"
        />
        <van-field :model-value="maskPhone(form.phone) || '-'" label="手机号" readonly>
          <template #button>
            <van-button size="small" type="primary" plain @click="goChangePhone">
              更换
            </van-button>
          </template>
        </van-field>
        <van-field
          v-model="form.idCard"
          label="身份证号"
          placeholder="请输入身份证号"
          :rules="idCardRules"
        />
        <van-field :model-value="ageText" label="年龄" readonly />
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserProfile, updateUserProfile, uploadUserAvatar } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { isIdCard } from '@/utils/validator'
import { calculateAgeByIdCard, maskPhone } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const uploading = ref(false)

const idCardRules = [
  { required: true, message: '请输入身份证号' },
  { validator: isIdCard, message: '身份证号格式不正确' }
]
const sexRules = [{ required: true, message: '请选择性别' }]

const form = reactive<{
  nickname: string
  username: string
  avatar: string
  phone: string
  idCard: string
  sex: number | null
  address: string
}>({
  nickname: '',
  username: '',
  avatar: '',
  phone: '',
  idCard: '',
  sex: null,
  address: ''
})
const ageText = computed(() => {
  const age = calculateAgeByIdCard(form.idCard)
  return age === null ? '-' : `${age}岁`
})

onMounted(loadProfile)

async function loadProfile() {
  try {
    const res = await getUserProfile()
    form.nickname = res.data?.nickname || ''
    form.username = res.data?.username || ''
    form.avatar = res.data?.avatar || ''
    form.phone = res.data?.phone || ''
    form.idCard = res.data?.idCard || ''
    form.sex = res.data?.sex ?? null
    form.address = res.data?.address || ''
  } catch {
    showToast('个人信息加载失败')
  }
}

function beforeAvatarRead(file: File | File[]) {
  const target = Array.isArray(file) ? file[0] : file
  if (!target.type.startsWith('image/')) {
    showToast('仅支持图片文件')
    return false
  }
  if (target.size > 5 * 1024 * 1024) {
    showToast('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleAvatarRead(item: any) {
  const file = (Array.isArray(item) ? item[0]?.file : item?.file) as File | undefined
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadUserAvatar(file)
    form.avatar = res.data
    if (userStore.profile) {
      userStore.applyProfile({ ...userStore.profile, avatar: res.data })
    }
    showToast('头像已更新')
  } catch {
    // 上传失败由拦截器展示后端错误
  } finally {
    uploading.value = false
  }
}

function goChangePhone() {
  router.push('/profile/change-phone')
}

async function onSubmit() {
  if (form.sex === null) return
  submitting.value = true
  try {
    const res = await updateUserProfile({
      username: form.username,
      nickname: form.nickname,
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
  min-height: 100dvh;
  background: $color-bg-page;
}

.card {
  margin: 16px;
}

.avatar-upload-placeholder {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed $color-border;
  border-radius: 50%;
  color: $color-text-secondary;
  font-size: 24px;
}

.action {
  margin-top: 24px;
}
</style>
