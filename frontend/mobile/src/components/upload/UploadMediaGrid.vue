<template>
  <div class="upload-moment">
    <div class="upload-moment__head row items-center justify-between mb-2">
      <span class="text-sm font-bold">{{ title }}</span>
      <span class="text-xs muted">{{ countLabel }}</span>
    </div>
    <p v-if="files.length" class="upload-moment__tip text-xs muted-2 mb-2">长按拖动可调整顺序，第一格为封面</p>

    <div class="upload-moment-grid" role="list">
      <div
        v-for="(file, idx) in files"
        :key="file.id || idx"
        class="upload-moment-tile"
        :class="{
          'is-cover': idx === 0,
          'is-active': highlightIndex === idx,
          'is-dragging': dragFrom === idx,
          'is-drag-over': dragOver === idx && dragFrom !== idx
        }"
        role="listitem"
        draggable="true"
        @dragstart="onDragStart(idx, $event)"
        @dragover="onDragOver(idx, $event)"
        @drop="onDrop(idx, $event)"
        @dragend="onDragEnd"
        @touchstart.passive="onTouchStart(idx, $event)"
        @touchmove="onTouchMove($event)"
        @touchend="onTouchEnd"
        @touchcancel="onTouchEnd"
      >
        <button
          type="button"
          class="upload-moment-tile__body"
          :aria-label="file.type === 'video' ? '预览视频' : '预览图片'"
          @click="onTileClick(idx, $event)"
        >
          <img
            v-if="file.type !== 'video'"
            :src="displayUrl(file)"
            alt=""
            class="upload-moment-tile__media"
            loading="lazy"
            draggable="false"
          />
          <video
            v-else
            :src="displayUrl(file)"
            muted
            playsinline
            preload="metadata"
            class="upload-moment-tile__media"
          />
          <span v-if="idx === 0" class="upload-moment-tile__cover">封面</span>
          <span v-if="file.type === 'video'" class="upload-moment-tile__play" aria-hidden="true">
            <i data-lucide="play" class="icon"></i>
          </span>
          <span v-if="file.duration" class="upload-moment-tile__dur">{{ file.duration }}</span>
          <span v-if="file.status === 'uploading'" class="upload-moment-tile__mask">
            <span class="upload-moment-tile__spinner" aria-hidden="true"></span>
            <span class="upload-moment-tile__mask-text">上传中</span>
          </span>
          <span v-else-if="file.status === 'error'" class="upload-moment-tile__mask upload-moment-tile__mask--error">
            上传失败
          </span>
        </button>
        <button
          type="button"
          class="upload-moment-tile__remove"
          aria-label="删除"
          :disabled="disabled"
          @click.stop="$emit('remove', idx)"
        >
          ×
        </button>
      </div>

      <button
        v-if="canAdd"
        type="button"
        class="upload-moment-tile upload-moment-tile--add"
        :disabled="disabled || uploading"
        aria-label="添加照片或视频"
        @click="$emit('add')"
      >
        <span class="upload-moment-tile__plus">+</span>
        <span class="upload-moment-tile__add-hint">{{ addHint }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { resolveDisplayUrl, resolveFileUrl } from '@/utils/fileUrl'
import { useLucideIcons } from '@/composables/useLucideIcons'
import {
  MAX_UPLOAD_IMAGES,
  MAX_UPLOAD_VIDEOS,
  countUploadFilesByType
} from '@/utils/uploadLimits'

const props = defineProps({
  files: { type: Array, default: () => [] },
  title: { type: String, default: '实验成果' },
  disabled: { type: Boolean, default: false },
  uploading: { type: Boolean, default: false },
  highlightIndex: { type: Number, default: -1 }
})

const emit = defineEmits(['add', 'remove', 'preview', 'reorder'])

const dragFrom = ref(-1)
const dragOver = ref(-1)

const touchDragFrom = ref(-1)
const touchDragOver = ref(-1)
let touchTimer = null
let touchDragging = false

const counts = computed(() => countUploadFilesByType(props.files))
const canAdd = computed(() => {
  const { image, video } = counts.value
  return image < MAX_UPLOAD_IMAGES || video < MAX_UPLOAD_VIDEOS
})

const countLabel = computed(() => {
  const { image, video } = counts.value
  return `${props.files.length} 个 · ${image}/${MAX_UPLOAD_IMAGES} 图 · ${video}/${MAX_UPLOAD_VIDEOS} 视频`
})

const addHint = computed(() => {
  const { image, video } = counts.value
  if (image >= MAX_UPLOAD_IMAGES && video < MAX_UPLOAD_VIDEOS) return '添加视频'
  if (video >= MAX_UPLOAD_VIDEOS && image < MAX_UPLOAD_IMAGES) return '添加照片'
  return '照片/视频'
})

function displayUrl(file) {
  if (file?.previewUrl?.startsWith('blob:')) return file.previewUrl
  return resolveDisplayUrl(file?.previewUrl, file?.url)
}

function onTileClick(idx, event) {
  if (touchDragging) {
    event.preventDefault()
    return
  }
  emit('preview', props.files[idx], idx)
}

function onDragStart(idx, event) {
  if (props.disabled) {
    event.preventDefault()
    return
  }
  dragFrom.value = idx
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', String(idx))
}

function onDragOver(idx, event) {
  event.preventDefault()
  dragOver.value = idx
}

function onDrop(idx, event) {
  event.preventDefault()
  const from = dragFrom.value
  if (from >= 0 && from !== idx) emit('reorder', { from, to: idx })
  onDragEnd()
}

function onDragEnd() {
  dragFrom.value = -1
  dragOver.value = -1
}

function indexFromTouch(event) {
  const t = event.changedTouches?.[0] || event.touches?.[0]
  if (!t) return -1
  const el = document.elementFromPoint(t.clientX, t.clientY)
  const tile = el?.closest?.('.upload-moment-tile:not(.upload-moment-tile--add)')
  if (!tile) return -1
  const grid = tile.parentElement
  const tiles = Array.from(grid.querySelectorAll('.upload-moment-tile:not(.upload-moment-tile--add)'))
  return tiles.indexOf(tile)
}

function onTouchStart(idx, event) {
  if (props.disabled) return
  touchTimer = setTimeout(() => {
    touchDragging = true
    touchDragFrom.value = idx
    if (navigator.vibrate) navigator.vibrate(20)
  }, 420)
}

function onTouchMove(event) {
  if (!touchDragging) return
  event.preventDefault()
  touchDragOver.value = indexFromTouch(event)
}

function onTouchEnd() {
  clearTimeout(touchTimer)
  if (touchDragging && touchDragFrom.value >= 0 && touchDragOver.value >= 0) {
    const { from, to } = { from: touchDragFrom.value, to: touchDragOver.value }
    if (from !== to) emit('reorder', { from, to })
  }
  setTimeout(() => {
    touchDragging = false
  }, 0)
  touchDragFrom.value = -1
  touchDragOver.value = -1
}

const { initIcons } = useLucideIcons()

watch(() => props.files.length, initIcons)
onMounted(initIcons)
</script>
