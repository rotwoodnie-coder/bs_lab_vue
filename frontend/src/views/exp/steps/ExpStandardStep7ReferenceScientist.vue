<template>
  <div class="step-block">
    <div class="video-step__toolbar">
      <div class="video-step__title">参考与故事</div>
      <div class="video-step__actions">
        <el-button type="primary" @click="addReferenceItem">增加参考引用</el-button>
        <el-button type="primary" @click="addScientistItem">增加科学家故事</el-button>
      </div>
    </div>

    <div class="section-group">
      <div class="section-group__title">参考引用</div>
      <div v-if="referenceList.length" class="step-list">
        <div v-for="(item, index) in referenceList" :key="item.referenceId || item.uid" class="step-card">
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
          <div class="step-card__editor-shell">
            <div class="step-card__toolbar">
              <el-button size="small" @mousedown.prevent="openMaterialPicker('reference', index)">选择图片素材</el-button>
              <input ref="imageInputRef" type="file" accept="image/*" hidden @change="handleImageUpload" />
            </div>
            <QuillEditor
              v-model:content="item.referenceComments"
              content-type="html"
              theme="snow"
              :toolbar="simpleToolbar"
              class="step-card__editor"
              :style="{ minHeight: '240px', height: '240px' }"
              @ready="(editor) => handleEditorReady('reference', item.uid, editor)"
              @textChange="handleReferenceContentChange(index)"
              @blur="handleReferenceContentBlur(index)"
            />
          </div>
        </div>
      </div>
      <div v-else class="step-placeholder">请点击“增加参考引用”添加参考引用信息</div>
    </div>

    <div class="section-group">
      <div class="section-group__title">科学家故事</div>
      <div v-if="scientistList.length" class="step-list">
        <div v-for="(item, index) in scientistList" :key="item.scientistId || item.uid" class="step-card">
          <div class="step-card__header">
            <div class="step-card__title">故事 {{ index + 1 }}</div>
            <div class="step-card__actions">
              <el-button link type="primary" :disabled="index === 0" @click="moveScientistUp(index)">上移</el-button>
              <el-button link type="primary" :disabled="index === scientistList.length - 1" @click="moveScientistDown(index)">下移</el-button>
              <el-button link type="primary" :loading="scientistSavingIndex === index" @click="confirmSaveScientistItem(index)">保存</el-button>
              <el-button link type="danger" @click="removeScientistItem(index)">删除</el-button>
            </div>
          </div>
          <div class="step-card__field-row">
            <span class="step-card__field-label">科学家：</span>
            <el-input v-model="item.scientistName" placeholder="请输入科学家名称" class="step-card__field-input" @blur="handleScientistNameBlur(index)" :maxlength="30" />
          </div>
          <div class="step-card__field-row">
            <span class="step-card__field-label">故事标题：</span>
            <el-input v-model="item.storyName" placeholder="请输入故事标题" class="step-card__field-input" @blur="handleStoryNameBlur(index)" :maxlength="30" />
          </div>
          <div class="step-card__editor-shell">
            <div class="step-card__toolbar">
              <el-button size="small" @mousedown.prevent="openMaterialPicker('scientist', index)">选择图片素材</el-button>
              <input ref="imageInputRef" type="file" accept="image/*" hidden @change="handleImageUpload" />
            </div>
            <QuillEditor
              :ref="(el) => setEditorRef('scientist', item.uid, el)"
              v-model:content="item.storyComments"
              content-type="html"
              theme="snow"
              :toolbar="simpleToolbar"
              class="step-card__editor"
              :style="{ minHeight: '240px', height: '240px' }"
              @ready="(editor) => handleEditorReady('scientist', item.uid, editor)"
              @textChange="handleScientistContentChange(index)"
              @blur="handleScientistContentBlur(index)"
            />
          </div>
        </div>
      </div>
      <div v-else class="step-placeholder">请点击“增加科学家故事”添加故事内容</div>
    </div>

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
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { deleteExpReference, deleteExpScientist, fetchExpReferences, fetchExpScientists, saveExpReference, saveExpScientist } from '../../../api/exp'
import { fetchImageMaterials } from '../../../api/data'
import { uploadFile } from '../../../api/system'

