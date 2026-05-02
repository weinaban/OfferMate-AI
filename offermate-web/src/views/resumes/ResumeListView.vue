<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <div class="page-header">
        <div>
          <h1>我的简历</h1>
          <p>维护你的求职资料，用更完整的简历提高沟通效率。</p>
        </div>
        <el-button type="primary" size="large" @click="router.push('/resumes/create')">新建简历</el-button>
      </div>

      <section class="attachment-card">
        <div>
          <h2>附件简历</h2>
          <p>支持上传 PDF、DOC、DOCX，本阶段仅展示上传链接。</p>
          <a v-if="attachmentUrl" :href="attachmentUrl" target="_blank" rel="noreferrer">{{ attachmentUrl }}</a>
        </div>
        <FileUploadButton
          biz-type="resumeAttachment"
          accept-type="document"
          text="上传附件简历"
          loading-text="上传中"
          @success="handleAttachmentUploaded"
        />
      </section>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else-if="resumes.length" class="resume-list">
        <ResumeCard
          v-for="resume in resumes"
          :key="resume.id"
          :resume="resume"
          @preview="goPreview"
          @edit="goEdit"
          @set-default="handleSetDefault"
          @delete="handleDelete"
        />
      </div>

      <el-empty v-else description="还没有简历">
        <el-button type="primary" @click="router.push('/resumes/create')">新建简历</el-button>
      </el-empty>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteResume, getMyResumes, setDefaultResume } from '../../api/resume'
import AppHeader from '../../components/common/AppHeader.vue'
import FileUploadButton from '../../components/common/FileUploadButton.vue'
import ResumeCard from '../../components/resume/ResumeCard.vue'

const router = useRouter()
const loading = ref(false)
const resumes = ref([])
const attachmentUrl = ref('')

async function fetchResumes() {
  loading.value = true

  try {
    const data = await getMyResumes()
    resumes.value = Array.isArray(data) ? data : data?.records || []
  } finally {
    loading.value = false
  }
}

function handleAttachmentUploaded(url) {
  attachmentUrl.value = url
}

function goPreview(resume) {
  router.push(`/resumes/${resume.id}`)
}

function goEdit(resume) {
  router.push(`/resumes/edit/${resume.id}`)
}

async function handleSetDefault(resume) {
  await setDefaultResume(resume.id)
  ElMessage.success('默认简历已更新')
  fetchResumes()
}

async function handleDelete(resume) {
  try {
    await ElMessageBox.confirm(`确认删除「${resume.title || '未命名简历'}」吗？`, '删除简历', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }

  await deleteResume(resume.id)
  ElMessage.success('简历已删除')
  fetchResumes()
}

onMounted(fetchResumes)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 72px;
}

.page-header,
.attachment-card {
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

h1,
h2 {
  margin: 0;
  color: #111827;
}

h1 {
  font-size: 28px;
}

h2 {
  font-size: 20px;
}

.page-header p,
.attachment-card p {
  margin: 10px 0 0;
  color: #6b7280;
}

.attachment-card a {
  display: block;
  max-width: 760px;
  margin-top: 10px;
  overflow: hidden;
  color: #00a7a6;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-list {
  display: grid;
  gap: 16px;
}
</style>
