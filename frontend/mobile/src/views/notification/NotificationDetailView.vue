<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail" data-notification-detail>
    <div class="topbar page-topbar safe-top">
      <PageBackButton fallback="/notifications" />
      <h1 class="topbar-title">消息详情</h1>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="!item" class="px-4 py-12 text-center muted-2">消息不存在</div>
    <div v-else class="px-4 pb-28 stack-4" data-notification-detail-body>
      <div class="notif-detail-hero" :class="'notif-detail-hero--' + (item.type || 'system')">
        <span class="notif-detail-hero__icon">
          <i :data-lucide="item.meta?.icon || 'megaphone'" class="icon"></i>
        </span>
        <span class="notif-type-badge" :class="'notif-type-badge--' + item.type">{{ item.meta?.label || '系统公告' }}</span>
      </div>

      <h2 class="notif-detail__title">{{ item.title }}</h2>
      <p v-if="item.timeLabel" class="text-xs muted">{{ item.timeLabel }}</p>

      <div class="card card-pad notif-detail__body">
        <FormattedText :value="item.body || item.preview" :options="FORMAT_EXP_LONG" />
      </div>

      <router-link
        v-if="item.linkId || item.linkRoute"
        :to="taskDetailLink(item)"
        class="btn btn-gradient btn-block notif-detail__cta--task"
      >{{ taskCtaLabel(item) }}</router-link>
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import FormattedText from '@/components/FormattedText.vue'
import { FORMAT_EXP_LONG } from '@/utils/richText'
import { fetchMessages, markMessageRead } from '@/api/notification'
import { fetchLatestNotice } from '@/api/home'
import { mapMessageItem, markNoticeRead, ensureNoticeReadState } from '@/utils/notificationDisplay'

const route = useRoute()
const loading = ref(true)
const item = ref(null)

const isNoticeRoute = computed(() => route.path.includes('/notifications/notice/'))

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    }).catch(() => {})
  })
}

async function loadDetail() {
  loading.value = true
  const id = route.params.id
  try {
    await ensureNoticeReadState()
    if (isNoticeRoute.value) {
      const res = await fetchLatestNotice()
      if (res?.data && res.data.id === id) {
        item.value = {
          id: res.data.id,
          type: 'system',
          title: res.data.title,
          body: res.data.body,
          preview: res.data.body,
          timeLabel: res.data.date,
          meta: { label: '系统公告', icon: 'megaphone' }
        }
        await markNoticeRead(id)
      }
    } else {
      const res = await fetchMessages({ pageNum: 1, pageSize: 200 })
      const records = res?.data?.records || []
      const raw = records.find((r) => r.msgId === id)
      if (raw) {
        item.value = mapMessageItem(raw)
        if (item.value.unread) {
          try { await markMessageRead(id) } catch { /* ignore */ }
        }
      }
    }
  } catch (e) {
    console.warn('加载消息详情失败', e)
  } finally {
    loading.value = false
    initIcons()
  }
}

function taskDetailLink(item) {
  if (item?.linkRoute) return item.linkRoute
  if (!item?.linkId) return '/notifications'
  if ((item.type || '').toLowerCase() === 'task') {
    return `/tasks/${item.linkId}`
  }
  if ((item.type || '').toLowerCase() === 'bind') {
    return '/parent-binds'
  }
  return `/exp/${item.linkId}`
}

function taskCtaLabel(item) {
  if (item?.linkRoute === '/parent-binds' || (item?.type || '').toLowerCase() === 'bind') {
    return '前往绑定审核'
  }
  return (item?.type || '').toLowerCase() === 'task' ? '查看任务详情' : '查看关联内容'
}

onMounted(loadDetail)
</script>
