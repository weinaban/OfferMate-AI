<template>
  <aside class="sidebar">
    <div class="brand">OfferMate AI</div>
    <nav class="menu">
      <router-link
        v-for="item in menus"
        :key="item.path"
        :to="item.path"
        :class="{ active: isActive(item) }"
      >
        <el-badge
          v-if="item.badge === 'chat'"
          :value="chatStore.unreadCount"
          :hidden="chatStore.unreadCount <= 0"
          :max="99"
        >
          <span>{{ item.label }}</span>
        </el-badge>
        <el-badge
          v-else-if="item.badge === 'notification'"
          :value="notificationStore.unreadCount"
          :hidden="notificationStore.unreadCount <= 0"
          :max="99"
        >
          <span>{{ item.label }}</span>
        </el-badge>
        <span v-else>{{ item.label }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useChatStore } from '../../stores/chat'
import { useNotificationStore } from '../../stores/notification'

const route = useRoute()
const chatStore = useChatStore()
const notificationStore = useNotificationStore()

const menus = [
  { label: '后台首页', path: '/company', exact: true },
  { label: '企业资料', path: '/company/profile', exact: true },
  { label: '发布岗位', path: '/company/jobs/create', exact: true },
  { label: '岗位管理', path: '/company/jobs', match: /^\/company\/jobs(\/edit\/[^/]+)?$/ },
  { label: '收到的投递', path: '/company/deliveries', exact: true },
  { label: '面试邀请', path: '/company/interviews', exact: true },
  { label: '消息沟通', path: '/chats', exact: true, badge: 'chat' },
  { label: '通知中心', path: '/notifications', exact: true, badge: 'notification' }
]

function isActive(item) {
  if (item.match) {
    return item.match.test(route.path)
  }

  return item.exact ? route.path === item.path : route.path.startsWith(item.path)
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
.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 220px;
  border-right: 1px solid #e8ecef;
  background: #ffffff;
}

.brand {
  height: 68px;
  padding: 22px 24px;
  color: #00a7a6;
  font-size: 22px;
  font-weight: 800;
}

.menu {
  display: grid;
  gap: 6px;
  padding: 10px 14px;
}

.menu a {
  height: 44px;
  padding: 0 16px;
  border-radius: 6px;
  color: #4b5563;
  font-size: 15px;
  font-weight: 700;
  line-height: 44px;
  transition: all 0.2s ease;
}

.menu a:hover,
.menu a.active {
  color: #00a7a6;
  background: #e9fbfb;
}

.menu :deep(.el-badge) {
  display: inline-flex;
  width: 100%;
}

.menu :deep(.el-badge__content) {
  transform: translate(70%, -35%);
}
</style>
