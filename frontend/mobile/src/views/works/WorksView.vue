<template>
  <div class="prototype-container pad-shell" data-layout="explore" data-works-wall>
    <BottomNav />

    <div class="pad-main pad-explore">
      <header class="pad-explore__topbar">
        <PageBackButton fallback="/profile" />
        <h1>{{ pageTitle }}</h1>
        <span class="muted text-sm shrink-0">共 {{ displayWorks.length }} 件</span>
      </header>

      <div class="px-4 safe-top pad-explore__mobile-head">
        <header class="topbar">
          <PageBackButton fallback="/profile" />
          <h1 class="topbar-title text-xl flex-1 min-w-0">{{ pageTitle }}</h1>
          <span class="muted text-sm shrink-0">共 {{ displayWorks.length }} 件</span>
        </header>
      </div>

      <div class="pad-explore__filters px-4 stack-2">
        <div class="tabs">
          <button
            type="button"
            class="tab"
            :class="{ active: !isMineScope }"
            @click="switchScope('public')"
          >全部作品</button>
          <button
            v-if="isStudent"
            type="button"
            class="tab"
            :class="{ active: isMineScope }"
            @click="switchScope('mine')"
          >我的作品</button>
        </div>
        <div class="tabs">
          <button
            v-for="tab in typeTabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: activeType === tab.key }"
            @click="switchType(tab.key)"
          >{{ tab.label }}</button>
        </div>
        <div v-if="isMineScope" class="tabs">
          <button
            v-for="tab in statusTabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: activeReviewStatus === tab.key }"
            @click="switchReviewStatus(tab.key)"
          >{{ tab.label }}</button>
        </div>
      </div>

      <div class="pad-explore__body">
        <div v-if="loading" class="px-4 py-8 text-center muted-2">加载中…</div>
        <div v-else class="pad-explore__grid grid-cards anim-fade-up px-4 pb-28">
          <button
            v-if="showSubmitEntry"
            type="button"
            class="works-submit-card anim-fade-up delay-1"
            :class="{ 'works-submit-card--busy': startingCreative }"
            :disabled="startingCreative"
            :aria-label="submitEntryAriaLabel"
            @click="onSubmitEntryClick"
          >
            <span class="works-submit-card__inner">
              <span class="works-submit-card__pill" aria-hidden="true">
                <i data-lucide="plus" class="icon"></i>
              </span>
              <span class="works-submit-card__title">提交作品</span>
              <span class="works-submit-card__hint">{{ submitEntryHint }}</span>
            </span>
          </button>

          <p
            v-if="!showSubmitEntry && !displayWorks.length"
            class="text-center py-12 muted-2 text-sm col-span-full"
          >
            {{ emptyHint }}
          </p>

          <component
            :is="workLinkComponent(work)"
            v-for="(work, i) in displayWorks"
            :key="work.id"
            v-bind="workLinkProps(work)"
            class="video-card video-card--work card-link"
            :class="'delay-' + ((i % 6) + 1)"
          >
            <div class="video-card__media" :class="work.tint">
              <img
                v-if="work.coverUrl"
                :src="resolveCoverUrl(work)"
                alt=""
                class="video-card__cover"
              />
              <span class="video-card__tag">{{ typeLabel(work.type) }}</span>
              <button
                v-if="isMineScope && work.reviewStatus !== 'draft'"
                type="button"
                class="video-card__edit-btn"
                title="编辑作品"
                @click.prevent.stop="goEditWork(work.id)"
              >
                <i data-lucide="pencil" class="icon"></i>
              </button>
              <span v-if="work.coverType === 'video' || !work.coverUrl" class="video-card__play"><i data-lucide="play" class="icon"></i></span>
            </div>
            <div class="video-card__body">
              <div class="video-card__info">
                <UserAvatar
                  v-if="!isMineScope"
                  size="sm"
                  extra-class="video-card__avatar"
                  :name="work.author"
                  :src="work.authorAvatarUrl"
                  role="student"
                />
                <div class="video-card__text">
                  <h3 class="video-card__title">{{ work.title }}</h3>
                  <p class="video-card__meta video-card__meta--work row items-center flex-wrap gap-1">
                    <span class="badge shrink-0" :class="work.reviewBadgeClass || 'badge-warning'">
                      {{ work.reviewStatusLabel || '待审核' }}
                    </span>
                    <template v-if="isMineScope && work.timeLabel">
                      <span class="video-card__dot">·</span>
                      <span>{{ work.timeLabel }}</span>
                    </template>
                    <template v-else-if="!isMineScope">
                      <span class="video-card__dot">·</span>
                      <span class="video-card__author">{{ work.author }}</span>
                      <span class="video-card__dot">·</span>{{ work.className }}
                    </template>
                  </p>
                </div>
              </div>
            </div>
          </component>
        </div>
      </div>
    </div>

    <div class="sheet-overlay" :class="{ show: submitSheetOpen }" @click="submitSheetOpen = false"></div>
    <div class="sheet works-submit-sheet" :class="{ open: submitSheetOpen }" role="dialog" aria-label="选择提交类型">
      <div class="sheet-handle"></div>
      <div class="works-submit-sheet__head">
        <h2 class="works-submit-sheet__title">提交作品</h2>
        <p class="works-submit-sheet__subtitle">选择类型后将进入对应任务或上传流程</p>
      </div>
      <div class="works-submit-sheet__list">
        <button
          v-for="opt in submitOptions"
          :key="opt.key"
          type="button"
          class="works-submit-sheet__option"
          :disabled="startingCreative"
          @click="goSubmitFlow(opt.key)"
        >
          <span class="works-submit-sheet__icon" :style="{ '--opt-color': opt.color }">
            <i :data-lucide="opt.icon" class="icon icon-lg"></i>
          </span>
          <span class="works-submit-sheet__text">
            <span class="works-submit-sheet__label">{{ opt.label }}</span>
            <span class="works-submit-sheet__desc">{{ opt.desc }}</span>
          </span>
          <i data-lucide="chevron-right" class="icon works-submit-sheet__arrow"></i>
        </button>
      </div>
      <button type="button" class="works-submit-sheet__cancel" @click="submitSheetOpen = false">取消</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { fetchWorks } from '@/api/work'
