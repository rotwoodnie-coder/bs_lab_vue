<template>

  <div class="prototype-container pad-shell theme-teacher safe-top" data-layout="list-workbench">

    <div class="pad-main pad-workbench">

      <header class="pad-workbench__topbar">

        <PageBackButton fallback="/tasks" />

        <h1 class="pad-workbench__title flex-1 truncate">{{ board.taskTitle || '任务汇总' }}</h1>

      </header>



      <div class="pad-workbench__body">

        <div class="pad-workbench__main">

          <div class="pad-workbench__mobile-head px-4 safe-top stack-4">

            <header class="topbar page-topbar">

              <PageBackButton fallback="/tasks" />

              <h1 class="topbar-title text-xl flex-1 truncate">{{ board.taskTitle || '任务汇总' }}</h1>

            </header>

          </div>



          <div v-if="loading" class="px-4 py-12 text-center muted-2">加载中…</div>

          <div v-else-if="error" class="px-4 py-12 text-center stack-3">

            <p class="muted-2">{{ error }}</p>

            <router-link to="/tasks" class="btn btn-primary btn-sm">返回任务</router-link>

          </div>



          <template v-else>

            <div class="px-4 py-4 stack-3">

              <p v-if="board.className" class="text-xs muted">{{ board.className }}</p>

              <div v-if="taskOptions.length > 1" class="task-summary-switcher stack-2">
                <label class="text-xs font-bold muted-2">切换任务</label>
                <select class="select w-full" :value="taskId" @change="switchTask">
                  <option v-for="opt in taskOptions" :key="opt.taskId" :value="opt.taskId">
                    {{ opt.title }}{{ opt.pendingReview > 0 ? `（待审 ${opt.pendingReview}）` : '' }}
                  </option>
                </select>
              </div>

              <div class="grid grid-4 gap-2">

                <div class="card card-pad text-center">

                  <div class="text-lg font-bold text-brand">{{ board.submitRate }}%</div>

                  <div class="text-xs muted">完成率</div>

                </div>

                <div class="card card-pad text-center">

                  <div class="text-lg font-bold">{{ board.submitted }}</div>

                  <div class="text-xs muted">已提交</div>

                </div>

                <div class="card card-pad text-center">

                  <div class="text-lg font-bold text-warning">{{ board.pendingReview }}</div>

                  <div class="text-xs muted">待审核</div>

                </div>

                <div class="card card-pad text-center">

                  <div class="text-lg font-bold text-danger">{{ board.unsubmitted }}</div>

                  <div class="text-xs muted">未提交</div>

                </div>

              </div>

              <button

                v-if="activeTab === 'unsubmitted' && unsubmittedList.length && !board.cancelled"

                type="button"

                class="btn btn-sm btn-outline"

                :disabled="reminding"

                @click="remindAll"

              >

                {{ reminding ? '发送中…' : '提醒未提交学生' }}

              </button>

              <button

                v-if="!board.cancelled"

                type="button"

                class="btn btn-sm btn-danger btn-outline"

                @click="openCancelModal"

              >

                取消任务

              </button>

              <div v-else class="card card-pad surface-2 text-sm text-center tint-slate">

                该任务已取消，学生端待办将不再展示；已提交成果仍可查看与批阅。

              </div>

            </div>



            <div class="px-4 pb-3">

              <div class="tabs scroll-x">

                <button

                  v-for="tab in tabs"

                  :key="tab.key"

                  type="button"

                  class="tab"

                  :class="{ active: activeTab === tab.key }"

                  @click="switchTab(tab.key)"

                >

                  {{ tab.label }}

                  <span v-if="tab.count > 0" class="ml-1">({{ tab.count }})</span>

                </button>

              </div>

            </div>



            <div class="px-4 stack-3 pb-8">

              <p v-if="!visibleStudents.length" class="text-center py-8 muted-2 text-sm">{{ emptyLabel }}</p>



              <div v-for="student in visibleStudents" :key="student.userId" class="card card-pad stack-3">

                <div class="row items-center justify-between gap-2">

                  <div class="row items-center gap-2 min-w-0">

                    <UserAvatar
                      size="sm"
                      :name="student.name"
                      :src="student.avatarUrl"
                      grad-class="avatar-grad-violet-indigo"
                      role="student"
                    />

                    <div class="min-w-0">

                      <div class="text-sm font-bold truncate">{{ student.name }}</div>

                      <div class="text-xs muted">{{ student.reviewStatusLabel || '—' }}</div>

                    </div>

                  </div>

                  <span class="badge shrink-0" :class="badgeClass(student)">{{ student.reviewStatusLabel }}</span>

                </div>



                <template v-if="activeTab === 'pending' && student.workId">

                  <WorkReviewPanel

                    v-model="reviewForms[student.workId]"

                    :submitting="submittingId === student.workId"

                    @submit="submitReview(student)"

                  />

                  <router-link

                    :to="`/tasks/${taskId}/submissions/${student.workId}`"

                    class="text-xs text-brand font-medium text-center"

                  >查看提交详情</router-link>

                </template>



                <router-link

                  v-else-if="activeTab === 'completed' && student.workId"

                  :to="`/tasks/${taskId}/submissions/${student.workId}`"

                  class="btn btn-sm btn-outline text-brand"

                >查看批阅</router-link>

              </div>

            </div>

          </template>

        </div>

      </div>

    </div>

    <div v-if="cancelModalOpen" class="modal-backdrop" @click.self="closeCancelModal">
      <div class="modal-panel card card-pad stack-4">
        <h2 class="text-base font-bold">确认取消任务？</h2>
        <p class="text-sm muted">
          取消后，未提交的学生将不再看到该任务，也无法继续上传成果。
        </p>
        <div v-if="board.submitted > 0" class="card card-pad tint-amber text-sm stack-2">
          <p class="font-bold text-warning">已有 {{ board.submitted }} 名学生提交</p>
          <p class="muted">已提交的成果与批阅记录会保留，此操作不可撤销。</p>
          <label class="row items-start gap-2 text-xs">
            <input v-model="cancelAcknowledged" type="checkbox" class="mt-0.5" />
            <span>我已了解：强制取消后，已提交学生仍可查看历史成果</span>
          </label>
        </div>
        <div>
          <label class="text-xs font-bold muted-2">取消原因（可选）</label>
          <textarea v-model="cancelReason" class="textarea mt-2" rows="2" placeholder="如：选错实验、时间调整…"></textarea>
        </div>
        <div class="row gap-2">
          <button type="button" class="btn btn-outline flex-1" :disabled="cancelling" @click="closeCancelModal">
            我再想想
          </button>
          <button
            type="button"
            class="btn btn-danger flex-1"
            :disabled="cancelling || !canConfirmCancel"
            @click="confirmCancel"
          >
            {{ cancelling ? '取消中…' : '确认取消' }}
          </button>
        </div>
      </div>
    </div>

  </div>

