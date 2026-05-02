<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <section class="hero-card">
        <div>
          <p class="eyebrow">AI 求职助手</p>
          <h1>AI 岗位匹配分析</h1>
          <p>选择一份简历和一个目标岗位，快速判断匹配度、优势短板与下一步提升方向。</p>
        </div>
        <el-button type="primary" plain @click="router.push('/ai/interview')">去模拟面试</el-button>
      </section>

      <div class="workspace">
        <section class="selector-card">
          <h2>匹配配置</h2>

          <el-form label-position="top">
            <el-form-item label="我的简历">
              <el-select
                v-model="form.resumeId"
                class="full"
                filterable
                placeholder="请选择简历"
                :loading="resumeLoading"
              >
                <el-option
                  v-for="resume in resumes"
                  :key="resume.id"
                  :label="resumeLabel(resume)"
                  :value="resume.id"
                />
              </el-select>
              <div v-if="!resumeLoading && !resumes.length" class="helper">
                暂无简历，请先
                <router-link to="/resumes/create">创建简历</router-link>
              </div>
            </el-form-item>

            <el-form-item label="目标岗位">
              <el-select
                v-model="form.jobId"
                class="full"
                filterable
                remote
                reserve-keyword
                placeholder="搜索或选择岗位"
                :remote-method="searchJobOptions"
                :loading="jobLoading"
              >
                <el-option
                  v-for="job in jobs"
                  :key="job.id || job.jobId"
                  :label="jobLabel(job)"
                  :value="job.id || job.jobId"
                />
              </el-select>
              <div v-if="!jobLoading && !jobs.length" class="helper">暂无可匹配岗位</div>
            </el-form-item>
          </el-form>

          <el-button
            class="submit-btn"
            type="primary"
            size="large"
            :loading="matching"
            :disabled="!form.resumeId || !form.jobId"
            @click="handleMatch"
          >
            开始匹配分析
          </el-button>
        </section>

        <section class="result-card">
          <template v-if="matching">
            <el-skeleton :rows="9" animated />
          </template>

          <template v-else-if="matchResult">
            <div class="score-row">
              <el-progress
                type="dashboard"
                :percentage="matchResult.score"
                :stroke-width="12"
                color="#00bebd"
              />
              <div>
                <h2>匹配度 {{ matchResult.score }}%</h2>
                <p>分数仅作为 AI 评估参考，建议结合岗位详情和自身偏好一起判断。</p>
              </div>
            </div>

            <div class="result-grid">
              <ResultBlock title="我的优势" type="success" :items="matchResult.advantages" empty="AI 暂未识别到明确优势" />
              <ResultBlock title="当前短板" type="warning" :items="matchResult.weaknesses" empty="AI 暂未识别到明显短板" />
              <ResultBlock title="改进建议" type="primary" :items="matchResult.suggestions" empty="AI 暂未返回改进建议" />
              <ResultBlock title="推荐学习方向" type="info" :items="matchResult.learningPath" empty="暂无推荐学习方向" />
            </div>

            <section v-if="matchResult.text" class="text-result">
              <h3>补充分析</h3>
              <p>{{ matchResult.text }}</p>
            </section>
          </template>

          <el-empty
            v-else
            description="选择简历和岗位后，AI 会在这里展示结构化匹配结果"
          />
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { matchJob } from '../../api/aiAdvanced'
import { getJobPage, searchJobs } from '../../api/job'
import { getMyResumes } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'
import { normalizeMatchResult } from '../../utils/aiResult'

const ResultBlock = defineComponent({
  name: 'ResultBlock',
  props: {
    title: { type: String, required: true },
    type: { type: String, default: 'primary' },
    items: { type: Array, default: () => [] },
    empty: { type: String, default: '暂无内容' }
  },
  setup(props) {
    return () =>
      h('section', { class: 'result-block' }, [
        h('div', { class: 'block-title' }, [
          h('span', { class: ['dot', props.type] }),
          h('h3', props.title)
        ]),
        props.items.length
          ? h(
              'ul',
              props.items.map((item) => h('li', item))
            )
          : h('p', { class: 'empty-text' }, props.empty)
      ])
  }
})

const route = useRoute()
const router = useRouter()

const resumes = ref([])
const jobs = ref([])
const resumeLoading = ref(false)
const jobLoading = ref(false)
const matching = ref(false)
const matchResult = ref(null)

const form = reactive({
  resumeId: '',
  jobId: ''
})

const initialJobId = computed(() => route.query.jobId ? Number(route.query.jobId) : '')
const initialResumeId = computed(() => route.query.resumeId ? Number(route.query.resumeId) : '')

