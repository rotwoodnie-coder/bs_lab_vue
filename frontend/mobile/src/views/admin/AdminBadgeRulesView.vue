<template>
  <MobilePageShell class="safe-top theme-primary" data-layout="list-workbench">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/admin" />
        <h1 class="pad-workbench__title flex-1">勋章规则</h1>
        <button type="button" class="btn btn-primary btn-sm" @click="openCreate">新增</button>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top">
            <header class="topbar page-topbar">
              <PageBackButton fallback="/admin" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">勋章规则</h1>
              <button type="button" class="icon-btn" aria-label="新增勋章" @click="openCreate">
                <i data-lucide="plus" class="icon"></i>
              </button>
            </header>
          </div>

          <div class="px-4 pb-28 stack-3">
            <div v-if="loading" class="py-12 text-center muted-2">加载中…</div>
            <div v-else-if="items.length === 0" class="py-12 text-center muted-2 text-sm">暂无勋章规则，点击右上角新增</div>

            <div v-for="badge in items" :key="badge.badgeId" class="card rounded-xl card-pad stack-2">
              <div class="row items-center justify-between">
                <div class="flex items-center gap-3 min-w-0">
                  <span class="badge-emoji">{{ badge.icon || '🏅' }}</span>
                  <div class="min-w-0">
                    <div class="text-sm font-bold">{{ badge.title }}</div>
                    <div class="text-xs muted mt-1">{{ criteriaLabel(badge.criteriaType) }} · 目标 {{ badge.criteriaValue || 1 }}</div>
                  </div>
                </div>
                <span class="badge" :class="badge.status === 'y' ? 'badge-success' : 'badge-muted'">
                  {{ badge.status === 'y' ? '启用' : '停用' }}
                </span>
              </div>
              <div v-if="badge.description" class="text-xs muted">{{ badge.description }}</div>
              <div class="row items-center justify-between mt-1">
                <span class="text-xs muted">奖励 {{ badge.rewardPoints || 0 }} 积分</span>
                <div class="row gap-2">
                  <button type="button" class="btn btn-ghost btn-sm" @click="openEdit(badge)">编辑</button>
                  <button type="button" class="btn btn-danger btn-sm" @click="remove(badge)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑/新增弹层 -->
    <div v-if="editing" class="admin-modal" @click.self="closeEditor">
      <div class="admin-modal__panel">
        <header class="admin-modal__head">
          <h2 class="text-base font-bold">{{ form.badgeId ? '编辑勋章' : '新增勋章' }}</h2>
          <button type="button" class="icon-btn" aria-label="关闭" @click="closeEditor">
            <i data-lucide="x" class="icon"></i>
          </button>
        </header>
        <div class="admin-modal__body stack-3">
          <label class="field">
            <span class="field-label">勋章名称 *</span>
            <input v-model="form.title" type="text" class="field-input" maxlength="30" placeholder="如：实验小达人" />
          </label>
          <label class="field">
            <span class="field-label">图标（Emoji）</span>
            <input v-model="form.icon" type="text" class="field-input" maxlength="4" placeholder="🏅" />
          </label>
          <label class="field">
            <span class="field-label">描述</span>
            <textarea v-model="form.description" class="field-input" rows="2" maxlength="120" placeholder="勋章获取说明"></textarea>
          </label>
          <label class="field">
            <span class="field-label">达成条件</span>
            <select v-model="form.criteriaType" class="field-input">
              <option v-for="c in criteriaOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
          </label>
          <label class="field">
            <span class="field-label">目标值</span>
            <input v-model.number="form.criteriaValue" type="number" min="1" class="field-input" />
          </label>
          <label class="field">
            <span class="field-label">奖励积分</span>
            <input v-model.number="form.rewardPoints" type="number" min="0" class="field-input" />
          </label>
          <label class="field">
            <span class="field-label">排序值（越小越靠前）</span>
            <input v-model.number="form.sortOrder" type="number" min="0" class="field-input" />
          </label>
          <div class="field row items-center justify-between">
            <span class="field-label">启用</span>
            <label class="switch">
              <input type="checkbox" :checked="form.status === 'y'" @change="form.status = $event.target.checked ? 'y' : 'n'" />
              <span class="switch-slider"></span>
            </label>
          </div>
        </div>
        <footer class="admin-modal__foot">
          <button type="button" class="btn btn-ghost flex-1" @click="closeEditor">取消</button>
          <button type="button" class="btn btn-primary flex-1" :disabled="saving" @click="save">
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </footer>
      </div>
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { useAppStore } from '@/stores/app'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { fetchBadgeDefinitions, saveBadgeDefinition, deleteBadgeDefinition } from '@/api/admin'
import { showToast } from '@/utils/toast'

const appStore = useAppStore()
const { initIcons } = useLucideIcons()
appStore.setActiveTab('admin')

