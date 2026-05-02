<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <section class="form-card">
        <div class="card-header">
          <h1>编辑简历</h1>
          <p>及时更新简历内容，保持求职信息准确清晰。</p>
        </div>

        <el-skeleton v-if="pageLoading" :rows="10" animated />
        <ResumeForm
          v-else
          ref="resumeFormRef"
          :model-value="resume"
          :loading="submitLoading"
          :ai-loading-section="aiLoadingSection"
          show-ai-optimize
          @submit="handleSubmit"
          @ai-optimize="handleAiOptimize"
          @cancel="router.push('/resumes')"
        />
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { normalizeAiText, optimizeResumeSection } from '../../api/ai'
import { getResumeDetail, updateResume } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'
import ResumeForm from '../../components/resume/ResumeForm.vue'

const route = useRoute()
const router = useRouter()
const resume = ref({})
const pageLoading = ref(false)
const submitLoading = ref(false)
const aiLoadingSection = ref('')
const resumeFormRef = ref(null)

async function fetchResume() {
  pageLoading.value = true

  try {
    resume.value = await getResumeDetail(route.params.id)
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit(form) {
  if (submitLoading.value) {
    return
  }

  submitLoading.value = true

  try {
    await updateResume(route.params.id, form)
    ElMessage.success('简历保存成功')
    router.replace('/resumes')
  } finally {
    submitLoading.value = false
  }
}

async function handleAiOptimize(section, form) {
  if (aiLoadingSection.value) {
    return
  }

  aiLoadingSection.value = section

  try {
    const data = await optimizeResumeSection({
      ...form,
      resumeId: Number(route.params.id),
      section
    })
    const aiText = normalizeAiText(data)

    if (!aiText) {
      ElMessage.warning('AI 暂未返回可回填的内容')
      return
    }

    resumeFormRef.value?.applySectionResult(section, aiText)
    ElMessage.success('AI 优化结果已回填，请确认后手动保存')
  } catch (error) {
    ElMessage.error('AI 生成较慢或服务暂时不可用，请稍后再试')
  } finally {
    aiLoadingSection.value = ''
  }
}

onMounted(fetchResume)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px 72px;
}

.form-card {
  padding: 34px 40px 40px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
}

.card-header {
  margin-bottom: 26px;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.card-header p {
  margin: 10px 0 0;
  color: #6b7280;
}
</style>
