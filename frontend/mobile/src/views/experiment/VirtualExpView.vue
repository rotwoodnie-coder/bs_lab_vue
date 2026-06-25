<template>
  <MobilePageShell class="theme-primary" data-layout="explore-home" data-exp-list>
    <VirtualExpList />
  </MobilePageShell>
</template>

<script setup>
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import VirtualExpList from './VirtualExpList.vue'
</script>
