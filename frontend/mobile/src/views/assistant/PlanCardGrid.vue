<template>
  <div class="plan-card-grid">
    <div class="plan-card-grid__header">
      <h3 class="plan-card-grid__title">设计方案</h3>
      <span class="plan-card-grid__count">共 {{ plans.length }} 个方案</span>
    </div>
    <div class="plan-card-grid__list">
      <div
        v-for="(plan, idx) in plans"
        :key="plan.id || idx"
        class="plan-card"
        :class="{ 'plan-card--active': activeId === plan.id }"
        @click="$emit('select', plan)"
      >
        <div class="plan-card__badge">方案{{ idx + 1 }}</div>
        <div class="plan-card__title">{{ plan.title || `方案 ${idx + 1}` }}</div>
        <div class="plan-card__summary">{{ plan.summary || plan.description || '' }}</div>
        <div class="plan-card__footer">
          <button
            type="button"
            class="btn btn-ghost btn-xs"
            @click.stop="$emit('edit', plan)"
          >
            <i data-lucide="pencil" class="icon icon-xs"></i> 编辑
          </button>
          <button
            type="button"
            class="btn btn-ghost btn-xs"
            @click.stop="$emit('save', plan)"
          >
            <i data-lucide="bookmark" class="icon icon-xs"></i> 收藏
          </button>
          <button
            type="button"
            class="btn btn-ghost btn-xs"
            @click.stop="$emit('export', plan)"
          >
            <i data-lucide="file-text" class="icon icon-xs"></i> 导出
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useLucideIcons } from '@/composables/useLucideIcons'

defineProps({
  plans: { type: Array, default: () => [] },
  activeId: { type: [String, Number], default: null }
})

defineEmits(['select', 'edit', 'save', 'export'])

const { initIcons } = useLucideIcons()
onMounted(initIcons)
</script>

<style scoped>
.plan-card-grid {
  padding: 12px 16px;
}
.plan-card-grid__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.plan-card-grid__title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-1);
}
.plan-card-grid__count {
  font-size: var(--text-xs);
  color: var(--color-text-3);
}
.plan-card-grid__list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}
.plan-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 14px;
  cursor: pointer;
  transition: box-shadow var(--dur-fast) var(--ease), border-color var(--dur-fast) var(--ease);
}
.plan-card:hover,
.plan-card--active {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.plan-card__badge {
  display: inline-block;
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-bg);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  margin-bottom: 6px;
}
.plan-card__title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.plan-card__summary {
  font-size: var(--text-xs);
  color: var(--color-text-2);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}
.plan-card__footer {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
