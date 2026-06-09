<template>
  <div class="file-uploader">
    <input type="hidden" :value="modelValue" />
    <!--
    <div v-if="isImagePreview" class="file-preview-wrap">
      <el-image :src="resolvedPreviewUrl" fit="cover" class="file-preview-image" :preview-src-list="[resolvedPreviewUrl]" preview-teleported />
    </div>
    -->
    <el-upload
      drag
      :show-file-list="false"
      :http-request="handleUpload"
      :before-upload="beforeUpload"
      :accept="accept"
      class="file-upload-dropzone"
    >
      <div class="file-upload-icon">⬆</div>
      <div class="file-upload-text">
        <strong>拖拽文件到这里</strong>，或点击选择文件
      </div>
      <div class="file-upload-tip">支持 {{ accept }}，单个文件不超过500MB</div>
      <el-button class="file-upload-button" :loading="uploading">{{ buttonText }}</el-button>
      <el-progress v-if="uploading" :percentage="progress" :stroke-width="8" class="file-upload-progress" />
    </el-upload>
    <el-button v-if="modelValue" link type="danger" @click="handleClear">清除</el-button>
    <el-button v-if="modelValue" link type="danger" @click="handleClear">删除附件</el-button>
    <!--<span class="file-upload-hint" v-if="fileName">{{ fileName }}</span>-->
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadMinioFile } from '../api/index'

const props = defineProps({
  modelValue: { type: String, default: '' },
  fileName: { type: String, default: '' },
  accept: { type: String, default: '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar' },
  buttonText: { type: String, default: '上传文件' }
})

const emit = defineEmits(['update:modelValue', 'update:fileName', 'delete-file'])
const uploading = ref(false)
const progress = ref(0)

const isImagePreview = computed(() => /\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/i.test(props.modelValue || props.fileName || ''))
const resolvedPreviewUrl = computed(() => resolvePreviewUrl(props.modelValue))

const beforeUpload = (file) => {
  const maxSize = 500 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件不能超过500MB')
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
      const uploadedFileUrl = res.data.data?.fileUrl || res.data.data?.url || ''
      emit('update:modelValue', uploadedFileUrl)
      emit('update:fileName', uploadedFileName)
      emit('uploaded', {
        fileUrl: uploadedFileUrl,
        fileName: uploadedFileName,
        fileExt: uploadedFileName.includes('.') ? uploadedFileName.split('.').pop().toLowerCase() : ''
      })
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.data.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
    setTimeout(() => { progress.value = 0 }, 800)
  }
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('update:fileName', '')
}

const resolvePreviewUrl = (value) => {
  if (!value) return ''
  const url = String(value).trim()
  if (/^https?:\/\//i.test(url) || url.startsWith('/')) return url
  return `${window.location.origin}/${url}`
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

.file-preview-wrap {
  width: 88px;
  height: 88px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.file-preview-image {
  width: 100%;
  height: 100%;
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

.file-upload-button {
  width: 120px;
}

.file-upload-progress {
  margin-top: 8px;
  width: 100%;
}

.file-upload-hint {
  color: #909399;
  font-size: 12px;
}
</style>
