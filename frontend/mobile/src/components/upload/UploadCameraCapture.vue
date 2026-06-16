<template>
  <Teleport to="body">
    <div v-if="visible" class="upload-camera" role="dialog" aria-label="拍摄">
      <video ref="videoRef" class="upload-camera__feed" playsinline muted autoplay />

      <div v-if="recording" class="upload-camera__rec-badge">
        <span class="upload-camera__rec-dot" />
        {{ recSeconds }}s
      </div>

      <div class="upload-camera__hint">
        {{ recording ? '松开结束录像' : '点击拍照 · 长按录像' }}
      </div>

      <div class="upload-camera__bar safe-bottom">
        <button type="button" class="upload-camera__side" @click="close">取消</button>

        <button
          type="button"
          class="upload-camera__shutter"
          :class="{ 'is-recording': recording }"
          aria-label="快门"
          @pointerdown.prevent="onShutterDown"
          @pointerup.prevent="onShutterUp"
          @pointerleave="onShutterLeave"
          @pointercancel="onShutterLeave"
        >
          <span class="upload-camera__shutter-inner" />
        </button>

        <span class="upload-camera__side" aria-hidden="true" />
      </div>

      <p v-if="errorText" class="upload-camera__error">{{ errorText }}</p>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const LONG_PRESS_MS = 280
const MAX_VIDEO_SEC = 60

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'capture'])

const videoRef = ref(null)
const errorText = ref('')
const recording = ref(false)
const recSeconds = ref(0)

let stream = null
let pressTimer = null
let longPressMode = false
let pointerActive = false
let mediaRecorder = null
let recordedChunks = []
let recTick = null

watch(() => props.visible, async (open) => {
  if (open) {
    errorText.value = ''
    await startCamera()
  } else {
    stopCamera()
  }
})

async function startCamera() {
  if (!navigator.mediaDevices?.getUserMedia) {
    errorText.value = '当前环境不支持相机，请从相册选择'
    return
  }
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: { ideal: 'environment' } },
      audio: true
    })
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      await videoRef.value.play().catch(() => {})
    }
  } catch {
    errorText.value = '无法打开相机，请检查权限或从相册选择'
  }
}

function stopCamera() {
  stopRecording(false)
  clearTimeout(pressTimer)
  longPressMode = false
  pointerActive = false
  if (stream) {
    stream.getTracks().forEach((t) => t.stop())
    stream = null
  }
  if (videoRef.value) videoRef.value.srcObject = null
}

function close() {
  emit('close')
}

function pickRecorderMime() {
  const candidates = [
    'video/webm;codecs=vp9,opus',
    'video/webm;codecs=vp8,opus',
    'video/webm',
    'video/mp4'
  ]
  return candidates.find((m) => MediaRecorder.isTypeSupported(m)) || ''
}

function onShutterDown() {
  if (errorText.value || !stream) return
  pointerActive = true
  longPressMode = false
  pressTimer = setTimeout(() => {
    if (!pointerActive) return
    longPressMode = true
    startRecording()
  }, LONG_PRESS_MS)
}

function onShutterUp() {
  if (!pointerActive) return
  pointerActive = false
  clearTimeout(pressTimer)
  if (longPressMode) {
    stopRecording(true)
    longPressMode = false
    return
  }
  takePhoto()
}

function onShutterLeave() {
  if (!pointerActive) return
  pointerActive = false
  clearTimeout(pressTimer)
  if (longPressMode) {
    stopRecording(true)
    longPressMode = false
  }
}

function takePhoto() {
  const video = videoRef.value
  if (!video?.videoWidth) return
  const canvas = document.createElement('canvas')
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  const ctx = canvas.getContext('2d')
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
  canvas.toBlob((blob) => {
    if (!blob) return
    const file = new File([blob], `photo-${Date.now()}.jpg`, { type: 'image/jpeg' })
    emit('capture', file)
    close()
  }, 'image/jpeg', 0.92)
}

function startRecording() {
  if (recording.value || !stream) return
  const mime = pickRecorderMime()
  if (!mime || !window.MediaRecorder) {
    errorText.value = '当前浏览器不支持录像，请从相册选择视频'
    return
  }
  try {
    recordedChunks = []
    mediaRecorder = new MediaRecorder(stream, { mimeType: mime })
    mediaRecorder.ondataavailable = (e) => {
      if (e.data?.size) recordedChunks.push(e.data)
    }
    mediaRecorder.onstop = () => {
      if (!recordedChunks.length) return
      const blob = new Blob(recordedChunks, { type: mediaRecorder.mimeType || mime })
      const ext = blob.type.includes('mp4') ? 'mp4' : 'webm'
      const file = new File([blob], `video-${Date.now()}.${ext}`, { type: blob.type })
      emit('capture', file)
      close()
    }
    mediaRecorder.start(200)
    recording.value = true
    recSeconds.value = 0
    recTick = setInterval(() => {
      recSeconds.value += 1
      if (recSeconds.value >= MAX_VIDEO_SEC) stopRecording(true)
    }, 1000)
  } catch {
    errorText.value = '录像启动失败，请从相册选择视频'
    recording.value = false
  }
}

function stopRecording(emitFile) {
  clearInterval(recTick)
  recTick = null
  if (!recording.value) return
  recording.value = false
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    if (!emitFile) {
      mediaRecorder.onstop = null
    }
    mediaRecorder.stop()
  }
  mediaRecorder = null
  if (!emitFile) recordedChunks = []
}

onBeforeUnmount(stopCamera)
</script>
