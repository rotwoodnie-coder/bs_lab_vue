import { ref, computed } from 'vue'

/** 步骤指示器 / 面包屑 — 对齐 mobile_prototypes 中 bind/register 脚本 */
export function usePrototypeStepper(totalSteps, stepLabels, breadcrumbItemsFn) {
  const currentStep = ref(1)
  const advancing = ref(false)

  const stepHint = computed(() => stepLabels[currentStep.value - 1] || '')

  const indicatorDots = computed(() => {
    const dots = []
    for (let i = 1; i <= totalSteps; i++) {
      let status = 'pending'
      if (i < currentStep.value) status = 'done'
      else if (i === currentStep.value) status = 'active'
      dots.push({ step: i, status, label: status === 'done' ? '✓' : String(i) })
    }
    return dots
  })

  const breadcrumbs = computed(() => breadcrumbItemsFn(currentStep.value))

  function goToStep(step) {
    currentStep.value = Math.max(1, Math.min(totalSteps, step))
  }

  function autoAdvanceAfterSelect(onStepChange) {
    if (currentStep.value >= totalSteps - 1) return
    advancing.value = true
    setTimeout(() => {
      goToStep(currentStep.value + 1)
      advancing.value = false
      if (onStepChange) onStepChange()
    }, 320)
  }

  return {
    currentStep,
    advancing,
    stepHint,
    indicatorDots,
    breadcrumbs,
    goToStep,
    autoAdvanceAfterSelect
  }
}
