<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">教学管理 > 教材管理</span>
          </div>
        </div>
      </template>
      <div class="user-toolbar user-toolbar--inline">
        <div class="dict-toolbar-actions dict-toolbar-actions--inline">
          <el-input v-model="query.keyword" clearable placeholder="搜索教材名称" style="width: 220px" @clear="handleSearch" @keyup.enter="handleSearch" />
          <el-select v-model="query.editionId" clearable filterable placeholder="教材版本" style="width: 160px" @change="handleSearch">
            <el-option v-for="item in editionOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="query.subjectId" clearable filterable placeholder="学科" style="width: 160px" @change="handleSearch">
            <el-option v-for="item in subjectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-select v-model="query.gradeId" clearable filterable placeholder="年级" style="width: 160px" @change="handleSearch">
            <el-option v-for="item in gradeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <div class="dict-toolbar-buttons dict-toolbar-buttons--inline">
            <el-button @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="primary" @click="openCreateDialog">新增教材</el-button>
          </div>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="coursebookName" label="教材名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="教材版本" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ getOptionLabel(editionOptions, scope.row, ['editionId', 'edition_id']) }}</template>
        </el-table-column>
        <el-table-column label="学科" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ getOptionLabel(subjectOptions, scope.row, ['subjectId', 'subject_id']) }}</template>
        </el-table-column>
        <el-table-column label="学段" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ getOptionLabel(levelOptions, scope.row, ['levelId', 'level_id']) }}</template>
        </el-table-column>
        <el-table-column label="年级" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ getOptionLabel(gradeOptions, scope.row, ['gradeId', 'grade_id']) }}</template>
        </el-table-column>
        <el-table-column label="学期" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ getOptionLabel(semesterOptions, scope.row, ['semesterId', 'semester_id']) }}</template>
        </el-table-column>
        <el-table-column label="电子文件" min-width="100" show-overflow-tooltip>
          <template #default="scope">
            <a v-if="scope.row.previewUrl || scope.row.fileUrl" :href="resolveFileUrl(scope.row.previewUrl || scope.row.fileUrl)" target="_blank" rel="noopener noreferrer">
              文件
            </a>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="y"
              inactive-value="n"
              active-text="启用"
              inactive-text="停用"
              inline-prompt
              :loading="statusLoadingId === getCoursebookId(scope.row)"
              @change="(value) => handleStatusChange(scope.row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="success" @click="openContentDialog(scope.row)">目录维护</el-button>
            <!--
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            -->
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">
            当前每页 {{ pagination.pageSize }} 条，共 {{ total }} 条数据
          </div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            :current-page="pagination.pageNum"
            :page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="教材名称" prop="coursebookName">
          <el-input v-model="form.coursebookName" placeholder="请输入教材名称" :maxlength="30" />
        </el-form-item>
        <el-form-item label="教材版本" prop="editionId">
          <el-select v-model="form.editionId" filterable placeholder="请选择教材版本" style="width: 100%">
            <el-option v-for="item in editionOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学科" prop="subjectId">
          <el-select v-model="form.subjectId" filterable placeholder="请选择学科" style="width: 100%">
            <el-option v-for="item in subjectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学段" prop="levelId">
          <el-select v-model="form.levelId" filterable placeholder="请选择学段" style="width: 100%">
            <el-option v-for="item in levelOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="gradeId">
          <el-select v-model="form.gradeId" filterable placeholder="请选择年级" style="width: 100%">
            <el-option v-for="item in gradeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" filterable placeholder="请选择学期" style="width: 100%">
            <el-option v-for="item in semesterOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" prop="comments">
          <el-input v-model="form.comments" type="textarea" :rows="3" placeholder="请输入说明" :maxlength="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="y">启用</el-radio>
            <el-radio value="n">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电子文件" prop="fileUrl">
          <div class="coursebook-file-upload-wrap">
            <MinioUploader
              v-model="form.fileUrl"
              v-model:file-name="uploadedFileName"
              :preview-url="resolveFileUrl(uploadedPreviewUrl || form.fileUrl)"
              button-text="上传文件"
              @uploaded="handleFileUploaded"
              @delete-file="handleDeleteAttachment"
            />
            <div v-if="resolveFileUrl(uploadedPreviewUrl || form.fileUrl)" class="coursebook-file-preview-link">
              <span class="coursebook-file-preview-label">文件预览</span>
              <a :href="resolveFileUrl(uploadedPreviewUrl || form.fileUrl)" target="_blank" rel="noopener noreferrer">打开文件</a>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contentDialogVisible" title="目录维护" width="920px" @closed="handleContentDialogClosed">
      <div class="content-toolbar">
        <div class="content-toolbar__title">{{ currentCoursebookName }}</div>
        <el-button type="primary" @click="openContentNodeDialog(null, 'unit')">新增单元</el-button>
      </div>
      <el-tree
        ref="contentTreeRef"
        :data="contentTreeData"
        node-key="content_id"
        :default-expand-all="true"
        :props="contentTreeProps"
        :expand-on-click-node="false"
        draggable
        :allow-drag="allowDrag"
        :allow-drop="allowDrop"
        @node-drop="handleNodeDrop"
        class="content-tree"
      >
        <template #default="{ data }">
          <div class="content-tree-node">
            <span class="content-tree-node__name">{{ data.content_name }}</span>
            <span class="content-tree-node__type">{{ contentTypeText(data.content_type) }}</span>
            <div class="content-tree-node__actions">
              <el-button v-if="canAddChild(data, 'chapter')" link type="primary" @click="openContentNodeDialog(null, 'chapter', data)">新增章</el-button>
              <el-button v-if="canAddChild(data, 'section')" link type="primary" @click="openContentNodeDialog(null, 'section', data)">新增节</el-button>
              <el-button link type="primary" @click="openContentNodeDialog(data, data.content_type, null)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteContent(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-dialog>

    <el-dialog v-model="contentNodeDialogVisible" :title="contentNodeDialogTitle" width="520px" @closed="handleContentNodeDialogClosed">
      <el-form ref="contentFormRef" :model="contentForm" :rules="contentRules" label-width="100px">
        <el-form-item label="目录名称" prop="content_name">
          <el-input v-model="contentForm.content_name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="目录类型" prop="content_type">
          <el-input :model-value="contentTypeText(contentForm.content_type)" disabled />
        </el-form-item>
        <el-form-item label="排序" prop="sort_order">
          <el-input-number v-model="contentForm.sort_order" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="说明" prop="comments">
          <el-input v-model="contentForm.comments" type="textarea" :rows="3" placeholder="请输入说明" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="contentForm.status">
            <el-radio value="y">启用</el-radio>
            <el-radio value="n">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contentNodeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="contentSubmitLoading" @click="handleContentNodeSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import MinioUploader from '../../components/MinioUploader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCoursebook, createCoursebookContent, deleteCoursebook, deleteCoursebookContent, deleteFileByUrl, fetchCoursebooks, fetchCoursebookContents, fetchDataDictItems, reorderCoursebookContents, updateCoursebook, updateCoursebookContent } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增教材')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const uploadedFileName = ref('')
