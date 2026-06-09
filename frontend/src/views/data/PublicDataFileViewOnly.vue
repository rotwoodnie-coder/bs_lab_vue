<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">数据维护 > 公共素材库查看</span>
          </div>
        </div>
      </template>

      <div class="public-file-body">
        <div class="public-file-sidebar">
          <div class="public-file-sidebar-title">素材类型</div>
          <div class="public-file-type-list">
            <div class="public-file-type-item" :class="{ active: !queryForm.fileTypeId }" @click="selectFileType('')">全部</div>
            <div
              v-for="item in fileTypeOptions"
              :key="item.typeId"
              class="public-file-type-item"
              :class="{ active: queryForm.fileTypeId === item.typeId }"
              @click="selectFileType(item.typeId)"
            >
              <img v-if="getTypeIconUrl(item.logoClass)" :src="getTypeIconUrl(item.logoClass)" :alt="item.typeName" class="public-file-type-icon" />
              <span>{{ item.typeName }}</span>
            </div>
          </div>
        </div>

        <div class="public-file-main">
          <div class="user-toolbar user-toolbar--stacked">
            <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
              <el-form-item label="关键字">
                <el-input v-model="queryForm.keyword" placeholder="文件名称/标签/后缀" clearable @change="loadItems" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadItems">查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="tableData" :row-class-name="currentRowClassName" border stripe v-loading="loading" class="user-table">
            <el-table-column label="预览" width="280" align="center">
              <template #default="scope">
                <div class="media-cell" v-if="isMediaFile(scope.row)">
                  <div class="media-player-wrap" >
                    <video
                      v-if="isVideoFile(scope.row)"
                      :src="getDisplayUrl(scope.row.previewUrl)"
                      controls
                      preload="metadata"
                      class="media-preview-video"
                      @play="handleMediaPlay(scope.row, $event)"
                      @pause="handleMediaPause(scope.row, $event)"
                      @ended="handleMediaPause(scope.row, $event)"
                    />
                    <audio
                      v-else-if="isAudioFile(scope.row)"
                      :src="getDisplayUrl(scope.row.previewUrl)"
                      controls
                      preload="metadata"
                      class="media-preview-audio"
                      @play="handleMediaPlay(scope.row, $event)"
                      @pause="handleMediaPause(scope.row, $event)"
                      @ended="handleMediaPause(scope.row, $event)"
                    />
                    <button
                      v-if="isMediaFile(scope.row) && isCurrentPlaying(scope.row)"
                      class="media-stop-icon-button"
                      type="button"
                      aria-label="停止播放"
                      @click="stopMediaPlayback"
                    >
                      ×
                    </button>
                  </div>
                  <el-image v-if="!isMediaFile(scope.row)" :src="getDisplayUrl(scope.row.coverImagePreviewUrl)" fit="contain" class="data-file-cover" />
                </div>
                <el-image v-else-if="getDisplayUrl(scope.row.coverImageUrl)" :src="getDisplayUrl(scope.row.coverImagePreviewUrl)" fit="contain" class="data-file-cover" />
            <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="fileName" label="文件名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="fileTag" label="标签" min-width="120" show-overflow-tooltip />
            <el-table-column prop="ownerUserId" label="创建人" min-width="100" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                {{ getFileTypeName(scope.row.fileTypeId) }} 
                <el-button link type="primary" :disabled="!getDisplayUrl(scope.row.fileUrl)" @click="previewFile(scope.row)">预览</el-button>
                <el-button link type="primary" @click="openLogDialog(scope.row)">查看日志</el-button>
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
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                :current-page="queryForm.pageNum"
                :page-size="queryForm.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                @current-change="handlePageChange"
                @size-change="handleSizeChange"
              />
            </div>
          </div>
        </div>
      </div>
    </el-card>

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
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDataFileLogs, fetchDataFileTypes, fetchPublicDataFiles, updateDataFile } from '../../api/index'

const loading = ref(false)
const logDialogVisible = ref(false)
const logLoading = ref(false)
const tableData = ref([])
const logTableData = ref([])
const total = ref(0)
const currentPlayingFileId = ref('')
const mediaElements = reactive({})
const queryForm = reactive({ keyword: '', isPublic: 'y', fileTypeId: '', status: 'y', pageNum: 1, pageSize: 10 })
const urlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
const iconMap = {
  image: '/icons/image.svg',
  video: '/icons/video.svg',
  audio: '/icons/audio.svg',
  pdf: '/icons/pdf.svg',
  word: '/icons/word.svg',
  ppt: '/icons/ppt.svg',
  excel: '/icons/excel.svg'
}
const fileTypeOptions = ref([])
const fileTypeNameMap = computed(() => Object.fromEntries(fileTypeOptions.value.map(item => [item.typeId, item.typeName])))

