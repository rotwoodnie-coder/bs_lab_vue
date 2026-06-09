<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail">
    <div class="topbar safe-top">
      <PageBackButton fallback="/home" />
      <div class="flex-1 min-w-0">
        <h1 class="topbar-title">✅ 审核批阅</h1>
      </div>
      <div class="row items-center gap-1">
        <span class="text-lg"><i data-lucide="clipboard-list" class="icon"></i></span>
        <span class="text-sm font-bold text-brand">{{ pending.length }}</span>
        <span class="text-xs muted">待批</span>
      </div>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="loadError" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ loadError }}</p>
      <button type="button" class="btn btn-primary btn-sm" @click="loadQueue">重试</button>
    </div>
    <div v-else-if="pending.length === 0" class="px-4 py-12 text-center">
      <div class="text-2xl mb-2">🎉</div>
      <p class="text-sm font-bold">全部批阅完成</p>
      <p class="text-xs muted mt-2">暂无待批作品</p>
    </div>

    <div v-else class="px-4 py-4 stack-4 pb-8">
      <div v-for="item in pending" :key="item.id" class="work-card card">
        <div class="card-body stack-3">
          <div class="row items-center gap-3">
            <div class="avatar" :class="item.avatarClass">{{ item.studentInitial }}</div>
            <div class="flex-1">
              <div class="text-base font-bold">{{ item.student }} · {{ item.title }}</div>
              <div class="text-xs muted">提交于 {{ item.time }}</div>
            </div>
            <span class="badge badge-warning text-xs">待批阅</span>
          </div>

          <div>
            <div class="text-xs font-medium muted mb-2">评级</div>
            <div class="row gap-2 flex-wrap">
              <button
                v-for="r in ratings"
                :key="r.key"
                type="button"
                class="rating-btn btn btn-sm"
                :class="item.rating === r.key ? 'btn-primary' : 'btn-outline'"
                @click="item.rating = r.key"
              >{{ r.label }}</button>
            </div>
          </div>

          <div>
            <div class="text-xs font-medium muted mb-2">评语</div>
            <textarea v-model="item.comment" class="textarea" rows="2" placeholder="输入评语..."></textarea>
          </div>

          <label v-if="item.rating === 'excellent'" class="row items-center gap-2 text-xs">
            <input v-model="item.featured" type="checkbox" />
            <span>展示到作品墙（创意之星）</span>
          </label>

          <button
            type="button"
            class="btn btn-primary btn-block text-sm"
            :disabled="item.submitting"
            @click="submitReview(item)"
          >
            <i data-lucide="send" class="icon"></i>
            {{ item.submitting ? '提交中…' : '提交批阅' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { fetchTeacherReviewQueue, submitTeacherReview } from '@/api/teacher'
import PageBackButton from '@/components/PageBackButton.vue'

const ratings = [
  { key: 'excellent', label: '优秀' },
  { key: 'good', label: '良好' },
  { key: 'pass', label: '合格' },
  { key: 'fail', label: '不合格' }
]

const loading = ref(true)
const loadError = ref('')
const pending = ref([])

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

async function loadQueue() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await fetchTeacherReviewQueue({ page: 1, size: 50 })
    const records = res?.data?.records || []
    pending.value = records.map((row) => ({
      ...row,
      rating: '',
      comment: '',
      featured: row.rating === 'excellent',
      submitting: false
    }))
  } catch (e) {
    loadError.value = '加载批阅队列失败'
    pending.value = []
  } finally {
    loading.value = false
    initIcons()
  }
}

async function submitReview(item) {
  if (!item.rating) {
    alert('请选择评级')
    return
  }
  item.submitting = true
  try {
    const res = await submitTeacherReview(item.id, {
      rating: item.rating,
      comment: item.comment || undefined,
      featured: item.rating === 'excellent' ? Boolean(item.featured) : false
    })
    if (res?.code === 200) {
      pending.value = pending.value.filter((p) => p.id !== item.id)
      initIcons()
      return
    }
    alert(res?.message || '批阅失败')
  } catch (e) {
    alert(e?.response?.data?.message || '批阅失败，请稍后重试')
  } finally {
    item.submitting = false
  }
}

onMounted(loadQueue)
</script>