const props = defineProps({ expId: { type: [String, Number], required: true } })

const referenceList = ref([])
const scientistList = ref([])
const referenceSavingIndex = ref(-1)
const scientistSavingIndex = ref(-1)
const editorRefs = ref({ reference: {}, scientist: {} })
const currentEditor = ref({ type: 'reference', uid: '' })

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

const createReferenceItem = (data = {}) => ({ uid: data.uid || `reference-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`, referenceId: data.referenceId || '', referenceName: data.referenceName || '', referenceSource: data.referenceSource || '', referenceComments: data.referenceComments || '', sortOrder: data.sortOrder || 0, _lastSavedSignature: data._lastSavedSignature || '' })
const createScientistItem = (data = {}) => ({ uid: data.uid || `scientist-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`, scientistId: data.scientistId || '', scientistName: data.scientistName || '', storyName: data.storyName || '', storyComments: data.storyComments || '', sortOrder: data.sortOrder || 0, _lastSavedSignature: data._lastSavedSignature || '' })

const getReferenceSignature = (item) => `${item.referenceId || ''}::${item.referenceName || ''}::${item.referenceSource || ''}::${item.referenceComments || ''}::${item.sortOrder || ''}`
const getScientistSignature = (item) => `${item.scientistId || ''}::${item.scientistName || ''}::${item.storyName || ''}::${item.storyComments || ''}::${item.sortOrder || ''}`

const loadReferenceItems = async () => {
  if (!props.expId) return
  const res = await fetchExpReferences(props.expId)
  if (res.data.code === 200) {
    referenceList.value = (Array.isArray(res.data.data) ? res.data.data : []).map((row, index) => createReferenceItem({ referenceId: String(row.referenceId || row.id || ''), referenceName: row.referenceName ?? '', referenceSource: row.referenceSource ?? '', referenceComments: row.referenceComments ?? '', sortOrder: row.sortOrder ?? index + 1 }))
  }
}

const loadScientistItems = async () => {
  if (!props.expId) return
  const res = await fetchExpScientists(props.expId)
  if (res.data.code === 200) {
    scientistList.value = (Array.isArray(res.data.data) ? res.data.data : []).map((row, index) => createScientistItem({ scientistId: String(row.scientistId || row.id || ''), scientistName: row.scientistName ?? '', storyName: row.storyName ?? '', storyComments: row.storyComments ?? '', sortOrder: row.sortOrder ?? index + 1 }))
  }
}

const setEditorRef = (type, uid, el) => {
  if (!el) return
  editorRefs.value[type][uid] = el
}

const handleEditorReady = (type, uid) => {
  currentEditor.value = { type, uid }
}

const getActiveListAndItem = () => {
  const { type, uid } = currentEditor.value
  const list = type === 'scientist' ? scientistList.value : referenceList.value
  const index = list.findIndex((item) => item.uid === uid)
  return { type, uid, list, index, item: index >= 0 ? list[index] : null }
}

