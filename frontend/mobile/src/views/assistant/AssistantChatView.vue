<template>
  <div class="prototype-container pad-shell safe-top" :class="shellThemeClass" data-layout="assistant-chat" data-assistant-chat>
    <div class="pad-main pad-chat">
      <header class="pad-chat__topbar safe-top">
        <PageBackButton fallback="/home" />
        <h1 class="pad-chat__topbar-title flex-1 min-w-0">🤖 AI助手</h1>
        <span v-if="modeBadge" class="badge badge-info text-xs shrink-0">{{ modeBadge }}</span>
        <button type="button" class="icon-btn" aria-label="更多" @click="showMenu = !showMenu">
          <i data-lucide="more-vertical" class="icon"></i>
        </button>
      </header>

      <!-- 下拉菜单 -->
      <div v-if="showMenu" class="chat-menu-overlay" @click="showMenu = false">
        <div class="chat-menu" @click.stop>
          <button class="chat-menu__item" @click="clearConversation">
            <i data-lucide="trash-2" class="icon"></i> 清除对话
          </button>
        </div>
      </div>

      <div class="pad-chat__messages stack-4" ref="messagesRef" data-assistant-messages>
        <!-- 欢迎消息 -->
        <div class="assistant-msg assistant-msg--ai row items-start gap-3">
          <div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">{{ aiAvatar }}</div>
          <div class="msg-bubble msg-ai">
            <div class="text-xs font-bold text-brand mb-1">{{ aiAvatar }} {{ aiName }} · {{ modeBadge }}</div>
            {{ welcomeMessage }}
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="assistant-msg row items-start gap-3"
          :class="msg.role === 'user' ? 'assistant-msg--user justify-end' : 'assistant-msg--ai'"
        >
          <div v-if="msg.role === 'assistant'" class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">{{ aiAvatar }}</div>
          <div
            class="msg-bubble"
            :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'"
            v-html="msg.content"
          ></div>
          <div v-if="msg.role === 'user'" class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">{{ userInitial }}</div>
        </div>

        <!-- 打字指示器 -->
        <div v-if="isBusy" class="assistant-msg assistant-msg--ai row items-start gap-3">
          <div class="avatar avatar-sm avatar-grad-warm shrink-0" aria-hidden="true">{{ aiAvatar }}</div>
          <div class="msg-bubble msg-ai assistant-typing">
            <span class="assistant-typing__dot"></span>
            <span class="assistant-typing__dot"></span>
            <span class="assistant-typing__dot"></span>
          </div>
        </div>

        <!-- 快捷场景按钮 -->
        <div v-if="!hasSent && !isBusy" class="assistant-prompts" data-assistant-prompts>
          <button
            v-for="scene in scenarios"
            :key="scene.key"
            type="button"
            class="btn btn-ghost btn-sm rounded-full"
            @click="triggerScenario(scene)"
          >{{ scene.label }}</button>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="pad-chat__composer pad-chat__composer--sticky" data-assistant-composer>
        <!-- 语音面板 -->
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
        <div class="row gap-2 items-center">
          <button
            type="button"
            class="btn btn-soft btn-icon"
            aria-label="语音输入"
            :class="{ 'is-active': isListening }"
            :disabled="isBusy"
            @click="toggleVoice"
          >
            <i data-lucide="mic" class="icon"></i>
          </button>
          <div class="input-group flex-1">
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
              :disabled="isBusy"
              @keydown.enter.prevent="handleSend"
              @input="updateSendState"
            >
          </div>
          <button
            type="button"
            class="btn btn-primary"
            aria-label="发送"
            data-assistant-send
            :disabled="!inputText.trim() || isBusy || isListening"
            @click="handleSend"
          >
            <i data-lucide="send" class="icon icon-sm"></i>
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { normalizeRole } from '@/utils/role'
import { sendChatMessage, fetchChatHistory, clearChatSession } from '@/api/chat'
import { createSpeechRecognizer, isSpeechRecognitionSupported } from '@/utils/speechRecognition'
import PageBackButton from '@/components/PageBackButton.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

