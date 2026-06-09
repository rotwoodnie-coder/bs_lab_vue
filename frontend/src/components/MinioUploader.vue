<template>
  <div class="file-uploader">
    <input type="hidden" :value="modelValue" />
    <el-upload
      drag
      :show-file-list="false"
      :http-request="handleUpload"
      :before-upload="beforeUpload"
      :accept="accept"
      class="file-upload-dropzone"
    >
      <div v-if="isImagePreview" class="file-upload-preview">
        <img class="file-upload-preview-image" :src="previewSrc" alt="上传预览" />
      </div>
      <template v-else>
        <div class="file-upload-icon">⬆</div>
        <div class="file-upload-text">
          <strong>拖拽文件到这里</strong>，或点击选择文件
        </div>
        <div class="file-upload-tip">文件上传，支持 {{ accept }}，单个文件不超过{{ formatMaxSize(maxSize) }}</div>
      </template>
      <el-button class="file-upload-button" :loading="uploading">{{ buttonText }}</el-button>
      <el-progress v-if="uploading" :percentage="progress" :stroke-width="8" class="file-upload-progress" />
    </el-upload>
    <el-button v-if="modelValue" link type="danger" @click="handleClear">清除</el-button>
    <el-button v-if="modelValue" link type="danger" @click="handleDelete">删除附件</el-button>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { deleteMinioFileByUrl, uploadMinioFile } from '../api/system'

const props = defineProps({
  modelValue: { type: String, default: '' },
  fileName: { type: String, default: '' },
  previewUrl: { type: String, default: '' },
  accept: { type: String, default: '.png,.jpg,.jpeg,.gif,.webp,.bmp,.svg,.mp4,.mov,.avi,.mkv,.webm,.mp3,.wav,.flac,.aac,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx' },
  buttonText: { type: String, default: '上传文件' },
  maxSize: { type: Number, default: 1024 * 1024 * 1024 }
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

const isImageFile = (url = '') => /\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/i.test(url)
const isImagePreview = computed(() => Boolean(previewUrl.value || props.previewUrl) && isImageFile(previewUrl.value || props.previewUrl))
const previewSrc = computed(() => previewUrl.value || props.previewUrl || props.modelValue)

const beforeUpload = (file) => {
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
    ElMessage.error(error?.response?.data?.message || '上传失败')
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
.file-uploader {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  width: 260px;
  max-width: 100%;
}

.file-upload-dropzone {
  width: 100%;
}

.file-upload-icon {
  font-size: 26px;
  color: #409eff;
  margin-bottom: 6px;
}

.file-upload-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.file-upload-tip {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.file-upload-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 160px;
  margin-bottom: 8px;
  overflow: hidden;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafafa;
}

.file-upload-preview-image {
  display: block;
  max-width: 100%;
  max-height: 160px;
  object-fit: contain;
}

.file-upload-button {
  width: 120px;
}

.file-upload-progress {
  margin-top: 8px;
  width: 100%;
}
</style>
