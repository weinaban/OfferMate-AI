<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <section class="hero-card">
        <div>
          <p class="eyebrow">AI 求职助手</p>
          <h1>AI 模拟面试</h1>
          <p>围绕目标岗位进行多轮问答，查看评分、追问和改进建议。</p>
        </div>
        <el-button type="primary" plain @click="router.push('/ai/job-match')">去做岗位匹配</el-button>
      </section>

      <div class="workspace">
        <aside class="config-card">
          <h2>面试配置</h2>

          <el-form label-position="top">
            <el-form-item label="我的简历">
              <el-select v-model="form.resumeId" class="full" filterable placeholder="请选择简历" :loading="resumeLoading" :disabled="!!sessionId">
                <el-option v-for="resume in resumes" :key="resume.id" :label="resumeLabel(resume)" :value="resume.id" />
              </el-select>
              <div v-if="!resumeLoading && !resumes.length" class="helper">
                暂无简历，请先 <router-link to="/resumes/create">创建简历</router-link>
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
                :disabled="!!sessionId"
              >
                <el-option v-for="job in jobs" :key="job.id || job.jobId" :label="jobLabel(job)" :value="job.id || job.jobId" />
              </el-select>
            </el-form-item>
          </el-form>

          <el-button
            class="full"
            type="primary"
            size="large"
            :loading="sessionLoading"
            :disabled="!form.resumeId || !form.jobId || !!sessionId"
            @click="startInterview"
          >
            开始模拟面试
          </el-button>

          <el-button
            v-if="sessionId"
            class="full secondary-btn"
            :loading="reportLoading"
            @click="loadReport"
          >
            生成面试报告
          </el-button>

          <div v-if="sessionId" class="session-box">
            <span>当前会话</span>
            <strong>#{{ sessionId }}</strong>
          </div>
        </aside>

        <section class="interview-card">
          <template v-if="!sessionId">
            <el-empty description="选择简历和岗位后，开始一次 AI 模拟面试" />
          </template>

          <template v-else>
            <div class="conversation">
              <div v-for="(round, index) in rounds" :key="index" class="round">
                <div v-if="round.question" class="message ai">
                  <div class="message-label">AI 面试官</div>
                  <p>{{ round.question }}</p>
                </div>

                <div v-if="round.answer" class="message mine">
                  <div class="message-label">我的回答</div>
                  <p>{{ round.answer }}</p>
                </div>

                <div v-if="round.score || round.followUp || round.suggestion" class="feedback-card">
                  <el-tag v-if="round.score" type="success" effect="plain">评分：{{ round.score }}</el-tag>
                  <div v-if="round.followUp">
                    <strong>追问</strong>
                    <p>{{ round.followUp }}</p>
                  </div>
                  <div v-if="round.suggestion">
                    <strong>建议</strong>
                    <p>{{ round.suggestion }}</p>
                  </div>
                </div>
              </div>

              <el-skeleton v-if="questionLoading || answerLoading" :rows="3" animated />
            </div>

            <div class="answer-box">
              <el-input
                v-model="answer"
                type="textarea"
                :rows="4"
                maxlength="2000"
                show-word-limit
                placeholder="请输入你的回答，尽量结合项目经历和具体场景"
                :disabled="answerLoading || questionLoading"
              />
              <div class="answer-actions">
                <el-button :loading="questionLoading" :disabled="answerLoading" @click="generateNextQuestion">
                  生成下一题
                </el-button>
                <el-button type="primary" :loading="answerLoading" :disabled="!canSubmitAnswer" @click="submitAnswer">
                  提交回答
                </el-button>
              </div>
            </div>
          </template>
        </section>
      </div>
    </main>

    <el-dialog v-model="reportVisible" title="AI 面试报告" width="760px">
      <div v-if="report" class="report">
        <div v-if="report.score" class="report-score">
          <span>综合评分</span>
          <strong>{{ report.score }}</strong>
        </div>
        <section v-if="report.summary">
          <h3>总结</h3>
          <p>{{ report.summary }}</p>
        </section>
        <ReportList title="优势" :items="report.advantages" />
        <ReportList title="不足" :items="report.weaknesses" />
        <ReportList title="改进建议" :items="report.suggestions" />
        <ReportList title="覆盖问题" :items="report.questions" />
        <section v-if="report.rawText && !report.summary && !report.advantages.length">
          <p>{{ report.rawText }}</p>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="reportVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, defineComponent, h, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createInterviewSession,
  generateInterviewQuestion,
  getInterviewReport,
  submitInterviewAnswer
} from '../../api/aiAdvanced'
import { getJobPage, searchJobs } from '../../api/job'
import { getMyResumes } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'
import {
  normalizeInterviewAnswer,
  normalizeInterviewQuestion,
  normalizeInterviewReport
} from '../../utils/aiResult'

