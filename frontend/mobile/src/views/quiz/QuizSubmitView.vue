<template>

  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail">

    <div class="topbar page-topbar safe-top">

      <PageBackButton fallback="/quiz" />

      <h1 class="topbar-title">🧠 每日答题</h1>

      <span class="badge badge-success shrink-0">{{ answeredCount }}/{{ totalQuestions }} 已选</span>

    </div>



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



    <BottomNav />

  </div>

</template>



<script setup>

import { ref, computed, onMounted, nextTick } from 'vue'

import { useRouter } from 'vue-router'

import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'

import { submitQuiz } from '@/api/quiz'



const QUIZ_DRAFT_KEY = 'mb_quiz_draft'

const QUIZ_RESULT_KEY = 'mb_quiz_result'



const router = useRouter()

const draft = ref({ answers: [], practice: false })

const submitting = ref(false)



const totalQuestions = computed(() => draft.value.answers?.length || 0)

const answeredCount = computed(() => draft.value.answers.filter((a) => a !== null && a !== undefined).length)

const progressPercent = computed(() => {

  if (!totalQuestions.value) return 0

  return Math.round((answeredCount.value / totalQuestions.value) * 100)

})



async function submitQuizNow() {

  if (submitting.value || answeredCount.value < totalQuestions.value) {

    alert('请完成全部题目后再提交')

    return

  }

  submitting.value = true

  try {

    const res = await submitQuiz({

      answers: draft.value.answers,

      practice: draft.value.practice

    })

    if (res?.code === 200 && res.data) {

      sessionStorage.setItem(QUIZ_RESULT_KEY, JSON.stringify(res.data))

      sessionStorage.removeItem(QUIZ_DRAFT_KEY)

      router.push(`/quiz/result/${res.data.resultType || 'low'}`)

      return

    }

    if (res?.code === 409) {

      alert(res.message || '今日已提交过答卷')

      router.replace('/quiz/completed')

      return

    }

    alert(res?.message || '提交失败')

  } catch (e) {

    if (e?.response?.status === 409) {

      router.replace('/quiz/completed')

      return

    }

    alert(e?.response?.data?.message || '提交失败，请稍后重试')

  } finally {

    submitting.value = false

  }

}



function initIcons() {

  nextTick(() => {

    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})

  })

}



onMounted(() => {

  try {

    const raw = sessionStorage.getItem(QUIZ_DRAFT_KEY)

    if (raw) draft.value = JSON.parse(raw)

  } catch { /* ignore */ }

  const answers = draft.value.answers || []

  const allAnswered = answers.length > 0 && answers.every((a) => a !== null && a !== undefined)

  if (!allAnswered) {

    router.replace('/quiz')

    return

  }

  initIcons()

})

</script>

