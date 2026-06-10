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
          <el-input v-model="query.keyword" placeholder="搜索记录ID/作业ID/实验ID/学生实验ID" clearable class="standard-search" @keyup.enter="loadItems" />
          <el-button @click="loadItems">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="standard-toolbar-right">
          <el-button type="primary" @click="openCreateDialog">新增记录</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="seqId" label="记录ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="homeworkId" label="作业ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="teacherExpId" label="教师实验ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="teacherUserId" label="教师ID" min-width="140" show-overflow-tooltip />
        <el-table-column prop="studentExpId" label="学生实验ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentSubmitDate" label="提交日期" min-width="140" show-overflow-tooltip />
        <el-table-column prop="markResult" label="批改结果" min-width="120" show-overflow-tooltip />
        <el-table-column prop="markTime" label="批改时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="820px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px" class="user-form">
        <el-form-item label="作业ID" prop="homeworkId"><el-input v-model="form.homeworkId" /></el-form-item>
        <el-form-item label="教师实验ID" prop="teacherExpId"><el-input v-model="form.teacherExpId" /></el-form-item>
        <el-form-item label="教师ID" prop="teacherUserId"><el-input v-model="form.teacherUserId" /></el-form-item>
        <el-form-item label="要求完成日期" prop="requireDate"><el-input v-model="form.requireDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="学生实验ID" prop="studentExpId"><el-input v-model="form.studentExpId" /></el-form-item>
        <el-form-item label="提交日期" prop="studentSubmitDate"><el-input v-model="form.studentSubmitDate" placeholder="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="批改人ID" prop="markUserId"><el-input v-model="form.markUserId" /></el-form-item>
        <el-form-item label="批改结果" prop="markResult"><el-input v-model="form.markResult" /></el-form-item>
        <el-form-item label="批改意见" prop="markComments"><el-input v-model="form.markComments" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createExpHomeworkStudent, deleteExpHomeworkStudent, fetchExpHomeworkStudents, updateExpHomeworkStudent } from '../../api/index'

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
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
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

const resetQuery = () => { query.keyword = ''; query.pageNum = 1; query.pageSize = 10; loadItems() }
const handlePageChange = (page) => { query.pageNum = page; loadItems() }
const handleSizeChange = (size) => { query.pageNum = 1; query.pageSize = size; loadItems() }
const openCreateDialog = () => { isEdit.value = false; editId.value = ''; dialogTitle.value = '新增记录'; resetForm(); dialogVisible.value = true }
const openEditDialog = (row) => { isEdit.value = true; editId.value = row.seqId; dialogTitle.value = '编辑记录'; form.homeworkId = row.homeworkId || ''; form.teacherExpId = row.teacherExpId || ''; form.teacherUserId = row.teacherUserId || ''; form.requireDate = row.requireDate || ''; form.studentExpId = row.studentExpId || ''; form.studentSubmitDate = row.studentSubmitDate || ''; form.markUserId = row.markUserId || ''; form.markResult = row.markResult || ''; form.markComments = row.markComments || ''; dialogVisible.value = true }
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

onMounted(loadItems)
</script>

<style scoped>
.standard-toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:12px}.standard-toolbar-inline{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.standard-search{width:360px}</style>