import { startCreativeTask } from '@/api/creative'
import { useUserStore } from '@/stores/user'
import { resolveMediaUrl } from '@/utils/fileUrl'
import { useLucideIcons } from '@/composables/useLucideIcons'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const submitOptions = [
  {
    key: 'homework',
    label: '我的实验',
    desc: '完成老师布置的实验任务',
    icon: 'notebook-text',
    color: 'var(--c-blue-600)'
  },
  {
    key: 'remix',
    label: '拍同款',
    desc: '跟着实验视频拍摄并提交',
    icon: 'camera',
    color: 'var(--c-amber-600)'
  },
  {
    key: 'creative',
    label: '创意实验',
    desc: '自由选题，提交原创实验',
    icon: 'lightbulb',
    color: 'var(--c-violet-600)'
  }
]

const mineTypeTabs = [
  { key: 'all', label: '全部' },
  { key: 'homework', label: '我的实验' },
  { key: 'remix', label: '拍同款' },
  { key: 'creative', label: '创意实验' }
]

const publicTypeTabs = [
  { key: 'all', label: '全部' },
  { key: 'homework', label: '我的实验' },
  { key: 'remix', label: '拍同款' },
  { key: 'creative', label: '创意实验' }
]

const statusTabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '审核中' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '未通过' },
  { key: 'draft', label: '草稿' }
]

const loading = ref(false)
const works = ref([])
const submitSheetOpen = ref(false)
const startingCreative = ref(false)

const isMineScope = computed(() => route.query.scope === 'mine')
const isStudent = computed(() => (userStore.userInfo.userRoleId || 'student').toLowerCase() === 'student')
const showSubmitEntry = computed(() => isMineScope.value && isStudent.value)
const pageTitle = computed(() => (isMineScope.value ? '我的作品墙' : '作品墙'))
const typeTabs = computed(() => (isMineScope.value ? mineTypeTabs : publicTypeTabs))

const submitEntryAriaLabel = computed(() => {
  if (startingCreative.value) return '处理中'
  return `提交作品：${submitEntryHint.value}`
})

const submitEntryHint = computed(() => {
  if (activeType.value === 'homework') return '完成老师布置的实验任务'
  if (activeType.value === 'remix') return '从待完成的拍同款任务进入上传'
  if (activeType.value === 'creative') return '发起创意实验并上传成果'
  return '实验 / 拍同款 / 创意'
})

const activeType = ref('homework')
const activeReviewStatus = ref('all')

const displayWorks = computed(() => {
  if (isMineScope.value) return works.value
  if (activeType.value === 'all') return works.value
  return works.value.filter((w) => w.type === activeType.value)
})

const emptyHint = computed(() => {
  if (isMineScope.value) return '暂无作品，上传并提交后将在此展示'
  return '暂无公开展示的作品'
})

function typeLabel(type) {
  if (type === 'remix') return '拍同款'
  if (type === 'creative') return '创意'
  if (type === 'homework') return '作品'
  return '作品'
}

function resolveCoverUrl(work) {
  if (!work) return ''
  return resolveMediaUrl(work, 'coverUrl')
}

function workLinkComponent() {
  return RouterLink
}

function workLinkProps(work) {
  // 草稿（待完成的创意/拍同款任务）尚无内容详情，点击进入上传流程继续完成并提交
  if (work.reviewStatus === 'draft') {
    const type = work.type === 'remix' ? 'remix' : 'creative'
    return { to: { path: '/upload', query: { type } }, title: '继续完成并提交' }
  }
  return { to: `/works/${work.id}` }
}

