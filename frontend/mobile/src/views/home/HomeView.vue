<template>
  <div class="prototype-container pad-shell" :class="themeClass" data-layout="explore-home" data-home-feed data-home-notices>
    <HomeStudent @notice-loaded="onNoticeLoaded" />

    <HomeNoticeModal :notice="latestNotice" />
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import BottomNav from '@/components/BottomNav.vue'
import HomeNoticeModal from '@/components/HomeNoticeModal.vue'
import HomeStudent from './HomeStudent.vue'

const userStore = useUserStore()
const latestNotice = ref(null)

const userRoleId = computed(() => (userStore.userInfo.userRoleId || '').toLowerCase())
const isStudent = computed(() => userRoleId.value === 'student' || userRoleId.value === '')
const isParent = computed(() => userRoleId.value === 'parent')
const isTeacher = computed(() => userRoleId.value === 'teacher')

const themeClass = computed(() => {
  if (isParent.value) return 'theme-parent'
  if (isTeacher.value) return 'theme-teacher'
  return 'theme-primary'
})

function onNoticeLoaded(notice) {
  latestNotice.value = notice
}
</script>
