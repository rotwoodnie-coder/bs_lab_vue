<template>
  <router-link
    :to="`/sim/${item.simulatorId}`"
    class="waterfall-card video-card video-card--sim card-link"
    :class="animClass"
  >
    <div
      class="video-card__media"
      :class="item.mediaGrad"
    >
      <img
        v-if="coverSrc && !coverFailed"
        :src="coverSrc"
        :alt="item.simulatorName"
        loading="lazy"
        @error="coverFailed = true"
      />
      <span class="video-card__tag">模拟</span>
      <span class="video-card__play"><i data-lucide="play" class="icon"></i></span>
    </div>
    <div class="video-card__body">
      <div class="video-card__info">
        <span class="video-card__avatar avatar avatar-sm" :class="item.avatarGrad">{{ item.avatarChar }}</span>
        <div class="video-card__text">
          <h3 class="video-card__title">{{ item.simulatorName }}</h3>
          <p v-if="item.metaText" class="video-card__meta">{{ item.metaText }}</p>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { resolveFileUrl } from '@/utils/fileUrl'

const props = defineProps({
  item: { type: Object, required: true },
  index: { type: Number, default: 0 }
})

const coverFailed = ref(false)
const coverSrc = computed(() => resolveFileUrl(props.item.coverImageUrl))

watch(() => props.item.coverImageUrl, () => {
  coverFailed.value = false
})

const animClass = computed(() => `anim-fade-up delay-${(props.index % 4) + 1}`)
</script>
