<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">我的标准实验</span>
          </div>
        </div>
      </template>

      <div class="standard-toolbar">
        <div class="standard-toolbar-inline">
          <el-input v-model="query.keyword" placeholder="搜索实验名称/事项" clearable class="standard-search" @keyup.enter="loadItems" />
          <el-select v-model="query.subjectId" placeholder="学科" clearable filterable class="standard-status-select">
            <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable class="standard-status-select">
            <el-option label="草稿" value="c" />
            <el-option label="待审核" value="t" />
            <el-option label="通过" value="y" />
            <el-option label="不通过" value="n" />
          </el-select>
          <el-button @click="loadItems">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="standard-toolbar-right">
          <el-button type="primary" @click="openCreateDialog">新增实验</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="expName" label="实验名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="chooseType" label="必做/选做" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ chooseTypeLabel(scope.row.chooseType) }}</template>
        </el-table-column>
        <el-table-column prop="subjectId" label="学科" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ getSubjectLabel(scope.row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="schoolLevelId" label="学段" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.schoolLevelName || getSchoolLevelLabel(scope.row.schoolLevelId) }}</template>
        </el-table-column>
        <el-table-column prop="gradeId" label="年级" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.gradeNames }}</template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.createUserName || scope.row.createUserId || '-' }}</template>
        </el-table-column>
        <el-table-column label="模拟器" min-width="180" align="center">
          <template #default="scope">
            <div class="simulator-cell">
              <el-button
                v-if="scope.row.simulatorPreviewUrl"
                link
                type="primary"
                @click="openSimulatorPreview(scope.row)"
              >
                预览
              </el-button>
              <span v-else>-</span>
              <el-button link type="primary" @click="openChangeSimulatorDialog(scope.row)">更改</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="light">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openViewDialog(scope.row)">查看</el-button>
            <el-button v-if="['c', 'n'].includes(String(scope.row.status || '').toLowerCase())" link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="['c', 'n'].includes(String(scope.row.status || '').toLowerCase())" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button link type="primary" @click="openLogDialog(scope.row)">日志</el-button>
          </template>
        </el-table-column>
      </el-table>

    <el-dialog v-model="viewDialogVisible" title="实验详情" width="1200px" class="user-dialog view-dialog" destroy-on-close @closed="closeViewDialog">
      <ExpStandardDetailView v-if="viewDialogVisible" :exp-id="viewExpId" :show-back-button="false" />
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ query.pageSize }} 条，共 {{ total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="共 {total} 条, sizes, prev, pager, next, jumper"
            :total="total"
            :current-page="query.pageNum"
            :page-size="query.pageSize"
            :page-sizes="pageSizes"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-card>
    <el-dialog v-model="simulatorDialogVisible" title="实验模拟器" width="980px" class="user-dialog" destroy-on-close @closed="closeSimulatorDialog">
      <div class="simulator-picker-toolbar">
        <el-input v-model="simulatorQuery.keyword" placeholder="搜索模拟器名称/说明" clearable class="simulator-picker-search" @keyup.enter="loadSimulatorItems" style="width:180px;"/>
        <el-button @click="loadSimulatorItems">查询</el-button>
        <el-button @click="resetSimulatorQuery">重置</el-button>
      </div>
      <el-form ref="simulatorFormRef" :model="simulatorForm" :rules="simulatorRules" label-width="90px" class="user-form">
          <el-table
            :data="simulatorOptions"
            border
            stripe
            v-loading="simulatorLoading"
            class="user-table simulator-picker-table"
            highlight-current-row
            @row-click="selectSimulatorRow"
          >
            <el-table-column width="54" align="center">
              <template #default="scope">
                <el-radio v-model="simulatorForm.simulatorId" :value="scope.row.value" @change="selectSimulatorByValue(scope.row.value)" />
              </template>
            </el-table-column>
            <el-table-column prop="label" label="模拟器名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="subjectName" label="学科" min-width="120" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.subjectName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="comments" label="说明" min-width="220" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.comments || '-' }}</template>
            </el-table-column>
            <el-table-column label="预览" width="100" align="center">
              <template #default="scope">
                <el-button v-if="scope.row.previewUrl" link type="primary" @click.stop="openSimulatorPreviewByUrl(scope.row.previewUrl)">预览</el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
      </el-form>
      <div class="pagination-footer simulator-picker-pagination">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ simulatorQuery.pageSize }} 条，共 {{ simulatorTotal }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="共 {total} 条, sizes, prev, pager, next, jumper"
            :total="simulatorTotal"
            :current-page="simulatorQuery.pageNum"
            :page-size="simulatorQuery.pageSize"
            :page-sizes="pageSizes"
            @current-change="handleSimulatorPageChange"
            @size-change="handleSimulatorSizeChange"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="simulatorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="simulatorSubmitting" @click="handleSaveSimulator">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="logDialogVisible" title="实验日志" width="980px" class="user-dialog">
      <el-table :data="logTableData" border stripe v-loading="logLoading" class="user-table">
        <el-table-column prop="logTime" label="时间" min-width="180">
          <template #default="scope">{{ formatLogTime(scope.row.logTime) }}</template>
        </el-table-column>
        <el-table-column prop="logTypeName" label="日志类型" min-width="140" />
        <el-table-column prop="logUserName" label="操作人" min-width="140" />
        <el-table-column prop="logComments" label="内容" min-width="240" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="logDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="980px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="user-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="实验名称" prop="expName"><el-input v-model="form.expName" placeholder="请输入实验名称" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="必做/选做" prop="chooseType"><el-select v-model="form.chooseType" placeholder="请选择" clearable style="width: 100%"><el-option label="必做" value="must" /><el-option label="选做" value="choose" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学科" prop="subjectId"><el-select v-model="form.subjectId" placeholder="请选择学科" clearable filterable style="width: 100%"><el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学段" prop="schoolLevelId"><el-select v-model="form.schoolLevelId" placeholder="请选择学段" clearable filterable style="width: 100%"><el-option v-for="item in schoolLevelOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年级" prop="gradeId"><el-select v-model="form.gradeId" placeholder="请选择年级" clearable filterable style="width: 100%"><el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="难度" prop="difficultyId"><el-select v-model="form.difficultyId" placeholder="请选择难度" clearable filterable style="width: 100%"><el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="实验原理" prop="expPrinciple"><el-input v-model="form.expPrinciple" type="textarea" :rows="4" placeholder="请输入实验原理" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="注意事项" prop="expCaution"><el-input v-model="form.expCaution" placeholder="请输入注意事项" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="危险提示" prop="expDanger"><el-input v-model="form.expDanger" placeholder="请输入危险提示" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="课时" prop="classHour"><el-input-number v-model="form.classHour" :min="0" :precision="2" :step="0.5" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择状态" clearable style="width: 100%"><el-option label="草稿" value="c" /><el-option label="待审核" value="t" /><el-option label="通过" value="y" /><el-option label="不通过" value="n" /></el-select></el-form-item></el-col>
        </el-row>
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
import { useRouter } from 'vue-router'
import ExpStandardDetailView from './ExpStandardDetailView.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteExpStandard, fetchDataDictItems, fetchExpStandardsMy, fetchLatestExpStandardDraft,fetchExpSimulators, updateExpTeachSimulator } from '../../api/index'
import { fetchExpLogs } from '../../api/exp'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增实验')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const total = ref(0)
const tableData = ref([])
const pageSizes = [10, 20, 30, 40, 50]
const subjectOptions = ref([])
const schoolLevelOptions = ref([])
const gradeOptions = ref([])
const difficultyOptions = ref([])
const logDialogVisible = ref(false)
const logLoading = ref(false)
const logTableData = ref([])
const viewDialogVisible = ref(false)
const viewExpId = ref('')

const simulatorDialogVisible = ref(false)
const simulatorSubmitting = ref(false)
const simulatorLoading = ref(false)
const simulatorFormRef = ref()
const simulatorOptions = ref([])
const simulatorTotal = ref(0)
const simulatorForm = reactive({ expId: '', simulatorId: '' })
const simulatorQuery = reactive({ keyword: '', status: 'y', pageNum: 1, pageSize: 10 })
const simulatorRules = {
  simulatorId: [{ required: true, message: '请选择模拟器', trigger: 'change' }]
}

const query = reactive({ keyword: '', subjectId: '', status: '', expType: 'standard', pageNum: 1, pageSize: 10 })
const form = reactive({ expName: '', chooseType: '', subjectId: '', schoolLevelId: '', gradeId: '', difficultyId: '', expPrinciple: '', expCaution: '', expDanger: '', classHour: null, status: 'c' })

const rules = {
  expName: [{ required: true, message: '请输入实验名称', trigger: 'blur' }],
  chooseType: [{ required: true, message: '请选择必做/选做', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  schoolLevelId: [{ required: true, message: '请选择学段', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const statusLabel = (value) => ({ c: '草稿', t: '待审核', y: '通过', n: '不通过' }[value] || value || '-')
const statusTagType = (value) => ({ c: 'info', t: 'warning', y: 'success', n: 'danger' }[value] || 'info')
const chooseTypeLabel = (value) => ({ must: '必做', choose: '选做' }[value] || value || '-')
const getSubjectLabel = (value) => subjectOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getSchoolLevelLabel = (value) => schoolLevelOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getGradeLabel = (value) => gradeOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getDifficultyLabel = (value) => difficultyOptions.value.find(item => item.value === String(value))?.label || value || '-'

const normalizeOptions = (rows, valueKey, labelKey) => (rows || [])
  .filter(item => item?.[valueKey] != null && item?.[labelKey] != null)
  .map(item => ({ value: String(item[valueKey]), label: item[labelKey] }))

const normalizeHtml = (value) => {
  const text = String(value || '').trim()
  if (!text) return '<span class="empty-rich">-</span>'
  if (/<[a-z][\s\S]*>/i.test(text)) return text
  return text.replace(/\r?\n/g, '<br>')
}

const formatLogTime = (value) => {
  if (!value) return '-'
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const loadDicts = async () => {
  const [subjectRes, levelRes, gradeRes, difficultyRes] = await Promise.all([
    fetchDataDictItems('data_school_subject'),
    fetchDataDictItems('data_school_level'),
    fetchDataDictItems('data_school_grade'),
    fetchDataDictItems('data_exp_difficulty')
  ])
  if (subjectRes.data.code === 200) subjectOptions.value = normalizeOptions(subjectRes.data.data || [], 'subject_id', 'subject_name')
  if (levelRes.data.code === 200) schoolLevelOptions.value = normalizeOptions(levelRes.data.data || [], 'level_id', 'level_name')
  if (gradeRes.data.code === 200) gradeOptions.value = normalizeOptions(gradeRes.data.data || [], 'grade_id', 'grade_name')
  if (difficultyRes.data.code === 200) difficultyOptions.value = normalizeOptions(difficultyRes.data.data || [], 'difficulty_id', 'difficulty_name')
}

const resetForm = () => {
  Object.assign(form, { expName: '', chooseType: '', subjectId: '', schoolLevelId: '', gradeId: '', difficultyId: '', expPrinciple: '', expCaution: '', expDanger: '', classHour: null, status: 'c' })
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchExpStandardsMy({ ...query, expType: 'standard' })
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
  query.subjectId = ''
  query.status = ''
  query.pageNum = 1
  query.pageSize = 10
  loadItems()
}

const handlePageChange = (page) => {
  query.pageNum = page
  loadItems()
}

const handleSizeChange = (size) => {
  query.pageSize = size
  query.pageNum = 1
  loadItems()
}

const openCreateDialog = async () => {
  try {
    const res = await fetchLatestExpStandardDraft()
    const draft = res.data.data
    if (res.data.code === 200 && draft && draft.expId) {
      router.push({ path: '/admin/exp/exp-standard/create', query: { expId: draft.expId } })
      return
    }
  } catch (error) {
    // ignore and create new draft
  }
  router.push('/admin/exp/exp-standard/create')
}

const openViewDialog = (row) => {
  const expId = String(row?.expId || '').trim()
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

const openEditDialog = (row) => {
  const expId = String(row?.expId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  router.push({ path: '/admin/exp/exp-standard/create', query: { expId } })
}

const openLogDialog = async (row) => {
  const expId = String(row?.expId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  logDialogVisible.value = true
  logLoading.value = true
  logTableData.value = []
  try {
    const res = await fetchExpLogs(expId)
    if (res.data.code === 200) {
      const rows = Array.isArray(res.data.data) ? res.data.data : (res.data.data?.records || res.data.data?.list || [])
      logTableData.value = rows.sort((a, b) => new Date(b.logTime || 0) - new Date(a.logTime || 0))
    } else {
      ElMessage.error(res.data.message || '加载日志失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载日志失败')
  } finally {
    logLoading.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form, status: 'y', expType: 'standard' }
      const res = isEdit.value ? await updateExpStandard(editId.value, payload) : await createExpStandard(payload)
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
  await ElMessageBox.confirm(`确定删除实验【${row.expName}】吗？`, '提示', { type: 'warning' })
  try {
    const res = await deleteExpStandard(row.expId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadItems()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const openSimulatorPreview = (row) => {
  const url = String(row?.simulatorPreviewUrl || '').trim()
  window.open(url, '_blank', 'noopener,noreferrer')
}

const openSimulatorPreviewByUrl = (url) => {
  const v = String(url || '').trim()
  if (!v) return
  window.open(v, '_blank', 'noopener,noreferrer')
}

const loadSimulatorItems = async () => {
  simulatorLoading.value = true
  try {
    const res = await fetchExpSimulators({
      ...simulatorQuery,
      subjectId: simulatorForm.subjectId,
      pageNum: simulatorQuery.pageNum,
      pageSize: simulatorQuery.pageSize
    })
    if (res.data.code === 200) {
      const records = res.data.data?.records || []
      simulatorOptions.value = records.map(item => ({
        value: String(item.simulatorId),
        label: item.simulatorName,
        subjectName: item.subjectName,
        comments: item.comments,
        previewUrl: item.simulatorPreviewUrl || item.simulatorUrl
      }))
      simulatorTotal.value = res.data.data?.total || 0
    } else {
      ElMessage.error(res.data.message || '加载模拟器失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载模拟器失败')
  } finally {
    simulatorLoading.value = false
  }
}

const resetSimulatorQuery = () => {
  simulatorQuery.keyword = ''
  simulatorQuery.status = 'y'
  simulatorQuery.pageNum = 1
  simulatorQuery.pageSize = 10
  loadSimulatorItems()
}

const handleSimulatorPageChange = (page) => {
  simulatorQuery.pageNum = page
  loadSimulatorItems()
}

const handleSimulatorSizeChange = (size) => {
  simulatorQuery.pageSize = size
  simulatorQuery.pageNum = 1
  loadSimulatorItems()
}

const selectSimulatorRow = (row) => {
  simulatorForm.simulatorId = row.value
}

const selectSimulatorByValue = (value) => {
  simulatorForm.simulatorId = value
}

const openChangeSimulatorDialog = async (row) => {
  const expId = String(row?.expId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  simulatorForm.expId = expId
  simulatorForm.subjectId = String(row?.subjectId || '').trim()
  simulatorForm.simulatorId = String(row?.simulatorId || '')
  simulatorDialogVisible.value = true
  simulatorQuery.pageNum = 1
  await loadSimulatorItems()
}

const closeSimulatorDialog = () => {
  simulatorDialogVisible.value = false
  simulatorForm.expId = ''
  simulatorForm.simulatorId = ''
}

const handleSaveSimulator = async () => {
  await simulatorFormRef.value?.validate(async (valid) => {
    if (!valid) return
    simulatorSubmitting.value = true
    try {
      const res = await updateExpTeachSimulator(simulatorForm.expId, simulatorForm.simulatorId)
      if (res.data.code === 200) {
        ElMessage.success('更改成功')
        simulatorDialogVisible.value = false
        loadItems()
      } else {
        ElMessage.error(res.data.message || '更改失败')
      }
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '更改失败')
    } finally {
      simulatorSubmitting.value = false
    }
  })
}

onMounted(async () => {
  await loadDicts()
  await loadItems()
})
</script>

<style scoped>
.standard-toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:nowrap;margin-bottom:12px;overflow-x:auto}.standard-toolbar-inline{display:flex;align-items:center;gap:8px;flex-wrap:nowrap;min-width:max-content}.standard-toolbar-right{display:flex;align-items:center;flex-shrink:0;margin-left:auto}.standard-search{width:240px;flex-shrink:0}.standard-status-select{width:160px;flex-shrink:0}@media (max-width:768px){.standard-search,.standard-status-select{width:auto}.standard-toolbar,.standard-toolbar-inline{width:100%}}
</style>
