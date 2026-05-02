<template>
  <AdminLayout>
    <section class="table-card">
      <div class="card-header">
        <div>
          <h1>企业审核</h1>
          <p>审核企业资料，维护平台招聘主体质量。</p>
        </div>
      </div>

      <div class="toolbar">
        <el-input v-model.trim="query.keyword" size="large" placeholder="搜索企业名称/行业" clearable @keyup.enter="fetchCompanies" />
        <el-select v-model="query.auditStatus" size="large" placeholder="审核状态" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="审核拒绝" :value="2" />
        </el-select>
        <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        <el-button size="large" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="companies" border>
        <el-table-column prop="id" label="企业ID" width="90" />
        <el-table-column label="Logo" width="90">
          <template #default="{ row }">
            <LogoAvatar :src="row.logo || row.companyLogo" :name="row.companyName" size="sm" />
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="企业名称" min-width="180" />
        <el-table-column prop="industry" label="行业" min-width="120" />
        <el-table-column label="规模" min-width="120">
          <template #default="{ row }">{{ row.scale || row.companyScale || '-' }}</template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="auditType(row.auditStatus)" effect="plain">{{ auditText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="190" fixed="right" align="center">
          <template #default="{ row }">
            <div class="audit-actions">
              <el-button
                size="small"
                type="primary"
                plain
                :disabled="Number(row.auditStatus) === 1"
                @click="handleAudit(row, 1)"
              >
                {{ Number(row.auditStatus) === 1 ? '已通过' : '通过' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                :disabled="Number(row.auditStatus) === 2"
                @click="handleAudit(row, 2)"
              >
                {{ Number(row.auditStatus) === 2 ? '已拒绝' : '拒绝' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="prev, pager, next, jumper"
          :total="total"
          @current-change="fetchCompanies"
        />
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditAdminCompany, getAdminCompanyAudits } from '../../api/admin'
import AdminLayout from '../../components/admin/AdminLayout.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'

const loading = ref(false)
const companies = ref([])
const total = ref(0)
const query = reactive({
  keyword: '',
  auditStatus: '',
  page: 1,
  pageSize: 10
})

function normalizePage(data) {
  const records = Array.isArray(data) ? data : data?.records || data?.list || data?.rows || []
  return {
    records,
    total: data?.total ?? records.length
  }
}

function auditText(status) {
  const map = {
    0: '待审核',
    1: '审核通过',
    2: '审核拒绝'
  }
  return map[Number(status)] || '待审核'
}

function auditType(status) {
  const statusNumber = Number(status)
  if (statusNumber === 1) return 'success'
  if (statusNumber === 2) return 'danger'
  return 'warning'
}

async function fetchCompanies() {
  loading.value = true

  try {
    const pageData = normalizePage(await getAdminCompanyAudits(query))
    companies.value = pageData.records
    total.value = pageData.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchCompanies()
}

function handleReset() {
  query.keyword = ''
  query.auditStatus = ''
  query.page = 1
  fetchCompanies()
}

async function handleAudit(row, status) {
  if (Number(row.auditStatus) === Number(status)) {
    return
  }

  const text = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${text}企业「${row.companyName || row.id}」吗？`, '企业审核', {
      type: 'warning',
      confirmButtonText: text,
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }

  await auditAdminCompany(row.id, status)
  row.auditStatus = status
  ElMessage.success(`企业审核已${text}`)
  fetchCompanies()
}

onMounted(fetchCompanies)
</script>

<style scoped>
.table-card {
  padding: 28px 32px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.card-header {
  margin-bottom: 22px;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 26px;
}

.card-header p {
  margin: 8px 0 0;
  color: #6b7280;
}

.toolbar {
  display: grid;
  grid-template-columns: 320px 180px 96px 96px;
  gap: 12px;
  margin-bottom: 18px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.audit-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.audit-actions .el-button {
  margin-left: 0;
}
</style>
