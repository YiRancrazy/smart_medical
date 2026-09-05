<template>
  <div class="chat-page">
    <van-nav-bar :title="doctorName" left-arrow @click-left="router.back()" />
    <div ref="scrollRef" class="message-list">
      <van-loading v-if="loading" class="loading" size="24px" />
      <empty-state v-else-if="!messages.length" description="暂无消息，发一句开始咨询吧" />
      <template v-else>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="isMine(msg) ? 'mine' : 'theirs'"
        >
          <div class="bubble">
            <img v-if="msg.contentType === 1" class="img" :src="msg.content" alt="图片" />
            <span v-else>{{ msg.content }}</span>
          </div>
          <div class="time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </template>
    </div>

    <div class="input-bar">
      <input
        v-model="input"
        class="input"
        type="text"
        placeholder="请输入内容"
        @keyup.enter="send"
      />
      <van-button type="primary" size="small" :loading="sending" @click="send">发送</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import EmptyState from '@/components/EmptyState.vue'
import { useUserStore } from '@/stores/user'
import { getChatHistory, sendChatText } from '@/api/chat'
import type { ChatRecord } from '@/api/chat'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const doctorId = route.params.doctorId as string
const doctorName = (route.query.doctorName as string) || '在线咨询'
const uid = userStore.uid

const scrollRef = ref<HTMLElement | null>(null)
const messages = ref<ChatRecord[]>([])
const input = ref('')
const loading = ref(false)
const sending = ref(false)

function isMine(msg: ChatRecord) {
  return String(msg.sendId) === String(uid)
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(5, 16)
}

async function loadHistory() {
  loading.value = true
  try {
    const res = await getChatHistory(doctorId)
    messages.value = res.data?.list || []
    await scrollToBottom()
  } catch (e) {
    showToast((e as Error)?.message || '加载聊天记录失败')
  } finally {
    loading.value = false
  }
}

async function scrollToBottom() {
  await nextTick()
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  }
}

async function send() {
  const content = input.value.trim()
  if (!content) return
  sending.value = true
  try {
    const res = await sendChatText(doctorId, content)
    if (res.data) {
      messages.value.push(res.data)
      input.value = ''
      await scrollToBottom()
    }
  } catch (e) {
    showToast((e as Error)?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

onMounted(loadHistory)
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;

  .loading {
    margin: 40px auto;
  }
}

.message-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;

  &.mine {
    align-items: flex-end;
  }

  &.theirs {
    align-items: flex-start;
  }

  .bubble {
    max-width: 72%;
    padding: 10px 14px;
    border-radius: 8px;
    line-height: 1.5;
    word-break: break-word;
    background: #fff;
    color: #333;

    .img {
      display: block;
      max-width: 180px;
      border-radius: 4px;
    }
  }

  &.mine .bubble {
    background: $color-primary;
    color: #fff;
  }

  .time {
    font-size: 11px;
    color: #999;
    margin-top: 4px;
  }
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-top: 1px solid #eee;

  .input {
    flex: 1;
    height: 36px;
    border: 1px solid #ddd;
    border-radius: 18px;
    padding: 0 14px;
    outline: none;
    font-size: 14px;
  }
}
</style>