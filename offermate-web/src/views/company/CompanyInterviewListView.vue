<template>
  <CompanyLayout>
    <section class="page-header">
      <div>
        <h1>面试邀请管理</h1>
        <p>查看已发送的面试邀请，跟进候选人的确认状态。</p>
      </div>
      <el-button type="primary" plain @click="fetchInterviews">刷新</el-button>
    </section>

    <section class="table-card">
      <el-skeleton v-if="loading" :rows="8" animated />
      <el-table v-else-if="interviews.length" :data="interviews" border>
        <el-table-column label="面试岗位" min-width="180">
          <template #default="{ row }">
            <strong>{{ row.jobTitle || row.title || '未命名岗位' }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="求职者" min-width="120">
          <template #default="{ row }">{{ row.seekerName || row.realName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" min-width="170" />
        <el-table-column prop="address" label="面试地点" min-width="220" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" min-width="110" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="140" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="Number(row.status || 1) === 1"
              text
              type="danger"
              :loading="cancelLoadingId === getInterviewId(row)"
              @click="handleCancel(row)"
            >
              取消邀请
            </el-button>
            <span v-else class="muted">暂无操作</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无面试邀请" />
    </section>
  </CompanyLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelInterview, getCompanyInterviews } from '../../api/interview'
import CompanyLayout from '../../components/company/CompanyLayout.vue'
import { useNotificationStore } from '../../stores/notification'

const notificationStore = useNotificationStore()
const loading = ref(false)
const cancelLoadingId = ref('')
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

function getInterviewId(row) {
  return row.id || row.interviewId
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
    interviews.value = normalizeList(await getCompanyInterviews({ page: 1, pageSize: 100 }))
  } finally {
    loading.value = false
  }
}

async function handleCancel(row) {
  const id = getInterviewId(row)
  if (!id || cancelLoadingId.value) {
    return
  }

  await ElMessageBox.confirm('确定取消这条面试邀请吗？', '取消邀请', {
    type: 'warning',
    confirmButtonText: '取消邀请',
    cancelButtonText: '返回'
  })

  cancelLoadingId.value = id

  try {
    await cancelInterview(id)
    ElMessage.success('面试邀请已取消')
    notificationStore.fetchUnreadCount()
    await fetchInterviews()
  } finally {
    cancelLoadingId.value = ''
  }
}

onMounted(fetchInterviews)
</script>

<style scoped>
.page-header,
.table-card {
  padding: 28px 32px;
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

.muted {
  color: #9ca3af;
  font-size: 13px;
}
</style>
