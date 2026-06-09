<template>

  <div class="prototype-container pad-shell" data-layout="upload-split">

    <div class="topbar safe-top">

      <button type="button" class="icon-btn" aria-label="返回" @click="goBack">

        <i data-lucide="arrow-left" class="icon"></i>

      </button>

      <span class="topbar-title">成果上传</span>

      <button type="button" class="icon-btn" aria-label="拍照" @click="openFilePicker">

        <i data-lucide="camera" class="icon"></i>

      </button>

      <button type="button" class="icon-btn" aria-label="相册" @click="openFilePicker">

        <i data-lucide="image" class="icon"></i>

      </button>

    </div>

    <div v-if="isParentView && selectedChild" class="px-4 pt-3">
      <div class="card card-pad text-sm">
        正在为 <strong>{{ selectedChild.name }}</strong> 协助上传
      </div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*,video/*"
      multiple
      style="display:none"
      @change="onFilesSelected"
    />



    <div v-if="isCreative" class="px-4 safe-top" style="padding-top:8px;">

      <p class="quiz-tip-banner">💡 <strong>创意实验</strong> · 自由上传实验成果，不关联老师作业，提交后归入作品墙「创意实验」</p>

    </div>



    <!-- 提交成功 -->

    <div v-if="submitted" class="px-4 stack-3 pb-8 anim-fade-up flex-1 overflow-y-auto">

      <div class="card card-pad tint-green text-center">

        <div class="text-2xl mb-2">✅</div>

        <div class="text-base font-bold text-success">{{ successTitle }}</div>

        <p class="text-xs muted mt-2">{{ successHint }}</p>

      </div>

      <router-link v-if="isHomework" to="/quiz" class="card card-pad card-link quiz-nudge row items-center gap-3">

        <div class="tile-icon tile-sm tint-violet shrink-0">🧠</div>

        <div class="flex-1 min-w-0">

          <div class="text-sm font-bold">继续完成每日答题</div>

          <p class="text-xs muted mt-1">每日答题 · 约 3 分钟</p>

        </div>

        <span class="btn btn-soft btn-sm shrink-0">去答题</span>

      </router-link>

      <router-link v-if="isRemix" to="/works?type=remix" class="btn btn-outline btn-block">查看拍同款作品墙</router-link>

      <router-link to="/tasks" class="btn btn-outline btn-block">返回我的任务</router-link>

    </div>



    <!-- 上传表单 -->

    <div v-else id="uploadFormBlock" class="flex-1 min-h-0" style="display:flex;flex-direction:column;overflow:hidden;">

      <!-- Pad 双栏 -->

      <div class="pad-upload">

        <section class="pad-upload__stage" aria-label="主预览">

          <div v-if="activeFile" class="upload-main-hero" :class="activeFile.grad">

            <span class="badge badge-info upload-main-hero__badge">{{ activeFile.type === 'video' ? '视频' : '照片' }}</span>

            <span class="upload-main-hero__icon">{{ activeFile.icon }}</span>

            <button

              v-if="activeFile.type === 'video'"

              type="button"

              class="upload-main-hero__play"

              aria-label="播放"

              @click="previewActiveFile"

            >

              <i data-lucide="play" class="icon"></i>

            </button>

            <span v-if="activeFile.duration" class="upload-main-hero__duration">{{ activeFile.duration }}</span>

            <div v-if="activeFile.type === 'video'" class="upload-main-hero__toolbar">

              <div class="upload-main-hero__progress"><div class="upload-main-hero__progress-fill"></div></div>

              <span class="upload-main-hero__hint">点击画面播放 · 支持 mp4 短视频</span>

            </div>

          </div>

          <div v-else class="upload-main-hero card-media-grad-slate upload-main-hero--empty">

            <span class="upload-main-hero__icon">📤</span>

            <p class="text-sm muted mt-2">点击上方或右侧按钮选择照片/视频</p>

          </div>

          <div v-if="activeFile" class="card card-pad row items-center justify-between gap-3">

            <div class="min-w-0">

              <div class="text-sm font-bold truncate">{{ activeFile.name }}</div>

              <div class="text-xs muted">{{ activeFile.size }} · 主图预览</div>

            </div>

            <button type="button" class="btn btn-soft btn-sm" disabled>主图</button>

          </div>

        </section>



        <aside class="pad-upload__rail" aria-label="文件与提交">

          <div class="row items-center justify-between gap-2">

            <div>

              <div class="text-sm font-bold">已选成果</div>

              <div class="text-xs muted">{{ files.length }} 个文件 · 最多 9 张图 + 3 段视频</div>

            </div>

            <div class="row gap-1 shrink-0">

              <button type="button" class="btn btn-soft btn-sm" :disabled="uploading" @click="openFilePicker"><i data-lucide="camera" class="icon icon-sm"></i></button>

              <button type="button" class="btn btn-soft btn-sm" :disabled="uploading" @click="openFilePicker"><i data-lucide="image" class="icon icon-sm"></i></button>

            </div>

          </div>

          <div class="pad-upload__thumb-grid">

            <button

              v-for="(file, idx) in files"

              :key="idx"

              type="button"

              class="upload-thumb"

              :class="{ 'is-active': idx === activeIndex }"

              @click="activeIndex = idx"

            >

              <span class="upload-thumb__inner" :class="file.grad">{{ file.icon }}</span>

              <span class="upload-thumb__tag">{{ file.type === 'video' ? '视频' : '图' }}</span>

              <span v-if="file.duration" class="upload-thumb__dur">{{ file.duration }}</span>

            </button>

          </div>

          <div class="field">

            <label class="label">📝 备注 (可选)</label>

            <textarea v-model="note" class="textarea" rows="2" placeholder="输入实验心得…"></textarea>

          </div>

          <div v-if="isRemix && taskContext" class="field">

            <label class="label">📷 拍同款任务</label>

            <div class="rounded-xl surface-2 px-4 py-3 text-sm font-medium">{{ taskContext.title }}</div>

          </div>

          <div v-else-if="!isCreative" class="field">

            <label class="label">📎 关联作业</label>

            <select v-model="relatedTask" class="select">

              <option v-if="!relatedTasks.length" value="" disabled>暂无待完成实验任务</option>

              <option v-for="t in relatedTasks" :key="t" :value="t">{{ t }}</option>

            </select>

          </div>

          <button type="button" class="btn btn-gradient btn-block btn-lg" :disabled="submitting || uploading || !hasFiles" @click="submitUpload">📤 确认提交</button>

          <p class="text-xs muted-2 text-center">⚡ 网络断开时自动保存到本地</p>

        </aside>

      </div>



      <!-- 手机单列 -->

      <div class="pad-upload__mobile-only px-4 stack-4 pb-28 overflow-y-auto">

        <div>

          <div class="row items-center justify-between mb-2">

            <span class="text-sm font-bold">主预览</span>

            <span class="text-xs muted">{{ files.length }} 个文件</span>

          </div>

          <div v-if="activeFile" class="upload-main-hero upload-mobile-main" :class="activeFile.grad">

            <span class="badge badge-info upload-main-hero__badge">{{ activeFile.type === 'video' ? '视频' : '照片' }}</span>

            <span class="upload-main-hero__icon">{{ activeFile.icon }}</span>

            <button

              v-if="activeFile.type === 'video'"

              type="button"

              class="upload-main-hero__play"

              aria-label="播放"

              @click="previewActiveFile"

            >

              <i data-lucide="play" class="icon"></i>

            </button>

            <span v-if="activeFile.duration" class="upload-main-hero__duration">{{ activeFile.duration }}</span>

          </div>

          <div v-else class="upload-main-hero upload-mobile-main card-media-grad-slate upload-main-hero--empty">

            <span class="upload-main-hero__icon">📤</span>

            <p class="text-xs muted mt-2">点击下方拍照或相册上传</p>

          </div>

          <p v-if="activeFile" class="text-xs muted mt-2 truncate">{{ activeFile.name }} · {{ activeFile.size }}</p>

        </div>



        <div>

          <div class="text-xs font-medium muted mb-2">全部文件（点击切换主预览）</div>

          <div class="upload-mobile-thumbs">

            <button

              v-for="(file, idx) in files"

              :key="idx"

              type="button"

              class="upload-thumb"

              :class="{ 'is-active': idx === activeIndex }"

              @click="activeIndex = idx"

            >

              <span class="upload-thumb__inner" :class="file.grad">{{ file.icon }}</span>

              <span class="upload-thumb__tag">{{ file.type === 'video' ? '视频' : '图' }}</span>

              <span v-if="file.duration" class="upload-thumb__dur">{{ file.duration }}</span>

            </button>

          </div>

        </div>



        <div class="field">

          <label class="label">📝 备注 (可选)</label>

          <textarea v-model="note" class="textarea" rows="3" placeholder="输入实验心得…"></textarea>

        </div>

        <div v-if="isRemix && taskContext" class="field">

          <label class="label">📷 拍同款任务</label>

          <div class="rounded-xl surface-2 px-4 py-3 text-sm font-medium">{{ taskContext.title }}</div>

        </div>

        <div v-else-if="!isCreative" class="field">

          <label class="label">📎 关联作业</label>

          <select v-model="relatedTask" class="select">

            <option v-if="!relatedTasks.length" value="" disabled>暂无待完成实验任务</option>

            <option v-for="t in relatedTasks" :key="t" :value="t">{{ t }}</option>

          </select>

        </div>

        <button type="button" class="btn btn-gradient btn-block btn-lg" :disabled="submitting || uploading || !hasFiles" @click="submitUpload">📤 确认提交</button>

        <p class="text-xs muted-2 text-center">⚡ 网络断开时自动保存到本地</p>

      </div>

    </div>

  </div>

</template>



<script setup>

import { ref, computed, onMounted, nextTick, watch } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import { createWork, uploadFile } from '@/api/work'
import { fetchTaskDetail, fetchTasks } from '@/api/task'
import { useParentContext } from '@/composables/useParentContext'
import { isParentRole } from '@/utils/role'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { childQueryParam, selectedChild, loadChildren, children: parentChildren } = useParentContext()
const isParentView = computed(() => isParentRole(userStore.userInfo.userRoleId))
const relatedTasks = ref([])
const taskContext = ref(null)
const fileInputRef = ref(null)

const isCreative = computed(() => route.query.type === 'creative')
const isRemix = computed(() => route.query.type === 'remix')
const isHomework = computed(() => !isCreative.value && !isRemix.value)

const note = ref('')
const relatedTask = ref('')
const files = ref([])
const activeIndex = ref(0)
const submitted = ref(false)
const submitting = ref(false)
const uploading = ref(false)

const hasFiles = computed(() => files.value.length > 0)
const activeFile = computed(() => files.value[activeIndex.value] || null)

const successTitle = computed(() => {
  if (isRemix.value) return '拍同款已完成'
  if (isCreative.value) return '创意实验已提交'
  return '作业已提交'
})

const successHint = computed(() => {
  if (isRemix.value) return '作品已归入作品墙「拍同款」，无需等待老师批阅'
  if (isCreative.value) return '可在作品墙「创意实验」中查看'
  return '老师将尽快批阅，可在「我的任务」中查看状态'
})

function openFilePicker() {
  fileInputRef.value?.click()
}

async function onFilesSelected(event) {
  const picked = Array.from(event.target.files || [])
  if (!picked.length) return
  uploading.value = true
  try {
    for (const file of picked) {
      const isVideo = file.type.startsWith('video/')
      let url = ''
      try {
        const up = await uploadFile(file)
        url = up?.data?.url || up?.data?.fileUrl || up?.url || ''
      } catch {
        url = URL.createObjectURL(file)
      }
      files.value.push({
        type: isVideo ? 'video' : 'image',
        name: file.name,
        size: formatSize(file.size),
        grad: isVideo ? 'card-media-grad-amber-rose' : 'card-media-grad-violet-blue',
        icon: isVideo ? '🎬' : '📷',
        duration: '',
        url
      })
    }
    activeIndex.value = Math.max(0, files.value.length - picked.length)
  } finally {
    uploading.value = false
    event.target.value = ''
    initIcons()
  }
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function previewActiveFile() {
  const f = activeFile.value
  if (!f?.url) return
  if (f.type === 'video') {
    window.open(f.url, '_blank', 'noopener')
  }
}

function goBack() {
  if (submitted.value) router.push('/tasks')
  else router.back()
}



async function submitUpload() {
  if (submitting.value) return
  if (!files.value.length) {
    alert('请先上传至少一张照片或视频')
    return
  }
  submitting.value = true
  const workType = isCreative.value ? 'creative' : (isRemix.value ? 'remix' : 'homework')
  const title = taskContext.value?.title
    || activeFile.value?.name?.replace(/\.[^.]+$/, '')
    || relatedTask.value
    || '实验成果'
  const payload = {
    title,
    description: note.value || undefined,
    workType,
    relatedTaskTitle: isCreative.value || isRemix.value ? undefined : relatedTask.value,
    taskId: route.query.taskId || undefined,
    sourceExpId: taskContext.value?.videoId || undefined,
    studentUserId: isParentView.value ? childQueryParam() : undefined,
    files: files.value.map((f) => ({
      type: f.type,
      name: f.name,
      size: f.size,
      grad: f.grad,
      icon: f.icon,
      duration: f.duration || undefined,
      url: f.url || undefined
    }))
  }
  try {
    const res = await createWork(payload)
    if (res?.code === 200) {
      submitted.value = true
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



function initIcons() {

  nextTick(() => {

    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})

  })

}



watch(activeIndex, initIcons)



onMounted(async () => {
  if (isParentView.value) {
    await loadChildren()
  }
  const taskId = route.query.taskId
  if (taskId) {
    try {
      const res = await fetchTaskDetail(taskId)
      if (res?.code === 200 && res.data) {
        taskContext.value = res.data
        if (res.data.title) {
          relatedTask.value = res.data.title
        }
      }
    } catch (e) {
      console.warn('加载任务上下文失败', e)
    }
  } else if (isHomework.value) {
    try {
      const res = await fetchTasks({ category: 'experiment', status: 'pending', page: 1, size: 30 })
      const titles = (res?.data?.records || []).map((t) => t.title).filter(Boolean)
      relatedTasks.value = titles
      if (titles.length) relatedTask.value = titles[0]
    } catch (e) {
      console.warn('加载关联任务失败', e)
    }
  }
  initIcons()
})

</script>

