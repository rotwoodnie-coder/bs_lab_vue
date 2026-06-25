<template>
  <div class="work-review-panel stack-3">
    <div>
      <div class="text-xs font-medium muted mb-2">评级</div>
      <div class="row gap-2 flex-wrap">
        <button
          v-for="r in ratings"
          :key="r.key"
          type="button"
          class="rating-btn btn btn-sm"
          :class="modelValue.rating === r.key ? 'btn-primary' : 'btn-outline'"
          @click="updateField('rating', r.key)"
        >{{ r.label }}</button>
      </div>
    </div>

    <div>
      <div class="text-xs font-medium muted mb-2">评语</div>
      <textarea
        :value="modelValue.comment"
        class="textarea"
        rows="2"
        placeholder="输入评语..."
        @input="updateField('comment', $event.target.value)"
      ></textarea>
    </div>

    <label v-if="modelValue.rating === 'excellent'" class="row items-center gap-2 text-xs">
      <input
        type="checkbox"
        :checked="modelValue.featured"
        @change="updateField('featured', $event.target.checked)"
      />
      <span>展示到作品墙（创意之星）</span>
    </label>

    <button
      type="button"
      class="btn btn-primary btn-block text-sm"
      :disabled="submitting"
      @click="$emit('submit')"
    >
      <i data-lucide="send" class="icon"></i>
      {{ submitting ? '提交中…' : submitLabel }}
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ rating: '', comment: '', featured: false })
  },
  submitting: { type: Boolean, default: false },
  submitLabel: { type: String, default: '提交评价' }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const ratings = [
  { key: 'excellent', label: '优秀' },
  { key: 'good', label: '良好' },
  { key: 'pass', label: '合格' },
  { key: 'fail', label: '不合格' }
]

function updateField(key, value) {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}
</script>
