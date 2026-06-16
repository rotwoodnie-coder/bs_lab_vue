<template>

  <router-link

    :to="to"

    class="waterfall-card video-card video-card--feed card-link"

    :class="'anim-fade-up delay-' + ((index % 4) + 1)"

    :data-home-subject="item.subject"

  >

    <div
      ref="mediaRef"
      class="video-card__media"
      :class="[item.gradientClass || 'card-media-grad-warm', 'video-card__media--scene']"
    >

      <img

        v-if="thumbnailSrc && !thumbFailed"

        class="video-card__preview-video"

        :src="thumbnailSrc"

        :alt="item.title"

        loading="lazy"

        @error="thumbFailed = true"

      />

      <video

        v-else-if="posterVideoSrc && !posterFailed"

        class="video-card__preview-video"

        :src="posterVideoSrc"

        playsinline

        muted

        preload="metadata"

        @error="posterFailed = true"

      ></video>

      <span v-if="!hasMedia || (thumbFailed && posterFailed)" class="video-card__scene-icon">

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



const thumbFailed = ref(false)

const posterFailed = ref(false)

const mediaRef = ref(null)



const to = computed(() => detailRoute(props.item))

const showPlay = computed(() =>
  props.item.tagType === 'video'
  || props.item.tagType === 'exp'
  || props.item.type === 'work'
  || isVideoMediaUrl(props.item.videoUrl)
)

/** 首页卡片优先展示图片缩略图 */
const thumbnailSrc = computed(() => {
  const cover = props.item.coverUrl
  if (!cover || isVideoMediaUrl(cover)) return ''
  return resolveFileUrl(cover)
})

/** 无独立缩略图时，用视频首帧（preload=metadata）作封面 */
const posterVideoSrc = computed(() => {
  if (thumbnailSrc.value) return ''
  const url = props.item.videoUrl
    || (isVideoMediaUrl(props.item.coverUrl) ? props.item.coverUrl : '')
  if (!url || !isVideoMediaUrl(url)) return ''
  return resolveFileUrl(url)
})



const hasMedia = computed(() => !!(thumbnailSrc.value || posterVideoSrc.value))



watch(

  () => [props.item.coverUrl, props.item.videoUrl],

  () => {

    thumbFailed.value = false

    posterFailed.value = false

  }

)

</script>
