<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 组织管理</span>
          </div>
        </div>
      </template>

      <div class="org-toolbar">
        <div class="org-actions">
          <el-button v-if="isSysAdmin" type="primary" @click="openCreateDialog">新增组织</el-button>
          <el-button :disabled="!currentNode" @click="openChildCreateDialog">新增子组织</el-button>
          <el-button :disabled="!currentNode" @click="openEditDialog">编辑组织</el-button>
          <el-button type="danger" :disabled="!currentNode" @click="handleDelete">删除组织</el-button>
        </div>
        <div class="org-tree-wrap">
          <el-tree
            ref="treeRef"
            :data="treeData"
            node-key="orgId"
            :props="treeProps"
            highlight-current
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ data }">
              <span class="org-node">
                <span>{{ data.orgName }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="组织名称" prop="orgName">
          <el-input v-model="form.orgName" placeholder="请输入组织名称" :maxlength="25" />
        </el-form-item>
        <el-form-item label="组织类型" prop="orgTypeId">
          <el-select v-model="form.orgTypeId" placeholder="请选择组织类型" filterable style="width: 100%">
            <el-option
              v-for="item in orgTypeOptions"
              :key="item.orgTypeId"
              :label="item.orgTypeName"
              :value="item.orgTypeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父组织" prop="parentOrgId">
          <el-select
            v-model="form.parentOrgId"
            placeholder="根组织可留空"
            filterable
            clearable
            style="width: 100%"
            :disabled="Boolean(currentParentId) && !isEdit"
          >
            <el-option label="根组织" value="" />
            <el-option
              v-for="item in parentOptions"
              :key="item.orgId"
              :label="item.orgName"
              :value="item.orgId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物化路径" prop="orgPath">
          <el-input v-model="form.orgPath" placeholder="例如 /root/school1/class1" :maxlength="200"  />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="y">启用</el-radio>
            <el-radio label="n">停用</el-radio>
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
import { createOrg, deleteOrg, fetchDictionaryItems, fetchOrgTree, updateOrg } from '../../api/index'

const treeRef = ref()
const treeData = ref([])
const orgTypeOptions = ref([])
const parentOptions = ref([])
const currentNode = ref(null)
const currentParentId = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增组织')
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const isSysAdmin = currentUserInfo.userRoleId === 'Sys_Admin'
const currentRootOrgId = currentUserInfo.rootOrgId || ''

const treeProps = { children: 'children', label: 'orgName' }

const form = reactive({
  orgName: '',
  orgTypeId: '',
  parentOrgId: '',
  orgPath: '',
  status: 'y',
  sortOrder: 0
})

const rules = computed(() => ({
  orgName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  orgTypeId: [{ required: true, message: '请选择组织类型', trigger: 'change' }]
}))

const getNextSortOrder = (parentOrgId = '') => {
  const parentNode = parentOrgId ? findOrgNode(parentOrgId) : null
  const siblings = parentNode?.children || treeData.value || []
  const maxSortOrder = siblings.reduce((max, item) => {
    const value = Number(item?.sortOrder ?? 0)
    return Number.isFinite(value) && value > max ? value : max
  }, 0)
  return maxSortOrder + 1
}

const resetForm = () => {
  form.orgName = ''
  form.orgTypeId = ''
  form.parentOrgId = currentParentId.value || ''
  form.orgPath = ''
  form.status = 'y'
  form.sortOrder = getNextSortOrder(form.parentOrgId)
}

const loadTree = async () => {
  const res = await fetchOrgTree()
  if (res.data.code === 200) {
    treeData.value = res.data.data || []
  }
}

const loadOrgTypes = async () => {
  const res = await fetchDictionaryItems('data_org_type')
  if (res.data.code === 200) {
    orgTypeOptions.value = (res.data.data || []).filter(item => item.status === 'y')
  }
}

const loadParentOptions = async () => {
  const res = await fetchOrgTree()
  if (res.data.code === 200) {
    const flat = []
    const walk = (nodes) => {
      nodes.forEach((node) => {
        flat.push({ orgId: node.orgId, orgName: node.orgName })
        if (node.children && node.children.length) {
          walk(node.children)
        }
      })
    }
    walk(res.data.data || [])
    parentOptions.value = flat
  }
}

const handleNodeClick = (node) => {
  currentNode.value = node
  currentParentId.value = node.orgId
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增组织'
  currentParentId.value = ''
  currentNode.value = null
  resetForm()
  dialogVisible.value = true
}

const openChildCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增子组织'
  const parentOrgId = currentNode.value?.orgId || ''
  currentParentId.value = parentOrgId
  resetForm()
  form.parentOrgId = parentOrgId
  form.sortOrder = getNextSortOrder(parentOrgId)
  dialogVisible.value = true
}

const openEditDialog = () => {
  if (!currentNode.value) return
  isEdit.value = true
  dialogTitle.value = '编辑组织'
  form.orgName = currentNode.value.orgName
  form.orgTypeId = currentNode.value.orgTypeId || ''
  form.parentOrgId = currentNode.value.parentOrgId || ''
  form.orgPath = currentNode.value.orgPath || ''
  form.status = currentNode.value.status || 'y'
  form.sortOrder = currentNode.value.sortOrder ?? 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form }
      const res = isEdit.value
        ? await updateOrg(currentNode.value.orgId, payload)
        : await createOrg(payload)
      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        await loadTree()
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

const handleDelete = async () => {
  if (!currentNode.value) return
  await ElMessageBox.confirm(`确定删除组织【${currentNode.value.orgName}】吗？`, '提示', { type: 'warning' })
  try {
    const res = await deleteOrg(currentNode.value.orgId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      currentNode.value = null
      currentParentId.value = ''
      await loadTree()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const findParentNode = (parentOrgId) => {
  if (!parentOrgId) return null
  const stack = [...treeData.value]
  while (stack.length) {
    const node = stack.shift()
    if (node.orgId === parentOrgId) {
      return node
    }
    if (node.children && node.children.length) {
      stack.push(...node.children)
    }
  }
  return null
}

const findOrgNode = (orgId) => {
  if (!orgId) return null
  const stack = [...treeData.value]
  while (stack.length) {
    const node = stack.shift()
    if (node.orgId === orgId) return node
    if (node.children && node.children.length) stack.push(...node.children)
  }
  return null
}

onMounted(async () => {
  await Promise.all([loadTree(), loadOrgTypes(), loadParentOptions()])
})
</script>
