<template>
  <main class="auth-page">
    <section class="brand-panel">
      <div class="brand-content">
        <p class="brand-kicker">OfferMate AI</p>
        <h1>创建账号，开启智能招聘体验</h1>
        <p class="brand-desc">为求职者、招聘者和平台管理者提供清晰高效的工作入口。</p>

        <div class="feature-list">
          <div class="feature-item">
            <span>人才</span>
            <p>快速建立职业档案，发现更合适的岗位机会。</p>
          </div>
          <div class="feature-item">
            <span>企业</span>
            <p>围绕岗位、候选人与沟通流程提升招聘效率。</p>
          </div>
          <div class="feature-item">
            <span>平台</span>
            <p>通过统一管理入口保障业务流程稳定运行。</p>
          </div>
        </div>
      </div>
    </section>

    <section class="form-panel">
      <el-card class="auth-card" shadow="always">
        <div class="card-header">
          <h2>注册</h2>
          <p>请选择你的平台身份</p>
        </div>

        <el-form
          ref="formRef"
          class="auth-form"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleRegister"
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

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              size="large"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="角色选择" prop="role">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio-button :label="1">求职者</el-radio-button>
              <el-radio-button :label="2">招聘者</el-radio-button>
              <el-radio-button :label="3">管理员</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-button
            class="submit-button"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form>

        <p class="switch-tip">
          已有账号？
          <router-link to="/login">去登录</router-link>
        </p>
      </el-card>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 1
})

function validateConfirmPassword(rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度为 2 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6 到 32 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

async function handleRegister() {
  if (loading.value) {
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    loading.value = true

    try {
      await userStore.register(form)
      ElMessage.success('注册成功，请登录')
      router.replace('/login')
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
    radial-gradient(circle at 18% 18%, rgba(0, 190, 189, 0.18), transparent 32%),
    linear-gradient(135deg, #effefd 0%, #f8fbff 52%, #ffffff 100%);
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
  max-width: 720px;
  margin: 0;
  color: #102a43;
  font-size: 54px;
  line-height: 1.16;
  font-weight: 800;
}

.brand-desc {
  max-width: 620px;
  margin: 28px 0 0;
  color: #52616f;
  font-size: 20px;
  line-height: 1.8;
}

.feature-list {
  display: grid;
  gap: 18px;
  max-width: 650px;
  margin-top: 66px;
}

.feature-item {
  display: grid;
  grid-template-columns: 84px 1fr;
  align-items: center;
  min-height: 82px;
  padding: 18px 22px;
  border: 1px solid rgba(0, 190, 189, 0.16);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 12px 32px rgba(15, 98, 103, 0.07);
}

.feature-item span {
  color: #00a7a6;
  font-size: 18px;
  font-weight: 800;
}

.feature-item p {
  margin: 0;
  color: #5b6675;
  font-size: 15px;
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
  width: 430px;
  border: none;
  border-radius: 8px;
  box-shadow: 0 22px 64px rgba(31, 41, 55, 0.12);
}

.card-header {
  margin-bottom: 26px;
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

.role-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  width: 100%;
}

.role-group :deep(.el-radio-button__inner) {
  width: 100%;
}

.submit-button {
  width: 100%;
  margin-top: 8px;
  font-weight: 700;
}

.switch-tip {
  margin: 24px 0 4px;
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
