<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">实验模拟器</span>
          </div>
        </div>
      </template>

      <div class="simulator-toolbar">
        <div class="simulator-toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索模拟器名称/说明" clearable class="simulator-search" @keyup.enter="loadItems" />
          <el-select v-model="query.status" placeholder="状态" clearable class="simulator-select">
            <el-option label="启用" value="y" />
            <el-option label="停用" value="n" />
          </el-select>
          <el-button @click="loadItems">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <div class="simulator-toolbar-right">
          <el-button type="primary" @click="openCreateDialog">新增模拟器</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="coverImageUrl" label="封面图片" min-width="160" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverImageUrl"
              :src="withFilePrefix(scope.row.coverImageUrl)"
              fit="cover"
              style="width: 72px; height: 48px; border-radius: 6px"
            />

            <span v-else>-</span>
            <!-- :preview-src-list="[withFilePrefix(scope.row.coverImageUrl)]" -->
          </template>
        </el-table-column>
        <el-table-column prop="simulatorName" label="模拟器名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="subjectId" label="学科" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ getSubjectName(scope.row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="simulatorUrl" label="模拟器URL" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <el-button link type="success" @click="openSimulator(scope.row)">打开模拟器</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="comments" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createUserName" label="创建人" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 'y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
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
          <div class="pagination-summary">当前每页 {{ query.pageSize }} 条，共 {{ total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="共 {total} 条, sizes, prev, pager, next, jumper"
            :total="total"
            :current-page="query.pageNum"
            :page-size="query.pageSize"
            :page-sizes="pageSizes"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" class="user-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="user-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="模拟器名称" prop="simulatorName">
              <el-input v-model="form.simulatorName" placeholder="请输入模拟器名称" :maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学科" prop="subjectId">
              <el-select v-model="form.subjectId" placeholder="请选择学科" filterable clearable style="width: 100%">
                <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="封面图片" prop="coverImageUrl">
              <div class="cover-field">
                <el-upload
                  class="cover-upload"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  accept="image/*"
                  :before-upload="beforeCoverUpload"
                  :on-change="handleCoverSelected"
                >
                  <el-button type="primary">本地上传</el-button>
                </el-upload>
                <el-button v-if="form.coverImageUrl" @click="clearCoverImage">清空封面</el-button>
                <span class="cover-tip">仅支持上传图片，上传后自动保存为图片地址</span>
              </div>
              <div v-if="form.coverImageUrl" class="cover-preview">
                <img :src="withFilePrefix(form.coverImageUrl)" alt="封面预览" />
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="模拟器URL" prop="simulatorUrl">
              <div class="simulator-url-field">
                <el-input  :maxlength="150"
                  v-model="form.simulatorUrl"
                  placeholder="请输入以 http/https 开头的地址，或上传 html/htm 文件后自动生成"
                />
                <el-upload
                  class="simulator-upload"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  accept=".html,.htm,text/html,application/xhtml+xml"
                  :before-upload="beforeSimulatorUpload"
                  :on-change="handleSimulatorSelected"
                >
                  <el-button type="primary">上传HTML</el-button>
                </el-upload>
                <el-button v-if="form.simulatorUrl" @click="clearSimulatorUrl">清空</el-button>
              </div>
              <div class="simulator-url-tip">支持直接输入 http/https 开头的地址，或上传 html/htm 文件。</div>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="说明" prop="comments">
              <el-input v-model="form.comments" type="textarea" :rows="3" placeholder="请输入说明" :maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="y">启用</el-radio>
                <el-radio label="n">停用</el-radio>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createExpSimulator,
  deleteExpSimulator,
  fetchDataDictItems,
  fetchExpSimulators,
  recordExpSimulatorLog,
  updateExpSimulator,
  uploadFile as uploadSimulatorFile
} from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增模拟器')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const total = ref(0)
const tableData = ref([])
const pageSizes = [10, 20, 30, 40, 50]
const subjectOptions = ref([])

const fileUrlPrefix = (import.meta.env.VITE_File_URL_PREFIX || '').trim()

const query = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 10 })
const form = reactive({ simulatorName: '', subjectId: '', coverImageUrl: '', simulatorUrl: '', comments: '', status: 'y' })

