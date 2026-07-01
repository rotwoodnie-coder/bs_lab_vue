<template>
  <MobilePageShell class="safe-top" data-layout="assistant-chat" data-assistant-chat>
    <div class="pad-main pad-chat">
      <!-- ===== Header ===== -->
      <header class="pad-chat__topbar safe-top">
        <PageBackButton fallback="/home" />
        <h1 class="pad-chat__topbar-title flex-1 min-w-0">AI助手</h1>
        <!-- Mode dropdown (replaces tabs to save space) -->
        <div class="mode-dropdown">
          <select
            :value="chatStore.currentMode"
            class="mode-select"
            @change="switchMode($event.target.value)"
          >
            <option
              v-for="m in chatStore.sortedModes"
              :key="m.key"
              :value="m.key"
            >{{ m.label }}</option>
          </select>
        </div>
        <button type="button" class="icon-btn" aria-label="更多" @click="showMenu = !showMenu">
          <i data-lucide="more-vertical" class="icon"></i>
        </button>
      </header>

      <!-- ===== Dropdown menu ===== -->
      <div v-if="showMenu" class="chat-menu-overlay" @click="showMenu = false">
        <div class="chat-menu" @click.stop>
          <button class="chat-menu__item" @click="clearConversation">
            <i data-lucide="trash-2" class="icon"></i> 清除对话
          </button>
        </div>
      </div>

      <!-- ===== Messages area ===== -->
      <div class="pad-chat__messages stack-4" ref="messagesRef" data-assistant-messages>
        <!-- Welcome -->
        <div v-if="!chatStore.currentHasSent" class="welcome-section">
          <div class="assistant-msg assistant-msg--ai row items-start gap-3">
            <div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">{{ aiAvatar }}</div>
            <div class="msg-bubble msg-ai">
              <div class="text-xs font-bold text-brand mb-1">{{ aiName }} · {{ modeLabel }}</div>
              {{ chatStore.welcomeMessage }}
            </div>
          </div>

          <!-- Quick scenarios for 对话 mode -->
          <div v-if="chatStore.currentMode === 'free' && !isBusy" class="assistant-prompts">
            <button
              v-for="scene in scenarios"
              :key="scene.key"
              type="button"
              class="btn btn-ghost btn-sm rounded-full"
              @click="triggerScenario(scene)"
            >{{ scene.label }}</button>
          </div>

          <!-- Plan design input for 方案设计 mode (no plans yet) -->
          <div
            v-if="chatStore.currentMode === 'plan' && !chatStore.currentPlans.length"
            class="plan-input-area"
          >
            <div class="plan-input-row">
              <input
                v-model="planTitle"
                type="text"
                class="input"
                placeholder="输入实验名称"
                :disabled="isBusy"
              />
            </div>
            <div class="plan-input-row">
              <select v-model="planGrade" class="input" :disabled="isBusy">
                <option value="">选择年级段</option>
                <option value="低段">低段（1-2年级）</option>
                <option value="中段">中段（3-4年级）</option>
                <option value="高段">高段（5-6年级）</option>
              </select>
              <button
                type="button"
                class="btn btn-primary"
                :disabled="!planTitle.trim() || !planGrade || isBusy"
                @click="generatePlansAction"
              >
                {{ isBusy ? '生成中…' : '开始设计' }}
              </button>
            </div>
          </div>

          <!-- Vision upload hint for 实验后 mode -->
          <div v-if="chatStore.currentMode === 'post' && !chatStore.currentHasSent" class="post-hint">
            <p class="text-xs text-gray-500">描述实验现象，或上传照片并补充说明，分析后可生成 A4 诊断报告</p>
          </div>
        </div>

        <!-- Message list -->
        <div
          v-for="(msg, idx) in chatStore.currentMessages"
          :key="idx"
          class="assistant-msg row items-start gap-3"
          :class="msg.role === 'user' ? 'assistant-msg--user justify-end' : 'assistant-msg--ai'"
        >
          <div
            v-if="msg.role === 'assistant'"
            class="avatar avatar-sm avatar-grad-warm shrink-0"
            aria-hidden="true"
          >{{ aiAvatar }}</div>
          <div
            class="msg-bubble"
            :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'"
          >
            <img
              v-if="msg.imageUrl || msg.imageThumb"
              :src="msg.imageUrl || msg.imageThumb"
              alt="实验照片"
              class="msg-image"
            />
            <div
              v-if="msg.content"
              v-html="msg.role === 'assistant' ? renderMarkdown(msg.content) : escapeHtml(msg.content)"
            ></div>
            <button
              v-if="msg.role === 'assistant' && msg.showReportBtn && msg.report"
              type="button"
              class="btn btn-soft btn-sm diagnosis-report-btn"
              @click="openDiagnosisReport(msg.report, idx)"
            >
              <i data-lucide="file-text" class="icon icon-sm"></i> 编辑诊断报告
            </button>
          </div>
          <div v-if="msg.role === 'user'" class="shrink-0">
            <UserAvatar size="sm" shrink aria-hidden />
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="isBusy" class="assistant-msg assistant-msg--ai row items-start gap-3">
          <div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">{{ aiAvatar }}</div>
          <div class="msg-bubble msg-ai assistant-typing">
            <span class="assistant-typing__dot"></span>
            <span class="assistant-typing__dot"></span>
            <span class="assistant-typing__dot"></span>
          </div>
        </div>

        <!-- Plan card grid (when plans exist) -->
        <PlanCardGrid
          v-if="chatStore.currentMode === 'plan' && chatStore.currentPlans.length"
          :plans="chatStore.currentPlans"
          :active-id="selectedPlanId"
          @select="onSelectPlan"
          @edit="onEditPlan"
          @save="onSavePlan"
          @export="onExportPlan"
        />
      </div>

      <!-- ===== Composer area ===== -->
      <div class="pad-chat__composer pad-chat__composer--sticky" data-assistant-composer>
        <!-- Voice panel -->
        <div class="assistant-voice-panel" data-assistant-voice-panel v-show="isListening">
          <div class="assistant-voice-panel__waves" aria-hidden="true">
            <span class="assistant-voice-panel__wave-bar"></span>
            <span class="assistant-voice-panel__wave-bar"></span>
            <span class="assistant-voice-panel__wave-bar"></span>
            <span class="assistant-voice-panel__wave-bar"></span>
            <span class="assistant-voice-panel__wave-bar"></span>
          </div>
          <p class="assistant-voice-panel__status" data-assistant-voice-status>{{ voiceStatus }}</p>
          <p class="assistant-voice-panel__preview" data-assistant-voice-preview aria-live="polite">{{ voicePreview }}</p>
        </div>

        <div v-if="pendingImage" class="pending-image-bar">
          <img :src="pendingImage.dataUrl" alt="待发送的实验照片" class="pending-image-bar__thumb" />
          <span class="pending-image-bar__hint">已选照片，可补充文字说明后发送</span>
          <button type="button" class="pending-image-bar__remove" aria-label="移除照片" @click="clearPendingImage">
            <i data-lucide="x" class="icon icon-sm"></i>
          </button>
        </div>

        <div class="row items-center assistant-composer__row">
          <!-- Image upload button for 实验后 mode -->
          <button
            v-if="chatStore.currentMode === 'post'"
            type="button"
            class="btn btn-soft assistant-composer__img"
            aria-label="上传图片"
            :disabled="isBusy"
            @click="triggerImageUpload"
          >
            <i data-lucide="camera" class="icon"></i>
          </button>

          <!-- Voice button for non-plan modes -->
          <button
            v-if="chatStore.currentMode !== 'plan'"
            type="button"
            class="btn btn-soft assistant-composer__mic"
            aria-label="语音输入"
            :class="{ 'is-active': isListening }"
            :disabled="isBusy"
            @click="toggleVoice"
          >
            <i data-lucide="mic" class="icon"></i>
          </button>

          <!-- Text input -->
          <div class="input-group flex-1 assistant-composer__input">
            <input
              ref="inputRef"
              v-model="inputText"
              type="text"
              class="input"
              :placeholder="isListening ? '正在录音…' : inputPlaceholder"
              aria-label="输入消息"
              data-assistant-input
              autocomplete="off"
              inputmode="text"
              :disabled="isBusy || chatStore.currentMode === 'plan'"
              @keydown.enter.prevent="handleSend"
            >
          </div>
          <button
            type="button"
            class="btn btn-primary assistant-composer__send"
            aria-label="发送"
            data-assistant-send
            :disabled="!canSend || isBusy || isListening || chatStore.currentMode === 'plan'"
            @click="handleSend"
          >
            <i data-lucide="send" class="icon"></i>
          </button>
        </div>
      </div>

    <!-- Hidden file input for image upload -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      capture="environment"
      style="display: none"
      @change="onImageSelected"
    />

    <!-- Plan Edit Bottom Sheet -->
    <PlanEditSheet
      :visible="showPlanEdit"
      :plan="editingPlan"
      @close="showPlanEdit = false"
      @save="onPlanSaveComplete"
    />

    <!-- A4 Document Preview -->
    <A4DocumentPreview
      :visible="showA4Preview"
      :plan="previewPlan"
      @close="showA4Preview = false"
    />

    <!-- Diagnosis Report Preview -->
    <DiagnosisReportPreview
      :visible="showDiagnosisReport"
      :report="diagnosisReport"
      @close="showDiagnosisReport = false"
      @save="onDiagnosisReportSave"
    />
  </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useChatStore } from '@/stores/chat'
