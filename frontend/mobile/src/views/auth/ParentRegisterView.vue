<template>
  <div class="screen theme-parent pad-bind" data-layout="register-flow">
    <div class="screen-body stack-4 px-5">
      <div class="text-center safe-top">
        <div class="tile-icon tile-lg tile-grad mx-auto mb-3">
          <i data-lucide="user-plus" class="icon icon-lg"></i>
        </div>
        <h1 class="text-xl font-bold">家长自助注册</h1>
        <p class="text-xs muted mt-1">{{ stepHint }}</p>
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
          <span v-if="i < indicatorDots.length - 1" class="stepper-line" :class="i + 1 < currentStep ? 'done' : 'pending'"></span>
        </template>
      </div>

      <div class="flex-1" style="min-height:260px;">
        <p v-if="submitError && currentStep === 6" class="text-sm text-danger mb-3">{{ submitError }}</p>

        <!-- 第 1 步：家长信息（P26 · 无验证码，见产品规格差异） -->
        <div v-show="currentStep === 1" class="step-panel" :class="{ active: currentStep === 1 }">
          <p v-if="step1Attempted && !isStep1Valid" class="text-sm text-danger mb-3">请完善标红项后再继续</p>
          <div class="stack-4">
            <div>
              <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">家长姓名</label>
              <div class="input-group" data-input-tools="off" :class="{ 'has-field-error': showError('parentName') }">
                <i data-lucide="user" class="icon"></i>
                <input v-model="form.parentName" class="input" type="text" placeholder="请输入家长姓名" maxlength="20" autocomplete="name" @input="syncBreadcrumb" @blur="touchField('parentName')" />
              </div>
              <p v-if="showError('parentName')" class="field-error">{{ fieldErrors.parentName }}</p>
            </div>
            <div>
              <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">手机号</label>
              <div class="input-group" data-input-tools="off" :class="{ 'has-field-error': showError('phone') }">
                <i data-lucide="phone" class="icon"></i>
                <input v-model="form.phone" class="input" type="tel" placeholder="请输入手机号" maxlength="11" autocomplete="tel" @blur="onPhoneBlur" />
              </div>
              <p v-if="showError('phone')" class="field-error">{{ fieldErrors.phone }}</p>
            </div>
            <div>
              <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">设置密码</label>
              <div class="input-group" data-input-tools="off" :class="{ 'has-field-error': showError('password') }">
                <i data-lucide="lock" class="icon"></i>
                <input v-model="form.password" class="input" type="password" :placeholder="passwordHint" autocomplete="new-password" @blur="touchField('password')" @input="touchField('password')" />
              </div>
              <p v-if="showError('password')" class="field-error">{{ fieldErrors.password }}</p>
              <p v-else class="field-hint">{{ passwordHint }}</p>
            </div>
            <div>
              <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">确认密码</label>
              <div class="input-group" data-input-tools="off" :class="{ 'has-field-error': showError('confirmPassword') }">
                <i data-lucide="lock-keyhole" class="icon"></i>
                <input v-model="form.confirmPassword" class="input" type="password" placeholder="请再次输入密码" autocomplete="new-password" @blur="touchField('confirmPassword')" @input="touchField('confirmPassword')" />
              </div>
              <p v-if="showError('confirmPassword')" class="field-error">{{ fieldErrors.confirmPassword }}</p>
            </div>
            <div>
              <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">与孩子的关系</label>
              <div class="relation-group">
                <button
                  v-for="r in relations"
                  :key="r"
                  type="button"
                  class="relation-btn"
                  :class="{ selected: form.relation === r }"
                  @click="form.relation = r; touchField('relation')"
                >{{ r }}</button>
              </div>
              <p v-if="showError('relation')" class="field-error">{{ fieldErrors.relation }}</p>
            </div>
          </div>
        </div>

        <!-- 第 2 步：学校 -->
        <div v-show="currentStep === 2" class="step-panel" :class="{ active: currentStep === 2 }">
          <h2 class="text-sm font-bold mb-2">选择孩子所在学校</h2>
          <div class="stack">
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

        <!-- 第 3 步：年级 -->
        <div v-show="currentStep === 3" class="step-panel" :class="{ active: currentStep === 3 }">
          <h2 class="text-sm font-bold mb-2">选择孩子所在年级</h2>
          <div class="stack">
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

        <!-- 第 4 步：班级 -->
        <div v-show="currentStep === 4" class="step-panel" :class="{ active: currentStep === 4 }">
          <h2 class="text-sm font-bold mb-2">选择孩子所在班级</h2>
          <div class="stack">
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

        <!-- 第 5 步：确认提交（P26） -->
        <div v-show="currentStep === 5" class="step-panel" :class="{ active: currentStep === 5 }">
          <p class="text-xs muted mb-3">请确认以下信息，确认无误后提交审核</p>
          <div class="confirm-summary mb-4">
            <div class="text-xs font-semibold muted-2 mb-2">家长信息</div>
            <div class="confirm-row">
              <div class="cr-icon"><i data-lucide="user" class="icon cr-svg"></i></div>
              <div>
                <div class="cr-label">家长姓名</div>
                <div class="cr-value">{{ form.parentName || '—' }}</div>
              </div>
            </div>
            <div class="confirm-row">
              <div class="cr-icon"><i data-lucide="phone" class="icon cr-svg"></i></div>
              <div>
                <div class="cr-label">手机号</div>
                <div class="cr-value">{{ form.phone || '—' }}</div>
              </div>
            </div>
            <div class="confirm-row">
              <div class="cr-icon"><i data-lucide="heart-handshake" class="icon cr-svg"></i></div>
              <div>
                <div class="cr-label">关系</div>
                <div class="cr-value">{{ form.relation || '—' }}</div>
              </div>
            </div>
            <div class="divider my-2"></div>
            <div class="text-xs font-semibold muted-2 mb-2">绑定学生</div>
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
            <input v-model="form.childName" type="text" placeholder="请输入孩子姓名" maxlength="20" class="input text-sm font-medium" @input="onChildNameInput" />
          </div>
          <div v-if="searchError && currentStep === 5" class="student-match-error mb-3">
            <p class="text-sm">{{ searchError }}</p>
          </div>
          <div v-if="searchError && currentStep === 5" class="mb-4">
            <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">学号（选填，用于区分同名）</label>
            <input v-model="studentNo" type="text" placeholder="请输入学号" maxlength="32" class="input text-sm font-medium" />
          </div>
        </div>

        <!-- 第 6 步：确认学生（查询后） -->
        <div v-show="currentStep === 6" class="step-panel" :class="{ active: currentStep === 6 }">
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
          <div class="security-notice mb-3">
            <span>📌</span>
            <span>注册信息提交后需等待学校审核，审核通过后可登录家长端。审核通过前暂无法使用家长端功能。</span>
          </div>
          <div class="card card-pad row items-start gap-3 surface-2" style="border:none;">
            <i data-lucide="lock" class="icon text-lg shrink-0" style="color:var(--brand);"></i>
            <p class="text-xs leading-relaxed muted">
              提交即表示同意 <a href="#" class="text-brand">《用户协议》</a> 和 <a href="#" class="text-brand">《隐私政策》</a>。
              一个家长账号最多可绑定 <strong>3 个孩子</strong>。
            </p>
          </div>
        </div>
      </div>

      <div class="bottom-bar">
        <div class="flex gap-3">
          <button type="button" class="btn btn-outline btn-block btn-lg flex-1" :disabled="currentStep === 1 || submitting" @click="goToStep(currentStep - 1)">上一步</button>
          <button type="button" class="btn btn-gradient btn-block btn-lg flex-1" :disabled="nextDisabled" @click="onNext">
            {{ nextButtonLabel }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchSchools, fetchGrades, fetchClasses } from '@/api/org'
