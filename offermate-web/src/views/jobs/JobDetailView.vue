<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <el-skeleton v-if="loading" :rows="12" animated />

      <template v-else-if="job">
        <section class="detail-main">
          <div class="job-hero">
            <div>
              <p class="company-name">{{ companyName }}</p>
              <h1>{{ job.title || '未命名岗位' }}</h1>
              <div class="meta-row">
                <span>{{ job.city || '城市不限' }}</span>
                <span>{{ job.experience || '经验不限' }}</span>
                <span>{{ job.education || '学历不限' }}</span>
              </div>
            </div>
            <strong class="salary">{{ salaryText }}</strong>
          </div>

          <div v-if="tagList.length" class="tag-row">
            <el-tag v-for="tag in tagList" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>

          <section class="section-card">
            <h2>岗位描述</h2>
            <p class="description">{{ job.description || '岗位描述正在完善中。' }}</p>
          </section>

          <section v-if="job.responsibility || job.duty" class="section-card">
            <h2>岗位职责</h2>
            <p class="description">{{ job.responsibility || job.duty }}</p>
          </section>

          <section v-if="job.requirement || job.requirements" class="section-card">
            <h2>任职要求</h2>
            <p class="description">{{ job.requirement || job.requirements }}</p>
          </section>

          <div class="action-bar">
            <el-button type="primary" size="large" :loading="resumeLoading" @click="openDeliveryDialog">
              立即投递
            </el-button>
            <el-button size="large" :loading="chatLoading" @click="startChat">
              立即沟通
            </el-button>
            <el-button
              v-if="isSeeker"
              type="success"
              size="large"
              :icon="MagicStick"
              @click="goAiMatch"
            >
              AI 匹配分析
            </el-button>
            <el-button
              v-if="isSeeker"
              size="large"
              :icon="MagicStick"
              :loading="aiLoading"
              @click="handleGenerateQuestions"
            >
              AI 生成面试题
            </el-button>
          </div>

          <section v-if="aiQuestionsVisible" class="ai-panel">
            <div class="ai-panel-header">
              <div>
                <h2>AI 模拟面试题</h2>
                <p>根据当前岗位生成，仅作为面试准备参考。</p>
              </div>
              <el-button plain :loading="aiLoading" @click="handleGenerateQuestions">重新生成</el-button>
            </div>
            <div class="ai-result" v-html="aiResultHtml || '<p>暂无面试题。</p>'"></div>
          </section>
        </section>

        <aside class="company-card">
          <h2>公司信息</h2>
          <div class="company-head">
            <LogoAvatar :src="companyLogo" :name="companyName" size="md" />
            <strong>{{ companyName }}</strong>
          </div>
          <div class="company-meta">
            <span>{{ job.companyIndustry || job.industry || '行业信息完善中' }}</span>
            <span>{{ job.companySize || job.scale || job.companyScale || '规模信息完善中' }}</span>
            <span>{{ job.address || job.companyAddress || '地址信息完善中' }}</span>
          </div>
          <p>{{ job.intro || job.companyIntro || job.companyDescription || '公司介绍正在完善中。' }}</p>
          <el-button v-if="companyId" class="company-home-btn" type="primary" plain @click="goCompanyHome">
            查看公司主页
          </el-button>
        </aside>
      </template>

      <el-empty v-else description="岗位不存在或已下线" />
    </main>

    <el-dialog v-model="deliveryDialogVisible" title="选择投递简历" width="620px">
      <div v-if="resumes.length" class="resume-options">
        <label
          v-for="resume in resumes"
          :key="resume.id"
          class="resume-option"
          :class="{ active: selectedResumeId === resume.id }"
        >
          <input v-model="selectedResumeId" type="radio" :value="resume.id" />
          <div>
            <div class="resume-title">
              <strong>{{ resume.title || '未命名简历' }}</strong>
              <el-tag v-if="Number(resume.isDefault) === 1" type="success" effect="plain">默认</el-tag>
            </div>
            <p>{{ resume.realName || '-' }} · {{ resume.education || '-' }} · {{ resume.phone || '-' }}</p>
          </div>
        </label>
      </div>

      <template #footer>
        <el-button @click="deliveryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="deliveryLoading" @click="submitDelivery">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formatAiMarkdown, generateInterviewQuestions, normalizeAiText } from '../../api/ai'