function syncQuery(overrides = {}) {
  const query = { ...route.query, ...overrides }
  if (isMineScope.value) {
    query.scope = 'mine'
  } else {
    delete query.scope
    delete query.reviewStatus
  }
  if (query.type === 'all') delete query.type
  if (query.reviewStatus === 'all') delete query.reviewStatus
  router.replace({ path: '/works', query })
}

function switchType(key) {
  activeType.value = key
  syncQuery({ type: key === 'all' ? undefined : key })
  loadWorks()
}

function switchReviewStatus(key) {
  activeReviewStatus.value = key
  syncQuery({ reviewStatus: key === 'all' ? undefined : key })
  loadWorks()
}

function switchScope(scope) {
  const query = { ...route.query }
  if (scope === 'mine') {
    query.scope = 'mine'
    if (!query.reviewStatus) query.reviewStatus = 'all'
  } else {
    delete query.scope
    delete query.reviewStatus
  }
  router.replace({ path: '/works', query })
}

function resolveFromRoute() {
  const scopeMine = route.query.scope === 'mine'
  const type = route.query.type
  if (scopeMine) {
    activeType.value = type && mineTypeTabs.some((t) => t.key === type) ? type : 'all'
    const rs = route.query.reviewStatus
    activeReviewStatus.value = rs && statusTabs.some((t) => t.key === rs) ? rs : 'all'
  } else {
    activeType.value = type && publicTypeTabs.some((t) => t.key === type) ? type : 'all'
    activeReviewStatus.value = 'all'
  }
}

async function loadWorks() {
  loading.value = true
  try {
    const params = {
      page: 1,
      size: 100
    }
    if (isMineScope.value) {
      params.scope = 'mine'
      if (activeType.value !== 'all') params.type = activeType.value
      if (activeReviewStatus.value !== 'all') params.reviewStatus = activeReviewStatus.value
    } else if (activeType.value !== 'all') {
      params.type = activeType.value
    }
    const res = await fetchWorks(params)
    if (res?.code === 401) {
      alert(res.message || '请先登录')
      works.value = []
      return
    }
    works.value = (res?.data?.records || []).map(mapWork)
  } catch {
    works.value = []
  } finally {
    loading.value = false
    initIcons()
  }
}

function mapWork(w) {
  return {
    ...w,
    reviewBadgeClass: w.reviewBadgeClass || badgeForStatus(w.reviewStatus),
    reviewStatusLabel: w.reviewStatusLabel || labelForStatus(w.reviewStatus)
  }
}

function badgeForStatus(status) {
  if (status === 'approved') return 'badge-success'
  if (status === 'draft') return 'badge-slate'
  if (status === 'rejected') return 'badge-danger'
  return 'badge-warning'
}

function labelForStatus(status) {
  if (status === 'approved') return '已通过'
  if (status === 'draft') return '草稿'
  if (status === 'rejected') return '未通过'
  return '审核中'
}

function onSubmitEntryClick() {
  if (startingCreative.value) return
  if (activeType.value !== 'all') {
    goSubmitFlow(activeType.value)
    return
  }
  submitSheetOpen.value = true
  initIcons()
}

async function goSubmitFlow(type) {
  submitSheetOpen.value = false
  if (type === 'homework') {
    router.push({ path: '/tasks', query: { category: 'experiment', status: 'pending' } })
    return
  }
  if (type === 'remix') {
    router.push({ path: '/tasks', query: { category: 'remix', status: 'pending' } })
    return
  }
  if (type === 'creative') {
    startingCreative.value = true
    try {
      const res = await startCreativeTask()
      const taskId = res?.data?.id
      if (taskId) {
        router.push(`/tasks/${taskId}`)
        return
      }
      router.push({ path: '/tasks', query: { category: 'creative', status: 'pending' } })
    } catch (e) {
      const taskId = e?.response?.data?.data?.id
      if (taskId) {
        router.push(`/tasks/${taskId}`)
      } else {
        console.warn('发起创意实验失败', e)
        alert('发起创意实验失败，请稍后重试')
      }
    } finally {
      startingCreative.value = false
      initIcons()
    }
  }
}

function goEditWork(workId) {
  router.push(`/upload?edit=${workId}`)
}

watch(submitSheetOpen, (open) => {
  if (open) initIcons()
})

watch(() => [route.query.scope, route.query.type, route.query.reviewStatus], () => {
  resolveFromRoute()
  loadWorks()
})

const { initIcons } = useLucideIcons()

onMounted(() => {
  resolveFromRoute()
  loadWorks()
})
</script>

<style scoped>
.video-card__edit-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0,0,0,0.55);
  border: none;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s;
}
.video-card__edit-btn:active {
  background: rgba(0,0,0,0.75);
}
.video-card__edit-btn .icon {
  width: 14px;
  height: 14px;
}
</style>