import { useProfileStore } from '@/stores/profile'
import { normalizeRole } from '@/utils/role'
import { resolveGradeLabel, resolveGradeSegment } from '@/utils/gradeLevel'
import {
  sendChatMessage,
  sendChatMessageStream,
  generatePlans,
  fetchChatHistory,
  clearChatSession
} from '@/api/chat'
import { compressImage } from '@/utils/imageCompress'
import { buildDiagnosisReport } from '@/utils/diagnosisReport'
import { renderMarkdown } from '@/utils/markdown'
import { createSpeechRecognizer, isSpeechRecognitionSupported } from '@/utils/speechRecognition'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'
import PlanCardGrid from './PlanCardGrid.vue'
import PlanEditSheet from './PlanEditSheet.vue'
import A4DocumentPreview from './A4DocumentPreview.vue'
import DiagnosisReportPreview from './DiagnosisReportPreview.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const chatStore = useChatStore()
const profileStore = useProfileStore()

appStore.setActiveTab('chat')

/* ===== Refs ===== */
const messagesRef = ref(null)
const inputRef = ref(null)
const fileInputRef = ref(null)
const inputText = ref('')
const isBusy = ref(false)
const showMenu = ref(false)
const isListening = ref(false)
const voiceStatus = ref('正在听，请说话…')
const voicePreview = ref('')
let voiceRecognizer = null

