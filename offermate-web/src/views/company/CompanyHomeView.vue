<template>
  <main class="home-page">
    <section class="home-card">
      <div>
        <p class="eyebrow">OfferMate AI 企业端</p>
        <h1>当前角色：招聘者</h1>
        <p class="desc">后续开发：企业后台、岗位管理、投递管理、聊天</p>
      </div>

      <el-button type="primary" size="large" @click="handleLogout">退出登录</el-button>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  padding: 72px;
  background: linear-gradient(135deg, #effefd 0%, #f6f8fb 100%);
}

.home-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1120px;
  min-height: 280px;
  margin: 72px auto 0;
  padding: 48px 56px;
  border: 1px solid #e5f4f4;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 18px 52px rgba(15, 98, 103, 0.1);
}

.eyebrow {
  margin: 0 0 18px;
  color: #00a7a6;
  font-size: 18px;
  font-weight: 800;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 36px;
}

.desc {
  margin: 20px 0 0;
  color: #64748b;
  font-size: 18px;
}
</style>
