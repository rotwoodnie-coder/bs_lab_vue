<template>
  <div class="step-block">
    <div class="video-step__toolbar">
      <div class="video-step__title">教学与安全</div>
      <div class="video-step__actions">
        <el-button type="primary" :loading="saving" @click="saveStep">保存</el-button>
      </div>
    </div>

    <el-form ref="securityFormRef" :model="securityForm" label-width="120px" class="user-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学科" prop="subjectId">
            <el-select v-model="securityForm.subjectId" placeholder="请选择学科" clearable filterable style="width: 100%" @change="handleSubjectChange">
              <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年级" prop="gradeId">
            <el-select v-model="securityForm.gradeId" placeholder="请选择年级" clearable filterable style="width: 100%" @change="queueSave">
              <el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学期" prop="semesterId">
            <el-select v-model="securityForm.semesterId" placeholder="请选择学期" clearable filterable style="width: 100%" @change="queueSave">
              <el-option v-for="item in semesterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="教材" prop="coursebookId">
            <el-select v-model="securityForm.coursebookId" placeholder="请选择教材" clearable filterable style="width: 100%" @change="handleCoursebookChange">
              <el-option v-for="item in coursebookOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单元" prop="unitId">
            <el-select v-model="securityForm.unitId" placeholder="请选择单元" clearable filterable style="width: 100%" @change="handleUnitChange">
              <el-option v-for="item in unitOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="章" prop="chapterId">
            <el-select v-model="securityForm.chapterId" placeholder="请选择章" clearable filterable style="width: 100%" @change="handleChapterChange">
              <el-option v-for="item in chapterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="节" prop="sectionId">
            <el-input
              v-model="securityForm.sectionId"
              placeholder="请输入节"
              clearable
              inputmode="text"
              @blur="queueSave"
               :maxlength="50"
            />
            <!--
            <el-select v-model="securityForm.sectionId" placeholder="请选择节" clearable filterable style="width: 100%" @change="queueSave">
              <el-option v-for="item in sectionOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            -->
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="课时" prop="classHour">
            <el-input
              v-model="securityForm.classHour"
              placeholder="请输入课时"
              clearable
              inputmode="decimal"
              @blur="handleClassHourBlur"
               :maxlength="5"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="安全注意事项" prop="expCaution">
            <SecurityTextEditor
              v-model="securityForm.expCaution"
              field-type="caution"
              :quick-options="materialSecurityOptions"
              :max-length="EXP_SAFETY_TEXT_MAX"
              @save="queueSave"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="危险提示" prop="expDanger">
            <SecurityTextEditor
              v-model="securityForm.expDanger"
              field-type="danger"
              :quick-options="materialSecurityOptions"
              :max-length="EXP_SAFETY_TEXT_MAX"
              @save="queueSave"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCoursebooks, fetchCoursebookContents } from '../../../api/index'
import { fetchExpStandard, updateExpStandard } from '../../../api/index'
import { fetchDataDictItems } from '../../../api/index'
import SecurityTextEditor from '../components/SecurityTextEditor.vue'

const EXP_SAFETY_TEXT_MAX = 500

const props = defineProps({
  expId: {
    type: [String, Number],
    required: true
  }
})

const securityFormRef = ref()
const saving = ref(false)
const subjectOptions = ref([])
const gradeOptions = ref([])
const semesterOptions = ref([])
const coursebookOptions = ref([])
const unitOptions = ref([])
const chapterOptions = ref([])
const sectionOptions = ref([])
const materialSecurityOptions = ref([])

const securityForm = reactive({
  subjectId: '',
  gradeId: '',
  semesterId: '',
  classHour: '',
  coursebookId: '',
  unitId: '',
  chapterId: '',
  sectionId: '',
  expCaution: '',
  expDanger: ''
})

const normalizeOptions = (rows, valueKey, labelKey) => (rows || [])
  .filter(item => item?.[valueKey] != null && item?.[labelKey] != null)
  .map(item => ({ value: String(item[valueKey]), label: item[labelKey] }))

