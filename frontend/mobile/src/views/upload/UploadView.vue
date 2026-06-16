<template>
  <div class="prototype-container pad-shell" data-layout="upload-split">
    <div class="topbar safe-top">
      <button type="button" class="icon-btn" aria-label="返回" @click="goBack">
        <i data-lucide="arrow-left" class="icon"></i>
      </button>
      <span class="topbar-title">成果上传</span>
    </div>

    <div v-if="isParentView && selectedChild" class="px-4 pt-3">
      <div class="card card-pad text-sm">
        正在为 <strong>{{ selectedChild.name }}</strong> 协助上传
      </div>
    </div>

    <input
      ref="galleryInputRef"
      type="file"
      accept="image/*,video/*"
      multiple
      style="display:none"
      @change="onGalleryPicked"
    />
    <input
      ref="cameraFallbackRef"
      type="file"
      accept="image/*"
      capture="environment"
      style="display:none"
      @change="onCameraFallbackPicked"
    />

    <UploadCameraCapture
      :visible="showCameraCapture"
      @close="showCameraCapture = false"
      @capture="onCameraCaptured"
    />

    <div v-if="isCreative" class="px-4 safe-top" style="padding-top:8px;">
      <p class="quiz-tip-banner">💡 <strong>创意实验</strong> · 自由上传实验成果，不关联老师作业，提交后由老师审核通过再展示在作品墙</p>
    </div>

    <div v-if="taskAlreadySubmitted && !submitted" class="px-4 pt-3">
      <div class="card card-pad tint-green text-sm">
        该任务已提交（{{ taskContext?.stateLabel || '已完成' }}），无需重复上传。
        <router-link
          v-if="relatedTaskId || route.query.taskId"
          :to="`/tasks/${relatedTaskId || route.query.taskId}`"
          class="text-success font-bold ml-1"
        >查看任务</router-link>
      </div>
    </div>

    <div v-else-if="draftRestored && !submitted" class="px-4 pt-3">
      <div class="card card-pad text-xs muted">已恢复上次未提交的草稿，可继续编辑后提交</div>
    </div>

    <!-- 提交成功 -->
    <div v-if="submitted" class="px-4 stack-3 pb-8 anim-fade-up flex-1 overflow-y-auto">
      <div class="card card-pad tint-green text-center">
        <div class="text-2xl mb-2">✅</div>
        <div class="text-base font-bold text-success">{{ successTitle }}</div>
        <p class="text-xs muted mt-2">{{ successHint }}</p>
      </div>
      <router-link v-if="createdWorkId" :to="`/works/${createdWorkId}`" class="btn btn-primary btn-block">查看本次成果</router-link>
      <router-link v-if="isCreative" to="/works?scope=mine&type=creative&reviewStatus=pending" class="btn btn-outline btn-block">查看我的创意实验</router-link>
      <router-link v-if="isRemix" to="/works?scope=mine&type=remix&reviewStatus=pending" class="btn btn-outline btn-block">查看我的拍同款</router-link>
      <router-link v-if="isHomework" to="/works?scope=mine" class="btn btn-outline btn-block">查看我的作品</router-link>
      <router-link v-if="isHomework && linkedTaskId" :to="`/tasks/${linkedTaskId}`" class="btn btn-outline btn-block">返回关联任务</router-link>
      <router-link v-else to="/tasks" class="btn btn-outline btn-block">返回我的任务</router-link>
    </div>

    <!-- 上传表单 -->
    <div v-else id="uploadFormBlock" class="flex-1 min-h-0 upload-form-body">
      <!-- Pad 横屏双栏 -->
      <div class="pad-upload">
        <section class="pad-upload__stage" aria-label="大图预览">
          <div v-if="stageFile" class="upload-stage-preview">
            <img
              v-if="stageFile.type !== 'video'"
              :src="previewSrc(stageFile)"
              alt=""
              class="upload-stage-preview__media"
            />
            <video
              v-else
              :src="previewSrc(stageFile)"
              controls
              playsinline
              class="upload-stage-preview__media"
            />
            <span v-if="stageFileIndex === 0" class="upload-stage-preview__cover-tag">封面</span>
          </div>
          <div v-else class="upload-stage-preview upload-stage-preview--empty">
            <span class="upload-stage-preview__placeholder">添加成果后在此预览<br>第一格默认为封面</span>
          </div>
          <p v-if="stageFile" class="text-xs muted mt-2 truncate px-1">
            {{ stageFile.name }} · {{ stageFile.size }}
          </p>
        </section>

        <aside class="pad-upload__rail" aria-label="文件与提交">
          <UploadMediaGrid
            :files="files"
            :disabled="taskAlreadySubmitted"
            :uploading="uploading"
            :highlight-index="previewIndex"
            title="全部文件"
            @add="openAddSheet"
            @remove="removeFile"
            @preview="setPreview"
            @reorder="reorderFiles"
          />

          <div class="field">
            <label class="label">📌 作品名称</label>
            <input
              v-model="workTitle"
              type="text"
              class="input"
              maxlength="80"
              placeholder="给本次成果起个名字，便于区分"
              @input="workTitleTouched = true"
            />
          </div>

          <div class="field">
            <label class="label">📝 实验描述 (可选)</label>
            <textarea v-model="note" class="textarea" rows="2" placeholder="简要描述实验过程、现象与结论…"></textarea>
          </div>

          <div v-if="isRemix && taskContext" class="field">
            <label class="label">📷 拍同款任务</label>
            <div class="rounded-xl surface-2 px-4 py-3 text-sm font-medium">{{ taskContext.title }}</div>
          </div>
          <div v-else-if="taskLocked && taskContext" class="field">
            <label class="label">📎 关联作业</label>
            <div class="rounded-xl surface-2 px-4 py-3 text-sm font-medium">{{ taskContext.title }}</div>
          </div>
          <div v-else-if="!isCreative" class="field">
            <label class="label">📎 关联作业</label>
            <select v-model="relatedTaskId" class="select">
              <option v-if="!relatedTasks.length" value="" disabled>暂无待完成实验任务</option>
              <option v-for="t in relatedTasks" :key="t.id" :value="t.id">{{ t.title }}</option>
            </select>
          </div>

          <button
            type="button"
            class="btn btn-gradient btn-block btn-lg"
            :disabled="submitting || uploading || !hasReadyFiles || taskAlreadySubmitted"
            @click="submitUpload"
          >📤 确认提交</button>
          <p class="text-xs muted-2 text-center">提交后可在「我的作品」或任务详情中查看进度</p>
        </aside>
      </div>

      <!-- 手机 / 平板竖屏 -->
      <div class="pad-upload__mobile-only px-4 stack-4 pb-28 overflow-y-auto">
        <UploadMediaGrid
          :files="files"
          :disabled="taskAlreadySubmitted"
          :uploading="uploading"
          title="全部文件"
          @add="openAddSheet"
          @remove="removeFile"
          @preview="openMobilePreview"
          @reorder="reorderFiles"
        />

        <div class="field">
          <label class="label">📌 作品名称</label>
          <input
            v-model="workTitle"
            type="text"
            class="input"
            maxlength="80"
            placeholder="给本次成果起个名字，便于区分"
            @input="workTitleTouched = true"
          />
        </div>

        <div class="field">
          <label class="label">📝 实验描述 (可选)</label>
          <textarea v-model="note" class="textarea" rows="3" placeholder="简要描述实验过程、现象与结论…"></textarea>
        </div>

        <div v-if="isRemix && taskContext" class="field">
          <label class="label">📷 拍同款任务</label>
          <div class="rounded-xl surface-2 px-4 py-3 text-sm font-medium">{{ taskContext.title }}</div>
        </div>
        <div v-else-if="taskLocked && taskContext" class="field">
          <label class="label">📎 关联作业</label>
          <div class="rounded-xl surface-2 px-4 py-3 text-sm font-medium">{{ taskContext.title }}</div>
        </div>
        <div v-else-if="!isCreative" class="field">
          <label class="label">📎 关联作业</label>
          <select v-model="relatedTaskId" class="select">
            <option v-if="!relatedTasks.length" value="" disabled>暂无待完成实验任务</option>
            <option v-for="t in relatedTasks" :key="t.id" :value="t.id">{{ t.title }}</option>
          </select>
        </div>

        <button
          type="button"
          class="btn btn-gradient btn-block btn-lg"
          :disabled="submitting || uploading || !hasReadyFiles || taskAlreadySubmitted"
          @click="submitUpload"
        >📤 确认提交</button>
        <p class="text-xs muted-2 text-center">提交后可在「我的作品」或任务详情中查看进度</p>
      </div>
    </div>

    <!-- 添加方式：朋友圈式底部面板 -->
    <Teleport to="body">
      <div
        v-if="showAddSheet"
        class="upload-sheet-backdrop"
        role="presentation"
        @click="closeAddSheet"
      >
        <div class="upload-sheet" role="dialog" aria-label="添加成果" @click.stop>
          <button type="button" class="upload-sheet__item" @click="pickCamera">
            拍摄<span class="upload-sheet__sub">点击拍照 · 长按录像</span>
          </button>
          <button type="button" class="upload-sheet__item" @click="pickGallery">从相册选择</button>
          <button type="button" class="upload-sheet__cancel" @click="closeAddSheet">取消</button>
        </div>
      </div>

      <div
        v-if="mobilePreviewFile"
        class="upload-preview-overlay"
        role="dialog"
        aria-label="预览"
        @click="mobilePreviewFile = null"
      >
        <button type="button" class="upload-preview-overlay__close" aria-label="关闭" @click.stop="mobilePreviewFile = null">×</button>
        <img
          v-if="mobilePreviewFile.type !== 'video'"
          :src="previewSrc(mobilePreviewFile)"
          alt=""
          class="upload-preview-overlay__media"
          @click.stop
        />
        <video
          v-else
          :src="previewSrc(mobilePreviewFile)"
          controls
          playsinline
          autoplay
          class="upload-preview-overlay__media"
          @click.stop
        />
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UploadMediaGrid from '@/components/upload/UploadMediaGrid.vue'
import UploadCameraCapture from '@/components/upload/UploadCameraCapture.vue'
import { createWork, uploadFile, parseUploadResponse } from '@/api/work'
import { fetchTaskDetail, fetchTasks } from '@/api/task'
import { useParentContext } from '@/composables/useParentContext'
import { isParentRole } from '@/utils/role'
import { useUserStore } from '@/stores/user'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { resolveDisplayUrl } from '@/utils/fileUrl'
import {
  buildUploadDraftKey,
  clearUploadDraft,
  loadUploadDraft,
  saveUploadDraft
} from '@/utils/uploadDraft'
import {
  MAX_UPLOAD_IMAGES,
  MAX_UPLOAD_VIDEOS,
  canAddUploadFile,
  countUploadFilesByType,
  isMinioStorageUrl,
  isServerUploadedUrl
} from '@/utils/uploadLimits'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { childQueryParam, selectedChild, loadChildren } = useParentContext()
const isParentView = computed(() => isParentRole(userStore.userInfo.userRoleId))

