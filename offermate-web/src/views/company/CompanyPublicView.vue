<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <el-skeleton v-if="loading" :rows="10" animated />

      <template v-else-if="company">
        <section class="company-hero">
          <LogoAvatar :src="companyLogo" :name="companyName" size="lg" />
          <div class="hero-main">
            <h1>{{ companyName }}</h1>
            <div class="meta-row">
              <span>{{ company.industry || '行业信息完善中' }}</span>
              <span>{{ company.scale || company.companyScale || '规模信息完善中' }}</span>
              <span>{{ company.address || '地址信息完善中' }}</span>
            </div>
          </div>
        </section>

        <section class="section-card">
          <h2>公司介绍</h2>
          <p>{{ company.intro || company.companyIntro || '公司介绍正在完善中。' }}</p>
        </section>

        <section class="section-card">
          <h2>在招岗位</h2>
          <div v-if="jobs.length" class="job-list">
            <JobCard v-for="job in jobs" :key="job.id" :job="mergeCompanyInfo(job)" />
          </div>
          <el-empty v-else description="暂无在招岗位" />
        </section>
      </template>

      <el-empty v-else description="公司信息不存在" />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getCompanyPublicInfo } from '../../api/company'
import AppHeader from '../../components/common/AppHeader.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'
import JobCard from '../../components/job/JobCard.vue'

const route = useRoute()
const loading = ref(false)
const company = ref(null)

const companyName = computed(() => company.value?.companyName || '优质企业')
const companyLogo = computed(() => company.value?.logo || company.value?.companyLogo || '')
const jobs = computed(() => (Array.isArray(company.value?.jobs) ? company.value.jobs : []))

function mergeCompanyInfo(job) {
  return {
    ...job,
    companyName: job.companyName || companyName.value,
    logo: job.logo || companyLogo.value,
    industry: job.industry || company.value?.industry,
    scale: job.scale || company.value?.scale || company.value?.companyScale
  }
}

async function fetchCompany() {
  loading.value = true

  try {
    company.value = await getCompanyPublicInfo(route.params.id)
  } finally {
    loading.value = false
  }
}

onMounted(fetchCompany)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.company-hero,
.section-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
}

.company-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 34px 38px;
  background: linear-gradient(135deg, #eafffd 0%, #ffffff 72%);
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 34px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.meta-row span {
  padding: 7px 12px;
  border-radius: 4px;
  color: #4b5563;
  background: #f5f7fa;
}

.section-card {
  margin-top: 22px;
  padding: 30px 34px;
}

.section-card h2 {
  margin: 0 0 18px;
  color: #111827;
  font-size: 22px;
}

.section-card p {
  margin: 0;
  color: #374151;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-line;
}

.job-list {
  display: grid;
  gap: 14px;
}
</style>
