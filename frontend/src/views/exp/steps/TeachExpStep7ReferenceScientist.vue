<template>
  <div class="step-block">
    <div class="video-step__toolbar">
      <div class="video-step__title">参考与故事</div>
      <div class="video-step__actions">
        <el-button type="primary" @click="handleAddReference">增加参考引用</el-button>
        <el-button type="primary" @click="handleAddScientist">增加科学家故事</el-button>
      </div>
    </div>

    <ReferenceCardList ref="referenceRef" :exp-id="expId" />
    <ScientistCardList ref="scientistRef" :exp-id="expId" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ReferenceCardList from './TeachExpReferenceCardList.vue'
import ScientistCardList from './TeachExpScientistCardList.vue'

defineProps({ expId: { type: [String, Number], required: true } })

const referenceRef = ref(null)
const scientistRef = ref(null)

const handleAddReference = () => {
  referenceRef.value?.addItem?.()
}

const handleAddScientist = () => {
  scientistRef.value?.addItem?.()
}

const flushPendingSaves = async () => {
  const refResults = await referenceRef.value?.flushPendingSaves?.() ?? []
  const sciResults = await scientistRef.value?.flushPendingSaves?.() ?? []
  return [...refResults, ...sciResults]
}

defineExpose({ flushPendingSaves })
</script>
<style scoped src="../css/ExpStandardCreateView.css"></style>
