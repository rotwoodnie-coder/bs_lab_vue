<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 菜单管理</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="菜单名称/编码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option label="启用" value="y" />
              <el-option label="停用" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadMenus">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openCreateDialog">新增菜单</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" row-key="menuId" class="menu-table">
        <el-table-column prop="menuName" label="菜单名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="menuCode" label="菜单编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="menuType" label="类型" width="100" align="center" />
        <el-table-column prop="path" label="路由路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 'y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button
              link
              :type="scope.row.status === 'y' ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 'y' ? '停用' : '启用' }}
            </el-button>
            <!--
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            -->
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">
            当前每页 {{ queryForm.pageSize }} 条，共 {{ total }} 条数据
          </div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="共 {total} 条, sizes, prev, pager, next, jumper"
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="user-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="父菜单" prop="parentId">
              <el-select v-model="form.parentId" placeholder="请选择父菜单" style="width: 100%">
                <el-option label="顶级菜单" value="0" />
                <el-option-group label="一级菜单">
                  <el-option
                    v-for="item in topMenuOptions"
                    :key="item.menuId"
                    :label="item.menuName"
                    :value="item.menuId"
                  />
                </el-option-group>
                <el-option-group label="二级菜单">
                  <el-option
                    v-for="item in secondLevelMenuOptions"
                    :key="item.menuId"
                    :label="`${parentNameMap[item.parentId] || ''} / ${item.menuName}`"
                    :value="item.menuId"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="menuType">
              <el-select v-model="form.menuType" placeholder="请选择类型" style="width: 100%">
                <el-option label="菜单" value="menu" />
                <el-option label="页面" value="page" />
                <el-option label="按钮" value="button" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单编码" prop="menuCode">
              <el-input v-model="form.menuCode" placeholder="请输入菜单编码" :maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" :maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由路径" :maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件标识" prop="component">
              <el-input v-model="form.component" placeholder="请输入组件标识" :maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="y">启用</el-radio>
                <el-radio label="n">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="comments">
              <el-input v-model="form.comments" type="textarea" :rows="3" placeholder="请输入备注" :maxlength="50" />
            </el-form-item>
          </el-col>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createMenu, deleteMenu, fetchMenus, updateMenu, updateMenuStatus } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref()
const total = ref(0)
const editId = ref('')
const tableData = ref([])
const pageSizes = [10, 20, 30, 40, 50]

const queryForm = reactive({
  keyword: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  parentId: '0',
  menuName: '',
  menuCode: '',
  menuType: 'menu',
  path: '',
  component: '',
  sortOrder: 0,
  status: 'y',
  comments: ''
})

const parentNameMap = computed(() => Object.fromEntries(tableData.value.map(item => [item.menuId, item.menuName])))
const topMenuOptions = computed(() => tableData.value.filter(item => item.parentId === '0'))
const secondLevelMenuOptions = computed(() => tableData.value.filter(item => topMenuOptions.value.some(parent => parent.menuId === item.parentId)))

const rules = {
  parentId: [{ required: true, message: '请选择父菜单', trigger: 'change' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const resetForm = () => {
  form.parentId = '0'
  form.menuName = ''
  form.menuCode = ''
  form.menuType = 'menu'
  form.path = ''
  form.component = ''
  form.sortOrder = 0
  form.status = 'y'
  form.comments = ''
}

const loadMenus = async () => {
  loading.value = true
  try {
    const res = await fetchMenus(queryForm)
    if (res.data.code === 200) {
      tableData.value = res.data.data.records || []
      total.value = res.data.data.total || 0
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
  queryForm.status = ''
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  loadMenus()
}

const handlePageChange = (page) => {
  queryForm.pageNum = page
  loadMenus()
}

const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadMenus()
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增菜单'
  editId.value = ''
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  editId.value = row.menuId
  form.parentId = row.parentId || '0'
  form.menuName = row.menuName
  form.menuCode = row.menuCode
  form.menuType = row.menuType || 'menu'
  form.path = row.path || ''
  form.component = row.component || ''
  form.sortOrder = row.sortOrder || 0
  form.status = row.status || 'y'
  form.comments = row.comments || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form }
      const res = isEdit.value ? await updateMenu(editId.value, payload) : await createMenu(payload)
      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadMenus()
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

const handleToggleStatus = async (row) => {
  const nextStatus = row.status === 'y' ? 'n' : 'y'
  const actionText = nextStatus === 'y' ? '启用' : '停用'
  await ElMessageBox.confirm(`确定${actionText}菜单【${row.menuName}】吗？`, '提示', {
    type: 'warning'
  })
  try {
    const res = await updateMenuStatus(row.menuId, nextStatus)
    if (res.data.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadMenus()
    } else {
      ElMessage.error(res.data.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || `${actionText}失败`)
    }
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除菜单【${row.menuName}】吗？`, '提示', {
    type: 'warning'
  })
  try {
    const res = await deleteMenu(row.menuId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadMenus()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(loadMenus)
</script>
