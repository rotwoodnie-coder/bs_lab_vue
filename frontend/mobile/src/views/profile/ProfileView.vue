<template>
  <div
    class="prototype-container safe-top pad-shell"
    :class="themeClass"
    data-layout="profile-hub"
  >
    <BottomNav />

    <div class="pad-main pad-workbench">
      <header class="pad-workbench__topbar">
        <PageBackButton />
        <h1 class="pad-workbench__title">个人中心</h1>
        <div class="pad-workbench__topbar-actions">
          <router-link to="/notifications" class="icon-btn profile-notify-btn" aria-label="消息通知">
            <i data-lucide="bell" class="icon"></i>
            <span v-if="stats.messages > 0" class="profile-notify-badge">{{ unreadMessagesBadge }}</span>
          </router-link>
          <router-link to="/settings" class="icon-btn" aria-label="设置">
            <i data-lucide="settings" class="icon"></i>
          </router-link>
        </div>
      </header>

      <div class="pad-workbench__body">
        <div class="pad-profile__mobile-head-wrap px-4 safe-top">
          <header class="topbar page-topbar">
            <PageBackButton />
            <h1 class="topbar-title text-xl flex-1 min-w-0">个人中心</h1>
            <router-link to="/notifications" class="icon-btn profile-notify-btn" aria-label="消息通知">
              <i data-lucide="bell" class="icon"></i>
              <span v-if="stats.messages > 0" class="profile-notify-badge">{{ unreadMessagesBadge }}</span>
            </router-link>
            <router-link to="/settings" class="icon-btn" aria-label="设置">
              <i data-lucide="settings" class="icon"></i>
            </router-link>
          </header>
        </div>
        <!-- ============ 用户 Banner ============ -->
        <div class="pad-profile__banner" :class="bannerClass">
          <div class="flex items-center gap-4">
            <UserAvatar size="xl" font-size="36px" />
            <div class="flex-1">
              <h2 class="text-lg font-bold" :class="bannerTextClass">{{ displayName }}</h2>
              <!-- 学生：班级+学号 -->
              <p v-if="isStudent" class="banner-sub">{{ classLabel }} · 学号 {{ studentNoLabel }}</p>
              <!-- 家长：家长介绍 -->
              <p v-if="isParent" class="banner-sub">家长 · 绑定 {{ childCountLabel }} 个孩子</p>
              <!-- 教师：教师介绍 -->
              <p v-if="isTeacher" class="banner-sub">科学教师 · {{ teacherClassLabel }}</p>
              <!-- 标语 -->
              <p v-if="isStudent" class="banner-sub" style="margin-top:8px;">小小科学家 · {{ badgeLabel }}</p>
              <p v-if="isParent" class="banner-sub" style="margin-top:8px;">
                <i data-lucide="smartphone" class="icon" style="width:12px;height:12px;vertical-align:middle;"></i>
                {{ maskedPhoneLabel }}
              </p>
              <p v-if="isTeacher && profile.perResume" class="banner-sub" style="margin-top:8px;">{{ profile.perResume }}</p>
              <p v-if="isResearcher && !isTeacher" class="banner-sub">教研员 · {{ classLabel || '实验审核与待办' }}</p>
            </div>
          </div>
        </div>

        <!-- ============ 统计数据（平板） ============ -->
        <div class="pad-profile__stats">
          <!-- 学生 -->
          <template v-if="isStudent">
            <router-link to="/tasks?status=pending&category=experiment" class="card rounded-xl card-pad text-center anim-fade-up delay-1">
              <div class="mb-1"><i data-lucide="notebook-text" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <div class="stat-num text-brand">{{ stats.homework }}</div>
              <div class="text-xs muted mt-1">我的实验</div>
            </router-link>
            <router-link to="/tasks?status=pending&category=remix" class="card rounded-xl card-pad text-center anim-fade-up delay-2">
              <div class="mb-1"><i data-lucide="camera" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <div class="stat-num text-warning">{{ stats.remix }}</div>
              <div class="text-xs muted mt-1">拍同款</div>
            </router-link>
            <router-link to="/tasks?status=pending&category=creative" class="card rounded-xl card-pad text-center anim-fade-up delay-3">
              <div class="mb-1"><i data-lucide="lightbulb" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <div class="stat-num text-accent">{{ stats.creative }}</div>
              <div class="text-xs muted mt-1">创意实验</div>
            </router-link>
            <router-link to="/growth" class="card rounded-xl card-pad text-center anim-fade-up delay-4">
              <div class="mb-1"><i data-lucide="sparkles" class="icon icon-lg" style="color:var(--c-orange-600);"></i></div>
              <div class="stat-num text-success">{{ stats.learningPoints }}</div>
              <div class="text-xs muted mt-1">学习积分</div>
            </router-link>
          </template>
          <!-- 家长 -->
          <template v-if="isParent">
            <router-link to="/bind-child" class="card rounded-xl card-pad text-center anim-fade-up delay-1">
              <div class="mb-1"><i data-lucide="baby" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <div class="stat-num text-brand">{{ stats.children }}</div>
              <div class="text-xs muted mt-1">绑定孩子</div>
            </router-link>
            <router-link to="/tasks?status=pending" class="card rounded-xl card-pad text-center anim-fade-up delay-2">
              <div class="mb-1"><i data-lucide="book-open" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <div class="stat-num text-warning">{{ stats.pending }}</div>
              <div class="text-xs muted mt-1">待完成</div>
            </router-link>
            <router-link to="/tasks?status=done" class="card rounded-xl card-pad text-center anim-fade-up delay-3">
              <div class="mb-1"><i data-lucide="check-circle" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <div class="stat-num text-success">{{ stats.completed }}</div>
              <div class="text-xs muted mt-1">已完成</div>
            </router-link>
            <router-link to="/growth" class="card rounded-xl card-pad text-center anim-fade-up delay-4">
              <div class="mb-1"><i data-lucide="sprout" class="icon icon-lg" style="color:var(--c-rose-600);"></i></div>
              <div class="stat-num text-accent">{{ stats.works }}</div>
              <div class="text-xs muted mt-1">成长档案</div>
            </router-link>
          </template>
          <!-- 教师 -->
          <template v-if="isTeacher">
            <router-link to="/audits" class="card rounded-xl card-pad text-center anim-fade-up delay-1">
              <div class="mb-1"><i data-lucide="file-check" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <div class="stat-num text-brand">{{ stats.pendingReview }}</div>
              <div class="text-xs muted mt-1">待批阅</div>
            </router-link>
            <router-link to="/assign" class="card rounded-xl card-pad text-center anim-fade-up delay-2">
              <div class="mb-1"><i data-lucide="clipboard-list" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <div class="stat-num text-warning">{{ stats.assigned }}</div>
              <div class="text-xs muted mt-1">已布置</div>
            </router-link>
            <router-link to="/tasks" class="card rounded-xl card-pad text-center anim-fade-up delay-3">
              <div class="mb-1"><i data-lucide="check-circle" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <div class="stat-num text-success">{{ stats.submitted }}</div>
              <div class="text-xs muted mt-1">已提交</div>
            </router-link>
            <router-link :to="boardLink" class="card rounded-xl card-pad text-center anim-fade-up delay-4">
              <div class="mb-1"><i data-lucide="users" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <div class="stat-num text-accent">{{ stats.students }}</div>
              <div class="text-xs muted mt-1">学生数</div>
            </router-link>
          </template>
        </div>

        <!-- ============ 快捷入口（平板） ============ -->
        <div class="pad-profile__shortcuts">
          <!-- 学生：9 个入口 -->
          <template v-if="isStudent">
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-1" @click="navigateTo('/tasks?status=pending&category=experiment')">
              <div class="mb-1"><i data-lucide="notebook-text" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <span class="text-xs font-bold">我的实验</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-2" @click="navigateTo('/tasks?status=pending&category=remix')">
              <div class="mb-1"><i data-lucide="camera" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <span class="text-xs font-bold">拍同款</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-3" @click="navigateTo('/tasks?status=pending&category=creative')">
              <div class="mb-1"><i data-lucide="lightbulb" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <span class="text-xs font-bold">创意实验</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-4" @click="navigateTo('/works?scope=mine')">
              <div class="mb-1"><i data-lucide="palette" class="icon icon-lg" style="color:var(--c-rose-600);"></i></div>
              <span class="text-xs font-bold">作品墙</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-5" @click="navigateTo('/growth')">
              <div class="mb-1"><i data-lucide="sprout" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <span class="text-xs font-bold">成长档案</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-6" @click="navigateTo('/badges')">
              <div class="mb-1"><i data-lucide="medal" class="icon icon-lg" style="color:var(--c-orange-600);"></i></div>
              <span class="text-xs font-bold">勋章</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-1" @click="navigateTo('/quiz')">
              <div class="mb-1"><i data-lucide="circle-help" class="icon icon-lg" style="color:var(--c-cyan-600);"></i></div>
              <span class="text-xs font-bold">每日答题</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-2" @click="navigateTo('/quiz/history')">
              <div class="mb-1"><i data-lucide="clipboard-list" class="icon icon-lg" style="color:var(--c-slate-600);"></i></div>
              <span class="text-xs font-bold">答题记录</span>
            </button>
          </template>
          <!-- 家长：协助上传（底部 Tab 未覆盖的核心操作） -->
          <template v-if="isParent">
            <router-link to="/upload" class="card rounded-xl card-pad text-center anim-fade-up delay-1 parent-profile-upload-card">
              <div class="mb-1"><i data-lucide="upload" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <span class="text-sm font-bold">协助上传成果</span>
            </router-link>
          </template>
          <!-- 教师 -->
          <template v-if="isTeacher">
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-1 relative" @click="navigateTo('/assign')">
              <div class="mb-1"><i data-lucide="pen-square" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <span class="text-xs font-bold">布置实验任务</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-2" @click="navigateTo('/audits')">
              <div class="mb-1"><i data-lucide="check-circle" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <span class="text-xs font-bold">批阅</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-3" @click="navigateTo(boardLink)">
              <div class="mb-1"><i data-lucide="bar-chart-3" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <span class="text-xs font-bold">看板</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-4 relative" @click="navigateTo('/parent-binds')">
              <div class="mb-1"><i data-lucide="users" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <span class="text-xs font-bold">绑定审核</span>
              <span v-if="stats.pendingParentBinds > 0" class="badge badge-warning" style="position:absolute;top:8px;right:8px;">{{ stats.pendingParentBinds }}</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-5" @click="navigateTo('/chat')">
              <div class="mb-1"><i data-lucide="bot" class="icon icon-lg" style="color:var(--c-cyan-600);"></i></div>
              <span class="text-xs font-bold">AI助手</span>
            </button>
          </template>
          <!-- 教研员 / 校管 -->
          <template v-if="isResearcher && !isTeacher">
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-1" @click="navigateTo('/content-audits')">
              <div class="mb-1"><i data-lucide="file-check" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <span class="text-xs font-bold">实验审核</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-2" @click="navigateTo('/audits')">
              <div class="mb-1"><i data-lucide="inbox" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <span class="text-xs font-bold">待办中心</span>
            </button>
            <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-3" @click="navigateTo('/chat')">
              <div class="mb-1"><i data-lucide="bot" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <span class="text-xs font-bold">教研助手</span>
            </button>
          </template>
        </div>

        <!-- ============ 平板额外内容 ============ -->
        <div class="pad-profile__content">
          <!-- 学生：最近动态 -->
          <template v-if="isStudent">
            <div class="card rounded-xl card-pad">
              <h3 class="text-sm font-bold mb-3">最近动态</h3>
              <div class="timeline">
                <div v-for="(item, i) in timelineItems" :key="i" class="timeline-item">
                  <div class="timeline-dot filled" :class="item.dotColor"></div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-bold">{{ item.title }}</p>
                    <p v-if="item.desc" class="text-xs muted mt-1">{{ item.desc }}</p>
                    <p class="text-xs muted mt-1">{{ item.time }}</p>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <!-- 教师：教学概览 -->
          <template v-if="isTeacher">
            <div class="card rounded-xl card-pad">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-sm font-bold">班级概览</h3>
                <button type="button" class="text-xs text-brand" @click="navigateTo(boardLink)">查看看板</button>
              </div>
              <div class="grid-3 gap-3">
                <div class="surface-2 rounded-xl py-3 px-3 text-center">
                  <p class="text-xs muted">提交率</p>
                  <p class="text-xl font-bold text-success">{{ stats.submitRate }}%</p>
                </div>
                <div class="surface-2 rounded-xl py-3 px-3 text-center">
                  <p class="text-xs muted">待批阅</p>
                  <p class="text-xl font-bold text-brand">{{ stats.pendingReview }}</p>
                </div>
                <div class="surface-2 rounded-xl py-3 px-3 text-center">
                  <p class="text-xs muted">未提交</p>
                  <p class="text-xl font-bold text-warning">{{ stats.unsubmitted }}</p>
                </div>
              </div>
              <button
                type="button"
                class="btn btn-primary btn-block btn-sm mt-3"
                :disabled="reminding || !stats.latestTaskId"
                @click="handleTeacherRemind"
              >
                {{ reminding ? '发送中…' : '📢 一键提醒未提交' }}
              </button>
            </div>
          </template>
        </div>

        <!-- ============ 手机竖屏：pad-profile__mobile-only ============ -->
        <div class="pad-profile__mobile-only pb-6">

          <!-- 手机端统计 -->
          <div v-if="isStudent" class="grid-2 gap-3 px-4 mt-3">
            <router-link to="/tasks?status=pending&category=experiment" class="card rounded-xl card-pad text-center anim-fade-up delay-1">
              <div class="mb-1"><i data-lucide="notebook-text" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <div class="stat-num text-brand">{{ stats.homework }}</div>
              <div class="text-xs muted mt-1">我的实验</div>
            </router-link>
            <router-link to="/tasks?status=pending&category=remix" class="card rounded-xl card-pad text-center anim-fade-up delay-2">
              <div class="mb-1"><i data-lucide="camera" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <div class="stat-num text-warning">{{ stats.remix }}</div>
              <div class="text-xs muted mt-1">拍同款</div>
            </router-link>
            <router-link to="/tasks?status=pending&category=creative" class="card rounded-xl card-pad text-center anim-fade-up delay-3">
              <div class="mb-1"><i data-lucide="lightbulb" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <div class="stat-num text-accent">{{ stats.creative }}</div>
              <div class="text-xs muted mt-1">创意实验</div>
            </router-link>
            <router-link to="/growth" class="card rounded-xl card-pad text-center anim-fade-up delay-4">
              <div class="mb-1"><i data-lucide="sparkles" class="icon icon-lg" style="color:var(--c-orange-600);"></i></div>
              <div class="stat-num text-success">{{ stats.learningPoints }}</div>
              <div class="text-xs muted mt-1">学习积分</div>
            </router-link>
          </div>
          <div v-if="isParent" class="grid-2 gap-3 px-4 mt-3">
            <router-link to="/bind-child" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="baby" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <div class="stat-num text-brand">{{ stats.children }}</div>
              <div class="text-xs muted mt-1">绑定孩子</div>
            </router-link>
            <router-link to="/tasks?status=pending" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="book-open" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <div class="stat-num text-warning">{{ stats.pending }}</div>
              <div class="text-xs muted mt-1">待完成</div>
            </router-link>
            <router-link to="/tasks?status=done" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="check-circle" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <div class="stat-num text-success">{{ stats.completed }}</div>
              <div class="text-xs muted mt-1">已完成</div>
            </router-link>
            <router-link to="/growth" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="sprout" class="icon icon-lg" style="color:var(--c-rose-600);"></i></div>
              <div class="stat-num text-accent">{{ stats.works }}</div>
              <div class="text-xs muted mt-1">成长档案</div>
            </router-link>
          </div>
          <div v-if="isTeacher" class="grid-2 gap-3 px-4 mt-3">
            <router-link to="/audits" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="file-check" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
              <div class="stat-num text-brand">{{ stats.pendingReview }}</div>
              <div class="text-xs muted mt-1">待批阅</div>
            </router-link>
            <router-link to="/assign" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="clipboard-list" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
              <div class="stat-num text-warning">{{ stats.assigned }}</div>
              <div class="text-xs muted mt-1">已布置</div>
            </router-link>
            <router-link :to="boardLink" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="check-circle" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
              <div class="stat-num text-success">{{ stats.submitted }}</div>
              <div class="text-xs muted mt-1">已提交</div>
            </router-link>
            <router-link :to="boardLink" class="card rounded-xl card-pad text-center">
              <div class="mb-1"><i data-lucide="users" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
              <div class="stat-num text-accent">{{ stats.students }}</div>
              <div class="text-xs muted mt-1">学生数</div>
            </router-link>
          </div>

          <div v-if="isTeacher" class="px-4 mt-4">
            <div class="card rounded-xl card-pad">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-sm font-bold">班级概览</h3>
                <button type="button" class="text-xs text-brand" @click="navigateTo(boardLink)">查看看板</button>
              </div>
              <div class="grid-3 gap-3">
                <div class="surface-2 rounded-xl py-3 px-3 text-center">
                  <p class="text-xs muted">提交率</p>
                  <p class="text-xl font-bold text-success">{{ stats.submitRate }}%</p>
                </div>
                <div class="surface-2 rounded-xl py-3 px-3 text-center">
                  <p class="text-xs muted">待批阅</p>
                  <p class="text-xl font-bold text-brand">{{ stats.pendingReview }}</p>
                </div>
                <div class="surface-2 rounded-xl py-3 px-3 text-center">
                  <p class="text-xs muted">未提交</p>
                  <p class="text-xl font-bold text-warning">{{ stats.unsubmitted }}</p>
                </div>
              </div>
              <button
                type="button"
                class="btn btn-primary btn-block btn-sm mt-3"
                :disabled="reminding || !stats.latestTaskId"
                @click="handleTeacherRemind"
              >
                {{ reminding ? '发送中…' : '📢 一键提醒未提交' }}
              </button>
            </div>
          </div>

          <div v-if="isParent" class="px-4 mt-4">
            <router-link to="/upload" class="btn btn-gradient btn-block btn-lg shadow-md">
              <i data-lucide="upload" class="icon"></i> 协助上传成果
            </router-link>
          </div>

          <!-- 手机端快捷入口（家长已精简，不在此展示） -->
          <div v-if="!isParent" class="px-4 mt-5">
            <h3 class="text-sm font-bold mb-3">快捷入口</h3>
            <div class="grid-3 gap-3">
              <!-- 学生：8 个 -->
              <template v-if="isStudent">
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-1" @click="navigateTo('/tasks?status=pending&category=experiment')">
                  <div class="mb-1"><i data-lucide="notebook-text" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
                  <span class="text-xs font-bold">我的实验</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-2" @click="navigateTo('/tasks?status=pending&category=remix')">
                  <div class="mb-1"><i data-lucide="camera" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
                  <span class="text-xs font-bold">拍同款</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-3" @click="navigateTo('/tasks?status=pending&category=creative')">
                  <div class="mb-1"><i data-lucide="lightbulb" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
                  <span class="text-xs font-bold">创意实验</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-4" @click="navigateTo('/works?scope=mine')">
                  <div class="mb-1"><i data-lucide="palette" class="icon icon-lg" style="color:var(--c-rose-600);"></i></div>
                  <span class="text-xs font-bold">作品墙</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-5" @click="navigateTo('/growth')">
                  <div class="mb-1"><i data-lucide="sprout" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
                  <span class="text-xs font-bold">成长档案</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-6" @click="navigateTo('/badges')">
                  <div class="mb-1"><i data-lucide="medal" class="icon icon-lg" style="color:var(--c-orange-600);"></i></div>
                  <span class="text-xs font-bold">勋章</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-1" @click="navigateTo('/quiz')">
                  <div class="mb-1"><i data-lucide="circle-help" class="icon icon-lg" style="color:var(--c-cyan-600);"></i></div>
                  <span class="text-xs font-bold">每日答题</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center anim-fade-up delay-2" @click="navigateTo('/quiz/history')">
                  <div class="mb-1"><i data-lucide="clipboard-list" class="icon icon-lg" style="color:var(--c-slate-600);"></i></div>
                  <span class="text-xs font-bold">答题记录</span>
                </button>
              </template>
              <!-- 教师 -->
              <template v-if="isTeacher">
                <button type="button" class="card rounded-xl card-pad text-center relative" @click="navigateTo('/assign')">
                  <div class="mb-1"><i data-lucide="pen-square" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
                  <span class="text-xs font-bold">布置实验任务</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center" @click="navigateTo('/audits')">
                  <div class="mb-1"><i data-lucide="check-circle" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
                  <span class="text-xs font-bold">批阅</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center" @click="navigateTo(boardLink)">
                  <div class="mb-1"><i data-lucide="bar-chart-3" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
                  <span class="text-xs font-bold">看板</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center relative" @click="navigateTo('/parent-binds')">
                  <div class="mb-1"><i data-lucide="users" class="icon icon-lg" style="color:var(--c-amber-600);"></i></div>
                  <span class="text-xs font-bold">绑定审核</span>
                  <span v-if="stats.pendingParentBinds > 0" class="badge badge-warning" style="position:absolute;top:8px;right:8px;">{{ stats.pendingParentBinds }}</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center" @click="navigateTo('/chat')">
                  <div class="mb-1"><i data-lucide="bot" class="icon icon-lg" style="color:var(--c-cyan-600);"></i></div>
                  <span class="text-xs font-bold">AI助手</span>
                </button>
              </template>
              <!-- 教研员 / 校管 -->
              <template v-if="isResearcher && !isTeacher">
                <button type="button" class="card rounded-xl card-pad text-center" @click="navigateTo('/content-audits')">
                  <div class="mb-1"><i data-lucide="file-check" class="icon icon-lg" style="color:var(--c-blue-600);"></i></div>
                  <span class="text-xs font-bold">实验审核</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center" @click="navigateTo('/audits')">
                  <div class="mb-1"><i data-lucide="inbox" class="icon icon-lg" style="color:var(--c-emerald-600);"></i></div>
                  <span class="text-xs font-bold">待办中心</span>
                </button>
                <button type="button" class="card rounded-xl card-pad text-center" @click="navigateTo('/chat')">
                  <div class="mb-1"><i data-lucide="bot" class="icon icon-lg" style="color:var(--c-violet-600);"></i></div>
                  <span class="text-xs font-bold">教研助手</span>
                </button>
              </template>
            </div>
          </div>

          <!-- 手机端最近动态（学生） -->
          <div v-if="isStudent" class="px-4 mt-5">
            <h3 class="text-sm font-bold mb-3">最近动态</h3>
            <div class="card rounded-xl card-pad">
              <div class="timeline">
                <div v-for="(item, i) in timelineItems" :key="i" class="timeline-item">
                  <div class="timeline-dot filled" :class="item.dotColor"></div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-bold">{{ item.title }}</p>
                    <p v-if="item.desc" class="text-xs muted mt-1">{{ item.desc }}</p>
                    <p class="text-xs muted mt-1">{{ item.time }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作：消息 + 系统设置 + 退出登录 -->
        <div class="px-4 pb-6 stack-2 profile-actions">
          <router-link to="/notifications" class="card rounded-xl card-pad profile-message-entry">
            <div class="row items-center gap-3">
              <span class="profile-message-entry__icon">
                <i data-lucide="bell" class="icon"></i>
              </span>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-bold">消息通知</div>
                <div class="text-xs muted mt-0.5">作业提醒、批阅反馈与系统公告</div>
              </div>
              <span v-if="stats.messages > 0" class="badge badge-danger">{{ unreadMessagesBadge }}</span>
              <i data-lucide="chevron-right" class="icon muted shrink-0"></i>
            </div>
          </router-link>
          <router-link to="/settings" class="btn btn-outline btn-block">
            <i data-lucide="settings" class="icon"></i> 系统设置
          </router-link>
          <button type="button" class="btn btn-danger btn-block" @click="handleLogout">
            <i data-lucide="log-out" class="icon"></i> 退出登录
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useProfileStore } from '@/stores/profile'
import { useAppStore } from '@/stores/app'
import BottomNav from '@/components/BottomNav.vue'
import PageBackButton from '@/components/PageBackButton.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { fetchParentDashboard } from '@/api/home'
import { fetchTeacherDashboard, remindTeacherTask } from '@/api/teacher'
import { showToast } from '@/utils/toast'
import { fetchUnreadCount } from '@/api/notification'
import { fetchTasks } from '@/api/task'
import { fetchBadges } from '@/api/badge'
import { fetchGrowth } from '@/api/growth'
import { useParentContext } from '@/composables/useParentContext'
import { useLucideIcons } from '@/composables/useLucideIcons'

