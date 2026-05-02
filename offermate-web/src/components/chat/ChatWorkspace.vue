<template>
  <main class="chat-page">
    <section class="chat-card">
      <ChatSessionList
        :sessions="sessions"
        :active-id="activeSessionId"
        :loading="sessionLoading"
        @select="selectSession"
      />

      <div class="chat-main">
        <ChatMessagePanel
          ref="messagePanelRef"
          :session="activeSession"
          :messages="messages"
          :current-user-id="currentUserId"
          :loading="messageLoading"
        />
        <ChatInputBox
          :loading="sending"
          :disabled="!activeSession"
          @send="sendMessage"
        />
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getChatMessages, getChatSessions, markSessionRead } from '../../api/chat'
import { getJobDetail } from '../../api/job'
import { useChatStore } from '../../stores/chat'
import { useUserStore } from '../../stores/user'
import { createChatWebSocket } from '../../utils/websocket'
import ChatInputBox from './ChatInputBox.vue'
import ChatMessagePanel from './ChatMessagePanel.vue'
import ChatSessionList from './ChatSessionList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const sessions = ref([])
const messages = ref([])
const activeSession = ref(null)
const sessionLoading = ref(false)
const messageLoading = ref(false)
const sending = ref(false)
const messagePanelRef = ref(null)
const wsClient = ref(null)
const jobTitleCache = new Map()

const activeSessionId = computed(() => getSessionId(activeSession.value))
const currentUserId = computed(() => userStore.userInfo?.userId)

function syncChatUnreadCount() {
  chatStore.setUnreadCount(
    sessions.value.reduce((total, session) => total + Number(session.unreadCount || 0), 0)
  )
}

function getSessionId(session) {
  return session?.id || session?.sessionId
}

function normalizeList(data) {
  return Array.isArray(data) ? data : data?.records || []
}

function normalizeMessage(rawMessage) {
  const message = rawMessage?.data && typeof rawMessage.data === 'object' ? rawMessage.data : rawMessage

  return {
    ...message,
    id: message.id || message.messageId,
    sessionId: message.sessionId || message.chatSessionId || message.chatId || message.conversationId || message.session?.id,
    senderId: message.senderId || message.fromUserId || message.fromId || message.userId,
    receiverId: message.receiverId || message.toUserId || message.toId,
    content: message.content || message.message || message.text || '',
    createTime: message.createTime || message.sendTime || message.createdAt || new Date().toLocaleString(),
    msgType: message.msgType || message.type || 1
  }
}

function normalizeSocketMessage(rawMessage) {
  if (!rawMessage) {
    return null
  }

  if (typeof rawMessage === 'string') {
    try {
      return normalizeMessage(JSON.parse(rawMessage))
    } catch (error) {
      return null
    }
  }

  return normalizeMessage(rawMessage)
}

function getFallbackOtherName(session) {
  const role = Number(userStore.userInfo?.role)

  if (role === 1) {
    return session.oppositeName || session.recruiterName || session.otherUserName || session.companyName || (session.recruiterId ? `招聘者 #${session.recruiterId}` : '招聘者')
  }

  return session.oppositeName || session.seekerName || session.realName || session.otherUserName || session.username || (session.seekerId ? `求职者 #${session.seekerId}` : '求职者')
}

async function getJobInfo(session) {
  const existingTitle = session.jobTitle || session.title

  if (!session.jobId) {
    return { title: existingTitle || '岗位沟通' }
  }

  if (jobTitleCache.has(session.jobId)) {
    return jobTitleCache.get(session.jobId)
  }

  try {
    const job = await getJobDetail(session.jobId)
    const info = {
      title: existingTitle || job?.title || `岗位 #${session.jobId}`,
      companyName: session.companyName || job?.companyName,
      companyLogo: session.companyLogo || session.logo || session.company?.logo || job?.companyLogo || job?.logo || job?.company?.logo
    }
    jobTitleCache.set(session.jobId, info)
    return info
  } catch (error) {
    const info = { title: existingTitle || `岗位 #${session.jobId}` }
    jobTitleCache.set(session.jobId, info)
    return info
  }
}

async function enrichSessions(list) {
  return Promise.all(
    list.map(async (session) => {
      const role = Number(userStore.userInfo?.role)
      const jobInfo = await getJobInfo(session)

      return {
        ...session,
        otherUserId: session.otherUserId || session.oppositeUserId,
        displayOtherName: getFallbackOtherName(session),
        displayJobTitle: jobInfo.title,
        displayCompanyName: role === 1 ? (session.companyName || jobInfo.companyName || '') : '求职者',
        displayAvatar: role === 1
          ? (session.oppositeAvatar || session.companyLogo || session.otherCompanyLogo || session.logo || session.company?.logo || jobInfo.companyLogo || '')
          : (session.oppositeAvatar || session.avatar || session.otherUserAvatar || session.seekerAvatar || ''),
        displayAvatarType: role === 1 ? 'company' : 'user'
      }
    })
  )
}

function isDuplicateMessage(list, message) {
  return list.some((item) => {
    if (message.id && item.id && Number(message.id) === Number(item.id)) {
      return true
    }

    if (
      String(item.id || '').startsWith('local-') &&
      Number(item.senderId) === Number(message.senderId) &&
      item.content === message.content
    ) {
      return true
    }

    return (
      Number(item.senderId) === Number(message.senderId) &&
      item.content === message.content &&
      item.createTime === message.createTime
    )
  })
}

