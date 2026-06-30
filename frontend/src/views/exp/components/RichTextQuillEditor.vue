<template>
  <div class="step-card__editor-shell">
    <div class="step-card__toolbar">
      <slot name="toolbar" />
    </div>
    <QuillEditor
      :key="editorKey"
      v-model:content="innerContent"
      content-type="html"
      theme="snow"
      :toolbar="toolbar"
      class="step-card__editor"
      :style="{ minHeight: '240px', height: '240px' }"
      @ready="handleReady"
      @textChange="handleTextChange"
      @selectionChange="handleSelectionChange"
      @focus="handleFocus"
      @blur="handleBlur"
    />
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const props = defineProps({
  modelValue: { type: String, default: '' },
  editorKey: { type: String, required: true },
  toolbar: {
    type: Array,
    default: () => [
      ['bold', 'italic', 'underline', 'strike'],
      [{ header: 1 }, { header: 2 }],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ align: [] }],
      ['link', 'image', 'clean']
    ]
  }
})

const emit = defineEmits(['update:modelValue', 'blur', 'ready'])

const innerContent = ref(props.modelValue || '')
const quillInstance = ref(null)
const isEditorFocused = ref(false)
const detachNativeListeners = ref(null)

watch(
  () => props.modelValue,
  (value) => {
    const next = value || ''
    if (next !== innerContent.value) {
      innerContent.value = next
    }
  }
)

watch(innerContent, (value) => {
  emit('update:modelValue', value || '')
})

/** v-model:content 已自动同步 Quill -> innerContent，无需额外 emit */
const handleTextChange = () => {
  // v-model:content handles the sync
}

const emitBlur = () => {
  if (!isEditorFocused.value) return
  isEditorFocused.value = false
  emit('blur')
}

const handleFocus = () => {
  isEditorFocused.value = true
}

const handleSelectionChange = ({ range } = {}) => {
  // 当 selection 变为空且编辑器已失焦时，补发 blur，避免工具栏/浮层切换导致漏发
  if (!range) {
    emitBlur()
  }
}

const handleBlur = () => {
  emitBlur()
}

const handleNativeFocusIn = () => {
  handleFocus()
}

const handleNativeFocusOut = () => {
  emitBlur()
}

const bindNativeFocusListeners = (quill) => {
  const root = quill?.root
  if (!root) return

  detachNativeListeners.value?.()

  root.addEventListener('focusin', handleNativeFocusIn)
  root.addEventListener('focusout', handleNativeFocusOut)

  detachNativeListeners.value = () => {
    root.removeEventListener('focusin', handleNativeFocusIn)
    root.removeEventListener('focusout', handleNativeFocusOut)
  }
}

const handleReady = (editor) => {
  quillInstance.value = editor?.getQuill?.() || editor
  bindNativeFocusListeners(quillInstance.value)
  emit('ready', quillInstance.value)
  syncFromEditor()
}

/** 从 Quill DOM 同步到 v-model */
const syncFromEditor = () => {
  const quill = quillInstance.value
  if (!quill?.root) return false
  const html = quill.root.innerHTML
  innerContent.value = html
  emit('update:modelValue', html)
  return true
}

const appendHtml = (html) => {
  const quill = quillInstance.value
  const next = `${props.modelValue || ''}${html}`
  if (quill?.root) {
    quill.root.innerHTML = next
  }
  innerContent.value = next
  emit('update:modelValue', next)
  return Boolean(quill?.root)
}

const setHtml = (html) => {
  const quill = quillInstance.value
  const next = html || ''
  if (quill?.root) {
    quill.root.innerHTML = next
  }
  innerContent.value = next
  emit('update:modelValue', next)
  return Boolean(quill?.root)
}

defineExpose({
  syncFromEditor,
  appendHtml,
  setHtml,
  getQuill: () => quillInstance.value
})

onBeforeUnmount(() => {
  detachNativeListeners.value?.()
  detachNativeListeners.value = null
  quillInstance.value = null
  isEditorFocused.value = false
})
</script>
