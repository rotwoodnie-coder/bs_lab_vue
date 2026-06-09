<template>

  <div class="prototype-container theme-primary safe-top pad-shell" data-layout="growth-archive">

    <BottomNav />



    <!-- Pad / 桌面 -->

    <div class="pad-main pad-workbench pad-profile__pad-only">

      <header class="pad-workbench__topbar">

        <PageBackButton fallback="/profile" />

        <h1 class="pad-workbench__title">🌱 成长档案</h1>

        <div class="pad-workbench__topbar-actions">

          <button type="button" class="btn btn-gradient btn-sm" @click="shareGrowth">

            <i data-lucide="share-2" class="icon"></i> 分享

          </button>

        </div>

      </header>



      <div class="pad-workbench__body">

        <GrowthBody

          :display-name="displayName"

          :user-initial="userInitial"

          :class-label="classLabel"

          :growth="growth"

          :plan="growth.plan"

          :quiz-nudge-text="quizNudgeText"

          :is-parent-view="isParentView"

          :is-restricted="isRestricted"

        />

      </div>

    </div>



    <!-- 手机竖屏 -->

    <div class="pad-profile__mobile-only">

      <div class="topbar safe-top">

        <PageBackButton fallback="/profile" />

        <h1 class="topbar-title">🌱 成长档案</h1>

        <div class="row gap-1">

          <button type="button" class="icon-btn" aria-label="分享" @click="shareGrowth">

            <i data-lucide="share-2" class="icon"></i>

          </button>

        </div>

      </div>



      <div class="row items-center gap-4 px-4 py-5">

        <div class="avatar avatar-lg avatar-grad-warm">{{ userInitial }}</div>

        <div>

          <div class="text-base font-bold">{{ displayName }}</div>

          <div class="text-sm muted mt-1">{{ classLabel }} · 小学</div>

          <span class="badge badge-info mt-2">本期</span>

        </div>

      </div>



      <div v-if="isRestricted" class="px-4 mb-4">

        <div class="card card-pad text-center tint-amber">

          <p class="text-sm">{{ growth.access?.restrictedReason || '暂无查看权限' }}</p>

        </div>

      </div>



      <template v-else>

      <div class="grid gap-3 px-4 mb-3" style="grid-template-columns: repeat(4, minmax(0, 1fr));">

        <router-link to="/tasks?category=experiment" class="card card-pad text-center tint-amber card-link">

          <div class="text-2xl font-bold text-warning">{{ growth.stats.experiments ?? 0 }}</div>

          <div class="text-xs muted mt-1">🧪 实验</div>

        </router-link>

        <router-link to="/works" class="card card-pad text-center tint-violet card-link">

          <div class="text-2xl font-bold">{{ growth.stats.works ?? 0 }}</div>

          <div class="text-xs muted mt-1">🎨 作品</div>

        </router-link>

        <router-link to="/quiz/history" class="card card-pad text-center tint-blue card-link">

          <div class="text-2xl font-bold text-brand">{{ growth.stats.quizDays ?? 0 }}</div>

          <div class="text-xs muted mt-1">🧠 答题天</div>

        </router-link>

        <router-link to="/badges" class="card card-pad text-center tint-green card-link">

          <div class="text-2xl font-bold text-success">{{ growth.stats.badges ?? 0 }}</div>

          <div class="text-xs muted mt-1">🏅 勋章</div>

        </router-link>

      </div>



      <div class="px-4 mb-3">

        <div class="card card-pad text-center tint-blue">

          <div class="text-2xl font-bold text-brand">+{{ growth.stats.periodPoints ?? 0 }}</div>

          <div class="text-xs muted mt-1">⭐ 本期获得积分</div>

          <div class="text-xs muted mt-1">累计 {{ growth.stats.totalPoints ?? growth.stats.points ?? 0 }} 分</div>

        </div>

      </div>



      <div class="px-4 mb-4" v-if="!isParentView">
        <router-link to="/quiz" class="card card-pad card-link quiz-nudge row items-center gap-3">
          <div class="tile-icon tile-sm tint-violet shrink-0">🧠</div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-bold">每日答题</div>
            <p class="text-xs muted mt-1">{{ quizNudgeText }}</p>
          </div>
          <i data-lucide="chevron-right" class="icon muted-2 shrink-0"></i>
        </router-link>
      </div>

      <div class="px-4 py-3 pb-28">
        <h2 class="text-base font-bold mb-4">📖 学习轨迹</h2>
        <TimelineList :items="growth.timeline" />
        <router-link to="/badges" class="btn btn-gradient btn-block mt-5">

          <i data-lucide="award" class="icon"></i> 查看勋章墙

        </router-link>

      </div>

      </template>

    </div>



  </div>

