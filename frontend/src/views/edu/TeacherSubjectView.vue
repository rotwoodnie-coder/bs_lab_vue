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
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'">{{ scope.row.status === 'y' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subjectNames" label="授课学科" min-width="180" show-overflow-tooltip>
          <template #default="scope">{{ getSubjectNames(scope.row).join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column prop="classNames" label="授课班级" min-width="250" show-overflow-tooltip>
          <template #default="scope">{{ getClassNames(scope.row).join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openSubjectDialog(scope.row)">分配学科</el-button>
            <el-button link type="primary" @click="openClassDialog(scope.row)">授课班级设置</el-button>
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

    <el-dialog v-model="subjectDialogVisible" title="分配授课学科" width="520px" @closed="handleSubjectDialogClosed">
      <el-form ref="subjectFormRef" :model="subjectForm" :rules="subjectRules" label-width="100px">
        <el-form-item label="教师姓名">
          <el-input v-model="subjectForm.userName" disabled />
        </el-form-item>
        <el-form-item label="授课学科" prop="subjectIds">
          <el-checkbox-group v-model="subjectForm.subjectIds" class="teacher-subject-checkbox-group">
            <el-checkbox v-for="item in subjectOptions" :key="item.subject_id || item.subjectId || item.id" :label="item.subject_id || item.subjectId || item.id">
              {{ item.subject_name || item.subjectName || item.name || item.id }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subjectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubjectSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="classDialogVisible" title="授课班级设置" width="640px" @closed="handleClassDialogClosed">
      <el-form ref="classFormRef" :model="classForm" label-width="100px">
        <el-form-item label="教师姓名">
          <el-input v-model="classForm.userName" disabled />
        </el-form-item>
        <el-form-item label="授课班级">
          <el-tree
            ref="orgTreeRef"
            v-loading="orgTreeLoading"
            class="class-org-tree"
            :data="orgTreeData"
            node-key="orgId"
            show-checkbox
            default-expand-all
            :props="treeProps"
            :check-strictly="false"
            :expand-on-click-node="false"
            :default-checked-keys="classForm.classIds"
            @check-change="handleTreeCheckChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="classSubmitLoading" @click="handleClassSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDataDictItems, fetchOrgTree, fetchTeacherSubjects, saveTeacherClasses, saveTeacherSubject } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const classSubmitLoading = ref(false)
const subjectDialogVisible = ref(false)
const classDialogVisible = ref(false)
const subjectFormRef = ref()
const classFormRef = ref()
const orgTreeRef = ref()
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const queryForm = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const subjectOptions = ref([])
const orgTreeData = ref([])
const orgTreeLoading = ref(false)
const currentTeacherId = ref('')
const currentRootOrgId = ref(JSON.parse(localStorage.getItem('userInfo') || '{}').rootOrgId || '')
const subjectForm = reactive({ userName: '', subjectIds: [] })
const classForm = reactive({ userName: '', classIds: [] })
const subjectNameMap = computed(() => Object.fromEntries(subjectOptions.value.map(item => [item.subject_id || item.subjectId || item.id, item.subject_name || item.subjectName || item.name || item.id])))
const classNameMap = ref({})
const treeProps = { children: 'children', label: 'orgName', disabled: 'disabled' }

const subjectRules = { subjectIds: [{ required: true, message: '请选择学科', trigger: 'change' }] }

const getSubjectNames = (row) => (Array.isArray(row?.subjectIds) ? row.subjectIds : []).map(id => subjectNameMap.value[id] || id).filter(Boolean)
const getClassNames = (row) => (Array.isArray(row?.classIds) ? row.classIds : []).map(id => classNameMap.value[id] || id).filter(Boolean)

const normalizeTree = (nodes = []) => nodes.map((node) => {
  const children = normalizeTree(node.children || [])
  const isLeaf = !children.length
  classNameMap.value[node.orgId] = node.orgName || node.orgId
  return { ...node, disabled: !isLeaf, children }
})

