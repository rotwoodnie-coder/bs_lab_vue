<template>
  <div class="tabs" data-tasks-status-tabs>
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      class="tab"
      :class="{ active: modelValue === tab.key }"
      @click="$emit('update:modelValue', tab.key)"
    >
      {{ tab.label }}<span v-if="tab.count > 0" class="tab-count">({{ tab.count }})</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  tabs: { type: Array, required: true },
  modelValue: { type: String, required: true }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.tab-count {
  margin-left: 0.15rem;
  font-weight: 600;
  opacity: 0.85;
}
</style>
