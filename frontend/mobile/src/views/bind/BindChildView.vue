<template>
  <div class="screen theme-student pad-bind" data-layout="bind-columns">
    <div class="screen-body stack-4 px-5">
      <div class="text-center safe-top">
        <div class="tile-icon tile-lg tile-grad mx-auto mb-3">
          <i data-lucide="user-plus" class="icon icon-lg"></i>
        </div>
        <h1 class="text-xl font-bold">绑定孩子</h1>
        <p class="text-xs muted mt-1">{{ padColumns ? '请依次选择学校、年级、班级，并填写姓名' : stepHint }}</p>
      </div>

      <div v-if="breadcrumbs.length" class="breadcrumb-bar">
        <template v-for="(chip, i) in breadcrumbs" :key="chip.key">
          <span v-if="i > 0" class="breadcrumb-sep">›</span>
          <button type="button" class="breadcrumb-chip" @click="goToStep(chip.step)">
            <i :data-lucide="chip.icon" class="icon chip-icon"></i>
            <span>{{ chip.label }}</span>
          </button>
        </template>
      </div>

      <div class="stepper mb-3">
        <template v-for="(dot, i) in indicatorDots" :key="dot.step">
          <span class="stepper-dot" :class="dot.status">{{ dot.label }}</span>
          <span v-if="i < indicatorDots.length - 1" class="stepper-line" :class="lineDone(i + 1) ? 'done' : 'pending'"></span>
        </template>
      </div>

      <div class="bind-layout flex-1" style="min-height:280px;">
        <div class="bind-columns-wrap">
          <div
            v-show="showColPanel(1)"
            class="step-panel bind-col"
            :class="{ active: showColPanel(1) }"
          >
            <h2 class="bind-col-head">选择学校</h2>
            <div class="stack bind-col-list">
              <button
                v-for="item in schools"
                :key="item.orgId"
                type="button"
                class="option-card row items-center justify-between w-full text-left"
                :class="{ selected: selected.schoolOrgId === item.orgId }"
                @click="pickSchool(item)"
              >
                <span class="row items-center gap-3">
                  <i data-lucide="school" class="icon opt-icon"></i>
                  <span class="text-sm font-semibold">{{ item.orgName }}</span>
                </span>
                <span class="option-check">✓</span>
              </button>
            </div>
          </div>

          <div
            v-show="showColPanel(2)"
            class="step-panel bind-col"
            :class="{ active: showColPanel(2) }"
          >
            <h2 class="bind-col-head">选择年级</h2>
            <div class="stack bind-col-list">
              <button
                v-for="item in grades"
                :key="item.orgId"
                type="button"
                class="option-card row items-center justify-between w-full text-left"
                :class="{ selected: selected.gradeOrgId === item.orgId }"
                @click="pickGrade(item)"
              >
                <span class="row items-center gap-3">
                  <i data-lucide="book-open" class="icon opt-icon"></i>
                  <span class="text-sm font-semibold">{{ item.orgName }}</span>
                </span>
                <span class="option-check">✓</span>
              </button>
            </div>
          </div>

          <div
            v-show="showColPanel(3)"
            class="step-panel bind-col"
            :class="{ active: showColPanel(3) }"
          >
            <h2 class="bind-col-head">选择班级</h2>
            <div class="stack bind-col-list">
              <button
                v-for="item in classes"
                :key="item.orgId"
                type="button"
                class="option-card row items-center justify-between w-full text-left"
                :class="{ selected: selected.classOrgId === item.orgId }"
                @click="pickClass(item)"
              >
                <span class="row items-center gap-3">
                  <i data-lucide="users" class="icon opt-icon"></i>
                  <span class="text-sm font-semibold">{{ item.orgName }}</span>
                </span>
                <span class="option-check">✓</span>
              </button>
            </div>
          </div>
        </div>

        <div
          v-show="showConfirmPanel"
          class="step-panel bind-confirm"
          :class="{ active: showConfirmPanel }"
        >
          <p class="text-xs muted mb-3 bind-confirm-hint">请确认信息，输入孩子姓名后完成绑定</p>
          <div class="confirm-summary mb-4">
            <div class="confirm-row">
              <div class="cr-icon"><i data-lucide="school" class="icon cr-svg"></i></div>
              <div>
                <div class="cr-label">学校</div>
                <div class="cr-value">{{ selected.schoolName || '—' }}</div>
              </div>
            </div>
            <div class="confirm-row">
              <div class="cr-icon"><i data-lucide="book-open" class="icon cr-svg"></i></div>
              <div>
                <div class="cr-label">年级</div>
                <div class="cr-value">{{ selected.gradeName || '—' }}</div>
              </div>
            </div>
            <div class="confirm-row">
              <div class="cr-icon"><i data-lucide="users" class="icon cr-svg"></i></div>
              <div>
                <div class="cr-label">班级</div>
                <div class="cr-value">{{ selected.className || '—' }}</div>
              </div>
            </div>
          </div>
          <div class="mb-4">
            <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">孩子姓名</label>
            <input v-model="childName" type="text" placeholder="请输入孩子姓名" maxlength="20" class="input text-sm font-medium" @input="onChildNameInput" />
          </div>
          <div v-if="searchError && isNameStep" class="student-match-error mb-3">
            <p class="text-sm">{{ searchError }}</p>
          </div>
          <div v-if="searchError && isNameStep" class="mb-4">
            <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">学号（选填，用于区分同名）</label>
            <input v-model="studentNo" type="text" placeholder="请输入学号" maxlength="32" class="input text-sm font-medium" />
          </div>
          <div v-if="padColumns && (selectedStudent || candidates.length > 1 || searching)" class="mt-4">
            <StudentMatchConfirm
              :searching="searching"
              :search-error="searchError"
              :candidates="candidates"
              :selected="selectedStudent"
              :student-no="studentNo"
              :show-student-no="!!searchError"
              :grade-name="selected.gradeName"
              @pick="onPickStudent"
              @update:student-no="studentNo = $event"
            />
          </div>
          <div v-if="!padColumns || !selectedStudent" class="card card-pad row items-start gap-3 surface-2" style="border:none;">
            <i data-lucide="lock" class="icon text-lg shrink-0" style="color:var(--brand);"></i>
            <p class="text-xs leading-relaxed muted">
              绑定后，您将可以查看孩子的作业、作品和成长记录。一个家长账号最多可绑定 <strong>3 个孩子</strong>。
            </p>
          </div>
        </div>

        <div
          v-show="showStudentStep"
          class="step-panel bind-confirm"
          :class="{ active: showStudentStep }"
        >
          <StudentMatchConfirm
            :searching="searching"
            :search-error="searchError"
            :candidates="candidates"
            :selected="selectedStudent"
            :student-no="studentNo"
            :show-student-no="!!searchError"
            :grade-name="selected.gradeName"
            @pick="onPickStudent"
            @update:student-no="studentNo = $event"
          />
          <div class="card card-pad row items-start gap-3 surface-2 mt-4" style="border:none;">
            <i data-lucide="lock" class="icon text-lg shrink-0" style="color:var(--brand);"></i>
            <p class="text-xs leading-relaxed muted">
              绑定后，您将可以查看孩子的作业、作品和成长记录。一个家长账号最多可绑定 <strong>3 个孩子</strong>。
            </p>
          </div>
        </div>
      </div>

      <div class="bottom-bar">
        <div class="flex gap-3">
          <button
            v-show="!padColumns"
            type="button"
            class="btn btn-outline btn-block btn-lg flex-1"
            :disabled="currentStep === 1 || submitting"
            @click="goToStep(currentStep - 1)"
          >上一步</button>
          <button
            type="button"
            class="btn btn-gradient btn-block btn-lg flex-1"
            :disabled="!canNext || submitting"
            @click="onNext"
          >{{ nextLabel }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchSchools, fetchGrades, fetchClasses } from '@/api/org'
