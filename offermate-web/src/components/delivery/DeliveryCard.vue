<template>
  <article
    class="delivery-card"
    :class="{ clickable: isInterview }"
    @click="handleCardClick"
  >
    <div class="main">
      <button class="job-title" type="button" :disabled="!delivery.jobId" @click.stop="goJob">
        {{ delivery.jobTitle || delivery.title || '未命名岗位' }}
      </button>
      <p class="company">{{ delivery.companyName || '优质企业' }}</p>
      <div class="meta">
        <span>{{ delivery.city || '城市不限' }}</span>
        <span>{{ salaryText }}</span>
        <span>简历：{{ delivery.resumeTitle || '未命名简历' }}</span>
        <span v-if="delivery.createTime">投递时间：{{ delivery.createTime }}</span>
      </div>
      <p v-if="isInterview" class="hint">企业已发起面试邀请，点击卡片进入沟通</p>
    </div>

    <div class="side">
      <el-tag :type="statusType" effect="plain">{{ statusText }}</el-tag>
      <el-button
        v-if="isInterview"
        type="primary"
        plain
        :loading="contactLoading"
        @click.stop="$emit('contact', delivery)"
      >
        去沟通
      </el-button>
      <el-button
        v-if="isRejected"
        type="danger"
        plain
        :loading="deleteLoading"
        @click.stop="$emit('delete', delivery)"
      >
        删除
      </el-button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  delivery: {
    type: Object,
    required: true
  },
  contactLoading: {
    type: Boolean,
    default: false
  },
  deleteLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['contact', 'delete'])
const router = useRouter()

const statusMap = {
  1: '已投递',
  2: '已查看',
  3: '感兴趣',
  4: '邀面试',
  5: '不合适',
  6: '已录用'
}

const statusTypeMap = {
  1: 'info',
  2: 'primary',
  3: 'success',
  4: 'warning',
  5: 'danger',
  6: 'success'
}

const statusNumber = computed(() => Number(props.delivery.status || 1))
const isInterview = computed(() => statusNumber.value === 4)
const isRejected = computed(() => statusNumber.value === 5)
const statusText = computed(() => statusMap[statusNumber.value] || '已投递')
const statusType = computed(() => statusTypeMap[statusNumber.value] || 'info')

const salaryText = computed(() => {
  const min = props.delivery.salaryMin
  const max = props.delivery.salaryMax

  if (min && max) {
    return `${min}-${max}K`
  }

  return '薪资面议'
})

function handleCardClick() {
  if (isInterview.value) {
    emit('contact', props.delivery)
  }
}

function goJob() {
  if (props.delivery.jobId) {
    router.push(`/jobs/${props.delivery.jobId}`)
  }
}
</script>

<style scoped>
.delivery-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
  transition: all 0.2s ease;
}

.delivery-card.clickable {
  cursor: pointer;
}

.delivery-card.clickable:hover {
  transform: translateY(-2px);
  border-color: #a7eeed;
  box-shadow: 0 14px 36px rgba(0, 190, 189, 0.12);
}

.job-title {
  padding: 0;
  border: none;
  color: #111827;
  background: transparent;
  font-size: 20px;
  font-weight: 800;
  cursor: pointer;
}

.job-title:disabled {
  cursor: default;
}

.job-title:not(:disabled):hover {
  color: #00a7a6;
}

.company {
  margin: 10px 0 0;
  color: #374151;
  font-weight: 700;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.meta span {
  padding: 6px 10px;
  border-radius: 4px;
  color: #4b5563;
  background: #f5f7fa;
  font-size: 14px;
}

.hint {
  margin: 14px 0 0;
  color: #00a7a6;
  font-size: 14px;
  font-weight: 700;
}

.side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex: 0 0 auto;
}
</style>
