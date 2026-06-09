<template>
  <div class="prototype-container pad-shell theme-teacher safe-top" data-layout="list-workbench">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/profile" />
        <h1 class="pad-workbench__title flex-1">📋 家长绑定审核</h1>
        <span v-if="statusFilter === 'pending' && total > 0" class="badge badge-warning">{{ total }}</span>
      </header>

      <div class="pad-workbench__filters px-4 py-3">
        <div class="tabs scroll-x">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: statusFilter === tab.key }"
            @click="switchTab(tab.key)"
          >{{ tab.label }}</button>
        </div>
      </div>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top stack-3">
            <header class="topbar page-topbar">
              <PageBackButton fallback="/profile" />
              <h1 class="topbar-title flex-1">📋 家长绑定审核</h1>
              <span v-if="statusFilter === 'pending' && total > 0" class="badge badge-warning">{{ total }}</span>
            </header>
            <div class="tabs scroll-x">
              <button
                v-for="tab in tabs"
                :key="'m-' + tab.key"
                type="button"
                class="tab"
                :class="{ active: statusFilter === tab.key }"
                @click="switchTab(tab.key)"
              >{{ tab.label }}</button>
            </div>
          </div>

          <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
          <div v-else-if="!items.length" class="px-4 py-12 text-center stack-3">
            <div class="text-2xl">📭</div>
            <p class="muted-2">暂无{{ emptyLabel }}申请</p>
          </div>

          <div v-else class="pad-list__grid stack-3 px-4 pb-28">
            <div v-for="item in items" :key="item.bindId" class="card card-pad stack-3">
              <div class="row items-start justify-between gap-2">
                <div class="min-w-0">
                  <div class="text-sm font-bold">{{ item.parentName || '家长' }} · {{ item.relation || '家长' }}</div>
                  <div v-if="item.parentPhoneMasked" class="text-xs muted mt-1">{{ item.parentPhoneMasked }}</div>
                </div>
                <span class="badge shrink-0" :class="statusBadgeClass(item.bindStatus)">{{ statusLabel(item.bindStatus) }}</span>
              </div>

              <div class="row items-center gap-3 surface-2 rounded-xl p-3">
                <div class="avatar avatar-sm avatar-grad-warm">{{ childInitial(item.childName) }}</div>
                <div class="flex-1 min-w-0">
                  <div class="text-sm font-bold">{{ item.childName || '学生' }}</div>
                  <div class="text-xs muted mt-1">{{ classLabel(item) }}</div>
                  <div v-if="item.studentNo" class="text-xs muted-2 mt-1">学号 {{ item.studentNo }}</div>
                </div>
              </div>

              <div v-if="item.submitTime" class="text-xs muted">申请时间：{{ item.submitTime }}</div>
              <div v-if="item.bindStatus === 'rejected' && item.rejectReason" class="text-xs text-danger">
                驳回原因：{{ item.rejectReason }}
              </div>

              <div v-if="item.bindStatus === 'pending'" class="row gap-2">
                <button
                  type="button"
                  class="btn btn-primary flex-1 btn-sm"
                  :disabled="item.auditing"
                  @click="approve(item)"
                >通过</button>
                <button
                  type="button"
                  class="btn btn-outline flex-1 btn-sm"
                  :disabled="item.auditing"
                  @click="openReject(item)"
                >驳回</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="rejectTarget" class="modal-backdrop" @click.self="closeReject">
      <div class="modal-panel card card-pad stack-3">
        <h3 class="text-sm font-bold">驳回绑定申请</h3>
        <p class="text-xs muted">{{ rejectTarget.parentName }} 申请绑定 {{ rejectTarget.childName }}</p>
        <textarea v-model="rejectReason" class="textarea" rows="3" placeholder="请填写驳回原因（必填）"></textarea>
        <div class="row gap-2">
          <button type="button" class="btn btn-outline flex-1" @click="closeReject">取消</button>
          <button type="button" class="btn btn-primary flex-1" :disabled="!rejectReason.trim() || rejecting" @click="confirmReject">
            {{ rejecting ? '提交中…' : '确认驳回' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, nextTick } from 'vue'
import { useAppStore } from '@/stores/app'
import { fetchTeacherParentBinds, auditTeacherParentBind } from '@/api/teacher'
import PageBackButton from '@/components/PageBackButton.vue'

const appStore = useAppStore()
appStore.setActiveTab('profile')

const tabs = [
  { key: 'pending', label: '待审核' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '已驳回' }
]

const statusFilter = ref('pending')
const loading = ref(true)
const items = ref([])
const total = ref(0)
const rejectTarget = ref(null)
const rejectReason = ref('')
const rejecting = ref(false)

const emptyLabel = computed(() => {
  if (statusFilter.value === 'approved') return '已通过'
  if (statusFilter.value === 'rejected') return '已驳回'
  return '待审核'
})

function classLabel(item) {
  return [item.schoolName, item.gradeName, item.className].filter(Boolean).join(' · ') || '班级待确认'
}

function childInitial(name) {
  return (name || '学').charAt(0)
}

function statusLabel(status) {
  if (status === 'approved') return '已通过'
  if (status === 'rejected') return '已驳回'
  return '待审核'
}

function statusBadgeClass(status) {
  if (status === 'approved') return 'badge-success'
  if (status === 'rejected') return 'badge-danger'
  return 'badge-warning'
}

async function loadList() {
  loading.value = true
  try {
    const res = await fetchTeacherParentBinds({ status: statusFilter.value, page: 1, size: 50 })
    if (res?.code === 200 && res.data) {
      total.value = Number(res.data.total || 0)
      items.value = (res.data.records || []).map((row) => ({ ...row, auditing: false }))
    } else {
      items.value = []
      total.value = 0
    }
  } catch {
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
    initIcons()
  }
}

function switchTab(key) {
  if (statusFilter.value === key) return
  statusFilter.value = key
  loadList()
}

async function approve(item) {
  if (item.auditing) return
  item.auditing = true
  try {
    const res = await auditTeacherParentBind(item.bindId, { action: 'approve' })
    if (res?.code === 200) {
      await loadList()
    } else {
      alert(res?.message || '审核失败')
    }
  } catch (e) {
    alert(e?.response?.data?.message || '审核失败，请稍后重试')
  } finally {
    item.auditing = false
  }
}

function openReject(item) {
  rejectTarget.value = item
  rejectReason.value = ''
}

function closeReject() {
  rejectTarget.value = null
  rejectReason.value = ''
}

async function confirmReject() {
  const item = rejectTarget.value
  const reason = rejectReason.value.trim()
  if (!item || !reason || rejecting.value) return
  rejecting.value = true
  try {
    const res = await auditTeacherParentBind(item.bindId, { action: 'reject', rejectReason: reason })
    if (res?.code === 200) {
      closeReject()
      await loadList()
    } else {
      alert(res?.message || '驳回失败')
    }
  } catch (e) {
    alert(e?.response?.data?.message || '驳回失败，请稍后重试')
  } finally {
    rejecting.value = false
  }
}

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

onMounted(loadList)
onActivated(loadList)
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 50;
}
.modal-panel {
  width: 100%;
  max-width: 360px;
}
</style>
