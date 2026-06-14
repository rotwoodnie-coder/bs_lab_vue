<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">实验作业管理</span>
          </div>
        </div>
      </template>

      <div class="standard-toolbar">
        <div class="standard-toolbar-inline">
          <el-input v-model="query.keyword" placeholder="实验/教师/班级/要求日期" clearable class="standard-search" @keyup.enter="loadItems" />
          <el-button @click="loadItems">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="standard-toolbar-right">
          <el-button type="primary" @click="openCreateDialog">新增作业</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="teacherExpName" label="关联实验" min-width="180" show-overflow-tooltip>
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
        <el-table-column prop="tearcherUserName" label="教师" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.teacherUserName || scope.row.tearcherUserName || scope.row.tearcherUserId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="className" label="班级" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.className || scope.row.classId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="requireDate" label="要求完成日期" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="120" align="center">
          <template #default="scope">{{ statusLabel(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip>
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-button v-if="String(scope.row.status || '').toLowerCase() === 'c'" link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="String(scope.row.status || '').toLowerCase() === 'c'" link type="success" @click="handleAssign(scope.row)">布置作业</el-button>
            <el-button v-if="String(scope.row.status || '').toLowerCase() === 'c'" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="user-form">
        <el-row :gutter="16">
          <el-col :span="16">
            <el-form-item label="关联实验" prop="teacherExpId">
              <el-select v-model="form.teacherExpId" filterable clearable placeholder="请选择实验" style="width: 100%" @focus="loadExperimentOptions">
                <el-option v-for="item in experimentOptions" :key="item.expId" :label="item.expName" :value="item.expId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="form.classId" filterable clearable placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="要求完成日期" prop="requireDate">
              <el-date-picker
                v-model="form.requireDate"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                placeholder="请选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
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
import { assignExpHomework, createExpHomework, deleteExpHomework, fetchExpHomeworks, updateExpHomework } from '../../api/index'
import { fetchExpStandardsAll } from '../../api/exp'
import { fetchTeacherClasses } from '../../api/edu'
import { fetchOrgTree } from '../../api/system'
import ExpStandardDetailView from '../exp/ExpStandardDetailView.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增作业')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const total = ref(0)
const tableData = ref([])
const pageSizes = [10, 20, 30, 40, 50]
const experimentOptions = ref([])
const classOptions = ref([])
const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const query = reactive({ keyword: '', teacherUserId: currentUserInfo.userId || '', pageNum: 1, pageSize: 10 })
const form = reactive({ teacherExpId: '', tearcherUserId: '', classId: '', status: 'c', requireDate: '' })
const viewExpId = ref('')
const viewDialogVisible = ref(false)

const statusLabel = (value) => ({ c: '待布置', y: '已布置', n: '已作废' }[String(value || '').toLowerCase()] || value || '-')
const formatDateTime = (value) => {
  if (!value) return '-'
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
const rules = {
  teacherExpId: [{ required: true, message: '请选择关联实验', trigger: 'change' }],
  classId: [{ required: true, message: '请输入班级ID', trigger: 'blur' }],
  requireDate: [{ required: true, message: '请输入要求完成日期', trigger: 'blur' }]
}

const resetForm = () => Object.assign(form, { teacherExpId: '', tearcherUserId: currentUserInfo.userId || '', classId: '', status: 'c', requireDate: '' })

const loadExperimentOptions = async () => {
  if (experimentOptions.value.length) return
  try {
    const res = await fetchExpStandardsAll({ pageNum: 1, pageSize: 200, status: 'y', expType: 'standard' })
    if (res.data.code === 200) {
      experimentOptions.value = res.data.data?.records || []
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载实验列表失败')
  }
}

const normalizeOrgTree = (nodes = []) => nodes.map(node => ({
  ...node,
  children: normalizeOrgTree(node.children || [])
}))

const loadClassOptions = async () => {
  try {
    const [treeRes, teacherClassRes] = await Promise.all([
      fetchOrgTree(),
      fetchTeacherClasses({ teacherId: currentUserInfo.userId || '', pageNum: 1, pageSize: 200 })
    ])
    if (treeRes.data.code === 200) {
      const treeMap = new Map()
      const walk = (nodes = []) => {
        nodes.forEach((node) => {
          treeMap.set(node.orgId, node)
          walk(node.children || [])
        })
      }
      const treeData = normalizeOrgTree(Array.isArray(treeRes.data.data) ? treeRes.data.data : [])
      walk(treeData)
      const classIds = Array.isArray(teacherClassRes.data.data?.records)
        ? teacherClassRes.data.data.records.flatMap(item => item.classIds || [])
        : []
      const uniqueClassIds = [...new Set(classIds)]
      classOptions.value = uniqueClassIds.map(id => {
        const org = treeMap.get(id) || {}
        return { value: id, label: org.orgName || id }
      })
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载班级列表失败')
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchExpHomeworks(query)
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

const resetQuery = () => {
  query.keyword = ''
  query.teacherUserId = currentUserInfo.userId || ''
  query.pageNum = 1
  query.pageSize = 10
  loadItems()
}
const handlePageChange = (page) => { query.pageNum = page; loadItems() }
const handleSizeChange = (size) => { query.pageNum = 1; query.pageSize = size; loadItems() }

const openCreateDialog = async () => { isEdit.value = false; editId.value = ''; dialogTitle.value = '新增作业'; resetForm(); await loadExperimentOptions(); await loadClassOptions(); dialogVisible.value = true }
const openEditDialog = async (row) => { isEdit.value = true; editId.value = row.homeworkId; dialogTitle.value = '编辑作业'; form.teacherExpId = row.teacherExpId || ''; form.tearcherUserId = currentUserInfo.userId || ''; form.classId = row.classId || ''; form.requireDate = row.requireDate || ''; await loadExperimentOptions(); await loadClassOptions(); dialogVisible.value = true }

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

const handleAssign = async (row) => {
  if (String(row.status || '').toLowerCase() !== 'c') return
  try {
    await ElMessageBox.confirm(`确定要布置作业【${row.teacherExpName || row.teacherExpId || row.homeworkId}】吗？`, '提示', { type: 'warning' })
    const res = await assignExpHomework(row.homeworkId)
    if (res.data.code === 200) {
      ElMessage.success('布置成功')
      loadItems()
    } else {
      ElMessage.error(res.data.message || '布置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '布置失败')
    }
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const res = isEdit.value ? await updateExpHomework(editId.value, form) : await createExpHomework(form)
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
  try {
    await ElMessageBox.confirm(`确定删除作业【${row.teacherExpName || row.teacherExpId || row.homeworkId}】吗？`, '提示', { type: 'warning' })
    const res = await deleteExpHomework(row.homeworkId)
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

onMounted(async () => {
  await loadExperimentOptions()
  await loadClassOptions()
  await loadItems()
})
</script>

<style scoped>
.standard-toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:12px}.standard-toolbar-inline{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.standard-search{width:320px}</style>
