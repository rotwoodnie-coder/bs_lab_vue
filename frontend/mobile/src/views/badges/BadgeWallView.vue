<template>
  <MobilePageShell class="theme-primary safe-top" data-layout="badge-wall">

    <!-- Pad -->
    <div class="pad-main pad-badges">
      <header class="pad-badges__topbar">
        <PageBackButton fallback="/profile" />
        <h1 class="pad-workbench__title">🏅 勋章墙</h1>
      </header>

      <div class="pad-badges__split">
        <aside class="pad-badges__rail" aria-label="勋章筛选">
          <button
            v-for="f in filters"
            :key="f.key"
            type="button"
            class="pad-badges__nav-item"
            :class="{ active: activeFilter === f.key }"
            @click="activeFilter = f.key"
          >
            <span>{{ f.label }}</span>
            <span class="pad-badges__nav-count">{{ filterCount(f.key) }}</span>
          </button>
        </aside>

        <div class="pad-badges__content">
          <div class="pad-badges__summary">
            <div class="pad-badges__summary-num">{{ badgeData.earned }}</div>
            <div class="pad-badges__summary-meta">
              <p class="text-sm">已解锁 <strong>{{ badgeData.earned }}</strong> 枚 · 共 <strong>{{ badgeData.total }}</strong> 枚勋章</p>
              <p v-if="badgeData.progressHint" class="pad-badges__hint text-xs mt-1">{{ badgeData.progressHint }}</p>
              <div class="progress">
                <div class="progress-fill progress-fill-warning" :style="{ width: progressWidth }"></div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="text-center text-sm muted py-8">加载中…</div>
          <div v-else-if="!filteredBadges.length" class="card card-pad text-center muted text-sm py-8">
            {{ emptyMessage }}
          </div>
          <BadgeWallGrid v-else :badges="filteredBadges" />
        </div>
      </div>
    </div>

    <!-- 手机竖屏（P19 mobile-only） -->
    <div class="pad-badges__mobile-only pb-28">
      <div class="topbar px-4">
        <div class="row items-center gap-3">
          <PageBackButton fallback="/profile" />
          <h1 class="topbar-title">🏅 我的勋章墙</h1>
        </div>
      </div>

      <div class="px-4 mt-3">
        <div class="scroll-x mb-3">
          <button
            v-for="f in filters"
            :key="'m-' + f.key"
            type="button"
            class="chip"
            :class="{ active: activeFilter === f.key }"
            @click="activeFilter = f.key"
          >{{ f.label }}</button>
        </div>

        <div class="banner banner-warm text-center rounded-xl py-6 px-4">
          <div class="text-5xl font-bold text-warning">{{ badgeData.earned }}</div>
          <div class="banner-sub">枚勋章已获得 · 共 {{ badgeData.total }} 枚</div>
          <p v-if="badgeData.progressHint" class="text-xs muted mt-2">{{ badgeData.progressHint }}</p>
          <div class="progress mt-3 mx-auto" style="max-width:280px">
            <div class="progress-fill progress-fill-warning" :style="{ width: progressWidth }"></div>
          </div>
        </div>
      </div>

      <div class="px-4 py-4">
        <div v-if="loading" class="text-center text-sm muted py-8">加载中…</div>
        <div v-else-if="!filteredBadges.length" class="card card-pad text-center muted text-sm py-8">
          {{ emptyMessage }}
        </div>
        <BadgeWallGrid v-else :badges="filteredBadges" mobile />
      </div>
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import BadgeWallGrid from '@/components/BadgeWallGrid.vue'
import { fetchBadges } from '@/api/badge'
import { mergeBadgeWallResponse } from '@/utils/badgeWall'
import { useLucideIcons } from '@/composables/useLucideIcons'

const badgeData = ref({ items: [], earned: 0, total: 0, progressHint: '', progressPercent: 0 })
const loading = ref(true)
const filters = [
  { key: 'all', label: '全部' },
  { key: 'earned', label: '已获得' },
  { key: 'locked', label: '未获得' }
]
const activeFilter = ref('all')

const progressWidth = computed(() => {
  const p = Math.min(100, Math.max(0, Number(badgeData.value.progressPercent) || 0))
  return `${p}%`
})

const filteredBadges = computed(() => {
  const items = badgeData.value.items || []
  if (activeFilter.value === 'earned') return items.filter((b) => b.earned)
  if (activeFilter.value === 'locked') return items.filter((b) => !b.earned)
  return items
})

function filterCount(key) {
  const items = badgeData.value.items || []
  if (key === 'earned') return items.filter((b) => b.earned).length
  if (key === 'locked') return items.filter((b) => !b.earned).length
  return badgeData.value.total || items.length
}

const emptyMessage = computed(() => {
  if (badgeData.value.total === 0) {
    return '暂无勋章规则，请联系老师在管理端配置'
  }
  if (activeFilter.value === 'earned') return '还没有获得勋章，快去完成任务吧'
  if (activeFilter.value === 'locked') return '恭喜！你已解锁全部勋章'
  return '暂无勋章数据'
})

const { initIcons } = useLucideIcons()

onMounted(async () => {
  try {
    const res = await fetchBadges()
    badgeData.value = mergeBadgeWallResponse(res)
  } catch {
    badgeData.value = mergeBadgeWallResponse(null)
  } finally {
    loading.value = false
  }
  initIcons()
})
</script>