appStore.setActiveTab('chat')

const messagesRef = ref(null)
const inputRef = ref(null)

const inputText = ref('')
const isBusy = ref(false)
const hasSent = ref(false)
const showMenu = ref(false)
const isListening = ref(false)
const voiceStatus = ref('正在听，请说话…')
const voicePreview = ref('')
const threadId = ref('')
const messages = ref([])
let voiceRecognizer = null

const userName = computed(() => userStore.userInfo.username || '用户')
const userInitial = computed(() => userName.value.charAt(0) || '用')
const userRoleId = computed(() => normalizeRole(userStore.userInfo.userRoleId))

const roleConfig = computed(() => {
  const configs = {
    student: {
      mode: 'student',
      modeBadge: '学习模式',
      aiName: '石头老师',
      aiAvatar: '🪨',
      welcomeMessage: `${userName.value}同学，你好！我是 AI 助手（学习模式），会帮你设计和优化小学科学实验方案。你可以直接提问，或点下面的话题开始聊～`,
      inputPlaceholder: '说说你的科学探究想法…'
    },
    parent: {
      mode: 'parent',
      modeBadge: '家长模式',
      aiName: '育儿助手',
      aiAvatar: '👨‍👩‍👧',
      welcomeMessage: `${userName.value}家长，您好！我是 AI 助手（家长模式），可以帮您了解孩子的学习进度、实验安排和科学素养成长情况。`,
      inputPlaceholder: '问问孩子的学习或实验安排…'
    },
    teacher: {
      mode: 'teacher',
      modeBadge: '教研模式',
      aiName: '教研助手',
      aiAvatar: '👩‍🏫',
      welcomeMessage: `${userName.value}老师，您好！我是 AI 助手（教研模式），可以帮您设计教案、推荐实验、分析班级数据。`,
      inputPlaceholder: '输入教学问题或班级分析需求…'
    }
  }
  return configs[userRoleId.value] || {
    mode: 'student',
    modeBadge: '',
    aiName: 'AI 助手',
    aiAvatar: '🤖',
    welcomeMessage: `${userName.value}，你好！我是 AI 助手，有什么可以帮你的吗？`,
    inputPlaceholder: '输入你的问题…'
  }
})

const aiName = computed(() => roleConfig.value.aiName)
const aiAvatar = computed(() => roleConfig.value.aiAvatar)
const welcomeMessage = computed(() => roleConfig.value.welcomeMessage)
const modeBadge = computed(() => roleConfig.value.modeBadge)
const inputPlaceholder = computed(() => roleConfig.value.inputPlaceholder)
const shellThemeClass = computed(() => {
  if (userRoleId.value === 'parent') return 'theme-parent'
  if (userRoleId.value === 'teacher') return 'theme-teacher'
  return 'theme-primary'
})

const STUDENT_SCENARIOS = [
  { key: 'wind', label: '🧭 自制风向标', prompt: '我想做一个简单的风向标，该怎么做？' },
  { key: 'rainbow', label: '🌈 彩虹液体分层', prompt: '彩虹液体分层实验，为什么不同颜色的液体不会混在一起？' },
  { key: 'balloon', label: '🚀 气球火箭', prompt: '气球火箭总是飞不远，有什么改进办法？' },
  { key: 'safety', label: '⚠️ 实验安全', prompt: '在家做科学小实验，要注意哪些安全事项？' },
  { key: 'seed', label: '🌱 种子发芽', prompt: '帮我设计一个观察种子发芽的实验方案' }
]

const PARENT_SCENARIOS = [
  { key: 'progress', label: '📊 学习进度', prompt: '如何了解孩子最近的实验任务完成情况？' },
  { key: 'materials', label: '🧪 材料准备', prompt: '孩子在家做实验，家长需要提前准备哪些常见材料？' },
  { key: 'safety', label: '🛡️ 实验安全', prompt: '家长陪同孩子做科学实验时，有哪些安全注意事项？' },
  { key: 'guide', label: '💡 科学启蒙', prompt: '如何在日常生活中引导孩子进行科学观察和思考？' }
]

