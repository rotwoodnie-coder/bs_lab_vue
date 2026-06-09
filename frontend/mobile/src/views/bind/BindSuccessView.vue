<template>
  <div class="screen pad-success" data-layout="bind-success-split">
    <div class="success-split">
      <section class="success-split__left">
        <div class="text-center mb-5">
          <span class="title-emoji">{{ pageEmoji }}</span>
          <h1 class="success-title">{{ pageTitle }}</h1>
          <p class="success-sub">{{ pageSubtitle }}</p>
        </div>

        <div v-if="accountStatusLabel" class="audit-card audit-account mb-3">
          <div class="row items-center justify-between mb-2">
            <div class="audit-card__title">账号状态</div>
            <span class="audit-card__status" :class="accountBadgeClass">
              <i :data-lucide="accountIcon" class="icon badge-icon"></i>
              <span>{{ accountStatusLabel }}</span>
            </span>
          </div>
          <div class="audit-card__desc">{{ accountHint }}</div>
        </div>

        <div class="audit-card mb-4" :class="bindAuditCardClass">
          <div class="row items-center justify-between mb-2">
            <div class="audit-card__title">绑定审核</div>
            <span class="audit-card__status">
              <i :data-lucide="bindStatusIcon" class="icon badge-icon"></i>
              <span>{{ bindStatusText }}</span>
            </span>
          </div>
          <div class="audit-card__desc">{{ auditDescription }}</div>
          <div v-if="primaryBindStatus === 'pending'" class="mt-2 text-xs" style="opacity:0.7;">预计 1-3 个工作日</div>
        </div>

        <div v-if="loading" class="text-sm muted mb-4">正在加载申请信息…</div>

        <div v-else-if="showChildCard" class="child-card mb-4">
          <div class="child-avatar">{{ primaryItem.childName.charAt(0) || '?' }}</div>
          <div class="child-name">{{ primaryItem.childName }}</div>
          <div class="child-class">{{ classLabel(primaryItem) || '班级信息待确认' }}</div>
          <div v-if="primaryItem.relation" class="child-relation text-xs muted mt-1">关系：{{ primaryItem.relation }}</div>
          <div v-if="primaryItem.submitTime" class="text-xs muted mt-2">申请时间：{{ primaryItem.submitTime }}</div>
          <div class="child-badge" :class="childBadgeClass">
            <i :data-lucide="bindStatusIcon" class="icon badge-icon"></i>
            <span>{{ bindStatusText }}</span>
          </div>
          <div v-if="primaryItem.rejectReason" class="text-xs text-danger mt-3 text-left">
            驳回原因：{{ primaryItem.rejectReason }}
          </div>
        </div>

        <div v-else-if="!loading && !primaryItem.childName" class="child-card mb-4">
          <div class="child-avatar">?</div>
          <div class="child-name">暂无申请详情</div>
          <div class="child-class">{{ restricted ? '请耐心等待审核' : '请稍后在个人中心查看审核进度' }}</div>
        </div>

        <div class="apply-list stack-2 mb-4" v-if="showApplyList">
          <div class="text-xs font-semibold muted-2 text-left">绑定申请记录</div>
          <div v-for="item in applications" :key="item.bindId || item.childName" class="apply-item">
            <div class="apply-item__info">
              <div class="apply-item__avatar">{{ (item.childName || '?').charAt(0) }}</div>
              <div>
                <div class="apply-item__name">{{ item.childName || '未填写' }}</div>
                <div class="apply-item__class">{{ classLabel(item) || '班级信息待确认' }}</div>
                <div v-if="item.relation" class="apply-item__relation text-xs muted mt-1">关系：{{ item.relation }}</div>
                <div v-if="item.rejectReason" class="text-xs text-danger mt-1">驳回：{{ item.rejectReason }}</div>
              </div>
            </div>
            <span class="apply-item__badge badge" :class="itemBadgeClass(item.bindStatus)">
              <i :data-lucide="statusIcon(item.bindStatus)" class="icon" style="width:14px;height:14px;"></i>
              <span>{{ statusText(item.bindStatus) }}</span>
            </span>
          </div>
        </div>
      </section>

      <section class="success-split__right">
        <div class="actions">
          <template v-if="celebrationMode">
            <button type="button" class="btn btn-gradient btn-block btn-lg" @click="enterParentHome">
              <i data-lucide="home" class="icon action-icon"></i>
              进入孩子任务
            </button>
            <router-link
              to="/bind-child"
              class="btn btn-outline btn-block btn-lg btn-secondary-action mt-2"
            >
              <i data-lucide="user-plus" class="icon action-icon"></i>
              继续绑定其他孩子
            </router-link>
          </template>
          <template v-else-if="restricted">
            <router-link
              v-if="canRebind"
              to="/bind-child"
              class="btn btn-gradient btn-block btn-lg"
            >
              <i data-lucide="refresh-cw" class="icon action-icon"></i>
              重新绑定
            </router-link>
            <button type="button" class="btn btn-outline btn-block btn-lg" :class="{ 'mt-2': canRebind }" @click="handleLogout">
              退出登录
            </button>
          </template>
          <template v-else>
            <router-link
              v-if="loggedIn && fromBind"
              to="/bind-child"
              class="btn btn-outline btn-block btn-lg btn-secondary-action"
            >
              <i data-lucide="user-plus" class="icon action-icon"></i>
              <span>新增绑定孩子</span>
            </router-link>
            <router-link
              v-if="!loggedIn"
              :to="loginLink"
              class="btn btn-gradient btn-block btn-lg"
            >去登录</router-link>
            <router-link
              v-else-if="loggedIn"
              to="/profile"
              class="btn btn-gradient btn-block btn-lg"
            >返回个人中心</router-link>
            <router-link
              v-if="loggedIn && fromRegister"
              to="/home"
              class="btn btn-outline btn-block btn-lg btn-secondary-action mt-2"
            >返回首页</router-link>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { isLoggedIn, getUserInfo } from '@/utils/authStorage'
