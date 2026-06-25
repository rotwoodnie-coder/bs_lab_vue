<template>
  <div class="dingtalk-callback pad-page">
    <div class="dingtalk-callback__card">
      <div v-if="status === 'loading'" class="dingtalk-callback__icon">
        <i data-lucide="loader-circle" class="icon spin"></i>
      </div>
      <div v-else-if="status === 'success'" class="dingtalk-callback__icon text-success">
        <i data-lucide="check-circle-2" class="icon"></i>
      </div>
      <div v-else class="dingtalk-callback__icon text-danger">
        <i data-lucide="x-circle" class="icon"></i>
      </div>

      <h1>{{ title }}</h1>
      <p class="muted">{{ message }}</p>

      <button
        v-if="status !== 'loading'"
        type="button"
        class="btn btn-primary btn-block"
        @click="goSettings"
      >
        返回设置
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { bindDingTalk } from '@/api/settings'

const route = useRoute()
const router = useRouter()

const status = ref('loading')
const title = ref('正在绑定钉钉…')
const message = ref('请稍候')

function goSettings() {
  router.replace({ path: '/settings', query: { panel: 'account' } })
}

async function refreshIcons() {
  await nextTick()
  if (window.lucide?.createIcons) {
    window.lucide.createIcons()
  }
}

onMounted(async () => {
  const code = route.query.code
  const state = route.query.state
  const authError = route.query.error || route.query.error_description

  if (authError) {
    status.value = 'error'
    title.value = '钉钉授权失败'
    message.value = String(authError)
    await refreshIcons()
    return
  }

  if (!code) {
    status.value = 'error'
    title.value = '缺少授权码'
    message.value = '请从设置页重新发起绑定'
    await refreshIcons()
    return
  }

  try {
    const res = await bindDingTalk({ code: String(code), state: state ? String(state) : '' })
    if (res?.code === 200) {
      status.value = 'success'
      title.value = '钉钉绑定成功'
      message.value = res.data?.label || '已绑定钉钉账号'
      setTimeout(goSettings, 1800)
    } else {
      status.value = 'error'
      title.value = '绑定失败'
      message.value = res?.message || '请稍后重试'
    }
  } catch (e) {
    status.value = 'error'
    title.value = '绑定失败'
    message.value = e?.response?.data?.message || e?.message || '网络异常，请稍后重试'
  }

  await refreshIcons()
})
</script>

<style scoped>
.dingtalk-callback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--bg-page, #f5f6f8);
}

.dingtalk-callback__card {
  width: 100%;
  max-width: 360px;
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.dingtalk-callback__icon {
  margin-bottom: 16px;
}

.dingtalk-callback__icon .icon {
  width: 48px;
  height: 48px;
}

.dingtalk-callback h1 {
  font-size: var(--text-xl);
  margin: 0 0 8px;
}

.dingtalk-callback p {
  margin: 0 0 24px;
  font-size: var(--text-xs);
  line-height: 1.5;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