import { registerParent, checkLoginNameAvailable, login as loginApi } from '@/api/auth'
import { usePrototypeStepper } from '@/composables/usePrototypeStepper'
import { useStudentMatch } from '@/composables/useStudentMatch'
import { useUserStore } from '@/stores/user'
import { apiMessage, mapFieldErrors } from '@/utils/apiError'
import { PASSWORD_HINT, validatePassword } from '@/utils/passwordRules'
import { buildBindSuccessQuery, persistBindSuccessSnapshot } from '@/utils/bindSuccessNav'
import StudentMatchConfirm from '@/components/parent/StudentMatchConfirm.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'

const REGISTER_FIELD_MAP = {
  loginName: 'phone',
  userPhone: 'phone',
  nickname: 'parentName',
  password: 'password',
  relation: 'relation'
}

const TOTAL = 6
const STEP_LABELS = ['填写家长信息', '选择学校', '选择年级', '选择班级', '填写姓名', '确认学生']
const relations = ['爸爸', '妈妈', '其他']
const passwordHint = PASSWORD_HINT

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const submitError = ref('')
const step1Attempted = ref(false)
const phoneChecking = ref(false)
const schools = ref([])
const grades = ref([])
const classes = ref([])

const fieldErrors = reactive({
  parentName: '',
  phone: '',
  password: '',
  confirmPassword: '',
  relation: ''
})

