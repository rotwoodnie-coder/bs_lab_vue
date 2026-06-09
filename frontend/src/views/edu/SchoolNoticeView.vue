<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">教学管理 > 通知公告</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="通知名称/内容" clearable @change="loadItems" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" clearable placeholder="全部" style="width: 160px" @change="loadItems">
              <el-option label="草稿" value="c" />
              <el-option label="待发布" value="y" />
              <el-option label="已发布" value="r" />
              <el-option label="作废" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="dict-toolbar-buttons">
          <el-button type="primary" @click="openCreateDialog">新增通知</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="noticeName" label="通知名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="noticeOrgName" label="学校" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="releaseUserName" label="发布人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="releaseTime" label="发布时间" min-width="170" show-overflow-tooltip :formatter="formatDateTime" />
        <el-table-column prop="createUserName" label="创建人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" show-overflow-tooltip :formatter="formatDateTime" />
        <el-table-column prop="updateUserName" label="修改人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="修改时间" min-width="170" show-overflow-tooltip :formatter="formatDateTime" />
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <template v-if="scope.row.status === 'c'">
              <el-button link type="success" @click="openConfirmDialog(scope.row)">确认内容</el-button>
              <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="handleVoid(scope.row)">作废</el-button>
            </template>
            <template v-else-if="scope.row.status === 'y'">
              <el-button link type="warning" @click="openPublishDialog(scope.row)">发布</el-button>
            </template>
            <el-button link type="info" @click="openViewDialog(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ queryForm.pageSize }} 条，共 {{ total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" :current-page="queryForm.pageNum" :page-size="queryForm.pageSize" :page-sizes="pageSizes" @current-change="handlePageChange" @size-change="handleSizeChange" />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="viewDialogVisible" title="查看通知" width="780px" @closed="handleViewDialogClosed">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="通知名称">{{ viewForm.noticeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label=""></el-descriptions-item>
        <el-descriptions-item label="学校">{{ viewForm.noticeOrgName || viewForm.noticeOrgId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(viewForm.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ viewForm.createUserName || viewForm.createUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(null, null, viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="修改人">{{ viewForm.updateUserName || viewForm.updateUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="修改时间">{{ formatDateTime(null, null, viewForm.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ viewForm.releaseUserName || viewForm.releaseUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDateTime(null, null, viewForm.releaseTime) }}</el-descriptions-item>
        <el-descriptions-item label="通知内容" :span="2">
          <div class="notice-text-display notice-content-display notice-content-view-display">
            {{ viewForm.noticeContent || '-' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="680px" @closed="handleEditDialogClosed">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="通知名称" prop="noticeName">
          <el-input v-model="editForm.noticeName" placeholder="请输入通知名称" :maxlength="60" />
        </el-form-item>
        <el-form-item label="通知内容" prop="noticeContent">
          <el-input v-model="editForm.noticeContent" type="textarea" :rows="12" placeholder="请输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmDialogVisible" title="确认内容" width="680px" @closed="handleConfirmDialogClosed">
      <el-form :model="confirmForm" label-width="100px">
        <el-form-item label="通知名称">
          <div class="notice-text-display">{{ confirmForm.noticeName || '-' }}</div>
        </el-form-item>
        <el-form-item label="通知内容">
          <div class="notice-text-display notice-content-display">{{ confirmForm.noticeContent || '-' }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="submitLoading" @click="handleConfirmSubmit">确认内容</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="publishDialogVisible" title="发布通知" width="680px" @closed="handlePublishDialogClosed">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="通知名称">
          <div class="notice-text-display">{{ publishForm.noticeName || '-' }}</div>
        </el-form-item>
        <el-form-item label="通知内容">
          <div class="notice-text-display notice-content-display">{{ publishForm.noticeContent || '-' }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="submitLoading" @click="handlePublishSubmit">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createSchoolNotice, deleteSchoolNotice, fetchSchoolNotices, updateSchoolNotice,publishSchoolNotice } from '../../api/edu'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const publishDialogVisible = ref(false)
const editDialogTitle = ref('新增通知')
const editFormRef = ref()
const viewForm = reactive({})
const editForm = reactive({ noticeName: '', noticeContent: '', noticeOrgId: '', status: 'c' })
const confirmForm = reactive({ noticeName: '', noticeContent: '', noticeOrgId: '', status: 'y' })
const publishForm = reactive({ noticeName: '', noticeContent: '', noticeOrgId: '', status: 'r' })
const editingId = ref('')

const queryForm = reactive({ keyword: '', status: '', noticeOrgId: userInfo.rootOrgId || '', pageNum: 1, pageSize: 10 })

const rules = {
  noticeName: [{ required: true, message: '请输入通知名称', trigger: 'blur' }],
  noticeContent: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchSchoolNotices(queryForm)
    const data = res?.data?.data || res?.data || {}
    tableData.value = data.records || data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  queryForm.noticeOrgId = userInfo.rootOrgId || ''
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  loadItems()
}

const fillEditForm = (row) => {
  editingId.value = row.noticeId || row.notice_id || ''
  editForm.noticeName = row.noticeName || row.notice_name || ''
  editForm.noticeContent = row.noticeContent || row.notice_content || ''
  editForm.noticeOrgId = row.noticeOrgId || row.notice_org_id || userInfo.rootOrgId || ''
  editForm.status = row.status || 'y'
}

const fillViewForm = (row) => {
  Object.assign(viewForm, row)
}

const openCreateDialog = () => {
  editDialogTitle.value = '新增通知'
  editingId.value = ''
  editForm.noticeName = ''
  editForm.noticeContent = ''
  editForm.noticeOrgId = userInfo.rootOrgId || ''
  editForm.status = 'y'
  editDialogVisible.value = true
}

const openViewDialog = (row) => {
  fillViewForm(row)
  viewDialogVisible.value = true
}

const openConfirmDialog = (row) => {
  editingId.value = row.noticeId || row.notice_id || ''
  confirmForm.noticeName = row.noticeName || row.notice_name || ''
  confirmForm.noticeContent = row.noticeContent || row.notice_content || ''
  confirmDialogVisible.value = true
}

const openEditDialog = (row) => {
  editDialogTitle.value = '编辑通知'
  fillEditForm(row)
  editDialogVisible.value = true
}

const openPublishDialog = (row) => {
  fillEditForm(row)
  publishForm.noticeName = editForm.noticeName
  publishForm.noticeContent = editForm.noticeContent
  publishForm.noticeOrgId = editForm.noticeOrgId
  publishForm.status = 'r'
  publishDialogVisible.value = true
}

const handleEditSubmit = async () => {
  await editFormRef.value?.validate()
  submitLoading.value = true
  try {
    editForm.status='c'
    if (editingId.value) await updateSchoolNotice(editingId.value, editForm)
    else await createSchoolNotice(editForm)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadItems()
  } finally {
    submitLoading.value = false
  }
}

const handleConfirmSubmit = async () => {
  await ElMessageBox.confirm('请确认通知内容无误，确认之后不能修改？', '再次确认', { type: 'warning' })
  submitLoading.value = true
  try {
    await updateSchoolNotice(editingId.value, {
      noticeName: confirmForm.noticeName,
      noticeContent: confirmForm.noticeContent,
      noticeOrgId: queryForm.noticeOrgId,
      status: 'y',
    })
    ElMessage.success('内容确认成功')
    confirmDialogVisible.value = false
    loadItems()
  } finally {
    submitLoading.value = false
  }
}

const handlePublishSubmit = async () => {
  await ElMessageBox.confirm('确认发布该通知吗？', '再次确认', { type: 'warning' })
  submitLoading.value = true
  try {
    await publishSchoolNotice(editingId.value, {
      releaseUserId: userInfo.rootOrgId || '',
      status: 'r',
    })
    ElMessage.success('发布成功')
    publishDialogVisible.value = false
    loadItems()
  } finally {
    submitLoading.value = false
  }
}

const handleVoid = async (row) => {
  await ElMessageBox.confirm(`确定将通知「${row.noticeName || ''}」作废吗？`, '提示', { type: 'warning' })
  submitLoading.value = true
  try {
    await updateSchoolNotice(row.noticeId || row.notice_id || editingId.value, {
      noticeName: row.noticeName || row.notice_name || '',
      noticeContent: row.noticeContent || row.notice_content || '',
      noticeOrgId: row.noticeOrgId || row.notice_org_id || userInfo.rootOrgId || '',
      status: 'n',
    })
    ElMessage.success('已作废')
    loadItems()
  } finally {
    submitLoading.value = false
  }
}


const handlePageChange = (pageNum) => {
  queryForm.pageNum = pageNum
  loadItems()
}
const handleSizeChange = (pageSize) => {
  queryForm.pageSize = pageSize
  queryForm.pageNum = 1
  loadItems()
}
const handleEditDialogClosed = () => editFormRef.value?.resetFields()
const handleConfirmDialogClosed = () => { confirmForm.noticeName = ''; confirmForm.noticeContent = '' }
const handleViewDialogClosed = () => { Object.keys(viewForm).forEach((key) => { delete viewForm[key] }) }
const handlePublishDialogClosed = () => { publishForm.noticeName = ''; publishForm.noticeContent = ''; publishForm.noticeOrgId = ''; publishForm.status = 'r' }
const getStatusText = (status) => ({ c: '草稿', y: '待发布', n: '作废', r: '已发布' }[status] || status || '-')
const getStatusType = (status) => ({ c: 'info', y: 'success', n: 'danger', r: 'warning' }[status] || 'info')
const formatDateTime = (_row, _column, cellValue) => {
  if (!cellValue) return '-'
  const date = new Date(cellValue)
  if (Number.isNaN(date.getTime())) return cellValue
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(loadItems)
</script>

<style scoped>
.notice-text-display {
  width: 100%;
  min-height: 40px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  box-sizing: border-box;
}

.notice-content-display {
  min-height: 220px;
}

.notice-content-view-display {
  max-height: 320px;
  overflow: auto;
}
</style>
