<template>
  <CompanyLayout>
    <section class="page-header">
      <div>
        <h1>收到的投递</h1>
        <p>查看候选人投递，维护处理状态，并发送面试邀请。</p>
      </div>
    </section>

    <section class="table-card">
      <el-skeleton v-if="loading" :rows="8" animated />
      <CompanyDeliveryTable
        v-else-if="deliveries.length"
        :deliveries="deliveries"
        :loading-id="statusLoadingId"
        :contact-loading-id="chatLoadingId"
        :invite-loading-id="inviteLoadingId"
        @status-change="handleStatusChange"
        @contact="handleContact"
        @invite="openInviteDialog"
      />
      <el-empty v-else description="暂无投递记录" />
    </section>

    <el-dialog v-model="inviteDialogVisible" title="发送面试邀请" width="620px" @closed="resetInviteForm">
      <el-form ref="inviteFormRef" :model="inviteForm" :rules="inviteRules" label-width="96px">
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="inviteForm.interviewTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择面试时间"
            class="form-control"
          />
        </el-form-item>
        <el-form-item label="面试地点" prop="address">
          <el-input v-model="inviteForm.address" placeholder="请输入面试地点" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="inviteForm.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="inviteForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="inviteForm.remark"
            type="textarea"
            :rows="4"
            placeholder="例如：请携带纸质简历，提前 10 分钟到场"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="inviteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="inviteSubmitting" @click="submitInvite">发送邀请</el-button>
      </template>
    </el-dialog>
  </CompanyLayout>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createOrGetChatSession } from '../../api/chat'
import { getCompanyDeliveries, updateDeliveryStatus } from '../../api/companyDelivery'
import { createInterview } from '../../api/interview'
import CompanyDeliveryTable from '../../components/company/CompanyDeliveryTable.vue'
import CompanyLayout from '../../components/company/CompanyLayout.vue'
import { useNotificationStore } from '../../stores/notification'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const loading = ref(false)
const statusLoadingId = ref('')
const chatLoadingId = ref('')
const inviteLoadingId = ref('')
const inviteSubmitting = ref(false)
const inviteDialogVisible = ref(false)
const inviteFormRef = ref(null)
const currentDelivery = ref(null)
const deliveries = ref([])

const inviteForm = reactive({
  interviewTime: '',
  address: '',
  contactName: '',
  contactPhone: '',
  remark: ''
})

const inviteRules = {
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  address: [{ required: true, message: '请输入面试地点', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function normalizeList(data) {
  return Array.isArray(data) ? data : data?.records || data?.list || data?.rows || []
}

async function fetchDeliveries() {
  loading.value = true

  try {
    deliveries.value = normalizeList(await getCompanyDeliveries())
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(row, status) {
  if (Number(row.status) === Number(status)) {
    return
  }

  statusLoadingId.value = row.id

  try {
    await updateDeliveryStatus(row.id, status)
    row.status = status
    ElMessage.success('投递状态已更新')
    notificationStore.fetchUnreadCount()
  } finally {
    statusLoadingId.value = ''
  }
}

async function handleContact(row) {
  if (chatLoadingId.value) {
    return
  }

  chatLoadingId.value = row.id

  try {
    const data = await createOrGetChatSession({
      jobId: row.jobId,
      targetUserId: row.seekerId || row.userId
    })
    const sessionId = data?.id || data?.sessionId

    if (!sessionId) {
      ElMessage.error('会话创建失败，请稍后重试')
      return
    }

    router.push({
      path: '/chats',
      query: { sessionId }
    })
  } finally {
    chatLoadingId.value = ''
  }
}

function openInviteDialog(row) {
  currentDelivery.value = row
  inviteLoadingId.value = row.id
  inviteDialogVisible.value = true
  nextTick(() => {
    inviteLoadingId.value = ''
  })
}

function resetInviteForm() {
  currentDelivery.value = null
  inviteForm.interviewTime = ''
  inviteForm.address = ''
  inviteForm.contactName = ''
  inviteForm.contactPhone = ''
  inviteForm.remark = ''
  inviteFormRef.value?.clearValidate()
}

async function submitInvite() {
  if (inviteSubmitting.value || !currentDelivery.value) {
    return
  }

  await inviteFormRef.value?.validate()
  inviteSubmitting.value = true

  try {
    const delivery = currentDelivery.value
    await createInterview({
      deliveryId: delivery.id || delivery.deliveryId,
      jobId: delivery.jobId,
      seekerId: delivery.seekerId || delivery.userId,
      recruiterId: userStore.userInfo?.userId,
      companyId: delivery.companyId,
      interviewTime: inviteForm.interviewTime,
      address: inviteForm.address,
      contactName: inviteForm.contactName,
      contactPhone: inviteForm.contactPhone,
      remark: inviteForm.remark
    })
    ElMessage.success('面试邀请发送成功')
    inviteDialogVisible.value = false
    notificationStore.fetchUnreadCount()
    fetchDeliveries()
  } finally {
    inviteSubmitting.value = false
  }
}

onMounted(fetchDeliveries)
</script>

<style scoped>
.page-header,
.table-card {
  padding: 28px 32px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.page-header {
  margin-bottom: 22px;
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

.form-control {
  width: 100%;
}
</style>