async function fetchSessions(preferredSessionId = route.query.sessionId) {
  sessionLoading.value = true

  try {
    const latestSessions = await enrichSessions(normalizeList(await getChatSessions()))
    sessions.value = latestSessions.map((session) => {
      if (activeSessionId.value && Number(getSessionId(session)) === Number(activeSessionId.value)) {
        return {
          ...session,
          unreadCount: 0
        }
      }

      return session
    })
    const targetSession = sessions.value.find((session) => Number(getSessionId(session)) === Number(preferredSessionId))
    const firstSession = sessions.value[0]

    if (!activeSession.value && (targetSession || firstSession)) {
      await selectSession(targetSession || firstSession, false)
    } else if (activeSession.value) {
      const latestActive = sessions.value.find((session) => Number(getSessionId(session)) === Number(activeSessionId.value))
      if (latestActive) {
        activeSession.value = latestActive
      }
    }
    syncChatUnreadCount()
  } finally {
    sessionLoading.value = false
  }
}

function clearSessionUnread(sessionId) {
  sessions.value = sessions.value.map((session) => {
    if (Number(getSessionId(session)) === Number(sessionId)) {
      return {
        ...session,
        unreadCount: 0
      }
    }

    return session
  })

  if (activeSession.value && Number(getSessionId(activeSession.value)) === Number(sessionId)) {
    activeSession.value = {
      ...activeSession.value,
      unreadCount: 0
    }
  }
  syncChatUnreadCount()
}

async function selectSession(session, updateUrl = true) {
  activeSession.value = {
    ...session,
    unreadCount: 0
  }
  const sessionId = getSessionId(session)
  clearSessionUnread(sessionId)

  if (updateUrl) {
    router.replace({
      path: '/chats',
      query: { sessionId }
    })
  }

  messageLoading.value = true

  try {
    messages.value = normalizeList(await getChatMessages(sessionId)).map(normalizeMessage)
    await markSessionRead(sessionId)
    clearSessionUnread(sessionId)
    nextTick(() => messagePanelRef.value?.scrollToBottom())
  } finally {
    messageLoading.value = false
  }
}

function getReceiverId() {
  const session = activeSession.value || {}
  const role = Number(userStore.userInfo?.role)
  return session.otherUserId || session.oppositeUserId || (role === 1 ? session.recruiterId : session.seekerId)
}

function appendMessage(message) {
  const normalized = normalizeMessage(message)

  if (!isDuplicateMessage(messages.value, normalized)) {
    messages.value.push(normalized)
  }
}

async function sendMessage(content) {
  if (!activeSession.value || sending.value) {
    return
  }

  const receiverId = getReceiverId()

  if (!receiverId) {
    ElMessage.error('暂时无法识别接收方')
    return
  }

  const payload = {
    sessionId: Number(activeSessionId.value),
    receiverId: Number(receiverId),
    content,
    msgType: 1
  }

  sending.value = true

  try {
    const sent = wsClient.value?.send(payload)

    if (!sent) {
      ElMessage.error('连接已断开，正在重连，请稍后再试')
      return
    }

    appendMessage({
      ...payload,
      id: `local-${Date.now()}`,
      senderId: currentUserId.value,
      createTime: new Date().toLocaleString()
    })

    activeSession.value.lastMessage = content
    activeSession.value.updateTime = '刚刚'
  } finally {
    sending.value = false
  }
}

async function handleSocketMessage(message) {
  const normalizedMessage = normalizeSocketMessage(message)

  if (!normalizedMessage || ![1, '1', 'text'].includes(normalizedMessage.msgType || 1)) {
    return
  }

  if (Number(normalizedMessage.sessionId) === Number(activeSessionId.value)) {
    appendMessage(normalizedMessage)
    await markSessionRead(activeSessionId.value)
    clearSessionUnread(activeSessionId.value)
    nextTick(() => messagePanelRef.value?.scrollToBottom())
  } else {
    const session = sessions.value.find((item) => Number(getSessionId(item)) === Number(normalizedMessage.sessionId))
    if (session) {
      session.lastMessage = normalizedMessage.content
      session.unreadCount = Number(session.unreadCount || 0) + 1
      syncChatUnreadCount()
      session.updateTime = message.createTime || '刚刚'
    }
  }

  fetchSessions(activeSessionId.value)
}

function setupWebSocket() {
  wsClient.value = createChatWebSocket({
    onMessage: handleSocketMessage,
    onClose: () => {
      ElMessage.warning('聊天连接已断开，正在尝试重连')
    },
    onError: () => {
      ElMessage.error('聊天连接异常')
    }
  })
  wsClient.value.connect()
}

onMounted(() => {
  fetchSessions()
  setupWebSocket()
})

onBeforeUnmount(() => {
  wsClient.value?.close()
})
</script>

<style scoped>
.chat-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  margin: 0;
  padding: 0;
}

.chat-card {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  border: none;
  border-top: 1px solid #eef0f2;
  border-radius: 0;
  background: #ffffff;
  box-shadow: none;
}

.chat-main {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  min-width: 0;
  min-height: 0;
}
</style>
