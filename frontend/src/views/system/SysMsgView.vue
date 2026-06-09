<template>
  <div :class="['page-card', { 'page-card--compact': compact }]">
    <el-card :class="['user-card', { 'user-card--compact': compact }]">
      <template v-if="!compact" #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">系统管理 > 消息管理</span>
          </div>
        </div>
      </template>

      <div v-if="!compact" class="user-toolbar user-toolbar--stacked">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="消息类型">
            <el-select v-model="queryForm.msgTypeId" clearable filterable placeholder="全部" style="width: 160px" @change="loadItems">
              <el-option v-for="item in msgTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="阅读状态">
            <el-select v-model="queryForm.readTag" clearable placeholder="全部" style="width: 120px" @change="loadItems">
              <el-option label="未读" value="0" />
              <el-option label="已读" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else class="message-inline-toolbar">
        <el-form :inline="true" :model="queryForm" class="search-form search-form--compact">
          <el-form-item label="消息类型">
            <el-select v-model="queryForm.msgTypeId" clearable filterable placeholder="全部" style="width: 160px" @change="loadItems">
              <el-option v-for="item in msgTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="阅读状态">
            <el-select v-model="queryForm.readTag" clearable placeholder="全部" style="width: 120px" @change="loadItems">
              <el-option label="未读" value="0" />
              <el-option label="已读" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadItems">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" class="user-table">
        <el-table-column prop="msgTypeId" label="消息类型" min-width="140">
          <template #default="scope">{{ getMsgTypeName(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="msgContent" label="消息内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="senderUserId" label="发送人" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ getUserName(scope.row.senderUserId) }}</template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" min-width="160">
          <template #default="scope">{{ formatDateTime(scope.row.sendTime) }}</template>
        </el-table-column>
        <el-table-column prop="readTag" label="阅读状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.readTag === '1' ? 'success' : 'warning'">
              {{ scope.row.readTag === '1' ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.readTag !== '1'" link type="primary" @click="markAsRead(scope.row)">标记已读</el-button>
            <span v-else>-</span>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDataDictItems } from '../../api/data'
import { fetchSystemMessages, markSystemMessageRead, fetchUsers } from '../../api/index'

const props = defineProps({
  compact: {
    type: Boolean,
    default: false
  }
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50, 100]
const queryForm = reactive({ msgTypeId: '', readTag: '0', pageNum: 1, pageSize: 10 })
const msgTypeOptions = ref([])
const userMap = ref({})
const msgTypeMap = computed(() => Object.fromEntries(msgTypeOptions.value.map(item => [item.id, item.name])))

const getMsgTypeName = (row) => msgTypeMap.value[row?.msgTypeId] || msgTypeMap.value[row?.msg_type_id] || row?.msgTypeName || row?.msg_type_name || row?.msgTypeId || row?.msg_type_id || '-'
const getUserName = (userId) => userMap.value[userId] || userId || '-'
const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const loadMsgTypes = async () => {
  try {
    const res = await fetchDataDictItems('data_msg_type')
    const rows = res.data.code === 200 ? (Array.isArray(res.data.data) ? res.data.data : []) : []
    msgTypeOptions.value = rows.map(item => ({
      id: item.msg_type_id || item.id,
      name: item.msg_type_name || item.name || item.id
    })).filter(item => item.id)
  } catch (error) {
    msgTypeOptions.value = []
  }
}

const loadUsers = async () => {
  try {
    const res = await fetchUsers({ pageNum: 1, pageSize: 1000, status: 'y' })
    const rows = res.data.code === 200 ? (res.data.data?.records || res.data.data?.list || res.data.data || []) : []
    userMap.value = Object.fromEntries(rows.map(item => [item.userId, item.userName || item.loginName || item.userId]))
  } catch (error) {
    userMap.value = {}
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await fetchSystemMessages({
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      msgTypeId: queryForm.msgTypeId,
      readTag: queryForm.readTag
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      const rows = Array.isArray(data) ? data : (data.records || data.list || data.rows || data.content || [])
      tableData.value = rows.map(item => ({
        ...item,
        msgTypeId: item.msgTypeId || item.msg_type_id || item.typeId || '',
        senderUserId: item.senderUserId || item.sender_user_id || '',
        receiverUserId: item.receiverUserId || item.receiver_user_id || '',
        readTag: String(item.readTag ?? item.read_tag ?? '0')
      }))
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

const refreshMessages = async () => {
  queryForm.pageNum = 1
  await loadItems()
}

const resetQuery = () => {
  queryForm.msgTypeId = ''
  queryForm.readTag = '0'
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

const markAsRead = async (row) => {
  try {
    const res = await markSystemMessageRead(row.msgId)
    if (res.data.code === 200) {
      ElMessage.success('已标记为已读')
      await loadItems()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  }
}

onMounted(async () => {
  await Promise.all([loadMsgTypes(), loadUsers(), loadItems()])
})
</script>
