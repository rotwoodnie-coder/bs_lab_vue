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
          <UserAvatar
            v-if="tab.key === 'profile'"
            size="sm"
            extra-class="nav-avatar"
          />
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
import { useScrollRevealBottomNav } from '@/composables/useScrollRevealBottomNav'
import UserAvatar from '@/components/UserAvatar.vue'

const navRef = ref(null)
useScrollRevealBottomNav(navRef)

const appStore = useAppStore()

const tabs = computed(() => appStore.tabs)
const activeTab = computed(() => appStore.activeTab)
</script>
