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
      @blur="emit('blur')"
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

const handleReady = (editor) => {
  quillInstance.value = editor?.getQuill?.() || editor
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
  quillInstance.value = null
})
</script>
