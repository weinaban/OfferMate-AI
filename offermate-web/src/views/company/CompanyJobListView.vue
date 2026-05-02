<template>
  <CompanyLayout>
    <section class="page-header">
      <div>
        <h1>岗位管理</h1>
        <p>管理已发布岗位，及时调整招聘状态。</p>
      </div>
      <el-button type="primary" size="large" @click="router.push('/company/jobs/create')">发布岗位</el-button>
    </section>

    <el-skeleton v-if="loading" :rows="8" animated />

    <div v-else-if="jobs.length" class="job-list">
      <CompanyJobCard
        v-for="job in jobs"
        :key="job.id"
        :job="job"
        @edit="handleEdit"
        @offline="handleOffline"
        @delete="handleDelete"
      />
    </div>

    <el-empty v-else description="暂无岗位">
      <el-button type="primary" @click="router.push('/company/jobs/create')">发布岗位</el-button>
    </el-empty>
  </CompanyLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteCompanyJob, getCompanyJobs, offlineCompanyJob } from '../../api/companyJob'
import CompanyJobCard from '../../components/company/CompanyJobCard.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])

function normalizeList(data) {
  return Array.isArray(data) ? data : data?.records || []
}

async function fetchJobs() {
  loading.value = true

  try {
    jobs.value = normalizeList(await getCompanyJobs())
  } finally {
    loading.value = false
  }
}

function handleEdit(job) {
  router.push(`/company/jobs/edit/${job.id}`)
}

async function handleOffline(job) {
  try {
    await ElMessageBox.confirm(`确认下架「${job.title || '未命名岗位'}」吗？`, '下架岗位', {
      type: 'warning',
      confirmButtonText: '下架',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }

  await offlineCompanyJob(job.id)
  ElMessage.success('岗位已下架')
  fetchJobs()
}

async function handleDelete(job) {
  try {
    await ElMessageBox.confirm(`确认删除「${job.title || '未命名岗位'}」吗？`, '删除岗位', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }

  await deleteCompanyJob(job.id)
  ElMessage.success('岗位已删除')
  fetchJobs()
}

onMounted(fetchJobs)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 28px 32px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
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

.job-list {
  display: grid;
  gap: 16px;
}
</style>
