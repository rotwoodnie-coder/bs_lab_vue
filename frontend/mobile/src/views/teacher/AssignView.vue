<template>
  <div class="prototype-container pad-shell theme-teacher safe-top" data-layout="teacher-form">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/home" />
        <h1 class="pad-workbench__title">📝 布置作业</h1>
      </header>

      <div class="pad-form__mobile-only pb-28">
        <div class="topbar safe-top">
          <PageBackButton fallback="/home" />
          <h1 class="topbar-title flex-1">📝 布置作业</h1>
        </div>

        <div class="px-4 py-4 stack-4">
          <div v-if="optionsLoading" class="text-sm muted py-2">加载选项中…</div>

          <div>
            <h2 class="text-sm font-bold mb-3">🧪 选择实验</h2>
            <div class="stack">
              <button
                v-for="(exp, idx) in expOptions"
                :key="exp.id"
                type="button"
                class="option-card w-full text-left"
                :class="{ selected: formExpId === exp.id }"
                @click="selectExp(exp.id)"
              >
                <div class="row items-center gap-3">
                  <div class="tile-icon tile-sm" :class="expTint(idx)">{{ expEmoji(idx) }}</div>
                  <div class="flex-1 min-w-0">
                    <div class="text-sm font-bold">{{ exp.label }}</div>
                    <div class="text-xs muted">{{ expSubtitle(exp) }}</div>
                  </div>
                </div>
                <span class="option-check">✓</span>
              </button>
              <p v-if="!optionsLoading && !expOptions.length" class="text-xs muted">暂无实验，请先在管理端发布</p>
            </div>
          </div>

          <div>
            <h2 class="text-sm font-bold mb-3">📋 选择班级</h2>
            <div class="stack">
              <button
                v-for="cls in classOptions"
                :key="cls.id"
                type="button"
                class="option-card w-full text-left"
                :class="{ selected: selectedClassIds.has(cls.id) }"
                @click="toggleClass(cls.id)"
              >
                <div class="flex-1">
                  <div class="text-sm font-bold">{{ cls.label }}</div>
                  <div class="text-xs muted">{{ cls.studentCount || 0 }} 名学生</div>
                </div>
                <span class="option-check">✓</span>
              </button>
              <p v-if="!optionsLoading && !classOptions.length" class="text-xs muted">暂无班级数据</p>
            </div>
          </div>

          <div>
            <h2 class="text-sm font-bold mb-3">📅 截止日期</h2>
            <input v-model="deadlineDate" class="input" type="date" />
          </div>

          <div>
            <h2 class="text-sm font-bold mb-3">📝 备注说明</h2>
            <textarea v-model="requirements" class="textarea" rows="3" placeholder="请输入补充说明（可选）…"></textarea>
          </div>

          <div class="card card-pad surface-2">
            <h3 class="text-xs font-bold muted mb-2">当前选择</h3>
            <div class="row wrap gap-2">
              <span v-if="selectedExp" class="badge badge-info">🔬 {{ selectedExp.label }}</span>
              <span v-if="selectedClassCount" class="badge badge-success">{{ selectedClassCount }} 个班级 · {{ selectedStudentTotal }} 人</span>
              <span v-if="deadlineLabel" class="badge badge-warning">截止 {{ deadlineLabel }}</span>
            </div>
          </div>

          <button
            type="button"
            class="btn btn-primary btn-block"
            :disabled="submitting || !canSubmit"
            @click="submit"
          >
            <i data-lucide="send" class="icon"></i>
            {{ submitting ? '发布中…' : '发布作业' }}
          </button>

          <div v-if="publishSuccess" class="card card-pad text-center tint-green">
            <div class="text-3xl mb-2">🎉</div>
            <div class="text-sm font-bold text-success">作业发布成功！</div>
            <div class="text-xs muted mt-1">已通知 {{ selectedStudentTotal }} 名学生</div>
            <router-link to="/home" class="btn btn-outline btn-sm mt-3">返回首页</router-link>
          </div>
        </div>
      </div>
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAppStore } from '@/stores/app'
import { createTask } from '@/api/task'
import { fetchTeacherAssignOptions } from '@/api/teacher'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'

