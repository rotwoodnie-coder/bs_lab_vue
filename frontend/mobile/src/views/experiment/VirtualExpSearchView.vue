<template>
  <div class="prototype-container pad-shell theme-primary" data-layout="search-home">
    <div class="pad-main search-page" data-search-page data-search-scope="exp">
      <header class="search-page__bar">
        <router-link to="/experiments" class="search-page__back" aria-label="返回模拟实验">
          <i data-lucide="arrow-left" class="icon"></i>
        </router-link>
        <div class="search-page__field">
          <form class="search-page__form" @submit.prevent="submitSearch">
            <div class="input-group" data-input-tools="off">
              <i data-lucide="search" class="icon"></i>
              <input
                ref="inputRef"
                v-model="query"
                type="search"
                inputmode="search"
                autocomplete="off"
                placeholder="搜索模拟实验…"
                class="input"
                aria-label="搜索"
                @input="onInput"
              />
            </div>
          </form>
        </div>
        <router-link
          :to="{ path: '/search/voice', query: { return: '/experiments/search' } }"
          class="search-page__mic"
          aria-label="语音搜索"
        >
          <i data-lucide="mic" class="icon"></i>
        </router-link>
      </header>

      <div class="search-page__body">
        <div v-if="!showResults">
          <section class="search-page__section">
            <h2 class="search-page__section-title">最近搜索</h2>
            <ul v-if="recentList.length" class="search-page__recent">
              <li v-for="item in recentList" :key="item" class="search-page__recent-item">
                <button type="button" class="search-page__recent-btn" @click="searchWith(item)">
                  <i data-lucide="clock" class="icon"></i>
                  <span>{{ item }}</span>
                </button>
                <button type="button" class="search-page__recent-remove" aria-label="删除" @click="removeRecent(item)">
                  <i data-lucide="x" class="icon"></i>
                </button>
              </li>
            </ul>
            <p v-else class="search-page__empty-hint">暂无最近搜索</p>
          </section>

          <section v-if="hotKeywords.length" class="search-page__section">
            <h2 class="search-page__section-title">猜你想搜</h2>
            <ul class="search-page__suggest">
              <li v-for="item in hotKeywords" :key="item">
                <button type="button" class="search-page__suggest-btn" @click="searchWith(item)">
                  <i data-lucide="search" class="icon"></i>
                  <span>{{ item }}</span>
                </button>
              </li>
            </ul>
          </section>
        </div>

        <section v-else class="search-page__section">
          <p class="search-page__results-meta">
            {{ searching ? '搜索中…' : `找到 ${resultItems.length} 个结果` }}
          </p>
          <div v-if="!searching && resultItems.length === 0" class="search-page__empty-hint py-6">
            未找到相关模拟实验
          </div>
          <div v-else class="search-page__results-grid">
            <SimCard
              v-for="(item, idx) in resultItems"
              :key="item.simulatorId"
              :item="item"
              :index="idx"
            />
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import SimCard from '@/components/SimCard.vue'
import { fetchSimulators } from '@/api/simulator'
import { fetchDictItems } from '@/api/dict'
import {
  buildSubjectMap,
  mapSimulatorItem,
  loadExpSearchRecent,
  saveExpSearchRecent,
  removeExpSearchRecent
} from '@/utils/simulatorDisplay'

const route = useRoute()

const inputRef = ref(null)
const query = ref('')
const searching = ref(false)
const hasSearched = ref(false)
const resultItems = ref([])
const recentList = ref([])
const hotKeywords = ref([])
const subjectMap = ref({})

const showResults = computed(() => hasSearched.value && query.value.trim().length > 0)

let debounceTimer = null

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    }).catch(() => {})
  })
}

function refreshRecent() {
  recentList.value = loadExpSearchRecent()
}

function removeRecent(keyword) {
  removeExpSearchRecent(keyword)
  refreshRecent()
  initIcons()
}

async function runSearch(keyword) {
  const trimmed = (keyword || '').trim()
  if (!trimmed) {
    hasSearched.value = false
    resultItems.value = []
    return
  }

  searching.value = true
  hasSearched.value = true
  try {
    const res = await fetchSimulators({ keyword: trimmed, pageNum: 1, pageSize: 50, status: 'y' })
    const records = (res && res.data && res.data.records) || []
    resultItems.value = records.map((row, idx) => mapSimulatorItem(row, subjectMap.value, idx))
    saveExpSearchRecent(trimmed)
    refreshRecent()
  } catch (e) {
    console.warn('搜索模拟实验失败', e)
    resultItems.value = []
  } finally {
    searching.value = false
    initIcons()
  }
}

function searchWith(keyword) {
  query.value = keyword
  runSearch(keyword)
}

function submitSearch() {
  runSearch(query.value)
}

function onInput() {
  clearTimeout(debounceTimer)
  const trimmed = query.value.trim()
  if (!trimmed) {
    hasSearched.value = false
    resultItems.value = []
    return
  }
  debounceTimer = setTimeout(() => runSearch(trimmed), 350)
}

onMounted(async () => {
  refreshRecent()
  try {
    const [dictRes, simRes] = await Promise.all([
      fetchDictItems('data_school_subject'),
      fetchSimulators({ pageNum: 1, pageSize: 8, status: 'y' })
    ])
    if (dictRes?.data) {
      subjectMap.value = buildSubjectMap(dictRes.data)
    }
    const records = simRes?.data?.records || []
    hotKeywords.value = records.map((r) => r.simulatorName).filter(Boolean)
  } catch (e) {
    console.warn('加载学科字典失败', e)
  }

  const initialQ = route.query.q
  if (initialQ) {
    query.value = initialQ
    await runSearch(initialQ)
  } else {
    initIcons()
    inputRef.value?.focus()
  }
})
</script>
