<template>
  <el-form ref="formRef" class="resume-form" :model="form" :rules="rules" label-position="top">
    <el-form-item label="简历标题" prop="title">
      <el-input v-model.trim="form.title" size="large" placeholder="例如：Java 后端开发简历" />
    </el-form-item>

    <div class="form-grid">
      <el-form-item label="姓名" prop="realName">
        <el-input v-model.trim="form.realName" size="large" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model.trim="form.phone" size="large" placeholder="请输入电话" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model.trim="form.email" size="large" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="学历" prop="education">
        <el-select v-model="form.education" size="large" placeholder="请选择学历">
          <el-option label="大专" value="大专" />
          <el-option label="本科" value="本科" />
          <el-option label="硕士" value="硕士" />
          <el-option label="博士" value="博士" />
        </el-select>
      </el-form-item>
      <el-form-item label="工作年限" prop="experienceYear">
        <el-input-number v-model="form.experienceYear" :min="0" :max="50" controls-position="right" />
      </el-form-item>
    </div>

    <el-form-item prop="skill">
      <template #label>
        <div class="field-label">
          <span>技能描述</span>
          <el-button
            v-if="showAiOptimize"
            type="success"
            plain
            size="small"
            :loading="aiLoadingSection === 'skill'"
            @click.prevent="submitForAi('skill')"
          >
            AI优化技能
          </el-button>
        </div>
      </template>
      <el-input v-model="form.skill" type="textarea" :rows="4" placeholder="例如：Java, Spring Boot, MySQL" />
    </el-form-item>

    <el-form-item prop="projectExp">
      <template #label>
        <div class="field-label">
          <span>项目经历</span>
          <el-button
            v-if="showAiOptimize"
            type="success"
            plain
            size="small"
            :loading="aiLoadingSection === 'projectExp'"
            @click.prevent="submitForAi('projectExp')"
          >
            AI优化项目
          </el-button>
        </div>
      </template>
      <el-input v-model="form.projectExp" type="textarea" :rows="5" placeholder="请描述你的项目经历" />
    </el-form-item>

    <el-form-item prop="selfIntro">
      <template #label>
        <div class="field-label">
          <span>自我评价</span>
          <el-button
            v-if="showAiOptimize"
            type="success"
            plain
            size="small"
            :loading="aiLoadingSection === 'selfIntro'"
            @click.prevent="submitForAi('selfIntro')"
          >
            AI优化评价
          </el-button>
        </div>
      </template>
      <el-input v-model="form.selfIntro" type="textarea" :rows="4" placeholder="请描述你的优势与求职意向" />
    </el-form-item>

    <div class="form-actions">
      <el-button size="large" @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" size="large" :loading="loading" @click="submit">保存简历</el-button>
    </div>
  </el-form>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  aiLoadingSection: {
    type: String,
    default: ''
  },
  showAiOptimize: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel', 'ai-optimize'])
const formRef = ref(null)

const form = reactive({
  title: '',
  realName: '',
  phone: '',
  email: '',
  education: '',
  experienceYear: 0,
  skill: '',
  projectExp: '',
  selfIntro: ''
})

const rules = {
  title: [{ required: true, message: '请输入简历标题', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  education: [{ required: true, message: '请选择学历', trigger: 'change' }]
}

watch(
  () => props.modelValue,
  (value) => {
    Object.assign(form, {
      title: value?.title || '',
      realName: value?.realName || '',
      phone: value?.phone || '',
      email: value?.email || '',
      education: value?.education || '',
      experienceYear: value?.experienceYear ?? 0,
      skill: value?.skill || '',
      projectExp: value?.projectExp || '',
      selfIntro: value?.selfIntro || ''
    })
  },
  { immediate: true, deep: true }
)

async function validateForm() {
  let isValid = false
  await formRef.value.validate((valid) => {
    isValid = valid
  })
  return isValid
}

async function submit() {
  if (props.loading) return

  if (await validateForm()) {
    emit('submit', { ...form })
  }
}

async function submitForAi(section) {
  if (props.loading || props.aiLoadingSection) return

  if (await validateForm()) {
    emit('ai-optimize', section, { ...form })
  }
}

function applySectionResult(section, text) {
  const value = cleanAiText(text)

  if (section === 'skill') {
    form.skill = value
  } else if (section === 'projectExp') {
    form.projectExp = value
  } else if (section === 'selfIntro') {
    form.selfIntro = value
  }
}

function cleanAiText(text) {
  return String(text || '')
    .replace(/\r\n/g, '\n')
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/\*\*/g, '')
    .replace(/^#+\s*/gm, '')
    .replace(/^\s*[-*]\s+/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

defineExpose({
  applySectionResult
})
</script>

<style scoped>
.resume-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 22px;
}

.form-grid :deep(.el-select),
.form-grid :deep(.el-input-number) {
  width: 100%;
}

.field-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 16px;
}

.field-label .el-button {
  font-weight: 700;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
}

.form-actions .el-button {
  min-width: 120px;
}
</style>
