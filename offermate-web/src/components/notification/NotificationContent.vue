<template>
  <main class="notification-page" :class="{ wide: mode === 'wide' }">
    <section class="notification-card">
      <div class="page-head">
        <div>
          <h1>通知中心</h1>
          <p>及时查看投递、审核、AI、面试与系统通知。</p>
        </div>
        <el-button
          type="primary"
          :loading="readAllLoading"
          :disabled="notificationStore.unreadCount <= 0"
          @click="handleReadAll"
        >
          全部已读
        </el-button>
      </div>

      <div class="filters">
        <el-select v-model="query.type" placeholder="通知类型" @change="handleFilterChange">
          <el-option
            v-for="item in typeOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="query.isRead" placeholder="是否已读" @change="handleFilterChange">
          <el-option
            v-for="item in readOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button @click="handleFilterChange">刷新</el-button>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else-if="notifications.length" class="notification-list">
        <article
          v-for="item in notifications"
          :key="getNotificationId(item)"
          class="notification-item"
          :class="{ unread: isUnread(item) }"
          @click="handleNotificationClick(item)"
        >
          <div class="item-main">
            <div class="item-title">
              <el-tag :type="getTypeMeta(item.type).tag" effect="light">
                {{ getTypeMeta(item.type).label }}
              </el-tag>
              <strong>{{ item.title || '通知提醒' }}</strong>
              <el-tag v-if="isUnread(item)" type="danger" effect="dark">未读</el-tag>
              <el-tag v-else type="info" effect="plain">已读</el-tag>
            </div>
            <p>{{ item.content || '暂无通知内容' }}</p>
          </div>
          <time>{{ item.createTime || '' }}</time>
        </article>
      </div>

      <el-empty v-else description="暂无通知" />

      <div v-if="total > 0" class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="query.page"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead
} from '../../api/notification'
import { useNotificationStore } from '../../stores/notification'
import { useUserStore } from '../../stores/user'

defineProps({
  mode: {
    type: String,
    default: 'normal'
  }
})

const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const loading = ref(false)
const readAllLoading = ref(false)
const notifications = ref([])
const total = ref(0)
const query = ref({
  page: 1,
  pageSize: 10,
  type: '',
  isRead: ''
})

const typeOptions = [
  { label: '全部类型', value: '' },
  { label: '投递', value: 1 },
  { label: '审核', value: 2 },
  { label: 'AI', value: 3 },
  { label: '面试', value: 4 },
  { label: '系统', value: 5 }
]

const readOptions = [
  { label: '全部状态', value: '' },
  { label: '未读', value: 0 },
  { label: '已读', value: 1 }
]

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

function getNotificationId(item) {
  return item.id || item.notificationId
}

function isUnread(item) {
  return item.isRead === 0 || item.isRead === false || item.read === 0 || item.read === false
}

function normalizeType(type) {
  const value = String(type || '').toLowerCase()
  const aliases = {
    1: 'delivery',
    delivery: 'delivery',
    2: 'audit',
    audit: 'audit',
    3: 'ai',
    ai: 'ai',
    4: 'interview',
    interview: 'interview',
    5: 'system',
    system: 'system'
  }

  return aliases[value] || 'system'
}

function getTypeMeta(type) {
  const map = {
    delivery: { label: '投递', tag: 'success' },
    audit: { label: '审核', tag: 'warning' },
    ai: { label: 'AI', tag: 'primary' },
    interview: { label: '面试', tag: 'danger' },
    system: { label: '系统', tag: 'info' }
  }

  return map[normalizeType(type)] || map.system
}

function sortByCreateTime(list) {
  return [...list].sort((a, b) => {
    const left = new Date(a.createTime || 0).getTime()
    const right = new Date(b.createTime || 0).getTime()
    return right - left
  })
}

function getNotificationTarget(item) {
  const customTarget = item.linkUrl || item.targetUrl || item.url || item.path

  if (customTarget && String(customTarget).startsWith('/')) {
    return customTarget
  }

  const role = Number(userStore.userInfo?.role)
  const type = normalizeType(item.type)

  if (role === 2 && type === 'delivery') {
    return '/company/deliveries'
  }

  if (role === 1 && ['delivery', 'interview'].includes(type)) {
    return '/deliveries'
  }

  if (role === 2 && type === 'audit') {
    return '/company/jobs'
  }

  if (role === 3 && type === 'audit') {
    return '/admin/companies'
  }

  return ''
}

async function fetchList() {
  loading.value = true

  try {
    const params = {
      page: query.value.page,
      pageSize: query.value.pageSize
    }

    if (query.value.type !== '') {
      params.type = query.value.type
    }

    if (query.value.isRead !== '') {
      params.isRead = query.value.isRead
    }

    const normalized = normalizeList(await getNotifications(params))
    notifications.value = sortByCreateTime(normalized.records)
    total.value = normalized.total
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  query.value.page = 1
  fetchList()
}

function handlePageChange(page) {
  query.value.page = page
  fetchList()
}

function handleSizeChange(pageSize) {
  query.value.pageSize = pageSize
  query.value.page = 1
  fetchList()
}

async function markReadIfNeeded(item) {
  if (!isUnread(item)) {
    return
  }

  const id = getNotificationId(item)
  if (!id) {
    return
  }

  await markNotificationRead(id)
  item.isRead = 1
  item.read = 1
  notificationStore.decreaseUnreadCount()
}

async function handleNotificationClick(item) {
  try {
    await markReadIfNeeded(item)
  } catch (error) {
    ElMessage.error('标记已读失败，请稍后再试')
    return
  }

  const target = getNotificationTarget(item)

  if (target) {
    router.push(target)
  }
}

async function handleReadAll() {
  if (readAllLoading.value) {
    return
  }

  readAllLoading.value = true

  try {
    await markAllNotificationsRead()
    notifications.value = notifications.value.map((item) => ({
      ...item,
      isRead: 1,
      read: 1
    }))
    notificationStore.clearUnreadCount()
    ElMessage.success('已全部标记为已读')
  } finally {
    readAllLoading.value = false
  }
}

onMounted(() => {
  fetchList()
  notificationStore.fetchUnreadCount()
})
</script>

<style scoped>
.notification-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 24px 64px;
}

.notification-page.wide {
  max-width: none;
  width: 100%;
  margin: 0;
  padding: 0;
}

.notification-card {
  padding: 28px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.06);
}

.notification-page.wide .notification-card {
  min-height: calc(100vh - 124px);
  border-radius: 0;
  box-shadow: none;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.page-head h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.page-head p {
  margin: 8px 0 0;
  color: #6b7280;
}

.filters {
  display: flex;
  gap: 14px;
  margin: 24px 0;
}

.filters .el-select {
  width: 180px;
}

.notification-list {
  display: grid;
  gap: 12px;
}

.notification-item {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  padding: 18px 20px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.notification-item.unread {
  border-color: #b8efee;
  background: #f0fdfd;
}

.notification-item.unread::before {
  position: absolute;
  top: 18px;
  bottom: 18px;
  left: 0;
  width: 4px;
  border-radius: 0 4px 4px 0;
  background: #00bebd;
  content: '';
}

.item-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-title strong {
  overflow: hidden;
  color: #111827;
  font-size: 17px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-item.unread .item-title strong {
  font-weight: 800;
}

.item-main p {
  margin: 10px 0 0;
  color: #4b5563;
  line-height: 1.7;
}

time {
  color: #9ca3af;
  font-size: 13px;
  white-space: nowrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
