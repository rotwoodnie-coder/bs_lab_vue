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
              <UserAvatar size="lg" font-size="24px" />
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
              <div
                class="relative settings-avatar-trigger"
                :class="{ 'settings-avatar-trigger--busy': avatarUploading }"
                @click="openAvatarSheet"
              >
                <UserAvatar size="xl" extra-class="settings-avatar" font-size="36px">
                  <template #overlay>
                    <span v-if="avatarUploading" class="settings-avatar__mask">上传中…</span>
                  </template>
                </UserAvatar>
                <div class="absolute row items-center justify-center settings-avatar__camera" aria-hidden="true">
                  <i data-lucide="camera" class="icon icon-sm muted"></i>
                </div>
              </div>
              <div class="text-xs muted mt-2" @click="openAvatarSheet" style="cursor:pointer;">
                {{ avatarUploading ? '正在上传头像…' : '点击更换头像' }}
              </div>
              <p v-if="profileMsg && activeSection === 'profile'" class="text-xs mt-2" :class="profileMsgType === 'success' ? 'text-success' : 'text-danger'">
                {{ profileMsg }}
              </p>

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
                    <UserAvatar
                      size="sm"
                      :name="child.name"
                      :src="child.avatarUrl"
                      :grad-class="child.isDefault ? 'avatar-grad-warm' : 'avatar-grad-cool'"
                      role="student"
                    />
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
                <div class="settings-row settings-row--action" @click="openAccountSubSection('password')">
                  <div class="settings-row__main">
                    <div class="settings-row__label">登录密码</div>
                    <div class="settings-row__hint">建议定期更换，不少于 8 位</div>
                  </div>
                  <span class="settings-row__value">修改</span>
                  <i data-lucide="chevron-right" class="icon muted-2"></i>
                </div>
                <div class="settings-row settings-row--action" @click="openAccountSubSection('phone')">
                  <div class="settings-row__main">
                    <div class="settings-row__label">绑定手机号</div>
                    <div class="settings-row__hint">{{ accountSecurity.phoneBound ? accountSecurity.maskedPhone : '未绑定' }}</div>
                  </div>
                  <span v-if="accountSecurity.phoneBound" class="settings-row__value text-success">已绑定</span>
                  <span v-else-if="!isStudent" class="settings-row__value">去绑定</span>
                  <i data-lucide="chevron-right" class="icon muted-2"></i>
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
                <div
                  v-if="!accountSecurity.wechatSupported"
                  class="settings-row is-disabled"
                >
                  <div class="settings-row__main">
                    <div class="settings-row__label">微信绑定</div>
                    <div class="settings-row__hint">用于快捷登录与消息提醒</div>
                  </div>
                  <span class="settings-row__value muted-2">即将上线</span>
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

          <!-- ===== 修改密码 ===== -->
          <SettingsEditSection
            :active="activeSection === 'password'"
            title="修改密码"
            description="请先输入原密码，再设置新密码"
          >
            <div v-if="pwdSuccess" class="card card-pad text-center tint-green stack-2">
              <div class="text-3xl">✅</div>
              <p class="text-sm font-bold">密码修改成功</p>
              <p class="text-xs muted">请使用新密码登录</p>
              <button type="button" class="btn btn-outline btn-block mt-2" @click="finishPasswordEdit">
                返回账号与安全
              </button>
            </div>
            <template v-else>
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
                  <input v-model="pwdForm.newPassword" class="input" type="password" placeholder="不少于 8 位，含字母和数字" autocomplete="new-password" />
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
              <p v-if="pwdMsg" class="text-sm" :class="pwdMsgType === 'error' ? 'text-danger' : 'text-success'">{{ pwdMsg }}</p>
            </template>
          </SettingsEditSection>

          <!-- ===== 绑定 / 换绑手机号 ===== -->
          <SettingsEditSection
            :active="activeSection === 'phone'"
            :title="phonePanelTitle"
            :description="phonePanelDescription"
          >
            <div v-if="phoneSuccess" class="card card-pad text-center tint-green stack-2">
              <div class="text-3xl">✅</div>
              <p class="text-sm font-bold">手机号已更新</p>
              <p class="text-xs muted">{{ accountSecurity.maskedPhone || maskPhone(phoneForm.userPhone) }}</p>
              <button type="button" class="btn btn-outline btn-block mt-2" @click="finishPhoneEdit">
                返回账号与安全
              </button>
            </div>
            <template v-else-if="isStudent">
              <div class="settings-card card-pad stack-2">
                <div class="text-xs muted">当前绑定手机号</div>
                <div class="text-base font-bold">{{ accountSecurity.maskedPhone || maskedPhoneLabel || '未绑定' }}</div>
                <p class="text-xs muted">学生账号手机号由学校统一管理，如需变更请联系班主任。</p>
              </div>
              <button type="button" class="btn btn-outline btn-block" @click="openSection('account')">
                返回账号与安全
              </button>
            </template>
            <template v-else>
              <div v-if="accountSecurity.phoneBound" class="settings-card card-pad stack-2 mb-1">
                <div class="text-xs muted">当前绑定</div>
                <div class="text-base font-bold">{{ accountSecurity.maskedPhone || maskedPhoneLabel }}</div>
              </div>
              <div>
                <label class="text-xs font-medium muted block mb-1">{{ accountSecurity.phoneBound ? '新手机号' : '手机号' }}</label>
                <div class="input-group">
                  <i data-lucide="phone" class="icon"></i>
                  <input
                    v-model="phoneForm.userPhone"
                    class="input"
                    type="tel"
                    inputmode="numeric"
                    maxlength="11"
                    placeholder="请输入 11 位手机号"
                    autocomplete="tel"
                  />
                </div>
              </div>
              <button type="button" class="btn btn-gradient btn-block" :disabled="phoneSaving" @click="savePhone">
                {{ phoneSaving ? '保存中…' : (accountSecurity.phoneBound ? '确认换绑' : '确认绑定') }}
              </button>
              <p v-if="phoneMsg" class="text-sm" :class="phoneMsgType === 'error' ? 'text-danger' : 'text-success'">{{ phoneMsg }}</p>
            </template>
          </SettingsEditSection>

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

          <!-- ===== 关于与帮助 ===== -->
          <section class="pad-settings__section" :class="{ 'is-active': activeSection === 'about' }">
            <header class="pad-settings__section-head">
              <h2>关于与帮助</h2>
            </header>
            <div class="settings-card settings-group">
              <router-link to="/legal/terms" class="settings-row settings-row--action">
                <div class="settings-row__main">
                  <div class="settings-row__label">用户协议</div>
                  <div class="settings-row__hint">查看服务条款</div>
                </div>
                <i data-lucide="chevron-right" class="icon muted-2"></i>
              </router-link>
              <router-link to="/legal/privacy" class="settings-row settings-row--action">
                <div class="settings-row__main">
                  <div class="settings-row__label">隐私政策</div>
                  <div class="settings-row__hint">了解我们如何保护你的信息</div>
                </div>
                <i data-lucide="chevron-right" class="icon muted-2"></i>
              </router-link>
              <button type="button" class="settings-row settings-row--action w-full" @click="showHelp">
                <div class="settings-row__main">
                  <div class="settings-row__label">帮助与反馈</div>
                  <div class="settings-row__hint">使用问题与意见反馈</div>
                </div>
                <i data-lucide="chevron-right" class="icon muted-2"></i>
              </button>
              <div class="settings-row">
                <div class="settings-row__main">
                  <div class="settings-row__label">关于应用</div>
                  <div class="settings-row__hint">实验探究平台</div>
                </div>
                <span class="text-xs muted-2">v{{ appVersion }}</span>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- 头像选择底部弹出 -->
    <div class="sheet-overlay" :class="{ show: avatarSheetOpen }" @click="avatarSheetOpen = false"></div>
    <div class="sheet" :class="{ open: avatarSheetOpen }">
      <div class="sheet-handle"></div>
      <div class="text-center text-sm font-bold muted pb-2">更换头像</div>
      <div class="sheet-item row items-center gap-3" @click.stop="chooseAvatar('camera')">
        <i data-lucide="camera" class="icon icon-lg"></i>
        <span class="text-sm">拍照</span>
      </div>
      <div class="sheet-item row items-center gap-3" @click.stop="chooseAvatar('gallery')">
        <i data-lucide="image" class="icon icon-lg"></i>
        <span class="text-sm">从相册选择</span>
      </div>
      <hr class="divider mx-4" />
      <div class="sheet-item row items-center justify-center" @click="avatarSheetOpen = false">
        <span class="text-sm font-medium text-danger">取消</span>
      </div>
    </div>
    <Teleport to="body">
      <input
        ref="galleryInputRef"
        type="file"
        accept="image/*"
        class="visually-hidden-file-input"
        @change="onAvatarSelected"
      />
      <input
        ref="cameraInputRef"
        type="file"
        accept="image/*"
        capture="environment"
        class="visually-hidden-file-input"
        @change="onAvatarSelected"
      />
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated, onUnmounted, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useProfileStore } from '@/stores/profile'
import { useAppStore } from '@/stores/app'
import BottomNav from '@/components/BottomNav.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import SettingsEditSection from '@/components/settings/SettingsEditSection.vue'
import { changePassword } from '@/api/profile'
import { validatePassword } from '@/utils/passwordRules'
import { validatePhone, maskPhone as maskPhoneLabel } from '@/utils/phoneRules'
import { fetchBadges } from '@/api/badge'
import { fetchUnreadCount, fetchMessages } from '@/api/notification'
import { fetchParentChildren, setDefaultChild } from '@/api/parent'
import { fetchAccountSecurity, fetchDingTalkAuthorizeUrl, unbindDingTalk, fetchPreferences, savePreferences } from '@/api/settings'
import { mapMessageItem } from '@/utils/notificationDisplay'
import { getDingTalkRedirectBase } from '@/utils/dingtalk'
import { useLucideIcons } from '@/composables/useLucideIcons'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const profileStore = useProfileStore()
const { profile, avatarUploading } = storeToRefs(profileStore)
const appStore = useAppStore()
appStore.setActiveTab('profile')

