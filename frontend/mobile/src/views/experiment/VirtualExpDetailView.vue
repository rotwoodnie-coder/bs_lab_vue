<template>
  <div class="prototype-container pad-shell safe-top" data-layout="sim-embed" data-sim-embed>
    <header class="topbar page-topbar safe-top">
      <button type="button" class="icon-btn page-back" aria-label="返回" @click="goBack">
        <i data-lucide="arrow-left" class="icon"></i>
      </button>
      <h1 class="topbar-title">{{ title }}</h1>
    </header>

    <div v-if="loading" class="flex items-center justify-center flex-1 py-12">
      <p class="muted-2">加载中…</p>
    </div>
    <div v-else-if="error" class="flex flex-col items-center justify-center flex-1 py-12 px-4">
      <p class="muted-2">{{ error }}</p>
      <button type="button" class="btn btn-ghost btn-sm mt-4" @click="goBack">返回列表</button>
    </div>
    <iframe
      v-else-if="simulatorUrl"
      class="sim-embed__frame"
      :src="simulatorUrl"
      :title="title"
      allow="fullscreen; autoplay; clipboard-read; clipboard-write"
      allowfullscreen
    ></iframe>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchSimulatorDetail } from '@/api/simulator'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { resolveMediaUrl } from '@/utils/fileUrl'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const title = ref('模拟实验')
const simulatorUrl = ref('')

const { initIcons } = useLucideIcons()

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/experiments')
  }
}

async function loadDetail() {
  const id = route.params.id
  if (!id) {
    error.value = '无效的实验 ID'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''
  try {
    const res = await fetchSimulatorDetail(id)
    const data = res && res.data
    if (!data) {
      error.value = '未找到该模拟实验'
      return
    }
    title.value = data.simulatorName || '模拟实验'
    document.title = `${title.value} · 模拟实验 · 宝山小实验社区`

    // simulatorPreviewUrl（后端已解析的完整 URL）优先，其次 simulatorUrl
    const url = resolveMediaUrl(data, 'simulatorUrl')
    if (!url) {
      error.value = '该实验暂未配置模拟地址'
      return
    }
    simulatorUrl.value = url
  } catch (e) {
    console.warn('加载模拟实验详情失败', e)
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
    initIcons()
  }
}

onMounted(() => {
  loadDetail()
})
</script>
