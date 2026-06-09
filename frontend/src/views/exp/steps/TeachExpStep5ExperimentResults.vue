<template>
  <div class="step-block">
    <div class="video-step__toolbar">
      <div class="video-step__title">实验结果</div>
      <div class="video-step__actions">
        <el-button type="primary" @click="addResultItem">增加结果</el-button>
      </div>
    </div>

    <div v-if="resultList.length" class="step-list">
      <div v-for="(item, index) in resultList" :key="item.resultId || item.uid" class="step-card">
        <div class="step-card__header">
          <div class="step-card__title">结果 {{ index + 1 }}</div>
          <div class="step-card__actions">
            <el-button link type="primary" :disabled="index === 0" @click="moveResultUp(index)">上移</el-button>
            <el-button link type="primary" :disabled="index === resultList.length - 1" @click="moveResultDown(index)">下移</el-button>
            <el-button link type="primary" :loading="resultSavingIndex === index" @click="saveResultItem(index)">保存</el-button>
            <el-button link type="danger" @click="confirmRemoveResultItem(index)">删除</el-button>
          </div>
        </div>

        <el-input
          v-model="item.resultName"
          placeholder="请输入结果名称"
          class="step-card__name"
          :maxlength="30"
          @blur="handleResultNameBlur(index)"
        />

        <div class="step-card__editor-shell">
          <div class="step-card__toolbar">
            <el-button size="small" @mousedown.prevent="openMaterialPicker(index)">选择图片素材</el-button>
            <input ref="imageInputRef" type="file" accept="image/*" hidden @change="handleImageUpload" />
          </div>
          <QuillEditor
            :ref="(el) => setEditorRef(item.uid, el)"
            v-model:content="item.resultComments"
            content-type="html"
            theme="snow"
            :toolbar="simpleToolbar"
            class="step-card__editor"
            :style="{ minHeight: '240px', height: '240px' }"
            @ready="(editor) => handleEditorReady(item.uid, editor)"
            @textChange="handleEditorChange(index)"
            @blur="handleEditorBlur(index)"
          />
        </div>
      </div>
    </div>

    <div v-else class="step-placeholder">请点击“增加结果”添加实验结果</div>

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
              <el-button
                size="small"
                :type="selectedMaterialPickerId === getMaterialPickerItemId(item) ? 'success' : 'primary'"
                @click.stop="selectMaterialPickerItem(item)"
              >
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import Quill from 'quill'
import { deleteExpResult, fetchExpResults, saveExpResult } from '../../../api/exp'
import { fetchImageMaterials } from '../../../api/data'
import { uploadFile } from '../../../api/system'

const props = defineProps({
  expId: { type: [String, Number], required: true }
})

const resultList = ref([])
const resultSavingIndex = ref(-1)
const resultSaveLocks = new Set()
const editorRefs = ref({})
const currentEditorUid = ref('')
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
  item?.id || item?.materialId || item?.fileId || item?.imageId || item?.dataFileId || item?.fileUrl || item?.previewUrl || item?.coverImageUrl || ''
)

