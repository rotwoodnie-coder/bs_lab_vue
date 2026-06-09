import { defineStore } from 'pinia'

import { ref, computed } from 'vue'

import { useUserStore } from './user'



const CHAT_TAB = { key: 'chat', label: 'AI助手', icon: 'bot', route: '/chat', hasBadge: true }

const SHARED_TABS = [

  { key: 'home', label: '首页', icon: 'house', route: '/home' },

  { key: 'experiments', label: '模拟实验', icon: 'flask-conical', route: '/experiments' },

  { key: 'tasks', label: '我的任务', icon: 'clipboard-list', route: '/tasks', hasBadge: true },

  CHAT_TAB,

  { key: 'profile', label: '我的', icon: 'user', route: '/profile' }

]



export const useAppStore = defineStore('app', () => {

  const activeTab = ref('home')



  const tabs = computed(() => {

    const userStore = useUserStore()

    const role = (userStore.userInfo.userRoleId || 'student').toLowerCase()

    if (role === 'parent') {

      return SHARED_TABS.map((tab) => {

        if (tab.key === 'tasks') {

          return { ...tab, label: '孩子任务' }

        }

        if (tab.key === 'chat') {

          return { ...CHAT_TAB, hasBadge: false }

        }

        return tab

      })

    }

    return SHARED_TABS

  })



  function setActiveTab(key) {

    activeTab.value = key

  }



  return { activeTab, tabs, setActiveTab }

})