/* ===== Plan state ===== */
const planTitle = ref('')
const planGrade = ref('')
const selectedPlanId = ref(null)
const editingPlan = ref({})
const previewPlan = ref({})
const showPlanEdit = ref(false)
const showA4Preview = ref(false)
const showDiagnosisReport = ref(false)
const diagnosisReport = ref({})
const activeReportMsgIdx = ref(-1)
const pendingImage = ref(null)

const { initIcons } = useLucideIcons()

/* ===== Computed ===== */
const canSend = computed(() => {
  if (chatStore.currentMode === 'post') {
    return Boolean(inputText.value.trim() || pendingImage.value)
  }
  return Boolean(inputText.value.trim())
})
const userRoleId = computed(() => normalizeRole(userStore.userInfo.userRoleId))

const aiName = computed(() => {
  const role = userRoleId.value
  if (role === 'student') return '石头老师'
  if (role === 'teacher') return '教研助手'
  if (role === 'parent') return '育儿助手'
  return 'AI 助手'
})

const aiAvatar = computed(() => {
  const role = userRoleId.value
  if (role === 'student') return '🪨'
  if (role === 'teacher') return '👩‍🏫'
  if (role === 'parent') return '👨‍👩‍👧'
  return '🤖'
})

const modeLabel = computed(() => chatStore.getModeLabel(chatStore.currentMode))

