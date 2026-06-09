<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">数据维护 > 教材版本</span>
          </div>
        </div>
      </template>

      <div class="toolbar-wrap">
        <div class="toolbar-filters">
          <el-input
            v-model="query.keyword"
            placeholder="请输入版本名称"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
        <div class="toolbar-actions">
          <el-button @click="loadItems">刷新</el-button>
          <el-button type="primary" @click="openCreateDialog">新增教材版本</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="edition_id" label="编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="edition_name" label="版本名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="comments" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="排序" width="90" align="center">
          <template #default="scope">{{ scope.row.sort_order ?? 0 }}</template>
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
 
      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">
            当前每页 {{ query.pageSize }} 条，共 {{ total }} 条数据
          </div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="共 {total} 条, sizes, prev, pager, next, jumper"
            :total="total"
            :current-page="query.pageNum"
            :page-size="query.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" class="user-dialog" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="user-form">
        <el-form-item label="编号" prop="editionId">
          <el-input v-model="form.editionId" :disabled="isEdit" placeholder="请输入编号" :maxlength="20" />
        </el-form-item>
        <el-form-item label="版本名称" prop="editionName">
          <el-input v-model="form.editionName" placeholder="请输入版本名称" :maxlength="30"/>
        </el-form-item>
        <el-form-item label="说明" prop="comments">
          <el-input v-model="form.comments" type="textarea" :rows="2" :maxlength="50" placeholder="请输入说明"/>
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
import { createDataDictItem, deleteDataDictItem, fetchDataDictItems, updateDataDictItem } from '../../api/index'

const type = 'data_textbook_edition'
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增教材版本')
const formRef = ref()
const tableData = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  editionId: '',
  editionName: '',
  comments: '',
  sortOrder: 0,
  status: 'y'
})

const rules = {
  editionId: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  editionName: [{ required: true, message: '请输入版本名称', trigger: 'blur' }],
  comments: [{ required: true, message: '请输入说明', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getNextSortOrder = () => {
  const maxSortOrder = tableData.value.reduce((max, row) => {
    const value = Number(row?.sort_order ?? row?.sortOrder ?? 0)
    return Number.isFinite(value) && value > max ? value : max
  }, 0)
  return maxSortOrder + 1
}

const resetForm = () => {
  form.editionId = ''
  form.editionName = ''
  form.comments = ''
  form.sortOrder = getNextSortOrder()
  form.status = 'y'
}

const clearValidate = () => {
  formRef.value?.clearValidate?.()
}

const handleDialogClosed = () => {
  resetForm()
  clearValidate()
}

const extractRow = (row) => ({
  editionId: row.edition_id ?? row.editionId ?? '',
  editionName: row.edition_name ?? row.editionName ?? '',
  comments: row.comments ?? '',
  sortOrder: Number(row.sort_order ?? row.sortOrder ?? 0),
  status: row.status ?? 'y'
})

const buildPayload = () => ({
  id: form.editionId,
  editionName: form.editionName,
  edition_name: form.editionName,
  comments: form.comments,
  sortOrder: form.sortOrder,
  status: form.status
})

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchDataDictItems(type, {
      params: {
        paged: true,
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        keyword: query.keyword || ''
      }
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      tableData.value = data.records || []
      total.value = data.total || 0
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  query.pageNum = 1
  await loadItems()
}

const resetSearch = async () => {
  query.keyword = ''
  query.pageNum = 1
  await loadItems()
}

const handlePageChange = async (page) => {
  query.pageNum = page
  loadItems()
}

const handleSizeChange = (size) => {
  query.pageSize = size
  loadItems()
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增教材版本'
  resetForm()
  form.sortOrder = getNextSortOrder()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  const payload = extractRow(row)
  isEdit.value = true
  dialogTitle.value = '编辑教材版本'
  resetForm()
  form.editionId = payload.editionId
  form.editionName = payload.editionName
  form.comments = payload.comments
  form.sortOrder = payload.sortOrder
  form.status = payload.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = buildPayload()
    const res = isEdit.value
      ? await updateDataDictItem(type, form.editionId, payload)
      : await createDataDictItem(type, payload)
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
  try {
    await ElMessageBox.confirm(`确定删除教材版本【${row.edition_name || row.editionName || ''}】吗？`, '提示', { type: 'warning' })
    const res = await deleteDataDictItem(type, row.edition_id || row.editionId)
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
.toolbar-wrap {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.toolbar-filters,
.toolbar-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