const ReportList = defineComponent({
  name: 'ReportList',
  props: {
    title: { type: String, required: true },
    items: { type: Array, default: () => [] }
  },
  setup(props) {
    return () =>
      props.items.length
        ? h('section', [
            h('h3', props.title),
            h(
              'ul',
              props.items.map((item) => h('li', item))
            )
          ])
        : null
  }
})

const route = useRoute()
const router = useRouter()

const form = reactive({
  resumeId: '',
  jobId: ''
})

const resumes = ref([])
const jobs = ref([])
const rounds = ref([])
const answer = ref('')
const report = ref(null)
const reportVisible = ref(false)
const sessionId = ref('')

const resumeLoading = ref(false)
const jobLoading = ref(false)
const sessionLoading = ref(false)
const questionLoading = ref(false)
const answerLoading = ref(false)
const reportLoading = ref(false)

const canSubmitAnswer = computed(() => {
  const current = rounds.value[rounds.value.length - 1]
  return !!sessionId.value && !!current?.question && !current.answer && answer.value.trim().length > 0
})

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
    if (route.query.resumeId) {
      form.resumeId = Number(route.query.resumeId)
    } else if (resumes.value.length) {
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
    const data = await api({ keyword, page: 1, pageSize: 20 })
    jobs.value = normalizePage(data)
    if (route.query.jobId) {
      form.jobId = Number(route.query.jobId)
    } else if (!form.jobId && jobs.value.length) {
      form.jobId = jobs.value[0].id || jobs.value[0].jobId
    }
  } finally {
    jobLoading.value = false
  }
}

async function startInterview() {
  if (sessionLoading.value || !form.resumeId || !form.jobId) return

  sessionLoading.value = true
  rounds.value = []
  report.value = null

  try {
    const data = await createInterviewSession({
      resumeId: form.resumeId,
      jobId: form.jobId
    })
    sessionId.value = data?.id || data?.sessionId || data
    if (!sessionId.value) {
      ElMessage.error('面试会话创建失败，请稍后再试')
      return
    }
    await generateNextQuestion()
  } catch (error) {
    ElMessage.error(getAiErrorMessage(error, '创建模拟面试失败，请稍后再试'))
  } finally {
    sessionLoading.value = false
  }
}

async function generateNextQuestion() {
  if (!sessionId.value || questionLoading.value) return

  questionLoading.value = true
  try {
    const data = await generateInterviewQuestion(sessionId.value)
    const question = normalizeInterviewQuestion(data)
    rounds.value.push({
      question: question || 'AI 暂未返回问题，请稍后重试',
      answer: '',
      score: '',
      followUp: '',
      suggestion: ''
    })
    answer.value = ''
    scrollToBottom()
  } catch (error) {
    ElMessage.error(getAiErrorMessage(error, '生成面试问题失败，请稍后再试'))
  } finally {
    questionLoading.value = false
  }
}

