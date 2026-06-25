<template>
  <div class="prototype-container pad-shell safe-top theme-primary" data-layout="list-workbench">
    <BottomNav />
    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton fallback="/profile" />
        <h1 class="pad-workbench__title flex-1">管理后台</h1>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-workbench__main">
          <div class="pad-workbench__mobile-head px-4 safe-top">
            <header class="topbar page-topbar">
              <PageBackButton fallback="/profile" />
              <h1 class="topbar-title text-xl flex-1 min-w-0">管理后台</h1>
            </header>
          </div>

          <div class="px-4 pb-28 stack-4">
            <div class="card rounded-xl card-pad">
              <div class="flex items-center gap-3">
                <div class="admin-hub__avatar"><i data-lucide="shield-check" class="icon icon-lg"></i></div>
                <div class="min-w-0">
                  <div class="text-sm font-bold">{{ roleLabel }}</div>
                  <div class="text-xs muted mt-1">{{ scopeLabel }}</div>
                </div>
              </div>
            </div>

            <router-link
              v-for="entry in entries"
              :key="entry.route"
              :to="entry.route"
              class="card card-link card-pad row items-center justify-between"
            >
              <div class="flex items-center gap-3 min-w-0">
                <span class="admin-hub__icon" :style="{ color: entry.color }">
                  <i :data-lucide="entry.icon" class="icon icon-lg"></i>
                </span>
                <div class="min-w-0">
                  <div class="text-sm font-bold">{{ entry.title }}</div>
                  <div class="text-xs muted mt-1">{{ entry.desc }}</div>
                </div>
              </div>
              <i data-lucide="chevron-right" class="icon muted"></i>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import { useUserStore } from '@/stores/user'
import { useProfileStore } from '@/stores/profile'
import { useAppStore } from '@/stores/app'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { isSysAdminRole } from '@/utils/role'

const userStore = useUserStore()
const profileStore = useProfileStore()
const { profile } = storeToRefs(profileStore)
const appStore = useAppStore()
const { initIcons } = useLucideIcons()
appStore.setActiveTab('admin')

const isSys = computed(() => isSysAdminRole(profile.value.userRoleId || userStore.userInfo.userRoleId))
const roleLabel = computed(() => (isSys.value ? '系统管理员' : '校管理员'))
const scopeLabel = computed(() => {
  if (isSys.value) return '管理范围：全局'
  return `管理范围：${profile.value.rootOrgName || profile.value.userOrgName || '本校'}`
})

const entries = computed(() => [
  { route: '/audits', icon: 'clipboard-check', color: 'var(--c-blue-600)', title: '审核中心', desc: '实验与学生作品审核' },
  { route: '/admin/work-reviews', icon: 'file-check', color: 'var(--c-rose-600)', title: '学生作品审核', desc: '审核待发布的学生创意/拍同款' },
  { route: '/admin/parent-registrations', icon: 'user-check', color: 'var(--c-emerald-600)', title: '家长注册审核', desc: '审核家长注册申请' },
  { route: '/admin/badges', icon: 'medal', color: 'var(--c-amber-600)', title: '勋章规则', desc: '配置成就勋章与积分奖励' },
  { route: '/admin/quiz-config', icon: 'settings-2', color: 'var(--c-violet-600)', title: '每日答题配置', desc: '设置答题数量与积分规则' }
])

onMounted(() => {
  profileStore.loadProfile().catch(() => {})
  nextTick(() => initIcons())
})
watch(entries, () => nextTick(() => initIcons()))
</script>

<style scoped>
.admin-hub__avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-2, rgba(99, 102, 241, 0.12));
  color: var(--c-brand, #6366f1);
  flex-shrink: 0;
}
.admin-hub__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg, 12px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-2, rgba(148, 163, 184, 0.12));
  flex-shrink: 0;
}
</style>
