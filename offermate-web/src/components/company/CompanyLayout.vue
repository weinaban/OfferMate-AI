<template>
  <div class="company-layout">
    <CompanySidebar />

    <section class="main">
      <header class="topbar">
        <div>
          <strong>OfferMate AI 企业端</strong>
          <span>招聘者后台</span>
        </div>

        <div class="user-area">
          <span>{{ username }}</span>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="content" :class="{ 'is-edge-content': isEdgeContentPage }">
        <slot />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useChatStore } from '../../stores/chat'
import { useNotificationStore } from '../../stores/notification'
import { useUserStore } from '../../stores/user'
import CompanySidebar from './CompanySidebar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const notificationStore = useNotificationStore()

const username = computed(() => userStore.userInfo?.username || '招聘者')
const isEdgeContentPage = computed(() => ['/chats', '/notifications'].includes(route.path))

function handleLogout() {
  chatStore.clearUnreadCount()
  notificationStore.clearUnreadCount()
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.company-layout {
  min-width: 1200px;
  min-height: 100vh;
  background: #f5f7fa;
}

.main {
  min-height: 100vh;
  margin-left: 220px;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68px;
  padding: 0 32px;
  border-bottom: 1px solid #e8ecef;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
}

.topbar strong {
  display: block;
  color: #111827;
  font-size: 18px;
}

.topbar span {
  color: #6b7280;
  font-size: 13px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

.content {
  padding: 28px 32px 72px;
}

.content.is-edge-content {
  height: calc(100vh - 68px);
  min-height: 0;
  padding: 0;
  overflow: auto;
}
</style>