const inputPlaceholder = computed(() => {
  if (chatStore.currentMode === 'plan') return ''
  if (chatStore.currentMode === 'post') {
    return pendingImage.value ? '补充实验现象或操作说明…' : '描述实验现象，或先上传照片…'
  }
  return '说说你的科学探究想法…'
})

/* ===== Scenarios for 对话 mode ===== */
const scenarios = computed(() => {
  const role = userRoleId.value
  if (role === 'parent') return PARENT_SCENARIOS
  if (role === 'teacher') return TEACHER_SCENARIOS
  return STUDENT_SCENARIOS
})

const STUDENT_SCENARIOS = [
  { key: 'wind', label: '🧭 自制风向标', text: '我想做一个简单的风向标，该怎么做？' },
  { key: 'rainbow', label: '🌈 彩虹液体分层', text: '彩虹液体分层实验，为什么不同颜色的液体不会混在一起？' },
  { key: 'balloon', label: '🚀 气球火箭', text: '气球火箭总是飞不远，有什么改进办法？' },
  { key: 'safety', label: '⚠️ 实验安全', text: '在家做科学小实验，要注意哪些安全事项？' },
  { key: 'seed', label: '🌱 种子发芽', text: '帮我设计一个观察种子发芽的实验方案' }
]

const PARENT_SCENARIOS = [
  { key: 'progress', label: '📊 学习进度', text: '如何了解孩子最近的实验任务完成情况？' },
  { key: 'materials', label: '🧪 材料准备', text: '孩子在家做实验，家长需要提前准备哪些常见材料？' },
  { key: 'safety', label: '🛡️ 实验安全', text: '家长陪同孩子做科学实验时，有哪些安全注意事项？' },
  { key: 'guide', label: '💡 科学启蒙', text: '如何在日常生活中引导孩子进行科学观察和思考？' }
]

const TEACHER_SCENARIOS = [
  { key: 'lesson', label: '📖 实验教案', text: '请帮我设计一份小学三年级科学实验教案，主题：植物生长' },
  { key: 'recommend', label: '💡 实验推荐', text: '推荐几个适合讲解「浮力」的动手实验' },
  { key: 'analyze', label: '📈 数据分析', text: '如何分析班级实验提交率偏低的原因并改进？' },
  { key: 'review', label: '✅ 评价建议', text: '如何评价学生实验报告并给出有效反馈？' }
]

/* ===== SSR modes: which use SSE streaming ===== */
const SSE_MODES = ['free', 'pre', 'post']

/* ===== Mode management ===== */
function switchMode(mode) {
  if (isBusy.value) return
  chatStore.switchMode(mode)
  updateUrlQuery(mode)
  scrollToBottom()
}

function updateUrlQuery(mode) {
  const query = { ...route.query }
  if (mode && mode !== chatStore.defaultMode) {
    query.mode = mode
  } else {
    delete query.mode
  }
  router.replace({ query }).catch(() => {})
}

/* ===== Send message ===== */
async function handleSend() {
  const text = inputText.value.trim()
  const mode = chatStore.currentMode

  if (mode === 'plan') return
  if (!canSend.value || isBusy.value) return

  // 实验后模式：带图片的图文联合分析
  if (mode === 'post' && pendingImage.value) {
    await sendPostWithImage(text, pendingImage.value)
    return
  }

  if (!text) return

  isBusy.value = true
  inputText.value = ''

  chatStore.addMessage(mode, 'user', text)

  if (SSE_MODES.includes(mode)) {
    await doSendStream(text, mode)
  } else {
    await doSendSync(text, mode)
  }

  isBusy.value = false
  scrollToBottom()
}

