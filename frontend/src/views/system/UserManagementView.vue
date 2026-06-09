<template>
  <div>
    <el-card>
      <template #header>
        <div class="user-page-header">
          <span class="user-page-title">系统管理 > 用户管理</span>
        </div>
      </template>
      <div class="user-toolbar">
        <el-form :inline="true" :model="queryForm" class="search-form">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="姓名/登录名" :maxlength="30" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="queryForm.roleId" placeholder="全部类型" clearable filterable style="width: 160px">
              <el-option v-for="role in roleOptions" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option label="启用" value="y" />
              <el-option label="停用" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadUsers">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      
          <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="userName" label="姓名" min-width="120" />
        <el-table-column prop="loginName" label="登录名" min-width="120" />
        <el-table-column prop="userOrgName" label="机构" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.userRoleName || scope.row.userRoleId || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'">
              {{ scope.row.status === 'y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已授权" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ formatUserRoleNames(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="expireDate" label="到期时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="openAuthDialog(scope.row)">授权角色</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="userName">
          <el-input v-model="form.userName" :maxlength="20" />
        </el-form-item>
        <el-form-item label="登录名" prop="loginName">
          <el-input v-model="form.loginName" :maxlength="20" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '不修改请留空' : '请输入密码'" :maxlength="20" />
        </el-form-item>
        <el-form-item label="机构" prop="userOrgId">
          <el-tree-select
            v-model="form.userOrgId"
            :data="orgTreeOptions"
            :props="orgTreeProps"
            node-key="orgId"
            check-strictly
            filterable
            clearable
            placeholder="请选择机构"
            style="width: 100%"
            @change="handleOrgChange"
          />
        </el-form-item>
        <el-form-item label="根机构" prop="rootOrgId">
          <el-input v-model="rootOrgName" disabled placeholder="根据所选机构自动带出" />
        </el-form-item>
        <el-form-item label="类型" prop="userRoleId">
          <el-select v-model="form.userRoleId" placeholder="请选择类型" filterable clearable style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="根机构" prop="rootOrgId" v-show="false">
          <el-input v-model="form.rootOrgId" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" :maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" :maxlength="30" />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireDate">
          <el-date-picker
            v-model="form.expireDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="不填则默认当前时间"
            style="width: 100%"
          />
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

    <el-dialog v-model="authDialogVisible" title="授权角色" width="620px">
      <div class="auth-role-tip">
        <div>当前用户：{{ currentUserName }}</div>
        <div>已授权角色：{{ selectedRoleNames || '暂无' }}</div>
      </div>
      <el-checkbox-group v-model="selectedRoleIds">
        <el-space wrap>
          <el-checkbox v-for="role in roleOptions" :key="role.roleId" :label="role.roleId">
            {{ role.roleName }}
          </el-checkbox>
        </el-space>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="authLoading" @click="handleSaveAuth">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createUser, deleteUser, fetchDictionaryItems, fetchOrgTree, fetchUserRoles, fetchUsers, saveUserRoles, updateUser, updateUserStatus } from '../../api/index';

const loading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const authDialogVisible = ref(false);
const authLoading = ref(false);
const isEdit = ref(false);
const dialogTitle = ref('新增用户');
const formRef = ref();
const total = ref(0)
const editId = ref('');
const tableData = ref([]);
const pageSizes = [10, 20, 30, 40, 50]
const currentUserName = ref('')
const selectedRoleNames = ref('')
const roleOptions = ref([])
const selectedRoleIds = ref([])
const orgTreeOptions = ref([])
const orgTreeProps = { children: 'children', value: 'orgId', label: 'orgName' }
const rootOrgMap = ref({})
const rootOrgName = ref('')
const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const isSysAdmin = currentUserInfo.userRoleId === 'Sys_Admin'
const currentRootOrgId = currentUserInfo.rootOrgId || ''

const queryForm = reactive({
  keyword: '',
  roleId: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  userName: '',
  loginName: '',
  password: '',
  userOrgId: '',
  rootOrgId: '',
  userRoleId: '',
  phone: '',
  email: '',
  expireDate: '',
  status: 'y'
})

const rules = {
  userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  loginName: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
  userOrgId: [{ required: true, message: '请选择机构', trigger: 'change' }],
  userRoleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  expireDate: [{ required: false }]
}

const getDefaultExpireDate = () => {
  const date = new Date()
  date.setFullYear(date.getFullYear() + 1)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const resetForm = () => {
  form.userName = '';
  form.loginName = '';
  form.password = '';
  form.userOrgId = '';
  form.rootOrgId = '';
  form.userRoleId = '';
  form.phone = '';
  form.email = '';
  form.expireDate = getDefaultExpireDate();
  form.status = 'y';
  rootOrgName.value = '';
}

const loadUsers = async () => {
  loading.value = true;
  try {
    const res = await fetchUsers(queryForm);
    if (res.data.code === 200) {
      tableData.value = res.data.data.records || [];
      total.value = res.data.data.total || 0;
    } else {
      ElMessage.error(res.data.message || '加载失败');
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

const resetQuery = () => {
  queryForm.keyword = '';
  queryForm.roleId = '';
  queryForm.status = '';
  queryForm.pageNum = 1;
  queryForm.pageSize = 10;
  loadUsers();
}

const handlePageChange = (page) => {
  queryForm.pageNum = page;
  loadUsers();
}

const handleSizeChange = (size) => {
  queryForm.pageNum = 1;
  queryForm.pageSize = size;
  loadUsers();
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  editId.value = ''
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  editId.value = row.userId
  form.userName = row.userName
  form.loginName = row.loginName
  form.password = ''
  form.userOrgId = row.userOrgId || ''
  form.rootOrgId = row.rootOrgId || rootOrgMap.value[row.userOrgId] || ''
  form.userRoleId = row.userRoleId || ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.expireDate = row.expireDate || ''
  form.status = row.status || 'y'
  rootOrgName.value = form.rootOrgId ? (orgTreeOptions.value.find(node => node.orgId === form.rootOrgId)?.orgName || '') : ''
  dialogVisible.value = true
}

const handleOrgChange = (orgId) => {
  form.rootOrgId = rootOrgMap.value[orgId] || ''
  rootOrgName.value = orgId ? (orgTreeOptions.value.find(node => node.orgId === form.rootOrgId)?.orgName || '') : ''
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    if (!isSysAdmin && form.rootOrgId !== currentRootOrgId) {
      ElMessage.error('只能选择登录用户根组织及其下级组织')
      return
    }
    submitLoading.value = true
    try {
      const payload = {
        ...form,
        expireDate: form.expireDate || new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      const res = isEdit.value ? await updateUser(editId.value, payload) : await createUser(payload)
      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadUsers()
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
  await ElMessageBox.confirm(`确定${actionText}用户【${row.userName}】吗？`, '提示', {
    type: 'warning'
  })
  try {
    const res = await updateUserStatus(row.userId, nextStatus)
    if (res.data.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadUsers()
    } else {
      ElMessage.error(res.data.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || `${actionText}失败`)
    }
  }
}

const loadOptions = async () => {
  const [roleRes, orgRes] = await Promise.all([
    fetchDictionaryItems('data_role'),
    fetchOrgTree()
  ])
  if (roleRes.data.code === 200) {
    roleOptions.value = (roleRes.data.data || []).filter(item => item.status === 'y')
  }
  if (orgRes.data.code === 200) {
    const buildRootMap = (nodes, rootId = '') => {
      nodes.forEach((node) => {
        const resolvedRootId = rootId || node.orgId
        rootOrgMap.value[node.orgId] = resolvedRootId
        if (node.children && node.children.length) {
          buildRootMap(node.children, resolvedRootId)
        }
      })
    }

    rootOrgMap.value = {}
    const sourceTree = orgRes.data.data || []
    buildRootMap(sourceTree)
    orgTreeOptions.value = sourceTree
  }
}

const formatUserRoleNames = (row) => {
  const roleNames = []
  if (Array.isArray(row.roleNames) && row.roleNames.length) {
    roleNames.push(...row.roleNames)
  }
  if (Array.isArray(row.userRoleNames) && row.userRoleNames.length) {
    roleNames.push(...row.userRoleNames)
  }
  if (typeof row.authRoleNames === 'string' && row.authRoleNames.trim()) {
    roleNames.push(...row.authRoleNames.split(';').map(name => name.trim()).filter(Boolean))
  }
  if (typeof row.userRoleName === 'string' && row.userRoleName.trim()) {
    roleNames.push(...row.userRoleName.split(';').map(name => name.trim()).filter(Boolean))
  }
  return [...new Set(roleNames)].join(';')
}

const openAuthDialog = async (row) => {
  currentUserName.value = row.userName
  selectedRoleNames.value = formatUserRoleNames(row)
  authDialogVisible.value = true
  const authRes = await fetchUserRoles(row.userId)
  if (authRes.data.code === 200) {
    selectedRoleIds.value = authRes.data.data.roleIds || []
    editId.value = row.userId
  }
}

const handleSaveAuth = async () => {
  authLoading.value = true
  try {
    const res = await saveUserRoles(editId.value, selectedRoleIds.value)
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

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除用户【${row.userName}】吗？`, '提示', {
    type: 'warning'
  })
  try {
    const res = await deleteUser(row.userId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadOptions()])
})
</script>
