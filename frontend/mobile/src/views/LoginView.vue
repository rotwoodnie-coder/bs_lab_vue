<template>

  <div class="prototype-container pad-shell" data-layout="login-split">

    <div class="pad-main pad-workbench">

      <div class="pad-login">

        <div class="pad-login__brand">

          <div class="tile-icon tile-lg tile-solid mx-auto">

            <i data-lucide="flask-conical" class="icon icon-xl"></i>

          </div>

          <h1 class="pad-login__product-name">宝山区小实验社区</h1>

          <p class="text-sm mt-2" style="opacity:0.85">探索科学 · 点亮好奇心</p>

          <p class="text-xs mt-6" style="opacity:0.6">面向 K12 的科学实验与探究学习平台</p>

        </div>



        <div class="pad-login__form">

          <div class="pad-login__pad-only">

            <h2 class="text-xl font-bold mb-6">欢迎登录</h2>

            <LoginFields />

          </div>



          <div class="pad-login__mobile-only">

            <div class="screen-body middle stack-4 anim-fade-up px-5">

              <div class="text-center">

                <div class="tile-icon tile-lg tile-solid mx-auto">

                  <i data-lucide="flask-conical" class="icon icon-xl"></i>

                </div>

                <h1 class="pad-login__product-name">宝山区小实验社区</h1>

                <p class="muted text-sm mt-1">探索科学 · 点亮好奇心</p>

              </div>

              <div style="height: var(--space-8)"></div>

              <LoginFields />

            </div>

          </div>

        </div>

      </div>

    </div>

  </div>

</template>



<script setup>

import { ref, onMounted, provide } from 'vue'

import { useRouter, useRoute } from 'vue-router'

import { useUserStore } from '@/stores/user'
import { useProfileStore } from '@/stores/profile'

import { login as loginApi } from '@/api/auth'
import { getUserInfo } from '@/utils/authStorage'
import { shouldShowParentApprovalCelebration } from '@/utils/parentAccess'
import { isParentRole } from '@/utils/role'

import LoginFields from './login/LoginFields.vue'
import { initLucideIcons } from '@/utils/lucideIcons'



const router = useRouter()

const route = useRoute()

const userStore = useUserStore()
const profileStore = useProfileStore()



const account = ref('')

const password = ref('')

const showPassword = ref(false)

const loading = ref(false)

const errorMsg = ref('')



provide('loginContext', {

  account,

  password,

  showPassword,

  loading,

  errorMsg,

  handleLogin

})



async function handleLogin() {

  if (!account.value || !password.value) {

    errorMsg.value = '请输入账号和密码'

    return

  }

  loading.value = true

  errorMsg.value = ''

  try {

    const res = await loginApi({

      loginName: account.value,

      loginPwd: password.value

    })

    if (res.code !== 200) {

      throw { response: { data: { message: res.message || '登录失败' } } }

    }

    userStore.applyTokenPayload(res.data)
    profileStore.loadProfile(true)

    const info = getUserInfo()
    if (info?.parentRestricted) {
      router.push({ path: '/bind-success', query: { from: 'bind' } })
      return
    }
    if (shouldShowParentApprovalCelebration(info)) {
      router.push({ path: '/bind-success', query: { from: 'approved' } })
      return
    }
    router.push(isParentRole(info?.userRoleId) ? '/tasks' : '/home')

  } catch (err) {
    const status = err.response?.status
    const body = err.response?.data
    const apiMessage = typeof body === 'object' ? (body.message || body.error) : ''

    if (!err.response) {
      errorMsg.value = '无法连接服务器。开发模式请确认本机后端已启动（8010）；局域网访问推荐 http://本机IP:8010/m/#/login'
    } else if (status === 404) {
      errorMsg.value = '登录接口不存在，请更新并重启后端（需包含 /api/mobile/auth/login）'
    } else if (apiMessage) {
      errorMsg.value = apiMessage
    } else {
      errorMsg.value = '登录失败，请检查账号密码'
    }
  } finally {

    loading.value = false

  }

}



onMounted(() => {

  const phone = route.query.phone
  if (phone && typeof phone === 'string') {
    account.value = phone.trim()
  }

  if (typeof window !== 'undefined') {
    initLucideIcons()
  }

})

</script>

<style>

body:has(.prototype-container[data-layout="login-split"]) {

  background: linear-gradient(180deg, #eef2ff 0%, #ffffff 100%);

  min-height: 100dvh;

}

</style>

