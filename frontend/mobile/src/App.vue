<template>
  <router-view />
</template>

<script setup>
import { onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { initLucideIcons } from '@/utils/lucideIcons'

const route = useRoute()

function refreshIcons() {
  nextTick(() => {
    nextTick(() => initLucideIcons())
  })
}

onMounted(refreshIcons)
watch(() => route.fullPath, refreshIcons)
</script>
