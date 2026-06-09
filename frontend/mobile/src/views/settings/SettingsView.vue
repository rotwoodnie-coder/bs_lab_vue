<template>
  <div
    class="prototype-container safe-top pad-shell"
    data-layout="settings-split"
    :data-settings-role="roleKey"
    :class="{ 'pad-settings--mobile-pane-open': isMobileSettingsLayout && !!activeSection }"
  >
    <BottomNav />

    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <button type="button" class="pad-workbench__back" aria-label="返回" @click="goBack">
          <i data-lucide="arrow-left" class="icon"></i>
        </button>
        <h1 class="pad-workbench__title">设置</h1>
        <div class="pad-workbench__topbar-actions"></div>
      </header>

      <!-- ===== 手机端顶栏 ===== -->
      <div class="px-4 safe-top pad-settings__mobile-head-wrap">
        <div class="pad-workbench__mobile-head topbar page-topbar">
          <button type="button" class="icon-btn page-back pad-settings__back-link" aria-label="返回" @click="handleMobileBack">
            <i data-lucide="arrow-left" class="icon"></i>
          </button>
          <h1 class="topbar-title text-xl">{{ activeSection ? currentPaneTitle : '设置' }}</h1>
        </div>
      </div>

      <!-- ===== 手机端首页：用户卡片 + 菜单列表 ===== -->
      <div class="pad-settings__mobile-only">
        <div class="pad-settings__mobile-home">
          <div class="px-4 mt-3 mb-1">
            <div class="card card-pad row items-center gap-3 w-full pad-settings__mobile-user">
              <div class="avatar avatar-lg" :class="avatarClass" style="font-size:24px;">{{ userInitial }}</div>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-bold truncate">{{ displayName }}</div>
                <div class="text-xs muted truncate">{{ mobileUserSubline }}</div>
              </div>
            </div>
          </div>

          <div class="px-4 pad-settings__mobile-menu stack">
            <button
              v-for="tab in visibleTabs"
              :key="tab.key"
              type="button"
              class="card card-pad row items-center justify-between w-full text-left"
              :data-role-only="tab.roleOnly || null"
              @click="openSection(tab.key)"
            >
              <div class="row items-center gap-3">
                <i :data-lucide="tab.icon" class="icon icon-lg" style="color:var(--color-text-2);"></i>
                <span class="text-sm font-medium">{{ tab.label }}</span>
              </div>
              <i data-lucide="chevron-right" class="icon muted-2"></i>
            </button>
          </div>

          <div class="px-4 mt-6 mb-4">
            <button type="button" class="btn btn-danger btn-block" @click="handleLogout">
              <i data-lucide="log-out" class="icon"></i> 退出登录
            </button>
          </div>
        </div>
      </div>

      <!-- ===== 平板双栏 / 手机子页 ===== -->
      <div class="pad-workbench__body">
        <nav class="pad-settings__nav" aria-label="设置分组">
          <button
            v-for="tab in visibleTabs"
            :key="tab.key"
            type="button"
            class="card card-pad row items-center gap-3 text-left pad-settings__nav-item"
            :class="{ active: activeSection === tab.key }"
            :data-role-only="tab.roleOnly || null"
            @click="openSection(tab.key)"
          >
            <i :data-lucide="tab.icon" class="icon"></i>
            <span class="text-sm font-medium">{{ tab.label }}</span>
          </button>
          <div class="pad-settings__nav-logout">
            <button type="button" class="btn btn-danger btn-block" @click="handleLogout">
              <i data-lucide="log-out" class="icon"></i> 退出登录
            </button>
          </div>
        </nav>

        <div class="pad-settings__panel">
          <!-- ===== 个人资料 ===== -->
          <section class="pad-settings__section" :class="{ 'is-active': activeSection === 'profile' }">
            <div class="settings-panel-head">
              <h2>个人资料</h2>
              <p>与「我的」页展示信息同步，修改后即时生效</p>
            </div>
            <div class="col items-center settings-profile-form">
              <!-- 头像 -->
              <div class="relative" @click="openAvatarSheet">
                <div class="avatar avatar-xl" :class="avatarClass" style="font-size:36px;">{{ userInitial }}</div>
                <div class="absolute row items-center justify-center" style="bottom:-4px;right:-4px;width:28px;height:28px;border-radius:var(--radius-full);background:var(--color-surface);box-shadow:var(--shadow-sm);cursor:pointer;">
                  <i data-lucide="camera" class="icon icon-sm muted"></i>
                </div>
              </div>
              <div class="text-xs muted mt-2" @click="openAvatarSheet" style="cursor:pointer;">点击更换头像</div>

              <!-- 角色标签：学生 -->
              <div v-if="isStudent" class="settings-profile-meta">
                <span class="pill text-xs">小小科学家</span>
                <span class="pill text-xs tint-violet">🏅 {{ badgeCountLabel }} 枚勋章</span>
              </div>
              <!-- 角色标签：家长 -->
              <div v-if="isParent" class="settings-profile-meta">
                <span class="pill text-xs">家长账号</span>
                <span class="pill text-xs">绑定 {{ profile.childrenCount || 0 }} 个孩子</span>
              </div>
              <!-- 角色标签：教师 -->
              <div v-if="isTeacher" class="settings-profile-meta">
                <span class="pill text-xs">科学教师</span>
                <span class="pill text-xs">{{ teacherClassLabel }}</span>
              </div>

              <!-- 学生：只读字段 -->
              <template v-if="isStudent">
                <div class="mt-5 w-full">
                  <label class="text-xs font-medium muted block mb-1">姓名</label>
                  <input type="text" class="input settings-field-readonly" :value="form.userName" readonly disabled />
                </div>
                <div class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">年级/班级</label>
                  <input type="text" class="input settings-field-readonly" :value="userOrgNameLabel || '-'" readonly disabled />
                </div>
                <div class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">学号</label>
                  <input type="text" class="input settings-field-readonly" :value="studentNoLabel" readonly disabled />
                </div>
                <div class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">学校</label>
                  <input type="text" class="input settings-field-readonly" :value="profile.rootOrgName || '-'" readonly disabled />
                </div>
              </template>

              <!-- 家长：可编辑 -->
              <template v-if="isParent">
                <div class="mt-5 w-full">
                  <label class="text-xs font-medium muted block mb-1">昵称</label>
                  <div class="row items-center gap-2">
                    <input v-model="form.userNickName" type="text" class="input flex-1" placeholder="昵称" />
                    <i data-lucide="pencil" class="icon muted-2" style="cursor:pointer;"></i>
                  </div>
                </div>
                <div class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">与孩子关系</label>
                  <select v-model="parentRelation" class="input w-full">
                    <option value="妈妈">妈妈</option>
                    <option value="爸爸">爸爸</option>
                    <option value="其他监护人">其他监护人</option>
                  </select>
                </div>
              </template>

              <!-- 教师：可编辑 + 只读 -->
              <template v-if="isTeacher">
                <div class="mt-5 w-full">
                  <label class="text-xs font-medium muted block mb-1">昵称</label>
                  <div class="row items-center gap-2">
                    <input v-model="form.userNickName" type="text" class="input flex-1" placeholder="昵称" />
                    <i data-lucide="pencil" class="icon muted-2" style="cursor:pointer;"></i>
                  </div>
                </div>
                <div class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">任教班级</label>
                  <input type="text" class="input settings-field-readonly" :value="teacherTeachClasses" readonly disabled />
                </div>
                <div class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">工号</label>
                  <input type="text" class="input settings-field-readonly" :value="teacherWorkNo" readonly disabled />
                </div>
                <div v-if="profile.prefTitleName" class="mt-3 w-full">
                  <label class="text-xs font-medium muted block mb-1">职称</label>
                  <input type="text" class="input settings-field-readonly" :value="profile.prefTitleName" readonly disabled />
                </div>
              </template>

              <!-- 家长：绑定孩子（原「孩子与家长」页合并） -->
              <div v-if="isParent" class="mt-5 w-full">
                <label class="text-xs font-medium muted block mb-2">已绑定孩子</label>
                <div v-if="!prototypeChildren.length" class="text-xs muted mb-2">暂未绑定孩子</div>
                <div v-else class="stack-2 mb-2">
                  <div
                    v-for="child in prototypeChildren"
                    :key="child.id || child.name"
                    class="row items-center gap-3 surface-2 rounded-xl p-3"
                  >
                    <div class="avatar avatar-sm" :class="child.isDefault ? 'avatar-grad-warm' : 'avatar-grad-cool'">{{ child.initial }}</div>
                    <div class="flex-1 min-w-0">
                      <div class="text-sm font-medium">{{ child.name }}</div>
                      <div v-if="child.hint" class="text-xs muted truncate">{{ child.hint }}</div>
                    </div>
                    <span v-if="child.isDefault" class="pill text-xs">默认</span>
                    <button v-else type="button" class="btn btn-ghost btn-sm text-xs" @click="switchSettingsChild(child)">设为默认</button>
                  </div>
                </div>
                <router-link to="/bind-child" class="btn btn-outline btn-sm w-full">+ 绑定新孩子</router-link>
              </div>

              <!-- 全校共用：个性签名 -->
              <div class="mt-3 w-full">
                <label class="text-xs font-medium muted block mb-1">个性签名</label>
                <textarea v-model="form.perResume" class="input textarea" rows="2" placeholder="写一句介绍自己吧…"></textarea>
              </div>

              <button type="button" class="btn btn-gradient mt-5 w-full" :disabled="saving" @click="saveProfile">
                <i data-lucide="save" class="icon"></i> {{ saving ? '保存中…' : '保存资料' }}
              </button>
              <p v-if="profileMsg" class="text-sm mt-2" :class="profileMsgType === 'error' ? 'text-danger' : 'text-success'">{{ profileMsg }}</p>
            </div>
          </section>

          <!-- ===== 账号与安全 ===== -->
          <section class="pad-settings__section" :class="{ 'is-active': activeSection === 'account' }">
            <div class="settings-panel-head">
              <h2>账号与安全</h2>
              <p>保护账号安全，管理登录方式</p>
            </div>

            <!-- 各角色统一：简洁版（参考学生） -->
            <div class="settings-group">
              <div class="settings-card">
                <div class="settings-row settings-row--action" @click="openSection('password')">
                  <div class="settings-row__main">
                    <div class="settings-row__label">登录密码</div>
                    <div class="settings-row__hint">建议定期更换，不少于 8 位</div>
                  </div>
                  <span class="settings-row__value">修改</span>
                  <i data-lucide="chevron-right" class="icon muted-2"></i>
                </div>
                <div class="settings-row" :class="{ 'settings-row--action': !isStudent }" @click="!isStudent && changePhone()">
                  <div class="settings-row__main">
                    <div class="settings-row__label">绑定手机号</div>
                    <div class="settings-row__hint">{{ accountSecurity.phoneBound ? accountSecurity.maskedPhone : '未绑定' }}</div>
                  </div>
                  <span v-if="accountSecurity.phoneBound" class="settings-row__value text-success">已绑定</span>
                  <i v-if="!isStudent && !accountSecurity.phoneBound" data-lucide="chevron-right" class="icon muted-2"></i>
                </div>
                <div
                  class="settings-row"
                  :class="{ 'settings-row--action': accountSecurity.dingTalkConfigured, 'is-disabled': dingTalkBusy }"
                  @click="accountSecurity.dingTalkConfigured ? handleDingTalkTap() : null"
                >
                  <div class="settings-row__main">
                    <div class="settings-row__label">钉钉绑定</div>
                    <div class="settings-row__hint">
                      {{ accountSecurity.dingTalkConfigured ? '用于学校统一身份认证' : '正式上线后由学校开通' }}
                    </div>
                  </div>
                  <span
                    class="settings-row__value"
                    :class="accountSecurity.dingTalkBound ? 'text-success' : (accountSecurity.dingTalkConfigured ? '' : 'muted-2')"
                  >
                    {{ dingTalkBusy ? '处理中…' : (accountSecurity.dingTalkLabel || (accountSecurity.dingTalkConfigured ? '未绑定' : '待开通')) }}
                  </span>
                  <i v-if="accountSecurity.dingTalkConfigured" data-lucide="chevron-right" class="icon muted-2"></i>
                </div>
                <div v-if="isStudent" class="settings-row">
                  <div class="settings-row__main">
                    <div class="settings-row__label">家长绑定</div>
                    <div class="settings-row__hint">用于绑定监护关系</div>
                  </div>
                  <span class="settings-row__value" :class="accountSecurity.parentBound ? 'text-success' : ''">{{ accountSecurity.parentBindLabel || '未绑定' }}</span>
                </div>
              </div>
            </div>
          </section>

          <!-- ===== 修改密码（内嵌，由 account 面板进入） ===== -->
          <section class="pad-settings__section" :class="{ 'is-active': activeSection === 'password' }">
            <div class="settings-panel-head">
              <h2>修改密码</h2>
              <p>请先输入原密码，再设置新密码</p>
            </div>
            <p class="text-xs muted mb-4 pad-settings__mobile-only">请先输入原密码，再设置新密码</p>
            <div class="stack-4 settings-profile-form">
              <div>
                <label class="text-xs font-medium muted block mb-1">原密码</label>
                <div class="input-group">
                  <i data-lucide="lock" class="icon"></i>
                  <input v-model="pwdForm.oldPassword" class="input" type="password" placeholder="请输入原密码" autocomplete="current-password" />
                </div>
              </div>
              <div>
                <label class="text-xs font-medium muted block mb-1">新密码</label>
                <div class="input-group">
                  <i data-lucide="lock" class="icon"></i>
                  <input v-model="pwdForm.newPassword" class="input" type="password" placeholder="不少于 6 位" autocomplete="new-password" />
                </div>
              </div>
              <div>
                <label class="text-xs font-medium muted block mb-1">确认新密码</label>
                <div class="input-group">
                  <i data-lucide="lock" class="icon"></i>
                  <input v-model="pwdForm.confirmPassword" class="input" type="password" placeholder="再次输入新密码" autocomplete="new-password" />
                </div>
              </div>
              <button type="button" class="btn btn-danger btn-block" :disabled="pwdSaving" @click="savePassword">
                {{ pwdSaving ? '修改中…' : '修改密码' }}
              </button>
              <p v-if="pwdMsg && pwdMsgType === 'error'" class="text-sm text-danger">{{ pwdMsg }}</p>
            </div>
          </section>

          <!-- ===== 消息通知 ===== -->
          <section class="pad-settings__section" :class="{ 'is-active': activeSection === 'notifications' }">
            <div class="settings-panel-head">
              <h2>消息通知</h2>
              <p>查看最近消息与全部通知</p>
            </div>

            <div v-if="isParent" class="settings-card settings-group mb-4">
              <div class="text-sm font-bold mb-3">隐私与提醒</div>
              <div class="stack-3">
                <div class="row items-center justify-between gap-3">
                  <div class="min-w-0">
                    <div class="text-sm">允许查看作品</div>
                    <div class="text-xs muted">控制是否可查看孩子作品</div>
                  </div>
                  <button
                    type="button"
                    class="settings-toggle shrink-0"
                    :class="{ 'is-on': parentPrefs.viewWorks }"
                    :disabled="prefsSaving"
                    aria-label="允许查看作品"
                    @click="toggleParentPref('viewWorks')"
                  ></button>
                </div>
                <div class="row items-center justify-between gap-3">
                  <div class="min-w-0">
                    <div class="text-sm">接收通知提醒</div>
                    <div class="text-xs muted">孩子进度与任务动态推送</div>
                  </div>
                  <button
                    type="button"
                    class="settings-toggle shrink-0"
                    :class="{ 'is-on': parentPrefs.receiveNotify }"
                    :disabled="prefsSaving"
                    aria-label="接收通知提醒"
                    @click="toggleParentPref('receiveNotify')"
                  ></button>
                </div>
                <div class="row items-center justify-between gap-3">
                  <div class="min-w-0">
                    <div class="text-sm">允许 AI 分析学情</div>
                    <div class="text-xs muted">用于个性化学习建议</div>
                  </div>
                  <button
                    type="button"
                    class="settings-toggle shrink-0"
                    :class="{ 'is-on': parentPrefs.aiAnalysis }"
                    :disabled="prefsSaving"
                    aria-label="允许 AI 分析学情"
                    @click="toggleParentPref('aiAnalysis')"
                  ></button>
                </div>
              </div>
              <p v-if="prefsMsg" class="text-xs mt-3" :class="prefsMsgType === 'success' ? 'text-success' : 'text-danger'">{{ prefsMsg }}</p>
            </div>

            <div class="settings-card settings-group">
              <div class="row items-center justify-between mb-3">
                <span class="text-sm font-bold">最近消息</span>
                <span class="badge badge-danger">{{ notifyUnreadLabel }}</span>
              </div>
              <div v-if="!notifyPreviewList.length" class="text-xs muted text-center py-4">暂无消息</div>
              <div v-else class="stack-2">
                <router-link
                  v-for="item in notifyPreviewList"
                  :key="item.id"
                  :to="`/notifications/${item.id}`"
                  class="settings-row settings-row--action notif-preview-row"
                  :class="{ 'notif-preview-row--unread': item.unread }"
                >
                  <span class="notif-dot mt-1 shrink-0" :class="item.unread ? 'notif-dot-danger' : ''" :style="item.unread ? {} : { visibility: 'hidden' }"></span>
                  <div class="settings-row__main">
                    <div class="settings-row__label">{{ item.title }}</div>
                    <div class="settings-row__hint">{{ item.hint }}</div>
                  </div>
                  <i data-lucide="chevron-right" class="icon muted-2"></i>
                </router-link>
              </div>
              <router-link to="/notifications" class="btn btn-outline w-full mt-2">查看全部消息</router-link>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- 修改密码成功对话框 -->
    <div v-if="pwdSuccessOpen" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="pwdSuccessTitle">
      <div class="modal-content">
        <div class="text-4xl mb-3">✅</div>
        <h2 id="pwdSuccessTitle" class="text-base font-bold mb-2">密码修改成功</h2>
        <p class="text-sm muted mb-5">您的登录密码已更新，请使用新密码登录</p>
        <button type="button" class="btn btn-danger btn-block" @click="confirmPwdSuccess">确定</button>
      </div>
    </div>

    <!-- 头像选择底部弹出 -->
    <div class="sheet-overlay" :class="{ show: avatarSheetOpen }" @click="avatarSheetOpen = false"></div>
    <div class="sheet" :class="{ open: avatarSheetOpen }">
      <div class="sheet-handle"></div>
      <div class="text-center text-sm font-bold muted pb-2">更换头像</div>
      <div class="sheet-item row items-center gap-3" @click="chooseAvatar('camera')">
        <i data-lucide="camera" class="icon icon-lg"></i>
        <span class="text-sm">拍照</span>
      </div>
      <div class="sheet-item row items-center gap-3" @click="chooseAvatar('gallery')">
        <i data-lucide="image" class="icon icon-lg"></i>
        <span class="text-sm">从相册选择</span>
      </div>
      <hr class="divider mx-4" />
      <div class="sheet-item row items-center justify-center" @click="avatarSheetOpen = false">
        <span class="text-sm font-medium text-danger">取消</span>
      </div>
    </div>
    <input
      ref="avatarInputRef"
      type="file"
      accept="image/*"
      style="display:none"
      @change="onAvatarSelected"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import BottomNav from '@/components/BottomNav.vue'
