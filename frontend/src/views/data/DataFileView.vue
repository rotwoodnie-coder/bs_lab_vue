<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">数据维护 > 个人素材</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="文件名称/关键字/后缀" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 100px">
              <el-option label="启用" value="y" />
              <el-option label="停用" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item label="素材类型">
            <el-select v-model="queryForm.fileTypeId" placeholder="全部类型" clearable filterable style="width: 150px">
              <el-option
                v-for="item in fileTypeOptions"
                :key="item.typeId"
                :label="item.typeName"
                :value="item.typeId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="是否公开">
            <el-select v-model="queryForm.isPublic" placeholder="全部" clearable style="width: 100px">
              <el-option label="公开" value="y" />
              <el-option label="待公开" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="dict-toolbar-buttons">
          <el-button type="primary" @click="openCreateDialog">新增素材资源</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column label="封面" width="90" align="center">
          <template #default="scope">
            <el-image v-if="getDisplayUrl(scope.row.coverImagePreviewUrl)" :src="getDisplayUrl(scope.row.coverImagePreviewUrl)" fit="contain" class="data-file-cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="fileTag" label="关键字" min-width="150" show-overflow-tooltip />
        <el-table-column label="文件类型" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ getFileTypeName(scope.row.fileTypeId) }}</template>
        </el-table-column>
        <!--
        <el-table-column prop="fileExt" label="后缀" width="90" align="center" />
        <el-table-column prop="fileSize" label="大小" width="100" align="center">
          <template #default="scope">{{ formatSize(scope.row.fileSize) }}</template>
        </el-table-column>
        -->

        <el-table-column label="公开状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="isPublicItem(scope.row) ? 'success' : 'info'" effect="light">
              {{ isPublicItem(scope.row) ? '公开' : '待公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" :disabled="!getDisplayUrl(scope.row.fileUrl)" @click="previewFile(scope.row)">预览</el-button>
            <!--
            <el-button link type="success" :disabled="!getDisplayUrl(scope.row.fileUrl)" @click="downloadFile(scope.row)">下载</el-button>
            -->
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 'y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerUserId" label="创建人" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" :disabled="isPublicItem(scope.row)" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" :disabled="isPublicItem(scope.row)" @click="handleDelete(scope.row)">删除</el-button>
            <el-button link type="primary" @click="openLogDialog(scope.row)">操作日志</el-button>
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

    <el-dialog v-model="logDialogVisible" title="操作日志详情" width="560px" class="user-dialog" @closed="handleLogDialogClosed">
      <el-table :data="logTableData" border stripe v-loading="logLoading" class="user-table">
        <el-table-column prop="logTime" label="日志时间" width="180" show-overflow-tooltip>
          <template #default="scope">{{ formatLogTime(scope.row.logTime) }}</template>
        </el-table-column>
        <el-table-column prop="logTypeName" label="操作类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="logUserName" label="操作人" min-width="120" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="960px" class="user-dialog" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="user-form">
        <div class="data-file-form-layout">
          <div class="data-file-form-left">
            <div class="data-file-form-section-title">封面图片</div>
            <MinioUploader
              :key="coverUploaderKey"
              v-model="form.coverImageUrl"
              v-model:file-name="form.coverImageName"
              :preview-url="form.coverImagePreviewUrl"
              accept=".png,.jpg,.jpeg,.webp,.gif"
              button-text="上传封面"
              @uploaded="handleCoverUploaded"
            />
          </div>

          <div class="data-file-form-right">
            <el-row :gutter="12">
              <el-col :span="24">
                <el-form-item label="文件名称" prop="fileName">
                  <el-input v-model="form.fileName" placeholder="请输入文件名称" :maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="文件关键字" prop="fileTag">
                  <el-input v-model="form.fileTag" placeholder="请输入文件关键字，方便查询" :maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="文件类型" prop="fileTypeId">
                  <el-select v-model="form.fileTypeId" placeholder="请选择文件类型" style="width: 100%" filterable clearable>
                    <el-option
                      v-for="item in fileTypeOptions"
                      :key="item.typeId"
                      :label="item.typeName"
                      :value="item.typeId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="状态" prop="status">
                  <input type="hidden" id="form.isPublic" :value="form.isPublic" />
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
          </div>
        </div>

        <div class="data-file-material-section">
          <div class="data-file-form-section-title">素材文件</div>
          <div class="data-file-material-row">
            <div class="data-file-material-upload-wrap">
              <MinioUploader
                :key="fileUploaderKey"
                v-model="form.fileUrl"
                v-model:file-name="form.fileName"
                :preview-url="form.previewUrl"
                :accept="acceptExt"
                button-text="上传文件"
                @uploaded="handleFileUploaded"
                @update:fileName="handleMaterialFileNameUpdate"
              />
            </div>
            <div v-if="getDisplayUrl(form.previewUrl)" class="data-file-form-preview">
              <el-button
                class="data-file-preview-button"
                type="primary"
                plain
                @click="previewFile({ previewUrl: form.previewUrl})"
              >
                文件预览
              </el-button>
            </div>
          </div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MinioUploader from '../../components/MinioUploader.vue'
import { createDataFile, deleteDataFile, fetchDataFileLogs, fetchDataFileTypes, fetchMyDataFiles, updateDataFile } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const logDialogVisible = ref(false)
const logLoading = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增素材资源')
const formRef = ref()
const editId = ref('')
const pageSizes = [10, 20, 50, 100]
const coverUploaderKey = ref(0)
const fileUploaderKey = ref(0)
const tableData = ref([])
const logTableData = ref([])
const total = ref(0)
const queryForm = reactive({ keyword: '', status: '', isPublic: '', fileTypeId: '', pageNum: 1, pageSize: 10 })
const urlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''

const form = reactive({
  fileId: '',
  fileName: '',
  fileTag: '',
  fileUrl: '',
  fileTypeId: '',
  status: 'y',
  ownerUserId: '',
  coverImageUrl: '',
  coverImagePreviewUrl: '',
  previewUrl: '',
  fileSize: null,
  fileExt: '',
  isPublic: 'n',
  comments: '',
  coverImageName: ''
})

const fileTypeOptions = ref([])
const fileTypeNameMap = computed(() => Object.fromEntries(fileTypeOptions.value.map(item => [item.typeId, item.typeName])))
const defaultFileTypeNameByExt = {
  png: 'Image',
  jpg: 'Image',
  jpeg: 'Image',
  webp: 'Image',
  gif: 'Image',
  bmp: 'Image',
  svg: 'Image',
  mp4: 'Video',
  mov: 'Video',
  avi: 'Video',
  mkv: 'Video',
  webm: 'Video',
  mp3: 'Audio',
  wav: 'Audio',
  flac: 'Audio',
  aac: 'Audio',
  pdf: 'PDF',
  doc: 'Word',
  docx: 'Word',
  ppt: 'PPT',
  pptx: 'PPT',
  xls: 'Excel',
  xlsx: 'Excel'
}
const acceptExt = computed(() => '.png,.jpg,.jpeg,.webp,.gif,.bmp,.svg,.mp4,.mov,.avi,.mkv,.webm,.mp3,.wav,.flac,.aac,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx')

const rules = {
  fileName: [{ required: true, message: '请输入文件名称', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请上传文件', trigger: 'change' }],
  coverImageUrl: [{ required: true, message: '请上传封面图片', trigger: 'change' }],
  fileTypeId: [{ required: true, message: '请选择文件类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  isPublic: [{ required: true, message: '请选择是否公开', trigger: 'change' }]
}

const resetForm = () => {
  form.fileId = ''
  form.fileName = ''
  form.fileTag = ''
  form.fileUrl = ''
  form.fileTypeId = ''
  form.status = 'y'
  form.ownerUserId = ''
  form.coverImageUrl = ''
  form.coverImagePreviewUrl = ''
  form.previewUrl = ''
  form.fileSize = null
  form.fileExt = ''
  form.isPublic = 'n'
  form.comments = ''
  form.coverImageName = ''
}

const resetUploaderState = () => {
  coverUploaderKey.value += 1
  fileUploaderKey.value += 1
}

const initDialogForm = (row = null) => {
  resetForm()
  if (!row) {
    resetUploaderState()
    return
  }

  form.fileId = row.fileId || ''
  form.fileName = row.fileName || ''
  form.fileTag = row.fileTag || ''
  form.fileUrl = row.fileUrl || ''
  form.fileTypeId = row.fileTypeId || ''
  form.status = row.status || 'y'
  form.ownerUserId = row.ownerUserId || ''
  form.coverImageUrl = row.coverImageUrl || ''
  form.coverImagePreviewUrl = getDisplayUrl(row.coverImagePreviewUrl || row.coverImageUrl || '')
  form.fileSize = row.fileSize ?? null
  form.fileExt = row.fileExt || getExtFromFileName(row.fileName || row.fileUrl)
  form.isPublic = row.isPublic || 'n'
  form.comments = row.comments || ''
  form.coverImageName = ''
  form.previewUrl = getDisplayUrl(row.previewUrl || row.fileUrl || '')

  syncFileTypeByFileName(row.fileName || row.fileUrl)
  if (row.fileTypeId) form.fileTypeId = row.fileTypeId
  resetUploaderState()
}

const getExtFromFileName = (fileName) => {
  const name = String(fileName || '').trim()
  if (!name || !name.includes('.')) return ''
  return name.split('.').pop().toLowerCase()
}

const syncFileTypeByExt = (ext) => {
  const normalizedExt = String(ext || '').trim().toLowerCase()
  if (!normalizedExt) return
  const matchedFileTypeName = defaultFileTypeNameByExt[normalizedExt]
  if (!matchedFileTypeName) return
  const matchedOption = fileTypeOptions.value.find(item => String(item.typeId || '').trim() === matchedFileTypeName)
  if (matchedOption?.typeId) {
    form.fileTypeId = matchedOption.typeId
  }
}

const syncFileTypeByFileName = (fileName) => {
  const ext = getExtFromFileName(fileName)
  form.fileExt = ext
  syncFileTypeByExt(ext)
}

const loadFileTypes = async () => {
  try {
    const res = await fetchDataFileTypes()
    if (res.data.code === 200) {
      fileTypeOptions.value = (Array.isArray(res.data.data) ? res.data.data : []).filter(item => item && item.typeId && item.typeName)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载文件类型失败')
  }
}

const getFileTypeName = (fileTypeId) => fileTypeNameMap.value[fileTypeId] || fileTypeId || '-'
const isPublicItem = (row) => String(row?.isPublic || '').toLowerCase() === 'y'
const isImageFile = (url, fileName) => /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(String(fileName || url || ''))
const getDisplayUrl = (url) => {
  if (!url) return ''
  const raw = String(url).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${urlPrefix}${raw}`
  return `${urlPrefix}/${raw}`
}

const previewFile = (row) => {
  const url = getDisplayUrl(row.previewUrl)
  if (!url) return
  window.open(url, '_blank', 'noopener,noreferrer')
}


const downloadFile = (row) => {
  const url = getDisplayUrl(row.fileUrl)
  if (!url) return
  const link = document.createElement('a')
  link.href = url
  link.download = row.fileName || ''
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchMyDataFiles({
      keyword: queryForm.keyword,
      status: queryForm.status,
      isPublic: queryForm.isPublic,
      fileTypeId: queryForm.fileTypeId,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    if (res.data.code === 200) {
      const result = res.data.data || {}
      tableData.value = result.records || []
      total.value = result.total || 0
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
  queryForm.isPublic = ''
  queryForm.fileTypeId = ''
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  loadItems()
}

const handlePageChange = (page) => {
  queryForm.pageNum = page
  loadItems()
}

const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadItems()
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = ''
  dialogTitle.value = '新增素材资源'
  initDialogForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.fileId
  dialogTitle.value = '编辑素材资源'
  initDialogForm(row)
  dialogVisible.value = true
}

const handleDialogClosed = () => {
  resetForm()
  formRef.value?.clearValidate?.()
}

const handleCoverUploaded = (payload) => {
  form.coverImageUrl = payload?.fileUrl || ''
  form.coverImagePreviewUrl = payload?.previewUrl || payload?.fileUrl || ''
  form.coverImageName = payload?.fileName || ''
}

const handleFileUploaded = (payload) => {
  const fileName = payload?.fileName || ''
  const fileUrl = payload?.fileUrl || ''
  const previewUrl = payload?.previewUrl || fileUrl
  const resolvedFileName = String(fileName || '').trim() || String(fileUrl || '').split('/').pop() || ''

  form.fileName = resolvedFileName
  form.fileUrl = fileUrl
  form.previewUrl = previewUrl
  syncFileTypeByFileName(resolvedFileName)
}

const handleMaterialFileNameUpdate = (fileName) => {
  form.fileName = fileName || ''
  syncFileTypeByFileName(fileName)
}

const buildPayload = () => ({
  ...form,
  fileId: String(form.fileId || '').trim(),
  fileExt: form.fileExt || getExtFromFileName(form.fileName)
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (!form.coverImageUrl) {
      ElMessage.warning('请上传封面图片')
      return
    }
    if (!form.fileUrl) {
      ElMessage.warning('请上传文件')
      return
    }
    if (!form.fileTypeId) {
      ElMessage.warning('请选择文件类型')
      return
    }
    submitLoading.value = true
    const payload = buildPayload()
    const res = isEdit.value ? await updateDataFile(editId.value, payload) : await createDataFile(payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      form.coverImagePreviewUrl=''
      form.filePreviewUrl=''
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
    await ElMessageBox.confirm(`确定删除素材资源【${row.fileName}】吗？`, '提示', { type: 'warning' })
    const res = await deleteDataFile(row.fileId)
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

const formatSize = (size) => {
  if (size == null || Number.isNaN(Number(size))) return '-'
  const num = Number(size)
  if (num < 1024) return `${num} B`
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`
  if (num < 1024 * 1024 * 1024) return `${(num / 1024 / 1024).toFixed(1)} MB`
  return `${(num / 1024 / 1024 / 1024).toFixed(1)} GB`
}

const formatLogTime = (value) => {
  if (!value) return '-'
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const openLogDialog = async (row) => {
  logDialogVisible.value = true
  logLoading.value = true
  logTableData.value = []
  try {
    const res = await fetchDataFileLogs({ fileId: row.fileId, pageNum: 1, pageSize: 100000 })
    if (res.data.code === 200) {
      const result = res.data.data || {}
      const records = Array.isArray(result) ? result : (result.records || result.list || [])
      logTableData.value = records.sort((a, b) => new Date(b.logTime || 0) - new Date(a.logTime || 0))
    } else {
      ElMessage.error(res.data.message || '加载日志失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载日志失败')
  } finally {
    logLoading.value = false
  }
}

const handleLogDialogClosed = () => {
  logTableData.value = []
}

onMounted(async () => {
  await loadFileTypes()
  await loadItems()
})
</script>

<style scoped>
.data-file-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  background: #fafafa;
  border: 1px solid #ebeef5;
}

.data-file-cover :deep(img),
.data-file-cover :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.data-file-form-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.data-file-form-left {
  width: 300px;
  flex: 0 0 300px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
  box-sizing: border-box;
}

.data-file-form-right {
  flex: 1;
  min-width: 0;
}

.data-file-form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.data-file-material-section {
  margin-top: 18px;
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.data-file-material-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.data-file-material-upload-wrap {
  flex: 0 0 260px;
  width: 260px;
}

.data-file-form-preview {
  margin-top: 0;
  margin-bottom: 0;
  flex: 0 0 260px;
  width: 260px;
}

.data-file-cover-preview-wrap {
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 10px 0 0;
}

.data-file-cover-preview-image {
  width: 160px;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  background: #fafafa;
}

.data-file-cover-preview-image :deep(img),
.data-file-form-preview-image :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.data-file-cover-preview-image :deep(.el-image__inner),
.data-file-form-preview-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.data-file-form-preview-image {
  width: 240px;
  height: 140px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  background: #fafafa;
}

.data-file-preview-button {
  width: 240px;
  height: 140px;
  border-radius: 10px;
}

.data-file-preview-button :deep(span) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

@media (max-width: 960px) {
  .data-file-form-layout {
    flex-direction: column;
  }

  .data-file-form-left {
    width: 100%;
    flex-basis: auto;
  }
}
</style>