const appStore = useAppStore()
appStore.setActiveTab('tasks')

const classOptions = ref([])
const expOptions = ref([])
const formExpId = ref('')
const selectedClassIds = ref(new Set())
const deadlineDate = ref('')
const requirements = ref('')
const submitting = ref(false)
const optionsLoading = ref(true)
const publishSuccess = ref(false)

const EXP_EMOJIS = ['🔬', '🌱', '⚡']
const EXP_TINTS = ['tint-blue', 'tint-green', 'tint-amber']

const selectedExp = computed(() => expOptions.value.find((e) => e.id === formExpId.value))
const selectedClasses = computed(() => classOptions.value.filter((c) => selectedClassIds.value.has(c.id)))
const selectedClassCount = computed(() => selectedClasses.value.length)
const selectedStudentTotal = computed(() =>
  selectedClasses.value.reduce((sum, c) => sum + (Number(c.studentCount) || 0), 0)
)
const canSubmit = computed(() => Boolean(selectedExp.value?.label && selectedClassCount.value > 0))

const deadlineLabel = computed(() => {
  if (!deadlineDate.value) return ''
  const parts = deadlineDate.value.split('-')
  if (parts.length !== 3) return deadlineDate.value
  return `${Number(parts[1])}月${Number(parts[2])}日`
})

function expEmoji(idx) {
  return EXP_EMOJIS[idx % EXP_EMOJIS.length]
}

function expTint(idx) {
  return EXP_TINTS[idx % EXP_TINTS.length]
}

function expSubtitle(exp) {
  return exp.subtitle || '实验库任务'
}

function selectExp(id) {
  formExpId.value = id
  publishSuccess.value = false
}

function toggleClass(id) {
  const next = new Set(selectedClassIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedClassIds.value = next
  publishSuccess.value = false
}

function defaultDeadline() {
  const d = new Date()
  d.setDate(d.getDate() + 7)
  return d.toISOString().slice(0, 10)
}

async function loadOptions() {
  optionsLoading.value = true
  try {
    const res = await fetchTeacherAssignOptions()
    if (res?.code === 200 && res.data) {
      classOptions.value = res.data.classes || []
      expOptions.value = res.data.experiments || []
      if (expOptions.value.length) formExpId.value = expOptions.value[0].id
      const ids = new Set()
      classOptions.value.slice(0, Math.min(2, classOptions.value.length)).forEach((c) => ids.add(c.id))
      selectedClassIds.value = ids
    }
  } catch (e) {
    console.warn('加载布置选项失败', e)
  } finally {
    optionsLoading.value = false
  }
}

async function submit() {
  if (submitting.value || !canSubmit.value) return
  submitting.value = true
  publishSuccess.value = false
  try {
    const exp = selectedExp.value
    const deadline = deadlineDate.value ? `${deadlineDate.value}T23:59:59` : undefined
    let ok = 0
    for (const cls of selectedClasses.value) {
      const res = await createTask({
        classOrgId: cls.id,
        className: cls.label,
        experimentId: exp.id,
        experimentTitle: exp.label,
        deadline,
        requirements: requirements.value || undefined,
        taskType: 'homework'
      })
      if (res?.code === 200) ok += 1
    }
    if (ok > 0) {
      publishSuccess.value = true
    } else {
      alert('发布失败，请稍后重试')
    }
  } catch (e) {
    alert(e?.response?.data?.message || '发布失败，请稍后重试')
  } finally {
    submitting.value = false
    initIcons()
  }
}

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

onMounted(async () => {
  deadlineDate.value = defaultDeadline()
  await loadOptions()
  initIcons()
})
</script>