function resumeLabel(resume) {
  return `${resume.title || '未命名简历'}${resume.realName ? ` · ${resume.realName}` : ''}`
}

function jobLabel(job) {
  return `${job.title || '未命名岗位'} · ${job.companyName || job.company?.companyName || '优质企业'} · ${salaryText(job)}`
}

function salaryText(job) {
  const min = job.salaryMin
  const max = job.salaryMax
  if (min && max) return `${min}-${max}K`
  if (min) return `${min}K以上`
  if (max) return `${max}K以下`
  return '薪资面议'
}

function normalizePage(data) {
  if (Array.isArray(data)) return data
  return data?.records || data?.list || data?.rows || []
}

async function loadResumes() {
  resumeLoading.value = true
  try {
    const data = await getMyResumes()
    resumes.value = normalizePage(data)
    if (initialResumeId.value) {
      form.resumeId = initialResumeId.value
    } else if (!form.resumeId && resumes.value.length) {
      const defaultResume = resumes.value.find((resume) => Number(resume.isDefault) === 1)
      form.resumeId = defaultResume?.id || resumes.value[0].id
    }
  } finally {
    resumeLoading.value = false
  }
}

async function searchJobOptions(keyword = '') {
  jobLoading.value = true
  try {
    const api = keyword ? searchJobs : getJobPage
    const data = await api({
      keyword,
      page: 1,
      pageSize: 20
    })
    jobs.value = normalizePage(data)
  } finally {
    jobLoading.value = false
  }
}

async function ensureInitialJob() {
  await searchJobOptions('')
  if (initialJobId.value) {
    form.jobId = initialJobId.value
  } else if (!form.jobId && jobs.value.length) {
    form.jobId = jobs.value[0].id || jobs.value[0].jobId
  }
}

async function handleMatch() {
  if (!form.resumeId || !form.jobId || matching.value) return

  matching.value = true
  matchResult.value = null

  try {
    const data = await matchJob({
      resumeId: form.resumeId,
      jobId: form.jobId
    })
    matchResult.value = normalizeMatchResult(data)
  } catch (error) {
    ElMessage.error(getAiErrorMessage(error, 'AI 匹配分析失败，请稍后再试'))
  } finally {
    matching.value = false
  }
}

function getAiErrorMessage(error, fallback) {
  if (error?.response?.status === 429) return '今日 AI 调用次数已达上限，请明天再试'
  return error?.response?.data?.msg || error?.message || fallback
}

onMounted(async () => {
  await Promise.all([loadResumes(), ensureInitialJob()])
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.hero-card,
.selector-card,
.result-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.05);
}

.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 30px 34px;
  background: linear-gradient(135deg, #e8fffd 0%, #ffffff 72%);
}

.eyebrow {
  margin: 0 0 8px;
  color: #00a7a6;
  font-weight: 800;
}

h1,
h2,
h3,
p {
  margin: 0;
}

h1 {
  color: #111827;
  font-size: 32px;
}

.hero-card p:last-child {
  margin-top: 10px;
  color: #6b7280;
}

.workspace {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 22px;
  margin-top: 22px;
}

.selector-card,
.result-card {
  padding: 28px;
}

.selector-card {
  align-self: start;
  position: sticky;
  top: 88px;
}

.selector-card h2,
.result-card h2 {
  color: #111827;
  font-size: 22px;
}

.full,
.submit-btn {
  width: 100%;
}

.helper {
  margin-top: 8px;
  color: #8a94a6;
  font-size: 13px;
}

.helper a {
  color: #00a7a6;
}

.submit-btn {
  margin-top: 8px;
}

.score-row {
  display: flex;
  align-items: center;
  gap: 26px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eef0f2;
}

.score-row p {
  margin-top: 10px;
  color: #6b7280;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 22px;
}

.result-block {
  min-height: 180px;
  padding: 20px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #fbfcfd;
}

.block-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.block-title h3 {
  color: #111827;
  font-size: 17px;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #409eff;
}

.dot.success {
  background: #67c23a;
}

.dot.warning {
  background: #e6a23c;
}

.dot.info {
  background: #909399;
}

.result-block ul {
  display: grid;
  gap: 10px;
  margin: 0;
  padding-left: 18px;
  color: #374151;
  line-height: 1.7;
}

.empty-text {
  color: #9ca3af;
}

.text-result {
  margin-top: 18px;
  padding: 20px;
  border-radius: 8px;
  background: #f0fdfc;
}

.text-result h3 {
  margin-bottom: 10px;
}

.text-result p {
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
