<template>
  <div class="section-group">
    <div class="section-group__title">参考引用</div>
    <div v-if="referenceList.length" class="step-list">
      <div v-for="(item, index) in referenceList" :key="item.uid" class="step-card">
        <div class="step-card__header">
          <div class="step-card__title">参考 {{ index + 1 }}</div>
          <div class="step-card__actions">
            <el-button link type="primary" :disabled="index === 0" @click="moveReferenceUp(index)">上移</el-button>
            <el-button link type="primary" :disabled="index === referenceList.length - 1" @click="moveReferenceDown(index)">下移</el-button>
            <el-button link type="primary" :loading="referenceSavingIndex === index" @click="confirmSaveReferenceItem(index)">保存</el-button>
            <el-button link type="danger" @click="removeReferenceItem(index)">删除</el-button>
          </div>
        </div>
        <div class="step-card__field-row">
          <span class="step-card__field-label">引用名称：</span>
          <el-input v-model="item.referenceName" placeholder="请输入引用名称" class="step-card__field-input" @blur="handleReferenceNameBlur(index)" :maxlength="30" />
        </div>
        <div class="step-card__field-row">
          <span class="step-card__field-label">引用出处：</span>
          <el-input v-model="item.referenceSource" placeholder="请输入引用出处或链接" class="step-card__field-input" @blur="handleReferenceSourceBlur(index)" :maxlength="30" />
        </div>
        <RichTextQuillEditor
          :ref="editorRegistry.getCallback(item.uid)"
          :editor-key="`reference-editor-${item.uid}`"
          v-model="item.referenceComments"
          :toolbar="simpleToolbar"
          @blur="handleReferenceContentBlur(index)"
        >
          <template #toolbar>
            <el-button size="small" @mousedown.prevent="openMaterialPicker(index)">选择图片素材</el-button>
            <input ref="imageInputRef" type="file" accept="image/*" hidden @change="handleImageUpload" />
          </template>
        </RichTextQuillEditor>
      </div>
    </div>
    <div v-else class="step-placeholder">请点击"增加参考引用"添加参考引用信息</div>

    <el-dialog v-model="materialPickerVisible" title="选择图片素材" width="1100px" class="user-dialog">
      <div class="picker-toolbar">
        <el-input v-model="materialPickerKeyword" placeholder="搜索素材名称/用途" clearable style="width: 260px" @clear="handleMaterialSearch" @keyup.enter="handleMaterialSearch" />
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
            <el-image v-if="resolveFileUrl(item.previewUrl || item.coverImageUrl || item.fileUrl)" :src="resolveFileUrl(item.previewUrl || item.coverImageUrl || item.fileUrl)" fit="contain" />
            <div v-else class="material-card__empty">无主图</div>
          </div>
          <div class="picker-card__body">
            <div class="picker-card__name" :title="item.fileName || ''">{{ item.fileName || '未命名素材' }}</div>
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
        <el-pagination background layout="prev, pager, next" :total="materialPickerTotal" :current-page="materialPickerPage.pageNum" :page-size="materialPickerPage.pageSize" @current-change="handleMaterialPageChange" />
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
import { ElMessage } from 'element-plus'
import RichTextQuillEditor from '../components/RichTextQuillEditor.vue'
import { deleteExpReference, fetchExpReferences, saveExpReference } from '../../../api/exp'
import { fetchImageMaterials } from '../../../api/data'
import { uploadFile } from '../../../api/system'
import { resolveApiEntityId } from '../utils/apiEntityId'
import { normalizeRichTextHtml } from '../utils/quillEditorSync'
import { createRichEditorRegistry, prepareRichTextForSave } from '../utils/richEditorRegistry'

const props = defineProps({ expId: { type: [String, Number], required: true } })

const referenceList = ref([])
const referenceSavingIndex = ref(-1)
const editorRegistry = createRichEditorRegistry()
const saveLocks = new Set()
const currentEditorIndex = ref(-1)
const imageInputRef = ref(null)

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

const createDataUrl = (blob) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => resolve(String(reader.result || ''))
  reader.onerror = () => reject(new Error('图片转码失败'))
  reader.readAsDataURL(blob)
})

const imageUrlToDataUrl = async (url) => {
  const resp = await fetch(url, { mode: 'cors' })
  if (!resp.ok) throw new Error('图片下载失败')
  return await createDataUrl(await resp.blob())
}

