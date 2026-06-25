<template>
  <div class="pad-main pad-home pad-home--expand-search" data-exp-list>
    <div class="pad-home__header-shell">
      <div class="pad-home__header">
        <header class="pad-home__topbar">
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
          v-for="s in GRADE_FILTERS"
          :key="'f-' + s.key"
          type="button"
          class="chip"
          :class="{ active: activeGradeKey === s.key }"
          @click="switchGrade(s.key)"
        >{{ s.label }}</button>
      </div>
      <div class="scroll-x pad-home__mobile-chips">
        <button
          v-for="s in GRADE_FILTERS"
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
          <div v-for="i in 4" :key="i" class="skeleton" style="height:160px;border-radius:var(--radius-lg)"></div>
        </div>

        <div v-else-if="items.length === 0" class="px-4">
          <p class="exp-empty">{{ emptyMessage }}</p>
        </div>

        <div v-else class="waterfall-grid sim-feed px-4">
          <SimCard
            v-for="(item, idx) in items"
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAppStore } from '@/stores/app'
import { fetchSimulators } from '@/api/simulator'
import { fetchDictItems } from '@/api/dict'
import SimCard from '@/components/SimCard.vue'
import { GRADE_FILTERS, gradeEmptyHint } from '@/utils/gradeFilters'
import { buildSubjectMap, mapSimulatorItem } from '@/utils/simulatorDisplay'
import { useLucideIcons } from '@/composables/useLucideIcons'

const appStore = useAppStore()
appStore.setActiveTab('experiments')

const loading = ref(false)
const items = ref([])
const subjectMap = ref({})
const activeGradeKey = ref('all')

const emptyMessage = computed(() => {
  if (activeGradeKey.value === 'all' && items.value.length === 0) return '暂无模拟实验'
  return gradeEmptyHint(activeGradeKey.value).replace('内容', '模拟实验')
})

const { initIcons } = useLucideIcons()

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
    const res = await fetchSimulators({
      pageNum: 1,
      pageSize: 200,
      status: 'y',
      gradeKey: activeGradeKey.value
    })
    const records = (res && res.data && res.data.records) || []
    items.value = records.map((row, idx) => mapSimulatorItem(row, subjectMap.value, idx))
  } catch (e) {
    console.warn('加载模拟实验失败', e)
    items.value = []
  } finally {
    loading.value = false
    initIcons()
  }
}

function switchGrade(key) {
  if (activeGradeKey.value === key) return
  activeGradeKey.value = key
  loadSimulators()
}

onMounted(async () => {
  await loadSubjects()
  await loadSimulators()
})
</script>