import { bindChild } from '@/api/parent'
import { apiMessage } from '@/utils/apiError'
import { buildBindSuccessQuery, persistBindSuccessSnapshot } from '@/utils/bindSuccessNav'
import { usePrototypeStepper } from '@/composables/usePrototypeStepper'
import { useStudentMatch } from '@/composables/useStudentMatch'
import StudentMatchConfirm from '@/components/parent/StudentMatchConfirm.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'

const TOTAL = 5
const STEP_LABELS = ['选择学校', '选择年级', '选择班级', '填写姓名', '确认学生']
const STEP_ICONS = ['school', 'book-open', 'users']

const router = useRouter()
const submitting = ref(false)
const padColumns = ref(false)
const schools = ref([])
const grades = ref([])
const classes = ref([])
const childName = ref('')

const {
  searching,
  searchError,
  candidates,
  selectedStudent,
  studentNo,
  resetStudentMatch,
  pickStudent,
  lookupStudent
} = useStudentMatch()

const selected = reactive({
  schoolOrgId: '',
  schoolName: '',
  gradeOrgId: '',
  gradeName: '',
  classOrgId: '',
  className: ''
})

const {
  currentStep,
  advancing,
  stepHint,
  indicatorDots,
  breadcrumbs,
  goToStep,
  autoAdvanceAfterSelect
} = usePrototypeStepper(TOTAL, STEP_LABELS, () => {
  const chips = []
  if (selected.schoolName) chips.push({ key: 'school', icon: STEP_ICONS[0], label: selected.schoolName, step: 1 })
  if (selected.gradeName) chips.push({ key: 'grade', icon: STEP_ICONS[1], label: selected.gradeName, step: 2 })
  if (selected.className) chips.push({ key: 'class', icon: STEP_ICONS[2], label: selected.className, step: 3 })
  return chips
})

