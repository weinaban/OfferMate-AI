<template>
  <footer class="input-box">
    <el-input
      v-model="content"
      type="textarea"
      :rows="3"
      resize="none"
      placeholder="请输入消息，Enter 发送，Shift + Enter 换行"
      :disabled="disabled"
      @keydown="handleKeydown"
    />
    <div class="input-actions">
      <span>仅支持文本消息</span>
      <el-button type="primary" :loading="loading" :disabled="disabled || !content.trim()" @click="submit">
        发送
      </el-button>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send'])
const content = ref('')

function submit() {
  const text = content.value.trim()

  if (!text) {
    return
  }

  emit('send', text)
  content.value = ''
}

function handleKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    submit()
  }
}
</script>

<style scoped>
.input-box {
  padding: 16px 18px;
  border-top: 1px solid #eef0f2;
  background: #ffffff;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.input-actions span {
  color: #9ca3af;
  font-size: 13px;
}

.input-actions .el-button {
  min-width: 96px;
}
</style>