async function doSendStream(text, mode, extraParams = {}) {
  const params = { ...buildChatParams(text, mode), ...extraParams }

  chatStore.addMessage(mode, 'assistant', '')

  return new Promise((resolve) => {
    let streamDiagnosisReport = null
    sendChatMessageStream(
      params,
      (fullText) => {
        const msgs = chatStore.modes[mode].messages
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content = fullText
        }
        scrollToBottom()
      },
      (fullText, meta = {}) => {
        streamDiagnosisReport = meta.diagnosisReport || streamDiagnosisReport
        const msgs = chatStore.modes[mode].messages
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant' && !last.content) {
          last.content = fullText || '收到你的问题，让我想想…'
        }
        if (mode === 'post' && last?.role === 'assistant' && last.content && !last.showReportBtn) {
          if (!extraParams.skipReportAttach) {
            const desc = extraParams.userDescription ?? text
            attachPostDiagnosisReport(desc, last.content, {
              imageUrl: extraParams.reportImageUrl || '',
              sections: extraParams.diagnosisReport || streamDiagnosisReport,
            })
          }
        }
        chatStore.persistAll()
        resolve({ diagnosisReport: streamDiagnosisReport })
      },
      () => {
        const msgs = chatStore.modes[mode].messages
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant' && !last.content) {
          last.content = `抱歉，${aiName.value}暂时无法回答，请稍后再试。`
        } else {
          chatStore.addMessage(mode, 'assistant', `抱歉，${aiName.value}暂时无法回答，请稍后再试。`)
        }
        resolve()
      }
    )
  })
}

async function doSendSync(text, mode) {
  try {
    const res = await sendChatMessage(buildChatParams(text, mode))
    const payload = res?.data ?? res
    const reply = payload?.reply || payload?.response || ''
    const threadId = payload?.thread_id || payload?.threadId || ''
    if (threadId) chatStore.setThreadId(mode, threadId)
    chatStore.addMessage(mode, 'assistant', reply || '收到你的问题，让我想想…')
  } catch {
    chatStore.addMessage(mode, 'assistant', `抱歉，${aiName.value}暂时无法回答，请稍后再试。`)
  }
}

function buildChatParams(text, mode) {
  const ROLE_MAP = {
    free: 'free_chat',
    pre: 'pre_experiment',
    plan: 'plan_design',
    post: 'post_experiment',
  }
  const params = {
    message: text,
    thread_id: chatStore.modes[mode].threadId || undefined,
    role: ROLE_MAP[mode] || 'student',
    user_name: userStore.userInfo.username || '',
    user_id: userStore.userInfo.userId || '',
  }

  if (mode === 'pre' || mode === 'plan' || mode === 'post') {
    params.grade_level =
      resolveGradeSegment({ profile: profileStore.profile, userInfo: userStore.userInfo }) ||
      (mode === 'post' ? '中段' : undefined)
  }

  return params
}

function makeImageThumb(dataUrl) {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      const maxSize = 240
      const scale = Math.min(maxSize / img.width, maxSize / img.height, 1)
      const w = Math.round(img.width * scale)
      const h = Math.round(img.height * scale)
      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h
      canvas.getContext('2d').drawImage(img, 0, 0, w, h)
      resolve(canvas.toDataURL('image/jpeg', 0.65))
    }
    img.onerror = () => resolve('')
    img.src = dataUrl
  })
}

function attachPostDiagnosisReport(userText, assistantContent, options = {}) {
  const msgs = chatStore.modes.post.messages
  const last = msgs[msgs.length - 1]
  if (!last || last.role !== 'assistant' || !assistantContent) return

  const description = (userText || '').trim()
  const imageUrl = options.imageUrl || ''
  const sections = options.sections || null

  last.showReportBtn = true
  last.report = buildDiagnosisReport({
    studentName:
      profileStore.profile.userName ||
      userStore.userInfo.username ||
      '',
    gradeLevel: resolveGradeLabel({
      profile: profileStore.profile,
      userInfo: userStore.userInfo,
    }),
    experimentTitle: description.slice(0, 40) || (imageUrl ? '实验照片分析' : '实验现象分析'),
    description: description || (imageUrl ? '（上传了实验照片）' : ''),
    imageUrl,
    analysis: assistantContent,
    findings: sections?.findings || '',
    diagnosis: sections?.diagnosis || '',
    recommendations: sections?.recommendations || '',
    userEdited: false,
  })
  chatStore.persistAll()
}

