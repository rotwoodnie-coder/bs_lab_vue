<template>
  <button type="button" class="icon-btn page-back" aria-label="返回" @click="handleBack">
    <i data-lucide="arrow-left" class="icon"></i>
  </button>
</template>

<script setup>
import { nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goBack } from '@/utils/navBack'

const props = defineProps({
  fallback: { type: String, default: '/home' }
})

const router = useRouter()

function handleBack() {
  goBack(router, props.fallback)
}

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

onMounted(initIcons)
</script>
