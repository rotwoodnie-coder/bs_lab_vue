<template>
  <MobilePageShell class="safe-top theme-primary" data-layout="list-workbench">
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/admin" />
        <h1 class="pad-workbench__title flex-1">家长注册审核</h1>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top">
            <header class="topbar page-topbar">
              <PageBackButton fallback="/admin" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">家长注册审核</h1>
            </header>
          </div>

          <div class="px-4 stack-3">
            <div class="search-row">
              <input
                v-model="keyword"
                type="search"
                class="field-input"
                placeholder="搜索姓名 / 手机号 / 账号"
                @keyup.enter="reload"
              />
              <button type="button" class="btn btn-primary btn-sm" @click="reload">搜索</button>
            </div>
            <div class="chip-row">
              <button
                v-for="f in statusFilters"
                :key="f.key"
                type="button"
                class="chip"
                :class="{ 'chip--active': activeStatus === f.key }"
                @click="switchStatus(f.key)"
              >{{ f.label }}</button>
            </div>
          </div>

          <div class="px-4 pb-28 stack-3 mt-3">
            <div v-if="loading && items.length === 0" class="py-12 text-center muted-2">加载中…</div>
            <div v-else-if="items.length === 0" class="py-12 text-center muted-2 text-sm">暂无记录</div>

            <div v-for="item in items" :key="item.userId" class="card rounded-xl card-pad stack-2">
              <div class="row items-center justify-between">
                <div class="min-w-0">
                  <div class="text-sm font-bold">{{ item.userName || item.userNickName || item.loginName || '家长' }}</div>
                  <div class="text-xs muted mt-1">{{ maskPhone(item.userPhone) }}</div>
                </div>
                <span class="badge" :class="statusBadgeClass(item.status)">{{ statusLabel(item.status) }}</span>
              </div>
              <div class="text-xs muted">{{ item.rootOrgName || '未知学校' }} · {{ item.createTime || '' }}</div>
              <div v-if="item.status === 't'" class="row gap-2 mt-1">
                <button type="button" class="btn btn-danger btn-sm flex-1" :disabled="busyId === item.userId" @click="audit(item, 'reject')">拒绝</button>
                <button type="button" class="btn btn-primary btn-sm flex-1" :disabled="busyId === item.userId" @click="audit(item, 'approve')">通过</button>
              </div>
            </div>

            <button
              v-if="items.length < total"
              type="button"
              class="btn btn-ghost btn-block btn-sm"
              :disabled="loading"
              @click="loadMore"
            >{{ loading ? '加载中…' : '加载更多' }}</button>
          </div>
        </div>
      </div>
    </div>
  </MobilePageShell>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import MobilePageShell from '@/components/layout/MobilePageShell.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { useAppStore } from '@/stores/app'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { fetchParentRegistrations, auditParentRegistration } from '@/api/admin'
import { showToast } from '@/utils/toast'

const appStore = useAppStore()
const { initIcons } = useLucideIcons()
appStore.setActiveTab('admin')

const statusFilters = [
  { key: 't', label: '待审核' },
  { key: 'y', label: '已通过' },
  { key: 'n', label: '已拒绝' },
  { key: '', label: '全部' }
]

const keyword = ref('')
const activeStatus = ref('t')
const items = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const busyId = ref('')

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '—'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}
function statusLabel(s) {
  return s === 'y' ? '已通过' : s === 'n' ? '已拒绝' : s === 't' ? '待审核' : '其他'
}
function statusBadgeClass(s) {
  return s === 'y' ? 'badge-success' : s === 'n' ? 'badge-danger' : 'badge-warning'
}

async function load(reset = false) {
  if (loading.value) return
  loading.value = true
  if (reset) {
    pageNum.value = 1
    items.value = []
  }
  try {
    const res = await fetchParentRegistrations({
      keyword: keyword.value,
      status: activeStatus.value,
      pageNum: pageNum.value,
      pageSize
    })
    if (res?.code === 200 && res.data) {
      const records = res.data.records || res.data.list || []
      items.value = reset ? records : items.value.concat(records)
      total.value = Number(res.data.total ?? items.value.length)
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

function reload() {
  load(true)
}
function switchStatus(key) {
  if (activeStatus.value === key) return
  activeStatus.value = key
  load(true)
}
function loadMore() {
  pageNum.value += 1
  load(false)
}

async function audit(item, action) {
  if (busyId.value) return
  busyId.value = item.userId
  try {
    const res = await auditParentRegistration(item.userId, action)
    if (res?.code === 200) {
      showToast(action === 'approve' ? '已通过' : '已拒绝')
      load(true)
    } else {
      showToast(res?.message || '操作失败', 'danger')
    }
  } catch (e) {
    showToast(e?.response?.data?.message || '操作失败', 'danger')
  } finally {
    busyId.value = ''
  }
}

onMounted(() => load(true))
</script>

<style scoped>
.search-row { display: flex; gap: 8px; align-items: center; }
.field-input {
  flex: 1;
  padding: 9px 12px;
  border-radius: var(--radius-lg, 12px);
  border: 1px solid var(--border, #e2e8f0);
  background: var(--surface-1, #fff);
  font-size: var(--text-xs);
}
.chip-row { display: flex; gap: 8px; overflow-x: auto; }
.chip {
  flex-shrink: 0;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--surface-1, #fff);
  font-size: var(--text-xs);
  color: var(--text-2, #64748b);
}
.chip--active {
  background: var(--c-brand, #6366f1);
  border-color: var(--c-brand, #6366f1);
  color: #fff;
  font-weight: var(--weight-bold);
}
</style>
