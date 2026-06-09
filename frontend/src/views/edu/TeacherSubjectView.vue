<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">教学管理 > 教师授课关系维护</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="教师姓名/账号/昵称" clearable @change="loadItems" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="userName" label="教师姓名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="loginName" label="登录账号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="userNickName" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'">{{ scope.row.status === 'y' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subjectNames" label="授课学科" min-width="180" show-overflow-tooltip>
          <template #default="scope">{{ getSubjectNames(scope.row).join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openDialog(scope.row)">分配学科</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ queryForm.pageSize }} 条，共 {{ total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :current-page="queryForm.pageNum"
            :page-size="queryForm.pageSize"
            :page-sizes="pageSizes"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="分配授课学科" width="520px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="教师姓名">
          <el-input v-model="form.userName" disabled />
        </el-form-item>
        <el-form-item label="授课学科" prop="subjectIds">
          <el-checkbox-group v-model="form.subjectIds" class="teacher-subject-checkbox-group">
            <el-checkbox v-for="item in subjectOptions" :key="item.subject_id || item.subjectId || item.id" :label="item.subject_id || item.subjectId || item.id">
              {{ item.subject_name || item.subjectName || item.name || item.id }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <!--
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="y">启用</el-radio>
            <el-radio label="n">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        -->
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDataDictItems, fetchTeacherSubjects, saveTeacherSubject } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const queryForm = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const subjectOptions = ref([])
const currentTeacherId = ref('')
const form = reactive({ userName: '', subjectIds: [], status: 'y' })
const subjectNameMap = computed(() => Object.fromEntries(subjectOptions.value.map(item => [item.subject_id || item.subjectId || item.id, item.subject_name || item.subjectName || item.name || item.id])))

const rules = {
  subjectIds: [{ required: true, message: '请选择学科', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getSubjectNames = (row) => {
  const ids = Array.isArray(row?.subjectIds) ? row.subjectIds : []
  return ids.map(id => subjectNameMap.value[id] || id).filter(Boolean)
}

const loadSubjects = async () => {
  try {
    const res = await fetchDataDictItems('data_school_subject')
    if (res.data.code === 200) {
      subjectOptions.value = (Array.isArray(res.data.data) ? res.data.data : []).filter(item => String(item.status || '').toLowerCase() === 'y')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载学科失败')
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherSubjects({
      keyword: queryForm.keyword,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      tableData.value = Array.isArray(data) ? data : (data.records || data.list || [])
      total.value = data.total || tableData.value.length || 0
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
  queryForm.keyword = ''
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  loadItems()
}

const handlePageChange = (page) => {
  queryForm.pageNum = page
  loadItems()
}

const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadItems()
}

const openDialog = (row) => {
  currentTeacherId.value = row.userId
  form.userName = row.userName || row.loginName || ''
  form.subjectIds = Array.isArray(row.subjectIds) ? row.subjectIds : (row.subjectIds ? [row.subjectIds] : [])
  form.status = row.teacherSubjectStatus || 'y'
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  form.userName = ''
  form.subjectIds = []
  form.status = 'y'
  formRef.value?.clearValidate?.()
}

const handleSubmit = async () => {
  if (!formRef.value || !currentTeacherId.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const res = await saveTeacherSubject(currentTeacherId.value, { subjectIds: form.subjectIds, status: form.status })
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadSubjects()
  await loadItems()
})
</script>
