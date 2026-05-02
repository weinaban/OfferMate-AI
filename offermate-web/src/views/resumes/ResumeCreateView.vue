<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <section class="form-card">
        <div class="card-header">
          <h1>新建简历</h1>
          <p>完善基础信息、技能与项目经历，让招聘方更快了解你。</p>
        </div>

        <ResumeForm
          ref="resumeFormRef"
          show-ai-optimize
          :loading="loading"
          :ai-loading-section="aiLoadingSection"
          @submit="handleSubmit"
          @ai-optimize="handleAiOptimize"
          @cancel="router.push('/resumes')"
        />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { normalizeAiText, optimizeResumeSection } from '../../api/ai'
import { createResume } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'
import ResumeForm from '../../components/resume/ResumeForm.vue'

const router = useRouter()
const loading = ref(false)
const aiLoadingSection = ref('')
const resumeFormRef = ref(null)

async function handleSubmit(form) {
  if (loading.value) {
    return
  }

  loading.value = true

  try {
    await createResume(form)
    ElMessage.success('简历创建成功')
    router.replace('/resumes')
  } finally {
    loading.value = false
  }
}

async function handleAiOptimize(section, form) {
  if (aiLoadingSection.value || loading.value) {
    return
  }

  aiLoadingSection.value = section

  try {
    const data = await optimizeResumeSection({
      ...form,
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
