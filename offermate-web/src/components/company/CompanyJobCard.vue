<template>
  <article class="job-card">
    <div class="main">
      <div class="title-row">
        <h3>{{ job.title || '未命名岗位' }}</h3>
        <span class="salary">{{ salaryText }}</span>
        <el-tag :type="statusType" effect="plain">{{ statusText }}</el-tag>
      </div>

      <div class="meta-row">
        <span>{{ job.city || '城市不限' }}</span>
        <span>{{ job.experience || '经验不限' }}</span>
        <span>{{ job.education || '学历不限' }}</span>
        <span v-if="job.viewCount !== undefined">浏览 {{ job.viewCount }}</span>
        <span v-if="job.createTime">发布时间 {{ job.createTime }}</span>
      </div>

      <div v-if="tagList.length" class="tag-row">
        <el-tag v-for="tag in tagList" :key="tag" effect="plain">{{ tag }}</el-tag>
      </div>
    </div>

    <div class="actions">
      <el-button text @click="$emit('edit', job)">编辑</el-button>
      <el-button text type="warning" :disabled="Number(job.status) === 0" @click="$emit('offline', job)">下架</el-button>
      <el-button text type="danger" @click="$emit('delete', job)">删除</el-button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  job: {
    type: Object,
    required: true
  }
})

defineEmits(['edit', 'offline', 'delete'])

const tagList = computed(() => {
  const tags = props.job.tags
  if (Array.isArray(tags)) {
    return tags.filter(Boolean)
  }
  if (typeof tags === 'string') {
    return tags.split(',').map((tag) => tag.trim()).filter(Boolean)
  }
  return []
})

const salaryText = computed(() => {
  const min = props.job.salaryMin
  const max = props.job.salaryMax
  return min && max ? `${min}-${max}K` : '薪资面议'
})

const statusText = computed(() => {
  const status = Number(props.job.status)
  if (status === 1) return '招聘中'
  if (status === 0) return '已下架'
  return status ? '待审核' : '未知状态'
})

const statusType = computed(() => {
  const status = Number(props.job.status)
  if (status === 1) return 'success'
  if (status === 0) return 'info'
  return 'warning'
})
</script>

<style scoped>
.job-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  padding: 24px 28px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.title-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

h3 {
  margin: 0;
  color: #111827;
  font-size: 20px;
}

.salary {
  color: #ff6b35;
  font-size: 19px;
  font-weight: 800;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 15px;
}

.meta-row span {
  padding: 6px 10px;
  border-radius: 4px;
  color: #4b5563;
  background: #f5f7fa;
  font-size: 14px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
