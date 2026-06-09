<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail" data-task-detail>
    <div class="topbar page-topbar safe-top">
      <router-link to="/tasks" class="icon-btn page-back" aria-label="返回">
        <i data-lucide="arrow-left" class="icon"></i>
      </router-link>
      <h1 class="topbar-title">任务详情</h1>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="error" class="px-4 py-12 text-center">
      <p class="muted-2">{{ error }}</p>
      <router-link to="/tasks" class="btn btn-ghost btn-sm mt-4">返回任务列表</router-link>
    </div>

    <div v-else-if="task" class="px-4 stack-4 pb-28">
      <div class="card card-pad" :class="'task-card--' + displayType">
        <span class="task-type-badge" :class="'task-type-badge--' + displayType">
          {{ taskTypeLabel(task.type) }}
        </span>
        <h1 class="text-lg font-bold mt-2">{{ task.title }}</h1>
        <p v-if="task.desc" class="text-xs muted mt-2">{{ task.desc }}</p>
        <div class="grid-2 gap-3 mt-4">
          <div class="rounded-xl surface-2 p-3">
            <div class="text-xs muted-2">状态</div>
            <div class="text-sm font-bold mt-1">{{ task.stateLabel || '待完成' }}</div>
          </div>
          <div class="rounded-xl surface-2 p-3">
            <div class="text-xs muted-2">{{ isTeacherAssigned ? '截止时间' : '任务类型' }}</div>
            <div class="text-sm font-bold mt-1">{{ isTeacherAssigned ? (task.deadline || '—') : (task.taskTypeLabel || '自愿完成') }}</div>
          </div>
        </div>
        <div v-if="task.teacherHint" class="rounded-xl p-3 mt-3 text-xs font-medium surface-2" :class="task.teacherHintClass || 'tint-orange'">
          {{ task.teacherHint }}
        </div>
      </div>

      <div v-if="showMediaCard" class="card card-pad">
        <div class="row justify-between items-center">
          <div>
            <div class="text-xs font-bold muted-2">{{ mediaCardTitle }}</div>
            <div class="text-base font-bold mt-1">{{ task.videoTitle || '—' }}</div>
          </div>
          <span v-if="task.videoDuration" class="badge badge-slate">{{ task.videoDuration }}</span>
        </div>
        <p v-if="task.videoMeta" class="text-xs muted mt-2">{{ task.videoMeta }}</p>
        <router-link
          :to="mediaLink"
          class="mt-3 btn btn-block"
          :class="isTeacherAssigned ? 'btn-primary' : 'btn-outline'"
        >
          {{ mediaButtonLabel }}
        </router-link>
      </div>

      <div v-if="hasExpBrief" class="card card-pad task-exp-brief">
        <div class="text-xs font-bold">实验资料</div>
        <div class="task-exp-brief__body mt-3 stack-3">
          <div v-if="task.expBrief.curriculumLine" class="task-exp-brief__row">
            <div class="task-exp-brief__label">📖 对照教材</div>
            <p class="task-exp-brief__text">{{ task.expBrief.curriculumLine }}</p>
          </div>
          <div v-if="task.expBrief.materialsLine" class="task-exp-brief__row">
            <div class="task-exp-brief__label">🧪 实验材料</div>
            <FormattedText class="task-exp-brief__text" :value="task.expBrief.materialsLine" :options="FORMAT_EXP_BRIEF" />
          </div>
          <div v-if="task.expBrief.stepsLine" class="task-exp-brief__row">
            <div class="task-exp-brief__label">📋 实验步骤</div>
            <FormattedText class="task-exp-brief__text" :value="task.expBrief.stepsLine" :options="FORMAT_EXP_BRIEF" />
          </div>
          <div v-if="task.expBrief.safetyLine" class="task-exp-brief__row">
            <div class="task-exp-brief__label">⚠️ 安全提示</div>
            <FormattedText class="task-exp-brief__text" :value="task.expBrief.safetyLine" :options="FORMAT_EXP_BRIEF" />
          </div>
        </div>
        <router-link
          v-if="task.videoId && !isSimulator"
          :to="`/exp/${task.videoId}`"
          class="btn btn-soft btn-block btn-sm mt-4"
        >
          查看完整实验详情
        </router-link>
      </div>

      <div v-if="completionGuide.length" class="card card-pad">
        <div class="text-xs font-bold muted-2">如何完成</div>
        <ol class="mt-3 stack-2">
          <li
            v-for="(line, i) in completionGuide"
            :key="i"
            class="rounded-xl surface-2 px-4 py-3 text-xs muted row items-start gap-2"
          >
            <span class="stepper-dot active shrink-0">{{ i + 1 }}</span>
            <span class="flex-1">{{ line }}</span>
          </li>
        </ol>
      </div>

      <router-link
        v-if="task.state !== 'done'"
        :to="uploadLink"
        class="btn btn-gradient btn-block btn-lg"
      >
        {{ uploadButtonLabel }}
      </router-link>
      <div v-else class="card card-pad tint-green text-center">
        <div class="text-sm font-bold text-success">任务已完成</div>
        <p v-if="task.type === 'remix'" class="text-xs muted mt-2">可在作品墙查看你的拍同款作品，或从实验详情再次发起新的拍同款</p>
        <p v-else-if="task.type === 'creative'" class="text-xs muted mt-2">可在作品墙查看你的创意实验作品</p>
      </div>

      <router-link v-if="isTeacherAssigned && task.type === 'homework'" to="/quiz" class="card card-pad card-link quiz-nudge row items-center gap-3 mt-4">
        <div class="tile-icon tile-sm tint-violet shrink-0">🧠</div>
        <div class="flex-1 min-w-0">
          <div class="text-sm font-bold">顺手完成每日答题</div>
          <p class="text-xs muted mt-1">每日答题 · 巩固本课知识点</p>
        </div>
        <i data-lucide="chevron-right" class="icon muted-2 shrink-0"></i>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import FormattedText from '@/components/FormattedText.vue'