function stripBase64Prefix(dataUrl) {
  if (!dataUrl) return ''
  const idx = dataUrl.indexOf(',')
  return idx >= 0 ? dataUrl.slice(idx + 1) : dataUrl
}

async function sendPostWithImage(text, image) {
  isBusy.value = true
  inputText.value = ''

  const displayText = text || '（上传了实验照片，请结合图片进行分析）'
  const apiMessage = text || '请结合上传的实验照片，分析实验现象并给出诊断意见。'
  const imageThumb = await makeImageThumb(image.dataUrl)

  chatStore.addMessage('post', 'user', displayText, {
    imageUrl: image.dataUrl,
    imageThumb
  })

  try {
    const { diagnosisReport } = await doSendStream(apiMessage, 'post', {
      image_base64: stripBase64Prefix(image.base64 || image.dataUrl),
      skipReportAttach: true,
    })

    const msgs = chatStore.modes.post.messages
    const last = msgs[msgs.length - 1]
    if (last?.role === 'assistant' && last.content) {
      attachPostDiagnosisReport(text || displayText, last.content, {
        imageUrl: imageThumb || image.dataUrl,
        sections: diagnosisReport,
      })
    }
  } finally {
    pendingImage.value = null
    isBusy.value = false
    scrollToBottom()
    initIcons()
  }
}

/* ===== Plan generation ===== */
async function generatePlansAction() {
  if (!planTitle.value.trim() || !planGrade.value || isBusy.value) return

  isBusy.value = true
  // Add user message to mark as sent
  chatStore.addMessage('plan', 'user', `帮我设计一个关于"${planTitle.value.trim()}"的实验方案（${planGrade.value}）`)
  try {
    const res = await generatePlans({
      experiment_title: planTitle.value.trim(),
      grade_level: planGrade.value,
      user_name: userStore.userInfo.username || '',
      user_id: userStore.userInfo.userId || '',
      thread_id: chatStore.modes.plan.threadId || undefined,
    })
    const payload = res?.data ?? res
    const rawPlans = payload?.plans || []
    const threadId = payload?.thread_id || payload?.threadId || ''
    const reply = payload?.reply || ''

    if (threadId) chatStore.setThreadId('plan', threadId)
    const plans = normalizePlanCards(rawPlans, planGrade.value)
    if (!plans.length) {
      chatStore.addMessage('plan', 'assistant', reply || '未能生成方案，请换个实验名称再试。')
    } else {
      chatStore.setPlans(plans)
      if (reply) chatStore.addMessage('plan', 'assistant', reply)
    }

    scrollToBottom()
  } catch (err) {
    console.error('[plan generation] error:', err)
    // Show error as assistant message
    chatStore.addMessage('plan', 'assistant', '方案生成失败，请稍后再试。')
  } finally {
    isBusy.value = false
  }
}

/* ===== Scenario ===== */
function triggerScenario(scene) {
  if (isBusy.value) return
  inputText.value = scene.text || scene.label || scene.prompt || ''
  handleSend()
}

/* ===== Image upload (实验后) ===== */
function triggerImageUpload() {
  fileInputRef.value?.click()
}

async function onImageSelected(event) {
  const file = event.target?.files?.[0]
  if (!file) return

  if (fileInputRef.value) fileInputRef.value.value = ''

  try {
    const compressed = await compressImage(file, {
      maxWidth: 1024,
      maxHeight: 1024,
      quality: 0.7,
      maxSizeKB: 500
    })
    pendingImage.value = {
      dataUrl: compressed.base64,
      base64: compressed.base64
    }
    initIcons()
  } catch {
    chatStore.addMessage('post', 'assistant', '图片读取失败，请换一张照片试试。')
    scrollToBottom()
  }
}

