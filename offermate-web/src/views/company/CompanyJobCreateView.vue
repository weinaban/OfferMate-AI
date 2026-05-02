<template>
  <CompanyLayout>
    <div class="publish-layout">
      <section class="form-card">
        <div class="card-header">
          <h1>发布岗位</h1>
          <p>填写清晰的职位信息，帮助候选人快速判断匹配度。</p>
        </div>

        <CompanyJobForm submit-text="发布岗位" :loading="loading" @submit="handleSubmit" @cancel="router.push('/company/jobs')" />
      </section>

      <aside class="side-card">
        <h2>发布建议</h2>
        <div class="tip-list">
          <div class="tip-item">
            <strong>岗位名称要具体</strong>
            <p>建议包含方向与级别，例如 Java后端开发工程师。</p>
          </div>
          <div class="tip-item">
            <strong>薪资范围要真实</strong>
            <p>清晰的薪资区间能提升候选人沟通意愿。</p>
          </div>
          <div class="tip-item">
            <strong>描述保持可读</strong>
            <p>职责、要求、团队情况分段描述，候选人更容易判断匹配度。</p>
          </div>
        </div>
      </aside>
    </div>
  </CompanyLayout>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createCompanyJob } from '../../api/companyJob'
import CompanyJobForm from '../../components/company/CompanyJobForm.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'

const router = useRouter()
const loading = ref(false)

async function handleSubmit(form) {
  if (loading.value) return
  loading.value = true

  try {
    await createCompanyJob(form)
    ElMessage.success('岗位发布成功')
    router.replace('/company/jobs')
  } finally {
    loading.value = false
  }
}
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
