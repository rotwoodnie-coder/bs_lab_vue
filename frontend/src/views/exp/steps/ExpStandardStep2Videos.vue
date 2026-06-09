<template>
  <div class="video-step">
    <div class="video-step__toolbar">
      <div class="video-step__title">实验视频</div>
      <div class="video-step__actions">
        <MinioButtonUploader
          button-text="上传视频"
          accept=".mp4,.mov,.webm,.m4v,.avi"
          :max-size="500 * 1024 * 1024"
          @uploaded="handleVideoUploaded"
        />
        <el-button @click="openVideoPicker">从素材库选择</el-button>
      </div>
    </div>

    <div class="video-grid" @dragover.prevent @drop.prevent="onGridDrop(-1)">
      <div
        v-for="(item, index) in videoList"
        :key="item.seqId || item.fileId || item.videoUrl || index"
        class="video-card"
        draggable="true"
        @dragstart="onGridDragStart(index)"
        @dragover.prevent
        @drop.prevent="onGridDrop(index)"
      >
        <div class="video-card__preview">
          <video
            :ref="(el) => setVideoRef(el, index)"
            :src="resolveFileUrl(item.previewUrl || item.videoUrl || '')"
            controls
            playsinline
            preload="metadata"
            @play="handleVideoPlay(index)"
          ></video>
          <div v-if="selectedVideoIndex === index" class="video-card__playing-mask">正在播放</div>
        </div>
        <div class="video-card__body">
          <div class="video-card__title" :title="item.fileName || '视频素材'">{{ item.fileName || '视频素材' }}</div>
          <div class="video-card__meta">排序 {{ item.sortOrder ?? index + 1 }}</div>
        </div>
        <div class="video-card__actions">
          <el-button link type="primary" :disabled="index === 0" @click="moveVideoUp(index)">上移</el-button>
          <el-button link type="primary" :disabled="index === videoList.length - 1" @click="moveVideoDown(index)">下移</el-button>
          <el-button link type="danger" @click="removeVideo(index)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="videoPickerVisible" title="选择视频素材" width="1100px">
      <div class="picker-toolbar">
        <el-input v-model="videoPickerQuery.keyword" placeholder="搜索文件名称/标签" clearable style="width: 260px" @clear="loadVideoMaterials" @keyup.enter="loadVideoMaterials" />
        <el-button type="primary" @click="loadVideoMaterials">查询</el-button>
      </div>
      <div v-loading="videoPickerLoading" class="picker-grid">
        <div v-for="(item, index) in videoMaterialTable" :key="item.fileId || item.fileUrl || index" class="picker-card">
          <div class="picker-card__preview">
            <video
              :ref="(el) => setPickerVideoRef(el, index)"
              :src="resolveFileUrl(item.previewUrl || item.fileUrl || '')"
              controls
              playsinline
              preload="metadata"
              @play="handlePickerVideoPlay(index)"
            ></video>
            <div v-if="selectedPickerVideoIndex === index" class="picker-card__playing-mask">正在播放</div>
          </div>
          <div class="picker-card__body">
            <div class="picker-card__name" :title="item.fileName || ''">{{ item.fileName || '未命名视频' }}</div>
            <div class="picker-card__tag" :title="item.fileTag || ''">{{ item.fileTag || '无标签' }}</div>
            <div class="picker-card__actions">
              <el-button type="primary" @click="chooseVideoMaterial(item)">选择</el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="videoPickerVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MinioButtonUploader from '../../../components/MinioButtonUploader.vue'
import { fetchVideoMaterials } from '../../../api/index'
import { fetchExpVideos, saveExpVideo, saveExpVideos } from '../../../api/exp'

const props = defineProps({
  expId: {
    type: [String, Number],
    required: true
  }
})

const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
const resolveFileUrl = (value) => {
  if (!value) return ''
  const raw = String(value).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
}

const videoSaving = ref(false)
const videoList = ref([])
const videoRefs = ref([])
const selectedVideoIndex = ref(-1)
const dragVideoIndex = ref(-1)
const videoPickerVisible = ref(false)
const videoPickerLoading = ref(false)
const videoMaterialTable = ref([])
const videoPickerQuery = reactive({ keyword: '' })
const pickerVideoRefs = ref([])
const selectedPickerVideoIndex = ref(-1)

const loadVideos = async () => {
  if (!props.expId) return
  try {
    const res = await fetchExpVideos(props.expId)
    if (res.data.code === 200) {
      videoList.value = Array.isArray(res.data.data) ? res.data.data : []
    }
  } catch (error) {
    videoList.value = []
  }
}

const openVideoPicker = () => {
  videoPickerVisible.value = true
  loadVideoMaterials()
}

const loadVideoMaterials = async () => {
  videoPickerLoading.value = true
  try {
    const res = await fetchVideoMaterials({ keyword: videoPickerQuery.keyword, pageNum: 1, pageSize: 20 })
    if (res.data.code === 200) {
      const rows = res.data.data?.records || []
      videoMaterialTable.value = rows
      pickerVideoRefs.value = []
      selectedPickerVideoIndex.value = -1
    }
  } catch (error) {
    videoMaterialTable.value = []
  } finally {
    videoPickerLoading.value = false
  }
}