const router = useRouter()
const userStore = useUserStore()
const profileStore = useProfileStore()
const { profile } = storeToRefs(profileStore)
const appStore = useAppStore()
appStore.setActiveTab('profile')

const stats = ref({
  homework: 0, remix: 0, creative: 0, badgeEarned: 0, learningPoints: 0,
  children: 0, pending: 0, completed: 0, works: 0, messages: 0,
  pendingReview: 0, assigned: 0, submitted: 0, students: 0,
  totalExperiments: 0, submitRate: 0, unsubmitted: 0,
  pendingParentBinds: 0, latestTaskId: ''
})
const reminding = ref(false)
const timelineItems = ref([])

const roleLower = computed(() => (profile.value.userRoleId || userStore.userInfo.userRoleId || 'student').toLowerCase())
const isStudent = computed(() => roleLower.value === 'student')
const isParent = computed(() => roleLower.value === 'parent')
const isTeacher = computed(() => roleLower.value === 'teacher')
const isResearcher = computed(() => {
  const r = roleLower.value
  return r === 'researcher' || r === 'sys_admin' || r === 'school_admin'
})

const displayName = computed(() => profileStore.displayName || '同学')
const classLabel = computed(() => profile.value.userOrgName || profile.value.rootOrgName || '')
const gradeLabel = computed(() => classLabel.value)
const studentNoLabel = computed(() => profile.value.loginName || '')
const maskedPhoneLabel = computed(() => maskPhone(profile.value.userPhone))
const childCountLabel = computed(() => stats.value.children || 0)
const unreadMessagesBadge = computed(() => {
  const count = stats.value.messages || 0
  return count > 99 ? '99+' : String(count)
})
const teacherClassLabel = computed(() => profile.value.userOrgName || '')
const boardLink = computed(() => '/tasks')
const { children: parentChildren, selectedChild: parentSelected, loadChildren } = useParentContext()