function clearPendingImage() {
  pendingImage.value = null
}

function openDiagnosisReport(report, msgIdx) {
  if (!report) return
  activeReportMsgIdx.value = msgIdx
  diagnosisReport.value = buildDiagnosisReport({
    ...report,
    analysis: report.analysisRaw || report.analysis || '',
    userEdited: Boolean(report.userEdited),
  })
  showDiagnosisReport.value = true
  nextTick(initIcons)
}

function onDiagnosisReportSave(edited) {
  const idx = activeReportMsgIdx.value
  if (idx < 0) return
  const msgs = chatStore.modes.post.messages
  if (msgs[idx]) {
    msgs[idx].report = { ...edited }
    diagnosisReport.value = { ...edited }
    chatStore.persistAll()
  }
}

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

/* ===== Plan actions ===== */
function onSelectPlan(plan) {
  selectedPlanId.value = plan.id || plan.title
  // Could show detail or scroll to more info
}

function onEditPlan(plan) {
  editingPlan.value = { ...plan }
  showPlanEdit.value = true
}

function onSavePlan(plan) {
  chatStore.savePlan(plan)
}

function onExportPlan(plan) {
  previewPlan.value = { ...plan }
  showA4Preview.value = true
}

function onPlanSaveComplete(plan) {
  chatStore.savePlan(plan)
  showPlanEdit.value = false
}

/* ===== Voice ===== */
function toggleVoice() {
  if (isListening.value) {
    stopVoiceRecognizer()
    isListening.value = false
    voicePreview.value = ''
    return
  }

  if (!isSpeechRecognitionSupported()) {
    voiceStatus.value = '当前浏览器不支持语音输入'
    return
  }

  isListening.value = true
  voiceStatus.value = '正在听，请说话…'
  voicePreview.value = ''

  voiceRecognizer = createSpeechRecognizer({
    interimResults: true,
    onResult(text) {
      voicePreview.value = text
    },
    onError(err) {
      voiceStatus.value = err === 'not-allowed' ? '请允许麦克风权限' : '语音识别失败'
      isListening.value = false
      stopVoiceRecognizer()
    },
    onEnd() {
      const text = voicePreview.value.trim()
      if (text) {
        voiceStatus.value = '识别完成'
        setTimeout(() => {
          isListening.value = false
          inputText.value = text
          voicePreview.value = ''
          stopVoiceRecognizer()
        }, 400)
      } else if (isListening.value) {
        voiceStatus.value = '未识别到内容，请重试'
        isListening.value = false
        stopVoiceRecognizer()
      }
    }
  })

  try {
    voiceRecognizer?.start()
  } catch {
    voiceStatus.value = '无法启动语音识别'
    isListening.value = false
    stopVoiceRecognizer()
  }
}

function stopVoiceRecognizer() {
  try {
    voiceRecognizer?.stop?.()
  } catch { /* ignore */ }
  voiceRecognizer = null
}

/* ===== Clear ===== */
function clearConversation() {
  const mode = chatStore.currentMode
  const currentThreadId = chatStore.modes[mode].threadId
  if (currentThreadId) {
    clearChatSession(currentThreadId).catch(() => {})
  }
  chatStore.clearModule(mode)
  pendingImage.value = null
  showMenu.value = false
}

