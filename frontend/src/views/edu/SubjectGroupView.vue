<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">教学管理 > 教研组管理</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="教研组名称/备注/学科" clearable @change="loadItems" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="dict-toolbar-buttons">
          <el-button type="primary" @click="openCreateDialog">新增教研组</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="groupName" label="教研组名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="subjectName" label="所属学科" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ getSubjectName(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="comments" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'">{{ scope.row.status === 'y' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="researcherCount" label="教研员数" width="100" align="center">
          <template #default="scope">{{ getResearcherCount(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="success" @click="openResearcherDialog(scope.row)">分配教研员</el-button>
            <el-button link type="warning" @click="openTeacherDialog(scope.row)">分配教师</el-button>
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="教研组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入教研组名称" :maxlength="30" />
        </el-form-item>
        <el-form-item label="所属学科" prop="subjectId">
          <el-select v-model="form.subjectId" filterable placeholder="请选择所属学科" style="width: 100%">
            <el-option v-for="item in subjectOptions" :key="item.subject_id || item.subjectId || item.id" :label="item.subject_name || item.subjectName || item.name || item.id" :value="item.subject_id || item.subjectId || item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="comments">
          <el-input v-model="form.comments" type="textarea" :rows="3" placeholder="请输入备注" :maxlength="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="y">启用</el-radio>
            <el-radio label="n">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="researcherDialogVisible" title="分配教研员" width="720px" @closed="handleResearcherDialogClosed">
      <el-form ref="researcherFormRef" :model="researcherForm" :rules="researcherRules" label-width="100px">
        <el-form-item label="教研组名称">
          <el-input :model-value="researcherForm.groupName" disabled />
        </el-form-item>
        <el-form-item label="所属学科">
          <el-input :model-value="researcherForm.subjectName" disabled />
        </el-form-item>
        <el-form-item label="教研员" prop="researcherUserIds">
          <el-select
            v-model="researcherForm.researcherUserIds"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择教研员"
            style="width: 100%"
          >
            <el-option
              v-for="item in researcherOptions"
              :key="item.userId"
              :label="item.userName || item.loginName || item.userId"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="researcherDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="researcherSubmitLoading" @click="handleResearcherSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="teacherDialogVisible" title="分配教师" width="720px" @closed="handleTeacherDialogClosed">
      <el-form ref="teacherFormRef" :model="teacherForm" :rules="teacherRules" label-width="100px">
        <el-form-item label="教研组名称">
          <el-input :model-value="teacherForm.groupName" disabled />
        </el-form-item>
        <el-form-item label="所属学科">
          <el-input :model-value="teacherForm.subjectName" disabled />
        </el-form-item>
        <el-form-item label="教师" prop="teacherUserIds">
          <el-select
            v-model="teacherForm.teacherUserIds"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择教师"
            style="width: 100%"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.userId"
              :label="`${getTeacherOrgName(item)} - ${getTeacherDisplayName(item)}`"
              :value="item.userId"
            >
              <div class="teacher-option-item">
                <span class="teacher-option-org">{{ getTeacherOrgName(item) }}</span>
                <span class="teacher-option-name">{{ getTeacherDisplayName(item) }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="已选教师">
          <el-input :model-value="`${teacherSelectedCount} 人`" disabled style="width: 160px" />
        </el-form-item>
        <el-form-item label="已选列表">
          <div class="selected-user-list">
            <el-tag
              v-for="item in selectedTeacherItems"
              :key="item.userId"
              class="selected-user-tag"
              effect="plain"
              type="warning"
            >
              {{ getTeacherOrgName(item) }} - {{ getTeacherDisplayName(item) }}
            </el-tag>
            <span v-if="!selectedTeacherItems.length" class="selected-user-empty">暂无已选教师</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teacherDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="teacherSubmitLoading" @click="handleTeacherSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchDataDictItems, fetchSubjectGroups, createSubjectGroup, updateSubjectGroup, deleteSubjectGroup, fetchSubjectGroupResearchers, saveSubjectGroupResearchers, fetchSubjectGroupTeachers, saveSubjectGroupTeachers, fetchTeacherAssignOptions } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增教研组')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const researcherFormRef = ref()
const teacherFormRef = ref()
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const queryForm = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const subjectOptions = ref([])
const subjectNameMap = computed(() => Object.fromEntries(subjectOptions.value.map(item => [item.subject_id || item.subjectId || item.id, item.subject_name || item.subjectName || item.name || item.id])))
const researcherDialogVisible = ref(false)
const researcherSubmitLoading = ref(false)
const researcherOptions = ref([])
const researcherForm = reactive({ groupId: '', groupName: '', subjectId: '', subjectName: '', researcherUserIds: [] })
const teacherDialogVisible = ref(false)
const teacherSubmitLoading = ref(false)
const teacherOptions = ref([])
const teacherForm = reactive({ groupId: '', groupName: '', subjectId: '', subjectName: '', teacherUserIds: [] })
const form = reactive({ groupName: '', subjectId: '', comments: '', status: 'y' })

const rules = {
  groupName: [{ required: true, message: '请输入教研组名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择所属学科', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const researcherRules = {
  researcherUserIds: [{ required: true, message: '请选择教研员', trigger: 'change' }]
}

const teacherRules = {
  teacherUserIds: [{ required: true, message: '请选择教师', trigger: 'change' }]
}

const getSubjectName = (row) => subjectNameMap.value[row?.subjectId] || row?.subjectName || row?.subjectId || '-'
const getResearcherCount = (row) => Number(row?.researcherCount || 0)
const selectedTeacherItems = computed(() => {
  const selectedIds = Array.isArray(teacherForm.teacherUserIds) ? teacherForm.teacherUserIds : []
  return teacherOptions.value.filter(item => selectedIds.includes(item.userId))
})
const teacherSelectedCount = computed(() => Array.isArray(teacherForm.teacherUserIds) ? teacherForm.teacherUserIds.length : 0)
const getTeacherDisplayName = (item) => item.userName || item.loginName || item.userId
const getTeacherOrgName = (item) => item.rootOrgName || item.rootOrgId || '-'

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

const loadResearchers = async () => {
  try {
    const res = await fetchUsers({ pageNum: 1, pageSize: 1000, status: 'y', userRoleId: 'Researcher' })
    const rows = res.data.code === 200 ? (res.data.data?.records || res.data.data?.list || res.data.data || []) : []
    researcherOptions.value = rows.filter(item => String(item.userRoleId || '').toLowerCase() === 'researcher' && String(item.status || '').toLowerCase() === 'y')
  } catch (error) {
    researcherOptions.value = []
  }
}

const loadTeachers = async (subjectId = teacherForm.subjectId) => {
  try {
    const res = await fetchTeacherAssignOptions({ subjectId })
    const rows = res.data.code === 200 ? (res.data.data || []) : []
    teacherOptions.value = Array.isArray(rows) ? rows : []
  } catch (error) {
    teacherOptions.value = []
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchSubjectGroups({ keyword: queryForm.keyword, pageNum: queryForm.pageNum, pageSize: queryForm.pageSize })
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

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = '新增教研组'
  form.groupName = ''
  form.subjectId = ''
  form.comments = ''
  form.status = 'y'
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.groupId
  dialogTitle.value = '编辑教研组'
  form.groupName = row.groupName || ''
  form.subjectId = row.subjectId || ''
  form.comments = row.comments || ''
  form.status = row.status || 'y'
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  form.groupName = ''
  form.subjectId = ''
  form.comments = ''
  form.status = 'y'
  formRef.value?.clearValidate?.()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = { groupName: form.groupName, subjectId: form.subjectId, comments: form.comments, status: form.status }
    const res = isEdit.value ? await updateSubjectGroup(editId.value, payload) : await createSubjectGroup(payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const openResearcherDialog = async (row) => {
  researcherForm.groupId = row.groupId
  researcherForm.groupName = row.groupName || ''
  researcherForm.subjectId = row.subjectId || ''
  researcherForm.subjectName = getSubjectName(row)
  researcherForm.researcherUserIds = []
  try {
    const res = await fetchSubjectGroupResearchers(row.groupId)
    if (res.data.code === 200) {
      const records = res.data.data?.records || res.data.data || []
      researcherForm.researcherUserIds = records.map(item => item.researcherUserId)
    }
  } catch (error) {
    researcherForm.researcherUserIds = []
  }
  researcherDialogVisible.value = true
}

const openTeacherDialog = async (row) => {
  teacherForm.groupId = row.groupId
  teacherForm.groupName = row.groupName || ''
  teacherForm.subjectId = row.subjectId || ''
  teacherForm.subjectName = getSubjectName(row)
  teacherForm.teacherUserIds = []
  try {
    const res = await fetchSubjectGroupTeachers(row.groupId)
    if (res.data.code === 200) {
      const records = res.data.data?.records || res.data.data || []
      teacherForm.teacherUserIds = records.map(item => item.teacherUserId)
    }
  } catch (error) {
    teacherForm.teacherUserIds = []
  }
  await loadTeachers(teacherForm.subjectId)
  teacherDialogVisible.value = true
}

const handleResearcherDialogClosed = () => {
  researcherForm.groupId = ''
  researcherForm.groupName = ''
  researcherForm.subjectId = ''
  researcherForm.subjectName = ''
  researcherForm.researcherUserIds = []
  researcherFormRef.value?.clearValidate?.()
}

const handleTeacherDialogClosed = () => {
  teacherForm.groupId = ''
  teacherForm.groupName = ''
  teacherForm.subjectId = ''
  teacherForm.subjectName = ''
  teacherForm.teacherUserIds = []
  teacherFormRef.value?.clearValidate?.()
}

watch(() => teacherForm.subjectId, async (newSubjectId, oldSubjectId) => {
  if (!teacherDialogVisible.value || newSubjectId === oldSubjectId) return
  teacherForm.teacherUserIds = []
  await loadTeachers(newSubjectId)
})

const handleResearcherSubmit = async () => {
  if (!researcherForm.groupId) return
  try {
    await researcherFormRef.value?.validate?.()
    researcherSubmitLoading.value = true
    const res = await saveSubjectGroupResearchers(researcherForm.groupId, { researcherUserIds: researcherForm.researcherUserIds })
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      researcherDialogVisible.value = false
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
  } finally {
    researcherSubmitLoading.value = false
  }
}

const handleTeacherSubmit = async () => {
  if (!teacherForm.groupId) return
  try {
    await teacherFormRef.value?.validate?.()
    teacherSubmitLoading.value = true
    const res = await saveSubjectGroupTeachers(teacherForm.groupId, { teacherUserIds: teacherForm.teacherUserIds })
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      teacherDialogVisible.value = false
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
  } finally {
    teacherSubmitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除教研组【${row.groupName}】吗？`, '提示', { type: 'warning' })
    const res = await deleteSubjectGroup(row.groupId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await loadSubjects()
  await loadItems()
  await loadResearchers()
  await loadTeachers()
})
</script>
