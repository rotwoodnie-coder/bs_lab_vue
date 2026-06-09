<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail" data-notifications-list>
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/home" />
      <h1 class="topbar-title">消息通知</h1>
      <button type="button" class="text-xs text-brand font-medium shrink-0" @click="markAllRead">全部已读</button>
    </div>

    <div class="px-4 mt-3">
      <div class="notif-summary card card-pad">
        <div class="notif-summary__count">{{ unreadCount }} 条未读</div>
        <div class="notif-summary__hint">学习提醒、批阅反馈与系统公告</div>
      </div>
    </div>

    <div class="px-4 mt-3">
      <div class="tabs notif-tabs scroll-x">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >{{ tab.label }}</button>
      </div>
    </div>

    <div class="px-4 mt-3 stack-4 pb-28">
      <div v-if="loading" class="text-center py-8 muted-2">加载中…</div>
      <div v-else-if="filteredItems.length === 0" class="notif-empty text-center py-8">
        <div class="notif-empty__icon"><i data-lucide="bell-off" class="icon"></i></div>
        <p class="muted-2 mt-3">暂无消息</p>
      </div>
      <template v-else>
        <router-link
          v-for="item in filteredItems"
          :key="item.id"
          :to="itemLink(item)"
          class="notif-item card card-link"
          :class="{ 'notif-item--unread': item.unread }"
          @click="onItemClick(item)"
        >
          <div class="notif-item__inner row gap-3">
            <span class="notif-item__icon" :class="'notif-item__icon--' + item.meta.tone">
              <i :data-lucide="item.meta.icon" class="icon"></i>
            </span>
            <div class="flex-1 min-w-0">
              <div class="row items-center gap-2">
                <span class="notif-type-badge" :class="'notif-type-badge--' + item.type">{{ item.meta.label }}</span>
                <span v-if="item.unread" class="notif-item__dot notif-dot notif-dot-brand"></span>
              </div>
              <div class="text-sm font-bold mt-1">{{ item.title }}</div>
              <p class="text-xs muted mt-1 line-clamp-2">{{ item.preview }}</p>
              <p class="text-xs muted-2 mt-2">{{ item.timeLabel }}</p>
            </div>
          </div>
        </router-link>
      </template>
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchMessages, fetchUnreadCount, markMessageRead } from '@/api/notification'
import { fetchLatestNotice } from '@/api/home'
import { mapMessageItem, isNoticeRead, markNoticeRead, ensureNoticeReadState } from '@/utils/notificationDisplay'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
appStore.setActiveTab('home')

const loading = ref(true)
const items = ref([])
const noticeItem = ref(null)
const unreadCount = ref(0)
const activeTab = ref('all')

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'study', label: '学习' },
  { key: 'social', label: '互动' },
  { key: 'system', label: '系统' }
]

const filteredItems = computed(() => {
  let list = [...items.value]
  if (noticeItem.value && !isNoticeRead(noticeItem.value.id)) {
    list = [noticeItem.value, ...list]
  }
  if (activeTab.value === 'unread') return list.filter((i) => i.unread)
  if (activeTab.value === 'all') return list
  return list.filter((i) => i.tab === activeTab.value)
})

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    }).catch(() => {})
  })
}

function itemLink(item) {
  if (item.kind === 'notice') return `/notifications/notice/${item.id}`
  if (item.linkRoute) return item.linkRoute
  if ((item.type || '').toLowerCase() === 'bind') return '/parent-binds'
  return `/notifications/${item.id}`
}

async function onItemClick(item) {
  if (item.kind === 'notice' && item.unread) {
    await markNoticeRead(item.id)
    item.unread = false
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    return
  }
  if (item.kind === 'msg' && item.unread) {
    try {
      await markMessageRead(item.id)
      item.unread = false
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch { /* ignore */ }
  }
}

async function markAllRead() {
  const unread = items.value.filter((i) => i.unread && i.kind === 'msg')
  for (const item of unread) {
    try {
      await markMessageRead(item.id)
      item.unread = false
    } catch { /* ignore */ }
  }
  if (noticeItem.value?.unread) {
    await markNoticeRead(noticeItem.value.id)
    noticeItem.value.unread = false
  }
  unreadCount.value = 0
}

async function loadData() {
  loading.value = true
  try {
    await ensureNoticeReadState()
    const [msgRes, countRes, noticeRes] = await Promise.all([
      fetchMessages({ pageNum: 1, pageSize: 100 }),
      fetchUnreadCount(),
      fetchLatestNotice().catch(() => null)
    ])
    items.value = (msgRes?.data?.records || []).map((row) => ({
      ...mapMessageItem(row),
      kind: 'msg'
    }))
    unreadCount.value = Number(countRes?.data || 0)
    if (noticeRes?.data) {
      noticeItem.value = {
        id: noticeRes.data.id,
        kind: 'notice',
        type: 'system',
        title: noticeRes.data.title,
        preview: noticeRes.data.body,
        body: noticeRes.data.body,
        timeLabel: noticeRes.data.date,
        unread: noticeRes.data.hasUnread !== false && !isNoticeRead(noticeRes.data.id),
        tab: 'system',
        meta: { label: '系统公告', tab: 'system', icon: 'megaphone', tone: 'slate' },
        notice: noticeRes.data
      }
      if (noticeItem.value.unread) unreadCount.value += 1
    }
  } catch (e) {
    console.warn('加载消息失败', e)
  } finally {
    loading.value = false
    initIcons()
  }
}

onMounted(loadData)
</script>
