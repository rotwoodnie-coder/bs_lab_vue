<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">实验作业学生记录</span>
          </div>
        </div>
      </template>

      <div class="standard-toolbar">
        <div class="standard-toolbar-inline">
          <!--
          <el-input v-model="query.keyword" placeholder="" clearable class="standard-search" @keyup.enter="loadItems" />
          -->
          <el-button @click="loadItems">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="standard-toolbar-right">
          <!--
          <el-button type="primary" @click="openCreateDialog">新增记录</el-button>
          -->
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="teacherExpName" label="教学实验" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <el-link
              v-if="scope.row.teacherExpId"
              type="primary"
              underline="never"
              @click="openExperimentDetail(scope.row)"
            >
              {{ scope.row.teacherExpName || scope.row.teacherExpId }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="teacherUserName" label="教师" min-width="140" show-overflow-tooltip />
        <el-table-column prop="requireDate" label="要求完成日期" min-width="140" show-overflow-tooltip />
        <el-table-column prop="studentUserName" label="学生" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentExpName" label="学生实验" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentSubmitDate" label="学生提交日期" min-width="140" show-overflow-tooltip />
        <el-table-column prop="markResult" label="评分" min-width="120" show-overflow-tooltip />
        <el-table-column prop="markTime" label="批改时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              :disabled="!String(scope.row.studentExpId || '').trim()"
              @click="openEditDialog(scope.row)"
            >
              批改作业
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ query.pageSize }} 条，共 {{ total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination background layout="共 {total} 条, sizes, prev, pager, next, jumper" :total="total" :current-page="query.pageNum" :page-size="query.pageSize" :page-sizes="pageSizes" @current-change="handlePageChange" @size-change="handleSizeChange" />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" class="user-dialog">
      <div class="mark-dialog-body">
        <div class="mark-video-panel">
          <div class="mark-panel-title">学生实验视频</div>
          <div v-loading="videoLoading" class="mark-video-content">
            <template v-if="videoList.length">
              <div v-for="item in videoList" :key="item.seqId || item.videoUrl || item.fileUrl || item.videoName" class="mark-video-item">
                <div class="mark-video-name">{{ item.videoName || item.name || item.title || '实验文件' }}</div>
                <video
                  v-if="isVideoFile(item) && getMediaUrl(item)"
                  class="mark-video-player"
                  controls
                  :src="getMediaUrl(item)"
                />
                <el-image
                  v-else-if="isImageFile(item) && getMediaUrl(item)"
                  class="mark-image-player"
                  :src="getMediaUrl(item)"
                  fit="contain"
                  :preview-src-list="[getMediaUrl(item)]"
                  preview-teleported
                />
                <el-link
                  v-else-if="getMediaUrl(item, true)"
                  :href="getMediaUrl(item, true)"
                  target="_blank"
                  type="primary"
                >
                  打开文件
                </el-link>
                <div v-else class="mark-video-empty">暂无可预览地址</div>
              </div>
            </template>
            <el-empty v-else description="暂无文件" />
          </div>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="130px" class="user-form mark-form">
          <el-form-item label="评分" prop="markResult"><el-input v-model="form.markResult" /></el-form-item>
          <el-form-item label="批改意见" prop="markComments"><el-input v-model="form.markComments" type="textarea" :rows="6" /></el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="实验详情" width="1200px" class="user-dialog view-dialog" destroy-on-close @closed="closeViewDialog">
      <ExpStandardDetailView v-if="viewDialogVisible" :exp-id="viewExpId" :show-back-button="false" />
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createExpHomeworkStudent, deleteExpHomeworkStudent, fetchExpHomeworkStudents, updateExpHomeworkStudent } from '../../api/index'
import { fetchExpVideos } from '../../api/exp'
import ExpStandardDetailView from '../exp/ExpStandardDetailView.vue'
const viewExpId = ref('')
const viewDialogVisible = ref(false)
const videoLoading = ref(false)
const videoList = ref([])

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const total = ref(0)
const tableData = ref([])
const pageSizes = [10, 20, 30, 40, 50]
const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const query = reactive({ keyword: '', teacherUserId: currentUserInfo.userId || '', pageNum: 1, pageSize: 10 })
const form = reactive({ homeworkId: '', teacherExpId: '', teacherUserId: '', requireDate: '', studentExpId: '', studentSubmitDate: '', markUserId: '', markResult: '', markComments: '' })
const rules = {
  homeworkId: [{ required: true, message: '请输入作业ID', trigger: 'blur' }]
}

const resetForm = () => Object.assign(form, { homeworkId: '', teacherExpId: '', teacherUserId: '', requireDate: '', studentExpId: '', studentSubmitDate: '', markUserId: '', markResult: '', markComments: '' })

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchExpHomeworkStudents(query)
    if (res.data.code === 200) {
      tableData.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadStudentVideos = async (studentExpId) => {
  const expId = String(studentExpId || '').trim()
  videoList.value = []
  if (!expId) return
  videoLoading.value = true
  try {
    const res = await fetchExpVideos(expId)
    if (res.data.code === 200) {
      const data = res.data.data
      videoList.value = Array.isArray(data) ? data : (data ? [data] : [])
    } else {
      ElMessage.error(res.data.message || '加载视频失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载视频失败')
  } finally {
    videoLoading.value = false
  }
}

const getMediaUrl = (item, allowFallback = false) => {
  const url = String(item?.previewUrl || item?.fileUrl || item?.url || item?.path || item?.videoPath || item?.imageUrl || item?.imgUrl || '').trim()
  if (url) return url
  if (!allowFallback) return ''
  const fallback = String(item?.videoName || item?.imageName || item?.name || item?.title || '').trim()
  return fallback.startsWith('http') ? fallback : ''
}

const getFileExt = (item) => {
  const name = String(item?.videoName || item?.imageName || item?.name || item?.title || getMediaUrl(item) || '').trim()
  const clean = name.split('?')[0].split('#')[0]
  const idx = clean.lastIndexOf('.')
  return idx >= 0 ? clean.slice(idx + 1).toLowerCase() : ''
}

const isVideoFile = (item) => {
  const ext = getFileExt(item)
  return ['mp4', 'webm', 'ogg', 'mov', 'm4v', 'avi', 'mkv'].includes(ext)
}

const isImageFile = (item) => {
  const ext = getFileExt(item)
  return ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg'].includes(ext)
}

const resetQuery = () => { query.keyword = ''; query.teacherUserId = currentUserInfo.userId || ''; query.pageNum = 1; query.pageSize = 10; loadItems() }
const handlePageChange = (page) => { query.pageNum = page; loadItems() }
const handleSizeChange = (size) => { query.pageNum = 1; query.pageSize = size; loadItems() }
const openCreateDialog = () => { isEdit.value = false; editId.value = ''; dialogTitle.value = '新增记录'; resetForm(); dialogVisible.value = true }
const openEditDialog = async (row) => { isEdit.value = true; editId.value = row.seqId; dialogTitle.value = '批改作业'; form.homeworkId = row.homeworkId || ''; form.teacherExpId = row.teacherExpId || ''; form.teacherUserId = row.teacherUserId || ''; form.requireDate = row.requireDate || ''; form.studentExpId = row.studentExpId || ''; form.studentSubmitDate = row.studentSubmitDate || ''; form.markUserId = row.markUserId || ''; form.markResult = row.markResult || ''; form.markComments = row.markComments || ''; dialogVisible.value = true; await loadStudentVideos(row.studentExpId) }
const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const res = isEdit.value ? await updateExpHomeworkStudent(editId.value, form) : await createExpHomeworkStudent(form)
      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadItems()
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除记录【${row.seqId}】吗？`, '提示', { type: 'warning' })
  try {
    const res = await deleteExpHomeworkStudent(row.seqId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadItems()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

const openExperimentDetail = (row) => {
  const expId = String(row?.teacherExpId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  viewExpId.value = expId
  viewDialogVisible.value = true
}

const closeViewDialog = () => {
  viewDialogVisible.value = false
  viewExpId.value = ''
}

onMounted(loadItems)
</script>

<style scoped>
.standard-toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:12px}.standard-toolbar-inline{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.standard-search{width:360px}
.mark-dialog-body{display:flex;gap:20px;align-items:flex-start;flex-wrap:wrap}
.mark-video-panel{flex:1 1 220px;min-width:0;border:1px solid var(--el-border-color-light);border-radius:8px;padding:16px;background:var(--el-fill-color-lighter)}
.mark-panel-title{font-size:15px;font-weight:600;margin-bottom:12px}
.mark-video-content{display:flex;flex-direction:column;gap:16px;min-height:120px}
.mark-video-item{display:flex;flex-direction:column;gap:8px}
.mark-video-name{font-weight:500;color:var(--el-text-color-primary)}
.mark-video-player{width:100%;max-height:420px;background:#000;border-radius:8px}
.mark-image-player{width:100%;max-height:420px;border-radius:8px;overflow:hidden;background:var(--el-fill-color-light)}
.mark-video-empty{color:var(--el-text-color-secondary);font-size:13px}
.mark-form{flex:0 0 350px;min-width:280px}
</style>