const loadFileTypes = async () => {
  try {
    const res = await fetchDataFileTypes()
    if (res.data.code === 200) {
      fileTypeOptions.value = (Array.isArray(res.data.data) ? res.data.data : []).filter(item => item && item.typeId && item.typeName && String(item.status || '').toLowerCase() === 'y')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载文件类型失败')
  }
}

const togglePublicStatus = async (row, value) => {
  const nextPublic = value ? 'y' : 'n'
  try {
    const payload = { ...row, isPublic: nextPublic }
    const res = await updateDataFile(row.fileId, payload)
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

const getFileTypeName = (fileTypeId) => fileTypeNameMap.value[fileTypeId] || fileTypeId || '-'
const getTypeIconUrl = (logoClass) => iconMap[String(logoClass || '').trim().toLowerCase()] || ''
const isPublicItem = (row) => String(row?.isPublic || '').toLowerCase() === 'y'
const isVideoFile = (row) => {
  const raw = String(row?.fileExt || row?.fileName || '').trim().toLowerCase()
  const ext = raw.includes('.') ? raw.slice(raw.lastIndexOf('.') + 1) : raw
  return ['mp4', 'webm', 'ogg'].includes(ext)
}
const isAudioFile = (row) => {
  const raw = String(row?.fileExt || row?.fileName || '').trim().toLowerCase()
  const ext = raw.includes('.') ? raw.slice(raw.lastIndexOf('.') + 1) : raw
  return ['mp3', 'wav', 'm4a', 'ogg'].includes(ext)
}
const isMediaFile = (row) => isVideoFile(row) || isAudioFile(row)
const isCurrentPlaying = (row) => currentPlayingFileId.value === row?.fileId
const stopMediaPlayback = () => {
  const fileId = currentPlayingFileId.value
  const el = mediaElements[fileId]
  if (el) {
    if (typeof el.pause === 'function') {
      el.pause()
    }
    try {
      el.currentTime = 0
    } catch (e) {
      // ignore
    }
    if (typeof el.load === 'function') {
      try {
        el.load()
      } catch (e) {
        // ignore
      }
    }
  }
  if (fileId && mediaElements[fileId]) {
    delete mediaElements[fileId]
  }
  currentPlayingFileId.value = ''
}

const handleMediaPlay = (row, event) => {
  const el = event?.target
  if (!el) return
  if (currentPlayingFileId.value && currentPlayingFileId.value !== row?.fileId) {
    const prevEl = mediaElements[currentPlayingFileId.value]
    if (prevEl && typeof prevEl.pause === 'function') prevEl.pause()
  }
  mediaElements[row?.fileId] = el
  currentPlayingFileId.value = row?.fileId || ''
}

const handleMediaPause = (row, event) => {
  const el = event?.target
  if (row?.fileId && mediaElements[row.fileId] === el) {
    delete mediaElements[row.fileId]
  }
  if (currentPlayingFileId.value === row?.fileId) {
    currentPlayingFileId.value = ''
  }
}
const getDisplayUrl = (url) => {
  if (!url) return ''
  const raw = String(url).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${urlPrefix}${raw}`
  return `${urlPrefix}/${raw}`
}

const previewFile = (row) => {
  const url = getDisplayUrl(row.fileUrl)
  if (url) window.open(url, '_blank', 'noopener,noreferrer')
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

const currentRowClassName = ({ row }) => (isCurrentPlaying(row) ? 'is-playing-row' : '')

const toggleMediaPlayback = (row) => {
  console.log("stop");
  const url = getDisplayUrl(row.fileUrl)
  if (!url) return
  if (isCurrentPlaying(row)) {
    stopMediaPlayback()
    return
  }
  currentPlayingFileId.value = row.fileId
}

const downloadFile = (row) => {
  const url = getDisplayUrl(row.fileUrl)
  if (!url) return
  const link = document.createElement('a')
  link.href = url
  link.download = row.fileName || ''
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchPublicDataFiles({
      keyword: queryForm.keyword,
      isPublic: queryForm.isPublic,
      status: 'y',
      fileTypeId: queryForm.fileTypeId,
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
  queryForm.fileTypeId = ''
  queryForm.status = 'y'
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  loadItems()
}

const selectFileType = async (typeId) => {
  queryForm.fileTypeId = typeId
  queryForm.pageNum = 1
  await loadItems()
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

onBeforeUnmount(() => {
  stopMediaPlayback()
})

onMounted(async () => {
  await loadFileTypes()
  await loadItems()
})
</script>

<style scoped>
.public-file-body {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

.public-file-sidebar {
  width: 180px;
  flex: 0 0 180px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fff;
  padding: 12px;
  box-sizing: border-box;
}

.public-file-sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.public-file-type-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: calc(100vh - 260px);
  overflow-y: auto;
}

.public-file-type-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #606266;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.public-file-type-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
  flex: 0 0 18px;
}

.public-file-type-item:hover {
  background: #eaf2ff;
  color: #2563eb;
}

.public-file-type-item.active {
  background:rgb(222, 223, 225);
  color: #fff;
}

.public-file-main {
  flex: 1;
  min-width: 0;
}

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

.media-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.media-player-wrap {
  position: relative;
  width: 100%;
  max-width: 100%;
}

.media-preview-video,
.media-preview-audio {
  display: block;
  width: 100%;
  max-width: 100%;
}

.media-preview-video {
  max-height: 140px;
  border-radius: 8px;
  background: #000;
  object-fit: contain;
}

.media-preview-audio {
  min-height: 40px;
}

.media-stop-icon-button {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 4;
  width: 22px;
  height: 22px;
  border: 0;
  border-radius: 999px;
  background: rgba(220, 38, 38, 0.5);
  color: rgba(255, 255, 255, 0.95);
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  padding: 0;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.18);
  backdrop-filter: blur(2px);
}

.media-stop-icon-button:hover {
  background: rgba(220, 38, 38, 0.78);
}

.playing-badge {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ecfdf3;
  color: #16a34a;
  font-size: 12px;
  line-height: 1.4;
}

:deep(.is-playing-row td) {
  background: #f0fdf4 !important;
}

@media (max-width: 960px) {
  .public-file-body {
    flex-direction: column;
  }

  .public-file-sidebar {
    width: 100%;
    flex-basis: auto;
  }
}
</style>