const criteriaOptions = [
  { value: 'work_first', label: '首次上传作品' },
  { value: 'work_submit_count', label: '累计上传作品数' },
  { value: 'work_featured', label: '作品被精选数' },
  { value: 'exp_task_done', label: '完成实验任务数' },
  { value: 'quiz_first', label: '首次每日答题' },
  { value: 'quiz_correct', label: '累计答对题数' },
  { value: 'quiz_streak', label: '连续答题天数' }
]
function criteriaLabel(type) {
  const hit = criteriaOptions.find((c) => c.value === type)
  return hit ? hit.label : (type || '未设置条件')
}

const items = ref([])
const loading = ref(true)
const saving = ref(false)
const editing = ref(false)
const form = reactive({
  badgeId: '',
  icon: '🏅',
  title: '',
  description: '',
  criteriaType: 'work_submit_count',
  criteriaValue: 1,
  rewardPoints: 0,
  sortOrder: 0,
  status: 'y'
})

async function load() {
  loading.value = true
  try {
    const res = await fetchBadgeDefinitions()
    if (res?.code === 200 && Array.isArray(res.data)) {
      items.value = res.data
    } else {
      showToast(res?.message || '加载失败', 'danger')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '加载失败', 'danger')
  } finally {
    loading.value = false
    nextTick(() => initIcons())
  }
}

function resetForm(source) {
  Object.assign(form, {
    badgeId: source?.badgeId || '',
    icon: source?.icon || '🏅',
    title: source?.title || '',
    description: source?.description || '',
    criteriaType: source?.criteriaType || 'work_submit_count',
    criteriaValue: source?.criteriaValue ?? 1,
    rewardPoints: source?.rewardPoints ?? 0,
    sortOrder: source?.sortOrder ?? 0,
    status: source?.status || 'y'
  })
}

function openCreate() {
  resetForm(null)
  editing.value = true
  nextTick(() => initIcons())
}
function openEdit(badge) {
  resetForm(badge)
  editing.value = true
  nextTick(() => initIcons())
}
function closeEditor() {
  editing.value = false
}

async function save() {
  if (saving.value) return
  if (!form.title.trim()) {
    showToast('请填写勋章名称', 'danger')
    return
  }
  saving.value = true
  try {
    const res = await saveBadgeDefinition({ ...form })
    if (res?.code === 200) {
      showToast('已保存')
      editing.value = false
      load()
    } else {
      showToast(res?.message || '保存失败', 'danger')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '保存失败', 'danger')
  } finally {
    saving.value = false
  }
}

async function remove(badge) {
  if (!confirm(`确定删除勋章「${badge.title}」？`)) return
  try {
    const res = await deleteBadgeDefinition(badge.badgeId)
    if (res?.code === 200) {
      showToast('已删除')
      load()
    } else {
      showToast(res?.message || '删除失败', 'danger')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '删除失败', 'danger')
  }
}

onMounted(load)
</script>

<style scoped>
.badge-emoji {
  font-size: var(--text-3xl);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-2, rgba(148, 163, 184, 0.12));
  border-radius: var(--radius-lg, 12px);
  flex-shrink: 0;
}
.badge-muted { background: var(--c-slate-200, #e2e8f0); color: var(--c-slate-600, #475569); }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: var(--text-xs); font-weight: var(--weight-bold); }
.field-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: var(--radius-lg, 12px);
  border: 1px solid var(--border, #e2e8f0);
  background: var(--surface-1, #fff);
  font-size: var(--text-sm);
}
.switch { position: relative; display: inline-block; width: 46px; height: 26px; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.switch-slider {
  position: absolute; inset: 0; cursor: pointer;
  background: var(--c-slate-300, #cbd5e1); border-radius: 999px; transition: .2s;
}
.switch-slider::before {
  content: ''; position: absolute; height: 20px; width: 20px; left: 3px; bottom: 3px;
  background: #fff; border-radius: 50%; transition: .2s;
}
.switch input:checked + .switch-slider { background: var(--c-brand, #6366f1); }
.switch input:checked + .switch-slider::before { transform: translateX(20px); }

.admin-modal {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(15, 23, 42, 0.45);
  display: flex; align-items: flex-end; justify-content: center;
}
.admin-modal__panel {
  width: 100%; max-width: 520px;
  background: var(--surface-1, #fff);
  border-radius: 20px 20px 0 0;
  max-height: 88vh; display: flex; flex-direction: column;
}
.admin-modal__head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px; border-bottom: 1px solid var(--border, #e2e8f0);
}
.admin-modal__body { padding: 16px; overflow-y: auto; }
.admin-modal__foot {
  display: flex; gap: 12px; padding: 16px;
  border-top: 1px solid var(--border, #e2e8f0);
}
@media (min-width: 640px) {
  .admin-modal { align-items: center; }
  .admin-modal__panel { border-radius: 20px; }
}
</style>
