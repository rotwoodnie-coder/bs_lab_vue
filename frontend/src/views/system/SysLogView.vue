<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 日志管理</span>
            <span class="user-page-subtitle">支持按时间查询、查看详情与删除</span>
          </div>
        </div>
      </template>

      <div class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="快捷筛选">
            <el-radio-group v-model="queryForm.quickRange" @change="handleQuickRangeChange">
              <el-radio-button value="today">今天</el-radio-button>
              <el-radio-button value="week">最近7天</el-radio-button>
              <el-radio-button value="month">本月</el-radio-button>
              <el-radio-button value="custom">自定义</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="queryForm.quickRange === 'custom'" label="时间范围">
            <el-date-picker
              v-model="queryForm.timeRange"
              type="datetimerange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              range-separator="至"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 360px"
              @change="handleTimeChange"
            />
          </el-form-item>
          <el-form-item label="日志类型">
            <el-input v-model="queryForm.logType" clearable placeholder="全部" style="width: 160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="logTime" label="记录时间" min-width="170" show-overflow-tooltip>
          <template #default="scope">{{ formatDateTimeDisplay(scope.row.logTime) }}</template>
        </el-table-column>
        <el-table-column prop="logType" label="日志类型" min-width="120" />
        <el-table-column prop="userName" label="操作人" min-width="180" show-overflow-tooltip />
        <el-table-column prop="logDataType" label="数据类型" min-width="140" show-overflow-tooltip />
        <el-table-column prop="logDataId" label="数据ID" min-width="140" show-overflow-tooltip />
        <el-table-column prop="logDataContent" label="内容摘要" min-width="260" show-overflow-tooltip>
          <template #default="scope">{{ getContentSummary(scope.row.logDataContent) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openDetailDialog(scope.row)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="720px" class="user-dialog">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录时间">{{ formatDateTimeDisplay(detailRow.logTime) }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">{{ detailRow.logType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailRow.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据类型">{{ detailRow.logDataType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据ID">{{ detailRow.logDataId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="日志ID">{{ detailRow.logId || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="log-detail-content">
        <div class="log-detail-title">内容详情</div>
        <pre class="log-detail-pre">{{ formattedDetailContent }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteSystemLog, fetchSystemLogs } from '../../api/index'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const detailVisible = ref(false)
const detailRow = ref({})
const userMap = ref({})
const queryForm = reactive({
  quickRange: 'week',
  startTime: '',
  endTime: '',
  timeRange: [],
  logType: '',
  pageNum: 1,
  pageSize: 10
})

const formattedDetailContent = computed(() => {
  const value = detailRow.value?.logDataContent
  if (!value) return '-'
  if (typeof value === 'string') return value
  try {
    return JSON.stringify(value, null, 2)
  } catch (error) {
    return String(value)
  }
})

const normalizeRow = (item) => ({
  ...item,
  logId: item.logId || item.log_id || '',
  userId: item.userId || item.user_id || '',
  userName: item.userName || item.user_name || item.operatorName || item.operator_name || '',
  logType: item.logType || item.log_type || '',
  logTime: item.logTime || item.log_time || '',
  logDataType: item.logDataType || item.log_data_type || '',
  logDataId: item.logDataId || item.log_data_id || '',
  logDataContent: item.logDataContent || item.log_data_content || ''
})

const getContentSummary = (value) => {
  if (!value) return '-'
  const text = typeof value === 'string' ? value : JSON.stringify(value)
  return text.length > 80 ? `${text.slice(0, 80)}...` : text
}


const applyQuickRange = (rangeKey) => {
  const now = new Date()
  const end = new Date(now)
  const start = new Date(now)

  if (rangeKey === 'today') {
    start.setHours(0, 0, 0, 0)
  } else if (rangeKey === 'week') {
    start.setDate(start.getDate() - 6)
    start.setHours(0, 0, 0, 0)
  } else if (rangeKey === 'month') {
    start.setDate(1)
    start.setHours(0, 0, 0, 0)
  }

  end.setHours(23, 59, 59, 999)
  queryForm.startTime = formatDateTime(start)
  queryForm.endTime = formatDateTime(end)
  queryForm.timeRange = [queryForm.startTime, queryForm.endTime]
}

const formatDateTime = (date) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatDateTimeDisplay = (value) => {
  if (!value) return '-'
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return formatDateTime(date)
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchSystemLogs({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      startTime: queryForm.startTime,
      endTime: queryForm.endTime,
      logType: queryForm.logType
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      const rows = Array.isArray(data) ? data : (data.records || data.list || data.rows || data.content || [])
      tableData.value = rows.map(normalizeRow)
      total.value = data.total || data.count || rows.length || 0
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleQuickRangeChange = (rangeKey) => {
  queryForm.quickRange = rangeKey
  if (rangeKey === 'custom') {
    queryForm.startTime = ''
    queryForm.endTime = ''
    queryForm.timeRange = []
    return
  }
  applyQuickRange(rangeKey)
  queryForm.pageNum = 1
  loadItems()
}

const handleTimeChange = (range) => {
  queryForm.startTime = range?.[0] || ''
  queryForm.endTime = range?.[1] || ''
}

const resetQuery = () => {
  queryForm.quickRange = 'week'
  applyQuickRange('week')
  queryForm.logType = ''
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

const openDetailDialog = (row) => {
  detailRow.value = normalizeRow({
    ...row,
    userName: row?.userName || row?.user_name || row?.operatorName || row?.operator_name || '-'
  })
  detailVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除该日志吗？`, '提示', { type: 'warning' })
    const res = await deleteSystemLog(row.logId)
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
  applyQuickRange(queryForm.quickRange)
  loadItems()
})
</script>
