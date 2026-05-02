<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <section class="profile-card">
        <div class="profile-main">
          <LogoAvatar :src="form.avatar" :name="form.username" type="user" size="lg" />
          <div>
            <h1>个人中心</h1>
            <p>{{ form.username || '求职者' }} · 求职者账号</p>
            <FileUploadButton
              class="upload-btn"
              biz-type="avatar"
              text="上传头像"
              loading-text="上传中"
              @success="handleAvatarUploaded"
            />
          </div>
        </div>

        <el-form class="info-form" label-position="top">
          <el-form-item label="用户名">
            <el-input v-model="form.username" size="large" disabled />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" size="large" disabled placeholder="暂无手机号" />
          </el-form-item>
          <el-form-item label="头像 URL">
            <el-input v-model="form.avatar" size="large" disabled placeholder="上传后自动保存" />
          </el-form-item>
        </el-form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updateSeekerAvatar } from '../../api/seeker'
import { getCurrentUser } from '../../api/user'
import AppHeader from '../../components/common/AppHeader.vue'
import FileUploadButton from '../../components/common/FileUploadButton.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const form = reactive({
  username: '',
  phone: '',
  avatar: ''
})

async function fetchProfile() {
  const data = await getCurrentUser()
  Object.assign(form, {
    username: data?.username || userStore.userInfo?.username || '',
    phone: data?.phone || '',
    avatar: data?.avatar || userStore.userInfo?.avatar || ''
  })
  userStore.updateUserInfo({
    username: form.username,
    avatar: form.avatar
  })
}

async function handleAvatarUploaded(url) {
  form.avatar = url
  await updateSeekerAvatar(url)
  userStore.updateUserInfo({ avatar: url })
  ElMessage.success('头像已保存')
}

onMounted(fetchProfile)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 980px;
  margin: 0 auto;
  padding: 32px 24px 72px;
}

.profile-card {
  padding: 34px 40px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
}

.profile-main {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 28px;
  border-bottom: 1px solid #eef0f2;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 30px;
}

p {
  margin: 10px 0 0;
  color: #6b7280;
}

.upload-btn {
  display: block;
  margin-top: 18px;
}

.info-form {
  max-width: 620px;
  margin-top: 28px;
}
</style>
