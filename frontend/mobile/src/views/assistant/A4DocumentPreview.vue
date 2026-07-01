<template>
  <div v-if="visible" class="a4-overlay" @click.self="$emit('close')">
    <div class="a4-container">
      <div class="a4-toolbar">
        <button type="button" class="btn btn-ghost btn-sm" @click="$emit('close')">
          <i data-lucide="x" class="icon"></i> 关闭
        </button>
        <span class="a4-toolbar__title">方案预览</span>
        <button
          type="button"
          class="btn btn-primary btn-sm"
          :disabled="exporting"
          @click="handleExport"
        >
          <i data-lucide="download" class="icon"></i>
          {{ exporting ? '导出中…' : '导出 PDF' }}
        </button>
      </div>

      <div class="a4-scroll">
        <div ref="previewRef" class="a4-page">
          <!-- Title -->
          <h1 class="a4-title">{{ plan.title || '实验方案' }}</h1>

          <!-- Meta info -->
          <table class="a4-meta">
            <tbody>
              <tr>
                <td class="a4-meta__label">实验名称</td>
                <td>{{ plan.experimentName || plan.title || '-' }}</td>
              </tr>
              <tr>
                <td class="a4-meta__label">适用年级</td>
                <td>{{ plan.gradeLevel || '-' }}</td>
              </tr>
            </tbody>
          </table>

          <!-- Objective -->
          <section class="a4-section">
            <h2 class="a4-section__title">一、实验目的</h2>
            <p class="a4-section__content">{{ plan.objective || '暂无' }}</p>
          </section>

          <!-- Materials -->
          <section class="a4-section">
            <h2 class="a4-section__title">二、材料准备</h2>
            <p class="a4-section__content" style="white-space: pre-line">{{ plan.materials || '暂无' }}</p>
          </section>

          <!-- Steps -->
          <section class="a4-section">
            <h2 class="a4-section__title">三、实验步骤</h2>
            <p class="a4-section__content" style="white-space: pre-line">{{ plan.steps || '暂无' }}</p>
          </section>

          <!-- Expected Result -->
          <section class="a4-section">
            <h2 class="a4-section__title">四、预期结果</h2>
            <p class="a4-section__content">{{ plan.expectedResult || '暂无' }}</p>
          </section>

          <!-- Notes -->
          <section class="a4-section">
            <h2 class="a4-section__title">五、注意事项</h2>
            <p class="a4-section__content" style="white-space: pre-line">{{ plan.notes || '暂无' }}</p>
          </section>

          <div class="a4-footer">
            <p>由 AI 助手生成 · {{ exportDate }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePlanExport } from '@/composables/usePlanExport'
import { useLucideIcons } from '@/composables/useLucideIcons'

const props = defineProps({
  visible: { type: Boolean, default: false },
  plan: { type: Object, default: () => ({}) }
})

defineEmits(['close'])

const previewRef = ref(null)
const { exporting, exportToPdf } = usePlanExport()

const exportDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})

async function handleExport() {
  if (!previewRef.value) return
  await exportToPdf(previewRef.value, {
    filename: `${props.plan.title || '实验方案'}.pdf`,
    title: props.plan.title || '实验方案'
  })
}

const { initIcons } = useLucideIcons()
onMounted(initIcons)
</script>

<style scoped>
.a4-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 300;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.a4-container {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 520px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}
.a4-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}
.a4-toolbar__title {
  font-weight: 600;
  font-size: var(--text-sm);
}
.a4-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
}
.a4-page {
  background: #fff;
  color: #333;
  padding: 24px 20px;
  border-radius: var(--radius-sm);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  font-size: 13px;
  line-height: 1.6;
}
.a4-title {
  font-size: 20px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 16px;
  color: #222;
}
.a4-meta {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}
.a4-meta td {
  padding: 4px 8px;
  border: 1px solid #e0e0e0;
  font-size: 12px;
}
.a4-meta__label {
  font-weight: 600;
  background: #f5f5f5;
  width: 100px;
  color: #555;
}
.a4-section {
  margin-bottom: 14px;
}
.a4-section__title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  border-left: 3px solid var(--color-primary, #4f46e5);
  padding-left: 8px;
}
.a4-section__content {
  font-size: 13px;
  color: #444;
  padding-left: 4px;
}
.a4-footer {
  margin-top: 24px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
  text-align: center;
  font-size: 11px;
  color: #999;
}
</style>
