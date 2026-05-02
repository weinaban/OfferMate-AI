<template>
  <CompanyLayout v-if="isCompanyUser">
    <ChatWorkspace />
  </CompanyLayout>

  <div v-else class="seeker-page">
    <AppHeader />
    <ChatWorkspace />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AppHeader from '../../components/common/AppHeader.vue'
import ChatWorkspace from '../../components/chat/ChatWorkspace.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isCompanyUser = computed(() => Number(userStore.userInfo?.role) === 2)
</script>

<style scoped>
.seeker-page {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  height: 100vh;
  min-height: 0;
  overflow: hidden;
  background: #f5f7fa;
}
</style>
