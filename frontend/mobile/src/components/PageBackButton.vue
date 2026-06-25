<template>
  <button type="button" class="icon-btn page-back" aria-label="返回" @click="handleBack">
    <i data-lucide="arrow-left" class="icon"></i>
  </button>
</template>

<script setup>
import { computed, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { goBack } from '@/utils/navBack'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { usePageNav } from '@/composables/usePageNav'

const props = defineProps({
  fallback: { type: String, default: '' }
})

const router = useRouter()
const route = useRoute()
const { backFallback } = usePageNav()

const resolvedFallback = computed(() => props.fallback || backFallback.value || '/home')

function handleBack() {
  goBack(router, resolvedFallback.value)
}

const { initIcons } = useLucideIcons()

onMounted(initIcons)
</script>