const uploadedPreviewUrl = ref('')
const tableData = ref([])
const statusLoadingId = ref('')
const editionOptions = ref([])
const contentTreeRef = ref()
const subjectOptions = ref([])
const levelOptions = ref([])
const gradeOptions = ref([])
const semesterOptions = ref([])
const contentDialogVisible = ref(false)
const contentNodeDialogVisible = ref(false)
const contentSubmitLoading = ref(false)
const contentTreeData = ref([])
const contentFormRef = ref()
const currentCoursebookId = ref('')
const currentCoursebookName = ref('')
const contentNodeParentId = ref('0')
const contentNodeEditingId = ref('')
const contentNodeDialogTitle =  ref('')

const query = reactive({ keyword: '', editionId: '', subjectId: '', gradeId: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const form = reactive({
  id: '',
  coursebookName: '',
  editionId: '',
  subjectId: '',
  levelId: '',
  gradeId: '',
  semesterId: '',
  fileUrl: '',
  comments: '',
  status: 'y'
})
const contentForm = reactive({
  content_id: '',
  content_name: '',
  content_type: 'unit',
  parent_id: '0',
  coursebook_id: '',
  comments: '',
  status: 'y',
  sort_order: 0
})

const rules = {
  coursebookName: [{ required: true, message: '请输入教材名称', trigger: 'blur' }],
  editionId: [{ required: true, message: '请选择教材版本', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  levelId: [{ required: true, message: '请选择学段', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const contentRules = {
  content_name: [{ required: true, message: '请输入目录名称', trigger: 'blur' }],
  content_type: [{ required: true, message: '请选择目录类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const resetForm = () => {
  form.id = ''
  form.coursebookName = ''
  form.editionId = ''
  form.subjectId = ''
  form.levelId = ''
  form.gradeId = ''
  form.semesterId = ''
  form.fileUrl = ''
  form.comments = ''
  form.status = 'y'
  uploadedFileName.value = ''
  uploadedPreviewUrl.value = ''
}

const resetContentForm = () => {
  contentForm.content_id = ''
  contentForm.content_name = ''
  contentForm.content_type = 'unit'
  contentForm.parent_id = '0'
  contentForm.coursebook_id = currentCoursebookId.value
  contentForm.comments = ''
  contentForm.status = 'y'
  contentForm.sort_order = 0
}

const mapOptions = (rows, idKeys, nameKeys) => rows.map((row) => ({
  id: firstValue(row, idKeys),
  name: firstValue(row, nameKeys)
})).filter((item) => item.id)

const getOptionLabel = (options, rowOrId, keys = []) => {
  const list = options;
  //Array.isArray(options?.value) ? options.value : []
  const rawId = typeof rowOrId === 'object' && rowOrId !== null ? firstValue(rowOrId, keys) : rowOrId
  const item = list.find((option) => String(option.id) === String(rawId))
  return item ? item.name : rawId || ''
}

const getCoursebookId = (row) => row.coursebookId || row.coursebook_id || row.id || ''

function firstValue(row, keys) {
  for (const key of keys) {
    const value = row?.[key]
    if (value !== undefined && value !== null && String(value).trim() !== '') return value
  }
  return ''
}

const loadOptions = async () => {
  const optionTypes = [
    ['data_textbook_edition', editionOptions],
    ['data_school_subject', subjectOptions],
    ['data_school_level', levelOptions],
    ['data_school_grade', gradeOptions],
    ['data_school_semester', semesterOptions]
  ]

  await Promise.all(optionTypes.map(async ([type, target]) => {
    try {
      const res = await fetchDataDictItems(type)
      const rows = Array.isArray(res.data.data) ? res.data.data : []
      const configMap = {
        data_textbook_edition: ['edition_id', 'editionId'],
        data_school_subject: ['subject_id', 'subjectId'],
        data_school_level: ['level_id', 'levelId'],
        data_school_grade: ['grade_id', 'gradeId'],
        data_school_semester: ['semester_id', 'semesterId']
      }
      const nameMap = {
        data_textbook_edition: ['edition_name', 'editionName'],
        data_school_subject: ['subject_name', 'subjectName'],
        data_school_level: ['level_name', 'levelName'],
        data_school_grade: ['grade_name', 'gradeName'],
        data_school_semester: ['semester_name', 'semesterName']
      }
      target.value = mapOptions(rows, configMap[type], nameMap[type])
    } catch (error) {
      target.value = []
    }
  }))
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchCoursebooks({ pageNum: pagination.pageNum, pageSize: pagination.pageSize, keyword: query.keyword, editionId: query.editionId, subjectId: query.subjectId, gradeId: query.gradeId, paged: true })
    if (res.data.code === 200) {
      const data = res.data.data
      if (Array.isArray(data)) {
        tableData.value = data
        pagination.total = data.length
      } else {
        tableData.value = data?.records || data?.list || data?.rows || []
        pagination.total = data?.total || 0
      }
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadItems()
}

const resetSearch = () => {
  query.keyword = ''
  query.editionId = ''
  query.subjectId = ''
  query.gradeId = ''
  pagination.pageNum = 1
  loadItems()
}

const handleCurrentChange = (pageNum) => {
  pagination.pageNum = pageNum
  loadItems()
}

const handleSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.pageNum = 1
  loadItems()
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = '新增教材'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.coursebookId || row.coursebook_id || row.id || ''
  dialogTitle.value = '编辑教材'
  resetForm()
  form.coursebookName = row.coursebookName || row.coursebook_name || ''
  form.editionId = row.editionId || row.edition_id || ''
  form.subjectId = row.subjectId || row.subject_id || ''
  form.levelId = row.levelId || row.level_id || ''
  form.gradeId = row.gradeId || row.grade_id || ''
  form.semesterId = row.semesterId || row.semester_id || ''
  form.fileUrl = row.fileUrl || row.file_url || ''
  uploadedPreviewUrl.value = row.previewUrl || row.preview_url || row.filePreviewUrl || row.file_preview_url || form.fileUrl || ''
  uploadedFileName.value = resolveFileName(form.fileUrl)
  form.comments = row.comments || ''
  form.status = row.status || 'y'
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  resetForm()
  formRef.value?.clearValidate?.()
}

const closeDialog = () => {
  dialogVisible.value = false
}

const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
const resolveFileUrl = (value) => {
  if (!value) return ''
  const raw = String(value).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
}

const resolveFileName = (value) => {
  if (!value) return '-'
  const name = String(value).split('/').pop() || value
  return decodeURIComponent(name)
}

const clearFile = () => {
  form.fileUrl = ''
  uploadedFileName.value = ''
  uploadedPreviewUrl.value = ''
}

const handleDeleteAttachment = async () => {
  if (!form.fileUrl) return
  try {
    await deleteFileByUrl(form.fileUrl)
    clearFile()
    ElMessage.success('附件已删除')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '附件删除失败')
  }
}

const handleFileUploaded = ({ previewUrl }) => {
  uploadedPreviewUrl.value = previewUrl || ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = {
      coursebookName: form.coursebookName,
      editionId: form.editionId,
      subjectId: form.subjectId,
      levelId: form.levelId,
      gradeId: form.gradeId,
      semesterId: form.semesterId,
      fileUrl: form.fileUrl,
      previewUrl: uploadedPreviewUrl.value || form.fileUrl,
      comments: form.comments,
      status: form.status
    }
    const res = isEdit.value ? await updateCoursebook(editId.value, payload) : await createCoursebook(payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      closeDialog()
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = async (row, value) => {
  const id = getCoursebookId(row)
  const previousStatus = value === 'y' ? 'n' : 'y'
  if (!id) {
    row.status = previousStatus
    ElMessage.error('未获取到教材ID')
    return
  }

  try {
    statusLoadingId.value = id
    const payload = {
      coursebookName: row.coursebookName || row.coursebook_name || '',
      editionId: row.editionId || row.edition_id || '',
      subjectId: row.subjectId || row.subject_id || '',
      levelId: row.levelId || row.level_id || '',
      gradeId: row.gradeId || row.grade_id || '',
      semesterId: row.semesterId || row.semester_id || '',
      fileUrl: row.fileUrl || row.file_url || '',
      previewUrl: row.previewUrl || row.preview_url || row.filePreviewUrl || row.file_preview_url || row.fileUrl || row.file_url || '',
      comments: row.comments || '',
      status: value
    }
    const res = await updateCoursebook(id, payload)
    if (res.data.code === 200) {
      ElMessage.success(value === 'y' ? '启用成功' : '停用成功')
      await loadItems()
    } else {
      row.status = previousStatus
      ElMessage.error(res.data.message || '状态更新失败')
    }
  } catch (error) {
    row.status = previousStatus
    ElMessage.error(error?.response?.data?.message || '状态更新失败')
  } finally {
    statusLoadingId.value = ''
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除教材【${row.coursebookName || ''}】吗？`, '提示', { type: 'warning' })
    const id = getCoursebookId(row)
    const res = await deleteCoursebook(id)
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

const contentTreeProps = { children: 'children', label: 'content_name' }
const contentTypeText = (type) => ({ unit: '单元', chapter: '章', section: '节' }[type] || type || '-')
const canAddChild = (node, childType) => (node?.content_type === 'unit' && childType === 'chapter') || (node?.content_type === 'chapter' && childType === 'section')
const allowDrag = (dragNode) => !!dragNode?.data && dragNode.data.content_type !== 'section'
const allowDrop = (dragNode, dropNode, type) => {
  const dragType = dragNode?.data?.content_type
  const dropType = dropNode?.data?.content_type
  if (!dragType || !dropType) return false
  if (type === 'prev' || type === 'next') return dragType === dropType
  if (type === 'inner') {
    if (dragType === 'unit') return dropNode.data.level === 0 || dropType === 'unit'
    if (dragType === 'chapter') return dropType === 'unit'
    if (dragType === 'section') return dropType === 'chapter'
  }
  return false
}

const normalizeContentNodes = (rows) => {
  const map = new Map()
  rows.forEach((row) => {
    const id = row.content_id || row.contentId || row.id
    if (!id) return
    const level = row.content_type === 'unit' ? 0 : row.content_type === 'chapter' ? 1 : 2
    map.set(String(id), {
      ...row,
      content_id: id,
      content_name: row.content_name || row.contentName || '',
      content_type: row.content_type || row.contentType || 'unit',
      parent_id: row.parent_id || row.parentId || '0',
      coursebook_id: row.coursebook_id || row.coursebookId || currentCoursebookId.value,
      comments: row.comments || '',
      status: row.status || 'y',
      sort_order: row.sort_order ?? row.sortOrder ?? 0,
      level,
      children: []
    })
  })
  const roots = []
  map.forEach((node) => {
    const parentId = String(node.parent_id || '0')
    const parent = map.get(parentId)
    if (parent && parent !== node) parent.children.push(node)
    else roots.push(node)
  })
  const sortTree = (nodes) => nodes.sort((a, b) => Number(a.sort_order || 0) - Number(b.sort_order || 0)).forEach((node) => sortTree(node.children || []))
  sortTree(roots)
  return roots
}

const loadContentTree = async () => {
  if (!currentCoursebookId.value) return
  try {
    const res = await fetchCoursebookContents({ coursebookId: currentCoursebookId.value })
    const rows = Array.isArray(res.data.data) ? res.data.data : res.data.data?.records || res.data.data?.list || []
    contentTreeData.value = normalizeContentNodes(rows)
  } catch (error) {
    contentTreeData.value = []
    ElMessage.error(error?.response?.data?.message || '目录加载失败')
  }
}

const syncContentOrder = async () => {
  if (!currentCoursebookId.value) return

  const flattenTree = (nodes, parentId = '0', startOrder = 0, result = []) => {
    nodes.forEach((node, index) => {
      const sortOrder = startOrder + index
      result.push({
        content_id: node.content_id,
        parent_id: parentId,
        sort_order: sortOrder
      })
      if (Array.isArray(node.children) && node.children.length > 0) {
        flattenTree(node.children, String(node.content_id), 0, result)
      }
    })
    return result
  }

  return reorderCoursebookContents(currentCoursebookId.value, flattenTree(contentTreeData.value))
}

const openContentDialog = async (row) => {
  currentCoursebookId.value = getCoursebookId(row)
  currentCoursebookName.value = row.coursebookName || row.coursebook_name || ''
  contentDialogVisible.value = true
  await loadContentTree()
}

const handleContentDialogClosed = () => {
  contentTreeData.value = []
  currentCoursebookId.value = ''
  currentCoursebookName.value = ''
}

const openContentNodeDialog = (node, type, parentNode = null) => {
  resetContentForm()
  const editing = !!node && (!type || type === node.content_type)
  contentNodeEditingId.value = editing ? (node.content_id || '') : ''
  contentNodeParentId.value = parentNode ? String(parentNode.content_id || '0') : node ? String(node.content_id || '0') : '0'
  contentForm.content_type = type || 'unit'

  if (editing) {
    contentForm.content_id = node.content_id || ''
    contentForm.content_name = node.content_name || ''
    contentForm.content_type = node.content_type || type || 'unit'
    contentForm.parent_id = node.parent_id || '0'
    contentForm.coursebook_id = node.coursebook_id || currentCoursebookId.value
    contentForm.comments = node.comments || ''
    contentForm.status = node.status || 'y'
    contentForm.sort_order = Number(node.sort_order || 0)
  } else {
    if (type === 'chapter') {
      contentForm.parent_id = parentNode ? String(parentNode.content_id || '0') : '0'
    } else if (type === 'section') {
      contentForm.parent_id = parentNode ? String(parentNode.content_id || '0') : '0'
    } else {
      contentForm.parent_id = '0'
    }
    contentForm.content_type = type || 'unit'
  }
  contentNodeDialogVisible.value = true
}

const handleContentNodeDialogClosed = () => {
  resetContentForm()
  contentFormRef.value?.clearValidate?.()
}

const handleContentNodeSubmit = async () => {
  if (!contentFormRef.value) return
  try {
    await contentFormRef.value.validate()
    contentSubmitLoading.value = true
    const payload = {
      content_name: contentForm.content_name,
      content_type: contentForm.content_type,
      parent_id: contentForm.parent_id,
      coursebook_id: currentCoursebookId.value,
      comments: contentForm.comments,
      status: contentForm.status,
      sort_order: contentForm.sort_order
    }
    const res = contentForm.content_id ? await updateCoursebookContent(contentForm.content_id, payload) : await createCoursebookContent(payload)
    if (res.data.code === 200) {
      ElMessage.success(contentForm.content_id ? '更新成功' : '新增成功')
      contentNodeDialogVisible.value = false
      await loadContentTree()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    contentSubmitLoading.value = false
  }
}

const handleNodeDrop = async () => {
  try {
    await syncContentOrder()
    ElMessage.success('排序已更新')
    await loadContentTree()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '排序更新失败')
    await loadContentTree()
  }
}

const handleDeleteContent = async (node) => {
  try {
    await ElMessageBox.confirm(`确定删除目录【${node.content_name || ''}】吗？`, '提示', { type: 'warning' })
    const res = await deleteCoursebookContent(node.content_id)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadContentTree()
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
  await loadOptions()
  await loadItems()
})
</script>

<style scoped>
.user-toolbar--inline {
  display: flex;
  align-items: center;
}

.dict-toolbar-actions--inline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
}

.dict-toolbar-buttons--inline {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.content-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.content-toolbar__title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.content-tree {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px 12px;
  max-height: 520px;
  overflow: auto;
}

.content-tree-node {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.content-tree-node__name {
  font-weight: 500;
  color: #1f2d3d;
}

.content-tree-node__type {
  color: #909399;
  font-size: 12px;
}

.content-tree-node__actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.coursebook-file-upload-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coursebook-file-preview-link {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.coursebook-file-preview-label {
  color: #909399;
}

.file-upload-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
