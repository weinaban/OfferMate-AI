<template>
  <CompanyLayout>
    <section class="profile-card">
      <div class="page-header">
        <div>
          <h1>企业资料</h1>
          <p>完善企业信息，让求职者更快建立信任。</p>
        </div>
      </div>

      <el-skeleton v-if="pageLoading" :rows="10" animated />

      <div v-else class="profile-body">
        <aside class="logo-panel">
          <LogoAvatar :src="form.logo" :name="form.companyName" size="lg" />
          <strong>{{ form.companyName || '公司名称' }}</strong>
          <p>{{ form.industry || '行业' }} · {{ form.scale || '规模' }}</p>
          <FileUploadButton
            class="upload-btn"
            biz-type="companyLogo"
            text="上传 Logo"
            loading-text="上传中"
            @success="handleLogoUploaded"
          />
        </aside>

        <el-form ref="formRef" class="profile-form" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model.trim="form.companyName" size="large" placeholder="请输入公司名称" />
            </el-form-item>
            <el-form-item label="公司 Logo URL" prop="logo">
              <el-input v-model.trim="form.logo" size="large" placeholder="上传后自动回填，也可手动输入 URL" />
            </el-form-item>
            <el-form-item label="所属行业" prop="industry">
              <el-input v-model.trim="form.industry" size="large" placeholder="例如：互联网" />
            </el-form-item>
            <el-form-item label="公司规模" prop="scale">
              <el-select v-model="form.scale" size="large" placeholder="请选择公司规模">
                <el-option label="0-20人" value="0-20人" />
                <el-option label="20-99人" value="20-99人" />
                <el-option label="100-499人" value="100-499人" />
                <el-option label="500-999人" value="500-999人" />
                <el-option label="1000人以上" value="1000人以上" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="公司地址" prop="address">
            <el-input v-model.trim="form.address" size="large" placeholder="请输入公司地址" />
          </el-form-item>

          <el-form-item label="公司简介" prop="intro">
            <el-input
              v-model="form.intro"
              type="textarea"
              :rows="7"
              placeholder="介绍公司业务、团队氛围、发展阶段等"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存资料</el-button>
          </div>
        </el-form>
      </div>
    </section>
  </CompanyLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCompanyInfo, updateCompanyInfo } from '../../api/company'
import FileUploadButton from '../../components/common/FileUploadButton.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'

const formRef = ref(null)
const pageLoading = ref(false)
const saving = ref(false)

const form = reactive({
  companyName: '',
  logo: '',
  industry: '',
  scale: '',
  address: '',
  intro: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  industry: [{ required: true, message: '请输入所属行业', trigger: 'blur' }],
  scale: [{ required: true, message: '请选择公司规模', trigger: 'change' }]
}

async function fetchCompanyInfo() {
  pageLoading.value = true

  try {
    const data = await getCompanyInfo()
    Object.assign(form, {
      companyName: data?.companyName || '',
      logo: data?.logo || data?.companyLogo || '',
      industry: data?.industry || '',
      scale: data?.scale || data?.companyScale || '',
      address: data?.address || '',
      intro: data?.intro || data?.companyIntro || ''
    })
  } catch (error) {
    ElMessage.error('企业资料获取失败，请稍后重试')
  } finally {
    pageLoading.value = false
  }
}

function handleLogoUploaded(url) {
  form.logo = url
  ElMessage.success('Logo 已上传，请记得保存企业资料')
}

async function handleSave() {
  if (saving.value) {
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    saving.value = true

    try {
      await updateCompanyInfo({ ...form })
      ElMessage.success('企业资料保存成功')
    } finally {
      saving.value = false
    }
  })
}

onMounted(fetchCompanyInfo)
</script>

<style scoped>
.profile-card {
  max-width: 1180px;
  padding: 30px 36px 36px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
}

.page-header {
  margin-bottom: 26px;
  padding-bottom: 22px;
  border-bottom: 1px solid #eef0f2;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.page-header p {
  margin: 10px 0 0;
  color: #6b7280;
}

.profile-body {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 34px;
}

.logo-panel {
  align-self: start;
  padding: 26px;
  border-radius: 8px;
  background: #f7f9fb;
  text-align: center;
}

.logo-panel strong {
  display: block;
  margin-top: 18px;
  color: #111827;
  font-size: 18px;
}

.logo-panel p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.upload-btn {
  display: block;
  margin-top: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 22px;
}

.profile-form :deep(.el-select) {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