const relatedTasks = ref([])
const taskContext = ref(null)
const galleryInputRef = ref(null)
const cameraFallbackRef = ref(null)
const draftRestored = ref(false)
const showAddSheet = ref(false)
const showCameraCapture = ref(false)
const previewIndex = ref(-1)
const mobilePreviewFile = ref(null)

const isCreative = computed(() => route.query.type === 'creative')
const isRemix = computed(() => route.query.type === 'remix')
const isHomework = computed(() => !isCreative.value && !isRemix.value)
const taskLocked = computed(() => Boolean(route.query.taskId))
const taskAlreadySubmitted = computed(() => {
  const state = taskContext.value?.state
  if (!state) return false
  return ['submitted', 'reviewed', 'done'].includes(state)
})

const draftScope = computed(() => ({
  userId: userStore.userInfo?.userId || userStore.userInfo?.id || 'anon',
  workType: isCreative.value ? 'creative' : (isRemix.value ? 'remix' : 'homework'),
  taskId: String(route.query.taskId || relatedTaskId.value || ''),
  childUserId: isParentView.value ? (childQueryParam() || '') : ''
}))
const draftKey = computed(() => buildUploadDraftKey(draftScope.value))

const note = ref('')
const workTitle = ref('')
const workTitleTouched = ref(false)
const relatedTaskId = ref('')
const linkedTaskId = ref('')
const createdWorkId = ref('')
const files = ref([])
const submitted = ref(false)
const submitting = ref(false)
const uploading = ref(false)