const isHttpUrl = (value) => /^https?:\/\//i.test(String(value || '').trim())
const isHtmlFile = (value) => {
  const v = String(value || '').trim().toLowerCase()
  return v.endsWith('.html') || v.endsWith('.htm') || v.startsWith('data:text/html') || v.startsWith('data:application/xhtml+xml')
}
const isHtmlUrl = (value) => isHttpUrl(value) || isHtmlFile(value) || /^\/uploads\//i.test(String(value || '').trim())

const normalizeUrl = (value) => {
  const v = String(value || '').trim()
  if (!v) return ''
  if (/^(https?:)?\/\//i.test(v) || /^data:/i.test(v)) return v
  if (!fileUrlPrefix) return v
  const prefix = fileUrlPrefix.endsWith('/') ? fileUrlPrefix.slice(0, -1) : fileUrlPrefix
  const suffix = v.startsWith('/') ? v : `/${v}`
  return `${prefix}${suffix}`
}

const displaySimulatorUrl = (value) => normalizeUrl(value)
const withFilePrefix = (value) => normalizeUrl(value)

const validateSimulatorUrlPattern = (value) => {
  const v = String(value || '').trim()
  return isHttpUrl(v) || isHtmlFile(v) || /^\/uploads\//i.test(v) || /^data:/i.test(v)
}

const validateSimulatorUrl = (_rule, value, callback) => {
  const v = String(value || '').trim()
  if (!v) return callback(new Error('请输入模拟器URL'))
  if (validateSimulatorUrlPattern(v)) return callback()
  callback(new Error('请输入以 http/https 开头的地址，或上传 html/htm 文件'))
}

const rules = {
  simulatorName: [{ required: true, message: '请输入模拟器名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  coverImageUrl: [{ required: true, message: '请上传封面图片', trigger: 'change' }],
  simulatorUrl: [
    { required: true, message: '请输入模拟器URL', trigger: 'blur' },
    { validator: validateSimulatorUrl, trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const resetForm = () => {
  form.simulatorName = ''
  form.subjectId = ''
  form.coverImageUrl = ''
  form.simulatorUrl = ''
  form.comments = ''
  form.status = 'y'
}

const clearCoverImage = () => {
  form.coverImageUrl = ''
}

const clearSimulatorUrl = () => {
  form.simulatorUrl = ''
}

const beforeCoverUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return false
  }
  return true
}

const beforeSimulatorUpload = (file) => {
  const name = (file.name || '').toLowerCase()
  const ok = name.endsWith('.html') || name.endsWith('.htm') || file.type === 'text/html' || file.type === 'application/xhtml+xml'
  if (!ok) {
    ElMessage.error('请选择 html 或 htm 文件')
    return false
  }
  return true
}

const normalizeOptions = (rows, valueKey, labelKey) => (rows || [])
  .filter(item => item?.[valueKey] != null && item?.[labelKey] != null)
  .map(item => ({ value: String(item[valueKey]), label: item[labelKey] }))

const loadSubjects = async () => {
  try {
    const res = await fetchDataDictItems('data_school_subject')
    if (res.data.code === 200) {
      subjectOptions.value = normalizeOptions(res.data.data || [], 'subject_id', 'subject_name')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载学科失败')
  }
}

const getSubjectName = (subjectId) => subjectOptions.value.find(item => item.value === String(subjectId))?.label || subjectId || '-'

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchExpSimulators(query)
    if (res.data.code === 200) {
      tableData.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
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
  query.keyword = ''
  query.status = ''
  query.pageNum = 1
  query.pageSize = 10
  loadItems()
}

const handlePageChange = (page) => {
  query.pageNum = page
  loadItems()
}

const handleSizeChange = (size) => {
  query.pageSize = size
  query.pageNum = 1
  loadItems()
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = '新增模拟器'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.simulatorId
  dialogTitle.value = '编辑模拟器'
  form.simulatorName = row.simulatorName || ''
  form.subjectId = row.subjectId != null ? String(row.subjectId) : ''
  form.coverImageUrl = row.coverImageUrl || ''
  form.simulatorUrl = row.simulatorUrl || ''
  form.comments = row.comments || ''
  form.status = row.status || 'y'
  dialogVisible.value = true
}

const subjectLabel = computed(() => subjectOptions.value.find(item => item.value === form.subjectId)?.label || '')

const handleSubmit = async () => {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        ...form,
        coverImageUrl: normalizeUrl(form.coverImageUrl),
        simulatorUrl: form.simulatorUrl,
        subjectName: subjectLabel.value
      }
      const res = isEdit.value ? await updateExpSimulator(editId.value, payload) : await createExpSimulator(payload)
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

const handleCoverSelected = async (uploadFile) => {
  const rawFile = uploadFile.raw
  if (!rawFile) return
  submitLoading.value = true
  try {
    const res = await uploadSimulatorFile(rawFile)
    if (res.data.code === 200) {
      const data = res.data.data || {}
      form.coverImageUrl = data.fileUrl || data.url || data.path || ''
      if (!form.coverImageUrl) {
        ElMessage.error('上传成功，但未返回图片地址')
      }
    } else {
      ElMessage.error(res.data.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '上传失败')
  } finally {
    submitLoading.value = false
  }
}

const handleSimulatorSelected = async (uploadFile) => {
  const rawFile = uploadFile.raw
  if (!rawFile) return
  submitLoading.value = true
  try {
    const res = await uploadSimulatorFile(rawFile)
    if (res.data.code === 200) {
      const data = res.data.data || {}
      const fileUrl = data.fileUrl || data.url || data.path || ''
      form.simulatorUrl = fileUrl ? normalizeUrl(fileUrl) : ''
      if (!form.simulatorUrl) {
        ElMessage.error('上传成功，但未返回文件地址')
      }
    } else {
      ElMessage.error(res.data.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '上传失败')
  } finally {
    submitLoading.value = false
  }
}

const openSimulator = async (row) => {
  const url = normalizeUrl(row.simulatorUrl)
  if (!validateSimulatorUrlPattern(url)) {
    ElMessage.warning('模拟器URL无效')
    return
  }
  try {
    await recordExpSimulatorLog(row.simulatorId)
  } catch (error) {
    ElMessage.warning(error?.response?.data?.message || '访问日志记录失败，但不影响打开模拟器')
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除模拟器【${row.simulatorName}】吗？`, '提示', { type: 'warning' })
  try {
    const res = await deleteExpSimulator(row.simulatorId)
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

onMounted(async () => {
  await loadSubjects()
  await loadItems()
})
</script>

<style scoped>
.simulator-toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:12px}.simulator-toolbar-left,.simulator-toolbar-right{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.simulator-search{width:260px}.simulator-select{width:140px}.simulator-url-link{color:#409eff;cursor:pointer;text-decoration:underline}.cover-field{display:flex;align-items:center;gap:12px;width:100%;flex-wrap:wrap}.cover-upload{flex-shrink:0}.cover-tip,.simulator-url-tip{color:#909399;font-size:12px}.cover-preview{margin-top:10px;width:180px;height:108px;border:1px solid #ebeef5;border-radius:8px;overflow:hidden;background:#fafafa}.cover-preview img{width:100%;height:100%;object-fit:cover;display:block}.simulator-url-field{display:flex;align-items:center;gap:12px;width:100%;flex-wrap:wrap}.simulator-url-field :deep(.el-input){flex:1}.simulator-upload{flex-shrink:0}@media (max-width:768px){.simulator-search,.simulator-select{width:100%}.simulator-toolbar,.simulator-toolbar-left,.simulator-toolbar-right{width:100%}.cover-field,.simulator-url-field{flex-direction:column;align-items:stretch}.cover-upload,.simulator-upload{width:100%}.cover-upload :deep(.el-button),.simulator-upload :deep(.el-button){width:100%}.cover-preview{width:100%}}
</style>
