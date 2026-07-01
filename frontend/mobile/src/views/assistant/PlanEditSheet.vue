<template>
  <div v-if="visible" class="plan-edit-overlay" @click.self="$emit('close')">
    <div class="plan-edit-sheet">
      <div class="plan-edit-sheet__header">
        <h3>编辑方案</h3>
        <button type="button" class="icon-btn" @click="$emit('close')">
          <i data-lucide="x" class="icon"></i>
        </button>
      </div>

      <div class="plan-edit-sheet__body">
        <div class="form-group">
          <label class="form-label">方案标题</label>
          <input v-model="localPlan.title" type="text" class="input" placeholder="输入方案标题" />
        </div>

        <div class="form-group">
          <label class="form-label">实验名称</label>
          <input v-model="localPlan.experimentName" type="text" class="input" placeholder="输入实验名称" />
        </div>

        <div class="form-group">
          <label class="form-label">适用年级</label>
          <select v-model="localPlan.gradeLevel" class="input">
            <option value="">请选择</option>
            <option value="低段">低段（1-2年级）</option>
            <option value="中段">中段（3-4年级）</option>
            <option value="高段">高段（5-6年级）</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">实验目的</label>
          <textarea v-model="localPlan.objective" class="input" rows="3" placeholder="实验目的"></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">材料准备</label>
          <textarea v-model="localPlan.materials" class="input" rows="3" placeholder="所需材料清单"></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">实验步骤</label>
          <textarea v-model="localPlan.steps" class="input" rows="5" placeholder="详细实验步骤"></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">预期结果</label>
          <textarea v-model="localPlan.expectedResult" class="input" rows="3" placeholder="预期实验结果"></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">注意事项</label>
          <textarea v-model="localPlan.notes" class="input" rows="2" placeholder="安全注意事项等"></textarea>
        </div>
      </div>

      <div class="plan-edit-sheet__footer">
        <button type="button" class="btn btn-ghost" @click="$emit('close')">取消</button>
        <button type="button" class="btn btn-primary" @click="handleSave">保存方案</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useLucideIcons } from '@/composables/useLucideIcons'

const props = defineProps({
  visible: { type: Boolean, default: false },
  plan: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['close', 'save'])

const localPlan = ref(createEmptyPlan())

function createEmptyPlan() {
  return {
    id: '',
    title: '',
    experimentName: '',
    gradeLevel: '',
    objective: '',
    materials: '',
    steps: '',
    expectedResult: '',
    notes: ''
  }
}

watch(() => props.plan, (val) => {
  localPlan.value = { ...createEmptyPlan(), ...val }
}, { immediate: true, deep: true })

function handleSave() {
  emit('save', { ...localPlan.value })
}

const { initIcons } = useLucideIcons()
onMounted(initIcons)
</script>

<style scoped>
.plan-edit-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.plan-edit-sheet {
  background: var(--color-surface);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  width: 100%;
  max-width: 480px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.plan-edit-sheet__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
  font-weight: 600;
  font-size: var(--text-base);
}
.plan-edit-sheet__body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.plan-edit-sheet__footer {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
  justify-content: flex-end;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-2);
}
</style>
