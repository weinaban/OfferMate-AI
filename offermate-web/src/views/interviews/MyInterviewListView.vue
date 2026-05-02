<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <section class="page-header">
        <div>
          <h1>我的面试</h1>
          <p>查看企业发来的面试邀请，并及时确认是否参加。</p>
        </div>
        <el-button type="primary" plain @click="fetchInterviews">刷新</el-button>
      </section>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else-if="interviews.length" class="interview-list">
        <article v-for="item in interviews" :key="getInterviewId(item)" class="interview-card">
          <LogoAvatar :src="item.companyLogo || item.logo" :name="item.companyName" type="company" size="lg" />

          <div class="main">
            <div class="title-line">
              <h2>{{ item.companyName || '优质企业' }}</h2>
              <el-tag :type="statusType(item.status)" effect="plain">{{ statusText(item.status) }}</el-tag>
            </div>
            <p class="job">{{ item.jobTitle || item.title || '面试岗位' }}</p>
            <div class="info-grid">
              <span>面试时间：{{ item.interviewTime || '-' }}</span>
              <span>面试地点：{{ item.address || '-' }}</span>
              <span>联系人：{{ item.contactName || '-' }}</span>
              <span>联系电话：{{ item.contactPhone || '-' }}</span>
            </div>
            <p v-if="item.remark" class="remark">备注：{{ item.remark }}</p>
            <p v-if="item.createTime" class="create-time">邀请时间：{{ item.createTime }}</p>
          </div>

          <div v-if="Number(item.status || 1) === 1" class="actions">
            <el-button
              type="primary"
              :loading="actionLoadingId === `${getInterviewId(item)}-accept`"
              @click="handleAccept(item)"
            >
              接受
            </el-button>
            <el-button
              type="danger"
              plain
              :loading="actionLoadingId === `${getInterviewId(item)}-reject`"
              @click="handleReject(item)"
            >
              拒绝
            </el-button>
          </div>
        </article>
      </div>

      <el-empty v-else description="暂无面试邀请" />
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { acceptInterview, getMyInterviews, rejectInterview } from '../../api/interview'
import AppHeader from '../../components/common/AppHeader.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'
import { useNotificationStore } from '../../stores/notification'

const notificationStore = useNotificationStore()
const loading = ref(false)
const actionLoadingId = ref('')
const interviews = ref([])

const statusMap = {
  1: '待确认',
  2: '已接受',
  3: '已拒绝',
  4: '已取消'
}

const statusTypeMap = {
  1: 'warning',
  2: 'success',
  3: 'danger',
  4: 'info'
}

function normalizeList(data) {
  return Array.isArray(data) ? data : data?.records || data?.list || data?.rows || []
}

function getInterviewId(item) {
  return item.id || item.interviewId
}

function statusText(status) {
  return statusMap[Number(status || 1)] || '待确认'
}

function statusType(status) {
  return statusTypeMap[Number(status || 1)] || 'warning'
}

async function fetchInterviews() {
  loading.value = true

  try {
    interviews.value = normalizeList(await getMyInterviews({ page: 1, pageSize: 100 }))
  } finally {
    loading.value = false
  }
}

async function handleAccept(item) {
  const id = getInterviewId(item)
  await ElMessageBox.confirm('确定接受这条面试邀请吗？', '接受邀请', {
    type: 'success',
    confirmButtonText: '接受',
    cancelButtonText: '取消'
  })

  actionLoadingId.value = `${id}-accept`

  try {
    await acceptInterview(id)
    ElMessage.success('已接受面试邀请')
    notificationStore.fetchUnreadCount()
    await fetchInterviews()
  } finally {
    actionLoadingId.value = ''
  }
}

async function handleReject(item) {
  const id = getInterviewId(item)
  await ElMessageBox.confirm('确定拒绝这条面试邀请吗？', '拒绝邀请', {
    type: 'warning',
    confirmButtonText: '拒绝',
    cancelButtonText: '取消'
  })

  actionLoadingId.value = `${id}-reject`

  try {
    await rejectInterview(id)
    ElMessage.success('已拒绝面试邀请')
    notificationStore.fetchUnreadCount()
    await fetchInterviews()
  } finally {
    actionLoadingId.value = ''
  }
}

onMounted(fetchInterviews)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 72px;
}

.page-header,
.interview-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 28px 32px;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.page-header p {
  margin: 10px 0 0;
  color: #6b7280;
}

.interview-list {
  display: grid;
  gap: 16px;
}

.interview-card {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) auto;
  gap: 20px;
  padding: 24px 28px;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 12px;
}

h2 {
  margin: 0;
  color: #111827;
  font-size: 21px;
}

.job {
  margin: 8px 0 0;
  color: #00a7a6;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 18px;
  margin-top: 16px;
  color: #4b5563;
}

.remark,
.create-time {
  margin: 12px 0 0;
  color: #6b7280;
}

.actions {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
</style>