const activeSection = ref('')
const isMobileSettingsLayout = ref(false)
let settingsLayoutMediaQuery = null
const avatarSheetOpen = ref(false)
const galleryInputRef = ref(null)
const cameraInputRef = ref(null)
const saving = ref(false)
const profileMsg = ref('')
const profileMsgType = ref('')
const pwdSaving = ref(false)
const pwdSuccess = ref(false)
const pwdMsg = ref('')
const pwdMsgType = ref('')
const phoneSaving = ref(false)
const phoneSuccess = ref(false)
const phoneMsg = ref('')
const phoneMsgType = ref('')
const phoneForm = reactive({ userPhone: '' })
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
  wechatSupported: false,
  parentBound: false,
  parentBindLabel: '未绑定',
  currentDeviceLabel: '本机',
  lastLoginLabel: '暂无记录'
})
const dingTalkBusy = ref(false)

const form = reactive({ userName: '', userNickName: '', userPhone: '', userEmail: '', perResume: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const ACCOUNT_SUB_SECTIONS = ['password', 'phone']

const parentRelation = ref('')

const allTabs = [
  { key: 'profile', label: '个人资料', icon: 'user' },
  { key: 'account', label: '账号与安全', icon: 'lock' },
  { key: 'notifications', label: '消息通知', icon: 'bell' },
  { key: 'about', label: '关于与帮助', icon: 'info' }
]

const appVersion = '1.0.0'

const visibleTabs = computed(() => allTabs)

const displayName = computed(() => profileStore.displayName)
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

const roleLower = computed(() => profileStore.roleLower)

const isStudent = computed(() => profileStore.isStudent)
const isParent = computed(() => profileStore.isParent)
const isTeacher = computed(() => profileStore.isTeacher)

function maskPhone(phone) {
  return maskPhoneLabel(phone)
}

const phonePanelTitle = computed(() => {
  if (isStudent.value) return '绑定手机号'
  return accountSecurity.value.phoneBound ? '换绑手机号' : '绑定手机号'
})

const phonePanelDescription = computed(() => {
  if (isStudent.value) return '查看当前绑定的手机号'
  return accountSecurity.value.phoneBound
    ? '输入新手机号完成换绑'
    : '绑定手机号可用于找回账号与接收通知'
})

const mobileUserSubline = computed(() => {
  if (isStudent.value) {
    const cls = userOrgNameLabel.value || ''
    return `${cls} · 学号 ${studentNoLabel.value}`
  }
  if (isParent.value) return `家长 · 绑定 ${(profile.value.childrenCount || 0)} 个孩子`
  if (isTeacher.value) return `科学教师 · ${teacherClassLabel.value}`
  return roleLabel.value
})

const roleLabel = computed(() => {
  if (isParent.value) return '家长'
  if (isTeacher.value) {
    const title = profile.value.prefTitleName
    return title ? `教师 · ${title}` : '教师'
  }
  return '学生'
})
const roleKey = computed(() => roleLower.value === 'parent' ? 'parent' : roleLower.value === 'teacher' ? 'teacher' : 'student')
const currentPaneTitle = computed(() => {
  if (activeSection.value === 'password') return '修改密码'
  if (activeSection.value === 'phone') return phonePanelTitle.value
  const t = visibleTabs.value.find((n) => n.key === activeSection.value)
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
      parentPrefs.receiveNotify = notify.parentAssist !== false && notify.childProgress !== false
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
        parentAssist: parentPrefs.receiveNotify,
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

async function savePhone() {
  phoneMsg.value = ''
  const nextPhone = phoneForm.userPhone.trim()
  const phoneError = validatePhone(nextPhone)
  if (phoneError) {
    phoneMsg.value = phoneError
    phoneMsgType.value = 'error'
    return
  }

  phoneSaving.value = true
  try {
    const payload = {
      userPhone: nextPhone,
      userNickName: form.userNickName || profile.value.userNickName || profile.value.userName
    }
    const res = await profileStore.patchProfile(payload)
    if (res?.code === 200) {
      phoneSuccess.value = true
      phoneMsg.value = ''
      await refreshAccountSecurity()
      resetForm()
    } else {
      phoneMsg.value = res?.message || '更新失败'
      phoneMsgType.value = 'error'
    }
  } catch {
    phoneMsg.value = '更新失败，请稍后重试'
    phoneMsgType.value = 'error'
  } finally {
    phoneSaving.value = false
  }
}

function resetPhoneEdit() {
  phoneForm.userPhone = profile.value.userPhone || form.userPhone || ''
  phoneMsg.value = ''
  phoneMsgType.value = ''
  phoneSuccess.value = false
}

function resetPasswordEdit() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdMsg.value = ''
  pwdMsgType.value = ''
  pwdSuccess.value = false
}

function openAccountSubSection(key) {
  if (key === 'phone') resetPhoneEdit()
  if (key === 'password') resetPasswordEdit()
  openSection(key)
}

function finishPasswordEdit() {
  resetPasswordEdit()
  openSection('account')
}

function finishPhoneEdit() {
  resetPhoneEdit()
  openSection('account')
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
  if (panel === 'password' || panel === 'phone') {
    activeSection.value = panel
    if (panel === 'phone') resetPhoneEdit()
    if (panel === 'password') resetPasswordEdit()
    return
  }
  if (typeof panel === 'string' && visibleTabs.value.some((tab) => tab.key === panel)) {
    activeSection.value = panel
    return
  }

  if (!preferMobileSettingsHome()) {
    activeSection.value = visibleTabs.value[0]?.key || 'profile'
  }
}

function openSection(key) {
  activeSection.value = key
  initIcons()
  nextTick(() => {
    const panel = document.querySelector('.pad-settings__panel')
    panel?.scrollTo?.({ top: 0, behavior: 'auto' })
  })
}
function closeSection() { activeSection.value = '' }
function handleMobileBack() {
  if (ACCOUNT_SUB_SECTIONS.includes(activeSection.value)) {
    if (activeSection.value === 'password' && pwdSuccess.value) {
      finishPasswordEdit()
      return
    }
    if (activeSection.value === 'phone' && phoneSuccess.value) {
      finishPhoneEdit()
      return
    }
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

function openAvatarSheet() {
  if (avatarUploading.value) return
  avatarSheetOpen.value = true
  initIcons()
}

function chooseAvatar(source) {
  const input = source === 'camera' ? cameraInputRef.value : galleryInputRef.value
  if (!input || avatarUploading.value) return
  // 须在用户手势内同步触发，避免 iOS/微信 WebView 拦截
  input.value = ''
  input.click()
  avatarSheetOpen.value = false
}

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || avatarUploading.value) return

  avatarSheetOpen.value = false
  profileMsg.value = ''

  try {
    await profileStore.uploadAvatar(file)
    profileMsg.value = '头像已更新'
    profileMsgType.value = 'success'
    resetForm()
  } catch (e) {
    console.warn('头像上传失败', e)
    profileMsg.value = e?.message || '头像上传失败，请稍后重试'
    profileMsgType.value = 'error'
    alert(profileMsg.value)
  } finally {
    initIcons()
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
        avatarUrl: c.avatarUrl || '',
        isDefault: !!c.current,
        hint: c.classLabel || ''
      }))
      await loadPreferences()
    }
  } catch { /* ignore */ }
}

