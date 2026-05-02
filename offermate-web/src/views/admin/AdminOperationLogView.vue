<template>
  <AdminLayout>
    <section class="table-card">
      <div class="card-header">
        <div>
          <h1>操作日志</h1>
          <p>查询管理员与平台用户的关键操作记录，便于追踪问题和审计。</p>
        </div>
      </div>

      <div class="toolbar">
        <el-input
          v-model.trim="query.username"
          size="large"
          placeholder="用户名"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model.trim="query.module"
          size="large"
          placeholder="模块"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model.trim="query.operation"
          size="large"
          placeholder="操作"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" size="large" placeholder="状态" clearable>
          <el-option label="全部" value="" />
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          class="date-range"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          size="large"
        />
        <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        <el-button size="large" @click="handleReset">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="logs"
        border
        empty-text="暂无操作日志"
      >
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ roleText(row.role) }}</template>
        </el-table-column>
        <el-table-column prop="module" label="模块" min-width="120" show-overflow-tooltip />
        <el-table-column prop="operation" label="操作" min-width="130" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="100" />
        <el-table-column prop="requestUri" label="请求路径" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'danger'" effect="plain">
              {{ Number(row.status) === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="110">
          <template #default="{ row }">{{ formatCostTime(row.costTime) }}</template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" min-width="130" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="fetchLogs"
        />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="900px">
      <template v-if="currentLog">
        <section class="detail-section">
          <h2>基本信息</h2>
          <div class="detail-grid">
            <div><span>用户ID</span><strong>{{ displayValue(currentLog.userId) }}</strong></div>
            <div><span>用户名</span><strong>{{ displayValue(currentLog.username) }}</strong></div>
            <div><span>角色</span><strong>{{ roleText(currentLog.role) }}</strong></div>
            <div><span>模块</span><strong>{{ displayValue(currentLog.module) }}</strong></div>
            <div><span>操作</span><strong>{{ displayValue(currentLog.operation) }}</strong></div>
            <div><span>方法</span><strong>{{ displayValue(currentLog.method) }}</strong></div>
            <div><span>请求方式</span><strong>{{ displayValue(currentLog.requestMethod) }}</strong></div>
            <div><span>请求路径</span><strong>{{ displayValue(currentLog.requestUri) }}</strong></div>
            <div><span>IP</span><strong>{{ displayValue(currentLog.ip) }}</strong></div>
            <div>
              <span>状态</span>
              <strong>{{ Number(currentLog.status) === 1 ? '成功' : '失败' }}</strong>
            </div>
            <div><span>耗时</span><strong>{{ formatCostTime(currentLog.costTime) }}</strong></div>
            <div><span>创建时间</span><strong>{{ displayValue(currentLog.createTime) }}</strong></div>
          </div>
        </section>

        <section class="detail-section">
          <h2>请求参数</h2>
          <pre>{{ formatLongText(currentLog.params) }}</pre>
        </section>

        <section class="detail-section">
          <h2>返回结果</h2>
          <pre>{{ formatLongText(currentLog.result) }}</pre>
        </section>

        <section class="detail-section">
          <h2>异常信息</h2>
          <pre>{{ formatLongText(currentLog.errorMsg) }}</pre>
        </section>
      </template>

      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { getAdminOperationLogs } from '../../api/admin'
import AdminLayout from '../../components/admin/AdminLayout.vue'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

const query = reactive({
  username: '',
  module: '',
  operation: '',
  status: '',
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

function buildParams() {
  return {
    ...query,
    startTime: dateRange.value?.[0] || '',
    endTime: dateRange.value?.[1] || ''
  }
}

async function fetchLogs() {
  loading.value = true

  try {
    const pageData = normalizePage(await getAdminOperationLogs(buildParams()))
    logs.value = pageData.records
    total.value = pageData.total
  } catch (error) {
    ElMessage.error(error?.message || '操作日志加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchLogs()
}

function handleReset() {
  query.username = ''
  query.module = ''
  query.operation = ''
  query.status = ''
  query.page = 1
  query.pageSize = 10
  dateRange.value = []
  fetchLogs()
}

function handleSizeChange() {
  query.page = 1
  fetchLogs()
}

function openDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}

function roleText(role) {
  const map = {
    1: '求职者',
    2: '招聘者',
    3: '管理员'
  }
  return map[Number(role)] || '-'
}

function formatCostTime(costTime) {
  if (costTime === undefined || costTime === null || costTime === '') return '-'
  return `${costTime} ms`
}

function displayValue(value) {
  if (value === undefined || value === null || value === '') return '-'
  return value
}

function formatLongText(value) {
  if (value === undefined || value === null || value === '') return '-'

  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }

  const text = String(value)
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch (error) {
    return text
  }
}

onMounted(fetchLogs)
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
  grid-template-columns: 180px 180px 180px 150px minmax(340px, 1fr) 92px 92px;
  gap: 12px;
  margin-bottom: 18px;
}

.date-range {
  width: 100%;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-section + .detail-section {
  margin-top: 22px;
}

.detail-section h2 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.detail-grid div {
  min-width: 0;
  padding: 12px;
  border-radius: 6px;
  background: #f7f9fb;
}

.detail-grid span {
  display: block;
  margin-bottom: 6px;
  color: #8a94a6;
  font-size: 13px;
}

.detail-grid strong {
  display: block;
  overflow: hidden;
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

pre {
  max-height: 280px;
  margin: 0;
  overflow: auto;
  padding: 14px 16px;
  border: 1px solid #eef0f2;
  border-radius: 6px;
  color: #374151;
  background: #f7f9fb;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
