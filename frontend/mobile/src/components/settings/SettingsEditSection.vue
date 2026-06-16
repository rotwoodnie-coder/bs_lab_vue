<template>
  <section class="pad-settings__section" :class="{ 'is-active': active }">
    <div class="settings-panel-head">
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>
    <div class="stack-4 settings-profile-form">
      <slot />
    </div>
  </section>
</template>

<script setup>
defineProps({
  active: { type: Boolean, default: false },
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>