</template>



<script setup>

import { ref, computed, onMounted, nextTick, watch, reactive } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import PageBackButton from '@/components/PageBackButton.vue'

import UserAvatar from '@/components/UserAvatar.vue'

import WorkReviewPanel from '@/components/teacher/WorkReviewPanel.vue'

import { fetchTeacherTaskBoard, fetchTeacherTasks, remindTeacherTask, submitTeacherReview, cancelTeacherTask } from '@/api/teacher'

import { showToast } from '@/utils/toast'
import { useLucideIcons } from '@/composables/useLucideIcons'



const route = useRoute()

const router = useRouter()

const taskId = computed(() => String(route.params.taskId || ''))



const loading = ref(true)

const error = ref('')

const reminding = ref(false)

const cancelling = ref(false)

const cancelModalOpen = ref(false)

const cancelReason = ref('')

const cancelAcknowledged = ref(false)

const submittingId = ref('')

const activeTab = ref('pending')

const taskOptions = ref([])

const board = ref({

  taskTitle: '',

  className: '',

  submitted: 0,

  unsubmitted: 0,

  submitRate: 0,

  pendingReview: 0,

  cancelled: false,

  students: []

})

const reviewForms = reactive({})



const canConfirmCancel = computed(() => {
  if (board.value.submitted > 0) return cancelAcknowledged.value
  return true
})



const pendingList = computed(() =>

  (board.value.students || []).filter((s) => s.done && (s.reviewStatus === 'pending' || !s.reviewStatus))

)

const unsubmittedList = computed(() => (board.value.students || []).filter((s) => !s.done))

const completedList = computed(() =>

  (board.value.students || []).filter((s) => s.done && s.reviewStatus && s.reviewStatus !== 'pending')

)



const tabs = computed(() => [

  { key: 'pending', label: '待审核', count: pendingList.value.length },

  { key: 'unsubmitted', label: '未提交', count: unsubmittedList.value.length },

  { key: 'completed', label: '已完成', count: completedList.value.length }

])



const visibleStudents = computed(() => {

  if (activeTab.value === 'pending') return pendingList.value

  if (activeTab.value === 'unsubmitted') return unsubmittedList.value

  return completedList.value

})



const emptyLabel = computed(() => {

  if (activeTab.value === 'pending') return '暂无待审核提交 🎉'

  if (activeTab.value === 'unsubmitted') return '全部已提交 🎉'

  return '暂无已完成记录'

})



