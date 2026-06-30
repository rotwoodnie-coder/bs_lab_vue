import { ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'

/** 富文本步骤索引：实验步骤 / 实验结果 / 参考与故事 */
export const RICH_TEXT_WIZARD_STEPS = [3, 4, 6]

export function useExpWizardRichTextFlush({ activeStep, stepSaving, step4Ref, step5Ref, step7Ref, expId }) {
  const visitedRichSteps = ref(new Set())

  watch(
    activeStep,
    (step) => {
      if (RICH_TEXT_WIZARD_STEPS.includes(step)) {
        visitedRichSteps.value = new Set([...visitedRichSteps.value, step])
      }
    },
    { immediate: true }
  )

  if (expId) {
    watch(expId, () => {
      visitedRichSteps.value = RICH_TEXT_WIZARD_STEPS.includes(activeStep.value)
        ? new Set([activeStep.value])
        : new Set()
    })
  }

  const getRichStepRef = (stepIndex) => {
    if (stepIndex === 3) return step4Ref.value
    if (stepIndex === 4) return step5Ref.value
    if (stepIndex === 6) return step7Ref.value
    return null
  }

  const flushRichTextStep = async (stepIndex) => {
    const comp = getRichStepRef(stepIndex)
    if (!comp?.flushPendingSaves) return { ok: true, failed: 0, results: [] }
    const results = await comp.flushPendingSaves()
    const failed = (results || []).filter((row) => !row.ok).length
    return { ok: failed === 0, failed, results: results || [] }
  }

  const flushBeforeLeaveRichStep = async (fromStep = activeStep.value) => {
    if (!RICH_TEXT_WIZARD_STEPS.includes(fromStep)) return true
    stepSaving.value = true
    try {
      const { ok, failed } = await flushRichTextStep(fromStep)
      if (ok) return true
      try {
        await ElMessageBox.confirm(
          `${failed} 条内容保存失败，是否仍要离开当前步骤？`,
          '提示',
          { type: 'warning', confirmButtonText: '仍要离开', cancelButtonText: '留在此步' }
        )
        return true
      } catch {
        return false
      }
    } finally {
      stepSaving.value = false
    }
  }

  return {
    visitedRichSteps,
    flushBeforeLeaveRichStep,
    RICH_TEXT_WIZARD_STEPS
  }
}
