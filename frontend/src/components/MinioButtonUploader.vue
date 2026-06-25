<template>

  <div class="minio-btn-uploader">

    <div class="minio-btn-uploader__actions">

      <el-upload
        action="#"
        :show-file-list="false"
        :http-request="handleUpload"
        :before-upload="beforeUpload"
        :accept="accept"
        class="minio-btn-uploader__upload"
      >

        <el-button :type="buttonType" :loading="uploading">{{ buttonText }}</el-button>

      </el-upload>

      <el-button v-if="showFileActions && modelValue" link type="danger" @click="handleClear">清除</el-button>

      <el-button v-if="showFileActions && modelValue" link type="danger" @click="handleDelete">删除附件</el-button>

    </div>

    <el-progress

      v-if="uploading && showProgress"

      :percentage="progress"

      :stroke-width="6"

      class="minio-btn-uploader__progress"

    />

  </div>

</template>



<script setup>

import { ref } from 'vue'

import { ElMessage } from 'element-plus'

import { deleteMinioFileByUrl, uploadMinioFile } from '../api/system'

import { formatUploadError } from '../utils/uploadError'



const props = defineProps({

  modelValue: { type: String, default: '' },

  fileName: { type: String, default: '' },

  previewUrl: { type: String, default: '' },

  accept: { type: String, default: '.png,.jpg,.jpeg,.gif,.webp,.bmp,.svg,.mp4,.mov,.avi,.mkv,.webm,.mp3,.wav,.flac,.aac,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx' },

  buttonText: { type: String, default: '上传文件' },

  buttonType: { type: String, default: 'primary' },

  maxSize: { type: Number, default: 1024 * 1024 * 1024 },

  showFileActions: { type: Boolean, default: true },

  showProgress: { type: Boolean, default: true }

})



const emit = defineEmits(['update:modelValue', 'update:fileName', 'delete-file', 'uploaded'])

const uploading = ref(false)

const progress = ref(0)

const previewUrl = ref('')



const formatMaxSize = (size) => {

  if (size >= 1024 * 1024 * 1024) return `${(size / 1024 / 1024 / 1024).toFixed(0)}GB`

  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(0)}MB`

  if (size >= 1024) return `${(size / 1024).toFixed(0)}KB`

  return `${size}B`

}



const matchesAccept = (file, accept) => {

  const rules = String(accept || '')

    .split(',')

    .map((item) => item.trim().toLowerCase())

    .filter(Boolean)

  if (!rules.length) return true

  const name = (file.name || '').toLowerCase()

  const mime = (file.type || '').toLowerCase()

  return rules.some((rule) => {

    if (rule.startsWith('.')) return name.endsWith(rule)

    if (rule.includes('/')) return mime === rule || (rule.endsWith('/*') && mime.startsWith(rule.slice(0, -1)))

    return false

  })

}



const beforeUpload = (file) => {

  if (!matchesAccept(file, props.accept)) {

    ElMessage.warning(`仅支持上传：${props.accept}`)

    return false

  }

  if (file.size > props.maxSize) {

    ElMessage.warning(`文件不能超过${formatMaxSize(props.maxSize)}`)

    return false

  }

  progress.value = 0

  return true

}



const handleUpload = async (options) => {

  uploading.value = true

  progress.value = 0

  try {

    const res = await uploadMinioFile(options.file, (event) => {

      if (event.total) {

        progress.value = Math.min(99, Math.round((event.loaded / event.total) * 100))

      }

    })

    if (res.data.code === 200) {

      progress.value = 100

      const uploadedFileName = res.data.data?.fileName || options.file.name || ''

      const fileUrl = res.data.data?.fileUrl || ''

      const nextPreviewUrl = res.data.data?.previewUrl || res.data.data?.url || fileUrl

      previewUrl.value = nextPreviewUrl

      emit('update:modelValue', fileUrl)

      emit('update:fileName', uploadedFileName)

      emit('uploaded', {

        fileUrl,

        previewUrl: nextPreviewUrl,

        fileName: uploadedFileName,

        objectName: res.data.data?.objectName || ''

      })

      ElMessage.success('文件上传成功')

    } else {

      ElMessage.error(res.data.message || '上传失败')

    }

  } catch (error) {

    ElMessage.error(formatUploadError(error))

  } finally {

    uploading.value = false

    setTimeout(() => {

      progress.value = 0

    }, 800)

  }

}



const handleClear = () => {

  previewUrl.value = ''

  emit('update:modelValue', '')

  emit('update:fileName', '')

}



const handleDelete = async () => {

  if (!props.modelValue) {

    handleClear()

    return

  }

  try {

    const res = await deleteMinioFileByUrl(props.modelValue)

    if (res.data.code === 200) {

      handleClear()

      emit('delete-file', props.modelValue)

      ElMessage.success('删除成功')

    } else {

      ElMessage.error(res.data.message || '删除失败')

    }

  } catch (error) {

    ElMessage.error(error?.response?.data?.message || '删除失败')

  }

}

</script>



<style scoped>

.minio-btn-uploader {

  display: flex;

  flex-direction: column;

  gap: 8px;

  flex-shrink: 0;

  max-width: 100%;

}



.minio-btn-uploader__actions {

  display: inline-flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 8px;

}



.minio-btn-uploader__upload {

  display: inline-block;

  line-height: normal;

}



.minio-btn-uploader__upload :deep(.el-upload) {

  display: inline-block;

}



.minio-btn-uploader__progress {

  width: 100%;

  min-width: 160px;

  max-width: 320px;

}

</style>


