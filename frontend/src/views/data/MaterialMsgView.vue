<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">数据维护 > 个人实验材料</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="材料名称/属性/分类/用途" clearable />
          </el-form-item>
        <el-form-item label="材料分类">
          <el-select v-model="queryForm.materialTypeId" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in materialTypeOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 100px">
              <el-option label="启用" value="y" />
              <el-option label="停用" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否公开">
            <el-select v-model="queryForm.isPublic" placeholder="全部" clearable style="width: 100px">
              <el-option label="已公开" value="y" />
              <el-option label="待公开" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="dict-toolbar-buttons">
          <el-button type="primary" @click="openCreateDialog">新增实验材料</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column label="主图片" width="100" align="center">
          <template #default="scope">
            <el-image v-if="getListImageUrl(scope.row.mainPicPreviewUrl)" :src="getListImageUrl(scope.row.mainPicPreviewUrl)" fit="contain" class="material-cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="材料名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="materialTypeId" label="材料分类" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ getTypeLabel(scope.row.materialTypeId) }}</template>
        </el-table-column>
        <el-table-column prop="materialPropId" label="材料属性" min-width="140" show-overflow-tooltip>
          <template #default="scope">{{ getPropLabel(scope.row.materialPropId) }}</template>
        </el-table-column>
        <el-table-column prop="materialNum" label="建议用量" min-width="120" show-overflow-tooltip />
        <el-table-column prop="expPurpose" label="实验用途" min-width="120" show-overflow-tooltip />
        <el-table-column label="公开状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isPublic === 'y' ? 'success' : 'info'" effect="light">
              {{ scope.row.isPublic === 'y' ? '已公开' : '待公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'y' ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 'y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="success" @click="openPicPreview(scope.row)">图片</el-button>
            <el-button link type="primary" :disabled="isPublicItem(scope.row)" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" :disabled="isPublicItem(scope.row)" @click="handleDelete(scope.row)">删除</el-button>           
            <el-button link type="primary" @click="openLogDialog(scope.row)">查看日志</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ queryForm.pageSize }} 条，共 {{ total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="960px" class="user-dialog" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="user-form">
        <div class="material-form-layout">
          <div class="material-form-left">
            <div class="material-form-section-title">图片管理</div>
            <div class="material-pic-actions">
              <el-button type="primary" @click="addPic">上传图片</el-button>
              <el-button type="primary" plain @click="openLibraryDialog">从素材库选择</el-button>
            </div>
            <div class="material-pic-grid">
              <div v-for="(pic, index) in form.pics" :key="pic.uid" class="material-pic-card" :class="{ 'is-main': form.mainPicIndex === index }">
                <div class="material-pic-card-top">
                  <el-tag size="small" :type="form.mainPicIndex === index ? 'success' : 'info'">{{ form.mainPicIndex === index ? '主图' : `图片 ${index + 1}` }}</el-tag>
                  <el-button link type="danger" :disabled="form.pics.length === 1" @click="removePic(index)">删除</el-button>
                </div>
                <MinioUploader
                  v-model="pic.url"
                  v-model:file-name="pic.name"
                  :preview-url="getDisplayUrl(pic.previewUrl || pic.url)"
                  accept=".png,.jpg,.jpeg,.webp,.gif"
                  button-text="上传图片"
                  @update:modelValue="handlePicUploaded(index, $event)"
                  @update:fileName="handlePicNameUpdated(index, $event)"
                  @uploaded="(data) => handlePicPreviewUpdated(index, data?.previewUrl || data?.fileUrl)"
                />
                <el-radio v-model="form.mainPicIndex" :label="index" class="material-main-radio">设为主图</el-radio>
              </div>
            </div>
            <div class="material-form-tip">请通过 MinIO 上传多张图片，或从素材库选择图片，并设置其中一张作为主图</div>
          </div>

          <div class="material-form-right">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="材料名称" prop="materialName">
                  <el-input v-model="form.materialName" placeholder="请输入材料名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="材料属性" prop="materialPropId">
                  <el-select v-model="form.materialPropId" placeholder="请选择材料属性" style="width: 100%" clearable filterable>
                    <el-option v-for="item in materialPropOptions" :key="item.id" :label="item.label" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="材料分类" prop="materialTypeId">
                  <el-select v-model="form.materialTypeId" placeholder="请选择材料分类" style="width: 100%" clearable filterable>
                    <el-option v-for="item in materialTypeOptions" :key="item.id" :label="item.label" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="建议用量" prop="materialNum">
                  <el-input v-model="form.materialNum" placeholder="如 500 毫升" :maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="实验用途" prop="expPurpose">
                  <el-input v-model="form.expPurpose" placeholder="请输入实验用途" :maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <input type="hidden" id="form.isPublic" :value="form.isPublic" />
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio value="y">启用</el-radio>
                    <el-radio value="n">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="安全说明" prop="securityComments">
                  <el-select
                    v-model="securityCommentValues"
                    placeholder="请选择或输入安全说明"
                    style="width: 100%"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    clearable
                    @change="handleSecurityCommentsChange"
                  >
                    <el-option
                      v-for="item in materialSecurityOptions"
                      :key="item.id"
                      :label="item.label"
                      :value="item.label"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="补充说明" prop="additionalComments">
                  <el-input v-model="form.additionalComments" type="textarea" :rows="3" placeholder="请输入补充说明" :maxlength="50" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="picPreviewVisible" title="图片预览" width="860px" class="user-dialog">
      <div v-if="previewPics.length" class="material-preview-grid">
        <div v-for="pic in previewPics" :key="pic.uid" class="material-preview-item">
          <el-image :src="getListImageUrl(pic.url)" fit="contain" class="material-preview-image" :preview-src-list="previewPics.map(item => getListImageUrl(item.url))" preview-teleported />
          <div class="material-preview-meta">
            <span class="material-preview-name">{{ pic.isMain ? '主图' : '图片' }}</span>
            <span class="material-preview-order">第 {{ pic.sortOrder }} 张</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无图片" />
    </el-dialog>

    <el-dialog v-model="logDialogVisible" title="日志详情" width="860px" class="user-dialog" @closed="handleLogDialogClosed">
      <el-table :data="logTableData" border stripe v-loading="logLoading" class="user-table">
        <el-table-column prop="logTime" label="日志时间" width="180" show-overflow-tooltip>
          <template #default="scope">{{ formatLogTime(scope.row.logTime) }}</template>
        </el-table-column>
        <el-table-column prop="logTypeName" label="日志类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="logUserName" label="操作人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="logType" label="类型编码" width="120" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog v-model="libraryDialogVisible" title="从素材库选择图片" width="1100px" class="user-dialog" @closed="handleLibraryDialogClosed">
      <div class="material-library-panel">
        <div class="material-library-toolbar">
          <el-input v-model="libraryQuery.keyword" placeholder="搜索文件名称" clearable style="width: 240px" @keyup.enter="loadLibraryFiles" @clear="loadLibraryFiles" />
          <el-button type="primary" @click="loadLibraryFiles">查询</el-button>
        </div>
        <div v-loading="libraryLoading" class="material-library-grid">
          <div v-for="item in libraryFiles" :key="item.fileId" class="material-library-card" :class="{ 'is-selected': selectedLibraryFileIds.includes(item.fileId) }" @click="toggleLibraryFile(item)">
            <div class="material-library-card__thumb">
              <el-image v-if="getDisplayUrl(item.coverImageUrl || item.fileUrl)" :src="getDisplayUrl(item.coverImagePreviewUrl || item.fileUrl)" fit="contain" class="material-library-card__image" />
              <div v-else class="material-library-empty">无图片</div>
            </div>
            <div class="material-library-card__body">
              <div class="material-library-card__name" :title="item.fileName || ''">{{ item.fileName || '未命名图片' }}</div>
              <div class="material-library-card__meta">{{ item.fileTypeName || '图片' }}</div>
            </div>
          </div>
        </div>
        <div class="material-library-actions">
          <div class="material-library-page-summary">共 {{ libraryQuery.total }} 条</div>
          <div class="material-library-page-control">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="libraryQuery.total"
              :current-page="libraryQuery.pageNum"
              :page-size="libraryQuery.pageSize"
              @current-change="handleLibraryPageChange"
              @size-change="handleLibrarySizeChange"
            />
          </div>
          <div class="material-library-action-buttons">
            <el-button @click="clearLibrarySelection">清空选择</el-button>
            <el-button type="primary" :disabled="!selectedLibraryFiles.length" @click="applyLibrarySelection">加入图片</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MinioUploader from '../../components/MinioUploader.vue'
import { createMaterialMsg, deleteMaterialMsg, fetchMaterialLogs, fetchMaterialMsg, fetchMaterialMsgsMy, updateMaterialMsg, fetchDataDictItems, fetchImageMaterials } from '../../api/index'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const picPreviewVisible = ref(false)
const logDialogVisible = ref(false)
const logLoading = ref(false)
const libraryDialogVisible = ref(false)
const dialogTitle = ref('新增实验材料')
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const tableData = ref([])
const logTableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const queryForm = reactive({ keyword: '', status: '', isPublic: '', materialPropId: '', materialTypeId: '', pageNum: 1, pageSize: 10 })
const materialPropOptions = ref([])
const materialTypeOptions = ref([])
const materialSecurityOptions = ref([])
const securityCommentValues = ref([])
const libraryLoading = ref(false)
const libraryFiles = ref([])
const selectedLibraryFileIds = ref([])
const selectedLibraryFiles = computed(() => libraryFiles.value.filter(item => selectedLibraryFileIds.value.includes(item.fileId)))
const libraryQuery = reactive({ keyword: '', pageNum: 1, pageSize: 12, total: 0 })
const propsMap = computed(() => Object.fromEntries(materialPropOptions.value.map(item => [item.id, item.label])))
const typesMap = computed(() => Object.fromEntries(materialTypeOptions.value.map(item => [item.id, item.label])))
const isPublicItem = (row) => String(row?.isPublic || '').toLowerCase() === 'y'
const createPicItem = (data = {}) => ({
  uid: data.uid || `pic-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  url: data.url || '',
  previewUrl: data.previewUrl || data.url || '',
  name: data.name || '',
  sortOrder: data.sortOrder || 0,
  isMain: Boolean(data.isMain)
})

const form = reactive({
  materialId: '',
  materialName: '',
  materialPropId: '',
  materialTypeId: '',
  materialNum: '',
  pics: [createPicItem()],
  mainPicIndex: 0,
  expPurpose: '',
  securityComments: '',
  additionalComments: '',
  status: 'y',
  isPublic: 'n'
})

const getPropLabel = (id) => propsMap.value[id] || id || '-'
const getTypeLabel = (id) => typesMap.value[id] || id || '-'
const getSecurityLabel = (id) => Object.fromEntries(materialSecurityOptions.value.map(item => [item.id, item.label]))[id] || id || '-'

const rules = {
  materialName: [{ required: true, message: '请输入材料名称', trigger: 'blur' }],
  materialPropId: [{ required: true, message: '请选择材料属性', trigger: 'change' }],
  materialTypeId: [{ required: true, message: '请选择材料分类', trigger: 'change' }],
  materialNum: [{ required: true, message: '请输入建议用量', trigger: 'blur' }],
  expPurpose: [{ required: true, message: '请输入实验用途', trigger: 'blur' }],
  securityComments: [{ required: true, message: '请输入安全说明', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  isPublic: [{ required: true, message: '请选择是否公开', trigger: 'change' }]
}

const previewSrcList = computed(() => form.pics.map(pic => getDisplayUrl(pic.url)).filter(Boolean))
const previewPics = ref([])

const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
const getDisplayUrl = (url) => {
  if (!url) return ''
  const raw = String(url).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
}

const getListImageUrl = (url) => getDisplayUrl(url)

const openLibraryDialog = async () => {
  libraryDialogVisible.value = true
  if (!libraryFiles.value.length) {
    await loadLibraryFiles()
  }
}

const loadLibraryFiles = async () => {
  libraryLoading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await fetchImageMaterials({
      keyword: libraryQuery.keyword,
      status: 'y',
      isPublic: 'y',
      currentUserId: userInfo.userId || '',
      pageNum: libraryQuery.pageNum,
      pageSize: libraryQuery.pageSize
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      const rows = Array.isArray(data) ? data : (data.records || data.list || [])
      libraryFiles.value = rows
      libraryQuery.total = Number(data.total || rows.length || 0)
    } else {
      libraryFiles.value = []
      libraryQuery.total = 0
    }
  } catch (error) {
    libraryFiles.value = []
    libraryQuery.total = 0
    ElMessage.error(error?.response?.data?.message || '加载素材库失败')
  } finally {
    libraryLoading.value = false
  }
}

const toggleLibraryFile = (item) => {
  const id = item?.fileId
  if (!id) return
  const idx = selectedLibraryFileIds.value.indexOf(id)
  if (idx >= 0) selectedLibraryFileIds.value.splice(idx, 1)
  else selectedLibraryFileIds.value.push(id)
}

const clearLibrarySelection = () => {
  selectedLibraryFileIds.value = []
}

const handleLibraryPageChange = async (page) => {
  libraryQuery.pageNum = page
  await loadLibraryFiles()
}

const handleLibrarySizeChange = async (size) => {
  libraryQuery.pageSize = size
  libraryQuery.pageNum = 1
  await loadLibraryFiles()
}

const handleLibraryDialogClosed = () => {
  libraryQuery.keyword = ''
  libraryQuery.pageNum = 1
  libraryQuery.pageSize = 12
  libraryQuery.total = 0
  selectedLibraryFileIds.value = []
}

const applyLibrarySelection = () => {
  const items = selectedLibraryFiles.value
  if (!items.length) return
  const pics = items.map((item) => createPicItem({
    uid: item.fileId,
    url: item.fileUrl || item.coverImageUrl || '',
    previewUrl: item.coverImagePreviewUrl || item.filePreviewUrl || item.fileUrl || item.coverImageUrl || '',
    name: item.fileName || '',
    isMain: false
  })).filter(pic => pic.url)
  if (!pics.length) return ElMessage.warning('所选素材没有可用图片地址')
  const currentPics = Array.isArray(form.pics) ? form.pics : []
  form.pics = [...currentPics, ...pics].map((pic, index) => ({
    ...pic,
    sortOrder: index + 1,
    isMain: index === form.mainPicIndex
  }))
  if (form.mainPicIndex >= form.pics.length) {
    form.mainPicIndex = form.pics.length - 1
  }
  if (!currentPics.length && pics[0]?.url) {
    form.mainPicIndex = 0
    form.materialName = form.materialName || items[0].fileName || ''
    form.expPurpose = form.expPurpose || items[0].comments || ''
  }
  libraryDialogVisible.value = false
}

const loadLookupOptions = async () => {
  try {
    const [propRes, typeRes, securityRes] = await Promise.all([
      fetchDataDictItems('data_material_prop'),
      fetchDataDictItems('data_material_type'),
      fetchDataDictItems('data_material_security')
    ])
    if (propRes.data.code === 200) {
      materialPropOptions.value = (Array.isArray(propRes.data.data) ? propRes.data.data : []).map(item => ({
        id: item.prop_id || item.propId || item.id,
        label: item.prop_name || item.propName || item.name || item.id
      })).filter(item => item.id)
    }
    if (typeRes.data.code === 200) {
      materialTypeOptions.value = (Array.isArray(typeRes.data.data) ? typeRes.data.data : []).map(item => ({
        id: item.type_id || item.typeId || item.id,
        label: item.type_name || item.typeName || item.name || item.id
      })).filter(item => item.id)
    }
    if (securityRes.data.code === 200) {
      materialSecurityOptions.value = (Array.isArray(securityRes.data.data) ? securityRes.data.data : [])
        .filter(item => String(item.status || '').toLowerCase() === 'y')
        .map(item => ({
          id: item.security_id || item.securityId || item.id,
          label: item.security_name || item.securityName || item.name || item.id
        }))
        .filter(item => item.id)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载材料字典失败')
  }
}

const resetForm = () => {
  form.materialId = ''
  form.materialName = ''
  form.materialPropId = ''
  form.materialTypeId = ''
  form.materialNum = ''
  form.pics = [createPicItem()]
  form.mainPicIndex = 0
  form.expPurpose = ''
  form.securityComments = ''
  securityCommentValues.value = []
  form.additionalComments = ''
  form.status = 'y'
  form.isPublic = 'n'
  selectedLibraryFileIds.value = []
  libraryFiles.value = []
  libraryQuery.keyword = ''
}

const loadItems = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await fetchMaterialMsgsMy({
      keyword: queryForm.keyword,
      status: queryForm.status,
      isPublic: queryForm.isPublic,
      materialPropId: queryForm.materialPropId,
      materialTypeId: queryForm.materialTypeId,
      currentUserId: userInfo.userId || '',
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      tableData.value = Array.isArray(data) ? data : (data.records || data.list || [])
      total.value = data.total || tableData.value.length || 0
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
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
    const res = await fetchMaterialLogs({ materialId: row.materialId, pageNum: 1, pageSize: 100000 })
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

const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  queryForm.isPublic = ''
  queryForm.materialPropId = ''
  queryForm.materialTypeId = ''
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
  dialogTitle.value = '新增实验材料'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  if (String(row?.isPublic || '').toLowerCase() === 'y') {
    ElMessage.warning('公共材料只允许修改公开状态，请在公共材料库中操作')
    return
  }
  isEdit.value = true
  editId.value = row.materialId
  dialogTitle.value = '编辑实验材料'
  resetForm()
  try {
    const res = await fetchMaterialMsg(row.materialId)
    if (res.data.code === 200) {
      const data = res.data.data || {}
      const pics = Array.isArray(data.pics) ? data.pics : []
      form.materialId = data.materialId || ''
      form.materialName = data.materialName || ''
      form.materialPropId = data.materialPropId || ''
      form.materialTypeId = data.materialTypeId || ''
      form.materialNum = data.materialNum || ''
      form.pics = pics.length
        ? pics.map((item, index) => createPicItem({
          uid: item.seqId || `pic-${index}`,
          url: item.materialUrl || '',
          previewUrl: item.materialPreviewUrl || item.materialUrl || '',
          name: item.fileName || item.materialName || ''
        }))
        : [createPicItem({ url: data.mainPicUrl || '', previewUrl: data.mainPicPreviewUrl || data.mainPicUrl || '' })]
      const mainIndex = form.pics.findIndex(pic => pic.url === (data.mainPicUrl || ''))
      form.mainPicIndex = mainIndex >= 0 ? mainIndex : 0
      form.expPurpose = data.expPurpose || ''
      form.securityComments = data.securityComments || ''
      securityCommentValues.value = splitSecurityComments(form.securityComments)
      form.additionalComments = data.additionalComments || ''
      form.status = data.status || 'y'
      form.isPublic = data.isPublic || 'n'
      dialogVisible.value = true
    } else {
      ElMessage.error(res.data.message || '加载详情失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载详情失败')
  }
}

const handleDialogClosed = () => {
  resetForm()
  picPreviewVisible.value = false
  libraryDialogVisible.value = false
  formRef.value?.clearValidate?.()
}

const splitSecurityComments = (value) => String(value || '')
  .split(/[，,；;\n]/)
  .map(item => item.trim())
  .filter(Boolean)

const handleSecurityCommentsChange = (values) => {
  form.securityComments = Array.from(new Set((values || []).map(item => String(item || '').trim()).filter(Boolean))).join('，')
}

const openPicPreview = async (row) => {
  try {
    const res = await fetchMaterialMsg(row.materialId)
    const data = res.data.code === 200 ? (res.data.data || {}) : row
    const pics = Array.isArray(data.pics) ? data.pics : []
    const mainPicUrl = data.mainPicUrl || row.mainPicUrl || ''
    const merged = pics.length
      ? pics.map((item, index) => ({
          uid: item.seqId || `preview-${index}`,
          url: item.materialPreviewUrl || item.materialUrl || '',
          sortOrder: item.sortOrder || index + 1,
          isMain: (item.materialUrl || '') === mainPicUrl
        })).filter(item => item.url)
      : (mainPicUrl ? [{ uid: 'preview-main', url: mainPicUrl, sortOrder: 1, isMain: true }] : [])
    previewPics.value = merged
    picPreviewVisible.value = true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载图片失败')
  }
}

const addPic = () => {
  form.pics.push(createPicItem({ sortOrder: form.pics.length + 1, isMain: false }))
}

const removePic = (index) => {
  if (form.pics.length <= 1) return
  form.pics.splice(index, 1)
  if (form.mainPicIndex === index) {
    form.mainPicIndex = Math.min(index, form.pics.length - 1)
  } else if (form.mainPicIndex > index) {
    form.mainPicIndex -= 1
  }
  if (form.mainPicIndex >= form.pics.length) {
    form.mainPicIndex = form.pics.length - 1
  }
  form.pics = form.pics.map((pic, idx) => ({
    ...pic,
    sortOrder: idx + 1,
    isMain: idx === form.mainPicIndex
  }))
}

const handlePicUploaded = (index, fileUrl) => {
  const target = form.pics[index]
  if (!target) return
  target.url = fileUrl || ''
  target.previewUrl = fileUrl || ''
  if (!form.pics[form.mainPicIndex]?.url) {
    form.mainPicIndex = index
  }
}

const handlePicPreviewUpdated = (index, previewUrl) => {
  const target = form.pics[index]
  if (!target) return
  target.previewUrl = previewUrl || target.url || ''
}

const handlePicNameUpdated = (index, fileName) => {
  const target = form.pics[index]
  if (!target) return
  target.name = fileName || ''
}

const buildPayload = () => {
  const pics = form.pics
    .filter(pic => pic.url)
    .map((pic, index) => ({
      url: pic.url || '',
      name: pic.name || '',
      sortOrder: index + 1
    }))
  const mainPic = pics[form.mainPicIndex] || pics[0] || { url: '' }
  return {
    ...form,
    materialId: String(form.materialId || '').trim(),
    mainPicUrl: mainPic.url || '',
    pics,
    materialPics: pics
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    handleSecurityCommentsChange(securityCommentValues.value)
    const hasMainPic = form.pics.some(pic => pic.url)
    if (!hasMainPic) {
      ElMessage.warning('请至少上传一张图片')
      return
    }
    submitLoading.value = true
    const payload = buildPayload()
    const res = isEdit.value ? await updateMaterialMsg(editId.value, payload) : await createMaterialMsg(payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      await loadItems()
    } else {
      const message = res?.data?.message || res?.data?.msg || '提交失败'
      ElMessage.error(message)
    }
  } catch (error) {
    const message = error?.response?.data?.message || error?.response?.data?.msg || error?.message || '提交失败'
    ElMessage.error(message)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除实验材料【${row.materialName}】吗？`, '提示', { type: 'warning' })
    const res = await deleteMaterialMsg(row.materialId)
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

onMounted(async () => {
  await loadLookupOptions()
  await loadItems()
})
</script>

<style scoped>
.material-cover {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
  border: 1px solid #ebeef5;
}

.material-cover :deep(img),
.material-cover :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.material-form-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.material-form-left {
  width: 320px;
  flex: 0 0 320px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
  box-sizing: border-box;
}

.material-form-right {
  flex: 1;
  min-width: 0;
}

.material-form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.material-pic-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.material-form-preview {
  margin-top: 14px;
}

.material-image-source-switch {
  margin-bottom: 12px;
}

.material-library-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.material-library-toolbar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.material-library-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
  max-height: 420px;
  overflow: auto;
}

.material-library-card {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
}

.material-library-card.is-selected {
  border-color: #409eff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.15);
}

.material-library-card__thumb {
  height: 120px;
  background: #f5f7fa;
  overflow: hidden;
  border-bottom: 1px solid #ebeef5;
}

.material-library-card__thumb :deep(.el-image),
.material-library-card__thumb :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
}

.material-library-card__image :deep(img),
.material-library-card__image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.material-library-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
}

