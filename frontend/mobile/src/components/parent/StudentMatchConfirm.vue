<template>
  <div class="student-match-confirm">
    <p v-if="hint" class="text-xs muted mb-3">{{ hint }}</p>

    <div v-if="searching" class="text-center py-6 text-sm muted">正在查询学生…</div>

    <div v-else-if="searchError" class="student-match-error mb-4">
      <p class="text-sm">{{ searchError }}</p>
      <div v-if="showStudentNo" class="mt-3">
        <label class="text-xs muted mb-2 block" style="font-weight:var(--weight-medium);">学号（选填，用于区分同名）</label>
        <input
          :value="studentNo"
          type="text"
          placeholder="请输入学号"
          maxlength="32"
          class="input text-sm font-medium"
          @input="$emit('update:studentNo', $event.target.value)"
        />
      </div>
    </div>

    <div v-else-if="candidates.length > 1 && !selected" class="mb-4">
      <p class="text-xs muted mb-2">找到 {{ candidates.length }} 名学生，请选择正确的孩子</p>
      <div class="stack">
        <button
          v-for="item in candidates"
          :key="item.userId"
          type="button"
          class="option-card row items-center justify-between w-full text-left"
          @click="$emit('pick', item)"
        >
          <span class="row items-center gap-3">
            <UserAvatar
              size="sm"
              :name="item.userName"
              :src="item.avatarUrl"
              role="student"
            />
            <span>
              <span class="text-sm font-semibold block">{{ item.userName }}</span>
              <span class="text-xs muted">{{ classLine(item) }}</span>
            </span>
          </span>
          <span class="option-check">✓</span>
        </button>
      </div>
    </div>

    <div v-else-if="selected" class="confirm-summary mb-4">
      <div class="text-xs font-semibold muted-2 mb-2">确认绑定学生</div>
      <div class="confirm-row">
        <div class="cr-icon cr-icon--avatar">
          <UserAvatar
            size="sm"
            :name="selected.userName"
            :src="selected.avatarUrl"
            role="student"
          />
        </div>
        <div>
          <div class="cr-label">孩子姓名</div>
          <div class="cr-value">{{ selected.userName }}</div>
        </div>
      </div>
      <div class="confirm-row">
        <div class="cr-icon"><i data-lucide="users" class="icon cr-svg"></i></div>
        <div>
          <div class="cr-label">班级</div>
          <div class="cr-value">{{ classLine(selected) }}</div>
        </div>
      </div>
      <div class="confirm-row">
        <div class="cr-icon"><i data-lucide="hash" class="icon cr-svg"></i></div>
        <div>
          <div class="cr-label">学号</div>
          <div class="cr-value">{{ selected.loginName || '—' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserAvatar from '@/components/UserAvatar.vue'

const props = defineProps({
  hint: { type: String, default: '请核对以下学生信息，确认无误后提交' },
  searching: { type: Boolean, default: false },
  searchError: { type: String, default: '' },
  candidates: { type: Array, default: () => [] },
  selected: { type: Object, default: null },
  studentNo: { type: String, default: '' },
  showStudentNo: { type: Boolean, default: false },
  gradeName: { type: String, default: '' }
})

defineEmits(['pick', 'update:studentNo'])

function classLine(item) {
  const grade = item.gradeName || props.gradeName || ''
  const cls = item.className || ''
  if (grade && cls) return `${grade} · ${cls}`
  return grade || cls || '—'
}
</script>
