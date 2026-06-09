<template>
  <div v-if="html" class="rich-text" v-html="html" />
</template>

<script setup>
import { computed } from 'vue'
import { formatDisplayHtml, hasRichContent } from '@/utils/richText'

const props = defineProps({
  value: { type: String, default: '' },
  options: { type: Object, default: () => ({}) }
})

const html = computed(() => {
  if (!hasRichContent(props.value)) return ''
  return formatDisplayHtml(props.value, props.options)
})
</script>
