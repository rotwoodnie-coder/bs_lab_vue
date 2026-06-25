<template>
  <MobilePageShell class="theme-teacher safe-top" data-layout="teacher-form">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/home" />
        <h1 class="pad-workbench__title">📝 发布实验任务</h1>
      </header>

      <div class="pad-form__body">
        <div class="pad-form__main stack-4">
          <AssignOptionSections
            :options-loading="optionsLoading"
            :exp-options="expOptions"
            :class-options="classOptions"
            :form-exp-id="formExpId"
            :selected-class-ids="selectedClassIds"
            :deadline-date="deadlineDate"
            :requirements="requirements"
            @select-exp="selectExp"
            @toggle-class="toggleClass"
            @update:deadline-date="deadlineDate = $event"
            @update:requirements="requirements = $event"
          />
        </div>

        <aside class="pad-form__side stack-4">
          <div class="card card-pad surface-2 sticky-top">
            <h3 class="text-sm font-bold mb-3">发布摘要</h3>
            <div class="stack-3">
              <div>
                <p class="text-xs muted mb-1">实验</p>
                <span v-if="selectedExp" class="badge badge-info">🔬 {{ selectedExp.label }}</span>
                <span v-else class="text-xs muted">未选择</span>
              </div>
              <div>
                <p class="text-xs muted mb-1">班级</p>
                <span v-if="selectedClassCount" class="badge badge-success">{{ selectedClassCount }} 个班级 · {{ selectedStudentTotal }} 人</span>
                <span v-else class="text-xs muted">未选择</span>
              </div>
              <div>
                <p class="text-xs muted mb-1">截止</p>
                <span v-if="deadlineLabel" class="badge badge-warning">{{ deadlineLabel }}</span>
                <span v-else class="text-xs muted">未设置</span>
              </div>
            </div>
            <button
              type="button"
              class="btn btn-primary btn-block mt-4"
              :disabled="submitting || !canSubmit"
              @click="submit"
            >
              <i data-lucide="send" class="icon"></i>
              {{ submitting ? '发布中…' : '发布实验任务' }}
            </button>
            <div v-if="publishSuccess" class="card card-pad text-center tint-green mt-3">
              <div class="text-3xl mb-2">🎉</div>
              <div class="text-sm font-bold text-success">实验任务已发布</div>
              <div class="text-xs muted mt-1">已通知 {{ selectedStudentTotal }} 名学生</div>
              <router-link to="/home" class="btn btn-outline btn-sm mt-3">返回首页</router-link>
            </div>
          </div>
        </aside>
      </div>

      <div class="pad-form__mobile-only pb-28">
        <div class="topbar safe-top">
          <PageBackButton fallback="/home" />
          <h1 class="topbar-title flex-1">📝 发布实验任务</h1>
        </div>

        <div class="px-4 py-4 stack-4">
          <AssignOptionSections
            :options-loading="optionsLoading"
            :exp-options="expOptions"
            :class-options="classOptions"
            :form-exp-id="formExpId"
            :selected-class-ids="selectedClassIds"
            :deadline-date="deadlineDate"
            :requirements="requirements"
            @select-exp="selectExp"
            @toggle-class="toggleClass"
            @update:deadline-date="deadlineDate = $event"
            @update:requirements="requirements = $event"
          />

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
            {{ submitting ? '发布中…' : '发布实验任务' }}
          </button>

          <div v-if="publishSuccess" class="card card-pad text-center tint-green">
            <div class="text-3xl mb-2">🎉</div>
            <div class="text-sm font-bold text-success">实验任务已发布</div>
            <div class="text-xs muted mt-1">已通知 {{ selectedStudentTotal }} 名学生</div>
            <router-link to="/home" class="btn btn-outline btn-sm mt-3">返回首页</router-link>
          </div>
        </div>
      </div>
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAppStore } from '@/stores/app'
import { createTask } from '@/api/task'
import { fetchTeacherAssignOptions } from '@/api/teacher'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import AssignOptionSections from '@/components/teacher/AssignOptionSections.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { defaultDeadlineDate, deadlineDateToIsoEnd } from '@/utils/deadlineDate'

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
    console.warn('加载发布选项失败', e)
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
    const dateStr = deadlineDate.value || defaultDeadlineDate()
    const deadline = deadlineDateToIsoEnd(dateStr)
    let ok = 0
    for (const cls of selectedClasses.value) {
      const isSimulator = exp.sourceType === 'simulator'
      const payload = {
        classOrgId: cls.id,
        className: cls.label,
        experimentTitle: exp.label,
        deadline,
        requirements: requirements.value || undefined,
        taskType: isSimulator ? 'simulator' : 'homework'
      }
      if (isSimulator) {
        payload.simulatorId = exp.simulatorId || exp.id
      } else {
        payload.experimentId = exp.id
      }
      const res = await createTask(payload)
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

const { initIcons } = useLucideIcons()

onMounted(async () => {
  deadlineDate.value = defaultDeadlineDate()
  await loadOptions()
  initIcons()
})
</script>