import {
  isParentRestricted,
  shouldShowParentApprovalCelebration,
  markParentApprovalAcknowledged,
  syncParentAccessState
} from '@/utils/parentAccess'
import { useUserStore } from '@/stores/user'
import { fetchBindApplications } from '@/api/parent'
import {
  readBindSuccessSnapshot,
  normalizeBindItem,
  formatClassLabel
} from '@/utils/bindSuccessNav'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loggedIn = computed(() => isLoggedIn())
const restricted = computed(() => loggedIn.value && isParentRestricted())
const celebrationMode = computed(() => {
  if (!loggedIn.value || restricted.value) return false
  if (route.query.from === 'approved') return true
  return shouldShowParentApprovalCelebration(getUserInfo())
})
const loading = ref(false)
const applicationsFromApi = ref([])
const accountStatus = ref('')
const accountStatusLabel = ref('')

const fromRegister = computed(() => route.query.from === 'register')
const fromBind = computed(() => route.query.from !== 'register')

const queryItem = computed(() => normalizeBindItem({
  bindId: route.query.bindId,
  childName: route.query.name,
  schoolName: route.query.schoolName,
  gradeName: route.query.gradeName,
  className: route.query.className,
  relation: route.query.relation,
  message: route.query.message,
  bindStatus: route.query.bindStatus || 'pending'
}))

const snapshotItem = ref(null)

const applications = computed(() => {
  if (applicationsFromApi.value.length) return applicationsFromApi.value
  if (primaryItem.value.childName) return [primaryItem.value]
  return []
})

