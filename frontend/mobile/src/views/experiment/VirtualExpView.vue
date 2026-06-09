<template>
  <div class="prototype-container pad-shell theme-primary" data-layout="explore-home" data-exp-list>
    <VirtualExpList />
    <BottomNav />
  </div>
</template>

<script setup>
import BottomNav from '@/components/BottomNav.vue'
import VirtualExpList from './VirtualExpList.vue'
</script>