const insertImageToEditor = async (imagePayload) => {
  const { type, item } = getActiveListAndItem()
  if (!item) return false
  try {
    const dataUrl = imagePayload.startsWith('data:') ? imagePayload : await imageUrlToDataUrl(imagePayload)
    const html = `<p><img src="${dataUrl}" style="max-width: 400px; height: auto; display: block;" data-img-resizable="1" /></p><p><br></p>`
    if (type === 'scientist') item.storyComments = `${item.storyComments || ''}${html}`
    else item.referenceComments = `${item.referenceComments || ''}${html}`
    const editorEl = editorRefs.value[type]?.[item.uid]
    const editor = editorEl?.getQuill?.() || editorEl
    if (editor?.root) {
      editor.root.innerHTML = type === 'scientist' ? item.storyComments : item.referenceComments
    }
    return true
  } catch (error) {
    console.error('insertImageToEditor failed', error)
    return false
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

const openMaterialPicker = async (type, index) => {
  currentEditor.value = { type, uid: (type === 'scientist' ? scientistList.value[index]?.uid : referenceList.value[index]?.uid) || '' }
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

const addReferenceItem = async () => {
  const item = createReferenceItem({ sortOrder: referenceList.value.length + 1 })
  referenceList.value.push(item)
  const res = await saveExpReference(props.expId, item)
  if (res?.data?.data) item.referenceId = res.data.data.referenceId || res.data.data.id || item.referenceId
}

const addScientistItem = async () => {
  const item = createScientistItem({ sortOrder: scientistList.value.length + 1 })
  scientistList.value.push(item)
  const res = await saveExpScientist(props.expId, item)
  if (res?.data?.data) item.scientistId = res.data.data.scientistId || res.data.data.id || item.scientistId
}

const saveWithRetry = async (saveFn, { retries = 2, delay = 400 } = {}) => {
  let lastError = null
  for (let attempt = 0; attempt <= retries; attempt += 1) {
    try { return await saveFn(attempt) } catch (error) { lastError = error; if (attempt < retries) await new Promise((resolve) => setTimeout(resolve, delay * (attempt + 1))) }
  }
  throw lastError || new Error('保存失败')
}

const saveReferenceItem = async (index) => {
  const item = referenceList.value[index]
  if (!item) return
  referenceSavingIndex.value = index
  try { await saveWithRetry(() => saveExpReference(props.expId, item)); item._lastSavedSignature = getReferenceSignature(item); ElMessage.success(`参考 ${index + 1} 已保存`) } catch (error) { ElMessage.error(error?.response?.data?.message || error?.message || '参考引用保存失败，请重试'); throw error } finally { referenceSavingIndex.value = -1 }
}
const confirmSaveReferenceItem = async (index) => { try { await saveReferenceItem(index) } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.response?.data?.message || error?.message || '提交失败') } }
const saveScientistItem = async (index) => {
  const item = scientistList.value[index]
  if (!item) return
  scientistSavingIndex.value = index
  try { await saveWithRetry(() => saveExpScientist(props.expId, item)); item._lastSavedSignature = getScientistSignature(item); ElMessage.success(`故事 ${index + 1} 已保存`) } catch (error) { ElMessage.error(error?.response?.data?.message || error?.message || '科学家故事保存失败，请重试'); throw error } finally { scientistSavingIndex.value = -1 }
}
const confirmSaveScientistItem = async (index) => { try { await saveScientistItem(index) } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.response?.data?.message || error?.message || '提交失败') } }

const removeReferenceItem = async (index) => { const item = referenceList.value[index]; if (!item) return; if (item.referenceId) await deleteExpReference(item.referenceId); referenceList.value.splice(index, 1) }
const removeScientistItem = async (index) => { const item = scientistList.value[index]; if (!item) return; if (item.scientistId) await deleteExpScientist(item.scientistId); scientistList.value.splice(index, 1) }
const moveReferenceUp = async (index) => { if (index <= 0) return; const list = referenceList.value; ;[list[index - 1], list[index]] = [list[index], list[index - 1]] }
const moveReferenceDown = async (index) => { if (index >= referenceList.value.length - 1) return; const list = referenceList.value; ;[list[index + 1], list[index]] = [list[index], list[index + 1]] }
const moveScientistUp = async (index) => { if (index <= 0) return; const list = scientistList.value; ;[list[index - 1], list[index]] = [list[index], list[index - 1]] }
const moveScientistDown = async (index) => { if (index >= scientistList.value.length - 1) return; const list = scientistList.value; ;[list[index + 1], list[index]] = [list[index], list[index + 1]] }

const handleReferenceNameBlur = async (index) => { await saveReferenceItem(index) }
const handleReferenceSourceBlur = async (index) => { await saveReferenceItem(index) }
const handleScientistNameBlur = async (index) => { await saveScientistItem(index) }
const handleStoryNameBlur = async (index) => { await saveScientistItem(index) }
const handleReferenceContentChange = (index) => { const item = referenceList.value[index]; if (item) item.referenceComments = item.referenceComments || '' }
const handleScientistContentChange = (index) => { const item = scientistList.value[index]; if (item) item.storyComments = item.storyComments || '' }
const handleReferenceContentBlur = async (index) => { await saveReferenceItem(index) }
const handleScientistContentBlur = async (index) => { await saveScientistItem(index) }

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

const reloadAll = async () => { await loadReferenceItems(); await loadScientistItems() }

onMounted(async () => { await reloadAll() })
</script>
<style scoped src="../css/ExpStandardCreateView.css"></style>
