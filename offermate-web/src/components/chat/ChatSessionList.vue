<template>
  <aside class="session-list">
    <div class="session-header">
      <h2>消息</h2>
      <span>{{ sessions.length }} 个会话</span>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />

    <div v-else-if="sessions.length" class="sessions">
      <button
        v-for="session in sessions"
        :key="getSessionId(session)"
        class="session-item"
        :class="{
          active: Number(activeId) === Number(getSessionId(session)),
          'has-unread': getUnreadCount(session) > 0
        }"
        type="button"
        @click="$emit('select', session)"
      >
        <LogoAvatar
          :src="getAvatar(session)"
          :name="getOtherName(session)"
          :type="session.displayAvatarType || 'company'"
          size="sm"
        />
        <div class="session-main">
          <div class="session-top">
            <strong>{{ getOtherName(session) }}</strong>
            <span>{{ session.updateTime || '' }}</span>
          </div>
          <p v-if="getCompanyLine(session)" class="company-line">{{ getCompanyLine(session) }}</p>
          <p class="job-title">{{ getJobTitle(session) }}</p>
          <p class="last-message">{{ session.lastMessage || '暂无消息' }}</p>
        </div>
        <em v-if="getUnreadCount(session) > 0">{{ formatUnreadCount(session) }}</em>
      </button>
    </div>

    <el-empty v-else description="暂无会话" />
  </aside>
</template>

<script setup>
import LogoAvatar from '../common/LogoAvatar.vue'

defineProps({
  sessions: {
    type: Array,
    default: () => []
  },
  activeId: {
    type: [String, Number],
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select'])

function getSessionId(session) {
  return session.id || session.sessionId
}

function getOtherName(session) {
  return session.displayOtherName || session.oppositeName || session.otherUserName || session.seekerName || session.recruiterName || session.companyName || '对方'
}

function getJobTitle(session) {
  return session.displayJobTitle || session.jobTitle || session.title || '岗位沟通'
}

function getCompanyLine(session) {
  return session.displayCompanyName || session.companyName || ''
}

function getAvatar(session) {
  return session.displayAvatar || session.oppositeAvatar || session.otherUserAvatar || session.avatar || session.companyLogo || session.otherCompanyLogo || session.logo || session.company?.logo || ''
}

function getUnreadCount(session) {
  return Number(session.unreadCount || 0)
}

function formatUnreadCount(session) {
  const count = getUnreadCount(session)
  return count > 99 ? '99+' : count
}
</script>

<style scoped>
.session-list {
  height: 100%;
  border-right: 1px solid #eef0f2;
  background: #ffffff;
}

.session-header {
  height: 72px;
  padding: 18px 20px;
  border-bottom: 1px solid #eef0f2;
}

.session-header h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
}

.session-header span {
  display: block;
  margin-top: 4px;
  color: #9ca3af;
  font-size: 13px;
}

.sessions {
  height: calc(100% - 72px);
  overflow-y: auto;
}

.session-item {
  position: relative;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  width: 100%;
  padding: 16px 18px;
  border: none;
  border-bottom: 1px solid #f3f4f6;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.session-item:hover,
.session-item.active {
  background: #e9fbfb;
}

.session-item.has-unread .last-message {
  padding-right: 40px;
  color: #374151;
  font-weight: 600;
}

.session-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.session-top strong {
  overflow: hidden;
  color: #111827;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-top span {
  flex: 0 0 auto;
  color: #9ca3af;
  font-size: 12px;
}

.company-line,
.job-title,
.last-message {
  overflow: hidden;
  margin: 5px 0 0;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.company-line {
  color: #4b5563;
  font-size: 13px;
}

.job-title {
  color: #00a7a6;
  font-size: 13px;
}

.last-message {
  color: #6b7280;
  font-size: 13px;
}

em {
  position: absolute;
  right: 14px;
  bottom: 18px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  color: #ffffff;
  background: #ff4d4f;
  font-size: 12px;
  font-style: normal;
  line-height: 20px;
  text-align: center;
  box-shadow: 0 4px 10px rgba(255, 77, 79, 0.24);
}
</style>
