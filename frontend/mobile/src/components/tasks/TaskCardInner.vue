<template>
  <div class="row justify-between items-start gap-3">
    <div class="min-w-0">
      <span class="task-type-badge" :class="'task-type-badge--' + variant">{{ typeLabel }}</span>
      <div class="text-base font-bold mt-2">{{ task.title }}</div>
      <p v-if="task.desc" class="text-xs muted mt-2">{{ task.desc }}</p>
      <p v-if="task.deadline" class="text-xs muted-2 mt-1">截止 {{ task.deadline }}</p>
      <div v-if="task.quizScore" class="task-quiz-compact__score mt-1 text-accent">{{ task.quizScore }}</div>
    </div>
    <span class="badge shrink-0" :class="task.badgeClass">{{ task.stateLabel }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  task: { type: Object, required: true }
})

const variant = computed(() => {
  const t = props.task
  if (t.category === 'quiz') return 'quiz'
  if (t.category === 'remix') return 'remix'
  if (t.category === 'creative') return 'creative'
  if (t.category === 'class') return 'homework'
  if (t.category === 'audit') return 'simulator'
  if (t.subType === 'simulator') return 'simulator'
  return 'homework'
})

const typeLabel = computed(() => {
  const t = props.task
  if (t.category === 'quiz') return '🧠 答题'
  if (t.category === 'remix') return '📷 拍同款'
  if (t.category === 'creative') return '💡 创意'
  if (t.category === 'class') return '📋 班级任务'
  if (t.category === 'audit') return '📑 实验审核'
  if (t.subType === 'simulator') return '🖥️ 模拟实验'
  return '🔬 实验'
})
</script>
