<template>
  <section class="message-panel">
    <header v-if="session" class="panel-header">
      <LogoAvatar
        :src="session.displayAvatar || session.oppositeAvatar || session.otherUserAvatar || session.avatar || session.companyLogo || session.otherCompanyLogo || session.logo || session.company?.logo || ''"
        :name="otherName"
        :type="session.displayAvatarType || 'company'"
        size="sm"
      />
      <div>
        <strong>{{ otherName }}</strong>
        <span v-if="companyName">{{ companyName }}</span>
        <span>{{ session.displayJobTitle || session.jobTitle || session.title || '岗位沟通' }}</span>
      </div>
    </header>

    <div v-if="!session" class="empty-panel">
      <el-empty description="选择一个会话开始沟通" />
    </div>

    <div v-else ref="scrollRef" class="message-list">
      <el-skeleton v-if="loading" :rows="8" animated />

      <template v-else>
        <div
          v-for="message in messages"
          :key="message.id || `${message.senderId}-${message.createTime}-${message.content}`"
          class="message-row"
          :class="{ mine: isMine(message) }"
        >
          <div class="bubble">
            <p>{{ message.content }}</p>
            <span>{{ message.createTime || '' }}</span>
          </div>
        </div>

        <el-empty v-if="!messages.length" description="暂无聊天记录" />
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import LogoAvatar from '../common/LogoAvatar.vue'

const props = defineProps({
  session: {
    type: Object,
    default: null
  },
  messages: {
    type: Array,
    default: () => []
  },
  currentUserId: {
    type: [String, Number],
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const scrollRef = ref(null)

const otherName = computed(() => {
  const session = props.session || {}
  return session.displayOtherName || session.oppositeName || session.otherUserName || session.seekerName || session.recruiterName || session.companyName || '对方'
})

const companyName = computed(() => props.session?.displayCompanyName || props.session?.companyName || '')

function isMine(message) {
  return Number(message.senderId) === Number(props.currentUserId)
}

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  })
}

watch(
  () => props.messages.length,
  () => scrollToBottom()
)

watch(
  () => props.session,
  () => scrollToBottom()
)

defineExpose({ scrollToBottom })
</script>

<style scoped>
.message-panel {
  display: grid;
  grid-template-rows: 72px minmax(0, 1fr);
  height: 100%;
  background: #f7f9fb;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 24px;
  border-bottom: 1px solid #eef0f2;
  background: #ffffff;
}

.panel-header strong {
  display: block;
  color: #111827;
  font-size: 18px;
}

.panel-header span {
  display: block;
  margin-top: 5px;
  color: #6b7280;
  font-size: 13px;
}

.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-list {
  padding: 24px;
  overflow-y: auto;
}

.message-row {
  display: flex;
  margin-bottom: 16px;
}

.message-row.mine {
  justify-content: flex-end;
}

.bubble {
  max-width: 62%;
  padding: 12px 14px;
  border-radius: 8px;
  color: #374151;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}

.message-row.mine .bubble {
  color: #ffffff;
  background: #00bebd;
}

.bubble p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.bubble span {
  display: block;
  margin-top: 6px;
  color: #9ca3af;
  font-size: 12px;
}

.message-row.mine .bubble span {
  color: rgba(255, 255, 255, 0.78);
}
</style>