const resolveFileUrl = (url) => {
  const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
  if (!url) return ''
  const raw = String(url).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
}

const getMaterialPickerItemId = (item) => String(item?.id || item?.materialId || item?.fileId || item?.imageId || item?.dataFileId || item?.fileUrl || item?.previewUrl || item?.coverImageUrl || '')

const createItem = (data = {}) => ({
  uid: data.uid || `reference-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  referenceId: data.referenceId || '',
  referenceName: data.referenceName || '',
  referenceSource: data.referenceSource || '',
  referenceComments: data.referenceComments || '',
  sortOrder: data.sortOrder || 0,
  _lastSavedSignature: data._lastSavedSignature || ''
})

const getSignature = (item) => `${item.referenceId || ''}::${item.referenceName || ''}::${item.referenceSource || ''}::${normalizeRichTextHtml(item.referenceComments)}::${item.sortOrder || ''}`

const syncEditorContent = async (index) => {
  const item = referenceList.value[index]
  if (!item) return
  await prepareRichTextForSave(editorRegistry, item.uid)
}

const buildPayload = (item, index) => ({
  referenceId: item.referenceId || '',
  referenceName: item.referenceName || '',
  referenceSource: item.referenceSource || '',
  referenceComments: item.referenceComments || '',
  sortOrder: item.sortOrder ?? index + 1
})

const insertImageToEditor = async (imagePayload) => {
  const item = referenceList.value[currentEditorIndex.value]
  if (!item) return false
  try {
    const dataUrl = imagePayload.startsWith('data:') ? imagePayload : await imageUrlToDataUrl(imagePayload)
    const html = `<p><img src="${dataUrl}" style="max-width: 400px; height: auto; display: block;" data-img-resizable="1" /></p><p><br></p>`
    const editor = editorRegistry.get(item.uid)
    if (editor?.appendHtml) {
      editor.appendHtml(html)
    } else {
      item.referenceComments = `${item.referenceComments || ''}${html}`
    }
    return true
  } catch (error) {
    console.error('insertImageToEditor failed', error)
    return false
  }
}

const loadItems = async () => {
  if (!props.expId) return
  const res = await fetchExpReferences(props.expId)
  if (res.data.code === 200) {
    referenceList.value = (Array.isArray(res.data.data) ? res.data.data : []).map((row, index) => {
      const item = createItem({ referenceId: String(row.referenceId || row.id || ''), referenceName: row.referenceName ?? '', referenceSource: row.referenceSource ?? '', referenceComments: row.referenceComments ?? '', sortOrder: row.sortOrder ?? index + 1 })
      item._lastSavedSignature = getSignature(item)
      return item
    })
  }
}

const getCurrentUserId = () => {
  try { const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}'); return userInfo.userId || userInfo.id || '' } catch { return '' }
}

const loadMaterialPickerFiles = async () => {
  materialPickerLoading.value = true
  try {
    const res = await fetchImageMaterials({ keyword: materialPickerKeyword.value, currentUserId: getCurrentUserId(), pageNum: materialPickerPage.value.pageNum, pageSize: materialPickerPage.value.pageSize })
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

const handleMaterialSearch = async () => { materialPickerPage.value.pageNum = 1; await loadMaterialPickerFiles() }
const handleMaterialPageChange = async (page) => { materialPickerPage.value.pageNum = page; await loadMaterialPickerFiles() }
const selectMaterialPickerItem = (item) => { selectedMaterialPickerId.value = getMaterialPickerItemId(item) }

const confirmMaterialSelection = async () => {
  const item = materialPickerFiles.value.find((row) => getMaterialPickerItemId(row) === selectedMaterialPickerId.value)
  if (!item) return ElMessage.warning('请先选择一张素材图片')
  const url = resolveFileUrl(item.previewUrl || item.coverImageUrl || item.fileUrl || item.mainPicUrl || '')
  if (!url) return ElMessage.warning('该素材没有可用图片')
  materialPickerVisible.value = false
  await Promise.resolve()
  const ok = await insertImageToEditor(url)
  if (!ok) ElMessage.warning('图片插入失败')
}

const addItem = async () => {
  if (!props.expId) {
    ElMessage.warning('请先保存基础信息')
    return
  }
  const item = createItem({ sortOrder: referenceList.value.length + 1 })
  referenceList.value.push(item)
  try {
    const res = await saveExpReference(props.expId, buildPayload(item, referenceList.value.length - 1))
    const referenceId = resolveApiEntityId(res, ['referenceId', 'id', 'value'])
    if (!referenceId) throw new Error('未返回引用ID')
    item.referenceId = referenceId
    item._lastSavedSignature = getSignature(item)
    ElMessage.success('参考引用已新增')
  } catch (error) {
    referenceList.value.pop()
    ElMessage.error(error?.response?.data?.message || error?.message || '参考引用新增失败')
  }
}

const saveWithRetry = async (saveFn, { retries = 2, delay = 400 } = {}) => {
  let lastError = null
  for (let attempt = 0; attempt <= retries; attempt += 1) {
    try { return await saveFn(attempt) } catch (error) { lastError = error; if (attempt < retries) await new Promise((resolve) => setTimeout(resolve, delay * (attempt + 1))) }
  }
  throw lastError || new Error('保存失败')
}

const saveItem = async (index, { force = false, silent = false } = {}) => {
  const item = referenceList.value[index]
  if (!item) return false
  if (!item.referenceId) {
    ElMessage.warning('参考引用尚未创建完成，请稍候或重新点击「增加参考引用」')
    return false
  }
  await syncEditorContent(index)
  const signature = getSignature(item)
  if (!force && item._lastSavedSignature === signature) return true
  if (force && item._lastSavedSignature === signature) {
    if (!silent) ElMessage.info('内容未变化，无需保存')
    return true
  }
  const lockKey = item.uid
  if (saveLocks.has(lockKey)) return false
  saveLocks.add(lockKey)
  referenceSavingIndex.value = index
  try {
    await saveWithRetry(() => saveExpReference(props.expId, buildPayload(item, index)))
    item._lastSavedSignature = getSignature(item)
    if (!silent) ElMessage.success(`参考 ${index + 1} 已保存`)
    return true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '参考引用保存失败，请重试')
    throw error
  } finally {
    saveLocks.delete(lockKey)
    referenceSavingIndex.value = -1
  }
}

const confirmSaveReferenceItem = async (index) => { try { await saveItem(index, { force: true }) } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.response?.data?.message || error?.message || '提交失败') } }

const flushPendingSaves = async () => {
  const results = []
  for (let i = 0; i < referenceList.value.length; i += 1) {
    const item = referenceList.value[i]
    if (!item?.referenceId) continue
    await syncEditorContent(i)
    if (item._lastSavedSignature === getSignature(item)) continue
    const ok = await saveItem(i, { force: true, silent: true }).catch(() => false)
    results.push({ index: i, ok })
  }
  return results
}

const removeReferenceItem = async (index) => {
  const item = referenceList.value[index]
  if (!item) return
  if (item.referenceId) await deleteExpReference(item.referenceId)
  editorRegistry.remove(item.uid)
  referenceList.value.splice(index, 1)
}

const moveReferenceUp = (index) => {
  if (index <= 0) return
  const list = referenceList.value
  ;[list[index - 1], list[index]] = [list[index], list[index - 1]]
}

const moveReferenceDown = (index) => {
  if (index >= referenceList.value.length - 1) return
  const list = referenceList.value
  ;[list[index + 1], list[index]] = [list[index], list[index + 1]]
}

const handleReferenceNameBlur = async (index) => { const item = referenceList.value[index]; if (!item?.referenceId) return; await saveItem(index) }
const handleReferenceSourceBlur = async (index) => { const item = referenceList.value[index]; if (!item?.referenceId) return; await saveItem(index) }
const handleReferenceContentBlur = async (index) => { const item = referenceList.value[index]; if (!item?.referenceId) return; await saveItem(index) }

const handleImageUpload = async (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return
  try {
    const reader = new FileReader()
    const dataUrl = await new Promise((resolve, reject) => { reader.onload = () => resolve(String(reader.result || '')); reader.onerror = () => reject(new Error('图片转码失败')); reader.readAsDataURL(file) })
    const res = await uploadFile(file)
    if (res.data.code !== 200) throw new Error(res.data.message || '图片上传失败')
    const ok = await insertImageToEditor(dataUrl)
    if (!ok) throw new Error('图片插入失败')
  } catch (error) {
    ElMessage.error(error?.message || '图片插入失败')
  } finally {
    if (event?.target) event.target.value = ''
  }
}

onMounted(loadItems)

defineExpose({ addItem, flushPendingSaves })
</script>
<style scoped src="../css/ExpStandardCreateView.css"></style>