</template>



<script setup>

import { ref, computed, onMounted, nextTick, defineComponent, h } from 'vue'
import { RouterLink } from 'vue-router'

import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'

import { fetchProfile } from '@/api/profile'

import { fetchGrowth } from '@/api/growth'
import { fetchTodayQuiz } from '@/api/quiz'
import { sharePage } from '@/utils/share'
import { useParentContext } from '@/composables/useParentContext'
import { isParentRole } from '@/utils/role'
import { useUserStore } from '@/stores/user'



function timelineTo(item) {
  if (!item?.linkType) return null
  switch (item.linkType) {
    case 'task':
      return item.linkId ? `/tasks/${item.linkId}` : '/tasks'
    case 'work':
      return item.linkId ? `/works/${item.linkId}` : '/works'
    case 'quiz':
      return '/quiz/history'
    case 'badge':
      return '/badges'
    default:
      return null
  }
}

function groupTimeline(items) {
  const groups = []
  const map = new Map()
  for (const item of items || []) {
    const key = item.groupLabel || '更早'
    if (!map.has(key)) {
      const group = { label: key, items: [] }
      map.set(key, group)
      groups.push(group)
    }
    map.get(key).items.push(item)
  }
  return groups
}

function renderTimelineItem(item) {
  const inner = h('div', { class: 'surface-2 rounded-xl p-3' }, [
    h('div', { class: 'row items-center gap-2' }, [
      h('span', { class: 'text-lg' }, item.emoji),
      h('div', { class: 'flex-1' }, [
        h('div', { class: 'text-sm font-bold' }, item.title),
        item.hint ? h('div', { class: 'text-xs muted mt-1' }, item.hint) : null,
        item.badges ? h('div', { class: 'row gap-2 mt-1' }, item.badges.map((b, j) =>
          h('span', { class: 'badge badge-info', key: j }, b)
        )) : null
      ]),
      item.badge ? h('span', { class: ['badge', item.badgeClass || 'badge-success'] }, item.badge) : null
    ])
  ])
  const route = timelineTo(item)
  const body = route
    ? h(RouterLink, { to: route, class: 'block card-link' }, [inner])
    : inner
  return h('div', { class: 'timeline-item', key: `${item.time}-${item.title}` }, [
    h('div', { class: ['timeline-dot', 'filled', item.dot].filter(Boolean) }),
    h('div', { class: 'flex-1 min-w-0' }, [
      h('p', { class: 'text-xs muted mb-1' }, item.time),
      body
    ])
  ])
}

const TimelineList = defineComponent({

  name: 'TimelineList',

  props: { items: { type: Array, default: () => [] } },

  setup(props) {
    return () => {
      if (!props.items || !props.items.length) {
        return h('div', { class: 'card card-pad text-center text-sm muted py-6' }, [
          h('p', { class: 'mb-3' }, '还没有学习记录，快去完成任务或每日答题吧'),
          h('div', { class: 'row gap-2 justify-center flex-wrap' }, [
            h(RouterLink, { to: '/tasks?category=experiment', class: 'btn btn-soft btn-sm' }, '去做实验'),
            h(RouterLink, { to: '/quiz', class: 'btn btn-soft btn-sm' }, '每日答题')
          ])
        ])
      }
      const groups = groupTimeline(props.items)
      return h('div', { class: 'stack-4' }, groups.map((group) =>
        h('div', { key: group.label, class: 'stack-3' }, [
          h('h3', { class: 'text-xs font-bold muted px-1' }, group.label),
          h('div', { class: 'timeline' }, group.items.map((item) => renderTimelineItem(item)))
        ])
      ))
    }
  }

})