/** 旧版分号分隔标签转为多行，便于在长文本框中编辑 */
const normalizeLegacySecurityText = (value) => {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (raw.includes(';') && !raw.includes('\n')) {
    return raw.split(/[,;]/).map(item => item.trim()).filter(Boolean).join('\n')
  }
  return raw
}

const loadDicts = async () => {
  const [subjectRes, gradeRes, semesterRes, securityRes] = await Promise.all([
    fetchDataDictItems('data_school_subject'),
    fetchDataDictItems('data_school_grade'),
    fetchDataDictItems('data_school_semester'),
    fetchDataDictItems('data_material_security')
  ])
  if (subjectRes.data.code === 200) subjectOptions.value = normalizeOptions(subjectRes.data.data || [], 'subject_id', 'subject_name')
  if (gradeRes.data.code === 200) gradeOptions.value = normalizeOptions(gradeRes.data.data || [], 'grade_id', 'grade_name')
  if (semesterRes.data.code === 200) semesterOptions.value = normalizeOptions(semesterRes.data.data || [], 'semester_id', 'semester_name')
  if (securityRes.data.code === 200) {
    materialSecurityOptions.value = (Array.isArray(securityRes.data.data) ? securityRes.data.data : [])
      .filter(item => String(item.status || '').toLowerCase() === 'y')
      .map(item => ({
        id: item.security_id || item.securityId || item.id,
        label: item.security_name || item.securityName || item.name || item.id
      }))
      .filter(item => item.id && item.label)
  }
}

const resetSecurityCatalogSelections = () => {
  securityForm.coursebookId = ''
  securityForm.unitId = ''
  securityForm.chapterId = ''
  securityForm.sectionId = ''
  unitOptions.value = []
  chapterOptions.value = []
  sectionOptions.value = []
}

const loadCoursebooksBySubject = async (subjectId) => {
  coursebookOptions.value = []
  const params = { pageNum: 1, pageSize: 500, paged: false }
  if (subjectId) params.subjectId = subjectId
  const res = await fetchCoursebooks(params)
  if (res.data.code === 200) {
    const rows = Array.isArray(res.data.data) ? res.data.data : []
    coursebookOptions.value = rows
      .filter(item => String(item.status || '').toLowerCase() === 'y')
      .map(item => ({
        value: String(item.coursebookId || item.coursebook_id || item.id || ''),
        label: item.coursebookName || item.coursebook_name || item.name || item.id || ''
      }))
      .filter(item => item.value)
  }
}

const loadCoursebookChildren = async (coursebookId, targetType, parentId = '') => {
  if (!coursebookId) return
  const res = await fetchCoursebookContents({ coursebookId, parentId: parentId || undefined, paged: false })
  if (res.data.code !== 200) return
  const rows = Array.isArray(res.data.data) ? res.data.data : []
  const options = rows
    .filter(item => String(item.status || '').toLowerCase() === 'y')
    .filter(item => {
      const type = String(item.contentType || item.content_type || '').toLowerCase()
      if (targetType === 'unit') return type === 'unit' && (!parentId || String(item.parentId || item.parent_id || '') === '')
      if (targetType === 'chapter') return type === 'chapter' && String(item.parentId || item.parent_id || '') === String(parentId || '')
      if (targetType === 'section') return type === 'section' && String(item.parentId || item.parent_id || '') === String(parentId || '')
      return true
    })
    .map(item => ({
      value: String(item.contentId || item.content_id || item.id || ''),
      label: item.contentName || item.content_name || item.name || item.id || ''
    }))
    .filter(item => item.value)
  if (targetType === 'unit') unitOptions.value = options
  if (targetType === 'chapter') chapterOptions.value = options
  if (targetType === 'section') sectionOptions.value = options
}

