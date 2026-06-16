<template>

  <div

    class="avatar"

    :class="rootClasses"

    :style="rootStyle"

    :title="resolvedTitle"

    :aria-hidden="ariaHidden ? 'true' : undefined"

  >

    <img

      v-if="showImage"

      :src="resolvedSrc"

      :key="resolvedSrc"

      alt="头像"

      :class="imgClass"

      @error="onImageError"

    />

    <slot v-else>{{ resolvedInitial }}</slot>

    <slot name="overlay" />

  </div>

</template>



<script setup>

import { computed, onMounted, ref, watch } from 'vue'

import { useProfileStore } from '@/stores/profile'

import { resolveFileUrl } from '@/utils/fileUrl'

import { avatarGradByRole, avatarInitial } from '@/utils/avatar'



const props = defineProps({

  /** 头像 URL（他人头像时传入；当前用户可省略） */

  src: {

    type: String,

    default: undefined

  },

  /** 显示名（他人头像时传入，用于首字占位） */

  name: {

    type: String,

    default: undefined

  },

  /** student | parent | teacher，用于无图时的渐变占位 */

  role: {

    type: String,

    default: ''

  },

  /** 自定义渐变 class，优先级高于 role */

  gradClass: {

    type: String,

    default: ''

  },

  size: {

    type: String,

    default: 'sm',

    validator: (v) => ['sm', 'lg', 'xl'].includes(v)

  },

  extraClass: {

    type: [String, Object, Array],

    default: ''

  },

  imgClass: {

    type: String,

    default: ''

  },

  title: {

    type: String,

    default: ''

  },

  ariaHidden: {

    type: Boolean,

    default: false

  },

  fontSize: {

    type: String,

    default: ''

  },

  shrink: {

    type: Boolean,

    default: false

  }

})



const profileStore = useProfileStore()

const imageBroken = ref(false)



const isSelf = computed(() => props.name == null && props.src == null)



const resolvedTitle = computed(() => {

  if (props.title) return props.title

  if (!isSelf.value) return props.name || ''

  return profileStore.displayName

})



const resolvedInitial = computed(() => {

  if (!isSelf.value) {

    return avatarInitial(props.name, '用')

  }

  return profileStore.userInitial

})



const resolvedSrc = computed(() => {

  if (imageBroken.value) return ''

  if (!isSelf.value) {

    return props.src ? resolveFileUrl(props.src) : ''

  }

  return profileStore.avatarUrl

})



const showImage = computed(() => Boolean(resolvedSrc.value))



const resolvedGradClass = computed(() => {

  if (props.gradClass) return props.gradClass

  if (!isSelf.value) return avatarGradByRole(props.role || 'student')

  return profileStore.avatarGradClass

})



const rootClasses = computed(() => {

  const classes = [`avatar-${props.size}`]

  if (props.shrink) classes.push('shrink-0')

  if (props.extraClass) classes.push(props.extraClass)

  if (!showImage.value) classes.push(resolvedGradClass.value)

  return classes

})



const rootStyle = computed(() => (props.fontSize ? { fontSize: props.fontSize } : undefined))



function onImageError() {

  imageBroken.value = true

}



watch(

  () => [props.src, props.name, profileStore.avatarUrl],

  () => {

    imageBroken.value = false

  }

)



onMounted(() => {

  if (isSelf.value && !profileStore.profile?.userId) {

    profileStore.loadProfile()

  }

})

</script>


