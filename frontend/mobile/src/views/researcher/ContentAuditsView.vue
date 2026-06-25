<template>
  <div class="prototype-container pad-shell safe-top" data-layout="list-workbench">
    <BottomNav />
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton :fallback="backFallback" />
        <h1 class="pad-workbench__title flex-1">实验待审核</h1>
        <span v-if="total > 0" class="badge badge-warning">{{ total }}</span>
        <router-link to="/settings" class="icon-btn" aria-label="设置与退出">
          <i data-lucide="settings" class="icon"></i>
        </router-link>
      </header>

      <div class="pad-workbench__filters px-4 py-3">
        <div class="tabs scroll-x">
          <button
            v-for="tab in typeTabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: expType === tab.key }"
            @click="switchType(tab.key)"
          >{{ tab.label }}</button>
        </div>
      </div>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top stack-3">
            <header class="topbar page-topbar">
              <PageBackButton :fallback="backFallback" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">实验待审核</h1>
              <span v-if="total > 0" class="badge badge-warning shrink-0">{{ total }}</span>
            </header>
            <div class="tabs scroll-x">
              <button
                v-for="tab in typeTabs"
                :key="tab.key"
                type="button"
                class="tab"
                :class="{ active: expType === tab.key }"
                @click="switchType(tab.key)"
              >{{ tab.label }}</button>
            </div>
          </div>

          <div class="px-4 pb-28">
            <div v-if="loading" class="py-12 text-center muted-2">加载中…</div>
            <div v-else-if="!items.length" class="py-12 text-center stack-3">
              <div class="text-2xl">🎉</div>
              <p class="muted-2 text-sm">暂无待审核实验</p>
            </div>
            <div v-else class="stack-3">
              <router-link
                v-for="item in items"
                :key="item.expId"
                :to="`/content-audits/${item.expId}`"
                class="card card-link card-pad stack-2"
              >
                <div class="row justify-between items-start gap-2">
                  <div class="min-w-0">
                    <span class="badge badge-info text-xs">{{ item.expTypeLabel }}</span>
                    <div class="text-sm font-bold mt-2">{{ item.expName }}</div>
                    <div class="text-xs muted mt-1">{{ item.subjectName || '—' }} · {{ item.submitterName }}</div>
                  </div>
                  <span class="badge badge-warning shrink-0">待审核</span>
                </div>
                <div v-if="item.submitTime" class="text-xs muted-2">提交于 {{ item.submitTime }}</div>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchResearcherExpAudits } from '@/api/researcher'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { isAdminRole } from '@/utils/role'

const userStore = useUserStore()
const appStore = useAppStore()
const { initIcons } = useLucideIcons()

const isAdmin = computed(() => isAdminRole(userStore.userInfo?.userRoleId))
const backFallback = computed(() => (isAdmin.value ? '/audits' : '/profile'))
if (isAdmin.value) {
  appStore.setActiveTab('admin')
}

const loading = ref(true)
const items = ref([])
const total = ref(0)
const expType = ref('')

const typeTabs = [
  { key: '', label: '全部' },
  { key: 'teach', label: '教学实验' },
  { key: 'standard', label: '标准实验' }
]

async function loadList() {
  loading.value = true
  try {
    const res = await fetchResearcherExpAudits({
      expType: expType.value || undefined,
      page: 1,
      size: 50
    })
    items.value = res?.data?.records || []
    total.value = res?.data?.total || items.value.length
  } catch (e) {
    console.warn('加载实验审核列表失败', e)
    items.value = []
  } finally {
    loading.value = false
    nextTick(() => initIcons())
  }
}

function switchType(key) {
  expType.value = key
  loadList()
}

onMounted(async () => {
  await loadList()
  nextTick(() => initIcons())
})
</script>
