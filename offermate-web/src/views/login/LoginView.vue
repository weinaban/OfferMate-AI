<template>
  <main class="auth-page">
    <section class="brand-panel">
      <div class="brand-content">
        <p class="brand-kicker">OfferMate AI</p>
        <h1>AI 驱动的智能招聘平台</h1>
        <p class="brand-desc">连接优秀人才与优质企业，让求职招聘更高效。</p>

        <div class="advantage-grid">
          <div class="advantage-item">
            <span class="advantage-number">01</span>
            <strong>精准匹配</strong>
            <p>用智能推荐缩短人才和岗位之间的距离。</p>
          </div>
          <div class="advantage-item">
            <span class="advantage-number">02</span>
            <strong>高效沟通</strong>
            <p>围绕招聘流程沉淀清晰、可靠的协作体验。</p>
          </div>
          <div class="advantage-item">
            <span class="advantage-number">03</span>
            <strong>多端协同</strong>
            <p>求职者、企业与管理端统一在同一平台中运转。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="form-panel">
      <el-card class="auth-card" shadow="always">
        <div class="card-header">
          <h2>登录</h2>
          <p>欢迎回到 OfferMate AI</p>
        </div>

        <el-form
          ref="formRef"
          class="auth-form"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model.trim="form.username"
              size="large"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <el-button
            class="submit-button"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <p class="switch-tip">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
      </el-card>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getRoleHome } from '../../router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度为 2 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6 到 32 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (loading.value) {
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    loading.value = true

    try {
      const userInfo = await userStore.login(form)
      ElMessage.success('登录成功')

      const homePath = getRoleHome(userInfo.role)
      const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : ''
      router.replace(redirectPath && redirectPath !== '/login' ? redirectPath : homePath)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-page {
  display: grid;
  grid-template-columns: minmax(640px, 1fr) 560px;
  min-height: 100vh;
  background:
    radial-gradient(circle at 20% 20%, rgba(0, 190, 189, 0.16), transparent 30%),
    linear-gradient(135deg, #ecfffd 0%, #f7fbff 48%, #ffffff 100%);
}

.brand-panel {
  display: flex;
  align-items: center;
  padding: 88px 96px;
}

.brand-content {
  max-width: 760px;
}

.brand-kicker {
  margin: 0 0 20px;
  color: #00a7a6;
  font-size: 28px;
  font-weight: 800;
}

h1 {
  max-width: 680px;
  margin: 0;
  color: #102a43;
  font-size: 56px;
  line-height: 1.16;
  font-weight: 800;
}

.brand-desc {
  max-width: 640px;
  margin: 28px 0 0;
  color: #52616f;
  font-size: 20px;
  line-height: 1.8;
}

.advantage-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-top: 72px;
}

.advantage-item {
  min-height: 156px;
  padding: 24px;
  border: 1px solid rgba(0, 190, 189, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 14px 36px rgba(15, 98, 103, 0.08);
}

.advantage-number {
  display: block;
  margin-bottom: 16px;
  color: #00bebd;
  font-size: 18px;
  font-weight: 800;
}

.advantage-item strong {
  display: block;
  color: #17233d;
  font-size: 18px;
}

.advantage-item p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.7;
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 72px 80px 72px 40px;
  background: rgba(255, 255, 255, 0.68);
}

.auth-card {
  width: 420px;
  border: none;
  border-radius: 8px;
  box-shadow: 0 22px 64px rgba(31, 41, 55, 0.12);
}

.card-header {
  margin-bottom: 28px;
}

.card-header h2 {
  margin: 0;
  color: #111827;
  font-size: 30px;
  line-height: 1.25;
}

.card-header p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 15px;
}

.auth-form {
  width: 100%;
}

.submit-button {
  width: 100%;
  margin-top: 8px;
  font-weight: 700;
}

.switch-tip {
  margin: 26px 0 4px;
  color: #6b7280;
  text-align: center;
  font-size: 14px;
}

.switch-tip a {
  color: #00a7a6;
  font-weight: 700;
}

@media (min-width: 1600px) {
  .auth-page {
    grid-template-columns: minmax(820px, 1fr) 640px;
  }

  .brand-panel {
    padding-left: 132px;
  }
}
</style>
