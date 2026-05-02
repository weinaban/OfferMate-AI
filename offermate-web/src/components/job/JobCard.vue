<template>
  <article class="job-card" @click="goDetail">
    <div class="job-main">
      <div class="job-title-row">
        <h3>{{ job.title || '未命名岗位' }}</h3>
        <span class="salary">{{ salaryText }}</span>
      </div>

      <div class="meta-row">
        <span>{{ job.city || '城市不限' }}</span>
        <span>{{ job.experience || '经验不限' }}</span>
        <span>{{ job.education || '学历不限' }}</span>
      </div>

      <div v-if="tagList.length" class="tag-row">
        <el-tag v-for="tag in tagList" :key="tag" effect="plain">{{ tag }}</el-tag>
      </div>
    </div>

    <div class="company-block">
      <LogoAvatar :src="companyLogo" :name="companyName" size="md" />
      <div class="company-text">
        <strong>{{ companyName }}</strong>
        <p>{{ companyInfo }}</p>
      </div>
      <span v-if="job.createTime || job.publishTime" class="publish-time">
        {{ job.createTime || job.publishTime }}
      </span>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import LogoAvatar from '../common/LogoAvatar.vue'

const props = defineProps({
  job: {
    type: Object,
    required: true
  }
})

const router = useRouter()

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

  if (min && max) {
    return `${min}-${max}K`
  }

  if (min) {
    return `${min}K以上`
  }

  if (max) {
    return `${max}K以下`
  }

  return '薪资面议'
})

const companyName = computed(() => props.job.companyName || props.job.company?.companyName || '优质企业')
const companyLogo = computed(() => props.job.logo || props.job.companyLogo || props.job.logoUrl || props.job.company?.logo || props.job.company?.logoUrl || '')

const companyInfo = computed(() => {
  return [
    props.job.companyIndustry || props.job.industry || props.job.company?.industry,
    props.job.companySize || props.job.scale || props.job.companyScale || props.job.company?.scale
  ]
    .filter(Boolean)
    .join(' · ') || '行业信息完善中'
})

function goDetail() {
  const id = props.job.id || props.job.jobId

  if (id) {
    router.push(`/jobs/${id}`)
  }
}
</script>

<style scoped>
.job-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 28px;
  padding: 24px 28px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.job-card:hover {
  transform: translateY(-3px);
  border-color: rgba(0, 190, 189, 0.32);
  box-shadow: 0 18px 42px rgba(15, 98, 103, 0.11);
}

.job-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

h3 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  line-height: 1.35;
}

.salary {
  flex: 0 0 auto;
  color: #ff6b35;
  font-size: 20px;
  font-weight: 800;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
  color: #4b5563;
  font-size: 14px;
}

.meta-row span {
  padding: 5px 10px;
  border-radius: 4px;
  background: #f5f7fa;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.company-block {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr);
  align-content: start;
  gap: 14px;
  padding-left: 28px;
  border-left: 1px solid #eef0f2;
}

.company-block strong {
  display: block;
  color: #1f2937;
  font-size: 16px;
  line-height: 1.5;
}

.company-block p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
}

.publish-time {
  grid-column: 1 / -1;
  display: inline-block;
  margin-top: 16px;
  color: #9ca3af;
  font-size: 13px;
}
</style>
