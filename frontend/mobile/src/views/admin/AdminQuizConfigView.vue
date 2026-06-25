<template>
  <div class="prototype-container pad-shell safe-top theme-primary" data-layout="list-workbench">
    <BottomNav />
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/admin" />
        <h1 class="pad-workbench__title flex-1">每日答题配置</h1>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top">
            <header class="topbar page-topbar">
              <PageBackButton fallback="/admin" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">每日答题配置</h1>
            </header>
          </div>

          <div class="px-4 pb-28 stack-4">
            <div v-if="loading" class="py-12 text-center muted-2">加载中…</div>
            <template v-else>
              <div v-if="form.poolWarning" class="card rounded-xl card-pad admin-warn">
                <i data-lucide="triangle-alert" class="icon"></i>
                <span class="text-xs">{{ form.poolWarning }}</span>
              </div>

              <div class="card rounded-xl card-pad row items-center justify-between">
                <div>
                  <div class="text-sm font-bold">启用每日答题</div>
                  <div class="text-xs muted mt-1">关闭后学生端不再展示答题入口</div>
                </div>
                <label class="switch">
                  <input type="checkbox" v-model="form.enabled" />
                  <span class="switch-slider"></span>
                </label>
              </div>

              <div class="card rounded-xl card-pad stack-4">
                <label class="field">
                  <span class="field-label">每日题目数量</span>
                  <input type="number" min="1" max="20" v-model.number="form.questionsPerDay" class="field-input" />
                </label>
                <label class="field">
                  <span class="field-label">基础积分（每题）</span>
                  <input type="number" min="0" max="100" v-model.number="form.basePoints" class="field-input" />
                </label>
                <label class="field">
                  <span class="field-label">连续答题奖励积分</span>
                  <input type="number" min="0" max="100" v-model.number="form.streakBonus" class="field-input" />
                </label>
                <div class="text-xs muted">当前可用题库题目数：{{ form.eligibleQuestionCount }}</div>
              </div>

              <button type="button" class="btn btn-primary btn-block" :disabled="saving" @click="save">
                {{ saving ? '保存中…' : '保存配置' }}
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { useAppStore } from '@/stores/app'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { fetchQuizConfig, saveQuizConfig } from '@/api/admin'
import { showToast } from '@/utils/toast'

const appStore = useAppStore()
const { initIcons } = useLucideIcons()
appStore.setActiveTab('admin')

const loading = ref(true)
const saving = ref(false)
const form = reactive({
  questionsPerDay: 5,
  basePoints: 10,
  streakBonus: 5,
  enabled: true,
  eligibleQuestionCount: 0,
  poolWarning: ''
})

async function load() {
  loading.value = true
  try {
    const res = await fetchQuizConfig()
    if (res?.code === 200 && res.data) {
      Object.assign(form, {
        questionsPerDay: res.data.questionsPerDay ?? 5,
        basePoints: res.data.basePoints ?? 10,
        streakBonus: res.data.streakBonus ?? 5,
        enabled: res.data.enabled ?? true,
        eligibleQuestionCount: res.data.eligibleQuestionCount ?? 0,
        poolWarning: res.data.poolWarning || ''
      })
    } else {
      showToast(res?.message || '加载配置失败', 'danger')
    }
  } catch (e) {
    showToast('加载配置失败', 'danger')
  } finally {
    loading.value = false
    nextTick(() => initIcons())
  }
}

async function save() {
  if (saving.value) return
  saving.value = true
  try {
    const res = await saveQuizConfig({
      questionsPerDay: form.questionsPerDay,
      basePoints: form.basePoints,
      streakBonus: form.streakBonus,
      enabled: form.enabled
    })
    if (res?.code === 200) {
      showToast('配置已保存')
      if (res.data) {
        form.eligibleQuestionCount = res.data.eligibleQuestionCount ?? form.eligibleQuestionCount
        form.poolWarning = res.data.poolWarning || ''
      }
      nextTick(() => initIcons())
    } else {
      showToast(res?.message || '保存失败', 'danger')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '保存失败', 'danger')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-warn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--c-amber-700, #b45309);
  background: var(--c-amber-50, #fffbeb);
}
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
</style>
