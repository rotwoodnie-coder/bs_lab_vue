import { ref, computed } from 'vue'
import { fetchParentChildren, setDefaultChild as apiSetDefaultChild } from '@/api/parent'
import { currentRole, isParentRole } from '@/utils/role'
import { getUserInfo } from '@/utils/authStorage'

const children = ref([])
const selectedChildId = ref('')
const loaded = ref(false)

function storageKey() {
  const uid = getUserInfo()?.userId || 'anon'
  return `mobile-parent-child-${uid}`
}

function loadStoredChildId() {
  try {
    return localStorage.getItem(storageKey()) || ''
  } catch {
    return ''
  }
}

function persistChildId(id) {
  try {
    if (id) localStorage.setItem(storageKey(), id)
    else localStorage.removeItem(storageKey())
  } catch { /* ignore */ }
}

const selectedChild = computed(() =>
  children.value.find((c) => c.id === selectedChildId.value) || children.value[0] || null
)

async function loadChildren(force = false) {
  if (!isParentRole(currentRole())) {
    children.value = []
    selectedChildId.value = ''
    loaded.value = true
    return []
  }
  if (loaded.value && !force && children.value.length) {
    return children.value
  }
  try {
    const res = await fetchParentChildren()
    const list = (res?.code === 200 && Array.isArray(res.data)) ? res.data : []
    children.value = list.map((c) => ({
      id: String(c.id),
      name: c.name,
      avatar: c.avatar || (c.name ? c.name.charAt(0) : '孩'),
      pending: c.pending ?? 0,
      completed: c.completed ?? 0,
      works: c.works ?? 0,
      classLabel: c.classLabel || '',
      isDefault: !!c.current,
      bindStatus: c.bindStatus
    }))
    const stored = loadStoredChildId()
    const defaultChild = children.value.find((c) => c.isDefault)
    const pick = children.value.find((c) => c.id === stored)
      || defaultChild
      || children.value[0]
    selectedChildId.value = pick?.id || ''
    persistChildId(selectedChildId.value)
  } catch (e) {
    console.warn('加载孩子列表失败', e)
    children.value = []
  } finally {
    loaded.value = true
  }
  return children.value
}

async function selectChild(id) {
  if (!id || id === selectedChildId.value) return
  selectedChildId.value = id
  persistChildId(id)
  try {
    await apiSetDefaultChild(id)
    children.value = children.value.map((c) => ({ ...c, isDefault: c.id === id }))
  } catch (e) {
    console.warn('设置默认孩子失败', e)
  }
}

function childQueryParam() {
  if (!isParentRole(currentRole())) return undefined
  return selectedChildId.value || undefined
}

export function useParentContext() {
  return {
    children,
    selectedChildId,
    selectedChild,
    loaded,
    loadChildren,
    selectChild,
    childQueryParam
  }
}