const showConfirmPanel = computed(() => padColumns.value || currentStep.value >= 4)
const isNameStep = computed(() => (padColumns.value ? currentStep.value >= 4 : currentStep.value === 4))
const showStudentStep = computed(() => !padColumns.value && currentStep.value === 5)

function showColPanel(n) {
  if (padColumns.value) return true
  return currentStep.value === n
}

function lineDone(stepNum) {
  if (padColumns.value) {
    if (stepNum < 5) return stepDone(stepNum)
    return stepDone(5)
  }
  return stepNum < currentStep.value
}

function stepDone(stepNum) {
  if (stepNum === 1) return !!selected.schoolOrgId
  if (stepNum === 2) return !!selected.gradeOrgId
  if (stepNum === 3) return !!selected.classOrgId
  if (stepNum === 4) return !!(selected.schoolOrgId && selected.gradeOrgId && selected.classOrgId && childName.value.trim())
  if (stepNum === 5) return !!(selectedStudent.value)
  return false
}

const canNext = computed(() => {
  if (padColumns.value) {
    if (!selected.schoolOrgId || !selected.gradeOrgId || !selected.classOrgId || !childName.value.trim()) {
      return false
    }
    if (selectedStudent.value) return true
    return !searching.value
  }
  switch (currentStep.value) {
    case 1: return !!selected.schoolOrgId
    case 2: return !!selected.gradeOrgId
    case 3: return !!selected.classOrgId
    case 4: return !!childName.value.trim() && !searching.value
    case 5: return !!selectedStudent.value
    default: return false
  }
})

const nextLabel = computed(() => {
  if (submitting.value) return '提交中…'
  if (searching.value) return '查询中…'
  if (padColumns.value) {
    return selectedStudent.value ? '确认绑定' : '查找学生'
  }
  if (currentStep.value === 4) return '查找学生'
  if (currentStep.value === 5) return '确认绑定'
  return '下一步'
})

