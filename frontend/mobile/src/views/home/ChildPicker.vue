<template>
  <div class="card card-pad">
    <div class="text-xs font-semibold muted mb-3">当前孩子</div>
    <div class="scroll-x row gap-3">
      <button
        v-for="child in children"
        :key="child.id"
        type="button"
        class="col items-center gap-1 shrink-0 parent-child-btn"
        :class="{ 'muted-2': child.id !== selectedChildId }"
        @click="selectChild(child.id)"
      >
        <UserAvatar
          size="sm"
          :name="child.name"
          :src="child.avatarUrl"
          :grad-class="child.id === selectedChildId ? 'avatar-grad-warm' : 'avatar-grad-cool'"
          role="student"
        />
        <span class="text-xs font-semibold">{{ child.name }}</span>
        <span v-if="child.id === selectedChildId" class="text-xs muted-2">当前</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import UserAvatar from '@/components/UserAvatar.vue'

const selectedChildId = inject('parentSelectedId')
const selectChild = inject('parentSelectChild')
const children = inject('parentChildren')
</script>

<style scoped>
.parent-child-btn {
  border: none;
  background: none;
  cursor: pointer;
}
</style>