const primaryItem = computed(() => {
  const q = queryItem.value
  if (q.childName || q.schoolName || q.gradeName || q.className) return q
  if (snapshotItem.value) return snapshotItem.value
  if (applicationsFromApi.value.length) {
    const pending = applicationsFromApi.value.find((i) => i.bindStatus === 'pending')
    const rejected = applicationsFromApi.value.find((i) => i.bindStatus === 'rejected')
    return pending || rejected || applicationsFromApi.value[0]
  }
  return normalizeBindItem({})
})

const primaryBindStatus = computed(() => {
  const apps = applicationsFromApi.value
  if (apps.some((i) => i.bindStatus === 'pending')) return 'pending'
  if (apps.some((i) => i.bindStatus === 'rejected')) return 'rejected'
  if (apps.length && apps.every((i) => i.bindStatus === 'approved')) return 'approved'
  return primaryItem.value.bindStatus || 'pending'
})

const showApplyList = computed(() => applications.value.length > 1)
const showChildCard = computed(() => !loading.value && !!primaryItem.value.childName && !showApplyList.value)

const canRebind = computed(() =>
  primaryBindStatus.value === 'rejected'
  || applicationsFromApi.value.some((i) => i.bindStatus === 'rejected')
)

const pageEmoji = computed(() => {
  if (primaryBindStatus.value === 'approved') return '✅'
  if (primaryBindStatus.value === 'rejected') return '❌'
  return '⏳'
})

const pageTitle = computed(() => {
  if (fromRegister.value && primaryBindStatus.value === 'pending') return '注册申请已提交'
  if (primaryBindStatus.value === 'approved') return '绑定已通过'
  if (primaryBindStatus.value === 'rejected') return '绑定未通过'
  return fromRegister.value ? '注册申请已提交' : '申请已提交'
})

const pageSubtitle = computed(() => {
  if (celebrationMode.value) {
    return '绑定已审核通过，点击下方按钮进入家长端开始使用'
  }
  if (restricted.value) {
    if (primaryBindStatus.value === 'rejected') return '请核对信息后重新提交绑定申请'
    return '审核通过前仅可查看申请状态，暂无法进入家长端'
  }
  if (fromRegister.value && !loggedIn.value) return '审核通过后可登录家长端'
  return '请耐心等待学校审核结果'
})

const bindStatusText = computed(() => statusText(primaryBindStatus.value))
const bindStatusIcon = computed(() => statusIcon(primaryBindStatus.value))
const bindAuditCardClass = computed(() => {
  if (primaryBindStatus.value === 'approved') return 'audit-approved'
  if (primaryBindStatus.value === 'rejected') return 'audit-rejected'
  return 'audit-pending'
})
const childBadgeClass = computed(() => {
  if (primaryBindStatus.value === 'approved') return 'badge-approved'
  if (primaryBindStatus.value === 'rejected') return 'badge-rejected'
  return 'badge-pending'
})

const accountHint = computed(() => {
  if (accountStatus.value === 't') return '账号注册后需等待学校激活，激活后可使用完整家长端功能。'
  if (accountStatus.value === 'n') return '账号未通过审核，请联系学校管理员。'
  if (accountStatus.value === 'y') return '账号已激活。'
  return ''
})

const accountBadgeClass = computed(() => {
  if (accountStatus.value === 'y') return 'badge-approved'
  if (accountStatus.value === 'n') return 'badge-rejected'
  return 'badge-pending'
})

const accountIcon = computed(() => {
  if (accountStatus.value === 'y') return 'check-circle'
  if (accountStatus.value === 'n') return 'x-circle'
  return 'clock'
})

