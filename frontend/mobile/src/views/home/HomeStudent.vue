<template>
  <div class="pad-main pad-home pad-home--expand-search pad-home--elevated">
    <div class="pad-home__header-shell">
      <div class="pad-home__header">
        <header class="pad-home__topbar">
          <PageBackButton />
          <router-link to="/home" class="pad-home__brand" aria-label="宝山小实验社区">
            <span class="pad-home__topbar-icon pad-home__brand-mark" aria-hidden="true">
              <i data-lucide="flask-conical" class="icon"></i>
            </span>
            <span class="pad-home__brand-text">宝山小实验社区</span>
          </router-link>
          <div class="pad-home__topbar-actions">
            <router-link to="/notifications" class="pad-home__bell" aria-label="消息通知">
              <i data-lucide="bell" class="icon"></i>
              <span v-if="unreadCount > 0" class="pad-home__bell-badge" data-notifications-badge>
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </span>
            </router-link>
            <router-link to="/search" class="pad-home__topbar-search-btn" aria-label="搜索">
              <i data-lucide="search" class="icon"></i>
            </router-link>
          </div>
        </header>
      </div>

      <div class="pad-home__filters scroll-x">
        <button
          v-for="s in gradeFilters"
          :key="'f-' + s.key"
          type="button"
          class="chip"
          :class="{ active: activeGradeKey === s.key }"
          @click="switchGrade(s.key)"
        >{{ s.label }}</button>
      </div>
      <div class="pad-home__mobile-chips scroll-x">
        <button
          v-for="s in gradeFilters"
          :key="'m-' + s.key"
          type="button"
          class="chip"
          :class="{ active: activeGradeKey === s.key }"
          @click="switchGrade(s.key)"
        >{{ s.label }}</button>
      </div>
    </div>

    <div class="pad-home__body">
      <div class="pad-home__main pt-3 pb-28">
        <div v-if="loading" class="px-4 stack-3">
          <div v-for="i in 3" :key="i" class="skeleton" style="height:180px;border-radius:var(--radius-lg)"></div>
        </div>

        <div v-else-if="items.length === 0" class="empty-state py-8">
          <i data-lucide="flask-conical" class="icon icon-xl muted-2"></i>
          <p class="muted-2 mt-3">{{ emptyHint }}</p>
        </div>

        <div v-else class="waterfall-grid home-feed px-4">
          <FeedCard
            v-for="(item, idx) in items"
            :key="item.id"
            :item="item"
            :index="idx"
          />
        </div>

        <div v-if="hasMore && !loading" class="text-center py-4">
          <button type="button" class="btn btn-ghost btn-sm" @click="loadMore" :disabled="loadingMore">
            {{ loadingMore ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { fetchHomeBootstrap, fetchHomeFeed } from '@/api/home'
import { GRADE_FILTERS, gradeEmptyHint } from '@/utils/gradeFilters'
import FeedCard from '@/components/FeedCard.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { useLucideIcons } from '@/composables/useLucideIcons'

const emit = defineEmits(['notice-loaded'])

const appStore = useAppStore()
appStore.setActiveTab('home')

const items = ref([])
const gradeFilters = ref(GRADE_FILTERS)
const activeGradeKey = ref('all')
const emptyHint = computed(() => gradeEmptyHint(activeGradeKey.value))
const unreadCount = ref(0)

const page = ref(1)
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(false)

const PAGE_SIZE = 12

const { initIcons } = useLucideIcons()

async function loadBootstrap() {
  loading.value = true
  page.value = 1
  try {
    const res = await fetchHomeBootstrap({
      gradeKey: activeGradeKey.value,
      size: PAGE_SIZE
    })
    if (res?.data) {
      applyFeedData(res.data.feed)
      unreadCount.value = Number(res.data.unreadCount || 0)
      if (res.data.notice) emit('notice-loaded', res.data.notice)
    }
  } catch (e) {
    console.warn('加载首页失败', e)
    await loadFeed(false)
  } finally {
    loading.value = false
    initIcons()
  }
}

function applyFeedData(feed) {
  if (!feed) return
  const records = feed.records || []
  hasMore.value = records.length >= PAGE_SIZE
  items.value = records
}

async function loadFeed(isLoadMore = false) {
  if (!isLoadMore) {
    loading.value = true
    page.value = 1
  } else {
    loadingMore.value = true
  }
  try {
    const res = await fetchHomeFeed({
      gradeKey: activeGradeKey.value,
      page: page.value,
      size: PAGE_SIZE
    })
    if (res?.data) {
      const records = res.data.records || []
      hasMore.value = records.length >= PAGE_SIZE
      items.value = isLoadMore ? [...items.value, ...records] : records
    }
  } catch (e) {
    console.warn('加载首页信息流失败', e)
  } finally {
    loading.value = false
    loadingMore.value = false
    initIcons()
  }
}

function switchGrade(key) {
  if (activeGradeKey.value === key) return
  activeGradeKey.value = key
  loadFeed()
}

function loadMore() {
  page.value++
  loadFeed(true)
}

onMounted(() => {
  loadBootstrap()
})
</script>
