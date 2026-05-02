<template>
  <article class="resume-card">
    <div class="card-main">
      <div class="title-row">
        <h3>{{ resume.title || '未命名简历' }}</h3>
        <el-tag v-if="Number(resume.isDefault) === 1" type="success" effect="plain">默认简历</el-tag>
      </div>

      <div class="info-grid">
        <span>姓名：{{ resume.realName || '-' }}</span>
        <span>电话：{{ resume.phone || '-' }}</span>
        <span>邮箱：{{ resume.email || '-' }}</span>
        <span>学历：{{ resume.education || '-' }}</span>
        <span>工作年限：{{ experienceText }}</span>
        <span v-if="resume.updateTime || resume.createTime">更新时间：{{ resume.updateTime || resume.createTime }}</span>
      </div>
    </div>

    <div class="actions">
      <el-button text @click="$emit('preview', resume)">预览</el-button>
      <el-button text @click="$emit('edit', resume)">编辑</el-button>
      <el-button text :disabled="Number(resume.isDefault) === 1" @click="$emit('set-default', resume)">
        设置默认
      </el-button>
      <el-button text type="danger" @click="$emit('delete', resume)">删除</el-button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  resume: {
    type: Object,
    required: true
  }
})

defineEmits(['preview', 'edit', 'set-default', 'delete'])

const experienceText = computed(() => {
  const year = props.resume.experienceYear
  if (year === 0 || year) {
    return `${year}年`
  }
  return '-'
})
</script>

<style scoped>
.resume-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  padding: 24px 28px;
  border: 1px solid #eef0f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.resume-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 38px rgba(15, 98, 103, 0.09);
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

h3 {
  margin: 0;
  color: #111827;
  font-size: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 20px;
  margin-top: 18px;
  color: #4b5563;
  font-size: 14px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