const auditDescription = computed(() => {
  const custom = primaryItem.value.message || String(route.query.message || '')
  if (celebrationMode.value) {
    return '孩子绑定已审核通过，您可以使用协助上传、查看任务与成长档案等完整家长端功能。'
  }
  if (primaryBindStatus.value === 'approved') {
    return '孩子绑定已审核通过，若账号也已激活即可使用家长端完整功能。'
  }
  if (primaryBindStatus.value === 'rejected') {
    const reason = primaryItem.value.rejectReason
    return reason
      ? `绑定申请被驳回：${reason}。请核对孩子信息后重新提交。`
      : '绑定申请被驳回，请核对孩子信息后重新提交。'
  }
  if (custom) return custom
  if (fromRegister.value) {
    return '注册申请已提交，账号与孩子绑定均待学校审核，通过后可使用家长端功能。'
  }
  return '绑定学生申请已经发送，请等待班级教师审核。审核通过后您将收到通知。'
})

const loginLink = computed(() => {
  const phone = route.query.phone
  return phone ? { path: '/login', query: { phone: String(phone) } } : '/login'
})

function statusText(status) {
  if (status === 'approved') return '已通过'
  if (status === 'rejected') return '已驳回'
  return '审核中'
}

function statusIcon(status) {
  if (status === 'approved') return 'check-circle'
  if (status === 'rejected') return 'x-circle'
  return 'clock'
}

function itemBadgeClass(status) {
  if (status === 'approved') return 'badge-success'
  if (status === 'rejected') return 'badge-danger'
  return 'badge-warning'
}

function classLabel(item) {
  return formatClassLabel(item)
}

async function handleLogout() {
  await userStore.logoutAndRedirect()
  router.replace('/login')
}

function enterParentHome() {
  markParentApprovalAcknowledged(getUserInfo()?.userId)
  router.replace('/tasks')
}

async function refreshPageState() {
  if (loggedIn.value) {
    await syncParentAccessState()
    userStore.userInfo = getUserInfo()
  }
  await loadApplicationsFromApi()
  if (!isParentRestricted(getUserInfo())) {
    stopPendingPoll()
  } else if (primaryBindStatus.value === 'pending') {
    startPendingPoll()
  } else {
    stopPendingPoll()
  }
}

let pollTimer = null

function startPendingPoll() {
  if (pollTimer) return
  pollTimer = window.setInterval(() => {
    refreshPageState()
  }, 15000)
}

function stopPendingPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function loadApplicationsFromApi() {
  if (!loggedIn.value) return
  loading.value = true
  try {
    const res = await fetchBindApplications()
    if (res?.code === 200 && res.data) {
      accountStatus.value = res.data.accountStatus || userStore.userInfo.status || ''
      accountStatusLabel.value = res.data.accountStatusLabel || ''
      applicationsFromApi.value = (res.data.applications || []).map(normalizeBindItem)
    }
  } catch {
    accountStatus.value = userStore.userInfo.status || ''
  } finally {
    loading.value = false
  }
}

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => createIcons({ icons })).catch(() => {})
  })
}

onMounted(async () => {
  snapshotItem.value = readBindSuccessSnapshot()
    ? normalizeBindItem(readBindSuccessSnapshot())
    : null
  await refreshPageState()
  initIcons()
})

onUnmounted(() => {
  stopPendingPoll()
})
</script>

<style scoped>
</style>