/* ===== Helpers ===== */
function normalizePlanCards(rawPlans, gradeLevel = '') {
  return (rawPlans || []).map((p, i) => {
    const materials = Array.isArray(p.materials)
      ? p.materials.map((m) => {
          const tip = m.tip ? `（${m.tip}）` : ''
          return `• ${m.name}：${m.quantity || '适量'}${tip}`
        }).join('\n')
      : String(p.materials || '')
    const steps = Array.isArray(p.steps) ? p.steps.join('\n') : String(p.steps || '')
    const notes = Array.isArray(p.safety_tips)
      ? p.safety_tips.map((t) => `• ${t}`).join('\n')
      : String(p.safety_tips || p.notes || '')

    return {
      id: p.id || `plan-${i + 1}`,
      title: p.plan_name || p.title || `方案 ${i + 1}`,
      summary: p.description || p.summary || '',
      experimentName: p.plan_name || p.title || '',
      gradeLevel: p.gradeLevel || gradeLevel,
      objective: p.description || p.objective || '',
      materials,
      steps,
      expectedResult: p.what_you_see || p.expectedResult || '',
      notes,
      raw: p,
    }
  })
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

/* ===== Init ===== */
onMounted(async () => {
  initIcons()

  await profileStore.loadProfile()

  // Initialize store from URL query & default
  chatStore.initFromQuery(route.query)

  if (!planGrade.value) {
    planGrade.value =
      resolveGradeSegment({ profile: profileStore.profile, userInfo: userStore.userInfo }) || ''
  }

  // Load chat history for current module if thread exists
  const mode = chatStore.currentMode
  const threadId = chatStore.modes[mode].threadId
  if (threadId && !chatStore.modes[mode].messages.length) {
    try {
      const res = await fetchChatHistory(threadId)
      const history = normalizeHistory(res?.data)
      if (history.length) {
        chatStore.modes[mode].hasSent = true
        chatStore.modes[mode].messages = history.map((item) => ({
          role: item.role === 'user' || item.role === 'human' ? 'user' : 'assistant',
          content: item.content || item.text || ''
        }))
        scrollToBottom()
      }
    } catch { /* ignore */ }
  }
})

onBeforeUnmount(() => {
  chatStore.persistAll()
  stopVoiceRecognizer()
})

/* ===== Normalize history (same as before) ===== */
function normalizeHistory(data) {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.messages)) return data.messages
  if (data && Array.isArray(data.history)) return data.history
  return []
}
</script>

<style scoped>
/* ===== Mode dropdown ===== */
.mode-dropdown {
  flex-shrink: 0;
  margin: 0 4px;
}
.mode-select {
  appearance: none;
  -webkit-appearance: none;
  padding: 4px 24px 4px 10px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface) url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E") no-repeat right 6px center;
  background-size: 14px;
  color: var(--color-text);
  cursor: pointer;
  min-width: 80px;
  max-width: 140px;
  outline: none;
  transition: border-color var(--dur-fast) var(--ease);
}
.mode-select:focus {
  border-color: var(--color-primary);
}
.mode-select option {
  font-size: 13px;
  padding: 4px 8px;
}

/* ===== Welcome section ===== */
.welcome-section {
  padding-bottom: 8px;
}

/* ===== Plan input area ===== */
.plan-input-area {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 4px;
}
.plan-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.plan-input-row .input {
  flex: 1;
}

/* ===== Post hint ===== */
.post-hint {
  margin-top: 8px;
  padding: 0 4px;
}

/* ===== Message image ===== */
.msg-image {
  display: block;
  max-width: 200px;
  max-height: 160px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  margin-bottom: 6px;
}

.diagnosis-report-btn {
  margin-top: 10px;
}

/* ===== Pending image bar ===== */
.pending-image-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 6px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  border: 1px dashed var(--color-border);
}
.pending-image-bar__thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.pending-image-bar__hint {
  flex: 1;
  font-size: var(--text-xs);
  color: var(--color-text-2);
}
.pending-image-bar__remove {
  border: none;
  background: none;
  padding: 4px;
  cursor: pointer;
  color: var(--color-text-2);
}

/* ===== Existing chat styles (preserved) ===== */
.chat-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.3);
}
.chat-menu {
  position: absolute;
  top: 56px;
  right: 12px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}
.chat-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  font-size: var(--text-sm);
  color: var(--color-text-1);
  cursor: pointer;
  transition: background var(--dur-fast) var(--ease);
}
.chat-menu__item:hover {
  background: var(--color-bg);
}
</style>
