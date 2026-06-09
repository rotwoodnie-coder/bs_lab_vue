<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 角色管理</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="角色名称/编码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option label="启用" value="y" />
              <el-option label="停用" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRoles">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openCreateDialog">新增角色</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="roleCode" label="角色编码" min-width="180" show-overflow-tooltip />
        <el-table-column prop="roleName" label="角色名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 'y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="openAuthDialog(scope.row)">授权菜单</el-button>
            <el-button
              link
              :type="scope.row.status === 'y' ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 'y' ? '停用' : '启用' }}
            </el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="user-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="角色编码" prop="roleCode">
              <el-input v-model="form.roleCode" placeholder="请输入角色编码"  :maxlength="20"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName" placeholder="请输入角色名称"  :maxlength="20"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="y">启用</el-radio>
                <el-radio value="n">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="authDialogVisible" title="授权菜单" width="720px" class="user-dialog">
      <div class="auth-role-tip">当前角色：{{ currentRoleName }}</div>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        node-key="menuId"
        show-checkbox
        default-expand-all
        :props="treeProps"
        :default-checked-keys="checkedMenuIds"
        :check-strictly="true"
      />
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="authLoading" @click="handleSaveAuth">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createRole, deleteRole, fetchMenus, fetchRoleMenus, fetchRoles, saveRoleMenus, updateRole, updateRoleStatus } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const authLoading = ref(false)
const dialogVisible = ref(false)
const authDialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref()
const menuTreeRef = ref()
const total = ref(0)
const editId = ref('')
const currentAuthRoleId = ref('')
const currentRoleName = ref('')
const checkedMenuIds = ref([])
const tableData = ref([])
const menuTreeData = ref([])
const pageSizes = [10, 20, 30, 40, 50]

const treeProps = {
  children: 'children',
  label: 'displayName'
}

const queryForm = reactive({
  keyword: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  roleName: '',
  roleCode: '',
  status: 'y'
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const resetForm = () => {
  form.roleName = ''
  form.roleCode = ''
  form.status = 'y'
}

const buildTree = (list) => {
  const map = new Map()
  const roots = []
  list.forEach((item) => {
    const displayName = item.comments ? `${item.menuName} - ${item.comments}` : item.menuName
    map.set(item.menuId, { ...item, displayName, children: [] })
  })
  map.forEach((item) => {
    if (item.parentId && item.parentId !== '0' && map.has(item.parentId)) {
      map.get(item.parentId).children.push(item)
    } else {
      roots.push(item)
    }
  })
  return roots
}

const loadMenusForAuth = async () => {
  const res = await fetchMenus({ pageNum: 1, pageSize: 9999 })
  if (res.data.code === 200) {
    menuTreeData.value = buildTree(res.data.data.records || [])
  }
}

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await fetchRoles(queryForm)
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
  loadRoles()
}

const handlePageChange = (page) => {
  queryForm.pageNum = page
  loadRoles()
}

const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadRoles()
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  editId.value = ''
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  editId.value = row.roleId
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.status = row.status || 'y'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form }
      const res = isEdit.value ? await updateRole(editId.value, payload) : await createRole(payload)
      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadRoles()
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
  await ElMessageBox.confirm(`确定${actionText}角色【${row.roleName}】吗？`, '提示', {
    type: 'warning'
  })
  try {
    const res = await updateRoleStatus(row.roleId, nextStatus)
    if (res.data.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadRoles()
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
  await ElMessageBox.confirm(`确定删除角色【${row.roleName}】吗？`, '提示', {
    type: 'warning'
  })
  try {
    const res = await deleteRole(row.roleId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadRoles()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const openAuthDialog = async (row) => {
  currentAuthRoleId.value = row.roleId
  currentRoleName.value = row.roleName
  authDialogVisible.value = true
  checkedMenuIds.value = []
  await loadMenusForAuth()
  const res = await fetchRoleMenus(row.roleId)
  if (res.data.code === 200) {
    checkedMenuIds.value = res.data.data.menuIds || []
    console.log("checkedMenuIds:"+checkedMenuIds.value);
    await nextTick()
    menuTreeRef.value?.setCheckedKeys(checkedMenuIds.value)
  }
}

const handleSaveAuth = async () => {
  authLoading.value = true
  try {
    const checked = menuTreeRef.value?.getCheckedKeys(false) || []
    const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || []
    const menuIds = Array.from(new Set([...checked, ...halfChecked]))
    const res = await saveRoleMenus(currentAuthRoleId.value, menuIds)
    if (res.data.code === 200) {
      ElMessage.success('授权成功')
      authDialogVisible.value = false
    } else {
      ElMessage.error(res.data.message || '授权失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '授权失败')
  } finally {
    authLoading.value = false
  }
}

onMounted(loadRoles)
</script>