function badgeClass(student) {

  if (!student.done) return 'badge-danger'

  if (student.reviewStatus === 'pending') return 'badge-warning'

  if (student.reviewStatus === 'rejected') return 'badge-danger'

  return 'badge-success'

}



function ensureReviewForm(workId) {

  if (!reviewForms[workId]) {

    reviewForms[workId] = { rating: '', comment: '', featured: false }

  }

}



watch(pendingList, (list) => {

  list.forEach((s) => {

    if (s.workId) ensureReviewForm(s.workId)

  })

}, { immediate: true })



const VALID_TABS = ['pending', 'unsubmitted', 'completed']



function applyRouteTab() {

  const tab = route.query.tab

  if (typeof tab === 'string' && VALID_TABS.includes(tab)) {

    activeTab.value = tab

    return true

  }

  return false

}



function switchTab(key) {

  activeTab.value = key

  router.replace({ path: route.path, query: { ...route.query, tab: key } })

}



function switchTask(event) {

  const nextId = event.target.value

  if (!nextId || nextId === taskId.value) return

  router.push({

    path: `/tasks/${nextId}/summary`,

    query: { tab: activeTab.value }

  })

}



async function loadTaskOptions() {

  try {

    const res = await fetchTeacherTasks()

    taskOptions.value = res?.code === 200 && Array.isArray(res.data) ? res.data : []

  } catch {

    taskOptions.value = []

  }

}



async function loadBoard() {

  if (!taskId.value) {

    error.value = '任务不存在'

    loading.value = false

    return

  }

  loading.value = true

  error.value = ''

  try {

    const res = await fetchTeacherTaskBoard(taskId.value)

    if (res?.code === 200 && res.data) {

      board.value = res.data

      if (!applyRouteTab()) {

        if (res.data.pendingReview === 0 && res.data.unsubmitted > 0) {

          activeTab.value = 'unsubmitted'

        }

      }

    } else {

      error.value = res?.message || '加载失败'

    }

  } catch {

    error.value = '加载失败，请稍后重试'

  } finally {

    loading.value = false

    initIcons()

  }

}



function openCancelModal() {
  cancelReason.value = ''
  cancelAcknowledged.value = false
  cancelModalOpen.value = true
}



function closeCancelModal() {
  if (cancelling.value) return
  cancelModalOpen.value = false
}



async function confirmCancel() {
  if (!taskId.value || cancelling.value || !canConfirmCancel.value) return
  cancelling.value = true
  try {
    const res = await cancelTeacherTask(taskId.value, {
      reason: cancelReason.value.trim() || undefined
    })
    if (res?.code === 200) {
      cancelModalOpen.value = false
      showToast(res.data?.message || '任务已取消')
      router.replace({ path: '/tasks', query: { status: 'cancelled' } })
      return
    }
    showToast(res?.message || '取消失败', 'danger')
  } catch (e) {
    showToast(e?.response?.data?.message || '取消失败', 'danger')
  } finally {
    cancelling.value = false
  }
}



async function remindAll() {

  if (!taskId.value || reminding.value) return

  reminding.value = true

  try {

    const res = await remindTeacherTask(taskId.value)

    if (res?.code === 200) {

      showToast(res.data?.message || '提醒已发送')

    } else {

      showToast(res?.message || '提醒发送失败', 'danger')

    }

  } catch {

    showToast('提醒发送失败', 'danger')

  } finally {

    reminding.value = false

  }

}



async function submitReview(student) {

  const workId = student.workId

  const form = reviewForms[workId]

  if (!form?.rating) {

    showToast('请选择评级', 'danger')

    return

  }

  submittingId.value = workId

  try {

    const res = await submitTeacherReview(workId, {

      rating: form.rating,

      comment: form.comment || undefined,

      featured: form.rating === 'excellent' ? Boolean(form.featured) : false

    })

    if (res?.code === 200) {

      showToast('批阅已提交')

      await loadBoard()

      return

    }

    showToast(res?.message || '批阅失败', 'danger')

  } catch (e) {

    showToast(e?.response?.data?.message || '批阅失败', 'danger')

  } finally {

    submittingId.value = ''

  }

}



const { initIcons } = useLucideIcons()



onMounted(() => {

  loadTaskOptions()

  applyRouteTab()

  loadBoard()

})



watch(taskId, () => {

  applyRouteTab()

  loadBoard()

})



watch(() => route.query.tab, () => {

  applyRouteTab()

})

</script>



<style scoped>

.grid-4 {

  display: grid;

  grid-template-columns: repeat(4, minmax(0, 1fr));

}

@media (max-width: 480px) {

  .grid-4 {

    grid-template-columns: repeat(2, minmax(0, 1fr));

  }

}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 50;
}

.modal-panel {
  width: 100%;
  max-width: 360px;
}

</style>


