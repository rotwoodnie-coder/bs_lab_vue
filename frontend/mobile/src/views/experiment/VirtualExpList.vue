<template>
  <div class="pad-main pad-home pad-home--expand-search" data-exp-list>
    <div class="pad-home__header-shell">
      <div class="pad-home__header">
        <header class="pad-home__topbar">
          <PageBackButton fallback="/home" />
          <h1 class="pad-home__topbar-title flex-1 min-w-0">模拟实验</h1>
          <div class="pad-home__topbar-actions">
            <router-link to="/experiments/search" class="pad-home__topbar-search-btn" aria-label="搜索">
              <i data-lucide="search" class="icon"></i>
            </router-link>
          </div>
        </header>
      </div>

      <div class="pad-home__filters scroll-x">
        <button
          v-for="s in SUBJECT_FILTERS"
          :key="'f-' + s.key"
          type="button"
          class="chip"
          :class="{ active: activeSubject === s.key }"
          @click="switchSubject(s.key)"
        >{{ s.label }}</button>
      </div>
      <div class="scroll-x pad-home__mobile-chips">
        <button
          v-for="s in SUBJECT_FILTERS"
          :key="'m-' + s.key"
          type="button"
          class="chip"
          :class="{ active: activeSubject === s.key }"
          @click="switchSubject(s.key)"
        >{{ s.label }}</button>
      </div>
    </div>

    <div class="pad-home__body">
      <div class="pad-home__main pt-3 pb-28">
        <div v-if="loading" class="px-4 stack-3">
          <div v-for="i in 4" :key="i" class="skeleton" style="height:160px;border-radius:var(--radius-lg)"></div>
        </div>

        <div v-else class="waterfall-grid sim-feed px-4">
          <p v-if="filteredItems.length === 0" class="exp-empty">
            {{ emptyMessage }}
          </p>
          <SimCard
            v-for="(item, idx) in filteredItems"
            :key="item.simulatorId"
            :item="item"
            :index="idx"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { fetchSimulators } from '@/api/simulator'
import { fetchDictItems } from '@/api/dict'
import SimCard from '@/components/SimCard.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import {
  SUBJECT_FILTERS,
  buildSubjectMap,
  mapSimulatorItem
} from '@/utils/simulatorDisplay'

const appStore = useAppStore()
const route = useRoute()

appStore.setActiveTab('experiments')

const loading = ref(false)
const allItems = ref([])
const subjectMap = ref({})
const activeSubject = ref('all')

const filteredItems = computed(() => {
  if (activeSubject.value === 'all') return allItems.value
  return allItems.value.filter((item) => item.subjectName === activeSubject.value)
})

const emptyMessage = computed(() => {
  if (allItems.value.length === 0) return '暂无模拟实验'
  if (activeSubject.value !== 'all') return '该分类下暂无模拟实验'
  return '暂无符合条件的模拟实验'
})

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    }).catch(() => {})
  })
}

function resolveInitialSubject() {
  const param = route.query.subject
  if (!param || param === 'all') return 'all'
  const allowed = SUBJECT_FILTERS.some((s) => s.key === param)
  return allowed ? param : 'all'
}

async function loadSubjects() {
  try {
    const res = await fetchDictItems('data_school_subject')
    if (res && res.data) {
      subjectMap.value = buildSubjectMap(res.data)
    }
  } catch (e) {
    console.warn('加载学科字典失败', e)
  }
}

async function loadSimulators() {
  loading.value = true
  try {
    const res = await fetchSimulators({ pageNum: 1, pageSize: 200, status: 'y' })
    const records = (res && res.data && res.data.records) || []
    allItems.value = records.map((row, idx) => mapSimulatorItem(row, subjectMap.value, idx))
  } catch (e) {
    console.warn('加载模拟实验失败', e)
    allItems.value = []
  } finally {
    loading.value = false
    initIcons()
  }
}

function switchSubject(key) {
  if (activeSubject.value === key) return
  activeSubject.value = key
  initIcons()
}

watch(filteredItems, () => initIcons())

onMounted(async () => {
  activeSubject.value = resolveInitialSubject()
  await loadSubjects()
  await loadSimulators()
})
</script>
