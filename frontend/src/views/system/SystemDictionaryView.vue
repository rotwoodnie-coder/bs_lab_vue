<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 系统数据字典维护</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar">
        <el-radio-group v-model="activeType" @change="handleTypeChange">
          <el-radio-button value="data_msg_type">消息类型</el-radio-button>
          <el-radio-button value="data_org_type">组织类型</el-radio-button>
          <el-radio-button value="data_role">用户类型</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openCreateDialog">新增{{ typeMeta.label }}</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column label="编号" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            {{ getRowId(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column :prop="typeMeta.nameField" :label="typeMeta.nameLabel" min-width="180" show-overflow-tooltip />
        <el-table-column prop="comments" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="user-form">
        <el-form-item label="编号" prop="id">
          <el-input v-model="form.id" placeholder="编号由数据库返回" disabled />
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
import { createDictionaryItem, deleteDictionaryItem, fetchDictionaryItems, updateDictionaryItem } from '../../api/index'

const TYPES = {
  data_msg_type: { label: '消息类型', nameField: 'msgTypeName', nameLabel: '类型名称' },
  data_org_type: { label: '组织类型', nameField: 'orgTypeName', nameLabel: '类型名称' },
  data_role: { label: '用户类型', nameField: 'roleName', nameLabel: '类型名称' }
}

const activeType = ref('data_msg_type')
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const tableData = ref([])

const form = reactive({
  id: '',
  msgTypeName: '',
  orgTypeName: '',
  roleName: '',
  comments: '',
  sortOrder: 0,
  status: 'y'
})

const typeMeta = computed(() => TYPES[activeType.value])

const rules = computed(() => ({
  id: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  [typeMeta.value.nameField]: [{ required: true, message: `请输入${typeMeta.value.nameLabel}`, trigger: 'blur' }],
  comments: [{ required: true, message: '请输入说明', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

const resetForm = () => {
  form.id = ''
  form.msgTypeName = ''
  form.orgTypeName = ''
  form.roleName = ''
  form.comments = ''
  form.sortOrder = 0
  form.status = 'y'
}

const pickNameValue = (row) => row[typeMeta.value.nameField] || ''

const getRowId = (row) => row.id || row.msgTypeId || row.orgTypeId || row.roleId || ''

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchDictionaryItems(activeType.value)
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleTypeChange = () => {
  loadItems()
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = `新增${typeMeta.value.label}`
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = getRowId(row)
  dialogTitle.value = `编辑${typeMeta.value.label}`
  resetForm()
  form.id = getRowId(row)
  form[typeMeta.value.nameField] = pickNameValue(row)
  form.comments = row.comments || ''
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status || 'y'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        id: form.id,
        [typeMeta.value.nameField]: form[typeMeta.value.nameField],
        comments: form.comments,
        sortOrder: form.sortOrder,
        status: form.status
      }
      const res = isEdit.value
        ? await updateDictionaryItem(activeType.value, editId.value, payload)
        : await createDictionaryItem(activeType.value, payload)
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
  const name = pickNameValue(row)
  await ElMessageBox.confirm(`确定删除${typeMeta.value.label}【${name}】吗？`, '提示', { type: 'warning' })
  try {
    const id = row[activeType.value === 'data_msg_type' ? 'msgTypeId' : activeType.value === 'data_org_type' ? 'orgTypeId' : 'roleId']
    const res = await deleteDictionaryItem(activeType.value, id)
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

onMounted(loadItems)
</script>
