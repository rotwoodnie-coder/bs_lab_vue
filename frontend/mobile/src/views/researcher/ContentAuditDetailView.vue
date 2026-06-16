<template>
  <div class="prototype-container pad-shell safe-top safe-bottom" data-layout="detail">
    <div class="topbar safe-top">
      <PageBackButton fallback="/content-audits" />
      <h1 class="topbar-title flex-1 truncate">{{ detail.expName || '实验审核' }}</h1>
    </div>

    <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>
    <div v-else-if="error" class="px-4 py-12 text-center stack-3">
      <p class="muted-2">{{ error }}</p>
      <router-link to="/content-audits" class="btn btn-primary btn-sm">返回列表</router-link>
    </div>

    <div v-else class="px-4 py-4 stack-4 pb-8">
      <div class="card card-pad stack-3">
        <div class="row justify-between items-start gap-2">
          <span class="badge badge-info">{{ expTypeLabel }}</span>
          <span class="badge badge-warning">待审核</span>
        </div>
        <div class="text-base font-bold">{{ detail.expName }}</div>
        <div v-if="detail.expPrinciple" class="text-sm stack-2">
          <div class="text-xs font-medium muted">实验原理</div>
          <p>{{ detail.expPrinciple }}</p>
        </div>
        <div v-if="detail.expCaution" class="text-sm stack-2">
          <div class="text-xs font-medium muted">注意事项</div>
          <p>{{ detail.expCaution }}</p>
        </div>
      </div>

      <div class="card card-pad stack-3">
        <div class="text-xs font-medium muted">审核意见</div>
        <textarea v-model="confirmComments" class="textarea" rows="3" placeholder="驳回时请填写原因…"></textarea>
        <div class="row gap-2">
          <button
            type="button"
            class="btn btn-primary flex-1"
            :disabled="submitting"
            @click="submitAudit('y')"
          >通过</button>
          <button
            type="button"
            class="btn btn-outline flex-1"
            :disabled="submitting"
            @click="submitAudit('n')"
          >驳回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageBackButton from '@/components/PageBackButton.vue'
import { fetchResearcherExpAuditDetail, auditResearcherExp } from '@/api/researcher'

const route = useRoute()
const router = useRouter()
const expId = computed(() => String(route.params.expId || ''))

const loading = ref(true)
const error = ref('')
const submitting = ref(false)
const detail = ref({})
const confirmComments = ref('')

const expTypeLabel = computed(() => {
  const t = String(detail.value.expType || '').toLowerCase()
  if (t === 'teach' || t === 'teaching') return '教学实验'
  if (t === 'standard') return '标准实验'
  return '实验'
})

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchResearcherExpAuditDetail(expId.value)
    if (res?.code === 200 && res.data) {
      detail.value = res.data
    } else {
      error.value = res?.message || '加载失败'
    }
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

async function submitAudit(status) {
  if (status === 'n' && !confirmComments.value.trim()) {
    alert('驳回时请填写审核意见')
    return
  }
  if (!confirm(`确认${status === 'y' ? '通过' : '驳回'}该实验？`)) return
  submitting.value = true
  try {
    const res = await auditResearcherExp(expId.value, {
      status,
      confirmComments: confirmComments.value.trim() || undefined
    })
    if (res?.code === 200) {
      router.replace('/content-audits')
      return
    }
    alert(res?.message || '审核失败')
  } catch (e) {
    alert(e?.response?.data?.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>