const touched = reactive({
  parentName: false,
  phone: false,
  password: false,
  confirmPassword: false,
  relation: false
})

const form = reactive({
  parentName: '',
  phone: '',
  password: '',
  confirmPassword: '',
  relation: '',
  childName: ''
})

const selected = reactive({
  schoolOrgId: '',
  schoolName: '',
  gradeOrgId: '',
  gradeName: '',
  classOrgId: '',
  className: ''
})

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
  if (form.parentName) {
    chips.push({
      key: 'name',
      icon: 'user',
      label: form.parentName.length > 4 ? form.parentName.slice(0, 4) + '..' : form.parentName,
      step: 1
    })
  }
  if (selected.schoolName) chips.push({ key: 'school', icon: 'school', label: selected.schoolName, step: 2 })
  if (selected.gradeName) chips.push({ key: 'grade', icon: 'book-open', label: selected.gradeName, step: 3 })
  if (selected.className) chips.push({ key: 'class', icon: 'users', label: selected.className, step: 4 })
  return chips
})

const canNext = computed(() => {
  switch (currentStep.value) {
    case 1:
      return isStep1Valid()
    case 2: return !!selected.schoolOrgId
    case 3: return !!selected.gradeOrgId
    case 4: return !!selected.classOrgId
    case 5: return !!form.childName.trim()
    case 6: return !!selectedStudent.value
    default: return false
  }
})

const nextDisabled = computed(() => {
  if (submitting.value || searching.value || phoneChecking.value) return true
  if (currentStep.value === 1) return false
  return !canNext.value
})

function isStep1Valid() {
  return Object.keys(validateStep1Fields()).length === 0
}

function validateStep1Fields() {
  const errors = {}
  const name = form.parentName.trim()
  if (!name) errors.parentName = '请输入家长姓名'
  else if (name.length < 2) errors.parentName = '姓名至少 2 个字符'

  const phone = form.phone.trim()
  if (!phone) errors.phone = '请输入手机号'
  else if (!/^1\d{10}$/.test(phone)) errors.phone = '手机号格式不正确'

  const pwdErr = validatePassword(form.password)
  if (pwdErr) errors.password = pwdErr
  else if (!form.confirmPassword) errors.confirmPassword = '请再次输入密码'
  else if (form.password !== form.confirmPassword) errors.confirmPassword = '两次输入的密码不一致'

  if (!form.relation) errors.relation = '请选择与孩子的关系'
  return errors
}

function syncFieldErrorsFromValidation() {
  const errors = validateStep1Fields()
  fieldErrors.parentName = errors.parentName || ''
  fieldErrors.phone = errors.phone || ''
  fieldErrors.password = errors.password || ''
  fieldErrors.confirmPassword = errors.confirmPassword || ''
  fieldErrors.relation = errors.relation || ''
}

function touchField(key) {
  touched[key] = true
  syncFieldErrorsFromValidation()
}

function showError(key) {
  return (touched[key] || step1Attempted.value) && fieldErrors[key]
}

function applyServerFieldErrors(errors) {
  const mapped = mapFieldErrors(errors, REGISTER_FIELD_MAP)
  Object.keys(mapped).forEach((key) => {
    if (key in fieldErrors) {
      fieldErrors[key] = mapped[key]
      touched[key] = true
    }
  })
  if (mapped.phone || mapped.parentName || mapped.password || mapped.relation) {
    step1Attempted.value = true
    goToStep(1)
  }
}

async function onPhoneBlur() {
  touchField('phone')
  const phone = form.phone.trim()
  if (fieldErrors.phone || !/^1\d{10}$/.test(phone)) return

  phoneChecking.value = true
  try {
    const res = await checkLoginNameAvailable(phone)
    if (res?.code === 200 && res.data && !res.data.available) {
      fieldErrors.phone = res.data.message || '该手机号不可用'
      touched.phone = true
    }
  } catch (e) {
    fieldErrors.phone = apiMessage(e, '手机号校验失败')
    touched.phone = true
  } finally {
    phoneChecking.value = false
  }
}

