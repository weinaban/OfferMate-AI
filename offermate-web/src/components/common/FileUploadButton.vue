<template>
  <el-upload
    :show-file-list="false"
    :http-request="customUpload"
    :before-upload="beforeUpload"
  >
    <el-button :type="type" :plain="plain" :size="size" :loading="loading">
      {{ loading ? loadingText : text }}
    </el-button>
  </el-upload>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '../../api/file'

const props = defineProps({
  bizType: {
    type: String,
    required: true
  },
  acceptType: {
    type: String,
    default: 'image'
  },
  text: {
    type: String,
    default: '上传文件'
  },
  loadingText: {
    type: String,
    default: '上传中'
  },
  type: {
    type: String,
    default: 'primary'
  },
  plain: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['success'])
const loading = ref(false)

const imageExts = ['jpg', 'jpeg', 'png', 'webp']
const docExts = ['pdf', 'doc', 'docx']

function getExt(file) {
  return file.name.split('.').pop()?.toLowerCase() || ''
}

function beforeUpload(file) {
  const ext = getExt(file)

  if (props.acceptType === 'image') {
    const typeOk = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || imageExts.includes(ext)
    if (!typeOk) {
      ElMessage.error('请上传 jpg、jpeg、png 或 webp 图片')
      return false
    }

    if (file.size / 1024 / 1024 > 5) {
      ElMessage.error('图片大小不能超过 5MB')
      return false
    }
  } else {
    const typeOk = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ].includes(file.type) || docExts.includes(ext)

    if (!typeOk) {
      ElMessage.error('请上传 pdf、doc 或 docx 文件')
      return false
    }

    if (file.size / 1024 / 1024 > 20) {
      ElMessage.error('附件简历大小不能超过 20MB')
      return false
    }
  }

  return true
}

async function customUpload(options) {
  if (loading.value) {
    return
  }

  loading.value = true

  try {
    const url = await uploadFile(options.file, props.bizType)

    if (!url) {
      ElMessage.error('上传失败，未获取到文件地址')
      options.onError?.(new Error('empty url'))
      return
    }

    ElMessage.success('上传成功')
    emit('success', url, options.file)
    options.onSuccess?.({ url })
  } catch (error) {
    ElMessage.error('上传失败，请稍后重试')
    options.onError?.(error)
  } finally {
    loading.value = false
  }
}
</script>
