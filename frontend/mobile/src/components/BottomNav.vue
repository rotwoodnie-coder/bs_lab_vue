<template>
  <nav ref="navRef" class="bottom-nav" aria-label="主导航">
    <div class="nav-items">
      <router-link
        v-for="tab in tabs"
        :key="tab.key"
        :to="tab.route"
        class="nav-item"
        :class="{ active: activeTab === tab.key, 'has-nav-badge': tab.hasBadge }"
        :aria-label="tab.ariaLabel"
      >
        <span class="nav-icon" :class="{ 'nav-icon--avatar': tab.key === 'profile' }">
          <template v-if="tab.key === 'profile'">
            <span class="avatar avatar-sm avatar-grad-warm nav-avatar" :title="userName">{{ userInitial }}</span>
          </template>
          <template v-else>
            <i :data-lucide="tab.icon" class="icon"></i>
          </template>
        </span>
        <span v-if="tab.hasBadge" class="nav-badge" aria-hidden="true"></span>
        <span class="nav-label">{{ tab.label }}</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useScrollRevealBottomNav } from '@/composables/useScrollRevealBottomNav'

const navRef = ref(null)
useScrollRevealBottomNav(navRef)

const appStore = useAppStore()
const userStore = useUserStore()

const tabs = computed(() => appStore.tabs)
const activeTab = computed(() => appStore.activeTab)

const userName = computed(() => userStore.userInfo.username || '用户')
const userInitial = computed(() => userName.value.charAt(0) || '用')
</script>
