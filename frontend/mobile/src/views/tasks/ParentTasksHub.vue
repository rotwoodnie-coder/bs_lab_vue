<template>
  <div class="parent-tasks-hub stack-4">
    <ChildPicker />
    <ParentDashboardBody />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide, watch } from 'vue'
import { fetchParentDashboard } from '@/api/home'
import { useParentContext } from '@/composables/useParentContext'
import ChildPicker from '@/views/home/ChildPicker.vue'
import ParentDashboardBody from '@/views/home/ParentDashboardBody.vue'

const dashboard = ref(null)
const { selectedChildId, children, loadChildren, selectChild } = useParentContext()

provide('parentSelectedId', selectedChildId)
provide('parentDashboard', dashboard)
provide('parentSelectChild', selectChild)
provide('parentChildren', computed(() => {
  if (children.value.length) return children.value
  return dashboard.value?.children || []
}))

function mapDashboard(data) {
  return {
    children: (data.children || []).map((c) => ({
      id: String(c.id),
      name: c.name,
      avatar: c.avatar || (c.name ? c.name.charAt(0) : '孩'),
      avatarUrl: c.avatarUrl || '',
      pending: c.pending ?? 0,
      completed: c.completed ?? 0
    })),
    activities: data.activitiesByChild || {},
    quizToday: data.quizTodayByChild || {}
  }
}

async function loadDashboard() {
  await loadChildren(true)
  try {
    const res = await fetchParentDashboard()
    if (res?.code === 200 && res.data) {
      dashboard.value = mapDashboard(res.data)
      if (!selectedChildId.value && res.data.children?.length) {
        const cur = res.data.children.find((c) => c.current) || res.data.children[0]
        if (cur?.id) await selectChild(String(cur.id))
      }
    }
  } catch (e) {
    console.warn('加载家长看板失败', e)
  }
}

watch(selectedChildId, () => {
  // 切换孩子时由 TasksView 重新拉任务列表
})

onMounted(loadDashboard)

defineExpose({ reload: loadDashboard })
</script>