import { fetchProfile, updateProfile, changePassword } from '@/api/profile'
import { uploadFile } from '@/api/work'
import { fetchBadges } from '@/api/badge'
import { fetchUnreadCount, fetchMessages } from '@/api/notification'
import { fetchParentChildren, setDefaultChild } from '@/api/parent'
import { fetchAccountSecurity, fetchDingTalkAuthorizeUrl, unbindDingTalk, fetchPreferences, savePreferences } from '@/api/settings'
import { mapMessageItem } from '@/utils/notificationDisplay'
import { getDingTalkRedirectBase } from '@/utils/dingtalk'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
appStore.setActiveTab('profile')

const profile = ref({})
const activeSection = ref('')
const isMobileSettingsLayout = ref(false)
let settingsLayoutMediaQuery = null
const avatarSheetOpen = ref(false)
const avatarInputRef = ref(null)
const avatarUploading = ref(false)
const saving = ref(false)
const profileMsg = ref('')
const profileMsgType = ref('')
const pwdSaving = ref(false)
const pwdSuccessOpen = ref(false)
const pwdMsg = ref('')
const pwdMsgType = ref('')
const prefsMsg = ref('')
const prefsMsgType = ref('')
const prefsSaving = ref(false)
const parentPrefs = reactive({ viewWorks: true, receiveNotify: true, aiAnalysis: false })
let parentPrefsSnapshot = null
const accountSecurity = ref({
  phoneBound: false,
  maskedPhone: '',
  dingTalkConfigured: false,
  dingTalkBound: false,
  dingTalkLabel: '待开通',
  wechatBound: false,
  parentBound: false,
  parentBindLabel: '未绑定',
  currentDeviceLabel: '本机',
  lastLoginLabel: '暂无记录'
})
const dingTalkBusy = ref(false)