.material-library-card__body {
  padding: 10px 12px;
}

.material-library-card__name {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
}

.material-library-card__meta {
  font-size: 12px;
  color: #909399;
}

.material-library-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.material-library-page-summary {
  color: #909399;
  font-size: 12px;
}

.material-library-page-control {
  flex: 1;
  display: flex;
  justify-content: center;
}

.material-library-action-buttons {
  display: flex;
  gap: 8px;
}

.material-pic-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.material-pic-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px;
  background: #fafafa;
  box-sizing: border-box;
}

.material-pic-card.is-main {
  border-color: #67c23a;
  background: #f0f9eb;
}

.material-pic-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.material-pic-thumb-wrap {
  margin-top: 12px;
}

.material-pic-thumb {
  width: 100%;
  height: 150px;
  border-radius: 10px;
  overflow: hidden;
  background: #fafafa;
  border: 1px solid #ebeef5;
}

.material-pic-thumb :deep(img),
.material-pic-thumb :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.material-main-radio {
  margin-top: 10px;
}

.material-add-pic-card {
  min-height: 240px;
  border: 1px dashed #c0c4cc;
  border-radius: 12px;
  background: #fff;
  color: #409eff;
}

.material-form-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.material-form-preview-image {
  width: 100%;
  height: 180px;
  border-radius: 10px;
  overflow: hidden;
}

.material-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.material-preview-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
}

.material-preview-image {
  width: 100%;
  height: 160px;
  display: block;
}

.material-preview-meta {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  font-size: 12px;
  color: #606266;
}

.material-preview-name {
  font-weight: 600;
  color: #409eff;
}

@media (max-width: 960px) {
  .material-form-layout {
    flex-direction: column;
  }

  .material-form-left {
    width: 100%;
    flex-basis: auto;
  }
}
</style>
