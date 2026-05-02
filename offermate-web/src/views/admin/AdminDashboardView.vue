<template>
  <AdminLayout>
    <section class="page-header">
      <div>
        <h1>管理后台</h1>
        <p>集中处理平台用户、企业与岗位审核。</p>
      </div>
    </section>

    <section class="card-grid">
      <div v-for="item in cards" :key="item.title" class="entry-card">
        <span>{{ item.title }}</span>
        <strong>{{ item.total }}</strong>
        <p>{{ item.desc }}</p>
        <el-button type="primary" plain @click="router.push(item.path)">进入管理</el-button>
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminCompanyAudits, getAdminJobAudits, getAdminUsers } from '../../api/admin'
import AdminLayout from '../../components/admin/AdminLayout.vue'

const router = useRouter()
const userTotal = ref(0)
const companyTotal = ref(0)
const jobTotal = ref(0)

const cards = computed(() => [
  { title: '用户管理', total: userTotal.value, desc: '查看与启停平台用户', path: '/admin/users' },
  { title: '企业审核', total: companyTotal.value, desc: '处理企业资料审核', path: '/admin/companies' },
  { title: '岗位审核', total: jobTotal.value, desc: '处理岗位发布审核', path: '/admin/jobs' }
])

function getTotal(data) {
  if (Array.isArray(data)) return data.length
  return data?.total ?? data?.records?.length ?? data?.list?.length ?? data?.rows?.length ?? 0
}

async function fetchTotals() {
  try {
    userTotal.value = getTotal(await getAdminUsers({ page: 1, pageSize: 1 }))
  } catch (error) {
    userTotal.value = 0
  }

  try {
    companyTotal.value = getTotal(await getAdminCompanyAudits({ page: 1, pageSize: 1 }))
  } catch (error) {
    companyTotal.value = 0
  }

  try {
    jobTotal.value = getTotal(await getAdminJobAudits({ page: 1, pageSize: 1 }))
  } catch (error) {
    jobTotal.value = 0
  }
}

onMounted(fetchTotals)
</script>

<style scoped>
.page-header,
.entry-card {
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header {
  padding: 28px 32px;
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

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 22px;
}

.entry-card {
  padding: 28px;
}

.entry-card span {
  color: #6b7280;
  font-size: 15px;
}

.entry-card strong {
  display: block;
  margin-top: 14px;
  color: #00a7a6;
  font-size: 36px;
}

.entry-card p {
  margin: 10px 0 22px;
  color: #4b5563;
}
</style>
