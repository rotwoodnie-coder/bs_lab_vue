import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { useUserStore } from './user'
import { normalizeRole } from '@/utils/role'

const STORAGE_PREFIX = 'bslab-chat4'
const MAX_MESSAGES = 50

/* ---------- localStorage helpers ---------- */
function lsKey(suffix) {
  return `${STORAGE_PREFIX}-${suffix}`
}

function lsSet(key, val) {
  try { localStorage.setItem(lsKey(key), JSON.stringify(val)) } catch { /* ignore */ }
}

function lsGet(key, fallback = null) {
  try {
    const raw = localStorage.getItem(lsKey(key))
    return raw ? JSON.parse(raw) : fallback
  } catch { return fallback }
}

function lsRemove(key) {
  try { localStorage.removeItem(lsKey(key)) } catch { /* ignore */ }
}

/* ---------- module defaults ---------- */
function createModuleState(mode) {
  return {
    threadId: '',
    messages: [],
    hasSent: false,
    // plan-specific
    experimentTitle: '',
    gradeLevel: '',
    plans: [],
    savedPlans: [],
    visionResult: null,
  }
}

/* ---------- welcome messages ---------- */
function welcomeForMode(mode, userName, userRole) {
  const callName = userRole === 'student' ? '同学' : userRole === 'parent' ? '家长' : '老师'
  const displayName = userName ? `${userName}${callName}` : callName
  const map = {
    free: `${displayName}，你好！我是 AI 科学小助手，你可以问我任何科学问题，比如"为什么天空是蓝色的？"、"怎样让纸飞机飞得更远？" 🚀`,
    pre: `${displayName}，你好！我是石头老师 😊 我们一起做实验前的准备吧。告诉我你们要做的实验，我一步步带你了解实验目的、材料、步骤和安全注意事项。`,
    plan: `${displayName}，你好！我是实验方案设计师 🎯 输入你想做的实验名称，选好年级段，我来为你自动生成 3 个有趣的实验方案，你可以修改、保存并导出成漂亮的 A4 文档！`,
    post: `${displayName}，实验做完了？太好了！我是实验分析助手 🔬 描述实验过程和现象，或上传实验照片并补充说明，我来帮你分析并生成可编辑的 A4 诊断报告。`,
  }
  return map[mode] || map.free
}

