<template>
  <div v-if="optionsLoading" class="text-sm muted py-2">加载选项中…</div>

  <div>
    <h2 class="text-sm font-bold mb-3">🧪 选择实验</h2>
    <div class="stack">
      <button
        v-for="(exp, idx) in expOptions"
        :key="exp.id"
        type="button"
        class="option-card w-full text-left"
        :class="{ selected: formExpId === exp.id }"
        @click="$emit('select-exp', exp.id)"
      >
        <div class="row items-center gap-3">
          <div class="tile-icon tile-sm" :class="expTint(idx)">{{ expEmoji(idx) }}</div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-bold">{{ exp.label }}</div>
            <div class="text-xs muted">{{ expSubtitle(exp) }}</div>
          </div>
        </div>
        <span class="option-check">✓</span>
      </button>
      <p v-if="!optionsLoading && !expOptions.length" class="text-xs muted">暂无实验，请先在管理端发布</p>
    </div>
  </div>

  <div>
    <h2 class="text-sm font-bold mb-3">📋 选择班级</h2>
    <div class="stack">
      <button
        v-for="cls in classOptions"
        :key="cls.id"
        type="button"
        class="option-card w-full text-left"
        :class="{ selected: selectedClassIds.has(cls.id) }"
        @click="$emit('toggle-class', cls.id)"
      >
        <div class="flex-1">
          <div class="text-sm font-bold">{{ cls.label }}</div>
          <div class="text-xs muted">{{ cls.studentCount || 0 }} 名学生</div>
        </div>
        <span class="option-check">✓</span>
      </button>
      <p v-if="!optionsLoading && !classOptions.length" class="text-xs muted">暂无班级数据</p>
    </div>
  </div>

  <div>
    <h2 class="text-sm font-bold mb-3">📅 截止日期</h2>
    <p class="text-xs muted mb-2">默认为发布日后 7 天，可手动修改</p>
    <input :value="deadlineDate" class="input" type="date" @input="$emit('update:deadlineDate', $event.target.value)" />
  </div>

  <div>
    <h2 class="text-sm font-bold mb-3">📝 备注说明</h2>
    <textarea
      :value="requirements"
      class="textarea"
      rows="3"
      placeholder="请输入补充说明（可选）…"
      @input="$emit('update:requirements', $event.target.value)"
    ></textarea>
  </div>
</template>

<script setup>
defineProps({
  optionsLoading: { type: Boolean, default: false },
  expOptions: { type: Array, default: () => [] },
  classOptions: { type: Array, default: () => [] },
  formExpId: { type: String, default: '' },
  selectedClassIds: { type: Object, required: true },
  deadlineDate: { type: String, default: '' },
  requirements: { type: String, default: '' }
})

defineEmits(['select-exp', 'toggle-class', 'update:deadlineDate', 'update:requirements'])

const EXP_EMOJIS = ['🔬', '🌱', '⚡']
const EXP_TINTS = ['tint-blue', 'tint-green', 'tint-amber']

function expEmoji(idx) {
  return EXP_EMOJIS[idx % EXP_EMOJIS.length]
}

function expTint(idx) {
  return EXP_TINTS[idx % EXP_TINTS.length]
}

function expSubtitle(exp) {
  return exp.subtitle || (exp.sourceType === 'simulator' ? '模拟实验' : '实验库任务')
}
</script>
