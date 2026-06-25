<template>
  <div v-if="displayUrl" class="media-preview">
    <video
      v-if="isVideo"
      class="media-preview__video"
      :src="displayUrl"
      controls
      playsinline
      preload="metadata"
    />
    <audio
      v-else-if="isAudio"
      class="media-preview__audio"
      :src="displayUrl"
      controls
      preload="metadata"
    />
    <el-image
      v-else-if="isImage"
      :src="displayUrl"
      fit="contain"
      class="media-preview__image"
    />
    <div v-else class="media-preview__fallback">
      <slot name="fallback">
        <a :href="displayUrl" target="_blank" rel="noopener noreferrer">在新窗口打开</a>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isAudioUrl, isImageUrl, isVideoUrl, resolveDisplayUrl } from '../utils/fileUrl'

const props = defineProps({
  previewUrl: { type: String, default: '' },
  fileUrl: { type: String, default: '' },
  fileName: { type: String, default: '' }
})

const displayUrl = computed(() => resolveDisplayUrl(props.previewUrl, props.fileUrl))
const label = computed(() => props.fileName || props.previewUrl || props.fileUrl || '')
const isImage = computed(() => isImageUrl(displayUrl.value, label.value))
const isVideo = computed(() => isVideoUrl(displayUrl.value, label.value))
const isAudio = computed(() => isAudioUrl(displayUrl.value, label.value))
</script>

<style scoped>
.media-preview {
  width: 100%;
}

.media-preview__video {
  display: block;
  width: 100%;
  max-width: 480px;
  height: 240px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  background: #000;
  object-fit: contain;
}

.media-preview__audio {
  display: block;
  width: 100%;
  max-width: 480px;
}

.media-preview__image {
  width: 100%;
  max-width: 480px;
  height: 240px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  background: #fafafa;
}

.media-preview__image :deep(img),
.media-preview__image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.media-preview__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 140px;
  border-radius: 10px;
  border: 1px dashed #dcdfe6;
  background: #fafafa;
}
</style>