const TEACHER_SCENARIOS = [
  { key: 'lesson', label: '📖 实验教案', prompt: '请帮我设计一份小学三年级科学实验教案，主题：植物生长' },
  { key: 'recommend', label: '💡 实验推荐', prompt: '推荐几个适合讲解「浮力」的动手实验' },
  { key: 'analyze', label: '📈 数据分析', prompt: '如何分析班级实验提交率偏低的原因并改进？' },
  { key: 'review', label: '✅ 批阅建议', prompt: '如何批阅学生实验报告并给出有效反馈？' }
]

const scenarios = computed(() => {
  if (userRoleId.value === 'parent') return PARENT_SCENARIOS
  if (userRoleId.value === 'teacher') return TEACHER_SCENARIOS
  return STUDENT_SCENARIOS
})

function threadStorageKey() {
  const uid = userStore.userInfo.userId || 'anon'
  const mode = roleConfig.value.mode || 'student'
  return `bslab-chat-thread-${mode}-${uid}`
}

function loadStoredThreadId() {
  try {
    return localStorage.getItem(threadStorageKey()) || ''
  } catch {
    return ''
  }
}

function saveStoredThreadId(id) {
  try {
    if (id) localStorage.setItem(threadStorageKey(), id)
    else localStorage.removeItem(threadStorageKey())
  } catch { /* ignore */ }
}

function normalizeHistory(data) {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.messages)) return data.messages
  if (data && Array.isArray(data.history)) return data.history
  return []
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function formatReply(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function addMessage(role, content) {
  messages.value.push({
    role,
    content: role === 'assistant' ? formatReply(content) : content
  })
  scrollToBottom()
}

function updateSendState() {}

async function doSend(text) {
  if (!text.trim() || isBusy.value) return

  isBusy.value = true
  hasSent.value = true
  inputText.value = ''

  addMessage('user', text)

  try {
    const res = await sendChatMessage({
      message: text,
      thread_id: threadId.value || undefined,
      role: roleConfig.value.mode || 'student',
      user_name: userStore.userInfo.username || '',
      user_id: userStore.userInfo.userId || '',
      grade_level: userStore.userInfo.gradeLevel || undefined
    })
    threadId.value = res.data.thread_id || threadId.value
    saveStoredThreadId(threadId.value)
    addMessage('assistant', res.data.reply || res.data.response || '收到你的问题，让我想想…')
  } catch (err) {
    addMessage('assistant', `抱歉，${aiName.value}暂时无法回答，请稍后再试。`)
  } finally {
    isBusy.value = false
    scrollToBottom()
  }
}

function handleSend() {
  doSend(inputText.value)
}

function triggerScenario(scene) {
  doSend(scene.prompt || scene.label)
}

function stopVoiceRecognizer() {
  try {
    voiceRecognizer?.stop?.()
  } catch { /* ignore */ }
  voiceRecognizer = null
}

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

function clearConversation() {
  const current = threadId.value
  if (current) {
    clearChatSession(current).catch(() => {})
  }
  threadId.value = ''
  saveStoredThreadId('')
  messages.value = []
  hasSent.value = false
  showMenu.value = false
}

watch(isBusy, (val) => {
  updateSendState()
})

onMounted(async () => {
  if (typeof window !== 'undefined') {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    })
  }
  threadId.value = loadStoredThreadId()
  if (threadId.value) {
    try {
      const res = await fetchChatHistory(threadId.value)
      const history = normalizeHistory(res?.data)
      if (history.length) {
        hasSent.value = true
        messages.value = history.map((item) => ({
          role: item.role === 'user' || item.role === 'human' ? 'user' : 'assistant',
          content: item.role === 'assistant' || item.role === 'ai'
            ? formatReply(item.content || item.text || '')
            : (item.content || item.text || '')
        }))
        scrollToBottom()
      }
    } catch { /* ignore */ }
  }
})

onBeforeUnmount(() => {
  stopVoiceRecognizer()
})
</script>

<style scoped>
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
  top: 56px; /* align under topbar more-vertical btn */
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
