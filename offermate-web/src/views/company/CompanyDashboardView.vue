<template>
  <CompanyLayout>
    <section class="page-header">
      <div>
        <h1>招聘者工作台</h1>
        <p>管理岗位发布与候选人投递进展。</p>
      </div>
      <el-button type="primary" size="large" @click="router.push('/company/jobs/create')">发布新岗位</el-button>
    </section>

    <section class="stat-grid">
      <div v-for="item in stats" :key="item.label" class="stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </section>

    <section class="quick-card">
      <h2>快捷入口</h2>
      <div class="quick-actions">
        <el-button type="primary" @click="router.push('/company/jobs/create')">发布新岗位</el-button>
        <el-button @click="router.push('/company/jobs')">查看岗位管理</el-button>
        <el-button @click="router.push('/company/deliveries')">查看收到的投递</el-button>
      </div>
    </section>
  </CompanyLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCompanyDeliveries } from '../../api/companyDelivery'
import { getCompanyJobs } from '../../api/companyJob'
import CompanyLayout from '../../components/company/CompanyLayout.vue'

const router = useRouter()
const jobs = ref([])
const deliveries = ref([])

const stats = computed(() => [
  { label: '我的岗位', value: jobs.value.length },
  { label: '收到投递', value: deliveries.value.length },
  { label: '待处理', value: deliveries.value.filter((item) => Number(item.status || 1) === 1).length },
  { label: '已邀面试', value: deliveries.value.filter((item) => Number(item.status) === 4).length }
])

function normalizeList(data) {
  return Array.isArray(data) ? data : data?.records || []
}

async function fetchDashboard() {
  try {
    jobs.value = normalizeList(await getCompanyJobs())
  } catch (error) {
    jobs.value = []
  }

  try {
    deliveries.value = normalizeList(await getCompanyDeliveries())
  } catch (error) {
    deliveries.value = []
  }
}

onMounted(fetchDashboard)
</script>

<style scoped>
.page-header,
.quick-card {
  padding: 28px 32px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin-top: 22px;
}

.stat-card {
  padding: 26px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.stat-card span {
  color: #6b7280;
  font-size: 14px;
}

.stat-card strong {
  display: block;
  margin-top: 14px;
  color: #00a7a6;
  font-size: 34px;
}

.quick-card {
  margin-top: 22px;
}

.quick-card h2 {
  margin: 0 0 18px;
  color: #111827;
  font-size: 22px;
}

.quick-actions {
  display: flex;
  gap: 12px;
}
</style>
