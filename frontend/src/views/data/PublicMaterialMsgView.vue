<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">数据维护 > 公共材料库维护</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="关键字">
            <el-input v-model="queryForm.keyword" placeholder="材料名称/属性/分类/用途" clearable @change="loadItems" />
          </el-form-item>
          <el-form-item label="材料分类">
            <el-select v-model="queryForm.materialTypeId" placeholder="全部" clearable style="width: 140px" @change="loadItems">
              <el-option v-for="item in materialTypeOptions" :key="item.id" :label="item.label" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否公开">
            <el-select v-model="queryForm.isPublic" placeholder="全部" clearable style="width: 100px" @change="loadItems">
              <el-option label="公开" value="y" />
              <el-option label="待公开" value="n" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column label="主图片" width="100" align="center">
          <template #default="scope">
            <el-image v-if="getDisplayUrl(scope.row.mainPicPreviewUrl)" :src="getDisplayUrl(scope.row.mainPicPreviewUrl)" fit="contain" class="material-cover" :preview-src-list="[getDisplayUrl(scope.row.mainPicUrl)]" preview-teleported />
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
        <el-table-column label="公开状态" width="120" align="center">
          <template #default="scope">
            <el-switch
              :model-value="isPublicItem(scope.row)"
              inline-prompt
              active-text="公开"
              inactive-text="待公开"
              active-color="#13ce66"
              inactive-color="#e6a23c"
              @change="(value) => togglePublicStatus(scope.row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openPicPreview(scope.row)">预览</el-button>
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

    <el-dialog v-model="picPreviewVisible" title="图片预览" width="860px" class="user-dialog">
      <div v-if="previewPics.length" class="material-preview-grid">
        <div v-for="pic in previewPics" :key="pic.uid" class="material-preview-item">
          <el-image :src="getDisplayUrl(pic.url)" fit="cover" class="material-preview-image" :preview-src-list="previewPics.map(item => getDisplayUrl(item.url))" preview-teleported />
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDataDictItems, fetchMaterialMsgsForPublic, updateMaterialMsg,updateMaterialMsgPublic, fetchMaterialMsg, fetchMaterialLogs } from '../../api/index'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const picPreviewVisible = ref(false)
const logDialogVisible = ref(false)
const logLoading = ref(false)
const previewPics = ref([])
const logTableData = ref([])
const queryForm = reactive({ keyword: '', isPublic: '', materialTypeId: '', status: 'y', pageNum: 1, pageSize: 10 })
const materialPropOptions = ref([])
const materialTypeOptions = ref([])
const propsMap = computed(() => Object.fromEntries(materialPropOptions.value.map(item => [item.id, item.label])))
const typesMap = computed(() => Object.fromEntries(materialTypeOptions.value.map(item => [item.id, item.label])))
const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''

const getDisplayUrl = (url) => {
  if (!url) return ''
  const raw = String(url).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
}
const getPropLabel = (id) => propsMap.value[id] || id || '-'
const getTypeLabel = (id) => typesMap.value[id] || id || '-'
const isPublicItem = (row) => String(row?.isPublic || '').toLowerCase() === 'y'

const loadLookupOptions = async () => {
  try {
    const propRes = await fetchDataDictItems('data_material_prop')
    const typeRes = await fetchDataDictItems('data_material_type')
    if (propRes.data.code === 200) {
      materialPropOptions.value = (Array.isArray(propRes.data.data) ? propRes.data.data : []).map(item => ({ id: item.prop_id || item.propId || item.id, label: item.prop_name || item.propName || item.name || item.id })).filter(item => item.id)
    }
    if (typeRes.data.code === 200) {
      materialTypeOptions.value = (Array.isArray(typeRes.data.data) ? typeRes.data.data : []).map(item => ({ id: item.type_id || item.typeId || item.id, label: item.type_name || item.typeName || item.name || item.id })).filter(item => item.id)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载材料属性/分类失败')
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchMaterialMsgsForPublic({
      keyword: queryForm.keyword,
      isPublic: queryForm.isPublic,
      status: queryForm.status,
      materialTypeId: queryForm.materialTypeId,
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

const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.isPublic = ''
  queryForm.materialTypeId = ''
  queryForm.status = 'y'
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

const openPicPreview = async (row) => {
  try {
    const res = await fetchMaterialMsg(row.materialId)
    const data = res.data.code === 200 ? (res.data.data || {}) : row
    const pics = Array.isArray(data.pics) ? data.pics : []
    previewPics.value = pics.length
      ? pics.map((item, index) => ({ uid: item.seqId || `preview-${index}`, url: item.materialPreviewUrl || item.materialUrl || '', sortOrder: item.sortOrder || index + 1, isMain: (item.materialUrl || '') === (data.mainPicUrl || row.mainPicUrl || '') })).filter(item => item.url)
      : (data.mainPicUrl || row.mainPicUrl ? [{ uid: 'preview-main', url: data.mainPicUrl || row.mainPicUrl, sortOrder: 1, isMain: true }] : [])
    picPreviewVisible.value = true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载图片失败')
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

const togglePublicStatus = async (row, value) => {
  const nextPublic = value ? 'y' : 'n'
  try {
    const payload = { ...row, isPublic: nextPublic }
    const res = await updateMaterialMsgPublic(row.materialId, payload)
    if (res.data.code === 200) {
      ElMessage.success(nextPublic === 'y' ? '已设为公开' : '已设为待公开')
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '操作失败')
      await loadItems()
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
    await loadItems()
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

.material-preview-order {
  color: #909399;
}
</style>