const createResultItem = (data = {}) => ({
  uid: data.uid || `result-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  resultId: data.resultId || '',
  resultName: data.resultName || '',
  resultComments: data.resultComments || '',
  sortOrder: data.sortOrder || 0,
  _lastSavedSignature: data._lastSavedSignature || ''
})

const getResultSignature = (item) => `${item.resultId || ''}::${item.resultName || ''}::${item.resultComments || ''}::${item.sortOrder || ''}`

const setEditorRef = (uid, el) => {
  if (el) editorRefs.value[uid] = el
}

const handleEditorReady = (uid, editor) => {
  editorRefs.value[uid] = editor?.getQuill?.() || editor
  currentEditorUid.value = uid
}

const handleEditorChange = (index) => {
  const item = resultList.value[index]
  if (!item) return
  item.resultComments = item.resultComments || ''
}

const handleEditorBlur = async (index) => {
  const item = resultList.value[index]
  if (!item || !item.resultId) return
  await saveResultItem(index)
}

const handleResultNameBlur = async (index) => {
  const item = resultList.value[index]
  if (!item || !item.resultId) return
  await saveResultItem(index)
}

const loadResultItems = async () => {
  if (!props.expId) return
  try {
    const res = await fetchExpResults(props.expId)
    if (res.data.code === 200) {
      resultList.value = (Array.isArray(res.data.data) ? res.data.data : []).map((row, index) =>
        createResultItem({
          resultId: String(row.resultId || row.id || row.result || ''),
          resultName: row.resultName ?? '',
          resultComments: row.resultComments ?? '',
          sortOrder: row.sortOrder ?? index + 1,
          _lastSavedSignature: ''
        })
      )
    }
  } catch {
    resultList.value = []
  }
}

const addResultItem = async () => {
  if (!props.expId) {
    ElMessage.warning('请先保存基础信息')
    return
  }
  const item = createResultItem({ sortOrder: resultList.value.length + 1 })
  resultList.value.push(item)
  try {
    const res = await saveExpResult(props.expId, {
      resultId: '',
      resultName: item.resultName || '',
      resultComments: item.resultComments || '',
      sortOrder: item.sortOrder || resultList.value.length
    })
    const resultId = res?.data?.data || res?.data
    if (!resultId) throw new Error('未返回结果ID')
    item.resultId = typeof resultId === 'object' ? (resultId.resultId || resultId.id || resultId.value || resultId.result || '') : resultId
    item._lastSavedSignature = getResultSignature(item)
    ElMessage.success('结果已新增')
  } catch (error) {
    resultList.value.pop()
    ElMessage.error(error?.response?.data?.message || error?.message || '结果新增失败')
  }
}

const saveResultItem = async (index) => {
  const item = resultList.value[index]
  if (!item || !item.resultId) return false
  const signature = getResultSignature(item)
  if (item._lastSavedSignature === signature) return true
  if (resultSaveLocks.has(index)) return false
  resultSaveLocks.add(index)
  resultSavingIndex.value = index
  try {
    const payload = {
      resultId: item.resultId || '',
      resultName: item.resultName || '',
      resultComments: item.resultComments || '',
      sortOrder: item.sortOrder ?? index + 1
    }
    const res = await saveExpResult(props.expId, payload)
    const result = res?.data?.data || res?.data || {}
    const resultId = result.resultId || result.id || result.value || result.result || item.resultId || ''
    if (resultId) item.resultId = resultId
    item._lastSavedSignature = getResultSignature(item)
    ElMessage.success('结果已保存')
    return true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '结果保存失败')
    return false
  } finally {
    resultSaveLocks.delete(index)
    resultSavingIndex.value = -1
  }
}

const confirmRemoveResultItem = async (index) => {
  const item = resultList.value[index]
  if (!item) return
  try {
    await ElMessageBox.confirm(`确定删除结果【${item.resultName || `结果 ${index + 1}`}】吗？`, '提示', { type: 'warning' })
    await removeResultItem(index)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.message || error?.message || '删除失败')
    }
  }
}

const removeResultItem = async (index) => {
  const item = resultList.value[index]
  if (!item) return
  try {
    if (item.resultId) await deleteExpResult(item.resultId)
    resultList.value.splice(index, 1)
    resultList.value = resultList.value.map((row, i) => ({ ...row, sortOrder: i + 1 }))
    ElMessage.success('结果已删除')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '结果删除失败')
    await loadResultItems()
  }
}

const moveResultUp = (index) => {
  if (index <= 0) return
  const list = [...resultList.value]
  ;[list[index - 1], list[index]] = [list[index], list[index - 1]]
  resultList.value = list.map((row, i) => ({ ...row, sortOrder: i + 1 }))
}

const moveResultDown = (index) => {
  if (index >= resultList.value.length - 1) return
  const list = [...resultList.value]
  ;[list[index + 1], list[index]] = [list[index], list[index + 1]]
  resultList.value = list.map((row, i) => ({ ...row, sortOrder: i + 1 }))
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
  currentEditorUid.value = resultList.value[index]?.uid || ''
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

const insertImageToEditor = async (imagePayload) => {
  const item = resultList.value.find((row) => row.uid === currentEditorUid.value)
  if (!item) return false
  try {
    const imageHtml = imagePayload.startsWith('data:')
      ? imagePayload
      : await imageUrlToDataUrl(imagePayload)
    const imgHtml = `<p><img src="${imageHtml}" style="max-width: 400px; height: auto; display: block;" data-img-resizable="1" /></p><p><br></p>`
    item.resultComments = `${item.resultComments || ''}${imgHtml}`
    const editor = editorRefs.value[item.uid]
    if (editor?.root) editor.root.innerHTML = item.resultComments
    
    return true
  } catch (error) {
    console.error('insertImageToEditor failed', error)
    return false
  }
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
  if (!ok) ElMessage.warning('图片插入失败')
}

const handleImageUpload = async (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file)
    if (res.data.code !== 200) throw new Error(res.data.message || '图片上传失败')
    const url = resolveFileUrl(res.data.data?.fileUrl || '')
    if (!url) throw new Error('未返回图片地址')
    const ok = await insertImageToEditor(url)
    if (!ok) throw new Error('图片插入失败')
  } catch (error) {
    ElMessage.error(error?.message || '图片插入失败')
  } finally {
    if (event?.target) event.target.value = ''
  }
}

onMounted(loadResultItems)
</script>

<style scoped src="../css/ExpStandardCreateView.css"></style>