const GrowthBody = defineComponent({

  name: 'GrowthBody',

  props: {

    displayName: String,

    userInitial: String,

    classLabel: String,

    growth: Object,

    plan: Object,

    quizNudgeText: String,

    isParentView: Boolean,

    isRestricted: Boolean

  },

  setup(props) {

    return () => h('div', null, [

      h('div', { class: 'pad-profile__banner banner banner-amber-rose stack-2' }, [

        h('div', { class: 'flex items-center gap-4' }, [

          h('div', { class: 'avatar avatar-lg avatar-grad-warm' }, props.userInitial),

          h('div', null, [

            h('div', { class: 'text-base font-bold' }, props.displayName),

            h('div', { class: 'text-sm banner-sub' }, `${props.classLabel} · 小学`),

            props.plan?.range ? h('span', { class: 'badge badge-info mt-2' }, props.plan.range) : null

          ])

        ])

      ]),

      props.isRestricted
        ? h('div', { class: 'card rounded-xl card-pad text-center tint-amber mb-3' }, [
            h('p', { class: 'text-sm' }, props.growth?.access?.restrictedReason || '暂无查看权限')
          ])
        : null,

      !props.isRestricted
        ? h('div', { class: 'pad-profile__stats', style: 'grid-template-columns:repeat(4,minmax(0,1fr));' }, [

        h(RouterLink, { to: '/tasks?category=experiment', class: 'card rounded-xl card-pad text-center tint-amber card-link' }, [

          h('div', { class: 'text-2xl font-bold text-warning' }, props.growth.stats.experiments ?? 0),

          h('div', { class: 'text-xs muted mt-1' }, '🧪 实验')

        ]),

        h(RouterLink, { to: '/works', class: 'card rounded-xl card-pad text-center tint-violet card-link' }, [

          h('div', { class: 'text-2xl font-bold' }, props.growth.stats.works ?? 0),

          h('div', { class: 'text-xs muted mt-1' }, '🎨 作品')

        ]),

        h(RouterLink, { to: '/quiz/history', class: 'card rounded-xl card-pad text-center tint-blue card-link' }, [

          h('div', { class: 'text-2xl font-bold text-brand' }, props.growth.stats.quizDays ?? 0),

          h('div', { class: 'text-xs muted mt-1' }, '🧠 答题天')

        ]),

        h(RouterLink, { to: '/badges', class: 'card rounded-xl card-pad text-center tint-green card-link' }, [

          h('div', { class: 'text-2xl font-bold text-success' }, props.growth.stats.badges ?? 0),

          h('div', { class: 'text-xs muted mt-1' }, '🏅 勋章')

        ])

      ]) : null,

      !props.isRestricted
        ? h('div', { class: 'card rounded-xl card-pad text-center tint-blue mb-3' }, [

          h('div', { class: 'text-2xl font-bold text-brand' }, `+${props.growth.stats.periodPoints ?? 0}`),

          h('div', { class: 'text-xs muted mt-1' }, `⭐ ${props.plan?.range || '本期'}获得积分`),

          h('div', { class: 'text-xs muted mt-1' }, `累计 ${props.growth.stats.totalPoints ?? props.growth.stats.points ?? 0} 分`)

        ]) : null,

      !props.isRestricted ? h('div', { class: 'pad-profile__content', style: 'grid-template-columns:minmax(0,1fr);' }, [

        !props.isParentView
          ? h('router-link', { to: '/quiz', class: 'card rounded-xl card-pad card-link quiz-nudge row items-center gap-3 mb-3' }, [

          h('div', { class: 'tile-icon tile-sm tint-violet shrink-0' }, '🧠'),

          h('div', { class: 'flex-1 min-w-0' }, [

            h('div', { class: 'text-sm font-bold' }, '每日答题'),

            h('p', { class: 'text-xs muted mt-1' }, props.quizNudgeText || '今日答题')

          ]),

          h('i', { 'data-lucide': 'chevron-right', class: 'icon muted-2 shrink-0' })

        ]) : null,

        h('div', { class: 'card rounded-xl card-pad' }, [

          h('h2', { class: 'text-base font-bold mb-4' }, '📖 学习轨迹'),

          h(TimelineList, { items: props.growth.timeline }),

          h('router-link', { to: '/badges', class: 'btn btn-gradient btn-block mt-5' }, [

            h('i', { 'data-lucide': 'award', class: 'icon' }),

            ' 查看勋章墙'

          ])

        ])

      ]) : null

    ])

  }

})



