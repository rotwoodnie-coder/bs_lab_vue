<template>
  <div class="prototype-container pad-shell" v-bind="shellAttrs">
    <BottomNav v-if="showBottomNav" />
    <slot />
  </div>
</template>

<script setup>
import { computed, useAttrs } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import { usePageNav } from '@/composables/usePageNav'

defineOptions({ inheritAttrs: false })

const attrs = useAttrs()
const { showBottomNav } = usePageNav()

const shellAttrs = computed(() => {
  const { class: extraClass, ...rest } = attrs
  const base = ['prototype-container', 'pad-shell']
  if (extraClass) {
    if (Array.isArray(extraClass)) base.push(...extraClass)
    else base.push(extraClass)
  }
  return { ...rest, class: base.join(' ') }
})
</script>
