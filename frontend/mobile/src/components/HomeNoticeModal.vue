<template>
  <div v-if="visible" class="notice-modal" role="dialog" aria-modal="true" aria-labelledby="noticeModalTitle">
    <div class="notice-modal__backdrop" @click.stop></div>
    <div class="notice-modal__panel">
      <div class="notice-modal__head">
        <span class="notice-modal__badge">{{ notice.badge || '通知公告' }}</span>
        <h2 class="notice-modal__title" id="noticeModalTitle">{{ notice.title }}</h2>
        <p v-if="notice.date" class="notice-modal__date">{{ notice.date }}</p>
      </div>
      <p class="notice-modal__body">{{ notice.body }}</p>
      <label class="notice-modal__check">
        <input v-model="checked" type="checkbox" />
        <span>我已阅读</span>
      </label>
      <button type="button" class="btn btn-primary btn-block" :disabled="!checked" @click="confirm">知道了</button>
      <router-link :to="detailLink" class="btn btn-ghost btn-block mt-2 text-sm" @click="confirm">在消息中心查看</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { isNoticeRead, markNoticeRead, ensureNoticeReadState } from '@/utils/noticeRead'

const props = defineProps({
  notice: { type: Object, default: null }
})

const checked = ref(false)
const dismissed = ref(false)
const ready = ref(false)

const visible = computed(() => {
  if (!ready.value || dismissed.value) return false
  if (!props.notice || !props.notice.id) return false
  if (props.notice.hasUnread === false) return false
  return !isNoticeRead(props.notice.id)
})

const detailLink = computed(() => `/notifications/notice/${props.notice?.id || ''}`)

async function confirm() {
  if (!props.notice?.id || !checked.value) return
  await markNoticeRead(props.notice.id)
  dismissed.value = true
}

watch(() => props.notice?.id, () => {
  checked.value = false
  dismissed.value = false
})

onMounted(async () => {
  await ensureNoticeReadState()
  ready.value = true
})
</script>
