<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <el-skeleton v-if="loading" :rows="12" animated />

      <template v-else-if="resume">
        <section class="preview-card">
          <div class="preview-header">
            <div>
              <h1>{{ resume.title || '未命名简历' }}</h1>
              <p>{{ resume.realName || '-' }} · {{ resume.education || '-' }} · {{ experienceText }}</p>
            </div>
            <div class="actions">
              <el-button @click="router.push('/resumes')">返回列表</el-button>
              <el-button type="primary" @click="router.push(`/resumes/edit/${resume.id}`)">编辑简历</el-button>
              <el-button type="success" :icon="MagicStick" @click="goMockInterview">
                模拟面试
              </el-button>
              <el-button :icon="MagicStick" :loading="aiLoading" @click="handleOptimizeResume">
                AI 优化
              </el-button>
            </div>
          </div>

          <section class="resume-section">
            <h2>基本信息</h2>
            <div class="info-grid">
              <span>姓名：{{ resume.realName || '-' }}</span>
              <span>电话：{{ resume.phone || '-' }}</span>
              <span>邮箱：{{ resume.email || '-' }}</span>
              <span>学历：{{ resume.education || '-' }}</span>
              <span>工作年限：{{ experienceText }}</span>
            </div>
          </section>

          <section class="resume-section">
            <h2>技能描述</h2>
            <p>{{ resume.skill || '暂无技能描述。' }}</p>
          </section>

          <section class="resume-section">
            <h2>项目经历</h2>
            <p>{{ resume.projectExp || '暂无项目经历。' }}</p>
          </section>

          <section class="resume-section">
            <h2>自我评价</h2>
            <p>{{ resume.selfIntro || '暂无自我评价。' }}</p>
          </section>
        </section>
      </template>

      <el-empty v-else description="简历不存在或已删除" />
    </main>

    <el-dialog v-model="aiDialogVisible" title="AI 简历优化建议" width="720px">
      <div class="ai-result">{{ aiResult || '暂无优化建议。' }}</div>
      <template #footer>
        <el-button type="primary" @click="aiDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { normalizeAiText, optimizeResume } from '../../api/ai'
import { getResumeDetail } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const aiLoading = ref(false)
const aiDialogVisible = ref(false)
const aiResult = ref('')
const resume = ref(null)

const experienceText = computed(() => {
  const year = resume.value?.experienceYear
  if (year === 0 || year) return `${year}年`
  return '-'
})

async function fetchResume() {
  loading.value = true
  try {
    resume.value = await getResumeDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

function goMockInterview() {
  router.push({
    path: '/ai/interview',
    query: { resumeId: route.params.id }
  })
}

async function handleOptimizeResume() {
  if (aiLoading.value) return

  aiLoading.value = true
  try {
    const data = await optimizeResume({
      resumeId: Number(route.params.id)
    })
    aiResult.value = normalizeAiText(data) || 'AI 暂未返回优化建议。'
    aiDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error?.response?.status === 429 ? '今日 AI 调用次数已达上限，请明天再试' : 'AI 服务暂时不可用，请稍后再试')
  } finally {
    aiLoading.value = false
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

.preview-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
}

.preview-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 34px 40px;
  border-bottom: 1px solid #eef0f2;
  background: linear-gradient(135deg, #eafffd 0%, #ffffff 76%);
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 30px;
}

.preview-header p {
  margin: 12px 0 0;
  color: #4b5563;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.resume-section {
  padding: 30px 40px 0;
}

.resume-section:last-child {
  padding-bottom: 40px;
}

.resume-section h2 {
  margin: 0 0 16px;
  color: #111827;
  font-size: 20px;
}

.resume-section p {
  margin: 0;
  color: #374151;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-line;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 22px;
  color: #374151;
}

.ai-result {
  max-height: 520px;
  overflow-y: auto;
  padding: 18px;
  border-radius: 8px;
  color: #1f2937;
  background: #f7f9fb;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