const badgeLabel = computed(() => {
  const count = stats.value.badgeEarned || 0
  const points = stats.value.learningPoints || 0
  return `已获 ${count} 枚勋章 · ${points} 积分`
})

function maskPhone(phone) {
  if (!phone || phone.length < 7) return ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const themeClass = computed(() => {
  if (isParent.value) return 'theme-parent'
  if (isTeacher.value) return 'theme-teacher'
  return 'theme-primary'
})
const bannerClass = computed(() => isParent.value ? 'banner banner-sky-indigo' : 'banner banner-amber-rose')
const bannerTextClass = computed(() => isParent.value ? 'text-on-brand' : '')

function navigateTo(path) {
  router.push(path)
}

async function handleTeacherRemind() {
  const taskId = stats.value.latestTaskId
  if (!taskId || reminding.value) return
  reminding.value = true
  try {
    const res = await remindTeacherTask(taskId)
    if (res?.code === 200) {
      const count = res.data?.notifiedCount ?? stats.value.unsubmitted
      showToast(res.data?.message || `已向 ${count} 名未提交学生发送提醒`)
    } else {
      showToast(res?.message || '提醒发送失败', 'danger')
    }
  } catch {
    showToast('提醒发送失败，请稍后重试', 'danger')
  } finally {
    reminding.value = false
  }
}

const { initIcons } = useLucideIcons()

async function handleLogout() {
  if (confirm('确定退出登录？')) {
    await userStore.logout()
    router.push('/login')
  }
}

onMounted(async () => {
  try {
    await profileStore.loadProfile(true)
  } catch { /* ignore */ }

  try {
    const [homeworkRes, remixRes, creativeRes, badgeRes, growthRes, msgRes] = await Promise.all([
      fetchTasks({ category: 'experiment', page: 1, size: 1 }),
      fetchTasks({ category: 'remix', page: 1, size: 1 }),
      fetchTasks({ category: 'creative', page: 1, size: 1 }),
      fetchBadges(),
      fetchGrowth(),
      fetchUnreadCount()
    ])
    const s = { ...stats.value, messages: Number(msgRes?.data?.count ?? msgRes?.data ?? 0) }

    if (isStudent.value) {
      s.homework = Number(homeworkRes?.data?.total || 0)
      s.remix = Number(remixRes?.data?.total || 0)
      s.creative = Number(creativeRes?.data?.total || 0)
      s.badgeEarned = Number(badgeRes?.data?.earned || 0)
      s.learningPoints = Number(
        profile.value.perScore ?? growthRes?.data?.stats?.points ?? 0
      )
      const timeline = growthRes?.data?.timeline
      timelineItems.value = Array.isArray(timeline) && timeline.length
        ? timeline.slice(0, 3).map(a => ({
            title: a.title || '—',
            desc: a.hint || '',
            time: a.time || '',
            dotColor: a.dot?.includes('green') ? 'green' : 'amber'
          }))
        : []
    } else if (isParent.value) {
      await loadChildren(true)
      const dashRes = await fetchParentDashboard().catch(() => null)
      const dash = dashRes?.data || {}
      s.children = Number(dash.children?.length || parentChildren.value.length || 0)
      const active = parentSelected.value
      s.pending = Number(active?.pending ?? dash.todayProgress?.pending ?? 0)
      s.completed = Number(active?.completed ?? dash.todayProgress?.completed ?? 0)
      s.works = Number(active?.works ?? 0)
    } else {
      const dashRes = await fetchTeacherDashboard().catch(() => null)
      const dash = dashRes?.data || {}
      s.pendingReview = Number(dash.pendingReview || 0)
      s.assigned = Number(dash.assigned || 0)
      s.submitted = Number(dash.submitted || 0)
      s.students = Number(dash.students || 0)
      s.submitRate = Number(dash.submitRate || 0)
      s.totalExperiments = Number(dash.assigned || 0)
      s.unsubmitted = Number(dash.unsubmitted || 0)
      s.pendingParentBinds = Number(dash.pendingParentBinds || 0)
      s.latestTaskId = dash.latestTaskId || ''
    }
    stats.value = s
  } catch {
    stats.value = { homework: 0, remix: 0, creative: 0, badgeEarned: 0, learningPoints: 0, children: 0, pending: 0, completed: 0, works: 0, messages: 0, pendingReview: 0, assigned: 0, submitted: 0, students: 0, totalExperiments: 0, submitRate: 0, unsubmitted: 0, pendingParentBinds: 0, latestTaskId: '' }
  }

  initIcons()
})
</script>

<style scoped>
.profile-notify-btn {
  position: relative;
}

.profile-notify-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: var(--radius-full);
  background: var(--c-rose-600);
  color: #fff;
  font-size: 10px;
  font-weight: var(--weight-bold);
  line-height: 16px;
  text-align: center;
}

.profile-message-entry {
  display: block;
  text-decoration: none;
  color: inherit;
}

.profile-message-entry__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  background: var(--surface-2);
  color: var(--c-blue-600);
}

:deep(.pad-profile__shortcuts) .parent-profile-upload-card {
  grid-column: 1 / -1;
  text-decoration: none;
  color: inherit;
}
</style>
