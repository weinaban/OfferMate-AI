<template>
  <el-table class="delivery-table" :data="deliveries" border>
    <el-table-column label="投递岗位" min-width="180">
      <template #default="{ row }">
        <strong>{{ row.jobTitle || row.title || '未命名岗位' }}</strong>
      </template>
    </el-table-column>

    <el-table-column label="求职者" min-width="120">
      <template #default="{ row }">{{ row.seekerName || row.realName || '-' }}</template>
    </el-table-column>

    <el-table-column label="简历" min-width="150">
      <template #default="{ row }">{{ row.resumeTitle || '未命名简历' }}</template>
    </el-table-column>

    <el-table-column label="联系方式" min-width="190">
      <template #default="{ row }">
        <div class="contact">
          <span>{{ row.phone || '-' }}</span>
          <span>{{ row.email || '-' }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="createTime" label="投递时间" min-width="160" />

    <el-table-column label="当前状态" min-width="120">
      <template #default="{ row }">
        <el-tag :type="statusType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
      </template>
    </el-table-column>

    <el-table-column label="操作" width="360" fixed="right">
      <template #default="{ row }">
        <div class="actions">
          <el-select
            :model-value="Number(row.status) || 1"
            size="small"
            :loading="loadingId === row.id"
            @change="(status) => $emit('status-change', row, status)"
          >
            <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button
            size="small"
            type="primary"
            plain
            :loading="contactLoadingId === row.id"
            @click="$emit('contact', row)"
          >
            联系求职者
          </el-button>
          <el-button
            size="small"
            type="success"
            plain
            :loading="inviteLoadingId === row.id"
            @click="$emit('invite', row)"
          >
            发送面试邀请
          </el-button>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  deliveries: {
    type: Array,
    default: () => []
  },
  loadingId: {
    type: [String, Number],
    default: ''
  },
  contactLoadingId: {
    type: [String, Number],
    default: ''
  },
  inviteLoadingId: {
    type: [String, Number],
    default: ''
  }
})

defineEmits(['status-change', 'contact', 'invite'])

const statuses = [
  { value: 1, label: '已投递' },
  { value: 2, label: '已查看' },
  { value: 3, label: '感兴趣' },
  { value: 4, label: '邀面试' },
  { value: 5, label: '不合适' },
  { value: 6, label: '已录用' }
]

function statusText(status) {
  return statuses.find((item) => item.value === Number(status))?.label || '已投递'
}

function statusType(status) {
  const statusNumber = Number(status)
  if ([3, 6].includes(statusNumber)) return 'success'
  if (statusNumber === 4) return 'warning'
  if (statusNumber === 5) return 'danger'
  if (statusNumber === 2) return 'primary'
  return 'info'
}
</script>

<style scoped>
.delivery-table {
  border-radius: 8px;
  overflow: hidden;
}

.contact {
  display: grid;
  gap: 4px;
  color: #4b5563;
  font-size: 13px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.actions :deep(.el-select) {
  width: 104px;
}
</style>
