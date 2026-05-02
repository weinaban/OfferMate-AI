<template>
  <div class="logo-avatar" :class="[typeClass, sizeClass]">
    <img v-if="imageVisible" :src="normalizedSrc" :alt="name || type" @error="handleError" />
    <el-icon v-else-if="type === 'user'"><UserFilled /></el-icon>
    <el-icon v-else><OfficeBuilding /></el-icon>
  </div>
</template>

<script setup>
import { OfficeBuilding, UserFilled } from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'company'
  },
  size: {
    type: String,
    default: 'md'
  }
})

const imageFailed = ref(false)

const normalizedSrc = computed(() => String(props.src || '').trim())
const imageVisible = computed(() => Boolean(normalizedSrc.value) && !imageFailed.value)
const typeClass = computed(() => (props.type === 'user' ? 'is-user' : 'is-company'))
const sizeClass = computed(() => `is-${props.size}`)

watch(
  () => normalizedSrc.value,
  () => {
    imageFailed.value = false
  }
)

function handleError() {
  imageFailed.value = true
}
</script>

<style scoped>
.logo-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #e8ecef;
  border-radius: 8px;
  color: #00a7a6;
  background: linear-gradient(135deg, #e9fbfb 0%, #f7f9fb 100%);
  font-weight: 800;
  flex: 0 0 auto;
}

.logo-avatar.is-user {
  border-radius: 50%;
  color: #ffffff;
  background: #00bebd;
}

.logo-avatar.is-sm {
  width: 42px;
  height: 42px;
  font-size: 18px;
}

.logo-avatar.is-md {
  width: 56px;
  height: 56px;
  font-size: 22px;
}

.logo-avatar.is-lg {
  width: 84px;
  height: 84px;
  font-size: 34px;
}

.logo-avatar :deep(.el-icon) {
  font-size: 1.05em;
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