import { createOrGetChatSession } from '../../api/chat'
import { createDelivery } from '../../api/delivery'
import { getJobDetail } from '../../api/job'
import { getMyResumes } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const job = ref(null)
const resumes = ref([])
const selectedResumeId = ref('')
const resumeLoading = ref(false)
const deliveryLoading = ref(false)
const chatLoading = ref(false)
const aiLoading = ref(false)
const aiQuestionsVisible = ref(false)
const deliveryDialogVisible = ref(false)
const aiResult = ref('')
const aiResultHtml = computed(() => formatAiMarkdown(aiResult.value))

const isSeeker = computed(() => Number(userStore.userInfo?.role) === 1)

const tagList = computed(() => {
  const tags = job.value?.tags
  if (Array.isArray(tags)) return tags.filter(Boolean)
  if (typeof tags === 'string') return tags.split(',').map((tag) => tag.trim()).filter(Boolean)
  return []
})

const salaryText = computed(() => {
  const min = job.value?.salaryMin
  const max = job.value?.salaryMax
  if (hasValue(min) && hasValue(max)) return `${min}-${max}K`
  if (hasValue(min)) return `${min}K以上`
  if (hasValue(max)) return `${max}K以下`
  return '薪资面议'
})

const companyId = computed(() => job.value?.companyId || job.value?.company?.id)
const companyName = computed(() => job.value?.companyName || job.value?.company?.companyName || '优质企业')
const companyLogo = computed(() => (
  job.value?.logo ||
  job.value?.companyLogo ||
  job.value?.logoUrl ||
  job.value?.company?.logo ||
  job.value?.company?.logoUrl ||
  ''
))

function hasValue(value) {
  return value !== undefined && value !== null && value !== ''
}

async function fetchDetail() {
  const id = route.params.id
  if (!id) {
    job.value = null
    return
  }

  loading.value = true
  try {
    job.value = await getJobDetail(id)
  } finally {
    loading.value = false
  }
}

async function openDeliveryDialog() {
  if (resumeLoading.value) return

  resumeLoading.value = true
  try {
    const data = await getMyResumes()
    resumes.value = Array.isArray(data) ? data : data?.records || data?.list || data?.rows || []

    if (!resumes.value.length) {
      try {
        await ElMessageBox.confirm('请先创建简历，再投递该岗位。', '暂无简历', {
          confirmButtonText: '去创建',
          cancelButtonText: '取消',
          type: 'info'
        })
        router.push('/resumes/create')
      } catch (error) {
        return
      }
      return
    }

    const defaultResume = resumes.value.find((resume) => Number(resume.isDefault) === 1)
    selectedResumeId.value = defaultResume?.id || resumes.value[0].id
    deliveryDialogVisible.value = true
  } finally {
    resumeLoading.value = false
  }
}

async function submitDelivery() {
  if (deliveryLoading.value || !selectedResumeId.value) return

  deliveryLoading.value = true
  try {
    await createDelivery({
      jobId: Number(route.params.id),
      resumeId: selectedResumeId.value
    })
    ElMessage.success('投递成功')
    deliveryDialogVisible.value = false
  } catch (error) {
    const message = error?.message || ''
    if (message.includes('重复') || message.includes('已投递')) {
      ElMessage.warning('你已经投递过该岗位')
    }
  } finally {
    deliveryLoading.value = false
  }
}