const loadSubjects = async () => {
  try {
    const res = await fetchDataDictItems('data_school_subject')
    if (res.data.code === 200) subjectOptions.value = (Array.isArray(res.data.data) ? res.data.data : []).filter(item => String(item.status || '').toLowerCase() === 'y')
  } catch (error) { ElMessage.error(error?.response?.data?.message || '加载学科失败') }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherSubjects({ keyword: queryForm.keyword, pageNum: queryForm.pageNum, pageSize: queryForm.pageSize })
    if (res.data.code === 200) { const data = res.data.data || {}; tableData.value = Array.isArray(data) ? data : (data.records || data.list || []); total.value = data.total || tableData.value.length || 0 } else { ElMessage.error(res.data.message || '加载失败') }
  } catch (error) { ElMessage.error(error?.response?.data?.message || '加载失败') } finally { loading.value = false }
}

const loadOrgTree = async () => {
  orgTreeLoading.value = true
  try {
    const res = await fetchOrgTree()
    if (res.data.code === 200) orgTreeData.value = normalizeTree((Array.isArray(res.data.data) ? res.data.data : []))
  } catch (error) { ElMessage.error(error?.response?.data?.message || '加载组织树失败') } finally { orgTreeLoading.value = false }
}

const resetQuery = () => { queryForm.keyword = ''; queryForm.pageNum = 1; queryForm.pageSize = 10; loadItems() }
const handlePageChange = (page) => { queryForm.pageNum = page; loadItems() }
const handleSizeChange = (size) => { queryForm.pageSize = size; queryForm.pageNum = 1; loadItems() }

const openSubjectDialog = (row) => { currentTeacherId.value = row.userId; subjectForm.userName = row.userName || row.loginName || ''; subjectForm.subjectIds = Array.isArray(row.subjectIds) ? row.subjectIds : (row.subjectIds ? [row.subjectIds] : []); subjectDialogVisible.value = true }
const applyCheckedClassIds = async () => {
  await nextTick()
  orgTreeRef.value?.setCheckedKeys?.(classForm.classIds)
}

const openClassDialog = async (row) => {
  currentTeacherId.value = row.userId
  classForm.userName = row.userName || row.loginName || ''
  classForm.classIds = Array.isArray(row.classIds) ? row.classIds : (row.classIds ? [row.classIds] : [])
  if (!orgTreeData.value.length) await loadOrgTree()
  classDialogVisible.value = true
  await applyCheckedClassIds()
}

const handleSubjectDialogClosed = () => { subjectForm.userName = ''; subjectForm.subjectIds = []; subjectFormRef.value?.clearValidate?.() }
const handleClassDialogClosed = () => { classForm.userName = ''; classForm.classIds = []; orgTreeRef.value?.setCheckedKeys?.([]) }

const handleSubjectSubmit = async () => {
  if (!subjectFormRef.value || !currentTeacherId.value) return
  try { await subjectFormRef.value.validate(); submitLoading.value = true; const res = await saveTeacherSubject(currentTeacherId.value, { subjectIds: subjectForm.subjectIds }); if (res.data.code === 200) { ElMessage.success('保存成功'); subjectDialogVisible.value = false; await loadItems() } else { ElMessage.error(res.data.message || '保存失败') } } catch (error) { ElMessage.error(error?.response?.data?.message || error?.message || '保存失败') } finally { submitLoading.value = false }
}

const getCheckedLeafIds = () => {
  const checked = orgTreeRef.value?.getCheckedNodes?.(false, true) || []
  return checked.filter(node => !(node.children && node.children.length)).map(node => node.orgId)
}
const handleTreeCheckChange = () => { classForm.classIds = getCheckedLeafIds() }
const handleClassSubmit = async () => {
  if (!currentTeacherId.value) return
  const classIds = getCheckedLeafIds()
  if (!classIds.length) return ElMessage.warning('请选择班级')
  try { classSubmitLoading.value = true; const res = await saveTeacherClasses(currentTeacherId.value, { teacherId: currentTeacherId.value, classIds, rootOrgId: currentRootOrgId.value }) ; if (res.data.code === 200) { ElMessage.success('保存成功'); classDialogVisible.value = false; await loadItems() } else { ElMessage.error(res.data.message || '保存失败') } } catch (error) { ElMessage.error(error?.response?.data?.message || error?.message || '保存失败') } finally { classSubmitLoading.value = false }
}

watch(classDialogVisible, (visible) => { if (visible) applyCheckedClassIds() })

onMounted(async () => { await loadSubjects(); await loadItems() })
</script>
