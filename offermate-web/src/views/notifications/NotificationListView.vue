<template>
  <CompanyLayout v-if="role === 2">
    <NotificationContent mode="wide" />
  </CompanyLayout>

  <AdminLayout v-else-if="role === 3">
    <NotificationContent mode="wide" />
  </AdminLayout>

  <div v-else class="seeker-page">
    <AppHeader />
    <NotificationContent />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AdminLayout from '../../components/admin/AdminLayout.vue'
import AppHeader from '../../components/common/AppHeader.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'
import NotificationContent from '../../components/notification/NotificationContent.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const role = computed(() => Number(userStore.userInfo?.role))
</script>

<style scoped>
.seeker-page {
  min-height: 100vh;
  background: #f5f7fa;
}
</style>
