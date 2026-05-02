<template>
  <div class="page">
    <AppHeader />

    <section class="search-panel">
      <div class="search-inner">
        <el-input
          v-model.trim="searchKeyword"
          size="large"
          placeholder="搜索职位、公司或技能关键词"
          clearable
          @keyup.enter="applySearch"
        />
        <el-button type="primary" size="large" @click="applySearch">搜索</el-button>
      </div>
    </section>

    <main class="content">
      <FilterBar v-model="filters" />

      <section class="list-panel">
        <div class="list-header">
          <div>
            <h1>岗位机会</h1>
            <p>共找到 {{ total }} 个相关岗位</p>
          </div>

          <div class="list-actions">
            <el-radio-group v-model="filters.sort">
              <el-radio-button label="relevance">相关度</el-radio-button>
              <el-radio-button label="latest">最新</el-radio-button>
              <el-radio-button label="salaryDesc">薪资最高</el-radio-button>
            </el-radio-group>
            <el-button text @click="resetFilters">清空筛选</el-button>
          </div>
        </div>

        <el-skeleton v-if="loading" :rows="8" animated />

        <div v-else-if="jobs.length" class="job-list">
          <JobCard v-for="job in jobs" :key="job.id || job.jobId" :job="job" />
        </div>

        <el-empty v-else description="暂无匹配岗位，试试调整关键词或筛选条件" />

        <div v-if="total > 0" class="pagination-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :current-page="pagination.page"
            :page-size="pagination.pageSize"
            :page-sizes="[10, 20, 30]"
            :total="total"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { searchJobs } from '../../api/job'
import AppHeader from '../../components/common/AppHeader.vue'
import FilterBar from '../../components/job/FilterBar.vue'
import JobCard from '../../components/job/JobCard.vue'

const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const loading = ref(false)
const jobs = ref([])
const total = ref(0)

const filters = ref({
  city: '',
  salaryKey: '',
  salaryMin: '',
  salaryMax: '',
  education: '',
  experience: '',
  industry: '',
  sort: 'relevance'
})

const pagination = reactive({
  page: 1,
  pageSize: 10
})

let syncingFromQuery = false

function normalizeList(data) {
  if (Array.isArray(data)) {
    return {
      records: data,
      total: data.length
    }
  }

  const records = data?.records || data?.list || data?.rows || []

  return {
    records: Array.isArray(records) ? records : [],
    total: Number(data?.total ?? records.length ?? 0)
  }
}

function getSalaryKey(min, max) {
  if (min === '' && max === '') return ''
  if (min === '' && Number(max) === 3) return '0-3'
  if (Number(min) === 3 && Number(max) === 5) return '3-5'
  if (Number(min) === 5 && Number(max) === 10) return '5-10'
  if (Number(min) === 10 && Number(max) === 20) return '10-20'
  if (Number(min) === 20 && max === '') return '20+'
  return ''
}

function syncFromQuery() {
  syncingFromQuery = true

  const salaryMin = route.query.salaryMin ?? ''
  const salaryMax = route.query.salaryMax ?? ''

  searchKeyword.value = route.query.keyword || ''
  filters.value = {
    city: route.query.city || '',
    salaryMin,
    salaryMax,
    salaryKey: route.query.salaryKey || getSalaryKey(salaryMin, salaryMax),
    education: route.query.education || '',
    experience: route.query.experience || '',
    industry: route.query.industry || '',
    sort: route.query.sort || 'relevance'
  }
  pagination.page = Number(route.query.page || 1)
  pagination.pageSize = Number(route.query.pageSize || 10)

  nextTick(() => {
    syncingFromQuery = false
  })
}

function cleanQuery(query) {
  return Object.entries(query).reduce((result, [key, value]) => {
    if (value !== '' && value !== undefined && value !== null) {
      result[key] = value
    }

    return result
  }, {})
}

function buildQuery() {
  return cleanQuery({
    keyword: searchKeyword.value,
    city: filters.value.city,
    salaryMin: filters.value.salaryMin,
    salaryMax: filters.value.salaryMax,
    salaryKey: filters.value.salaryKey,
    education: filters.value.education,
    experience: filters.value.experience,
    industry: filters.value.industry,
    sort: filters.value.sort,
    page: pagination.page,
    pageSize: pagination.pageSize
  })
}

function replaceQuery() {
  router.replace({
    path: '/jobs',
    query: buildQuery()
  })
}

function applySearch() {
  pagination.page = 1
  replaceQuery()
}

function handleFilterChange() {
  if (syncingFromQuery) {
    return
  }

  pagination.page = 1
  replaceQuery()
}

function handlePageChange(page) {
  pagination.page = page
  replaceQuery()
}

function handleSizeChange(pageSize) {
  pagination.pageSize = pageSize
  pagination.page = 1
  replaceQuery()
}

function resetFilters() {
  searchKeyword.value = ''
  filters.value = {
    city: '',
    salaryKey: '',
    salaryMin: '',
    salaryMax: '',
    education: '',
    experience: '',
    industry: '',
    sort: 'relevance'
  }
  pagination.page = 1
  pagination.pageSize = 10
  router.replace('/jobs')
}

async function fetchJobs() {
  loading.value = true

  try {
    const normalized = normalizeList(await searchJobs({
      keyword: searchKeyword.value,
      city: filters.value.city,
      salaryMin: filters.value.salaryMin,
      salaryMax: filters.value.salaryMax,
      education: filters.value.education,
      experience: filters.value.experience,
      industry: filters.value.industry,
      sort: filters.value.sort,
      page: pagination.page,
      pageSize: pagination.pageSize
    }))

    jobs.value = normalized.records
    total.value = normalized.total
  } catch (error) {
    jobs.value = []
    total.value = 0
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

watch(
  filters,
  () => handleFilterChange(),
  { deep: true }
)

watch(
  () => route.query,
  async () => {
    syncFromQuery()
    await nextTick()
    fetchJobs()
  }
)

onMounted(() => {
  syncFromQuery()
  fetchJobs()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.search-panel {
  border-bottom: 1px solid #eef0f2;
  background: linear-gradient(135deg, #eafffd 0%, #ffffff 100%);
}

.search-inner {
  display: grid;
  grid-template-columns: 1fr 132px;
  gap: 12px;
  max-width: 980px;
  margin: 0 auto;
  padding: 34px 24px;
}

.content {
  display: grid;
  gap: 22px;
  max-width: 1320px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.list-panel {
  min-width: 0;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
  padding: 20px 24px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
}

.list-header h1 {
  margin: 0;
  color: #111827;
  font-size: 22px;
}

.list-header p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.list-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 0 0 auto;
}

.job-list {
  display: grid;
  gap: 14px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