function onChildNameInput() {
  if (isNameStep.value) resetStudentMatch()
}

function onPickStudent(item) {
  pickStudent(item)
  initIcons()
}

function updatePadColumns() {
  padColumns.value = window.matchMedia('(min-width: 768px) and (orientation: landscape)').matches
}

async function loadSchools() {
  const res = await fetchSchools()
  schools.value = res?.code === 200 ? (res.data || []) : []
}

async function pickSchool(item) {
  if (advancing.value) return
  selected.schoolOrgId = item.orgId
  selected.schoolName = item.orgName
  selected.gradeOrgId = ''
  selected.gradeName = ''
  selected.classOrgId = ''
  selected.className = ''
  grades.value = []
  classes.value = []
  const res = await fetchGrades(item.orgId)
  grades.value = res?.code === 200 ? (res.data || []) : []
  if (!padColumns.value && currentStep.value === 1) autoAdvanceAfterSelect(initIcons)
}

async function pickGrade(item) {
  if (advancing.value) return
  selected.gradeOrgId = item.orgId
  selected.gradeName = item.orgName
  selected.classOrgId = ''
  selected.className = ''
  classes.value = []
  const res = await fetchClasses(item.orgId)
  classes.value = res?.code === 200 ? (res.data || []) : []
  if (!padColumns.value && currentStep.value === 2) autoAdvanceAfterSelect(initIcons)
}

function pickClass(item) {
  if (advancing.value) return
  selected.classOrgId = item.orgId
  selected.className = item.orgName
  if (!padColumns.value && currentStep.value === 3) autoAdvanceAfterSelect(initIcons)
}

function onNext() {
  if (!canNext.value || submitting.value || advancing.value || searching.value) return

  const onNameStep = padColumns.value
    ? (selected.schoolOrgId && selected.gradeOrgId && selected.classOrgId && childName.value.trim() && !selectedStudent.value)
    : currentStep.value === 4

  if (onNameStep) {
    lookupStudent(selected.classOrgId, childName.value).then((ok) => {
      if (ok && !padColumns.value) {
        goToStep(5)
        initIcons()
      } else if (ok) {
        initIcons()
      }
    })
    return
  }

  if (padColumns.value && selectedStudent.value) {
    submitBind()
    return
  }

  if (!padColumns.value && currentStep.value < TOTAL) {
    goToStep(currentStep.value + 1)
    initIcons()
    return
  }

  submitBind()
}

function submitBind() {
  if (!selectedStudent.value) return
  submitting.value = true
  bindChild({
    classOrgId: selected.classOrgId,
    childName: childName.value.trim(),
    childUserId: selectedStudent.value.userId,
    studentNo: studentNo.value.trim() || undefined
  })
    .then((res) => {
      if (res?.code === 200) {
        const data = res.data || {}
        const snapshot = {
          bindId: data.bindId,
          childName: data.childName || childName.value.trim(),
          schoolName: data.schoolName || selected.schoolName,
          gradeName: data.gradeName || selected.gradeName,
          className: data.className || selected.className,
          relation: data.relation || '',
          bindStatus: data.bindStatus || 'pending',
          message: data.message || '绑定申请已提交，等待学校审核'
        }
        persistBindSuccessSnapshot(snapshot)
        router.push({
          path: '/bind-success',
          query: buildBindSuccessQuery(snapshot, 'bind')
        })
        return
      }
      alert(res?.message || '绑定失败')
    })
    .catch((e) => alert(apiMessage(e, '绑定失败')))
    .finally(() => { submitting.value = false })
}

const { initIcons } = useLucideIcons()

watch(currentStep, (step, prev) => {
  initIcons()
  if (step === 4 && prev === 5) resetStudentMatch()
})

onMounted(() => {
  updatePadColumns()
  window.addEventListener('resize', updatePadColumns)
  loadSchools()
  initIcons()
})

onUnmounted(() => {
  window.removeEventListener('resize', updatePadColumns)
})
</script>
