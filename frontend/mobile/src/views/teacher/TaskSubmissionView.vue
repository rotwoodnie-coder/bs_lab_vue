<template>
  <MobilePageShell class="theme-teacher safe-top safe-bottom" data-layout="detail">
    <div class="topbar safe-top">
      <PageBackButton :fallback="`/tasks/${taskId}/summary`" />
      <h1 class="topbar-title flex-1">提交详情</h1>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="error" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ error }}</p>
      <router-link :to="`/tasks/${taskId}/summary`" class="btn btn-primary btn-sm">返回</router-link>
    </div>

    <div v-else class="px-4 py-4 stack-4 pb-8">
      <WorkMediaViewer :files="work.files || []" />

      <div class="card card-pad stack-3">
        <div class="text-base font-bold">{{ work.title || '学生作品' }}</div>
        <div class="text-xs muted">{{ work.author }} · {{ work.className }} · {{ work.time }}</div>
        <p v-if="work.desc" class="text-sm">{{ work.desc }}</p>
        <div v-if="work.teacherReview" class="surface-2 rounded-xl p-3 text-sm stack-2">
          <div class="font-bold text-brand">已评价</div>
          <div>评级：{{ gradeLabel(work.grade) }}</div>
          <div v-if="work.teacherReview.text">评语：{{ work.teacherReview.text }}</div>
        </div>
      </div>

      <WorkReviewPanel
        v-if="canReview"
        v-model="reviewForm"
        :submitting="submitting"
        @submit="submitReview"
      />
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import WorkReviewPanel from '@/components/teacher/WorkReviewPanel.vue'
import WorkMediaViewer from '@/components/works/WorkMediaViewer.vue'
import { fetchTeacherWorkDetail, submitTeacherReview } from '@/api/teacher'
import { useLucideIcons } from '@/composables/useLucideIcons'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => String(route.params.taskId || ''))
const workId = computed(() => String(route.params.workId || ''))

const loading = ref(true)
const error = ref('')
const submitting = ref(false)
const work = ref({})
const reviewForm = ref({ rating: '', comment: '', featured: false })

const canReview = computed(() => !work.value.grade)

function gradeLabel(grade) {
  const map = { excellent: '优秀', good: '良好', pass: '合格', fail: '不合格' }
  return map[grade] || grade || '—'
}

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchTeacherWorkDetail(workId.value)
    if (res?.code === 200 && res.data) {
      work.value = res.data
      if (res.data.grade) {
        reviewForm.value.rating = res.data.grade
        reviewForm.value.comment = res.data.teacherReview?.text || ''
      }
    } else {
      error.value = res?.message || '加载失败'
    }
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
    initIcons()
  }
}

async function submitReview() {
  if (!reviewForm.value.rating) {
    alert('请选择评级')
    return
  }
  submitting.value = true
  try {
    const res = await submitTeacherReview(workId.value, {
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment || undefined,
      featured: reviewForm.value.rating === 'excellent' ? Boolean(reviewForm.value.featured) : false
    })
    if (res?.code === 200) {
      router.replace(`/tasks/${taskId.value}/summary`)
      return
    }
    alert(res?.message || '评价失败')
  } catch (e) {
    alert(e?.response?.data?.message || '评价失败')
  } finally {
    submitting.value = false
  }
}

const { initIcons } = useLucideIcons()

onMounted(loadDetail)
</script>
