<template>
  <CompanyLayout>
    <div class="publish-layout">
      <section class="form-card">
        <div class="card-header">
          <h1>编辑岗位</h1>
          <p>更新岗位信息，确保候选人看到准确的招聘需求。</p>
        </div>

        <el-skeleton v-if="pageLoading" :rows="10" animated />
        <CompanyJobForm
          v-else
          :model-value="job"
          submit-text="保存修改"
          :loading="submitLoading"
          @submit="handleSubmit"
          @cancel="router.push('/company/jobs')"
        />
      </section>

      <aside class="side-card">
        <h2>编辑提示</h2>
        <div class="tip-list">
          <div class="tip-item">
            <strong>保持信息一致</strong>
            <p>岗位名称、薪资和描述应与当前招聘需求同步。</p>
          </div>
          <div class="tip-item">
            <strong>及时更新状态</strong>
            <p>岗位暂停招聘时，可以在岗位管理页执行下架操作。</p>
          </div>
          <div class="tip-item">
            <strong>优化关键词</strong>
            <p>标签使用英文逗号分隔，有助于候选人快速识别技术栈。</p>
          </div>
        </div>
      </aside>
    </div>
  </CompanyLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getJobDetail } from '../../api/job'
import { updateCompanyJob } from '../../api/companyJob'
import CompanyJobForm from '../../components/company/CompanyJobForm.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'

const route = useRoute()
const router = useRouter()
const job = ref({})
const pageLoading = ref(false)
const submitLoading = ref(false)

async function fetchJob() {
  pageLoading.value = true

  try {
    job.value = await getJobDetail(route.params.id)
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit(form) {
  if (submitLoading.value) return
  submitLoading.value = true

  try {
    await updateCompanyJob(route.params.id, form)
    ElMessage.success('岗位修改成功')
    router.replace('/company/jobs')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchJob)
</script>

<style scoped>
.publish-layout {
  display: grid;
  grid-template-columns: minmax(0, 900px) 300px;
  align-items: start;
  gap: 22px;
  max-width: 1240px;
}

.form-card,
.side-card {
  padding: 32px 38px 38px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
}

.card-header {
  margin-bottom: 26px;
}

h1,
.side-card h2 {
  margin: 0;
  color: #111827;
}

h1 {
  font-size: 28px;
}

.card-header p {
  margin: 10px 0 0;
  color: #6b7280;
}

.side-card {
  position: sticky;
  top: 96px;
  padding: 26px;
}

.side-card h2 {
  font-size: 20px;
}

.tip-list {
  display: grid;
  gap: 18px;
  margin-top: 20px;
}

.tip-item {
  padding: 16px;
  border-radius: 8px;
  background: #f7f9fb;
}

.tip-item strong {
  color: #111827;
  font-size: 15px;
}

.tip-item p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.7;
}
</style>
