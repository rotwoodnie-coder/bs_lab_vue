<template>
  <div class="prototype-container pad-shell safe-top theme-primary" data-layout="detail">
    <BottomNav />
    <div class="topbar safe-top">
      <PageBackButton fallback="/admin" />
      <div class="flex-1 min-w-0">
        <h1 class="topbar-title">学生作品审核</h1>
      </div>
      <div class="row items-center gap-1 shrink-0">
        <span class="text-sm font-bold text-brand">{{ pending.length }}</span>
        <span class="text-xs muted">待审</span>
      </div>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="loadError" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ loadError }}</p>
      <button type="button" class="btn btn-primary btn-sm" @click="loadQueue">重试</button>
    </div>
    <div v-else-if="pending.length === 0" class="px-4 py-12 text-center pb-28">
      <div class="text-2xl mb-2">🎉</div>
      <p class="text-sm font-bold">暂无待审核作品</p>
      <p class="text-xs muted mt-2">学生提交的作品审核通过后将展示在首页</p>
    </div>

    <div v-else class="px-4 py-4 stack-4 pb-28">
      <div v-for="item in pending" :key="item.id" class="work-card card">
        <div class="card-body stack-3">
          <div class="row items-center gap-3">
            <UserAvatar
              size="sm"
              :name="item.studentName"
              role="student"
            />
            <div class="flex-1 min-w-0">
              <div class="text-base font-bold truncate">{{ item.studentName }} · {{ item.title }}</div>
              <div class="text-xs muted">
                提交于 {{ item.time }}
                <span v-if="item.workTypeLabel"> · {{ item.workTypeLabel }}</span>
                <span v-if="item.groupLabel"> · {{ item.groupLabel }}</span>
              </div>
            </div>
            <span class="badge badge-warning text-xs shrink-0">待审核</span>
          </div>

          <router-link :to="`/works/${item.id}`" class="text-xs text-brand">查看作品详情 →</router-link>

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
            <p class="text-xs muted mt-2">合格及以上将审核通过并展示到首页；选择「不合格」则驳回。</p>
          </div>

          <div>
            <div class="text-xs font-medium muted mb-2">评语（可选）</div>
            <textarea v-model="item.comment" class="textarea" rows="2" placeholder="输入评语…"></textarea>
          </div>

          <button
            type="button"
            class="btn btn-primary btn-block text-sm"
            :disabled="item.submitting || !item.rating"
            @click="submitReview(item)"
          >
            <i data-lucide="send" class="icon"></i>
            {{ item.submitting ? '提交中…' : '提交审核' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { fetchAdminWorkReviews, submitAdminWorkReview } from '@/api/admin'
import { useAppStore } from '@/stores/app'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { showToast } from '@/utils/toast'

const ratings = [
  { key: 'excellent', label: '优秀' },
  { key: 'good', label: '良好' },
  { key: 'pass', label: '合格' },
  { key: 'fail', label: '不合格' }
]

const appStore = useAppStore()
appStore.setActiveTab('admin')

const loading = ref(true)
const loadError = ref('')
const pending = ref([])

const { initIcons } = useLucideIcons()

async function loadQueue() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await fetchAdminWorkReviews({ page: 1, size: 50 })
    const records = res?.data?.records || []
    pending.value = records.map((row) => ({
      ...row,
      rating: '',
      comment: '',
      submitting: false
    }))
  } catch {
    loadError.value = '加载审核队列失败'
    pending.value = []
  } finally {
    loading.value = false
    nextTick(() => initIcons())
  }
}

async function submitReview(item) {
  if (!item.rating || item.submitting) return
  item.submitting = true
  try {
    const res = await submitAdminWorkReview(item.id, {
      result: item.rating,
      comment: item.comment?.trim() || ''
    })
    if (res?.code === 200) {
      showToast('审核已提交')
      pending.value = pending.value.filter((p) => p.id !== item.id)
      nextTick(() => initIcons())
      return
    }
    showToast(res?.message || '提交失败', 'danger')
  } catch {
    showToast('提交失败，请稍后重试', 'danger')
  } finally {
    item.submitting = false
  }
}

onMounted(loadQueue)
</script>
