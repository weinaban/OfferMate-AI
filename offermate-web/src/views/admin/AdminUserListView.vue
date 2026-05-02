<template>
  <AdminLayout>
    <section class="table-card">
      <div class="card-header">
        <div>
          <h1>用户管理</h1>
          <p>查看用户信息并维护启用状态。</p>
        </div>
      </div>

      <div class="toolbar">
        <el-input v-model.trim="query.keyword" size="large" placeholder="搜索用户名/手机号" clearable @keyup.enter="fetchUsers" />
        <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        <el-button size="large" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="users" border>
        <el-table-column prop="id" label="用户ID" width="90" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="头像" width="90">
          <template #default="{ row }">
            <LogoAvatar :src="row.avatar" :name="row.username" type="user" size="sm" />
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110">
          <template #default="{ row }">{{ roleText(row.role) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 0 ? 'danger' : 'success'" effect="plain">
              {{ Number(row.status) === 0 ? '禁用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="Number(row.status) === 0"
              text
              type="primary"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button v-else text type="danger" @click="handleDisable(row)">禁用</el-button>
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
          @current-change="fetchUsers"
        />
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { disableAdminUser, enableAdminUser, getAdminUsers } from '../../api/admin'
import AdminLayout from '../../components/admin/AdminLayout.vue'
import LogoAvatar from '../../components/common/LogoAvatar.vue'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const query = reactive({
  keyword: '',
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

function roleText(role) {
  const map = {
    1: '求职者',
    2: '招聘者',
    3: '管理员'
  }
  return map[Number(role)] || '未知'
}

async function fetchUsers() {
  loading.value = true

  try {
    const pageData = normalizePage(await getAdminUsers(query))
    users.value = pageData.records
    total.value = pageData.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchUsers()
}

function handleReset() {
  query.keyword = ''
  query.page = 1
  fetchUsers()
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm(`确认禁用用户「${row.username || row.id}」吗？`, '禁用用户', {
      type: 'warning',
      confirmButtonText: '禁用',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }

  await disableAdminUser(row.id)
  ElMessage.success('用户已禁用')
  fetchUsers()
}

async function handleEnable(row) {
  try {
    await ElMessageBox.confirm(`确认启用用户「${row.username || row.id}」吗？`, '启用用户', {
      type: 'info',
      confirmButtonText: '启用',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }

  await enableAdminUser(row.id)
  ElMessage.success('用户已启用')
  fetchUsers()
}

onMounted(fetchUsers)
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
  grid-template-columns: 320px 96px 96px;
  gap: 12px;
  margin-bottom: 18px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
