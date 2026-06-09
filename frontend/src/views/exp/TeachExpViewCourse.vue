<template>
  <div class="page-card teach-course-page">
    <el-card class="user-card">
      <el-dialog v-model="viewDialogVisible" title="实验详情" width="1200px" class="user-dialog view-dialog" destroy-on-close @closed="closeViewDialog">
        <ExpStandardDetailView v-if="viewDialogVisible" :exp-id="viewExpId" :show-back-button="false" />
        <template #footer>
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="logDialogVisible" title="实验日志" width="980px" class="user-dialog">
        <el-table :data="logTableData" border stripe v-loading="logLoading" class="user-table">
          <el-table-column prop="logTime" label="时间" min-width="180">
            <template #default="scope">{{ formatLogTime(scope.row.logTime) }}</template>
          </el-table-column>
          <el-table-column prop="logTypeName" label="日志类型" min-width="140" />
          <el-table-column prop="logUserName" label="操作人" min-width="140" />
          <el-table-column prop="logComments" label="内容" min-width="240" show-overflow-tooltip />
        </el-table>
        <template #footer>
          <el-button @click="logDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
      <template #header>
        <div class="user-page-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">我的教学实验</span>
          </div>
          <el-button type="primary" plain @click="goListPageModel">列表模式</el-button> 
        </div>
      </template>

      <el-row :gutter="16" class="course-top-list">
        <el-col :span="24">
          <el-card shadow="never" class="course-summary-card">
            <el-skeleton v-if="loading" :rows="1" animated />
            <el-empty v-else-if="!subjectList.length" description="暂无数据" :image-size="64" />
            <div v-else class="course-text-line">
              学科：
              <span v-for="item in subjectList" :key="item.value" class="course-link-item">
                <a href="javascript:void(0)" class="course-link" :class="{ active: query.subjectId === item.value }" @click.prevent="toggleFilter('subjectId', item.value)">{{ item.label }}</a>
              </span>
            </div>
          </el-card>
        </el-col>

        <el-col :span="24">
          <el-card shadow="never" class="course-summary-card">
            <el-skeleton v-if="loading" :rows="1" animated />
            <el-empty v-else-if="!editionList.length" description="暂无数据" :image-size="64" />
            <div v-else class="course-text-line">
              版本：
              <span v-for="item in editionList" :key="item.value" class="course-link-item">
                <a href="javascript:void(0)" class="course-link" :class="{ active: query.editionId === item.value }" @click.prevent="toggleFilter('editionId', item.value)">{{ item.label }}</a>
              </span>
            </div>
          </el-card>
        </el-col>

        <el-col :span="24">
          <el-card shadow="never" class="course-summary-card">
            <el-skeleton v-if="loading" :rows="1" animated />
            <el-empty v-else-if="!gradeList.length" description="暂无数据" :image-size="64" />
            <div v-else class="course-text-line">
              年级：
              <span v-for="item in gradeList" :key="item.value" class="course-link-item">
                <a href="javascript:void(0)" class="course-link" :class="{ active: query.gradeId === item.value }" @click.prevent="toggleFilter('gradeId', item.value)">{{ item.label }}</a>
              </span>
            </div>
          </el-card>
        </el-col>

        <el-col :span="24">
          <el-card shadow="never" class="course-summary-card">
            <el-skeleton v-if="loading" :rows="1" animated />
            <el-empty v-else-if="!semesterList.length" description="暂无数据" :image-size="64" />
            <div v-else class="course-text-line">
              学期：
              <span v-for="item in semesterList" :key="item.value" class="course-link-item">
                <a href="javascript:void(0)" class="course-link" :class="{ active: query.semesterId === item.value }" @click.prevent="toggleFilter('semesterId', item.value)">{{ item.label }}</a>
              </span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="course-top-list">
        <el-col :span="24">
          <el-card shadow="never" class="course-summary-card">
            <el-skeleton v-if="loading" :rows="1" animated />
            <div v-else class="course-text-line">
              教材：
              <span v-if="hasAllCourseConditions && filteredCoursebookList.length">
                <span v-for="item in filteredCoursebookList" :key="item.value" class="course-link-item">
                  <a href="javascript:void(0)" class="course-link" :class="{ active: query.coursebookId === item.value }" @click.prevent="toggleFilter('coursebookId', item.value)">{{ item.label }}</a>
                </span>
              </span>
              <span v-else class="course-link-empty">暂无数据</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="course-bottom-split">
        <el-col :span="6">
          <el-card shadow="never" class="course-summary-card course-tree-card">
            <template #header>
              <div class="course-tree-title">教材目录</div>
            </template>
            <el-skeleton v-if="treeLoading" :rows="8" animated />
            <el-empty v-else-if="!query.coursebookId" description="请先选择教材" :image-size="64" />
            <el-empty v-else-if="!coursebookTree.length" description="暂无目录数据" :image-size="64" />
            <el-tree
              v-else
              :data="coursebookTree"
              :props="treeProps"
              node-key="contentId"
              default-expand-all
              highlight-current
              class="coursebook-tree"
              @node-click="handleTreeNodeClick"
            >
              <template #default="{ data }">
                <span class="course-tree-node">
                  <span class="course-tree-node-type">{{ contentTypeLabel(data.contentType) }}</span>
                  <span class="course-tree-node-name">{{ data.contentName }}</span>
                  <span class="course-tree-node-count">（{{ data.teachCount || 0 }}）</span>
                </span>
              </template>
            </el-tree>
          </el-card>
        </el-col>
        <el-col :span="18">
          <el-card shadow="never" class="course-summary-card course-detail-card">
            <template #header>
              <div class="course-tree-title">教学实验列表 &nbsp;&nbsp; <el-button type="primary" @click="openCreateDialog">新增实验</el-button></div>
            </template>
            <el-skeleton v-if="teachLoading" :rows="8" animated />
            <el-empty v-else-if="!selectedTreeNode" description="请点击左侧目录" :image-size="64" />
            <el-empty v-else-if="!teachExpList.length" description="暂无我的教学实验" :image-size="64" />
            <el-table v-else :data="teachExpList" border stripe class="user-table">
              <el-table-column prop="expName" label="实验名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="chooseType" label="必做/选做" min-width="100" show-overflow-tooltip>
                <template #default="scope">{{ chooseTypeLabel(scope.row.chooseType) }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="statusTagType(scope.row.status)" effect="light">
                    {{ statusLabel(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip>
                <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="250" fixed="right" align="center">
                <template #default="scope">
                  <el-button link type="primary" @click="openViewDialog(scope.row)">查看</el-button>
                  <el-button v-if="['c', 'n'].includes(String(scope.row.status || '').toLowerCase())" link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
                  <el-button v-if="['c', 'n'].includes(String(scope.row.status || '').toLowerCase())" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
                  <el-button link type="primary" @click="openLogDialog(scope.row)">日志</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ExpStandardDetailView from './ExpStandardDetailView.vue'
import { deleteExpStandard, fetchDataDictItems, fetchExpLogs, fetchLatestExpTeachesDraft } from '../../api/index'
import { fetchCoursebooks, fetchCoursebookContents } from '../../api/edu'
import { fetchMyTeachBySection } from '../../api/exp'

const router = useRouter()
const loading = ref(false)
const treeLoading = ref(false)
const teachLoading = ref(false)
const subjectList = ref([])
const editionList = ref([])
const gradeList = ref([])
const semesterList = ref([])
const coursebookList = ref([])
const coursebookTree = ref([])
const teachExpList = ref([])
const selectedTreeNode = ref(null)
const viewDialogVisible = ref(false)
const viewExpId = ref('')
const logDialogVisible = ref(false)
const logLoading = ref(false)
const logTableData = ref([])
const treeProps = { children: 'children', label: 'contentName' }

const query = reactive({ subjectId: '', editionId: '', gradeId: '', semesterId: '', coursebookId: '' })

const normalizeOptions = (rows, valueKey, labelKey) => (rows || [])
  .filter((item) => item?.[valueKey] != null && item?.[labelKey] != null)
  .map((item) => ({ value: String(item[valueKey]), label: item[labelKey] }))

const loadLists = async () => {
  loading.value = true
  try {
    const [subjectRes, editionRes, gradeRes, semesterRes] = await Promise.all([
      fetchDataDictItems('data_school_subject'),
      fetchDataDictItems('data_textbook_edition'),
      fetchDataDictItems('data_school_grade'),
      fetchDataDictItems('data_school_semester'),
    ])

    if (subjectRes.data.code === 200) subjectList.value = normalizeOptions(subjectRes.data.data || [], 'subject_id', 'subject_name')
    if (editionRes.data.code === 200) editionList.value = normalizeOptions(editionRes.data.data || [], 'edition_id', 'edition_name')
    if (gradeRes.data.code === 200) gradeList.value = normalizeOptions(gradeRes.data.data || [], 'grade_id', 'grade_name')
    if (semesterRes.data.code === 200) semesterList.value = normalizeOptions(semesterRes.data.data || [], 'semester_id', 'semester_name')

    const coursebookRes = await fetchCoursebooks({ paged: false })
    const coursebookRows = Array.isArray(coursebookRes.data?.data) ? coursebookRes.data.data : (coursebookRes.data?.data?.records || coursebookRes.data?.data?.list || [])
    coursebookList.value = coursebookRows
      .filter((item) => item && String(item.status || 'y') === 'y')
      .map((item) => ({
        value: String(item.coursebookId || item.id || item.coursebook_id || ''),
        label: item.coursebookName || item.coursebook_name || item.name || '-',
        subjectId: String(item.subjectId || item.subject_id || ''),
        editionId: String(item.editionId || item.edition_id || ''),
        gradeId: String(item.gradeId || item.grade_id || ''),
        semesterId: String(item.semesterId || item.semester_id || ''),
        raw: item,
      }))
  } finally {
    loading.value = false
  }
}

const goListPageModel = () => {
  router.push('/admin/exp/exp-teach')
}

const resetQuery = () => {
  query.subjectId = ''
  query.editionId = ''
  query.gradeId = ''
  query.semesterId = ''
  query.coursebookId = ''
}

const toggleFilter = (key, value) => {
  query[key] = query[key] === value ? '' : value
  if (key !== 'coursebookId') {
    query.coursebookId = ''
  }
  selectedTreeNode.value = null
  teachExpList.value = []
}

const contentTypeLabel = (value) => ({ unit: '单元', chapter: '章', section: '节' }[value] || value || '-')
const chooseTypeLabel = (value) => ({ must: '必做', choose: '选做' }[value] || value || '-')
const statusLabel = (value) => ({ c: '草稿', t: '待审核', y: '通过', n: '不通过' }[value] || value || '-')
const statusTagType = (value) => ({ c: 'info', t: 'warning', y: 'success', n: 'danger' }[value] || 'info')
const openCreateDialog = async () => {
  try {
    const res = await fetchLatestExpTeachesDraft()
    const draft = res.data.data
    if (res.data.code === 200 && draft && draft.expId) {
      router.push({ path: '/admin/exp/exp-teach/create', query: { expId: draft.expId } })
      return
    }
  } catch (error) {
    // ignore and create new draft
  }
  router.push('/admin/exp/exp-teach/create')
}

const openViewDialog = (row) => {
  const expId = String(row?.expId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  viewExpId.value = expId
  viewDialogVisible.value = true
}

const closeViewDialog = () => {
  viewDialogVisible.value = false
  viewExpId.value = ''
}

const openEditDialog = (row) => {
  const expId = String(row?.expId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  router.push({ path: '/admin/exp/exp-teach/create', query: { expId } })
}

const openLogDialog = async (row) => {
  const expId = String(row?.expId || '').trim()
  if (!expId) {
    ElMessage.warning('未找到实验ID')
    return
  }
  logDialogVisible.value = true
  logLoading.value = true
  logTableData.value = []
  try {
    const res = await fetchExpLogs(expId)
    if (res.data.code === 200) {
      const rows = Array.isArray(res.data.data) ? res.data.data : (res.data.data?.records || res.data.data?.list || [])
      logTableData.value = rows.sort((a, b) => new Date(b.logTime || 0) - new Date(a.logTime || 0))
    } else {
      ElMessage.error(res.data.message || '加载日志失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加载日志失败')
  } finally {
    logLoading.value = false
  }
}

const formatLogTime = (value) => {
  if (!value) return '-'
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除实验【${row.expName}】吗？`, '提示', { type: 'warning' })
  try {
    const res = await deleteExpStandard(row.expId)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      if (selectedTreeNode.value) {
        await handleTreeNodeClick(selectedTreeNode.value)
      }
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const hasAllCourseConditions = computed(() => Boolean(query.subjectId && query.editionId && query.gradeId && query.semesterId))

const filteredCoursebookList = computed(() => {
  if (!hasAllCourseConditions.value) return []
  return coursebookList.value.filter((item) => item.subjectId === query.subjectId
    && item.editionId === query.editionId
    && item.gradeId === query.gradeId
    && item.semesterId === query.semesterId)
})

const coursebookText = computed(() => filteredCoursebookList.value.map((item) => item.label).join('、'))

const normalizeTree = (rows) => {
  const map = new Map()
  const roots = []
  ;(rows || []).forEach((item) => {
    const node = {
      contentId: String(item.contentId || item.id || item.content_id || ''),
      contentName: item.contentName || item.content_name || '-',
      contentType: item.contentType || item.content_type || '',
      parentId: String(item.parentId || item.parent_id || ''),
      coursebookId: String(item.coursebookId || item.coursebook_id || ''),
      comments: item.comments || '',
      status: item.status || '',
      teachCount: Number(item.teach_count || 0),
      sortOrder: item.sortOrder ?? item.sort_order ?? 0,
      raw: item,
      children: [],
    }
    map.set(node.contentId, node)
  })
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) map.get(node.parentId).children.push(node)
    else roots.push(node)
  })
  const sortFn = (a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0)
  const sortTree = (list) => list.sort(sortFn).forEach((node) => node.children?.length && sortTree(node.children))
  sortTree(roots)
  return roots
}

const loadCoursebookTree = async (coursebookId) => {
  const id = String(coursebookId || query.coursebookId || '').trim()
  if (!id) {
    coursebookTree.value = []
    selectedTreeNode.value = null
    return
  }
  treeLoading.value = true
  try {
    const res = await fetchCoursebookContents({ coursebookId: id })
    const rows = Array.isArray(res.data?.data) ? res.data.data : (res.data?.data?.records || res.data?.data?.list || [])
    coursebookTree.value = normalizeTree(rows)
    selectedTreeNode.value = null
  } finally {
    treeLoading.value = false
  }
}

const handleTreeNodeClick = async (data) => {
  selectedTreeNode.value = data || null
  const sectionId = String(data?.contentId || '').trim()
  if (!sectionId) {
    teachExpList.value = []
    return
  }
  teachLoading.value = true
  try {
    const res = await fetchMyTeachBySection(sectionId)
    const row = res?.data?.data
    teachExpList.value = row ? [row] : []
  } catch (error) {
    teachExpList.value = []
  } finally {
    teachLoading.value = false
  }
}

watch(
  () => query.coursebookId,
  async (value) => {
    if (value) await loadCoursebookTree(value)
    else {
      coursebookTree.value = []
      selectedTreeNode.value = null
      teachExpList.value = []
    }
  },
)

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(loadLists)
</script>

<style scoped>
.teach-course-page {
  width: 100%;
}

.course-top-list {
  margin-top: 0;
}
.course-top-list :deep(.el-col) {
  margin-bottom: 6px;
}

.course-summary-card {
  min-height: auto;
  padding: 6px 10px;
}

.course-summary-card :deep(.el-card__body) {
  padding: 6px 8px;
}

.course-text-line {
  font-size: 14px;
  line-height: 1.2;
  min-height: 1.2em;
  word-break: break-word;
}

.course-link-item {
  white-space: nowrap;
  margin-right: 10px;
}

.course-link {
  color: var(--el-text-color-regular);
  text-decoration: none;
  cursor: pointer;
}

.course-link:hover,
.course-link.active {
  color: var(--el-color-primary);
  font-weight: 600;
}

.course-link-empty {
  color: var(--el-text-color-secondary);
}

.course-bottom-split {
  margin-top: 0;
}
.course-bottom-split :deep(.el-col) {
  margin-bottom: 6px;
}

.course-tree-card :deep(.el-card__body),
.course-detail-card :deep(.el-card__body) {
  padding-top: 6px;
}

.course-tree-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.2;
}

.course-tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.course-tree-node-type {
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.course-tree-node-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-tree-node-count {
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.teach-exp-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.teach-exp-item {
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.teach-exp-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.teach-exp-meta {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
