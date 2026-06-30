<template>
  <div class="step-block">
    <div class="video-step__toolbar">
      <div class="video-step__title">实验步骤</div>
      <div class="video-step__actions">
        <el-button type="primary" @click="addStepItem">增加步骤</el-button>
      </div>
    </div>

    <div v-if="stepList.length" class="step-list">
      <div v-for="(item, index) in stepList" :key="item.uid" class="step-card">
        <div class="step-card__header">
          <div class="step-card__title">步骤 {{ index + 1 }}</div>
          <div class="step-card__actions">
            <el-button link type="primary" :disabled="index === 0" @click="moveStepUp(index)">上移</el-button>
            <el-button link type="primary" :disabled="index === stepList.length - 1" @click="moveStepDown(index)">下移</el-button>
            <el-button link type="primary" :loading="stepSavingIndex === index" @click="saveStepItem(index, { force: true })">保存</el-button>
            <el-button link type="danger" @click="confirmRemoveStepItem(index)">删除</el-button>
          </div>
        </div>

        <el-input
          v-model="item.stepName"
          placeholder="请输入步骤名称"
          class="step-card__name"
          :maxlength="30"
          @blur="handleStepNameBlur(index)"
        />

        <RichTextQuillEditor
          :ref="richEditorRegistry.getCallback(item.uid)"
          :editor-key="`step-editor-${item.uid}`"
          v-model="item.stepComments"
          :toolbar="simpleToolbar"
          @blur="handleEditorBlur(index)"
        >
          <template #toolbar>
            <el-button size="small" @mousedown.prevent="openMaterialPicker(index)">选择图片素材</el-button>
          </template>
        </RichTextQuillEditor>
      </div>
    </div>

    <div v-else class="step-placeholder">请点击“增加步骤”添加实验步骤</div>

    <el-dialog v-model="materialPickerVisible" title="选择图片素材" width="1100px" class="user-dialog">
      <div class="picker-toolbar">
        <el-input
          v-model="materialPickerKeyword"
          placeholder="搜索素材名称/用途"
          clearable
          style="width: 260px"
          @clear="handleMaterialSearch"
          @keyup.enter="handleMaterialSearch"
        />
        <el-button type="primary" @click="handleMaterialSearch">查询</el-button>
      </div>
      <div v-loading="materialPickerLoading" class="picker-grid">
        <div
          v-for="item in materialPickerFiles"
          :key="getMaterialPickerItemId(item)"
          class="picker-card"
          :class="{ 'is-selected': selectedMaterialPickerId === getMaterialPickerItemId(item) }"
          @click="selectMaterialPickerItem(item)"
        >
          <div class="picker-card__preview">
            <el-image
              v-if="resolveFileUrl(item.previewUrl || item.coverImageUrl || item.fileUrl)"
              :src="resolveFileUrl(item.previewUrl || item.coverImageUrl || item.fileUrl)"
              fit="contain"
            />
            <div v-else class="material-card__empty">无主图</div>
          </div>
          <div class="picker-card__body">
            <div class="picker-card__name" :title="item.fileName || ''">
              {{ item.fileName || '未命名素材' }}
            </div>
            <div class="picker-card__actions">
              <el-button size="small" :type="selectedMaterialPickerId === getMaterialPickerItemId(item) ? 'success' : 'primary'" @click.stop="selectMaterialPickerItem(item)">
                {{ selectedMaterialPickerId === getMaterialPickerItemId(item) ? '已选择' : '选择' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="picker-footer">
        <div class="picker-footer__summary">共 {{ materialPickerTotal }} 条</div>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="materialPickerTotal"
          :current-page="materialPickerPage.pageNum"
          :page-size="materialPickerPage.pageSize"
          @current-change="handleMaterialPageChange"
        />
      </div>
      <template #footer>
        <el-button @click="materialPickerVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!selectedMaterialPickerId" @click="confirmMaterialSelection">插入图片</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RichTextQuillEditor from '../components/RichTextQuillEditor.vue'
import { deleteExpStep, fetchExpSteps, saveExpStep } from '../../../api/exp'
import { fetchImageMaterials } from '../../../api/data'


import { resolveApiEntityId } from '../utils/apiEntityId'
import { normalizeRichTextHtml } from '../utils/quillEditorSync'
import { createRichEditorRegistry, prepareRichTextForSave } from '../utils/richEditorRegistry'

const props = defineProps({
  expId: { type: [String, Number], required: true }
})

const stepList = ref([])
const stepSavingIndex = ref(-1)
const stepSaveLocks = new Set()
const richEditorRegistry = createRichEditorRegistry()
const currentEditorIndex = ref(-1)

const materialPickerVisible = ref(false)
const materialPickerLoading = ref(false)
const materialPickerKeyword = ref('')
const materialPickerFiles = ref([])
const materialPickerTotal = ref(0)
const selectedMaterialPickerId = ref('')
const materialPickerPage = ref({ pageNum: 1, pageSize: 12, total: 0 })

const simpleToolbar = [
  ['bold', 'italic', 'underline', 'strike'],
  [{ header: 1 }, { header: 2 }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ align: [] }],
  ['link', 'image', 'clean']
]

const resolveFileUrl = (url) => {
  const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
  if (!url) return ''
  const raw = String(url).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
}

const blobToDataUrl = (blob) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => resolve(String(reader.result || ''))
  reader.onerror = () => reject(new Error('图片转码失败'))
  reader.readAsDataURL(blob)
})