const nextButtonLabel = computed(() => {
  if (submitting.value) return '提交中…'
  if (searching.value) return '查询中…'
  if (phoneChecking.value) return '校验中…'
  if (currentStep.value === 5) return '查找学生'
  if (currentStep.value === 6) return '提交审核'
  return '下一步'
})

function onChildNameInput() {
  if (currentStep.value === 5) {
    resetStudentMatch()
  }
}

function onPickStudent(item) {
  pickStudent(item)
  initIcons()
}

function syncBreadcrumb() { /* triggers computed breadcrumbs */ }

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
  if (currentStep.value === 2) autoAdvanceAfterSelect(initIcons)
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
  if (currentStep.value === 3) autoAdvanceAfterSelect(initIcons)
}

function pickClass(item) {
  if (advancing.value) return
  selected.classOrgId = item.orgId
  selected.className = item.orgName
  if (currentStep.value === 4) autoAdvanceAfterSelect(initIcons)
}

function onNext() {
  if (submitting.value || advancing.value || searching.value || phoneChecking.value) return

  if (currentStep.value === 1) {
    step1Attempted.value = true
    syncFieldErrorsFromValidation()
    if (!isStep1Valid()) return
    goToStep(2)
    initIcons()
    return
  }

  if (!canNext.value) return
  if (currentStep.value === 5) {
    lookupStudent(selected.classOrgId, form.childName).then((ok) => {
      if (ok) {
        goToStep(6)
        initIcons()
      }
    })
    return
  }
  if (currentStep.value < TOTAL) {
    goToStep(currentStep.value + 1)
    initIcons()
    return
  }
  submitRegister()
}

async function submitRegister() {
  if (!selectedStudent.value) return
  submitting.value = true
  submitError.value = ''
  try {
    const res = await registerParent({
      loginName: form.phone.trim(),
      password: form.password,
      nickname: form.parentName.trim(),
      userPhone: form.phone.trim(),
      relation: form.relation,
      classOrgId: selected.classOrgId,
      childName: form.childName.trim(),
      childUserId: selectedStudent.value.userId,
      studentNo: studentNo.value.trim() || undefined
    })
    if (res?.code === 200) {
      const data = res.data || {}
      const snapshot = {
        bindId: data.bindId,
        childName: data.childName || form.childName.trim(),
        schoolName: data.schoolName || selected.schoolName,
        gradeName: data.gradeName || selected.gradeName,
        className: data.className || selected.className,
        relation: form.relation,
        bindStatus: data.bindStatus || 'pending',
        message: data.message || '注册申请已提交，账号与孩子绑定均待学校审核',
        phone: form.phone.trim()
      }
      persistBindSuccessSnapshot(snapshot)
      const successQuery = buildBindSuccessQuery(snapshot, 'register')
      try {
        const loginRes = await loginApi({
          loginName: form.phone.trim(),
          loginPwd: form.password
        })
        if (loginRes?.code === 200) {
          userStore.applyTokenPayload(loginRes.data)
          successQuery.autoLogin = '1'
        }
      } catch {
        successQuery.autoLogin = '0'
      }
      router.push({ path: '/bind-success', query: successQuery })
      return
    }
    if (Array.isArray(res?.data)) {
      applyServerFieldErrors(res.data)
    }
    submitError.value = res?.message || '提交失败'
  } catch (e) {
    const serverErrors = e?.response?.data?.data
    if (Array.isArray(serverErrors)) {
      applyServerFieldErrors(serverErrors)
    }
    submitError.value = apiMessage(e, '提交失败')
  } finally {
    submitting.value = false
  }
}

const { initIcons } = useLucideIcons()

watch(currentStep, (step, prev) => {
  initIcons()
  if (step === 5 && prev === 6) {
    resetStudentMatch()
  }
})

onMounted(() => {
  loadSchools()
  initIcons()
})
</script>

<style scoped>
.field-error {
  margin-top: 6px;
  font-size: var(--text-xs);
  color: var(--color-danger);
  line-height: 1.4;
}
.field-hint {
  margin-top: 6px;
  font-size: var(--text-xs);
  color: var(--color-text-3);
  line-height: 1.4;
}
.input-group.has-field-error > .input {
  border-color: var(--color-danger);
}
.input-group.has-field-error > .icon {
  color: var(--color-danger);
}
</style>
