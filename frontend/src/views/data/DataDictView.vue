<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">数据维护 > 数据字典</span>
          </div>
        </div>
      </template>

      <div class="dict-layout">
        <aside class="dict-sidebar">
          <div class="dict-sidebar-title">数据分类</div>
          <el-radio-group v-model="activeType" @change="handleTypeChange" class="dict-type-group">
            <el-radio-button
              v-for="item in typeTabs"
              :key="item.type"
              :value="item.type"
              :class="{ 'is-active-type': activeType === item.type }"
            >
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </aside>

        <section class="dict-main">
          <div class="dict-toolbar-actions">
            <div class="dict-toolbar-hint">
              当前分类：<strong>{{ typeMeta.label }}</strong>，共 {{ tableData.length }} 条
            </div>
            <div class="dict-toolbar-buttons">
              <el-button @click="loadItems">刷新</el-button>
              <el-button type="primary" @click="openCreateDialog">新增{{ typeMeta.label }}</el-button>
            </div>
          </div>

          <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
            <el-table-column label="编号" min-width="220" show-overflow-tooltip>
              <template #default="scope">{{ getRowId(scope.row) }}</template>
            </el-table-column>
            <el-table-column :label="typeMeta.nameLabel" min-width="180" show-overflow-tooltip>
              <template #default="scope">{{ getRowName(scope.row) }}</template>
            </el-table-column>
            <el-table-column prop="comments" label="说明" min-width="220" show-overflow-tooltip />
            <el-table-column label="排序" width="90" align="center">
              <template #default="scope">{{ getRowSortOrder(scope.row) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'" effect="light">
                  {{ scope.row.status === 'y' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
                <!--
                <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
                -->
              </template>
            </el-table-column>
          </el-table>
        </section>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" class="user-dialog" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="user-form">
        <el-form-item label="编号" prop="id">
          <el-input v-model="form.id" :disabled="isEdit" placeholder="请输入编号" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="typeMeta.nameLabel" :prop="typeMeta.nameField">
          <el-input v-model="form[typeMeta.nameField]" :placeholder="`请输入${typeMeta.nameLabel}`" :maxlength="30" />
        </el-form-item>
        <el-form-item label="说明" prop="comments">
          <el-input v-model="form.comments" type="textarea" :rows="3" placeholder="请输入说明" :maxlength="50" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="y">启用</el-radio>
            <el-radio value="n">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { createDataDictItem, deleteDataDictItem, fetchDataDictItems, updateDataDictItem } from '../../api/index'

const typeTabs = [
  { type: 'data_school_subject', label: '学科', nameField: 'subjectName', nameLabel: '学科名称', idField: 'subjectId' },
  { type: 'data_school_semester', label: '学期', nameField: 'semesterName', nameLabel: '学期名称', idField: 'semesterId' },
  { type: 'data_school_level', label: '学段', nameField: 'levelName', nameLabel: '学段名称', idField: 'levelId' },
  { type: 'data_school_grade', label: '年级', nameField: 'gradeName', nameLabel: '年级名称', idField: 'gradeId' },
  { type: 'data_material_prop', label: '材料属性', nameField: 'propName', nameLabel: '属性名称', idField: 'propId' },
  { type: 'data_material_type', label: '材料类型', nameField: 'typeName', nameLabel: '类型名称', idField: 'typeId' },
  { type: 'data_material_unit', label: '计量单位', nameField: 'unitName', nameLabel: '单位名称', idField: 'unitId' },
  { type: 'data_material_security', label: '安全性', nameField: 'securityName', nameLabel: '安全性名称', idField: 'securityId' },
  { type: 'data_difficulty_type', label: '难度', nameField: 'difficultyName', nameLabel: '难度名称', idField: 'difficultyId' },
  { type: 'data_question_capacity', label: '能力侧重点', nameField: 'capacityName', nameLabel: '能力侧重点', idField: 'capacityId' },
  { type: 'data_question_type', label: '题型', nameField: 'typeName', nameLabel: '题型名称', idField: 'typeId' },
  { type: 'data_pref_title', label: '职称', nameField: 'titleName', nameLabel: '职称名称', idField: 'titleId' },
  { type: 'data_rating_scale', label: '评分等级', nameField: 'scaleName', nameLabel: '等级名称', idField: 'scaleId' }
]

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增')
const formRef = ref()
const tableData = ref([])
const activeType = ref(typeTabs[0].type)
const editId = ref('')

const form = reactive({
  id: '',
  subjectName: '',
  semesterName: '',
  levelName: '',
  gradeName: '',
  propName: '',
  typeName: '',
  unitName: '',
  securityName: '',
  titleName: '',
  scaleName: '',
  comments: '',
  sortOrder: 0,
  status: 'y'
})

const typeMeta = computed(() => typeTabs.find(item => item.type === activeType.value) || typeTabs[0])
const toSnakeCase = (value) => value.replace(/([A-Z])/g, '_$1').toLowerCase()
const pickFirstValue = (row, keys) => {
  for (const key of keys) {
    const value = row?.[key]
    if (value !== undefined && value !== null && String(value).trim() !== '') return value
  }
  return ''
}

const getRowId = (row) => pickFirstValue(row, [
  typeMeta.value.idField,
  toSnakeCase(typeMeta.value.idField),
  'id',
  'dictId',
  'dataId',
  'code',
  'value'
])
const getRowName = (row) => pickFirstValue(row, [
  typeMeta.value.nameField,
  toSnakeCase(typeMeta.value.nameField),
  'name',
  'dictName',
  'dataName',
  'title',
  'label',
  'text'
])
const getRowSortOrder = (row) => pickFirstValue(row, ['sortOrder', 'sort_order', 'sort', 'order'])
const getNextSortOrder = () => {
  const maxSortOrder = tableData.value.reduce((max, row) => {
    const value = Number(getRowSortOrder(row))
    return Number.isFinite(value) && value > max ? value : max
  }, 0)
  return maxSortOrder + 1
}

const rules = computed(() => ({
  id: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  [typeMeta.value.nameField]: [{ required: true, message: `请输入${typeMeta.value.nameLabel}`, trigger: 'blur' }],
  comments: [{ required: true, message: '请输入说明', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

const extractRowPayload = (row) => ({
  id: getRowId(row),
  name: getRowName(row),
  comments: row.comments || '',
  sortOrder: Number(getRowSortOrder(row)) || 0,
  status: row.status || 'y'
})

const buildPayload = () => ({
  id: form.id,
  [typeMeta.value.nameField]: form[typeMeta.value.nameField],
  [toSnakeCase(typeMeta.value.nameField)]: form[typeMeta.value.nameField],
  comments: form.comments,
  sortOrder: form.sortOrder,
  status: form.status
})

const isDuplicateId = (id) => {
  const normalizedId = String(id || '').trim()
  if (!normalizedId) return false
  return tableData.value.some((row) => String(getRowId(row) || '').trim() === normalizedId)
}

const resetForm = () => {
  form.id = ''
  form.subjectName = ''
  form.semesterName = ''
  form.levelName = ''
  form.gradeName = ''
  form.propName = ''
  form.typeName = ''
  form.unitName = ''
  form.securityName = ''
  form.titleName = ''
  form.scaleName = ''
  form.comments = ''
  form.sortOrder = getNextSortOrder()
  form.status = 'y'
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchDataDictItems(activeType.value)
    if (res.data.code === 200) {
      const rawData = res.data.data || []
      tableData.value = Array.isArray(rawData) ? rawData : (rawData.records || rawData.list || rawData.rows || [])
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleTypeChange = async () => {
  dialogVisible.value = false
  resetForm()
  clearValidate()
  await loadItems()
}

const clearValidate = () => {
  formRef.value?.clearValidate?.()
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = `新增${typeMeta.value.label}`
  resetForm()
  form.sortOrder = getNextSortOrder()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  const payload = extractRowPayload(row)
  isEdit.value = true
  editId.value = payload.id
  dialogTitle.value = `编辑${typeMeta.value.label}`
  resetForm()
  form.id = payload.id
  form[typeMeta.value.nameField] = payload.name
  form.comments = payload.comments
  form.sortOrder = payload.sortOrder
  form.status = payload.status
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  resetForm()
  clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (!isEdit.value && isDuplicateId(form.id)) {
      ElMessage.error('编号已被占用，请更改')
      return
    }
    submitLoading.value = true
    const payload = buildPayload()
    const res = isEdit.value
      ? await updateDataDictItem(activeType.value, editId.value, payload)
      : await createDataDictItem(activeType.value, payload)

    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    if (error?.message !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  const name = getRowName(row)
  try {
    await ElMessageBox.confirm(`确定删除${typeMeta.value.label}【${name}】吗？`, '提示', { type: 'warning' })
    const res = await deleteDataDictItem(activeType.value, getRowId(row))
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

onMounted(loadItems)
</script>

<style scoped>
.dict-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.dict-sidebar {
  width: 220px;
  flex: 0 0 220px;
  padding: 16px 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
  position: sticky;
  top: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.dict-sidebar-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.dict-type-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.dict-type-group :deep(.el-radio-button) {
  width: 100%;
}

.dict-type-group :deep(.el-radio-button__inner) {
  width: 100%;
  text-align: left;
  border-radius: 8px !important;
  border: 1px solid #dcdfe6;
  background: #fff;
  box-shadow: none;
  transition: all 0.2s ease;
}

.dict-type-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  box-shadow: inset 0 0 0 1px var(--el-color-primary);
}

.dict-type-group :deep(.el-radio-button__inner:hover) {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.is-active-type :deep(.el-radio-button__inner) {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.dict-main {
  flex: 1;
  min-width: 0;
}

.dict-main {
  flex: 1;
  min-width: 0;
}

.dict-toolbar-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.dict-toolbar-hint {
  color: #606266;
  font-size: 14px;
}

.dict-toolbar-buttons {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .dict-layout {
    flex-direction: column;
  }

  .dict-sidebar {
    width: 100%;
    flex-basis: auto;
    position: static;
  }

  .dict-type-group {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .dict-type-group :deep(.el-radio-button) {
    width: auto;
  }

  .dict-type-group :deep(.el-radio-button__inner) {
    width: auto;
  }

  .dict-toolbar-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
