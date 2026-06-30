<template>
  <div class="security-text-editor">
    <div v-if="quickOptions.length" class="security-option-panel">
      <div class="security-option-hint">字典快捷选项（点击追加，再次点击移除）</div>
      <div class="security-option-group">
        <el-button
          v-for="item in quickOptions"
          :key="item.id"
          :type="isLineSelected(item.label) ? activeButtonType : 'info'"
          :plain="!isLineSelected(item.label)"
          class="security-option-button"
          @click="toggleLine(item.label)"
        >{{ item.label }}</el-button>
      </div>
    </div>

    <div class="security-tag-editor__input-row">
      <el-input
        v-model="customInput"
        class="security-tag-editor__input"
        :placeholder="customPlaceholder"
        clearable
        :maxlength="entryMaxLength"
        @keyup.enter="addCustomLine"
      />
      <el-button :type="addButtonType" @click="addCustomLine">添加</el-button>
    </div>

    <div v-if="selectedLines.length" class="security-tag-wrap">
      <el-tag
        v-for="line in selectedLines"
        :key="line"
        closable
        :type="tagType"
        effect="light"
        class="security-tag"
        @close="removeLine(line)"
      >{{ line }}</el-tag>
    </div>

    <el-input
      :model-value="modelValue"
      type="textarea"
      :rows="5"
      :placeholder="textareaPlaceholder"
      :maxlength="maxLength"
      show-word-limit
      @update:model-value="emit('update:modelValue', $event)"
      @blur="emit('save')"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

const ENTRY_MAX_LENGTH = 80

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  fieldType: {
    type: String,
    required: true,
    validator: (value) => ['caution', 'danger'].includes(value)
  },
  quickOptions: {
    type: Array,
    default: () => []
  },
  maxLength: {
    type: Number,
    default: 500
  },
  entryMaxLength: {
    type: Number,
    default: ENTRY_MAX_LENGTH
  }
})

const emit = defineEmits(['update:modelValue', 'save'])

const customInput = ref('')

const activeButtonType = computed(() => (props.fieldType === 'danger' ? 'danger' : 'success'))
const addButtonType = computed(() => (props.fieldType === 'danger' ? 'warning' : 'primary'))
const tagType = computed(() => (props.fieldType === 'danger' ? 'warning' : 'success'))
const customPlaceholder = computed(() => (
  props.fieldType === 'danger'
    ? '输入危险提示条目后回车或点击添加'
    : '输入安全注意事项条目后回车或点击添加'
))
const textareaPlaceholder = computed(() => (
  props.fieldType === 'danger'
    ? '可继续补充完整危险描述，支持换行与序号列表'
    : '可继续补充完整安全描述，支持换行与序号列表'
))

const securityTextLines = (value) => String(value || '')
  .split(/\n/)
  .map(item => item.trim())
  .filter(Boolean)

const selectedLines = computed(() => securityTextLines(props.modelValue))

const isLineSelected = (label) => {
  const value = String(label || '').trim()
  return value ? selectedLines.value.includes(value) : false
}

const buildNextText = (lines) => lines.join('\n')

const toggleLine = (label) => {
  const value = String(label || '').trim()
  if (!value) return
  const lines = [...selectedLines.value]
  const index = lines.findIndex(item => item === value)
  if (index >= 0) {
    lines.splice(index, 1)
  } else {
    const next = buildNextText([...lines, value])
    if (next.length > props.maxLength) {
      ElMessage.warning(`内容不能超过 ${props.maxLength} 字`)
      return
    }
    lines.push(value)
  }
  emit('update:modelValue', buildNextText(lines))
  emit('save')
}

const addCustomLine = () => {
  const value = String(customInput.value || '').trim()
  if (!value) return
  if (isLineSelected(value)) {
    ElMessage.warning('该条目已存在')
    return
  }
  const lines = [...selectedLines.value, value]
  const next = buildNextText(lines)
  if (next.length > props.maxLength) {
    ElMessage.warning(`内容不能超过 ${props.maxLength} 字`)
    return
  }
  emit('update:modelValue', next)
  customInput.value = ''
  emit('save')
}

const removeLine = (label) => {
  const value = String(label || '').trim()
  if (!value) return
  const lines = selectedLines.value.filter(item => item !== value)
  emit('update:modelValue', buildNextText(lines))
  emit('save')
}
</script>

<style scoped src="../css/ExpStandardCreateView.css"></style>
