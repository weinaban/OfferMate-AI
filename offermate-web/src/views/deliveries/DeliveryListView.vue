<template>
  <div class="page">
    <AppHeader />

    <main class="content">
      <div class="page-header">
        <div>
          <h1>投递记录</h1>
          <p>集中查看你的岗位投递状态和后续进展。</p>
        </div>
        <el-button type="primary" size="large" @click="router.push('/jobs')">继续找工作</el-button>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <div v-else-if="deliveries.length" class="delivery-list">
        <DeliveryCard
          v-for="delivery in deliveries"
          :key="delivery.id"
          :delivery="delivery"
          :contact-loading="contactLoadingId === getDeliveryId(delivery)"
          :delete-loading="deleteLoadingId === getDeliveryId(delivery)"
          @contact="handleContact"
          @delete="handleDelete"
        />
      </div>

      <el-empty v-else description="暂无投递记录">
        <el-button type="primary" @click="router.push('/jobs')">去找工作</el-button>
      </el-empty>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { createOrGetChatSession } from '../../api/chat'
import { deleteDelivery, getMyDeliveries } from '../../api/delivery'
import AppHeader from '../../components/common/AppHeader.vue'
import DeliveryCard from '../../components/delivery/DeliveryCard.vue'

const router = useRouter()
const loading = ref(false)
const contactLoadingId = ref('')
const deleteLoadingId = ref('')
const deliveries = ref([])

function normalizeList(data) {
  return Array.isArray(data) ? data : data?.records || data?.list || data?.rows || []
}

function getDeliveryId(delivery) {
  return delivery.id || delivery.deliveryId
}

async function fetchDeliveries() {
  loading.value = true

  try {
    const data = await getMyDeliveries()
    deliveries.value = normalizeList(data)
  } finally {
    loading.value = false
  }
}

async function handleContact(delivery) {
  const deliveryId = getDeliveryId(delivery)

  if (contactLoadingId.value) {
    return
  }

  contactLoadingId.value = deliveryId

  try {
    const data = await createOrGetChatSession({
      jobId: delivery.jobId,
      recruiterId: delivery.recruiterId || delivery.hrId || delivery.companyUserId,
      targetUserId: delivery.recruiterId || delivery.hrId || delivery.companyUserId
    })
    const sessionId = data?.id || data?.sessionId

    if (!sessionId) {
      ElMessage.error('暂时无法进入沟通，请稍后再试')
      return
    }

    router.push({
      path: '/chats',
      query: { sessionId }
    })
  } finally {
    contactLoadingId.value = ''
  }
}

async function handleDelete(delivery) {
  const deliveryId = getDeliveryId(delivery)

  if (!deliveryId || deleteLoadingId.value) {
    return
  }

  await ElMessageBox.confirm('确定删除这条不合适的投递记录吗？删除后列表中将不再显示。', '删除投递记录', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })

  deleteLoadingId.value = deliveryId

  try {
    await deleteDelivery(deliveryId)
    deliveries.value = deliveries.value.filter((item) => getDeliveryId(item) !== deliveryId)
    ElMessage.success('投递记录已删除')
  } finally {
    deleteLoadingId.value = ''
  }
}

onMounted(fetchDeliveries)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 72px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding: 28px 32px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
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

.delivery-list {
  display: grid;
  gap: 16px;
}
</style>