async function loadProfile() {
  try {
    await profileStore.loadProfile(true)
    resetForm()
    await loadExtras()
  } catch {
    // profileStore keeps last known profile when fetch fails
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
    const res = await profileStore.patchProfile(payload)
    if (res.code === 200) {
      profileMsg.value = '✅ 资料已保存！'
      profileMsgType.value = 'success'
      resetForm()
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
  const passwordError = validatePassword(pwdForm.newPassword)
  if (passwordError) { pwdMsg.value = passwordError; pwdMsgType.value = 'error'; return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { pwdMsg.value = '两次密码不一致'; pwdMsgType.value = 'error'; return }

  pwdSaving.value = true
  try {
    const res = await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    if (res.code === 200) {
      pwdSuccess.value = true
      pwdMsg.value = ''
    } else {
      pwdMsg.value = res.message || '修改失败'
      pwdMsgType.value = 'error'
    }
  } catch {
    pwdMsg.value = '修改失败，请检查原密码是否正确'
    pwdMsgType.value = 'error'
  } finally {
    pwdSaving.value = false
  }
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

function showHelp() {
  alert('如需帮助或反馈，请联系所在学校管理员，或发送邮件至 support@xuanyue.com')
}

const { initIcons } = useLucideIcons()

onMounted(() => {
  settingsLayoutMediaQuery = window.matchMedia('(max-width: 767px)')
  syncSettingsLayoutMode()
  settingsLayoutMediaQuery.addEventListener('change', syncSettingsLayoutMode)
  loadProfile()
  initIcons()
})

onActivated(() => {
  loadProfile()
})

onUnmounted(() => {
  settingsLayoutMediaQuery?.removeEventListener('change', syncSettingsLayoutMode)
})
</script>
