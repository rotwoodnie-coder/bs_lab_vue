<template>
  <div class="prototype-container pad-shell safe-top" data-layout="detail">
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/profile" />
      <h1 class="topbar-title">积分明细</h1>
    </div>

    <div class="px-4 pt-3">
      <div class="card card-pad row items-center justify-between">
        <div>
          <div class="text-xs muted">当前积分</div>
          <div class="stat-num text-success mt-1">{{ totalPoints }}</div>
        </div>
        <i data-lucide="sparkles" class="icon icon-lg" style="color:var(--c-orange-600);"></i>
      </div>
    </div>

    <div v-if="loading && list.length === 0" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else class="px-4 pb-28 pt-3 stack-2">
      <p v-if="list.length === 0" class="text-center py-12 muted-2">暂无积分记录</p>
      <div
        v-for="item in list"
        :key="item.id"
        class="card card-pad row items-center justify-between"
      >
        <div class="min-w-0">
          <div class="text-sm font-bold">{{ item.sourceTypeLabel }}</div>
          <div v-if="item.remark" class="text-xs muted mt-1 truncate">{{ item.remark }}</div>
          <div class="text-xs muted-2 mt-1">{{ item.time }}</div>
        </div>
        <div class="text-right shrink-0 ml-3">
          <div class="text-base font-bold" :class="item.delta >= 0 ? 'text-success' : 'text-danger'">
            {{ item.delta >= 0 ? '+' : '' }}{{ item.delta }}
          </div>
          <div v-if="item.balanceAfter != null" class="text-xs muted-2 mt-1">余额 {{ item.balanceAfter }}</div>
        </div>
      </div>

      <button
        v-if="hasMore"
        type="button"
        class="btn btn-outline btn-block mt-2"
        :disabled="loading"
        @click="loadMore"
      >{{ loading ? '加载中…' : '加载更多' }}</button>
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchPointsLedger } from '@/api/points'
import { useProfileStore } from '@/stores/profile'
import { useLucideIcons } from '@/composables/useLucideIcons'

const PAGE_SIZE = 20

const profileStore = useProfileStore()
const loading = ref(false)
const list = ref([])
const page = ref(1)
const total = ref(0)

const totalPoints = computed(() => Number(profileStore.profile?.perScore ?? 0))
const hasMore = computed(() => list.value.length < total.value)

const { initIcons } = useLucideIcons()

async function load(targetPage) {
  loading.value = true
  try {
    const res = await fetchPointsLedger({ page: targetPage, size: PAGE_SIZE })
    if (res?.code === 200 && res.data) {
      const records = res.data.records || []
      total.value = Number(res.data.total || 0)
      list.value = targetPage === 1 ? records : list.value.concat(records)
      page.value = targetPage
    }
  } catch {
    if (targetPage === 1) list.value = []
  } finally {
    loading.value = false
    nextTick(() => initIcons())
  }
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  load(page.value + 1)
}

onMounted(async () => {
  try {
    await profileStore.loadProfile()
  } catch { /* ignore */ }
  await load(1)
})
</script>
