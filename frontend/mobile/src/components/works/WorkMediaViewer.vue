<template>
  <div class="work-media-viewer">
    <div class="work-media-viewer__stage card-media media-wide" :class="stageClass">
      <template v-if="activeFile">
        <img
          v-if="activeFile.type !== 'video'"
          :src="mediaUrl(activeFile)"
          :alt="activeFile.name || '作品图片'"
          class="work-media-viewer__media"
        />
        <video
          v-else
          :key="mediaUrl(activeFile)"
          :src="mediaUrl(activeFile)"
          controls
          playsinline
          preload="metadata"
          class="work-media-viewer__media"
        />
        <span v-if="activeIndex === 0" class="upload-stage-preview__cover-tag">封面</span>
        <span v-if="activeFile.type === 'video' && activeFile.duration" class="work-media-viewer__duration">
          {{ activeFile.duration }}
        </span>
      </template>
      <div v-else class="work-media-viewer__empty">
        <i data-lucide="image" class="icon-xl"></i>
        <span class="text-xs muted mt-2">暂无附件</span>
      </div>
    </div>

    <div v-if="normalizedFiles.length > 1" class="work-media-viewer__thumbs" role="tablist" aria-label="作品附件">
      <button
        v-for="(file, index) in normalizedFiles"
        :key="file.id || `${file.url}-${index}`"
        type="button"
        class="work-media-viewer__thumb"
        :class="{ 'work-media-viewer__thumb--active': index === activeIndex }"
        :aria-selected="index === activeIndex"
        @click="activeIndex = index"
      >
        <img
          v-if="file.type !== 'video'"
          :src="mediaUrl(file)"
          alt=""
          class="work-media-viewer__thumb-media"
        />
        <span v-else class="work-media-viewer__thumb-video">
          <i data-lucide="play" class="icon"></i>
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { resolveWorkFileUrl } from '@/utils/fileUrl'
import { useLucideIcons } from '@/composables/useLucideIcons'

const props = defineProps({
  files: {
    type: Array,
    default: () => []
  },
  tint: {
    type: String,
    default: 'card-media-grad-amber-rose'
  }
})

const activeIndex = ref(0)

const normalizedFiles = computed(() =>
  (props.files || []).filter((file) => file?.url)
)

const activeFile = computed(() => normalizedFiles.value[activeIndex.value] || null)

const stageClass = computed(() => {
  if (normalizedFiles.value.length) return ''
  return props.tint || 'card-media-grad-amber-rose'
})

function mediaUrl(fileOrUrl) {
  if (fileOrUrl && typeof fileOrUrl === 'object') {
    return resolveWorkFileUrl(fileOrUrl)
  }
  return resolveWorkFileUrl({ url: fileOrUrl })
}

watch(
  () => props.files,
  () => {
    if (activeIndex.value >= normalizedFiles.value.length) {
      activeIndex.value = 0
    }
    initIcons()
  },
  { deep: true }
)

const { initIcons } = useLucideIcons()

onMounted(initIcons)
</script>