<style>
.screen.pad-success {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #fef3c7 0%, #f1f5f9 45%);
  padding: var(--space-5);
  position: relative;
  overflow: hidden;
}
.success-split {
  width: 100%;
  max-width: 380px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  padding: var(--space-6) var(--space-5) var(--space-5);
  text-align: center;
  position: relative;
  z-index: 2;
}
.title-emoji {
  font-size: 56px;
  line-height: 1;
  display: block;
  margin-bottom: var(--space-3);
  animation: emojiPop 0.6s var(--ease) 0.1s both;
}
@keyframes emojiPop {
  0% { transform: scale(0) rotate(-20deg); opacity: 0; }
  60% { transform: scale(1.2) rotate(4deg); }
  100% { transform: scale(1) rotate(0deg); opacity: 1; }
}
.success-title { font-size: var(--text-2xl); font-weight: var(--weight-bold); margin-bottom: var(--space-1); }
.success-sub { font-size: var(--text-sm); color: var(--color-text-2); margin-bottom: var(--space-5); }
.child-card {
  background: var(--color-surface-2);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  margin-bottom: 0;
  border: 1px solid var(--color-border-soft);
}
.child-avatar {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: var(--gradient-amber-rose);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto var(--space-3);
  font-size: 30px; font-weight: var(--weight-bold); color: #fff;
  box-shadow: 0 4px 16px rgba(217,119,6,0.24);
}
.child-name { font-size: var(--text-xl); font-weight: var(--weight-bold); }
.child-class { font-size: var(--text-xs); color: var(--color-text-3); margin-top: var(--space-1); }
.child-badge {
  display: inline-flex; align-items: center; gap: 6px;
  margin-top: var(--space-3);
  padding: 5px 14px; border-radius: var(--radius-full);
  font-size: var(--text-xs); font-weight: var(--weight-bold);
}
.child-badge.badge-pending { background: var(--c-amber-100); color: var(--c-amber-600); }
.child-badge.badge-approved { background: var(--c-emerald-100); color: var(--c-emerald-700); }
.child-badge.badge-rejected { background: var(--c-rose-100); color: var(--c-rose-700); }
.child-badge .badge-icon { width: 14px; height: 14px; }
.audit-card {
  text-align: left;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-soft);
}
.audit-card.audit-pending { background: var(--c-amber-100); border-color: var(--c-amber-200); }
.audit-card.audit-approved { background: var(--c-emerald-100); border-color: var(--c-emerald-200); }
.audit-card.audit-rejected { background: var(--c-rose-100); border-color: var(--c-rose-200); }
.audit-card.audit-account { background: var(--color-surface-2); }
.audit-card__status {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px; border-radius: var(--radius-full);
  font-size: var(--text-xs); font-weight: var(--weight-bold);
}
.audit-card.audit-pending .audit-card__status { background: var(--c-amber-200); color: var(--c-amber-700); }
.audit-card.audit-approved .audit-card__status,
.audit-card__status.badge-approved { background: var(--c-emerald-200); color: var(--c-emerald-700); }
.audit-card.audit-rejected .audit-card__status,
.audit-card__status.badge-rejected { background: var(--c-rose-200); color: var(--c-rose-700); }
.audit-card__status.badge-pending { background: var(--c-amber-200); color: var(--c-amber-700); }
.audit-card__title { font-size: var(--text-sm); font-weight: var(--weight-bold); margin-bottom: var(--space-1); }
.audit-card__desc { font-size: var(--text-xs); line-height: 1.5; opacity: 0.85; }
.apply-list { margin-top: 16px; }
.apply-item {
  display: flex;
  align-items: center; justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-surface-2);
  border: 1px solid var(--color-border-soft);
}
.apply-item + .apply-item { margin-top: var(--space-2); }
.apply-item__info { display: flex; align-items: center; gap: var(--space-3); min-width: 0; text-align: left; }
.apply-item__avatar {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: var(--weight-bold); color: #fff;
  flex-shrink: 0;
  background: var(--gradient-amber-rose);
}
.apply-item__name { font-size: var(--text-sm); font-weight: var(--weight-bold); color: var(--color-text-1); }
.apply-item__class { font-size: var(--text-xs); color: var(--color-text-3); margin-top: 1px; }
.apply-item__badge { flex-shrink: 0; }

.actions { margin-top: var(--space-4); }
.actions .btn + .btn,
.actions .btn-secondary-action + .btn { margin-top: var(--space-2); }
.btn-secondary-action {
  display: flex;
  align-items: center; justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-surface-2);
  color: var(--color-text-2);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  border: 1px solid var(--color-border-soft);
  text-decoration: none;
}
.btn-secondary-action:active { transform: scale(0.98); background: var(--color-border); }
.btn-secondary-action .action-icon { width: 18px; height: 18px; }
</style>
