<template>
  <div class="page-shell">
    <el-container class="layout">
      <el-aside class="sidebar" :width="sidebarWidth">
        <div class="brand-wrap">
          <div class="brand-icon">
            <div class="brand-mark">
              <span class="brand-mark-top"></span>
              <span class="brand-mark-flask"></span>
              <span class="brand-mark-base"></span>
            </div>
          </div>
          <div v-show="!collapsed" class="brand-text-wrap">
            <div class="brand-title">科学实验云</div>
            <div class="brand-subtitle">实验教学平台</div>
          </div>
        </div>
        <el-menu
          class="menu"
          :default-active="activeMenu"
          router
          :collapse="collapsed"
          :collapse-transition="false"
          :default-openeds="defaultOpeneds"
          background-color="#ffffff"
          text-color="#1f2d3d"
          active-text-color="#2563eb"
        >
          <template v-for="item in menuConfig" :key="item.key">
            <el-menu-item v-if="!hasChildren(item)" :index="item.path">
              <img
                class="menu-svg-icon"
                :src="resolveMenuIcon(item.icon)"
                :alt="`${item.label} icon`"
                @error="handleIconError"
              />
              <span>{{ item.label }}</span>
            </el-menu-item>
            <el-sub-menu v-else :index="item.key">
              <template #title>
                <img
                  class="menu-svg-icon"
                  :src="resolveMenuIcon(item.icon)"
                  :alt="`${item.label} icon`"
                  @error="handleIconError"
                />
                <span>{{ item.label }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                <img
                  class="menu-svg-icon menu-svg-icon--nested"
                  :src="resolveMenuIcon(child.icon)"
                  :alt="`${child.label} icon`"
                  @error="handleIconError"
                />
                <span>{{ child.label }}</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-button link class="collapse-btn" @click="toggleCollapse">
              {{ collapsed ? '展开菜单' : '收起菜单' }}
            </el-button>
            <div class="header-title">{{ rootOrgName }}</div>
          </div>
          <div class="header-user">
            <MessageBell />
            <el-dropdown trigger="click" @command="handleUserCommand">
              <span class="user-dropdown-trigger">
                <span class="user-name">{{ userName }}</span>
                <el-icon class="user-dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="password">更改口令</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="content">
          <router-view />
        </el-main>
        <ChangePasswordDialog v-model="changePasswordVisible" />
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, markRaw, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChangePasswordDialog from '../components/ChangePasswordDialog.vue'
import MessageBell from '../components/MessageBell.vue'
import { Aim, ArrowDown, Avatar, ChatDotRound, Collection, DataAnalysis, Document, EditPen, Files, FolderOpened, HomeFilled, List, Memo, Menu, Notebook, OfficeBuilding, Picture, Reading, Setting, User, UserFilled, VideoPlay, WarningFilled } from '@element-plus/icons-vue'
import { fetchVisibleMenus } from '../api/system'
import { publicUrl } from '@/utils/publicUrl'

const route = useRoute()
const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const collapsed = ref(false)
const changePasswordVisible = ref(false)
const menuConfig = ref([])
const menuCacheKey = `visible-menus-${userInfo.userId || 'guest'}`

const activeMenu = computed(() => route.path)
const userName = computed(() => userInfo.username || '管理员')
const rootOrgName = computed(() => userInfo.rootOrgName || '未绑定机构')
const sidebarWidth = computed(() => (collapsed.value ? '64px' : '200px'))
const defaultOpeneds = []

const iconMap = {
  HomeFilled: markRaw(HomeFilled),
  Reading: markRaw(Reading),
  Notebook: markRaw(Notebook),
  UserFilled: markRaw(UserFilled),
  Memo: markRaw(Memo),
  Aim: markRaw(Aim),
  EditPen: markRaw(EditPen),
  WarningFilled: markRaw(WarningFilled),
  VideoPlay: markRaw(VideoPlay),
  Document: markRaw(Document),
  FolderOpened: markRaw(FolderOpened),
  Files: markRaw(Files),
  Picture: markRaw(Picture),
  Collection: markRaw(Collection),
  List: markRaw(List),
  Setting: markRaw(Setting),
  User: markRaw(User),
  Avatar: markRaw(Avatar),
  Menu: markRaw(Menu),
  OfficeBuilding: markRaw(OfficeBuilding),
  DataAnalysis: markRaw(DataAnalysis),
  ChatDotRound: markRaw(ChatDotRound)
}

const resolveIcon = (name) => iconMap[name] || HomeFilled

const resolveMenuIcon = (componentName) => {
  const iconName = String(componentName || 'default').replace(/^\//, '')
  if (iconName.includes('/')) {
    return publicUrl(iconName)
  }
  return publicUrl(`icons/${iconName}.svg`)
}

const handleIconError = (event) => {
  if (event?.target) {
    event.target.src = publicUrl('icons/default.svg')
  }
}

const hasChildren = (item) => Array.isArray(item.children) && item.children.length > 0

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const handleUserCommand = async (command) => {
  if (command === 'password') {
    changePasswordVisible.value = true
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem(menuCacheKey)
  router.push('/login')
}

const normalizeMenuNode = (node) => ({
  key: node.key || node.path,
  path: node.path,
  label: node.label,
  icon: node.icon,
  component: node.component,
  children: Array.isArray(node.children) ? node.children.map(normalizeMenuNode) : []
})

const loadVisibleMenus = async () => {
  const cached = localStorage.getItem(menuCacheKey)
  if (cached) {
    try {
      menuConfig.value = JSON.parse(cached)
      return
    } catch (error) {
      localStorage.removeItem(menuCacheKey)
    }
  }

  try {
    const res = await fetchVisibleMenus()
    if (res.data.code === 200) {
      const list = Array.isArray(res.data.data) ? res.data.data : []
      const normalized = list.map(normalizeMenuNode)
      menuConfig.value = normalized
      localStorage.setItem(menuCacheKey, JSON.stringify(normalized))
    } else {
      menuConfig.value = []
    }
  } catch (error) {
    menuConfig.value = []
  }
}

onMounted(loadVisibleMenus)
</script>

<style scoped>
.menu-svg-icon {
  width: 28px;
  height: 28px;
  margin-right: 8px;
  object-fit: contain;
  flex-shrink: 0;
}

.menu-svg-icon--nested {
  width: 26px;
  height: 26px;
}
</style>
