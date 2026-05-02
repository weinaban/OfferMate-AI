<template>
  <aside class="sidebar">
    <div class="brand">OfferMate AI</div>
    <nav class="menu">
      <router-link
        v-for="item in menus"
        :key="item.path"
        :to="item.path"
        :class="{ active: route.path === item.path }"
      >
        <el-badge
          v-if="item.badge"
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
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useNotificationStore } from '../../stores/notification'

const route = useRoute()
const notificationStore = useNotificationStore()

const menus = [
  { label: '后台首页', path: '/admin' },
  { label: '用户管理', path: '/admin/users' },
  { label: '企业审核', path: '/admin/companies' },
  { label: '岗位审核', path: '/admin/jobs' },
  { label: '操作日志', path: '/admin/operation-logs' },
  { label: '通知中心', path: '/notifications', badge: true }
]

onMounted(() => {
  notificationStore.fetchUnreadCount()
})
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
