import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { showBottomNavForMeta, showBackForMeta } from '@/utils/pageNav'

export function usePageNav() {
  const route = useRoute()

  const nav = computed(() => route.meta?.nav || 'stack')
  const showBottomNav = computed(() => showBottomNavForMeta(nav.value))
  const showBack = computed(() => showBackForMeta(nav.value))
  const backFallback = computed(() => route.meta?.backFallback || '/home')

  return { nav, showBottomNav, showBack, backFallback }
}