async function startChat() {
  if (chatLoading.value) return

  const targetUserId = job.value?.targetUserId || job.value?.recruiterId || job.value?.userId
  if (!targetUserId) {
    ElMessage.error('当前岗位缺少招聘者信息，暂时无法发起沟通')
    return
  }

  chatLoading.value = true
  try {
    const data = await createOrGetChatSession({
      jobId: Number(route.params.id),
      targetUserId
    })
    const sessionId = data?.id || data?.sessionId
    if (!sessionId) {
      ElMessage.error('会话创建失败，请稍后重试')
      return
    }
    router.push({ path: '/chats', query: { sessionId } })
  } finally {
    chatLoading.value = false
  }
}

function goAiMatch() {
  router.push({
    path: '/ai/job-match',
    query: { jobId: route.params.id }
  })
}

async function handleGenerateQuestions() {
  if (aiLoading.value) return

  aiLoading.value = true
  aiQuestionsVisible.value = true

  try {
    const data = await generateInterviewQuestions({
      jobId: Number(route.params.id),
      title: job.value?.title,
      description: job.value?.description
    })
    aiResult.value = normalizeAiText(data) || 'AI 暂未返回面试题。'
  } catch (error) {
    ElMessage.error(error?.response?.status === 429 ? '今日 AI 调用次数已达上限，请明天再试' : 'AI 生成耗时较长或服务暂时不可用，请稍后再试')
  } finally {
    aiLoading.value = false
  }
}

function goCompanyHome() {
  if (companyId.value) router.push(`/company/${companyId.value}`)
}

watch(
  () => route.params.id,
  () => {
    aiQuestionsVisible.value = false
    aiResult.value = ''
    fetchDetail()
  }
)

onMounted(fetchDetail)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 22px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.detail-main,
.company-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.job-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 30px;
  padding: 34px 36px;
  border-bottom: 1px solid #eef0f2;
}

.company-name {
  margin: 0 0 12px;
  color: #00a7a6;
  font-size: 15px;
  font-weight: 700;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 34px;
  line-height: 1.25;
}

.salary {
  flex: 0 0 auto;
  color: #ff6b35;
  font-size: 30px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.meta-row span {
  padding: 6px 12px;
  border-radius: 4px;
  color: #4b5563;
  background: #f5f7fa;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 24px 36px 0;
}

.section-card {
  padding: 30px 36px 0;
}

.section-card h2,
.company-card h2 {
  margin: 0 0 16px;
  color: #111827;
  font-size: 21px;
}

.description {
  margin: 0;
  color: #374151;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-line;
}

.action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 34px 36px 38px;
}

.action-bar .el-button {
  min-width: 150px;
  font-weight: 700;
}

.ai-panel {
  margin: 0 36px 38px;
  padding: 24px;
  border: 1px solid rgba(0, 190, 189, 0.2);
  border-radius: 8px;
  background: #f0fdfc;
}

.ai-panel-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
}

.ai-panel h2 {
  margin: 0;
  color: #111827;
  font-size: 21px;
}

.ai-panel p {
  margin: 8px 0 0;
  color: #6b7280;
}

.ai-result {
  max-height: 520px;
  overflow-y: auto;
  padding: 18px;
  border-radius: 8px;
  color: #1f2937;
  background: #ffffff;
  font-size: 15px;
  line-height: 1.9;
}

.company-card {
  align-self: start;
  padding: 28px;
  position: sticky;
  top: 88px;
}

.company-head {
  display: flex;
  align-items: center;
  gap: 14px;
}

.company-card strong {
  display: block;
  color: #1f2937;
  font-size: 20px;
  line-height: 1.5;
}

.company-meta {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}

.company-meta span {
  padding: 10px 12px;
  border-radius: 4px;
  color: #4b5563;
  background: #f7f9fb;
  font-size: 14px;
}

.company-card p {
  margin: 22px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.8;
}

.company-home-btn {
  width: 100%;
  margin-top: 22px;
}

.resume-options {
  display: grid;
  gap: 12px;
}

.resume-option {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  padding: 16px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.resume-option.active,
.resume-option:hover {
  border-color: rgba(0, 190, 189, 0.42);
  background: #e9fbfb;
}

.resume-option input {
  margin-top: 4px;
}

.resume-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.resume-option p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}
</style>
