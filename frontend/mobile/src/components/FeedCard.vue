<template>

  <router-link

    :to="to"

    class="waterfall-card video-card video-card--feed card-link"

    :class="'anim-fade-up delay-' + ((index % 4) + 1)"

    :data-home-subject="item.subject"

  >

    <div class="video-card__media" :class="[item.gradientClass || 'card-media-grad-warm', 'video-card__media--scene']">

      <video

        v-if="videoSrc && !videoFailed"

        class="video-card__preview-video"

        :src="videoSrc"

        playsinline

        muted

        preload="metadata"

        @error="videoFailed = true"

      ></video>

      <img

        v-else-if="coverSrc && !coverFailed"

        :src="coverSrc"

        :alt="item.title"

        loading="lazy"

        @error="coverFailed = true"

      />

      <span v-if="!hasMedia || (videoFailed && coverFailed)" class="video-card__scene-icon">

        <i :data-lucide="sceneIcon(item)" class="icon"></i>

      </span>

      <span class="video-card__tag" :class="'video-card__tag--' + (item.tagType || 'video')">

        <i :data-lucide="tagIcon(item)" class="icon"></i>{{ item.tagLabel }}

      </span>

      <span v-if="showPlay" class="video-card__play"><i data-lucide="play" class="icon"></i></span>

    </div>

    <div class="video-card__body">

      <h3 class="video-card__title">{{ item.title }}</h3>

      <div class="video-card__meta-block">

        <p class="video-card__meta-line">

          <template v-for="(part, i) in metaLineParts(item)" :key="'meta-' + i">

            <span v-if="i > 0" class="video-card__dot">·</span>{{ part }}

          </template>

        </p>

        <p class="video-card__meta-line video-card__meta-line--creator">

          <template v-for="(part, i) in authorLineParts(item)" :key="'author-' + i">

            <span v-if="i > 0" class="video-card__dot">·</span>

            <span v-if="i === 0" class="video-card__author">{{ part }}</span>

            <template v-else>{{ part }}</template>

          </template>

        </p>

      </div>

    </div>

  </router-link>

</template>



<script setup>

import { ref, computed, watch } from 'vue'

import {

  detailRoute,

  sceneIcon,

  tagIcon,

  metaLineParts,

  authorLineParts

} from '@/utils/feedDisplay'

import { resolveFileUrl, isVideoMediaUrl } from '@/utils/fileUrl'



const props = defineProps({

  item: { type: Object, required: true },

  index: { type: Number, default: 0 }

})



const coverFailed = ref(false)

const videoFailed = ref(false)



const to = computed(() => detailRoute(props.item))

const showPlay = computed(() => props.item.tagType === 'video' || props.item.tagType === 'exp' || !!videoSrc.value)



const videoSrc = computed(() => {

  const raw = props.item.videoUrl || props.item.coverUrl

  if (!raw || !isVideoMediaUrl(raw)) return ''

  return resolveFileUrl(raw)

})



const coverSrc = computed(() => {

  if (videoSrc.value) return ''

  const raw = props.item.coverUrl

  if (!raw || isVideoMediaUrl(raw)) return ''

  return resolveFileUrl(raw)

})



const hasMedia = computed(() => !!(videoSrc.value || coverSrc.value))



watch(

  () => [props.item.coverUrl, props.item.videoUrl],

  () => {

    coverFailed.value = false

    videoFailed.value = false

  }

)

</script>


