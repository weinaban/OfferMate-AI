<template>
  <el-form ref="formRef" class="job-form" :model="form" :rules="rules" label-position="top">
    <section class="form-section">
      <div class="section-title">
        <span>01</span>
        <h2>基础信息</h2>
      </div>

      <el-form-item label="岗位名称" prop="title">
        <el-input v-model.trim="form.title" size="large" placeholder="例如：Java 后端开发工程师" />
      </el-form-item>

      <div class="basic-grid">
        <el-form-item label="工作城市" prop="city">
          <el-input v-model.trim="form.city" size="large" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="工作经验" prop="experience">
          <el-select v-model="form.experience" size="large" placeholder="请选择经验要求">
            <el-option label="不限" value="不限" />
            <el-option label="应届" value="应届" />
            <el-option label="1年内" value="1年内" />
            <el-option label="1-3年" value="1-3年" />
            <el-option label="3-5年" value="3-5年" />
            <el-option label="5年以上" value="5年以上" />
          </el-select>
        </el-form-item>
        <el-form-item label="学历要求" prop="education">
          <el-select v-model="form.education" size="large" placeholder="请选择学历要求">
            <el-option label="不限" value="不限" />
            <el-option label="大专" value="大专" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
          </el-select>
        </el-form-item>
      </div>
    </section>

    <section class="form-section">
      <div class="section-title">
        <span>02</span>
        <h2>薪资与标签</h2>
      </div>

      <div class="salary-row">
        <el-form-item label="最低薪资（K）" prop="salaryMin">
          <el-input-number v-model="form.salaryMin" :min="1" :max="200" controls-position="right" />
        </el-form-item>
        <div class="salary-separator">至</div>
        <el-form-item label="最高薪资（K）" prop="salaryMax">
          <el-input-number v-model="form.salaryMax" :min="1" :max="200" controls-position="right" />
        </el-form-item>
      </div>

      <el-form-item label="技能标签" prop="tags">
        <el-input v-model.trim="form.tags" size="large" placeholder="Java,Spring Boot,MySQL" />
      </el-form-item>
    </section>

    <section class="form-section">
      <div class="section-title description-title">
        <div>
          <span>03</span>
          <h2>岗位描述</h2>
        </div>
        <el-button
          v-if="enableAiDescription"
          type="success"
          plain
          :icon="MagicStick"
          :loading="aiLoading"
          @click="handleGenerateDescription"
        >
          AI 生成岗位描述
        </el-button>
      </div>

      <el-form-item prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="9"
          placeholder="建议包含：岗位职责、任职要求、团队情况、加分项。"
        />
      </el-form-item>
    </section>

    <div class="form-actions">
      <el-button size="large" @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" size="large" :loading="loading" @click="submit">{{ submitText }}</el-button>
    </div>
  </el-form>
</template>

<script setup>
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reactive, ref, watch } from 'vue'
import { generateJobDescription, normalizeAiText } from '../../api/ai'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  submitText: {
    type: String,
    default: '保存岗位'
  },
  enableAiDescription: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['submit', 'cancel'])
const formRef = ref(null)
const aiLoading = ref(false)

const form = reactive({
  title: '',
  salaryMin: 10,
  salaryMax: 20,
  city: '',
  experience: '',
  education: '',
  tags: '',
  description: ''
})

function validateSalary(rule, value, callback) {
  if (value === undefined || value === null || value === '') {
    callback(new Error('请输入薪资'))
  } else if (Number(form.salaryMin) > Number(form.salaryMax)) {
    callback(new Error('最低薪资不能大于最高薪资'))
  } else {
    callback()
  }
}

const rules = {
  title: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  salaryMin: [{ validator: validateSalary, trigger: 'change' }],
  salaryMax: [{ validator: validateSalary, trigger: 'change' }],
  city: [{ required: true, message: '请输入工作城市', trigger: 'blur' }],
  experience: [{ required: true, message: '请选择工作经验', trigger: 'change' }],
  education: [{ required: true, message: '请选择学历要求', trigger: 'change' }],
  description: [{ required: true, message: '请输入岗位描述', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (value) => {
    Object.assign(form, {
      title: value?.title || '',
      salaryMin: value?.salaryMin ?? 10,
      salaryMax: value?.salaryMax ?? 20,
      city: value?.city || '',
      experience: value?.experience || '',
      education: value?.education || '',
      tags: Array.isArray(value?.tags) ? value.tags.join(',') : value?.tags || '',
      description: value?.description || ''
    })
  },
  { immediate: true, deep: true }
)

async function handleGenerateDescription() {
  if (aiLoading.value) return

  if (!form.title) {
    ElMessage.warning('请先填写岗位名称')
    return
  }

  aiLoading.value = true

  try {
    const data = await generateJobDescription({
      title: form.title,
      city: form.city,
      experience: form.experience,
      education: form.education,
      tags: form.tags,
      salaryMin: Number(form.salaryMin),
      salaryMax: Number(form.salaryMax)
    })
    const nextDescription = normalizeAiText(data)

    if (!nextDescription) {
      ElMessage.warning('AI 暂未生成岗位描述，请稍后重试')
      return
    }

    if (form.description) {
      await ElMessageBox.confirm('当前岗位描述已有内容，是否使用 AI 生成结果覆盖？', '覆盖确认', {
        confirmButtonText: '覆盖',
        cancelButtonText: '取消',
        type: 'warning'
      })
    }

    form.description = nextDescription
    ElMessage.success('AI 岗位描述已生成')
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error('生成失败，请检查岗位信息后重试')
  } finally {
    aiLoading.value = false
  }
}

async function submit() {
  if (props.loading) {
    return
  }

  await formRef.value.validate((valid) => {
    if (!valid) {
      return
    }

    emit('submit', {
      ...form,
      salaryMin: Number(form.salaryMin),
      salaryMax: Number(form.salaryMax)
    })
  })
}
</script>

<style scoped>
.job-form {
  width: 100%;
}

.form-section + .form-section {
  margin-top: 26px;
  padding-top: 24px;
  border-top: 1px solid #eef0f2;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.section-title > div {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title span {
  width: 32px;
  height: 24px;
  border-radius: 4px;
  color: #00a7a6;
  background: #e9fbfb;
  font-size: 13px;
  font-weight: 800;
  line-height: 24px;
  text-align: center;
}

.section-title h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.description-title {
  justify-content: space-between;
}

.basic-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 22px;
}

.salary-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 32px minmax(0, 1fr);
  align-items: start;
  max-width: 560px;
}

.salary-separator {
  padding-top: 42px;
  color: #6b7280;
  text-align: center;
}

.basic-grid :deep(.el-select),
.salary-row :deep(.el-input-number) {
  width: 100%;
}

.job-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 700;
}

.job-form :deep(.el-textarea__inner) {
  line-height: 1.8;
}

.form-actions {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin: 30px -38px -38px;
  padding: 18px 38px;
  border-top: 1px solid #eef0f2;
  border-radius: 0 0 8px 8px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
}

.form-actions .el-button {
  min-width: 120px;
}
</style>
