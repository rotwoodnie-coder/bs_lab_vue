<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">实验题库</span>
          </div>
        </div>
      </template>

      <div class="question-toolbar">
        <div class="question-toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索题干/知识点" clearable class="question-search" @keyup.enter="loadItems" />
          <el-select v-model="query.status" placeholder="状态" clearable class="question-select">
            <el-option label="发布" value="y" />
              <el-option label="作废" value="n" />
              <el-option label="草稿" value="t" />
          </el-select>
          <el-button @click="loadItems">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="question-toolbar-right">
          <el-button type="primary" @click="openCreateDialog">新增题目</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="questionTypeId" label="题型" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ questionTypeLabel(scope.row.questionTypeId) }}</template>
        </el-table-column>
        <el-table-column prop="difficultyTypeId" label="难度" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ difficultyLabel(scope.row.difficultyTypeId) }}</template>
        </el-table-column>
        <el-table-column prop="gradeId" label="年级" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ gradeLabel(scope.row.gradeId) }}</template>
        </el-table-column>
        <el-table-column prop="subjectId" label="学科" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ subjectLabel(scope.row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="questionCapacityId" label="能力侧重点" min-width="160" show-overflow-tooltip>
          <template #default="scope">{{ capacityLabel(scope.row.questionCapacityId) }}</template>
        </el-table-column>
        <el-table-column prop="knowledgeContent" label="知识点" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="140" align="center">
          <template #default="scope">
            <el-select :model-value="scope.row.status" size="small" style="width: 110px" @change="(value) => handleStatusChange(scope.row, value)">
              <el-option label="发布" value="y" />
              <el-option label="作废" value="n" />
              <el-option label="草稿" value="t" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" :disabled="scope.row.status === 'y'" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" :disabled="scope.row.status === 'y'" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="question-pagination">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :page-sizes="[10, 20, 50, 100]" :total="page.total" layout="total, sizes, prev, pager, next, jumper" @size-change="handlePageChange" @current-change="handlePageChange" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="980px" class="user-dialog" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="user-form">
        <el-form-item label="题目内容" prop="questionContent">
          <div class="rich-editor-wrap">
            <div class="rich-toolbar">
              <el-button-group>
                <el-button size="small" @click="execEditorCommand('bold')">加粗</el-button>
                <el-button size="small" @click="execEditorCommand('italic')">斜体</el-button>
                <el-button size="small" @click="execEditorCommand('underline')">下划线</el-button>
              </el-button-group>
              <el-button size="small" @click="insertSymbol('√')">√</el-button>
              <el-button size="small" @click="insertSymbol('×')">×</el-button>
              <el-button size="small" @click="insertSymbol('±')">±</el-button>
              <el-button size="small" @click="triggerImagePicker">插入图片</el-button>
              <input ref="imageInputRef" class="rich-image-input" type="file" accept="image/*" @change="handleImageSelected" />
            </div>
            <div ref="editorRef" class="rich-editor" contenteditable="true"></div>
          </div>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="题型" prop="questionTypeId"><el-select v-model="form.questionTypeId" placeholder="请选择题型" clearable filterable><el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="难度" prop="difficultyTypeId"><el-select v-model="form.difficultyTypeId" placeholder="请选择难度" clearable filterable><el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="能力侧重点" prop="questionCapacityId"><el-select v-model="form.questionCapacityId" placeholder="请选择能力侧重点" clearable filterable><el-option v-for="item in capacityOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年级" prop="gradeId"><el-select v-model="form.gradeId" placeholder="请选择年级" clearable filterable @change="handleGradeChange"><el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学科" prop="subjectId"><el-select v-model="form.subjectId" placeholder="请选择学科" clearable filterable @change="handleSubjectChange"><el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="教材" prop="coursebookId"><el-select v-model="form.coursebookId" placeholder="请选择教材" clearable filterable :disabled="!form.subjectId" :loading="coursebookLoading" @change="handleCoursebookChange"><el-option v-for="item in coursebookOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单元" prop="contentUnitId"><el-select v-model="form.contentUnitId" placeholder="请选择单元" clearable filterable :disabled="!form.coursebookId" @change="handleUnitChange"><el-option v-for="item in unitOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="章" prop="contentChapterId"><el-select v-model="form.contentChapterId" placeholder="请选择章" clearable filterable :disabled="!form.contentUnitId" @change="handleChapterChange"><el-option v-for="item in chapterOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="节" prop="contentSectionId"><el-select v-model="form.contentSectionId" placeholder="请选择节" clearable filterable :disabled="!form.contentChapterId"><el-option v-for="item in sectionOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="知识点" prop="knowledgeContent"><el-input v-model="form.knowledgeContent" placeholder="请输入知识点" :maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="y">发布</el-radio><el-radio value="n">作废</el-radio><el-radio value="t">草稿</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>

        <div class="select-section">
          <div class="select-dialog-header">
            <div class="select-dialog-title">题目选项</div>
            <el-button type="primary" @click="addSelectRow">新增选项</el-button>
          </div>
          <el-table :data="selectList" border stripe class="user-table">
            <el-table-column label="排序" width="90" align="center"><template #default="scope"><el-input-number v-model="scope.row.sortOrder" :min="1" controls-position="right" /></template></el-table-column>
            <el-table-column label="选项内容" min-width="320"><template #default="scope"><el-input v-model="scope.row.selectContent" type="textarea" :rows="2" placeholder="请输入选项内容" /></template></el-table-column>
            <el-table-column label="正确答案" width="110" align="center"><template #default="scope"><el-switch v-model="scope.row.isRight" active-value="y" inactive-value="n" @change="(v) => handleRightChange(scope.$index, v)" /></template></el-table-column>
            <el-table-column label="操作" width="120" align="center"><template #default="scope"><el-button link type="danger" @click="removeSelectRow(scope.$index)">删除</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createExpQuestion, deleteExpQuestion, deleteExpQuestionSelect, fetchCoursebookContents, fetchCoursebooks, fetchDataDictItems, fetchExpQuestions, fetchExpQuestionSelects, saveExpQuestionSelects, updateExpQuestion } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增题目')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const editorRef = ref()
const imageInputRef = ref()
const editorInstance = ref(false)
const tableData = ref([])
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const query = reactive({ keyword: '', status: '' })

const questionTypeOptions = ref([])
const difficultyOptions = ref([])
const capacityOptions = ref([])
const gradeOptions = ref([])
const subjectOptions = ref([])
const coursebookOptions = ref([])
const unitOptions = ref([])
const chapterOptions = ref([])
const sectionOptions = ref([])
const coursebookLoading = ref(false)
const coursebookContentOptions = ref([])
const selectList = ref([])

const form = reactive({ questionContent: '', questionTypeId: '', difficultyTypeId: '', questionCapacityId: '', gradeId: '', subjectId: '', coursebookId: '', contentUnitId: '', contentChapterId: '', contentSectionId: '', knowledgeContent: '', status: 'y' })

const rules = computed(() => ({
  questionContent: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
  questionTypeId: [{ required: true, message: '请选择题型', trigger: 'change' }],
  difficultyTypeId: [{ required: true, message: '请选择难度', trigger: 'change' }],
  questionCapacityId: [{ required: true, message: '请输入能力侧重点ID', trigger: 'blur' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

const resetForm = () => {
  Object.assign(form, { questionContent: '', questionTypeId: '', difficultyTypeId: '', questionCapacityId: '', gradeId: '', subjectId: '', coursebookId: '', contentUnitId: '', contentChapterId: '', contentSectionId: '', knowledgeContent: '', status: 'y' })
  coursebookOptions.value = []
  coursebookContentOptions.value = []
  unitOptions.value = []
  chapterOptions.value = []
  sectionOptions.value = []
  selectList.value = []
  setEditorContent('')
}

const loadItems = async () => {
  loading.value = true
  try { const res = await fetchExpQuestions({ ...query, ...page, paged: true }); if (res.data.code === 200) { const data = res.data.data || {}; tableData.value = data.records || []; page.total = data.total || 0 } else { ElMessage.error(res.data.message || '加载失败') } } catch (e) { ElMessage.error(e?.response?.data?.message || '加载失败') } finally { loading.value = false }
}

const resetQuery = async () => { query.keyword = ''; query.status = ''; page.pageNum = 1; await loadItems() }
const handlePageChange = async () => loadItems()
const openCreateDialog = async () => { isEdit.value = false; editId.value = ''; dialogTitle.value = '新增题目'; resetForm(); form.status = 't'; dialogVisible.value = true; await nextTick(); initEditor(); initDefaultSelectRows() }

const openEditDialog = async (row) => {
  if (row.status === 'y') {
    ElMessage.warning('发布状态的题目不能编辑')
    return
  }
  isEdit.value = true
  editId.value = row.questionId
  dialogTitle.value = '编辑题目'
  resetForm()
  Object.assign(form, row)
  form.gradeId = row.gradeId != null ? String(row.gradeId) : ''
  form.subjectId = row.subjectId != null ? String(row.subjectId) : ''
  form.coursebookId = row.coursebookId != null ? String(row.coursebookId) : ''
  form.contentUnitId = row.contentUnitId != null ? String(row.contentUnitId) : ''
  form.contentChapterId = row.contentChapterId != null ? String(row.contentChapterId) : ''
  form.contentSectionId = row.contentSectionId != null ? String(row.contentSectionId) : ''
  form.questionContent = row.questionContent || ''
  dialogVisible.value = true
  await nextTick()
  initEditor()
  await loadCoursebookOptions(form.subjectId)
  await loadContentOptions(form.coursebookId)
  if (form.contentUnitId) chapterOptions.value = coursebookContentOptions.value.filter(i => i.type === 'chapter' && i.parentId === form.contentUnitId)
  if (form.contentChapterId) sectionOptions.value = coursebookContentOptions.value.filter(i => i.type === 'section' && i.parentId === form.contentChapterId)
  setEditorContent(form.questionContent)
  await loadSelectList(editId.value)
}

const normalizeSelectList = (rows) => (rows || []).map(item => ({ selectId: item.select_id ?? item.selectId ?? '', questionId: item.question_id ?? item.questionId ?? editId.value, selectContent: item.select_content ?? item.selectContent ?? '', sortOrder: item.sort_order ?? item.sortOrder ?? 1, isRight: item.is_right ?? item.isRight ?? 'n' })).sort((a, b) => Number(a.sortOrder ?? 0) - Number(b.sortOrder ?? 0))
const initDefaultSelectRows = () => { if (!selectList.value.length) selectList.value = ['A', 'B', 'C', 'D'].map((c, idx) => ({ selectId: '', questionId: editId.value || '', selectContent: c, sortOrder: idx + 1, isRight: 'n' })) }
const loadSelectList = async (questionId) => { const res = await fetchExpQuestionSelects(questionId); if (res.data.code === 200) { const list = normalizeSelectList(res.data.data || []); selectList.value = list.length ? list : ['A', 'B', 'C', 'D'].map((c, idx) => ({ selectId: '', questionId, selectContent: c, sortOrder: idx + 1, isRight: 'n' })) } else { selectList.value = [] } }
const addSelectRow = () => selectList.value.push({ selectId: '', questionId: editId.value || '', selectContent: '', sortOrder: selectList.value.length + 1, isRight: 'n' })
const removeSelectRow = (index) => { selectList.value.splice(index, 1); if (!selectList.value.length) initDefaultSelectRows() }
const handleRightChange = (index, value) => { if (value === 'y') selectList.value.forEach((item, idx) => { if (idx !== index) item.isRight = 'n' }) }
const handleDialogClosed = () => { resetForm(); formRef.value?.clearValidate?.() }

const handleSubmit = async () => {
  if (!formRef.value) return
  form.questionContent = getEditorContent()
  await formRef.value.validate()
  if (!selectList.value.length) initDefaultSelectRows()
  const emptyIndex = selectList.value.findIndex(item => !String(item.selectContent || '').trim())
  if (emptyIndex !== -1) {
    ElMessage.error(`第 ${emptyIndex + 1} 个选项内容不能为空`)
    return
  }
  if (!selectList.value.some(item => item.isRight === 'y')) {
    ElMessage.error('请至少勾选一个正确答案')
    return
  }
  submitLoading.value = true
  try {
    const payload = { ...form }
    const res = isEdit.value ? await updateExpQuestion(editId.value, payload) : await createExpQuestion(payload)
    if (res.data.code !== 200) {
      ElMessage.error(res.data.message || '操作失败')
      return
    }
    const questionId = isEdit.value
      ? editId.value
      : (res.data.data?.questionId || res.data.data?.question_id || res.data.data?.id || res.data?.data?.data?.questionId || editId.value)
    const selectPayload = selectList.value.map((item, index) => ({
      selectId: item.selectId,
      selectContent: String(item.selectContent || '').trim(),
      sortOrder: item.sortOrder || index + 1,
      isRight: item.isRight || 'n'
    }))
    if (questionId) {
      const selectRes = await saveExpQuestionSelects(questionId, selectPayload)
      if (selectRes.data.code !== 200) {
        ElMessage.error(selectRes.data.message || '选项保存失败')
        return
      }
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    await loadItems()
  } catch (e) {
    if (e?.message !== 'cancel') ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = async (row, status) => { try { const res = await updateExpQuestion(row.questionId, { ...row, status }); if (res.data.code === 200) { row.status = status; ElMessage.success('状态更新成功') } else { ElMessage.error(res.data.message || '状态更新失败') } } catch (e) { ElMessage.error(e?.response?.data?.message || '状态更新失败') } }
const handleDelete = async (row) => { 
  if (row.status === 'y') {
    ElMessage.warning('发布状态的题目不能删除')
    return
  }
  try { await ElMessageBox.confirm(`确定删除题目【${row.questionId}】吗？`, '提示', { type: 'warning' }); const res = await deleteExpQuestion(row.questionId); if (res.data.code === 200) { ElMessage.success('删除成功'); await loadItems() } else { ElMessage.error(res.data.message || '删除失败') } } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.response?.data?.message || '删除失败') } }

const normalizeDictOptions = (rows, idKey, nameKey) => (rows || []).filter(item => item?.[idKey] != null && item?.[nameKey] != null).map(item => ({ value: String(item[idKey]), label: item[nameKey] }))
const normalizeGradeOptions = (rows) => (rows || []).filter(item => item.status === 'y' && item.grade_id != null && item.grade_name != null).sort((a, b) => (Number(a.sort_order ?? 0) - Number(b.sort_order ?? 0)) || String(a.grade_name || '').localeCompare(String(b.grade_name || ''))).map(item => ({ value: String(item.grade_id), label: item.grade_name }))
const normalizeCoursebookOptions = (rows) => (rows || []).map(item => ({ coursebookId: item.coursebook_id ?? item.coursebookId ?? item.id, coursebookName: item.coursebook_name ?? item.coursebookName ?? item.name, status: item.status, sortOrder: item.sort_order ?? item.sortOrder })).filter(item => item.status === 'y' && item.coursebookId != null && item.coursebookName != null).sort((a, b) => (Number(a.sortOrder ?? 0) - Number(b.sortOrder ?? 0)) || String(a.coursebookName || '').localeCompare(String(b.coursebookName || ''))).map(item => ({ value: String(item.coursebookId), label: item.coursebookName }))

const loadCoursebookOptions = async (subjectId) => { coursebookOptions.value = []; if (!subjectId) return; coursebookLoading.value = true; try { const res = await fetchCoursebooks({ subject_id: subjectId, status: 'y' }); if (res.data.code === 200) { const data = res.data.data || []; const rows = Array.isArray(data) ? data : (data.records || data.list || []); coursebookOptions.value = normalizeCoursebookOptions(rows) } } finally { coursebookLoading.value = false } }
const normalizeContentOptions = (rows, type) => (rows || []).filter(item => item.status === 'y' && item.content_id != null && item.content_name != null && item.content_type === type).sort((a, b) => (Number(a.sort_order ?? 0) - Number(b.sort_order ?? 0)) || String(a.content_name || '').localeCompare(String(b.content_name || ''))).map(item => ({ value: String(item.content_id), label: item.content_name, parentId: item.parent_id != null ? String(item.parent_id) : '', coursebookId: item.coursebook_id != null ? String(item.coursebook_id) : '' }))
const loadContentOptions = async (coursebookId) => { coursebookContentOptions.value = []; unitOptions.value = []; chapterOptions.value = []; sectionOptions.value = []; if (!coursebookId) return; const res = await fetchCoursebookContents({ coursebookId, status: 'y' }); if (res.data.code === 200) { const data = res.data.data || []; const rows = Array.isArray(data) ? data : (data.records || data.list || []); coursebookContentOptions.value = (rows || []).map(item => ({ value: String(item.content_id ?? ''), label: item.content_name ?? '', type: item.content_type, parentId: item.parent_id != null ? String(item.parent_id) : '', coursebookId: item.coursebook_id != null ? String(item.coursebook_id) : '', status: item.status, sortOrder: item.sort_order ?? 0 })).filter(item => item.status === 'y' && item.value && item.label && String(item.coursebookId) === String(coursebookId)).sort((a, b) => (Number(a.sortOrder) - Number(b.sortOrder)) || String(a.label).localeCompare(String(b.label))); unitOptions.value = coursebookContentOptions.value.filter(item => item.type === 'unit' && (item.parentId === '0' || item.parentId === '')) } }
const handleGradeChange = () => {}
const handleSubjectChange = async (subjectId) => { form.coursebookId=''; form.contentUnitId=''; form.contentChapterId=''; form.contentSectionId=''; unitOptions.value=[]; chapterOptions.value=[]; sectionOptions.value=[]; coursebookContentOptions.value=[]; await loadCoursebookOptions(subjectId) }
const handleCoursebookChange = async (coursebookId) => { form.contentUnitId=''; form.contentChapterId=''; form.contentSectionId=''; await loadContentOptions(coursebookId) }
const handleUnitChange = (unitId) => { form.contentChapterId=''; form.contentSectionId=''; chapterOptions.value = coursebookContentOptions.value.filter(item => item.type === 'chapter' && item.parentId === unitId); sectionOptions.value = [] }
const handleChapterChange = (chapterId) => { form.contentSectionId=''; sectionOptions.value = coursebookContentOptions.value.filter(item => item.type === 'section' && item.parentId === chapterId) }
const loadLinkedOptions = async () => { const [typeRes, difficultyRes, capacityRes, subjectRes, gradeRes] = await Promise.all([fetchDataDictItems('data_question_type'), fetchDataDictItems('data_difficulty_type'), fetchDataDictItems('data_question_capacity'), fetchDataDictItems('data_school_subject'), fetchDataDictItems('data_school_grade')]); if (typeRes.data.code === 200) questionTypeOptions.value = normalizeDictOptions(typeRes.data.data || [], 'type_id', 'type_name'); if (difficultyRes.data.code === 200) difficultyOptions.value = normalizeDictOptions(difficultyRes.data.data || [], 'difficulty_id', 'difficulty_name'); if (capacityRes.data.code === 200) capacityOptions.value = normalizeDictOptions(capacityRes.data.data || [], 'capacity_id', 'capacity_name'); if (subjectRes.data.code === 200) subjectOptions.value = normalizeDictOptions(subjectRes.data.data || [], 'subject_id', 'subject_name'); if (gradeRes.data.code === 200) { const rows = gradeRes.data.data || []; gradeOptions.value = normalizeGradeOptions(Array.isArray(rows) ? rows : (rows.records || rows.list || [])) } }

const questionTypeLabel = (v) => questionTypeOptions.value.find(i => i.value === v)?.label || v || '-'
const difficultyLabel = (v) => difficultyOptions.value.find(i => i.value === v)?.label || v || '-'
const capacityLabel = (v) => capacityOptions.value.find(i => i.value === v)?.label || v || '-'
const gradeLabel = (v) => gradeOptions.value.find(i => i.value === v)?.label || v || '-'
const subjectLabel = (v) => subjectOptions.value.find(i => i.value === v)?.label || v || '-'

const initEditor = () => { if (!editorRef.value || editorInstance.value) return; editorRef.value.setAttribute('data-placeholder', '请输入富文本题干内容'); editorRef.value.addEventListener('input', syncEditorToForm); editorRef.value.addEventListener('blur', syncEditorToForm); editorInstance.value = true }
const syncEditorToForm = () => { if (editorRef.value) form.questionContent = editorRef.value.innerHTML || '' }
const setEditorContent = (html) => { if (editorRef.value) editorRef.value.innerHTML = html || '' }
const getEditorContent = () => editorRef.value ? (editorRef.value.innerHTML || '') : form.questionContent
const execEditorCommand = (command) => { editorRef.value?.focus(); document.execCommand(command, false, null); syncEditorToForm() }
const insertSymbol = (symbol) => { editorRef.value?.focus(); document.execCommand('insertText', false, symbol); syncEditorToForm() }
const triggerImagePicker = () => imageInputRef.value?.click()
const handleImageSelected = async (event) => { const file = event.target.files?.[0]; if (!file) return; const reader = new FileReader(); reader.onload = () => { editorRef.value?.focus(); document.execCommand('insertHTML', false, `<img src="${reader.result}" alt="图片" style="max-width: 100%; height: auto;" />`); syncEditorToForm() }; reader.readAsDataURL(file); event.target.value = '' }

watch(() => dialogVisible.value, async (visible) => { if (!visible) return; await nextTick(); initEditor(); setEditorContent(form.questionContent) })

onBeforeUnmount(() => { if (editorRef.value) { editorRef.value.removeEventListener('input', syncEditorToForm); editorRef.value.removeEventListener('blur', syncEditorToForm) } })
onMounted(async () => { await loadLinkedOptions(); await loadItems() })
</script>

<style scoped>
.question-toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:12px}.question-toolbar-left,.question-toolbar-right{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.question-search{width:240px}.question-select{width:140px}.question-pagination{margin-top:16px;display:flex;justify-content:flex-end}.rich-editor-wrap{width:100%}.rich-editor{height:200px;max-height:200px;border:1px solid #dcdfe6;border-radius:6px;padding:12px;outline:none;line-height:1.6;overflow:auto}.rich-editor:empty::before{content:attr(data-placeholder);color:#a8abb2}.rich-editor-small{min-height:160px}.select-section{margin-top:16px}.select-dialog-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}.select-dialog-title{font-weight:600}.rich-image-input{display:none}:deep(.el-form-item__content .el-select){width:100%}@media (max-width:768px){.question-search,.question-select{width:100%}.question-toolbar,.question-toolbar-left,.question-toolbar-right{width:100%}}
</style>
