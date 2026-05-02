<template>
  <div class="page">
    <AppHeader />

    <section class="hero">
      <div class="hero-inner">
        <p class="kicker">OfferMate AI</p>
        <h1>找工作，上 OfferMate AI</h1>
        <p class="subtitle">AI 驱动的智能招聘平台</p>

        <div class="search-box">
          <el-input
            v-model.trim="keyword"
            size="large"
            placeholder="搜索职位、公司或技能关键词"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>
      </div>
    </section>

    <main class="content">
      <section class="city-section">
        <div class="section-title">
          <h2>热门城市</h2>
          <span>快速发现高质量机会</span>
        </div>

        <div class="city-list">
          <button v-for="city in hotCities" :key="city" type="button" @click="goCity(city)">
            {{ city }}
          </button>
        </div>
      </section>

      <section class="recommend-section">
        <div class="section-title">
          <h2>推荐岗位</h2>
          <router-link to="/jobs">查看更多</router-link>
        </div>

        <el-skeleton v-if="loading" :rows="5" animated />

        <div v-else-if="jobs.length" class="job-list">
          <JobCard v-for="job in jobs" :key="job.id" :job="job" />
        </div>

        <el-empty v-else description="暂无推荐岗位" />
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getJobPage } from '../../api/job'
import AppHeader from '../../components/common/AppHeader.vue'
import JobCard from '../../components/job/JobCard.vue'

const router = useRouter()
const keyword = ref('')
const loading = ref(false)
const jobs = ref([])

const hotCities = ['北京', '上海', '深圳', '广州', '杭州', '成都', '武汉', '南京', '苏州']

function handleSearch() {
  router.push({
    path: '/jobs',
    query: keyword.value ? { keyword: keyword.value } : {}
  })
}

function goCity(city) {
  router.push({
    path: '/jobs',
    query: { city }
  })
}

async function fetchRecommendJobs() {
  loading.value = true

  try {
    const data = await getJobPage({
      page: 1,
      pageSize: 8
    })

    jobs.value = data?.records || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchRecommendJobs)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.hero {
  background:
    radial-gradient(circle at 22% 24%, rgba(0, 190, 189, 0.18), transparent 30%),
    linear-gradient(135deg, #eafffd 0%, #f7fbff 62%, #ffffff 100%);
}

.hero-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 82px 24px 88px;
  text-align: center;
}

.kicker {
  margin: 0 0 14px;
  color: #00a7a6;
  font-size: 20px;
  font-weight: 800;
}

h1 {
  margin: 0;
  color: #102a43;
  font-size: 52px;
  line-height: 1.16;
  font-weight: 800;
}

.subtitle {
  margin: 18px 0 0;
  color: #52616f;
  font-size: 20px;
}

.search-box {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 12px;
  max-width: 760px;
  margin: 42px auto 0;
  padding: 10px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 18px 48px rgba(15, 98, 103, 0.12);
}

.search-box :deep(.el-input__wrapper) {
  box-shadow: none;
  background: #f7f9fb;
}

.content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 34px 24px 72px;
}

.city-section,
.recommend-section {
  margin-top: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.section-title h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
}

.section-title span,
.section-title a {
  color: #6b7280;
  font-size: 14px;
}

.section-title a:hover {
  color: #00a7a6;
}

.city-list {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  padding: 24px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.city-list button {
  min-width: 86px;
  height: 40px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  color: #374151;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.city-list button:hover {
  color: #00a7a6;
  border-color: rgba(0, 190, 189, 0.36);
  background: #e9fbfb;
}

.job-list {
  display: grid;
  gap: 16px;
}
</style>
