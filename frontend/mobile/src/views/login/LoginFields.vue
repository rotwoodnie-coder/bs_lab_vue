<template>
  <div class="stack-6" data-input-tools="off">
    <div class="input-group">
      <i data-lucide="user" class="icon"></i>
      <input
        v-model="ctx.account.value"
        class="input"
        type="text"
        placeholder="用户名 / 手机号"
        autocomplete="username"
      />
    </div>

    <div class="input-group" style="position:relative;">
      <i data-lucide="lock" class="icon"></i>
      <input
        v-model="ctx.password.value"
        class="input"
        :type="ctx.showPassword.value ? 'text' : 'password'"
        placeholder="密码"
        autocomplete="current-password"
        style="padding-right:44px;"
      />
      <button
        type="button"
        class="icon-btn"
        aria-label="切换密码可见"
        style="position:absolute;right:2px;top:50%;transform:translateY(-50%);z-index:1;"
        @click="ctx.showPassword.value = !ctx.showPassword.value"
      >
        <i :data-lucide="ctx.showPassword.value ? 'eye-off' : 'eye'" class="icon"></i>
      </button>
    </div>

    <p v-if="ctx.errorMsg.value" class="text-sm text-danger" style="margin-top:4px;">{{ ctx.errorMsg.value }}</p>

    <button
      type="button"
      class="btn btn-gradient btn-block btn-lg"
      :disabled="ctx.loading.value"
      @click="ctx.handleLogin()"
    >{{ ctx.loading.value ? '登录中…' : '登 录' }}</button>

    <div class="text-center text-xs muted-2 mt-6" style="line-height:1.8;">
      登录即表示同意<br>
      <a href="#" class="text-brand">《用户协议》</a>
      <a href="#" class="text-brand">《隐私政策》</a>
    </div>
    <div class="text-center text-xs muted mt-5">首次使用？请联系学校获取账号</div>
    <div class="text-center text-xs muted-2 mt-3">— 或 —</div>
    <div class="text-center text-xs mt-2">
      <router-link to="/register/parent" class="text-brand font-medium">家长账号自助注册申请 →</router-link>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
const ctx = inject('loginContext')
</script>
