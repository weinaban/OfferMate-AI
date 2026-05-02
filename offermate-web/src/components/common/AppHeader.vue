<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link class="logo" to="/">OfferMate AI</router-link>

      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/jobs">找工作</router-link>
        <router-link to="/resumes">我的简历</router-link>
        <router-link to="/deliveries">投递记录</router-link>
        <router-link to="/interviews">我的面试</router-link>
        <el-dropdown trigger="hover" @command="handleAiCommand">
          <span class="ai-menu" :class="{ active: route.path.startsWith('/ai') }">
            AI 求职助手
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="/ai/job-match">AI 岗位匹配</el-dropdown-item>
              <el-dropdown-item command="/ai/interview">AI 模拟面试</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <router-link class="badge-link" to="/chats">
          <el-badge
            :value="chatStore.unreadCount"
            :hidden="chatStore.unreadCount <= 0"
            :max="99"
          >
            <span>消息</span>
          </el-badge>
        </router-link>
        <router-link class="badge-link" to="/notifications">
          <el-badge
            :value="notificationStore.unreadCount"
            :hidden="notificationStore.unreadCount <= 0"
            :max="99"
          >
            <span>通知</span>
          </el-badge>
        </router-link>
        <router-link to="/profile">个人中心</router-link>
      </nav>

      <div class="user-area">
        <LogoAvatar :src="avatar" :name="username" type="user" size="sm" />
        <span class="username">{{ username }}</span>
        <el-button text @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useChatStore } from '../../stores/chat'
import { useNotificationStore } from '../../stores/notification'
import { useUserStore } from '../../stores/user'
import LogoAvatar from './LogoAvatar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()
const notificationStore = useNotificationStore()

const username = computed(() => userStore.userInfo?.username || '求职者')
const avatar = computed(() => userStore.userInfo?.avatar || '')

function handleAiCommand(path) {
  router.push(path)
}

function handleLogout() {
  chatStore.clearUnreadCount()
  notificationStore.clearUnreadCount()
  userStore.logout()
  router.replace('/login')
}

function refreshBadges() {
  if (route.path !== '/chats') {
    chatStore.fetchUnreadCount()
  }
  notificationStore.fetchUnreadCount()
}

onMounted(() => {
  refreshBadges()
})

watch(
  () => route.fullPath,
  () => refreshBadges()
)
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 64px;
  border-bottom: 1px solid #eef0f2;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: 1500px;
  height: 64px;
  margin: 0 auto;
  padding: 0 24px;
}

.logo {
  color: #00a7a6;
  font-size: 24px;
  font-weight: 800;
  white-space: nowrap;
}

.nav {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-left: 36px;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.nav a,
.ai-menu {
  color: #1f2937;
  cursor: pointer;
  transition: color 0.2s ease;
}

.nav a:hover,
.nav a.router-link-active,
.ai-menu:hover,
.ai-menu.active {
  color: #00bebd;
}

.badge-link {
  display: inline-flex;
  align-items: center;
}

.badge-link :deep(.el-badge__content) {
  transform: translate(75%, -45%);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.user-area :deep(.logo-avatar) {
  width: 34px;
  height: 34px;
  font-size: 15px;
}

.username {
  color: #374151;
  font-size: 14px;
}
</style>
