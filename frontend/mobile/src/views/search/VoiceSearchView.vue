<template>
  <div class="prototype-container pad-shell theme-primary" data-layout="voice-search">
    <div class="voice-search-page" data-voice-search-page>
      <div class="voice-search-page__top">
        <router-link :to="closeLink" class="voice-search-page__close" aria-label="关闭">
          <i data-lucide="x" class="icon"></i>
        </router-link>
      </div>

      <p v-if="listening" class="voice-search-page__listen-tip">正在听，请说话！</p>

      <main class="voice-search-page__main">
        <div class="voice-search-page__content">
          <p v-if="statusText" class="voice-search-page__status">{{ statusText }}</p>
          <p class="voice-search-page__transcript" aria-live="polite">{{ transcript }}</p>
          <p class="voice-search-page__hint">{{ hintText }}</p>
        </div>

        <div class="voice-search-page__hero" :class="{ 'is-done': done, 'is-retry': retry }">
          <div class="voice-search-page__mic-block">
            <div class="voice-search-page__mic-row">
              <button
                type="button"
                class="voice-search-page__pulse"
                :class="{ 'is-done': done, 'is-retry': retry }"
                aria-label="麦克风"
                @click="startListen"
              >
                <span class="voice-search-page__pulse-ring"></span>
                <span class="voice-search-page__pulse-ring"></span>
                <span class="voice-search-page__pulse-ring"></span>
                <span class="voice-search-page__pulse-core">
                  <i data-lucide="mic" class="icon"></i>
                </span>
              </button>
              <div class="voice-search-page__waves" aria-hidden="true">
                <span v-for="i in 5" :key="i" class="voice-search-page__wave-bar"></span>
              </div>
            </div>
            <p v-if="retry" class="voice-search-page__retry-tip">点击麦克风重试</p>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createSpeechRecognizer, isSpeechRecognitionSupported } from '@/utils/speechRecognition'

const route = useRoute()
const router = useRouter()

const listening = ref(false)
const done = ref(false)
const retry = ref(false)
const transcript = ref('')
const statusText = ref('')
const hintText = ref('识别完成后将自动返回搜索并展示结果')

const returnPath = computed(() => {
  const r = route.query.return
  return typeof r === 'string' && r.startsWith('/') ? r : '/search'
})

const closeLink = computed(() => returnPath.value)

let recognizer = null

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

function stopRecognizer() {
  try {
    recognizer?.stop?.()
  } catch { /* ignore */ }
  recognizer = null
}

function finishWithQuery(q) {
  if (!q) {
    statusText.value = '未识别到内容'
    hintText.value = '请靠近麦克风后重试'
    listening.value = false
    retry.value = true
    return
  }
  transcript.value = q
  statusText.value = '识别完成'
  listening.value = false
  done.value = true
  setTimeout(() => {
    router.replace({ path: returnPath.value, query: { q } })
  }, 800)
}

function startListen() {
  stopRecognizer()
  listening.value = true
  done.value = false
  retry.value = false
  statusText.value = ''
  transcript.value = ''
  hintText.value = '识别完成后将自动返回搜索并展示结果'

  if (!isSpeechRecognitionSupported()) {
    statusText.value = '当前浏览器不支持语音输入'
    hintText.value = '请返回搜索页手动输入关键词'
    listening.value = false
    retry.value = true
    return
  }

  recognizer = createSpeechRecognizer({
    onResult(text) {
      finishWithQuery(text)
    },
    onError(err) {
      statusText.value = err === 'not-allowed' ? '请允许麦克风权限' : '语音识别失败'
      hintText.value = '点击麦克风重试'
      listening.value = false
      retry.value = true
    },
    onEnd() {
      if (listening.value && !done.value) {
        listening.value = false
        if (!transcript.value) retry.value = true
      }
    }
  })

  if (!recognizer) {
    statusText.value = '无法启动语音识别'
    listening.value = false
    retry.value = true
    return
  }

  try {
    recognizer.start()
  } catch {
    statusText.value = '无法启动语音识别'
    listening.value = false
    retry.value = true
  }
}

onMounted(() => {
  initIcons()
  startListen()
})

onBeforeUnmount(() => {
  stopRecognizer()
})
</script>