const hasReadyFiles = computed(() => files.value.some((f) => f.status === 'done' && isServerUploadedUrl(f.url)))

const stageFileIndex = computed(() => {
  if (previewIndex.value >= 0 && files.value[previewIndex.value]) return previewIndex.value
  return files.value.length ? 0 : -1
})

const stageFile = computed(() => {
  const idx = stageFileIndex.value
  return idx >= 0 ? files.value[idx] : null
})

const successTitle = computed(() => {
  if (isRemix.value) return '拍同款已提交'
  if (isCreative.value) return '创意实验已提交'
  return '作业已提交'
})

const successHint = computed(() => {
  if (isRemix.value) return '老师审核通过后，作品将展示在作品墙「拍同款」'
  if (isCreative.value) return '老师审核通过后，作品将展示在作品墙「创意实验」'
  return '老师将尽快批阅，可在「我的任务」中查看状态'
})

function newFileId() {
  return `uf-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

function previewSrc(file) {
  if (file?.previewUrl?.startsWith('blob:')) return file.previewUrl
  return resolveDisplayUrl(file?.previewUrl, file?.url)
}

function openAddSheet() {
  if (taskAlreadySubmitted.value || uploading.value) return
  showAddSheet.value = true
}

function closeAddSheet() {
  showAddSheet.value = false
}

function pickCamera() {
  closeAddSheet()
  if (taskAlreadySubmitted.value) return
  if (navigator.mediaDevices?.getUserMedia) {
    showCameraCapture.value = true
    return
  }
  cameraFallbackRef.value?.click()
}

function onCameraCaptured(file) {
  showCameraCapture.value = false
  if (file) ingestNativeFiles([file], { fromCamera: true })
}

async function onCameraFallbackPicked(event) {
  const picked = Array.from(event.target.files || [])
  event.target.value = ''
  await ingestNativeFiles(picked, { fromCamera: true })
}

function pickGallery() {
  closeAddSheet()
  if (taskAlreadySubmitted.value) return
  galleryInputRef.value?.click()
}

function setPreview(_file, idx) {
  previewIndex.value = idx
}

function openMobilePreview(file) {
  mobilePreviewFile.value = file
}

function reorderFiles({ from, to }) {
  if (from === to || from < 0 || to < 0) return
  const list = [...files.value]
  if (from >= list.length || to >= list.length) return
  const [item] = list.splice(from, 1)
  list.splice(to, 0, item)
  files.value = list

  if (previewIndex.value === from) previewIndex.value = to
  else if (from < previewIndex.value && to >= previewIndex.value) previewIndex.value -= 1
  else if (from > previewIndex.value && to <= previewIndex.value) previewIndex.value += 1

  persistDraft()
}

function revokePreview(file) {
  if (file?.previewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(file.previewUrl)
  }
}

function removeFile(index) {
  if (index < 0 || index >= files.value.length) return
  const removed = files.value[index]
  revokePreview(removed)
  files.value.splice(index, 1)
  if (previewIndex.value === index) {
    previewIndex.value = files.value.length ? Math.min(index, files.value.length - 1) : -1
  } else if (previewIndex.value > index) {
    previewIndex.value -= 1
  }
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function readVideoDuration(sourceUrl) {
  return new Promise((resolve) => {
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.onloadedmetadata = () => {
      const sec = Math.round(video.duration || 0)
      if (!sec || !Number.isFinite(sec)) {
        resolve('')
        return
      }
      const m = Math.floor(sec / 60)
      const s = sec % 60
      resolve(`${m}:${String(s).padStart(2, '0')}`)
    }
    video.onerror = () => resolve('')
    video.src = sourceUrl
  })
}

function normalizeDraftFile(raw) {
  if (!raw || !isServerUploadedUrl(raw.url)) return null
  return {
    id: raw.id || newFileId(),
    type: raw.type === 'video' ? 'video' : 'image',
    name: raw.name || '文件',
    size: raw.size || '',
    previewUrl: raw.previewUrl || '',
    url: raw.url,
    status: 'done',
    duration: raw.duration || '',
    grad: raw.grad,
    icon: raw.icon
  }
}

function buildDefaultWorkTitle() {
  const taskTitle = taskContext.value?.title
    || relatedTasks.value.find((t) => t.id === relatedTaskId.value)?.title
  const now = new Date()
  const mmdd = `${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  if (taskTitle) return `${taskTitle} · ${mmdd}`
  if (isCreative.value) return `创意实验 · ${mmdd}`
  return `实验成果 · ${mmdd}`
}

function applyDefaultWorkTitle() {
  if (workTitleTouched.value) return
  workTitle.value = buildDefaultWorkTitle()
}

function restoreDraftIfAny() {
  const draft = loadUploadDraft(draftKey.value)
  if (!draft) return
  if (typeof draft.note === 'string') note.value = draft.note
  if (typeof draft.workTitle === 'string' && draft.workTitle.trim()) {
    workTitle.value = draft.workTitle.trim()
    workTitleTouched.value = true
  }
  if (Array.isArray(draft.files)) {
    const valid = draft.files.map(normalizeDraftFile).filter(Boolean)
    if (valid.length) {
      files.value = valid
      previewIndex.value = Math.min(draft.previewIndex ?? 0, valid.length - 1)
      draftRestored.value = true
    }
  }
  if (!taskLocked.value && draft.relatedTaskId) {
    relatedTaskId.value = draft.relatedTaskId
  }
}

function persistDraft() {
  if (submitted.value) return
  const ready = files.value
    .filter((f) => f.status === 'done' && isServerUploadedUrl(f.url))
    .map(({ id, type, name, size, url, previewUrl, duration, grad, icon }) => ({
      id, type, name, size, url, previewUrl, duration, grad, icon
    }))
  saveUploadDraft(draftKey.value, {
    note: note.value,
    workTitle: workTitle.value,
    files: ready,
    previewIndex: previewIndex.value,
    relatedTaskId: relatedTaskId.value
  })
}

async function ingestNativeFiles(picked, { fromCamera = false } = {}) {
  if (!picked.length) return
  uploading.value = true
  const failedNames = []

  try {
    for (const file of picked) {
      const gate = canAddUploadFile(files.value, file)
      if (!gate.ok) {
        alert(gate.message)
        if (fromCamera) break
        continue
      }

      const isVideo = file.type.startsWith('video/')
      const previewUrl = URL.createObjectURL(file)
      const item = {
        id: newFileId(),
        type: isVideo ? 'video' : 'image',
        name: file.name || (isVideo ? '视频.mp4' : '照片.jpg'),
        size: formatSize(file.size),
        previewUrl,
        url: '',
        status: 'uploading',
        duration: isVideo ? await readVideoDuration(previewUrl) : '',
        grad: isVideo ? 'card-media-grad-amber-rose' : 'card-media-grad-violet-blue',
        icon: isVideo ? '🎬' : '📷'
      }
      files.value.push(item)
      previewIndex.value = files.value.length - 1

      try {
        const up = await uploadFile(file)
        const { fileUrl, previewUrl } = parseUploadResponse(up)
        if (!isMinioStorageUrl(fileUrl)) throw new Error('invalid url')
        item.url = fileUrl
        item.previewUrl = previewUrl || fileUrl
        item.status = 'done'
      } catch {
        item.status = 'error'
        failedNames.push(item.name)
      }
    }

    if (failedNames.length) {
      alert(`以下文件上传失败，请删除后重试：\n${failedNames.join('\n')}`)
    }
    persistDraft()
  } finally {
    uploading.value = false
    initIcons()
  }
}

async function onGalleryPicked(event) {
  const picked = Array.from(event.target.files || [])
  event.target.value = ''
  await ingestNativeFiles(picked, { fromCamera: false })
}

function goBack() {
  if (submitted.value) router.push('/tasks')
  else router.back()
}

async function submitUpload() {
  if (submitting.value || taskAlreadySubmitted.value) return
  if (isParentView.value && !childQueryParam()) {
    alert('请先选择要协助的孩子')
    router.push('/bind-child')
    return
  }

  const readyFiles = files.value.filter((f) => f.status === 'done' && isServerUploadedUrl(f.url))
  if (!readyFiles.length) {
    alert('请先添加至少一张上传成功的照片或视频')
    return
  }
  if (files.value.some((f) => f.status === 'uploading')) {
    alert('仍有文件正在上传，请稍候')
    return
  }
  if (files.value.some((f) => f.status === 'error')) {
    alert('存在上传失败的文件，请删除后重新添加')
    return
  }

  const counts = countUploadFilesByType(readyFiles)
  if (counts.image > MAX_UPLOAD_IMAGES || counts.video > MAX_UPLOAD_VIDEOS) {
    alert(`最多 ${MAX_UPLOAD_IMAGES} 张照片和 ${MAX_UPLOAD_VIDEOS} 段视频`)
    return
  }

  submitting.value = true
  const workType = isCreative.value ? 'creative' : (isRemix.value ? 'remix' : 'homework')
  const relatedTitle = relatedTasks.value.find((t) => t.id === relatedTaskId.value)?.title
    || taskContext.value?.title
    || ''
  const title = workTitle.value?.trim() || buildDefaultWorkTitle()
  if (!title) {
    alert('请填写作品名称')
    submitting.value = false
    return
  }
  const resolvedTaskId = route.query.taskId || relatedTaskId.value || undefined

  const payload = {
    title,
    description: note.value?.trim() || undefined,
    workType,
    relatedTaskTitle: isCreative.value || isRemix.value ? undefined : (relatedTitle || undefined),
    taskId: resolvedTaskId,
    sourceExpId: taskContext.value?.videoId || undefined,
    studentUserId: isParentView.value ? childQueryParam() : undefined,
    files: readyFiles.map((f) => ({
      type: f.type,
      name: f.name,
      size: f.size,
      grad: f.grad,
      icon: f.icon,
      duration: f.duration || undefined,
      url: f.url
    }))
  }

  try {
    const res = await createWork(payload)
    if (res?.code === 200) {
      createdWorkId.value = res.data?.id || res.data?.workId || ''
      linkedTaskId.value = resolvedTaskId || ''
      submitted.value = true
      clearUploadDraft(draftKey.value)
      files.value.forEach(revokePreview)
      initIcons()
      return
    }
    alert(res?.message || '提交失败')
  } catch (e) {
    console.warn('提交作品失败', e)
    alert('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const { initIcons } = useLucideIcons()

watch([note, workTitle, files, relatedTaskId, previewIndex], () => {
  persistDraft()
}, { deep: true })

watch([taskContext, relatedTaskId, relatedTasks, isCreative], () => {
  applyDefaultWorkTitle()
}, { deep: true })

onMounted(async () => {
  if (isParentView.value) await loadChildren()

  const taskId = route.query.taskId
  if (taskId) {
    try {
      const res = await fetchTaskDetail(taskId)
      if (res?.code === 200 && res.data) {
        taskContext.value = res.data
        if (res.data.id || res.data.taskId) {
          relatedTaskId.value = res.data.id || res.data.taskId
        }
      }
    } catch (e) {
      console.warn('加载任务上下文失败', e)
    }
  } else if (isHomework.value) {
    try {
      const [expRes, remixRes] = await Promise.all([
        fetchTasks({ category: 'experiment', status: 'pending', page: 1, size: 30 }),
        fetchTasks({ category: 'remix', status: 'pending', page: 1, size: 30 })
      ])
      const merged = [...(expRes?.data?.records || []), ...(remixRes?.data?.records || [])]
      const seen = new Set()
      relatedTasks.value = merged.filter((t) => {
        if (!t?.id || seen.has(t.id)) return false
        seen.add(t.id)
        return Boolean(t.title)
      }).map((t) => ({ id: t.id, title: t.title }))
      if (relatedTasks.value.length && !relatedTaskId.value) {
        relatedTaskId.value = relatedTasks.value[0].id
      }
    } catch (e) {
      console.warn('加载关联任务失败', e)
    }
  }

  restoreDraftIfAny()
  applyDefaultWorkTitle()
  initIcons()
})

onBeforeUnmount(() => {
  files.value.forEach(revokePreview)
})
</script>
