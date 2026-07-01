import { ref, watch, onBeforeUnmount } from 'vue'

/**
 * Reactive form for plan editing with auto-save (debounced).
 *
 * @param {string} storageKey - localStorage key for persistence
 * @param {object} defaults - Default form values
 * @param {number} [debounceMs=2000] - Auto-save debounce delay
 */
export function usePlanForm(storageKey, defaults = {}, debounceMs = 2000) {
  const form = ref({ ...defaults })
  const isDirty = ref(false)
  const lastSaved = ref(null)

  let saveTimer = null

  // Load persisted data
  try {
    const raw = localStorage.getItem(storageKey)
    if (raw) {
      const parsed = JSON.parse(raw)
      form.value = { ...defaults, ...parsed }
      lastSaved.value = Date.now()
    }
  } catch { /* ignore */ }

  function persist() {
    try {
      localStorage.setItem(storageKey, JSON.stringify(form.value))
      isDirty.value = false
      lastSaved.value = Date.now()
    } catch { /* ignore */ }
  }

  // Auto-save on change (debounced)
  const stop = watch(
    form,
    () => {
      isDirty.value = true
      if (saveTimer) clearTimeout(saveTimer)
      saveTimer = setTimeout(persist, debounceMs)
    },
    { deep: true }
  )

  // Manual save
  function save() {
    if (saveTimer) clearTimeout(saveTimer)
    persist()
  }

  // Reset
  function reset() {
    if (saveTimer) clearTimeout(saveTimer)
    form.value = { ...defaults }
    isDirty.value = false
    try { localStorage.removeItem(storageKey) } catch { /* ignore */ }
  }

  onBeforeUnmount(() => {
    stop()
    if (saveTimer) clearTimeout(saveTimer)
    // Final auto-save on unmount if dirty
    if (isDirty.value) persist()
  })

  return { form, isDirty, lastSaved, save, reset }
}