const imageUrlToDataUrl = async (url) => {
  const resp = await fetch(url, { mode: 'cors' })
  if (!resp.ok) throw new Error('图片下载失败')
  const blob = await resp.blob()
  return await blobToDataUrl(blob)
}

const getMaterialPickerItemId = (item) => String(
  item?.id ||
  item?.materialId ||
  item?.fileId ||
  item?.imageId ||
  item?.dataFileId ||
  item?.fileUrl ||
  item?.previewUrl ||
  item?.coverImageUrl ||
  ''
)

const createStepItem = (data = {}) => ({
  uid: data.uid || `step-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  stepId: data.stepId || '',
  stepName: data.stepName || '',
  stepComments: data.stepComments || '',
  sortOrder: data.sortOrder || 0,
  _lastSavedSignature: data._lastSavedSignature || ''
})

const getStepSignature = (item) => `${item.stepId || ''}::${item.stepName || ''}::${normalizeRichTextHtml(item.stepComments)}::${item.sortOrder || ''}`

const syncStepEditorContent = async (index) => {
  const item = stepList.value[index]
  if (!item) return
  await prepareRichTextForSave(richEditorRegistry, item.uid)
}

const handleEditorBlur = async (index) => {
  const item = stepList.value[index]
  if (!item || !item.stepId) return
  await saveStepItem(index)
}
const handleStepNameBlur = async (index) => {
  const item = stepList.value[index]
  if (!item || !item.stepId) return
  await saveStepItem(index)
}

const loadStepItems = async () => {
  if (!props.expId) return
  try {
    const res = await fetchExpSteps(props.expId)
    if (res.data.code === 200) {
      stepList.value = (Array.isArray(res.data.data) ? res.data.data : []).map((row, index) => {
        const item = createStepItem({
          stepId: String(row.stepId || row.id || row.step || ''),
          stepName: row.stepName ?? '',
          stepComments: row.stepComments ?? '',
          sortOrder: row.sortOrder ?? index + 1,
          _lastSavedSignature: ''
        })
        item._lastSavedSignature = getStepSignature(item)
        return item
      })
    }
  } catch {
    stepList.value = []
  }
}

const insertImageToEditor = async (imagePayload) => {
  const item = stepList.value[currentEditorIndex.value]
  if (!item) return false
  try {
    const imageHtml = imagePayload.startsWith('data:')
      ? imagePayload
      : await imageUrlToDataUrl(imagePayload)
    const imgHtml = `<p><img src="${imageHtml}" style="max-width: 400px; height: auto; display: block;" data-img-resizable="1" /></p><p><br></p>`
    const editor = richEditorRegistry.get(item.uid)
    if (editor?.appendHtml) {
      editor.appendHtml(imgHtml)
    } else {
      item.stepComments = `${item.stepComments || ''}${imgHtml}`
    }
    handleEditorBlur(currentEditorIndex.value)
    return true
  } catch (error) {
    console.error('insertImageToEditor failed', error)
    return false
  }
}

const getCurrentUserId = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.userId || userInfo.id || ''
  } catch {
    return ''
  }
}

const loadMaterialPickerFiles = async () => {
  materialPickerLoading.value = true
  try {
    const res = await fetchImageMaterials({
      keyword: materialPickerKeyword.value,
      currentUserId: getCurrentUserId(),
      pageNum: materialPickerPage.value.pageNum,
      pageSize: materialPickerPage.value.pageSize,
    })
    if (res.data.code === 200) {
      const data = res.data.data || {}
      const rows = Array.isArray(data) ? data : (data.records || data.list || [])
      materialPickerFiles.value = rows
      materialPickerTotal.value = Number(data.total || rows.length || 0)
      materialPickerPage.value.total = materialPickerTotal.value
    } else {
      materialPickerFiles.value = []
      materialPickerTotal.value = 0
      materialPickerPage.value.total = 0
    }
  } catch (error) {
    materialPickerFiles.value = []
    materialPickerTotal.value = 0
    materialPickerPage.value.total = 0
    ElMessage.error(error?.response?.data?.message || '加载素材图片失败')
  } finally {
    materialPickerLoading.value = false
  }
}

const openMaterialPicker = async (index) => {
  currentEditorIndex.value = index
  materialPickerVisible.value = true
  selectedMaterialPickerId.value = ''
  materialPickerPage.value.pageNum = 1
  materialPickerKeyword.value = ''
  await loadMaterialPickerFiles()
}

const handleMaterialSearch = async () => {
  materialPickerPage.value.pageNum = 1
  await loadMaterialPickerFiles()
}

const handleMaterialPageChange = async (page) => {
  materialPickerPage.value.pageNum = page
  await loadMaterialPickerFiles()
}

const selectMaterialPickerItem = (item) => {
  selectedMaterialPickerId.value = getMaterialPickerItemId(item)
}

const confirmMaterialSelection = async () => {
  const item = materialPickerFiles.value.find((row) => getMaterialPickerItemId(row) === selectedMaterialPickerId.value)
  if (!item) {
    ElMessage.warning('请先选择一张素材图片')
    return
  }
  const url = resolveFileUrl(item.previewUrl || item.coverImageUrl || item.fileUrl || item.mainPicUrl || '')
  if (!url) {
    ElMessage.warning('该素材没有可用图片')
    return
  }
  materialPickerVisible.value = false
  const ok = await insertImageToEditor(url)
  if (!ok) {
    ElMessage.warning('图片插入失败')
  }
}

const addStepItem = async () => {
  if (!props.expId) {
    ElMessage.warning('请先保存基础信息')
    return
  }
  const item = createStepItem({ sortOrder: stepList.value.length + 1 })
  stepList.value.push(item)
  try {
    const res = await saveExpStep(props.expId, {
      stepId: '',
      stepName: item.stepName || '',
      stepComments: item.stepComments || '',
      sortOrder: item.sortOrder || stepList.value.length
    })
    const stepId = resolveApiEntityId(res, ['stepId', 'id', 'value', 'result'])
    if (!stepId) throw new Error('未返回步骤ID')
    item.stepId = stepId
    item._lastSavedSignature = getStepSignature(item)
    ElMessage.success('步骤已新增')
  } catch (error) {
    stepList.value.pop()
    ElMessage.error(error?.response?.data?.message || error?.message || '步骤新增失败')
  }
}

const saveStepItem = async (index, { force = false, silent = false } = {}) => {
  const item = stepList.value[index]
  if (!item) return false
  if (!item.stepId) {
    ElMessage.warning('步骤尚未创建完成，请稍候或重新点击「增加步骤」')
    return false
  }
  await syncStepEditorContent(index)
  const signature = getStepSignature(item)
  if (!force && item._lastSavedSignature === signature) return true
  if (force && item._lastSavedSignature === signature) {
    if (!silent) ElMessage.info('内容未变化，无需保存')
    return true
  }
  const lockKey = item.uid
  if (stepSaveLocks.has(lockKey)) return false
  stepSaveLocks.add(lockKey)
  stepSavingIndex.value = index
  try {
    const payload = {
      stepId: item.stepId || '',
      stepName: item.stepName || '',
      stepComments: item.stepComments || '',
      sortOrder: item.sortOrder ?? index + 1
    }
    const res = await saveExpStep(props.expId, payload)
    const stepId = resolveApiEntityId(res, ['stepId', 'id', 'value', 'result']) || item.stepId
    if (stepId) item.stepId = stepId
    item._lastSavedSignature = getStepSignature(item)
    if (!silent) ElMessage.success('步骤已保存')
    return true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '步骤保存失败')
    return false
  } finally {
    stepSaveLocks.delete(lockKey)
    stepSavingIndex.value = -1
  }
}

const flushPendingSaves = async () => {
  const results = []
  for (let i = 0; i < stepList.value.length; i += 1) {
    const item = stepList.value[i]
    if (!item?.stepId) continue
    await syncStepEditorContent(i)
    if (item._lastSavedSignature === getStepSignature(item)) continue
    const ok = await saveStepItem(i, { force: true, silent: true })
    results.push({ index: i, ok })
  }
  return results
}

const confirmRemoveStepItem = async (index) => {
  const item = stepList.value[index]
  if (!item) return
  try {
    await ElMessageBox.confirm(`确定删除步骤【${item.stepName || `步骤 ${index + 1}`}】吗？`, '提示', { type: 'warning' })
    await removeStepItem(index)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.message || error?.message || '删除失败')
    }
  }
}

const removeStepItem = async (index) => {
  const item = stepList.value[index]
  if (!item) return
  try {
    if (item.stepId) await deleteExpStep(item.stepId)
    richEditorRegistry.remove(item.uid)
    stepList.value.splice(index, 1)
    stepList.value = stepList.value.map((row, i) => ({ ...row, sortOrder: i + 1 }))
    ElMessage.success('步骤已删除')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '步骤删除失败')
  }
}

const moveStepUp = (index) => {
  if (index <= 0) return
  const list = [...stepList.value]
  ;[list[index - 1], list[index]] = [list[index], list[index - 1]]
  stepList.value = list.map((row, i) => ({ ...row, sortOrder: i + 1 }))
}

const moveStepDown = (index) => {
  if (index >= stepList.value.length - 1) return
  const list = [...stepList.value]
  ;[list[index + 1], list[index]] = [list[index], list[index + 1]]
  stepList.value = list.map((row, i) => ({ ...row, sortOrder: i + 1 }))
}

onMounted(loadStepItems)

defineExpose({ flushPendingSaves })
</script>

<style scoped src="../css/ExpStandardCreateView.css"></style>
