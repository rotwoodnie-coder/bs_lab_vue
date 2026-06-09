<template>
  <div :class="gridClass">
    <template v-for="badge in badges" :key="badge.id">
      <router-link
        v-if="badge.action && !badge.earned"
        :to="badge.action"
        class="badge-card badge-card--locked badge-card--action"
        :aria-label="`去解锁：${badge.title}`"
      >
        <div class="badge-card__icon">{{ badge.icon }}</div>
        <div class="badge-card__title">{{ badge.title }}</div>
        <p class="badge-card__desc">{{ badge.desc }}</p>
        <span class="badge-card__status badge badge-slate">{{ badge.progress }}</span>
      </router-link>
      <article v-else class="badge-card" :class="{ 'badge-card--locked': !badge.earned }">
        <div class="badge-card__icon">{{ badge.icon }}</div>
        <div class="badge-card__title">{{ badge.title }}</div>
        <p class="badge-card__desc">{{ badge.desc }}</p>
        <span class="badge-card__status badge" :class="badge.earned ? 'badge-success' : 'badge-slate'">
          {{ badge.earned ? '已获得' : badge.progress }}
        </span>
      </article>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  badges: { type: Array, default: () => [] },
  mobile: { type: Boolean, default: false }
})

const gridClass = computed(() => (props.mobile ? 'grid grid-3 gap-3' : 'pad-badges__grid'))
</script>
