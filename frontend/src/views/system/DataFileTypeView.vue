<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 文件类型</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar">
        <div class="dict-toolbar-hint">
          共 {{ tableData.length }} 条数据
        </div>
        <div class="dict-toolbar-buttons">
          <el-button @click="loadItems">刷新</el-button>
          <el-button type="primary" @click="openCreateDialog">新增素材类别</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column label="图标" width="90" align="center">
          <template #default="scope">
            <img
              v-if="getIconUrl(scope.row.logoClass)"
              :src="getIconUrl(scope.row.logoClass)"
              :alt="scope.row.logoClass || 'icon'"
              class="logo-icon"
            />
            <span v-else class="logo-icon-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="typeId" label="编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="typeName" label="类别名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="comments" label="说明" min-width="220" show-overflow-tooltip />
        <!--
        <el-table-column prop="logoClass" label="图标样式" min-width="160" show-overflow-tooltip />
        -->
        <el-table-column label="排序" width="90" align="center">
          <template #default="scope">{{ scope.row.sortOrder ?? 0 }}</template>
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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" class="user-dialog" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="user-form">
        <el-form-item label="编号" prop="typeId">
          <el-input v-model="form.typeId" :disabled="isEdit" placeholder="请输入编号" :maxlength="30" />
        </el-form-item>
        <el-form-item label="类别名称" prop="typeName">
          <el-input v-model="form.typeName" placeholder="请输入类别名称" :maxlength="30" />
        </el-form-item>
        <el-form-item label="说明" prop="comments">
          <el-input v-model="form.comments" type="textarea" :rows="3" placeholder="请输入说明" :maxlength="50" />
        </el-form-item>
        <el-form-item label="图标样式" prop="logoClass">
          <el-input v-model="form.logoClass" placeholder="请输入图标样式" :maxlength="30" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createDataFileType, deleteDataFileType, fetchDataFileTypes, updateDataFileType } from '../../api/index'
import { FILE_TYPE_ICON_MAP } from '@/utils/publicUrl'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增素材类别')
const formRef = ref()
const editId = ref('')
const tableData = ref([])

const form = reactive({
  typeId: '',
  typeName: '',
  comments: '',
  logoClass: '',
  sortOrder: 0,
  status: 'y'
})

const rules = {
  typeId: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  typeName: [{ required: true, message: '请输入类别名称', trigger: 'blur' }],
  comments: [{ required: true, message: '请输入说明', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getIconUrl = (logoClass) => {
  const key = String(logoClass || '').trim().toLowerCase()
  return FILE_TYPE_ICON_MAP[key] || ''
}

const resetForm = () => {
  form.typeId = ''
  form.typeName = ''
  form.comments = ''
  form.logoClass = ''
  form.sortOrder = 0
  form.status = 'y'
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchDataFileTypes()
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

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = '新增素材类别'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.typeId
  dialogTitle.value = '编辑素材类别'
  form.typeId = row.typeId
  form.typeName = row.typeName || ''
  form.comments = row.comments || ''
  form.logoClass = row.logoClass || ''
  form.sortOrder = row.sortOrder || 0
  form.status = row.status || 'y'
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  resetForm()
  formRef.value?.clearValidate?.()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = {
      ...form,
      typeId: String(form.typeId || '').trim()
    }
    const res = isEdit.value ? await updateDataFileType(editId.value, payload) : await createDataFileType(payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除素材类别【${row.typeName}】吗？`, '提示', { type: 'warning' })
    const res = await deleteDataFileType(row.typeId)
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

onMounted(loadItems)
</script>

<style scoped>
.logo-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
  display: inline-block;
}

.logo-icon-placeholder {
  color: #c0c4cc;
  font-size: 15px;
}
</style>