const loadStep = async () => {
  if (!props.expId) return
  await loadDicts()
  const res = await fetchExpStandard(String(props.expId).trim())
  if (res.data.code === 200 && res.data.data) {
    const draft = res.data.data
    securityForm.subjectId = draft.subjectId || ''
    securityForm.gradeId = draft.gradeId || ''
    securityForm.semesterId = draft.semesterId || draft.semester_id || ''
    securityForm.classHour = draft.classHour ?? ''
    securityForm.expCaution = normalizeLegacySecurityText(draft.expCaution)
    securityForm.expDanger = normalizeLegacySecurityText(draft.expDanger)
    if (securityForm.subjectId) {
      await loadCoursebooksBySubject(securityForm.subjectId)
    }
    securityForm.coursebookId = draft.coursebookId || ''
    securityForm.unitId = draft.unitId || ''
    securityForm.chapterId = draft.chapterId || ''
    securityForm.sectionId = draft.sectionId || ''
    if (securityForm.coursebookId) {
      await loadCoursebookChildren(securityForm.coursebookId, 'unit')
      if (securityForm.unitId) await loadCoursebookChildren(securityForm.coursebookId, 'chapter', securityForm.unitId)
      if (securityForm.chapterId) await loadCoursebookChildren(securityForm.coursebookId, 'section', securityForm.chapterId)
    }
  }
}

const normalizeClassHour = (value) => {
  const raw = String(value ?? '').trim()
  if (!raw) return ''
  if (!/^\d+(?:\.\d{1,2})?$/.test(raw)) return null
  return raw
}

const handleClassHourBlur = async () => {
  const normalized = normalizeClassHour(securityForm.classHour)
  if (normalized === null) {
    ElMessage.warning('课时必须是最多2位小数的数字')
    return
  }
  securityForm.classHour = normalized
  await queueSave()
}

const saveStep = async () => {
  if (!props.expId) return false
  saving.value = true
  try {
    await updateExpStandard(String(props.expId).trim(), {
      subjectId: securityForm.subjectId || '',
      gradeId: securityForm.gradeId || '',
      semesterId: securityForm.semesterId || '',
      classHour: securityForm.classHour || '',
      coursebookId: securityForm.coursebookId || '',
      unitId: securityForm.unitId || '',
      chapterId: securityForm.chapterId || '',
      sectionId: securityForm.sectionId || '',
      expCaution: securityForm.expCaution || '',
      expDanger: securityForm.expDanger || ''
    })
    ElMessage.success('教学与安全已保存')
    return true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '教学与安全保存失败')
    return false
  } finally {
    saving.value = false
  }
}

const queueSave = async () => {
  await saveStep()
}

const handleSubjectChange = async (value) => {
  securityForm.subjectId = value || ''
  resetSecurityCatalogSelections()
  await loadCoursebooksBySubject(securityForm.subjectId)
  await queueSave()
}

const handleCoursebookChange = async (value) => {
  securityForm.coursebookId = value || ''
  securityForm.unitId = ''
  securityForm.chapterId = ''
  securityForm.sectionId = ''
  unitOptions.value = []
  chapterOptions.value = []
  sectionOptions.value = []
  if (securityForm.coursebookId) {
    await loadCoursebookChildren(securityForm.coursebookId, 'unit')
  }
  await queueSave()
}

const handleUnitChange = async (value) => {
  securityForm.unitId = value || ''
  securityForm.chapterId = ''
  securityForm.sectionId = ''
  chapterOptions.value = []
  sectionOptions.value = []
  if (securityForm.coursebookId && securityForm.unitId) {
    await loadCoursebookChildren(securityForm.coursebookId, 'chapter', securityForm.unitId)
  }
  await queueSave()
}

const handleChapterChange = async (value) => {
  securityForm.chapterId = value || ''
  securityForm.sectionId = ''
  sectionOptions.value = []
  if (securityForm.coursebookId && securityForm.chapterId) {
    await loadCoursebookChildren(securityForm.coursebookId, 'section', securityForm.chapterId)
  }
  await queueSave()
}

onMounted(loadStep)
</script>

<style scoped src="../css/ExpStandardCreateView.css"></style>