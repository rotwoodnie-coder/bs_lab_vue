<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail">
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/quiz" />
      <h1 class="topbar-title">🧠 每日答题</h1>
      <span class="badge badge-success shrink-0">{{ answeredCount }}/{{ totalQuestions }} 已选</span>
    </div>

    <div v-if="pageLoading" class="px-4 py-12 text-center muted-2">加载中…</div>

    <template v-else>
      <div class="px-4 mt-3">
        <div class="card card-pad quiz-nudge">
          <div class="row items-center justify-between gap-2 mb-2">
            <span class="text-sm font-bold">今日 {{ totalQuestions }} 题</span>
            <span class="badge badge-success">{{ answeredCount }} / {{ totalQuestions }} 题已选</span>
          </div>
          <div class="progress"><div class="progress-fill progress-fill-success" :style="{ width: progressPercent + '%' }"></div></div>
        </div>
      </div>

      <div class="px-4 mt-2">
        <p class="quiz-tip-banner">✅ 已选择全部答案，确认无误后提交计分。</p>
      </div>

      <div class="px-4 mt-3 pb-28">
        <div class="card card-pad">
          <p class="text-sm muted mb-4">今日共 {{ totalQuestions }} 题，提交后将写入正式答题记录。</p>
          <div class="row gap-2">
            <router-link to="/quiz" class="btn btn-outline flex-1">返回修改</router-link>
            <button type="button" class="btn btn-gradient flex-1" :disabled="submitting" @click="submitQuizNow">
              {{ submitting ? '提交中…' : '提交答卷' }}
            </button>
          </div>
        </div>
      </div>
    </template>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchTodayQuiz, fetchQuizRecord, submitQuiz } from '@/api/quiz'
import { useLucideIcons } from '@/composables/useLucideIcons'

const QUIZ_DRAFT_KEY = 'mb_quiz_draft'
const QUIZ_RESULT_KEY = 'mb_quiz_result'

const router = useRouter()
const draft = ref({ answers: [], practice: false })
const submitting = ref(false)
const pageLoading = ref(true)

const totalQuestions = computed(() => draft.value.answers?.length || 0)
const answeredCount = computed(() => draft.value.answers.filter((a) => a !== null && a !== undefined).length)
const progressPercent = computed(() => {
  if (!totalQuestions.value) return 0
  return Math.round((answeredCount.value / totalQuestions.value) * 100)
})

function todayResultToSubmitShape(todayResult) {
  if (!todayResult) return null
  return {
    score: todayResult.score,
    total: todayResult.total,
    points: todayResult.points,
    perfect: todayResult.perfect,
    resultType: todayResult.resultType || (todayResult.perfect ? 'perfect' : 'low'),
    streakDays: todayResult.streakDays
  }
}

function navigateToSuccess(data, { replace = true } = {}) {
  if (!data) return false
  sessionStorage.setItem(QUIZ_RESULT_KEY, JSON.stringify(data))
  sessionStorage.removeItem(QUIZ_DRAFT_KEY)
  const path = `/quiz/result/${data.resultType || (data.perfect ? 'perfect' : 'low')}`
  if (replace) {
    router.replace(path)
  } else {
    router.push(path)
  }
  return true
}

async function recoverExistingSubmission() {
  try {
    const todayRes = await fetchTodayQuiz()
    if (todayRes?.code === 200 && todayRes.data?.submittedToday && todayRes.data.todayResult) {
      return todayResultToSubmitShape(todayRes.data.todayResult)
    }
  } catch { /* ignore */ }
  try {
    const recordRes = await fetchQuizRecord()
    if (recordRes?.code === 200 && recordRes.data) {
      return todayResultToSubmitShape(recordRes.data)
    }
  } catch { /* ignore */ }
  return null
}

async function redirectIfAlreadySubmitted() {
  if (draft.value.practice) return false
  const recovered = await recoverExistingSubmission()
  if (recovered) {
    navigateToSuccess(recovered)
    return true
  }
  return false
}

async function submitQuizNow() {
  if (submitting.value || answeredCount.value < totalQuestions.value) {
    alert('请完成全部题目后再提交')
    return
  }
  if (draft.value.practice) {
    alert('请从练习模式页面提交')
    router.replace('/quiz?mode=practice')
    return
  }

  submitting.value = true
  try {
    const res = await submitQuiz({
      answers: draft.value.answers,
      practice: false
    })
    if (res?.code === 200 && res.data) {
      navigateToSuccess(res.data)
      return
    }
    if (res?.code === 409) {
      const recovered = await recoverExistingSubmission()
      if (recovered) {
        navigateToSuccess(recovered)
        return
      }
      router.replace('/quiz/completed')
      return
    }
    alert(res?.message || '提交失败')
  } catch (e) {
    if (e?.response?.status === 409) {
      const recovered = await recoverExistingSubmission()
      if (recovered) {
        navigateToSuccess(recovered)
        return
      }
      router.replace('/quiz/completed')
      return
    }
    alert(e?.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const { initIcons } = useLucideIcons()

onMounted(async () => {
  try {
    const raw = sessionStorage.getItem(QUIZ_DRAFT_KEY)
    if (raw) draft.value = JSON.parse(raw)
  } catch { /* ignore */ }

  if (draft.value.practice) {
    router.replace('/quiz?mode=practice')
    return
  }

  const answers = draft.value.answers || []
  const allAnswered = answers.length > 0 && answers.every((a) => a !== null && a !== undefined)
  if (!allAnswered) {
    router.replace('/quiz')
    return
  }

  if (await redirectIfAlreadySubmitted()) {
    return
  }

  pageLoading.value = false
  initIcons()
})
</script>