const form = reactive({ userName: '', userNickName: '', userPhone: '', userEmail: '', perResume: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const parentRelation = ref('')

const allTabs = [
  { key: 'profile', label: '个人资料', icon: 'user' },
  { key: 'account', label: '账号与安全', icon: 'lock' },
  { key: 'notifications', label: '消息通知', icon: 'bell' }
]

const visibleTabs = computed(() => allTabs)

const displayName = computed(() => profile.value.userNickName || profile.value.userName || '用户')
const userOrgNameLabel = computed(() => profile.value.userOrgName || profile.value.rootOrgName || '')
const studentNoLabel = computed(() => profile.value.loginName || '')
const maskedPhoneLabel = computed(() => maskPhone(profile.value.userPhone))
const badgeEarned = ref(0)
const unreadCount = ref(0)
const teacherClassLabel = computed(() => profile.value.userOrgName || '')
const teacherTeachClasses = computed(() => profile.value.userOrgName || '')
const teacherWorkNo = computed(() => profile.value.loginName || '')
const prototypeChildren = ref([])
const notifyPreviewList = ref([])
const badgeCountLabel = computed(() => badgeEarned.value)
const notifyUnreadLabel = computed(() => `${unreadCount.value} 条未读`)

const roleLower = computed(() => (profile.value.userRoleId || 'student').toLowerCase())

const isStudent = computed(() => roleLower.value === 'student')
const isParent = computed(() => roleLower.value === 'parent')
const isTeacher = computed(() => roleLower.value === 'teacher')

function maskPhone(phone) {
  if (!phone || phone.length < 7) return ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const mobileUserSubline = computed(() => {
  if (isStudent.value) {
    const cls = userOrgNameLabel.value || ''
    return `${cls} · 学号 ${studentNoLabel.value}`
  }
  if (isParent.value) return `家长 · 绑定 ${(profile.value.childrenCount || 0)} 个孩子`
  if (isTeacher.value) return `科学教师 · ${teacherClassLabel.value}`
  return roleLabel.value
})
const userInitial = computed(() => displayName.value.charAt(0) || '用')
const roleLabel = computed(() => {
  if (isParent.value) return '家长'
  if (isTeacher.value) {
    const title = profile.value.prefTitleName
    return title ? `教师 · ${title}` : '教师'
  }
  return '学生'
})
const roleKey = computed(() => roleLower.value === 'parent' ? 'parent' : roleLower.value === 'teacher' ? 'teacher' : 'student')
const avatarClass = computed(() => {
  if (isParent.value) return 'avatar-grad-cool'
  if (isTeacher.value) return 'avatar-grad-ocean'
  return 'avatar-grad-warm'
})
const currentPaneTitle = computed(() => {
  if (activeSection.value === 'password') return '修改密码'
  const t = visibleTabs.find((n) => n.key === activeSection.value)
  return t ? t.label : ''
})

async function loadPreferences() {
  if (!isParent.value) return
  try {
    const prefRes = await fetchPreferences()
    if (prefRes?.code === 200 && prefRes.data) {
      const privacy = prefRes.data.privacy || {}
      const notify = prefRes.data.notify || {}
      parentPrefs.viewWorks = privacy.childWorks !== false
      parentPrefs.receiveNotify = notify.childProgress !== false
      parentPrefs.aiAnalysis = !!privacy.ai
      parentPrefsSnapshot = { ...parentPrefs }
    }
  } catch { /* ignore */ }
}

async function toggleParentPref(key) {
  if (prefsSaving.value) return
  parentPrefs[key] = !parentPrefs[key]
  prefsSaving.value = true
  prefsMsg.value = ''
  try {
    const res = await savePreferences({
      privacy: {
        childWorks: parentPrefs.viewWorks,
        ai: parentPrefs.aiAnalysis
      },
      notify: {
        childProgress: parentPrefs.receiveNotify
      }
    })
    if (res?.code === 200) {
      parentPrefsSnapshot = { ...parentPrefs }
      prefsMsg.value = '已保存'
      prefsMsgType.value = 'success'
    } else {
      if (parentPrefsSnapshot) Object.assign(parentPrefs, parentPrefsSnapshot)
      prefsMsg.value = res?.message || '保存失败'
      prefsMsgType.value = 'error'
    }
  } catch {
    if (parentPrefsSnapshot) Object.assign(parentPrefs, parentPrefsSnapshot)
    prefsMsg.value = '保存失败，请稍后重试'
    prefsMsgType.value = 'error'
  } finally {
    prefsSaving.value = false
  }
}

async function refreshAccountSecurity() {
  const accountRes = await fetchAccountSecurity().catch(() => null)
  if (accountRes?.code === 200 && accountRes.data) {
    accountSecurity.value = { ...accountSecurity.value, ...accountRes.data }
  }
}

async function handleDingTalkTap() {
  if (dingTalkBusy.value || !accountSecurity.value.dingTalkConfigured) return
  if (accountSecurity.value.dingTalkBound) {
    if (!confirm('确定解绑钉钉账号？解绑后将无法使用钉钉身份认证。')) return
    dingTalkBusy.value = true
    try {
      const res = await unbindDingTalk()
      if (res?.code === 200) {
        await refreshAccountSecurity()
        prefsMsg.value = '已解绑钉钉'
        prefsMsgType.value = 'success'
      } else {
        alert(res?.message || '解绑失败')
      }
    } catch {
      alert('解绑失败，请稍后重试')
    } finally {
      dingTalkBusy.value = false
    }
    return
  }

  dingTalkBusy.value = true
  try {
    const res = await fetchDingTalkAuthorizeUrl(getDingTalkRedirectBase())
    if (res?.code !== 200 || !res.data) {
      alert(res?.message || '无法获取授权地址')
      return
    }
    const { authorizeUrl, state, configured, message } = res.data
    if (!configured || !authorizeUrl) {
      alert(message || '钉钉绑定未配置')
      return
    }
    if (state) {
      sessionStorage.setItem('dingtalk_oauth_state', state)
    }
    window.location.href = authorizeUrl
  } catch {
    alert('发起绑定失败，请检查网络')
  } finally {
    dingTalkBusy.value = false
  }
}

async function changePhone() {
  const current = profile.value.userPhone || form.userPhone || ''
  const val = window.prompt('请输入手机号', current)
  if (!val || !val.trim()) return
  try {
    const payload = isStudent.value
      ? { userPhone: val.trim() }
      : { userPhone: val.trim(), userNickName: form.userNickName }
    const res = await updateProfile(payload)
    if (res?.code === 200) {
      await loadProfile()
      const accountRes = await fetchAccountSecurity().catch(() => null)
      if (accountRes?.code === 200 && accountRes.data) {
        accountSecurity.value = { ...accountSecurity.value, ...accountRes.data }
      }
      prefsMsg.value = '手机号已更新'
      prefsMsgType.value = 'success'
    } else {
      alert(res?.message || '更新失败')
    }
  } catch {
    alert('更新失败，请稍后重试')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
  } catch { return dateStr }
}

function syncSettingsLayoutMode() {
  isMobileSettingsLayout.value = window.matchMedia('(max-width: 767px)').matches
}

function preferMobileSettingsHome() {
  return isMobileSettingsLayout.value
}

function ensureInitialSection() {
  if (activeSection.value) return

  const panel = route.query.panel
  if (typeof panel === 'string' && visibleTabs.some((tab) => tab.key === panel)) {
    activeSection.value = panel
    return
  }

  if (!preferMobileSettingsHome()) {
    activeSection.value = visibleTabs[0]?.key || 'profile'
  }
}

function openSection(key) { activeSection.value = key; initIcons() }
function closeSection() { activeSection.value = '' }
function handleMobileBack() {
  if (activeSection.value === 'password') {
    openSection('account')
    return
  }
  if (activeSection.value) {
    closeSection()
    initIcons()
    return
  }
  goBack()
}

function openAvatarSheet() { avatarSheetOpen.value = true }

function chooseAvatar(source) {
  avatarSheetOpen.value = false
  if (!avatarInputRef.value) return
  avatarInputRef.value.accept = source === 'camera' ? 'image/*' : 'image/*'
  avatarInputRef.value.setAttribute('capture', source === 'camera' ? 'environment' : '')
  if (source !== 'camera') avatarInputRef.value.removeAttribute('capture')
  avatarInputRef.value.click()
}

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  avatarUploading.value = true
  try {
    const up = await uploadFile(file)
    const url = up?.data?.url || up?.data?.fileUrl || up?.url
    if (!url) throw new Error('上传失败')
    const res = await updateProfile({ userLogo: url })
    if (res?.code === 200) {
      await loadProfile()
    } else {
      alert(res?.message || '头像更新失败')
    }
  } catch (e) {
    console.warn('头像上传失败', e)
    alert('头像上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
  }
}

async function switchSettingsChild(child) {
  if (!child?.id || child.isDefault) return
  try {
    await setDefaultChild(child.id)
    prototypeChildren.value = prototypeChildren.value.map((c) => ({
      ...c,
      isDefault: c.id === child.id
    }))
  } catch {
    alert('切换默认孩子失败')
  }
}

function resetForm() {
  const p = profile.value
  form.userName = p.userName || ''
  form.userNickName = p.userNickName || p.userName || ''
  form.userPhone = p.userPhone || ''
  form.userEmail = p.userEmail || ''
  form.perResume = p.perResume || ''
}

async function loadExtras() {
  try {
    const accountRes = await fetchAccountSecurity().catch(() => null)
    if (accountRes?.code === 200 && accountRes.data) {
      accountSecurity.value = { ...accountSecurity.value, ...accountRes.data }
    }

    const msgTasks = [
      fetchUnreadCount(),
      fetchMessages({ pageNum: 1, pageSize: 5 })
    ]
    if (isStudent.value) {
      msgTasks.push(fetchBadges())
    }
    const results = await Promise.all(msgTasks)
    let idx = 0
    unreadCount.value = Number(results[idx++]?.data || 0)
    notifyPreviewList.value = (results[idx++]?.data?.records || []).slice(0, 3).map((row) => {
      const item = mapMessageItem(row)
      return {
        id: item.id,
        title: item.title,
        hint: item.timeLabel || item.preview || '',
        unread: item.unread
      }
    })
    if (isStudent.value) {
      badgeEarned.value = Number(results[idx]?.data?.earned || 0)
    }

    if (isParent.value) {
      const childRes = await fetchParentChildren().catch(() => null)
      const children = childRes?.data || []
      prototypeChildren.value = children.map((c) => ({
        id: c.id,
        name: c.name || '孩子',
        initial: (c.avatar || c.name || '孩').charAt(0),
        isDefault: !!c.current,
        hint: c.classLabel || ''
      }))
      await loadPreferences()
    }
  } catch { /* ignore */ }
}

async function loadProfile() {
  try {
    const res = await fetchProfile()
    if (res.code === 200 && res.data) {
      profile.value = res.data
      resetForm()
      await loadExtras()
    } else if (userStore.userInfo?.userId) {
      profile.value = {
        userRoleId: userStore.userInfo.userRoleId,
        userName: userStore.userInfo.username,
        userNickName: userStore.userInfo.username,
        rootOrgName: userStore.userInfo.rootOrgName
      }
    }
  } catch {
    if (userStore.userInfo?.userId) {
      profile.value = {
        userRoleId: userStore.userInfo.userRoleId,
        userName: userStore.userInfo.username,
        userNickName: userStore.userInfo.username,
        rootOrgName: userStore.userInfo.rootOrgName
      }
    }
  } finally {
    ensureInitialSection()
    initIcons()
  }
}

async function saveProfile() {
  saving.value = true
  profileMsg.value = ''
  try {
    const payload = { perResume: form.perResume }
    if (isStudent.value) {
      payload.userName = form.userName
    } else {
      payload.userNickName = form.userNickName
      payload.userEmail = form.userEmail
    }
    const res = await updateProfile(payload)
    if (res.code === 200) {
      profileMsg.value = '✅ 资料已保存！'
      profileMsgType.value = 'success'
      await loadProfile()
    } else {
      profileMsg.value = res.message || '保存失败'
      profileMsgType.value = 'error'
    }
  } catch {
    profileMsg.value = '保存失败，请检查网络'
    profileMsgType.value = 'error'
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  pwdMsg.value = ''
  if (!pwdForm.oldPassword) { pwdMsg.value = '请输入原密码'; pwdMsgType.value = 'error'; return }
  if (!pwdForm.newPassword) { pwdMsg.value = '请输入新密码'; pwdMsgType.value = 'error'; return }
  if (pwdForm.newPassword.length < 6) { pwdMsg.value = '新密码至少 6 位'; pwdMsgType.value = 'error'; return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { pwdMsg.value = '两次密码不一致'; pwdMsgType.value = 'error'; return }

  pwdSaving.value = true
  try {
    const res = await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    if (res.code === 200) {
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
      pwdSuccessOpen.value = true
    } else {
      pwdMsg.value = res.message || '修改失败'
      pwdMsgType.value = 'error'
    }
  } catch (e) {
    pwdMsg.value = '修改失败，请检查原密码是否正确'
    pwdMsgType.value = 'error'
  } finally {
    pwdSaving.value = false
  }
}

function confirmPwdSuccess() {
  pwdSuccessOpen.value = false
  openSection('account')
}

async function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    await userStore.logout()
    router.push('/login')
  }
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/profile')
}

function initIcons() {
  nextTick(() => {
    import('lucide').then(({ createIcons, icons }) => {
      createIcons({ icons })
    }).catch(() => {})
  })
}

onMounted(() => {
  settingsLayoutMediaQuery = window.matchMedia('(max-width: 767px)')
  syncSettingsLayoutMode()
  settingsLayoutMediaQuery.addEventListener('change', syncSettingsLayoutMode)
  loadProfile()
  initIcons()
})

onUnmounted(() => {
  settingsLayoutMediaQuery?.removeEventListener('change', syncSettingsLayoutMode)
})
</script>