async function submitAnswer() {
  if (!canSubmitAnswer.value || answerLoading.value) return

  const current = rounds.value[rounds.value.length - 1]
  const answerText = answer.value.trim()
  answerLoading.value = true
  current.answer = answerText

  try {
    const data = await submitInterviewAnswer(sessionId.value, { answer: answerText })
    const result = normalizeInterviewAnswer(data)
    current.score = result.score
    current.suggestion = result.suggestion || result.text

    if (result.followUp && !result.finished) {
      rounds.value.push({
        question: result.followUp,
        answer: '',
        score: '',
        followUp: '',
        suggestion: ''
      })
    } else {
      current.followUp = result.followUp
      if (result.finished) {
        ElMessage.success('本轮模拟面试已完成，可以生成面试报告')
      }
    }

    answer.value = ''
    scrollToBottom()
  } catch (error) {
    current.answer = ''
    ElMessage.error(getAiErrorMessage(error, '提交回答失败，请稍后再试'))
  } finally {
    answerLoading.value = false
  }
}

async function loadReport() {
  if (!sessionId.value || reportLoading.value) return

  reportLoading.value = true
  try {
    const data = await getInterviewReport(sessionId.value)
    report.value = normalizeInterviewReport(data)
    reportVisible.value = true
  } catch (error) {
    ElMessage.error(getAiErrorMessage(error, '生成面试报告失败，请稍后再试'))
  } finally {
    reportLoading.value = false
  }
}

function getAiErrorMessage(error, fallback) {
  if (error?.response?.status === 429) return '今日 AI 调用次数已达上限，请明天再试'
  return error?.response?.data?.msg || error?.message || fallback
}

function scrollToBottom() {
  nextTick(() => {
    const container = document.querySelector('.conversation')
    if (container) container.scrollTop = container.scrollHeight
  })
}

onMounted(async () => {
  await Promise.all([loadResumes(), searchJobOptions('')])
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1320px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.hero-card,
.config-card,
.interview-card {
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
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 22px;
  margin-top: 22px;
}

.config-card,
.interview-card {
  padding: 26px;
}

.config-card {
  align-self: start;
  position: sticky;
  top: 88px;
}

.full {
  width: 100%;
}

.secondary-btn {
  margin-top: 12px;
  margin-left: 0;
}

.helper {
  margin-top: 8px;
  color: #8a94a6;
  font-size: 13px;
}

.helper a {
  color: #00a7a6;
}

.session-box {
  margin-top: 18px;
  padding: 14px;
  border-radius: 8px;
  background: #f0fdfc;
  color: #4b5563;
}

.session-box strong {
  display: block;
  margin-top: 4px;
  color: #00a7a6;
}

.interview-card {
  min-height: 640px;
  display: flex;
  flex-direction: column;
}

.conversation {
  flex: 1;
  max-height: 620px;
  overflow-y: auto;
  padding-right: 8px;
}

.round {
  display: grid;
  gap: 14px;
  margin-bottom: 20px;
}

.message {
  max-width: 78%;
  padding: 16px 18px;
  border-radius: 8px;
  line-height: 1.8;
}

.message.ai {
  justify-self: start;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.message.mine {
  justify-self: end;
  color: #ffffff;
  background: #00bebd;
}

.message-label {
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 800;
  opacity: 0.78;
}

.feedback-card {
  justify-self: start;
  width: min(680px, 100%);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid rgba(0, 190, 189, 0.18);
  background: #f0fdfc;
}

.feedback-card > div {
  margin-top: 12px;
}

.feedback-card strong {
  color: #111827;
}

.feedback-card p {
  margin-top: 6px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
}

.answer-box {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #eef0f2;
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

.report {
  display: grid;
  gap: 18px;
  color: #374151;
}

.report-score {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background: #f0fdfc;
}

.report-score strong {
  color: #00a7a6;
  font-size: 30px;
}

.report h3 {
  margin-bottom: 8px;
  color: #111827;
}

.report p,
.report li {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