const setPickerVideoRef = (el, index) => {
  if (!el) return
  pickerVideoRefs.value[index] = el
}

const handlePickerVideoPlay = (index) => {
  selectedPickerVideoIndex.value = index
  pickerVideoRefs.value.forEach((video, i) => {
    if (i !== index && video && !video.paused) video.pause()
  })
}

const buildVideoPayload = () => videoList.value.map((item, index) => ({
  seqId: item.seqId || '',
  fileId: item.fileId || '',
  fileName: item.fileName || '',
  videoUrl: item.videoUrl || '',
  previewUrl: item.previewUrl || item.videoUrl || '',
  sortOrder: index + 1
}))

const persistVideos = async () => {
  if (!props.expId) return false
  if (videoSaving.value) return false
  videoSaving.value = true
  try {
    await saveExpVideos(props.expId, buildVideoPayload())
    return true
  } finally {
    videoSaving.value = false
  }
}

const persistSingleVideo = async (video) => {
  if (!props.expId) return false
  if (videoSaving.value) return false
  videoSaving.value = true
  try {
    const res = await saveExpVideo(props.expId, video)
    const data = res?.data?.data || res?.data || {}
    const seqId = data.seqId || data.id || data.value || data.result || video.seqId || ''
    if (seqId) video.seqId = seqId
    return true
  } finally {
    videoSaving.value = false
  }
}

const handleVideoUploaded = async (payload) => {
  if (videoSaving.value) {
    ElMessage.warning('视频正在保存，请稍后再试')
    return
  }
  try {
    const fileUrl = payload?.fileUrl || ''
    const previewUrl = payload?.previewUrl || fileUrl
    if (!fileUrl) throw new Error('未返回视频地址')
    const newVideo = {
      seqId: '',
      fileId: '',
      fileName: payload?.fileName || '',
      videoUrl: fileUrl,
      previewUrl: resolveFileUrl(previewUrl),
      sortOrder: videoList.value.length + 1
    }
    await persistSingleVideo(newVideo)
    videoList.value.push(newVideo)
    selectedVideoIndex.value = videoList.value.length - 1
    ElMessage.success('视频已保存到数据库')
  } catch (error) {
    ElMessage.error(error?.message || '上传失败')
  }
}

const setVideoRef = (el, index) => {
  if (!el) return
  videoRefs.value[index] = el
}

const handleVideoPlay = (index) => {
  selectedVideoIndex.value = index
  videoRefs.value.forEach((video, i) => { if (i !== index && video && !video.paused) video.pause() })
}

const reorderVideoList = () => {
  videoList.value = videoList.value.map((item, index) => ({ ...item, sortOrder: index + 1 }))
}

const moveVideoUp = async (index) => {
  if (videoSaving.value || index <= 0) return
  const list = [...videoList.value]
  ;[list[index - 1], list[index]] = [list[index], list[index - 1]]
  videoList.value = list
  reorderVideoList()
  await persistVideos()
}

const moveVideoDown = async (index) => {
  if (videoSaving.value || index >= videoList.value.length - 1) return
  const list = [...videoList.value]
  ;[list[index + 1], list[index]] = [list[index], list[index + 1]]
  videoList.value = list
  reorderVideoList()
  await persistVideos()
}

const onGridDragStart = (index) => {
  dragVideoIndex.value = index
}

const onGridDrop = async (dropIndex) => {
  if (videoSaving.value) return
  const fromIndex = dragVideoIndex.value
  dragVideoIndex.value = -1
  if (fromIndex < 0) return
  const list = [...videoList.value]
  const refs = [...videoRefs.value]
  const [moved] = list.splice(fromIndex, 1)
  const [movedRef] = refs.splice(fromIndex, 1)
  const insertIndex = dropIndex < 0 ? list.length : Math.min(dropIndex, list.length)
  list.splice(insertIndex, 0, moved)
  refs.splice(insertIndex, 0, movedRef)
  videoList.value = list
  videoRefs.value = refs
  reorderVideoList()
  await persistVideos()
}

const removeVideo = async (index) => {
  if (videoSaving.value) return
  const item = videoList.value[index]
  if (!item) return
  try {
    await ElMessageBox.confirm(`确定删除视频【${item.fileName || `视频 ${index + 1}`}】吗？`, '提示', { type: 'warning' })
    videoList.value.splice(index, 1)
    videoRefs.value.splice(index, 1)
    reorderVideoList()
    await persistVideos()
    ElMessage.success('视频已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.message || error?.message || '删除失败')
    }
  }
}

const chooseVideoMaterial = async (item) => {
  if (!item) return
  const newVideo = { seqId: '', fileId: item.fileId || '', fileName: item.fileName || '', videoUrl: item.fileUrl || '', previewUrl: resolveFileUrl(item.previewUrl || item.fileUrl || ''), sortOrder: videoList.value.length + 1 }
  await persistSingleVideo(newVideo)
  videoList.value.push(newVideo)
  videoPickerVisible.value = false
}

onMounted(loadVideos)
</script>

<style scoped src="../css/ExpStandardCreateView.css"></style>