export const useChatStore = defineStore('chat', () => {
  const userStore = useUserStore()
  const userName = computed(() => userStore.userInfo.username || '用户')
  const userRoleId = computed(() => normalizeRole(userStore.userInfo.userRoleId))

  /* ---- current mode ---- */
  const currentMode = ref('free') // free | pre | plan | post

  /* ---- 4 module states ---- */
  const modes = reactive({
    free: createModuleState('free'),
    pre: createModuleState('pre'),
    plan: createModuleState('plan'),
    post: createModuleState('post'),
  })

  /* ---- computed convenience ---- */
  const current = computed(() => modes[currentMode.value])

  const currentMessages = computed(() => current.value.messages)
  const currentThreadId = computed(() => current.value.threadId)
  const currentHasSent = computed(() => current.value.hasSent)
  const currentPlans = computed(() => current.value.plans)
  const currentSavedPlans = computed(() => current.value.savedPlans)
  const currentVisionResult = computed(() => current.value.visionResult)
  const currentExperimentTitle = computed({
    get: () => current.value.experimentTitle,
    set: (v) => { current.value.experimentTitle = v; persistModule() }
  })
  const currentGradeLevel = computed({
    get: () => current.value.gradeLevel,
    set: (v) => { current.value.gradeLevel = v; persistModule() }
  })

  const welcomeMessage = computed(() => welcomeForMode(currentMode.value, userName.value, userRoleId.value))

  /* ---- derived role-based defaults ---- */
  const defaultMode = computed(() => {
    const role = userRoleId.value
    if (role === 'student') return 'pre'
    return 'free' // teacher / parent / admin
  })

  /* ---- mode helpers ---- */
  const modeLabelMap = {
    free: '对话',
    pre: '实验前',
    plan: '方案设计',
    post: '实验后',
  }

  const sortedModes = [
    { key: 'free', label: '对话' },
    { key: 'pre', label: '实验前' },
    { key: 'plan', label: '方案设计' },
    { key: 'post', label: '实验后' },
  ]

  function getModeLabel(mode) {
    return modeLabelMap[mode] || mode
  }

  /* ---- persistence ---- */
  function sanitizeMessageForStorage(msg) {
  const copy = { ...msg }
  if (copy.imageThumb) {
    copy.imageUrl = copy.imageThumb
  } else if (copy.imageUrl && copy.imageUrl.length > 80000) {
    delete copy.imageUrl
  }
  return copy
}

function persistModule() {
    const m = currentMode.value
    const state = modes[m]
    lsSet(`messages-${m}`, state.messages.slice(-MAX_MESSAGES).map(sanitizeMessageForStorage))
    lsSet(`thread-${m}`, state.threadId)
    lsSet(`hasSent-${m}`, state.hasSent)
    if (m === 'plan') {
      lsSet(`plans-${m}`, state.plans)
      lsSet(`savedPlans-${m}`, state.savedPlans)
    }
    if (m === 'post') {
      lsSet(`visionResult-${m}`, state.visionResult)
    }
  }

  function loadModule(mode) {
    const state = modes[mode]
    state.messages = lsGet(`messages-${mode}`, [])
    state.threadId = lsGet(`thread-${mode}`, '')
    state.hasSent = lsGet(`hasSent-${mode}`, false)
    if (mode === 'plan') {
      state.plans = lsGet(`plans-${mode}`, [])
      state.savedPlans = lsGet(`savedPlans-${mode}`, [])
    }
    if (mode === 'post') {
      state.visionResult = lsGet(`visionResult-${mode}`, null)
    }
  }

  function persistAll() {
    ;['free', 'pre', 'plan', 'post'].forEach((m) => {
      const state = modes[m]
      lsSet(`messages-${m}`, state.messages.slice(-MAX_MESSAGES).map(sanitizeMessageForStorage))
      lsSet(`thread-${m}`, state.threadId)
      lsSet(`hasSent-${m}`, state.hasSent)
      if (m === 'plan') {
        lsSet(`plans-${m}`, state.plans)
        lsSet(`savedPlans-${m}`, state.savedPlans)
      }
      if (m === 'post') {
        lsSet(`visionResult-${m}`, state.visionResult)
      }
    })
  }

  /* ---- switch mode ---- */
  function switchMode(mode) {
    if (!mode || mode === currentMode.value) return
    // persist current module
    persistModule()
    // switch
    currentMode.value = mode
    // load target module
    loadModule(mode)
  }

  /* ---- init from URL / default ---- */
  function initFromQuery(query) {
    const modeFromUrl = query?.mode
    if (['free', 'pre', 'plan', 'post'].includes(modeFromUrl)) {
      currentMode.value = modeFromUrl
    } else {
      currentMode.value = defaultMode.value
    }
    // load all persisted modules
    ;['free', 'pre', 'plan', 'post'].forEach(loadModule)
  }

  /* ---- message helpers ---- */
  function addMessage(mode, role, content, extra = {}) {
    const state = modes[mode]
    state.messages.push({ role, content, ...extra })
    if (!state.hasSent && role === 'user') state.hasSent = true
    if (state.messages.length > MAX_MESSAGES) {
      state.messages = state.messages.slice(-MAX_MESSAGES)
    }
    persistModule()
  }

  function setThreadId(mode, id) {
    modes[mode].threadId = id
    persistModule()
  }

  function clearModule(mode) {
    const state = modes[mode]
    state.messages = []
    state.threadId = ''
    state.hasSent = false
    if (mode === 'plan') {
      state.plans = []
      state.savedPlans = []
    }
    if (mode === 'post') {
      state.visionResult = null
    }
    lsRemove(`messages-${mode}`)
    lsRemove(`thread-${mode}`)
    lsRemove(`hasSent-${mode}`)
    if (mode === 'plan') {
      lsRemove(`plans-${mode}`)
      lsRemove(`savedPlans-${mode}`)
    }
    if (mode === 'post') {
      lsRemove(`visionResult-${mode}`)
    }
  }

  /* ---- plan-specific ---- */
  function setPlans(plans) {
    modes.plan.plans = plans
    persistModule()
  }

  function savePlan(plan) {
    const saved = modes.plan.savedPlans
    const idx = saved.findIndex((p) => p.id === plan.id)
    if (idx >= 0) {
      saved[idx] = { ...saved[idx], ...plan }
    } else {
      saved.push({ ...plan, savedAt: Date.now() })
    }
    persistModule()
  }

  function removeSavedPlan(planId) {
    modes.plan.savedPlans = modes.plan.savedPlans.filter((p) => p.id !== planId)
    persistModule()
  }

  /* ---- vision-specific ---- */
  function setVisionResult(result) {
    modes.post.visionResult = result
    persistModule()
  }

  return {
    // state
    currentMode,
    modes,
    // computed
    current,
    currentMessages,
    currentThreadId,
    currentHasSent,
    currentPlans,
    currentSavedPlans,
    currentVisionResult,
    currentExperimentTitle,
    currentGradeLevel,
    welcomeMessage,
    defaultMode,
    sortedModes,
    // methods
    getModeLabel,
    switchMode,
    initFromQuery,
    addMessage,
    setThreadId,
    clearModule,
    setPlans,
    savePlan,
    removeSavedPlan,
    setVisionResult,
    persistAll,
  }
})
