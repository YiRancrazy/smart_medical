<template>
  <div class="page">
    <van-nav-bar :title="pageTitle" left-arrow @click-left="$router.back()" />
    <glass-card class="card">
      <van-form @submit="onSubmit">
        <van-field v-model="form.name" label="姓名" placeholder="请输入姓名" :rules="nameRules" />
        <van-field v-model="form.idCard" label="身份证号" placeholder="请输入身份证号" :rules="idCardRules" />
        <van-cell v-if="isSelf && cardSn" title="就诊卡号" :value="cardSn" />
        <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" :rules="phoneRules" />
        <van-field
          v-model="relationText"
          label="关系"
          placeholder="请选择关系"
          :is-link="!isSelf"
          readonly
          @click="!isSelf && (showRelationPicker = true)"
          :rules="[{ required: true, message: '请选择关系' }]"
        />
        <van-field v-model="form.remark" label="备注" placeholder="请输入备注" />
        <van-field label="设为默认">
          <template #input>
            <van-switch v-model="form.defaulted" />
          </template>
        </van-field>
        <div class="action">
          <van-button round block type="primary" native-type="submit" :loading="submitting">保存</van-button>
        </div>
      </van-form>
    </glass-card>

    <van-popup v-model:show="showRelationPicker" round position="bottom">
      <van-picker :columns="relations" :model-value="[form.relation]" @confirm="onRelationConfirm" @cancel="showRelationPicker = false" />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePatientStore } from '@/stores/patient'
import { addPatient, updatePatient, getPatientDetail, getSelfPatientCardStatus } from '@/api/patient'
import GlassCard from '@/components/GlassCard.vue'
import { showToast } from 'vant'
import { isPhone, isIdCard } from '@/utils/validator'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const patientStore = usePatientStore()
const submitting = ref(false)
const showRelationPicker = ref(false)
// L5: vue-router 的 query.id 可能为 string[],规范化取首个字符串值
const rawId = route.query.id
const relationId = ref<string | null>(
  Array.isArray(rawId) ? (rawId[0] || null) : (typeof rawId === 'string' ? rawId : null)
)
const isEdit = computed(() => !!relationId.value)
const isSelf = computed(() => route.query.self === '1')
const pageTitle = computed(() => (
  isSelf.value ? '完善本人就诊卡信息' : (isEdit.value ? '编辑就诊人' : '添加就诊人')
))
const cardSn = ref('')

const nameRules = [{ required: true, message: '请输入姓名' }]
// U06: 补身份证与手机号格式校验，避免脏数据提交后端
const idCardRules = [
  { required: true, message: '请输入身份证号' },
  { validator: isIdCard, message: '身份证号格式不正确' }
]
const phoneRules = [
  { required: true, message: '请输入手机号' },
  { validator: isPhone, message: '手机号格式不正确' }
]

// 选项必须带 value：Vant Picker 默认按 value 字段比对选中项，缺失会导致任何选择都回落到第一项
const relations = [
  { text: '本人', value: '本人' },
  { text: '配偶', value: '配偶' },
  { text: '子女', value: '子女' },
  { text: '父母', value: '父母' },
  { text: '其他', value: '其他' }
]

const form = reactive({
  name: '',
  idCard: '',
  phone: '',
  relation: '',
  remark: '',
  defaulted: false
})

const relationText = computed(() => form.relation)

onMounted(() => {
  if (isSelf.value) {
    loadSelfPatientCard()
  } else if (isEdit.value) {
    loadPatient()
  }
})

async function loadSelfPatientCard() {
  try {
    const res = await getSelfPatientCardStatus()
    const item = res.data
    if (!item) return
    relationId.value = item.relationId
    form.name = item.patientName || userStore.userInfo?.name || ''
    form.idCard = item.patientIdCard || ''
    form.phone = item.patientPhone || userStore.userInfo?.phone || ''
    form.relation = '本人'
    form.remark = item.remark || ''
    form.defaulted = item.defaultPatient
    cardSn.value = item.patientCardSn || ''
  } catch {
    showToast('本人就诊卡信息加载失败')
  }
}

async function loadPatient() {
  try {
    // F24: 用详情接口替代拉全列表 find by id；同时回填 remark（U06）
    const res = await getPatientDetail(relationId.value!)
    const item = res.data
    if (!item) {
      showToast('就诊人不存在')
      return
    }
    form.name = item.patientName || ''
    form.idCard = item.patientIdCard || ''
    form.phone = item.patientPhone || ''
    form.relation = item.relation || ''
    form.remark = item.remark || ''
    form.defaulted = !!item.defaultPatient
  } catch {
    showToast('加载就诊人失败')
  }
}

function onRelationConfirm({ selectedOptions }: any) {
  form.relation = selectedOptions[0]?.text || selectedOptions[0] || ''
  showRelationPicker.value = false
}

async function onSubmit() {
  if (!userStore.uid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePatient({
        id: relationId.value!,
        name: form.name,
        idCard: form.idCard,
        phone: form.phone,
        relation: form.relation,
        remark: form.remark,
        defaulted: form.defaulted ? '1' : '0'
      })
    } else {
      await addPatient({
        name: form.name,
        idCard: form.idCard,
        phone: form.phone,
        relation: form.relation,
        remark: form.remark,
        defaulted: form.defaulted ? '1' : '0'
      })
    }
    await patientStore.loadPatients()
    if (isSelf.value) {
      await userStore.ensureOwnPatientCardCompleted(true)
      showToast('本人就诊卡信息已保存')
      await router.replace('/profile')
      return
    }
    showToast(isEdit.value ? '保存成功' : '添加成功')
    router.back()
  } catch (e: any) {
    showToast(e?.message || (isEdit.value ? '保存失败' : '添加失败'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 24px;
}

.card {
  margin: 16px;
}

.action {
  margin-top: 24px;
}
</style>