const profile = ref({})

const quizNudgeText = ref('今日 3 题 · 加载中…')

const growth = ref({
  timeline: [],
  stats: { experiments: 0, works: 0, quizDays: 0, points: 0, totalPoints: 0, periodPoints: 0, badges: 0 },
  plan: { content: '', visibility: '', range: '' },
  access: { canEditPlan: true, canViewDetail: true }
})

const displayName = computed(() => {
  if (isParentView.value && selectedChild.value?.name) {
    return selectedChild.value.name
  }
  return profile.value.userNickName || profile.value.userName || '同学'
})

const userInitial = computed(() => displayName.value.charAt(0) || '同')

const classLabel = computed(() => {
  if (isParentView.value && selectedChild.value?.classLabel) {
    return selectedChild.value.classLabel
  }
  return profile.value.userOrgName || profile.value.rootOrgName || ''
})

const userStore = useUserStore()
const { childQueryParam, selectedChild, loadChildren } = useParentContext()
const isParentView = computed(() => isParentRole(userStore.userInfo.userRoleId))
const isRestricted = computed(() => growth.value.access?.canViewDetail === false)

async function shareGrowth() {
  const title = isParentView.value && selectedChild.value
    ? `${selectedChild.value.name}的成长档案`
    : `${displayName.value}的成长档案`
  const stats = growth.value.stats || {}
  await sharePage({
    title,
    text: `实验 ${stats.experiments ?? 0} · 作品 ${stats.works ?? 0} · 答题 ${stats.quizDays ?? 0} 天 · 勋章 ${stats.badges ?? 0}`
  })
}



async function reloadGrowth() {
  try {
    const gRes = await fetchGrowth({ childUserId: childQueryParam() })
    if (gRes?.code === 200 && gRes.data) {
      growth.value = {
        ...gRes.data,
        stats: {
          experiments: 0,
          works: 0,
          quizDays: 0,
          points: 0,
          totalPoints: 0,
          periodPoints: 0,
          badges: 0,
          ...(gRes.data.stats || {})
        },
        timeline: gRes.data.timeline || [],
        access: gRes.data.access || { canEditPlan: !isParentView.value, canViewDetail: true }
      }
      if (gRes.data.nudge?.text) {
        quizNudgeText.value = gRes.data.nudge.text
      }
    }
  } catch { /* ignore */ }
}

function initIcons() {

  nextTick(() => {

    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})

  })

}



onMounted(async () => {
  if (isParentRole(userStore.userInfo.userRoleId)) {
    await loadChildren()
  }

  try {

    const res = await fetchProfile()

    if (res.code === 200 && res.data) profile.value = res.data

  } catch { /* prototype fallback */ }

  await reloadGrowth()

  if (!growth.value.nudge?.text) {
    try {
      const qRes = await fetchTodayQuiz()
      if (qRes?.code === 200 && qRes.data) {
        if (qRes.data.submittedToday && qRes.data.todayResult) {
          quizNudgeText.value = `今日已完成 ${qRes.data.todayResult.score}/${qRes.data.todayResult.total} · 连对 ${qRes.data.todayResult.streakDays ?? 0} 天`
        } else if (qRes.data.ready) {
          const n = qRes.data.questionsPerDay || qRes.data.questions?.length || 3
          quizNudgeText.value = `今日 ${n} 题待完成`
        } else {
          quizNudgeText.value = qRes.data.message || '题库暂无可用题目'
        }
      }
    } catch {
      quizNudgeText.value = '加载答题状态失败'
    }
  }

  initIcons()

})

</script>

