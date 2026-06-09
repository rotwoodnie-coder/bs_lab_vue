<template>

  <div class="prototype-container pad-shell safe-top" data-layout="detail">

    <div class="topbar page-topbar safe-top">

      <PageBackButton :fallback="backLink" />

      <h1 class="topbar-title">错题解析</h1>

    </div>



    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>

    <div v-else-if="error" class="px-4 py-12 text-center stack-3">

      <p class="muted-2">{{ error }}</p>

      <router-link to="/quiz/history" class="btn btn-ghost btn-sm">返回答题记录</router-link>

    </div>



    <div v-else-if="items.length" class="px-4 pb-28 stack-4">

      <div v-for="(review, idx) in items" :key="idx" class="card card-pad">

        <div class="row items-center gap-2 mb-2">

          <span class="badge badge-danger">错题</span>

          <span v-if="review.questionNo" class="badge badge-slate">第 {{ review.questionNo }} 题</span>

          <span v-if="review.meta" class="badge badge-slate">{{ review.meta }}</span>

          <span v-if="review.date" class="text-xs muted ml-auto">{{ review.date }}</span>

        </div>

        <p class="text-sm font-bold">{{ review.title }}</p>

        <p class="text-xs muted mt-3">你的答案：{{ review.userAnswerText || '未作答' }}</p>

        <p class="text-xs mt-2 text-success">正确答案：{{ review.correctAnswerText }}</p>

        <p v-if="review.meta" class="text-xs muted mt-3">知识点：{{ review.meta }}</p>

      </div>

    </div>

  </div>

</template>



<script setup>

import { computed, onMounted, nextTick, ref } from 'vue'

import { useRoute } from 'vue-router'

import { fetchQuizReview } from '@/api/quiz'
import PageBackButton from '@/components/PageBackButton.vue'



const route = useRoute()

const loading = ref(true)

const error = ref('')

const items = ref([])



const recordDate = computed(() => (typeof route.query.date === 'string' ? route.query.date : ''))



const backLink = computed(() => {

  const date = recordDate.value

  return date ? `/quiz/result/low?date=${date}` : '/quiz/history'

})



function initIcons() {

  nextTick(() => {

    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})

  })

}



onMounted(async () => {

  try {

    const res = await fetchQuizReview(recordDate.value || undefined)

    if (res?.code === 200 && res.data) {

      items.value = Array.isArray(res.data) ? res.data : [res.data]

      return

    }

    error.value = res?.message || '暂无错题解析'

  } catch {

    error.value = '加载失败，请稍后重试'

  } finally {

    loading.value = false

    initIcons()

  }

})

</script>