import { FORMAT_EXP_BRIEF } from '@/utils/richText'
import { fetchTaskDetail } from '@/api/task'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const task = ref(null)

const isSimulator = computed(() => task.value?.type === 'simulator')
const isTeacherAssigned = computed(() => ['homework', 'simulator'].includes(task.value?.type))
const isCreative = computed(() => task.value?.type === 'creative')
const isRemix = computed(() => task.value?.type === 'remix')

const displayType = computed(() => {
  const type = task.value?.type
  if (type === 'simulator') return 'simulator'
  if (type === 'creative') return 'creative'
  if (type === 'remix') return 'remix'
  if (type === 'homework') return 'homework'
  return 'homework'
})

const showMediaCard = computed(() => {
  if (isCreative.value) return false
  return !!(task.value?.videoId || task.value?.videoTitle)
})

const mediaCardTitle = computed(() => {
  if (isSimulator.value) return '模拟实验'
  if (isRemix.value) return '参考视频'
  return '关联视频'
})

const mediaLink = computed(() => {
  const id = task.value?.videoId
  if (!id) return '/home'
  if (isSimulator.value) return `/sim/${id}`
  return `/exp/${id}`
})

const mediaButtonLabel = computed(() => {
  if (isSimulator.value) return '进入模拟实验'
  if (isRemix.value) return '回看参考视频'
  return '去观看视频'
})

const uploadButtonLabel = computed(() => {
  if (isRemix.value) return '开始拍同款上传'
  if (isCreative.value) return '上传创意实验成果'
  if (isSimulator.value) return '提交模拟实验成果'
  return '进入成果上传'
})

const hasExpBrief = computed(() => {
  const brief = task.value?.expBrief
  if (!brief) return false
  return !!(brief.curriculumLine || brief.materialsLine || brief.stepsLine || brief.safetyLine)
})

const completionGuide = computed(() => {
  const lines = task.value?.completionGuide
  return Array.isArray(lines) ? lines.filter(Boolean) : []
})

const uploadLink = computed(() => {
  const q = task.value?.uploadQuery
  if (q) return `/upload?${q}`
  const id = task.value?.id
  if (id && isRemix.value) return `/upload?type=remix&taskId=${id}`
  if (id && isCreative.value) return `/upload?type=creative&taskId=${id}`
  return '/upload'
})

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

function taskTypeLabel(type) {
  if (type === 'homework') return '🔬 实验'
  if (type === 'simulator') return '🖥️ 模拟实验'
  if (type === 'remix') return '📷 拍同款'
  if (type === 'creative') return '💡 创意'
  return '📋 任务'
}

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchTaskDetail(route.params.id)
    if (res?.code === 200 && res.data) {
      task.value = { ...res.data }
    } else {
      throw new Error('not found')
    }
  } catch {
    error.value = '任务不存在或加载失败'
  } finally {
    loading.value = false
    initIcons()
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.task-exp-brief__row + .task-exp-brief__row {
  padding-top: 12px;
  border-top: 1px solid var(--color-border, rgba(0, 0, 0, 0.06));
}
.task-exp-brief__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-muted-2, #64748b);
}
.task-exp-brief__text {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--color-muted, #475569);
  word-break: break-word;
}
</style>
