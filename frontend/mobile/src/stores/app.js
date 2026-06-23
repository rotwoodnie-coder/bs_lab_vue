import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'
import { normalizeRole } from '@/utils/role'

const TAB_HOME = { key: 'home', label: '首页', icon: 'house', route: '/home' }
const TAB_EXPERIMENTS = { key: 'experiments', label: '模拟实验', icon: 'flask-conical', route: '/experiments' }
const TAB_TASKS = { key: 'tasks', label: '我的任务', icon: 'clipboard-list', route: '/tasks', hasBadge: true }
const TAB_CHAT = { key: 'chat', label: 'AI助手', icon: 'bot', route: '/chat', hasBadge: true }
const TAB_PROFILE = { key: 'profile', label: '我的', icon: 'user', route: '/profile' }

// 学生/家长/教师/教研员统一 5 Tab：首页(信息流) · 模拟实验 · 我的任务 · AI助手 · 我的
// 角色差异只体现在「首页 feed 的范围/筛选」，而不是替换某个 Tab。
const BASE_TABS = [TAB_HOME, TAB_EXPERIMENTS, TAB_TASKS, TAB_CHAT, TAB_PROFILE]

export const useAppStore = defineStore('app', () => {
  const activeTab = ref('home')

  const tabs = computed(() => {
    const userStore = useUserStore()
    const role = normalizeRole(userStore.userInfo.userRoleId)
    if (role === 'parent') {
      // 家长暂无 AI 助手未读提醒
      return BASE_TABS.map((tab) => (tab.key === 'chat' ? { ...TAB_CHAT, hasBadge: false } : tab))
    }
    return BASE_TABS
  })

  function setActiveTab(key) {
    activeTab.value = key
  }

  return { activeTab, tabs, setActiveTab }
})
