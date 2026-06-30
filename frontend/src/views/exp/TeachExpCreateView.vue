<template>
  <div class="page-card">
    <el-card class="user-card">
      <template #header>
        <div class="user-page-header create-header">
          <div class="user-page-title-wrap">
            <span class="user-page-title">{{ pageTitle }}</span>
          </div>
          <div class="create-header-actions">
            <el-button @click="goBack">返回列表</el-button>
          </div>
        </div>
      </template>

      <div class="step-wrap">
        <el-steps :active="activeStep" align-center finish-status="success">
          <el-step @click="goStep(0)">
            <template #title>
              <span class="step-title-wrap">
                <span>基础信息</span>
                <span class="step-title-percent">{{ stepCompletion[0] }}%</span>
              </span>
            </template>
          </el-step>
          <el-step @click="goStep(1)">
            <template #title>
              <span class="step-title-wrap">
                <span>实验视频</span>
                <span class="step-title-percent">{{ stepCompletion[1] }}%</span>
              </span>
            </template>
          </el-step>
          <el-step @click="goStep(2)">
            <template #title>
              <span class="step-title-wrap">
                <span>实验材料</span>
                <span class="step-title-percent">{{ stepCompletion[2] }}%</span>
              </span>
            </template>
          </el-step>
          <el-step @click="goStep(3)">
            <template #title>
              <span class="step-title-wrap">
                <span>实验步骤</span>
                <span class="step-title-percent">{{ stepCompletion[3] }}%</span>
              </span>
            </template>
          </el-step>
          <el-step @click="goStep(4)">
            <template #title>
              <span class="step-title-wrap">
                <span>实验结果</span>
                <span class="step-title-percent">{{ stepCompletion[4] }}%</span>
              </span>
            </template>
          </el-step>
          <el-step @click="goStep(5)">
            <template #title>
              <span class="step-title-wrap">
                <span>教学与安全</span>
                <span class="step-title-percent">{{ stepCompletion[5] }}%</span>
              </span>
            </template>
          </el-step>
          <el-step @click="goStep(6)">
            <template #title>
              <span class="step-title-wrap">
                <span>参考与故事</span>
                <span class="step-title-percent">{{ stepCompletion[6] }}%</span>
              </span>
            </template>
          </el-step>
        </el-steps>
      </div>

      <div class="step-content">
        <template v-if="activeStep === 0">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="user-form">
            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item label="标准实验" prop="standardExpId">
                  <el-input v-model="form.linkExpName" placeholder="请选择标准实验" readonly>
                    <template #append>
                      <el-button @click="openStandardDialog">选择标准实验</el-button>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12"><el-form-item label="实验名称" prop="expName"><el-input v-model="form.expName" placeholder="请输入实验名称" :maxlength="30" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="必做/选做" prop="chooseType"><el-select v-model="form.chooseType" placeholder="请选择" clearable style="width: 100%"><el-option label="必做" value="must" /><el-option label="选做" value="choose" /></el-select></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="学科" prop="subjectId"><el-select v-model="form.subjectId" placeholder="请选择学科" clearable filterable style="width: 100%"><el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="学段" prop="schoolLevelId"><el-select v-model="form.schoolLevelId" placeholder="请选择学段" clearable filterable style="width: 100%"><el-option v-for="item in schoolLevelOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="适用年级" prop="gradeIds"><el-checkbox-group v-model="form.gradeIds" class="grade-checkbox-group"><el-checkbox v-for="item in gradeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-checkbox></el-checkbox-group></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="难度" prop="difficultyId"><el-select v-model="form.difficultyId" placeholder="请选择难度" clearable filterable style="width: 100%"><el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
              
              <el-col :span="24"><el-form-item label="实验原理" prop="expPrinciple"><el-input v-model="form.expPrinciple" type="textarea" :rows="4" placeholder="请输入实验原理" /></el-form-item></el-col>
            </el-row>
          </el-form>
        </template>
        <template v-else-if="activeStep === 1">
          <Step2Videos :exp-id="expId" />
        </template>
        <template v-else-if="activeStep === 2">
          <Step3Materials :exp-id="expId" />
        </template>
        <div v-show="activeStep === 3">
          <Step4ExperimentSteps v-if="visitedRichSteps.has(3)" :key="`${expId || 'new'}-step4`" :exp-id="expId" ref="step4Ref" />
        </div>
        <div v-show="activeStep === 4">
          <Step5ExperimentResults v-if="visitedRichSteps.has(4)" :key="`${expId || 'new'}-step5`" :exp-id="expId" ref="step5Ref" />
        </div>
        <template v-if="activeStep === 5">
          <Step6TeachingSafety :exp-id="expId" />
        </template>
        <div v-show="activeStep === 6">
          <Step7ReferenceScientist v-if="visitedRichSteps.has(6)" :key="`${expId || 'new'}-step7`" :exp-id="expId" ref="step7Ref" />
        </div>
      </div>

      <div class="step-actions">
        <el-button :disabled="activeStep === 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < stepTitles.length - 1" type="primary" :loading="stepSaving" @click="nextStep">下一步</el-button>
        <slot v-else>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存草稿</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitToAudit">提交审核</el-button>
        </slot>
      </div>
    </el-card>

    <el-dialog v-model="logDialogVisible" title="实验日志" width="900px">
      <el-table :data="logTableData" v-loading="logLoading" border stripe max-height="520">
        <el-table-column prop="logTime" label="日志时间" width="180">
          <template #default="scope">{{ formatLogTime(scope.row.logTime) }}</template>
        </el-table-column>
        <el-table-column prop="logTypeName" label="日志类型" width="140" />
        <el-table-column prop="logUserName" label="操作人" width="140" />
        <el-table-column prop="logContent" label="日志内容" min-width="260" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="logDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="standardDialogVisible" title="选择标准实验" width="980px" class="user-dialog">
      <div class="standard-toolbar standard-dialog-toolbar" :style="{ flexWrap: 'nowrap', whiteSpace: 'nowrap' }">
        <div class="standard-toolbar-inline standard-dialog-search-inline" :style="{ flexWrap: 'nowrap', whiteSpace: 'nowrap' }">
          <el-input v-model="standardQuery.keyword" placeholder="搜索实验名称/事项" clearable class="standard-search" @keyup.enter="loadStandardList" style="width:180px;"/>
          <el-select v-model="standardQuery.subjectId" placeholder="学科" clearable filterable class="standard-status-select" @keyup.enter="loadStandardList"  style="width:100px;">
            <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button :loading="standardLoading" @click="loadStandardList">查询</el-button>
          <el-button @click="resetStandardQuery">重置</el-button>
        </div>
      </div>
      <el-table :data="standardTableData" border stripe v-loading="standardLoading" class="user-table">
        <el-table-column prop="expName" label="实验名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="chooseType" label="必做/选做" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ chooseTypeLabel(scope.row.chooseType) }}</template>
        </el-table-column>
        <el-table-column prop="subjectId" label="学科" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ getSubjectLabel(scope.row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="schoolLevelId" label="学段" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.schoolLevelName || getSchoolLevelLabel(scope.row.schoolLevelId) }}</template>
        </el-table-column>
        <el-table-column prop="gradeId" label="年级" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.gradeNames }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="light">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleStandardSelect(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-footer">
        <div class="pagination-left">
          <div class="pagination-summary">当前每页 {{ standardQuery.pageSize }} 条，共 {{ standardPagination.total }} 条数据</div>
        </div>
        <div class="pagination-wrap pagination-bar">
          <el-pagination
            background
            layout="共 {total} 条, sizes, prev, pager, next, jumper"
            :total="standardPagination.total"
            :current-page="standardQuery.pageNum"
            :page-size="standardQuery.pageSize"
            :page-sizes="[10, 20, 30, 40, 50]"
            @current-change="handleStandardPageChange"
            @size-change="handleStandardSizeChange"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="standardDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchDataDictItems } from '../../api/index'
import { createExpStandard, createExpTeachesFromStandard, fetchExpStandard, fetchExpStandards, updateExpStandard, fetchExpVideos, fetchExpMaterials, fetchExpSteps, fetchExpResults, fetchExpReferences, fetchExpScientists, fetchExpLogs } from '../../api/exp'
import Step2Videos from './steps/TeachExpStep2Videos.vue'
import Step3Materials from './steps/TeachExpStep3Materials.vue'
import Step4ExperimentSteps from './steps/TeachExpStep4ExperimentSteps.vue'
import Step5ExperimentResults from './steps/TeachExpStep5ExperimentResults.vue'
import Step6TeachingSafety from './steps/TeachExpStep6TeachingSafety.vue'
import Step7ReferenceScientist from './steps/TeachExpStep7ReferenceScientist.vue'
import { useExpWizardRichTextFlush } from './utils/useExpWizardRichTextFlush'

const route = useRoute()
const router = useRouter()
const pageTitle = computed(() => String(route.query.expId || '').trim() ? '编辑教学实验' : '新增教学实验')
const activeStep = ref(0)
const submitLoading = ref(false)
const formRef = ref()
const expId = ref('')
const stepSaving = ref(false)
const step4Ref = ref(null)
const step5Ref = ref(null)
const step7Ref = ref(null)
const { visitedRichSteps, flushBeforeLeaveRichStep } = useExpWizardRichTextFlush({
  activeStep,
  stepSaving,
  step4Ref,
  step5Ref,
  step7Ref,
  expId
})
const logDialogVisible = ref(false)
const logLoading = ref(false)
const logTableData = ref([])
const standardDialogVisible = ref(false)
const standardLoading = ref(false)
const standardTableData = ref([])
const selectedStandardId = ref('')
const selectedStandardText = ref('')
const standardQuery = reactive({ expName: '', subjectId: '', schoolLevelId: '', status: 'y', pageNum: 1, pageSize: 10 })
const standardPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const subjectOptions = ref([])
const schoolLevelOptions = ref([])
const gradeOptions = ref([])
const difficultyOptions = ref([])
const stepTitles = ['基础信息', '实验视频', '实验材料', '实验步骤', '实验结果', '安全与提示', '教学与参考']
const stepStats = reactive({ videos: 0, materials: 0, steps: 0, results: 0, safety: 0, references: 0 })
const stepCompletion = computed(() => [
  completeStep0.value,
  stepStats.videos,
  stepStats.materials,
  stepStats.steps,
  stepStats.results,
  stepStats.safety,
  stepStats.references,
])
const completeStep0 = computed(() => {
  const fields = [
    form.expName,
    form.chooseType,
    form.subjectId,
    form.schoolLevelId,
    Array.isArray(form.gradeIds) ? form.gradeIds.length : 0,
    form.difficultyId,
    form.expPrinciple,
  ]
  const filled = fields.filter((v) => Array.isArray(v) ? v.length > 0 : String(v || '').trim()).length
  return Math.round((filled / fields.length) * 100)
})

const form = reactive({ expName: '', chooseType: '', subjectId: '', schoolLevelId: '', gradeIds: [], difficultyId: '', expPrinciple: '', expCaution: '', expDanger: '', classHour: null, status: 'c', expType: 'teach' })

const rules = computed(() => ({
  expName: [{ required: true, message: '请输入实验名称', trigger: 'blur' }],
  chooseType: [{ required: true, message: '请选择必做/选做', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  schoolLevelId: [{ required: true, message: '请选择学段', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

const normalizeOptions = (rows, valueKey, labelKey) => (rows || []).filter(item => item?.[valueKey] != null && item?.[labelKey] != null).map(item => ({ value: String(item[valueKey]), label: item[labelKey] }))
const chooseTypeLabel = (value) => ({ must: '必做', choose: '选做' }[value] || value || '-')
const statusLabel = (value) => ({ c: '草稿', t: '待审核', y: '通过', n: '不通过' }[value] || value || '-')
const statusTagType = (value) => ({ c: 'info', t: 'warning', y: 'success', n: 'danger' }[value] || 'info')
const getSubjectLabel = (value) => subjectOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getSchoolLevelLabel = (value) => schoolLevelOptions.value.find(item => item.value === String(value))?.label || value || '-'

const pct = (n) => Math.max(0, Math.min(100, Math.round(n || 0)))
const hasText = (v) => String(v || '').trim().length > 0

const formatLogTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const openLogDialog = async () => {
  const id = String(expId.value || route.query.expId || '').trim()
  if (!id) {
    ElMessage.warning('请先保存实验后再查看日志')
    return
  }
  logDialogVisible.value = true
  logLoading.value = true
  try {
    const res = await fetchExpLogs(id)
    const rows = Array.isArray(res.data?.data) ? res.data.data : (res.data?.data?.records || res.data?.data?.list || [])
    logTableData.value = [...rows].sort((a, b) => new Date(b.logTime || 0) - new Date(a.logTime || 0))
  } catch (error) {
    logTableData.value = []
    ElMessage.error(error?.response?.data?.message || error?.message || '加载日志失败')
  } finally {
    logLoading.value = false
  }
}

const loadStepStats = async () => {
  const id = String(expId.value || route.query.expId || '').trim()
  if (!id) {
    stepStats.videos = 0
    stepStats.materials = 0
    stepStats.steps = 0
    stepStats.results = 0
    stepStats.safety = 0
    stepStats.references = 0
    return
  }
  const [videosRes, materialsRes, stepsRes, resultsRes, referencesRes, scientistsRes, standardRes] = await Promise.allSettled([
    fetchExpVideos(id),
    fetchExpMaterials(id),
    fetchExpSteps(id),
    fetchExpResults(id),
    fetchExpReferences(id),
    fetchExpScientists(id),
    fetchExpStandard(id),
  ])
  const toCount = (res) => {
    const data = res?.status === 'fulfilled' ? res.value?.data?.data : null
    if (Array.isArray(data)) return data.length
    if (data && Array.isArray(data.records)) return data.records.length
    if (data && Array.isArray(data.list)) return data.list.length
    return 0
  }
  stepStats.videos = pct(toCount(videosRes) * 100)
  stepStats.materials = pct(toCount(materialsRes) * 100)
  stepStats.steps = pct(toCount(stepsRes) * 100)
  stepStats.results = pct(toCount(resultsRes) * 100)
  stepStats.references = pct(((toCount(referencesRes) + toCount(scientistsRes))) * 50)
  const std = standardRes?.status === 'fulfilled' ? standardRes.value?.data?.data : null
  const safetyCount = [std?.expCaution, std?.expDanger, std?.classHour, std?.subjectId, std?.gradeId, std?.semesterId, std?.coursebookId, std?.unitId, std?.chapterId, std?.sectionId].filter(hasText).length
  stepStats.safety = pct((safetyCount / 10) * 100)
}

const loadDicts = async () => {
  const [subjectRes, levelRes, gradeRes, difficultyRes] = await Promise.all([
    fetchDataDictItems('data_school_subject'),
    fetchDataDictItems('data_school_level'),
    fetchDataDictItems('data_school_grade'),
    fetchDataDictItems('data_difficulty_type')
  ])
  if (subjectRes.data.code === 200) subjectOptions.value = normalizeOptions(subjectRes.data.data || [], 'subject_id', 'subject_name')
  if (levelRes.data.code === 200) schoolLevelOptions.value = normalizeOptions(levelRes.data.data || [], 'level_id', 'level_name')
  if (gradeRes.data.code === 200) gradeOptions.value = normalizeOptions(gradeRes.data.data || [], 'grade_id', 'grade_name')
  if (difficultyRes.data.code === 200) difficultyOptions.value = normalizeOptions(difficultyRes.data.data || [], 'difficulty_id', 'difficulty_name')
}

const prevStep = async () => {
  if (activeStep.value <= 0) return
  if (!(await flushBeforeLeaveRichStep(activeStep.value))) return
  activeStep.value -= 1
  await loadStepStats()
}

const goStep = async (index) => {
  if (index < 0 || index >= stepTitles.length) return
  if (index === activeStep.value) return
  if (index > 0 && !expId.value && activeStep.value === 0) {
    ElMessage.warning('请先保存基础信息')
    return
  }
  if (activeStep.value === 0 && index > 0) {
    stepSaving.value = true
    try {
      await saveStep0()
      ElMessage.success('基础信息已保存')
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
      return
    } finally {
      stepSaving.value = false
    }
  } else if (!(await flushBeforeLeaveRichStep(activeStep.value))) {
    return
  }
  activeStep.value = index
  await loadStepStats()
}

const resolveCreatedId = (data) => {
  if (!data) return ''
  if (typeof data === 'string') return data
  if (typeof data === 'number') return String(data)
  if (typeof data === 'object') {
    return data.expId || data.exp_id || data.id || data.value || data.result || ''
  }
  return ''
}

const saveStep0 = async () => {
  await formRef.value?.validate()
  const payload = { ...form, status: 'c', expType: 'teach', gradeIds: Array.isArray(form.gradeIds) ? form.gradeIds : [] }
  if (expId.value && String(expId.value).trim()) {
    const currentId = String(expId.value).trim()
    await updateExpStandard(currentId, payload)
    return currentId
  }
  const res = await createExpStandard(payload)
  const createdId = resolveCreatedId(res.data.data)
  if (!createdId || typeof createdId !== 'string') {
    throw new Error('未返回有效实验ID')
  }
  expId.value = createdId.trim()
  return expId.value
}

const nextStep = async () => {
  if (activeStep.value === 0) {
    stepSaving.value = true
    try {
      await saveStep0()
      ElMessage.success('基础信息已保存')
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
      return
    } finally {
      stepSaving.value = false
    }
  } else if (!(await flushBeforeLeaveRichStep(activeStep.value))) {
    return
  }
  if (activeStep.value < stepTitles.length - 1) {
    activeStep.value += 1
  }
  await loadStepStats()
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    await flushBeforeLeaveRichStep(activeStep.value)
    await saveStep0()
    await updateExpStandard(expId.value, { status: 'c' })
    ElMessage.success('提交草稿成功')
    router.push('/admin/exp/exp-teach')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '提交草稿失败')
  } finally {
    submitLoading.value = false
  }
}

const handleSubmitToAudit = async () => {
  //check  stepCompletion
    console.log(completeStep0.value); 
    console.log(stepStats.videos); 
    console.log(stepStats.materials); 
    console.log(stepStats.steps); 
    console.log(stepStats.results); 
    console.log(stepStats.safety); 
    console.log(stepStats.references); 
    if(completeStep0.value<100){
      ElMessage.error("请先完善基础数据");
      return 
    }
    if(stepStats.videos<100){
      ElMessage.error("请先完善实验视频，至少一个");
      return 
    }
    if(stepStats.materials<100){
      ElMessage.error("请先完善实验材料，至少一个");
      return 
    }
    if(stepStats.steps<100){
      ElMessage.error("请先完善实验步骤，至少一个");
      return 
    }
    if(stepStats.results<100){
      ElMessage.error("请先完善实验结果，至少一个");
      return 
    }
    if(stepStats.safety<100){
      ElMessage.error("请先完善教学与安全");
      return 
    }

  try {
    await ElMessageBox.confirm('提交审核后内容将进入审核流程，确认继续吗？', '提交审核确认', {
      type: 'warning',
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await flushBeforeLeaveRichStep(activeStep.value)
    await saveStep0()
    await updateExpStandard(expId.value, { status: 't' })
    ElMessage.success('提交审核成功')
    router.push('/admin/exp/exp-teach')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '提交审核失败')
  } finally {
    submitLoading.value = false
  }
}

const normalizePagedResult = (data) => {
  if (!data) return { list: [], total: 0 }
  if (Array.isArray(data)) return { list: data, total: data.length }
  if (Array.isArray(data.records)) return { list: data.records, total: data.total ?? data.records.length }
  if (Array.isArray(data.list)) return { list: data.list, total: data.total ?? data.list.length }
  return { list: [], total: data.total ?? 0 }
}

const standardRowLabel = (row) => String(row?.expName || row?.name || row?.title || row?.expId || row?.id || '')

const formatStandardText = (row) => {
  if (!row) return ''
  const id = String(row.expId || row.id || '').trim()
  const name = standardRowLabel(row)
  return id ? `${name}（${id}）` : name
}

const openStandardDialog = async () => {
  standardDialogVisible.value = true
  await loadStandardList()
}

const resetStandardQuery = () => {
  standardQuery.expName = ''
  standardQuery.subjectId = ''
  standardQuery.schoolLevelId = ''
  standardQuery.status = 'y'
  standardQuery.pageNum = 1
  standardQuery.expType = 'standard'
  standardQuery.pageSize = 10
  standardPagination.pageNum = 1
  standardPagination.pageSize = 10
  selectedStandardId.value = ''
}

const buildStandardParams = () => ({
  ...standardQuery,
  status: 'y',
  expType: 'standard',
  pageNum: standardPagination.pageNum,
  pageSize: standardPagination.pageSize,
})

const loadStandardList = async () => {
  standardLoading.value = true
  try {
    const res = await fetchExpStandards(buildStandardParams())
    const result = normalizePagedResult(res.data?.data)
    standardTableData.value = result.list
    standardPagination.total = result.total || 0
    standardPagination.pageNum = Number(standardQuery.pageNum || 1)
    standardPagination.pageSize = Number(standardQuery.pageSize || 10)
  } catch (error) {
    standardTableData.value = []
    standardPagination.total = 0
    ElMessage.error(error?.response?.data?.message || error?.message || '加载标准实验失败')
  } finally {
    standardLoading.value = false
  }
}

const searchStandardList = async () => {
  standardPagination.pageNum = 1
  standardQuery.pageNum = 1
  await loadStandardList()
}

const handleStandardPageChange = async (page) => {
  standardPagination.pageNum = page
  standardQuery.pageNum = page
  await loadStandardList()
}

const handleStandardSizeChange = async (size) => {
  standardPagination.pageSize = size
  standardQuery.pageSize = size
  standardPagination.pageNum = 1
  standardQuery.pageNum = 1
  await loadStandardList()
}

const standardDialogQueryStyle = {
  flexWrap: 'nowrap',
  whiteSpace: 'nowrap',
}

const handleStandardSelect = async (row) => {
  selectedStandardId.value = String(row?.expId || row?.id || '')
  selectedStandardText.value = formatStandardText(row)
  const fromExpId = String(row?.expId || row?.id || '').trim()
  const toExpId = String(expId.value || '').trim()
  if (!fromExpId) {
    ElMessage.warning('未找到标准实验ID')
    return
  }
  try {
    await ElMessageBox.confirm('确认选择此标准实验吗？选择之后会覆盖当前实验输入内容。', '选择标准实验确认', {
      type: 'warning',
      confirmButtonText: '确认选择',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    standardLoading.value = true
    const res = await createExpTeachesFromStandard({ fromExpId, toExpId })
    const createdExpId = resolveCreatedId(res?.data?.data)
    if (createdExpId) {
      expId.value = String(createdExpId).trim()
      await loadByExpId(expId.value)
    }
    ElMessage.success('已关联标准实验')
    standardDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '关联标准实验失败')
  } finally {
    standardLoading.value = false
  }
}

const confirmStandardSelect = () => {
  const selected = standardTableData.value.find((item) => String(item?.expId || item?.id || '') === String(selectedStandardId.value))
  if (selected) {
    form.expName = selected.expName || form.expName
    if (selected.subjectId != null) form.subjectId = String(selected.subjectId)
    if (selected.schoolLevelId != null) form.schoolLevelId = String(selected.schoolLevelId)
    if (selected.gradeIds != null && !Array.isArray(form.gradeIds)) form.gradeIds = []
    if (selected.difficultyId != null) form.difficultyId = String(selected.difficultyId)
    if (selected.expPrinciple != null) form.expPrinciple = selected.expPrinciple
    selectedStandardText.value = formatStandardText(selected)
  }
  standardDialogVisible.value = false
}

const goBack = () => {
  router.push('/admin/exp/exp-teach')
}

const loadDraft = async () => {
  const draftId = String(route.query.expId || '').trim()
  if (!draftId) return
  const res = await fetchExpStandard(draftId)
  if (res.data.code === 200 && res.data.data) {
    const draft = res.data.data
    expId.value = draft.expId || draftId
    form.expName = draft.expName || ''
    form.chooseType = draft.chooseType || ''
    form.subjectId = draft.subjectId || ''
    form.schoolLevelId = draft.schoolLevelId || ''
    form.gradeIds = Array.isArray(draft.gradeIds)
      ? draft.gradeIds.map(item => String(item))
      : String(draft.gradeIds || '')
        .split(/[,;\s]+/)
        .map(item => item.trim())
        .filter(Boolean)
    form.difficultyId = draft.difficultyId || ''
    form.expPrinciple = draft.expPrinciple || ''
    form.expCaution = draft.expCaution || ''
    form.expDanger = draft.expDanger || ''
    form.classHour = draft.classHour ?? null
    form.status = draft.status || 'c'
    form.gradeId = draft.gradeId || ''
    form.coursebookId = draft.coursebookId || ''
    form.unitId = draft.unitId || ''
    form.chapterId = draft.chapterId || ''
    form.sectionId = draft.sectionId || ''
  }
  await loadStepStats()
}


const loadByExpId = async (id) => {
  const targetId = String(id || '').trim()
  if (!targetId) return
  expId.value = targetId
  await loadDicts()
  const res = await fetchExpStandard(targetId)
  if (res.data.code === 200 && res.data.data) {
    const draft = res.data.data
    form.expName = draft.expName || ''
    form.chooseType = draft.chooseType || ''
    form.subjectId = draft.subjectId || ''
    form.schoolLevelId = draft.schoolLevelId || ''
    form.gradeIds = Array.isArray(draft.gradeIds)
      ? draft.gradeIds.map(item => String(item))
      : String(draft.gradeIds || '')
        .split(/[,;\s]+/)
        .map(item => item.trim())
        .filter(Boolean)
    form.difficultyId = draft.difficultyId || ''
    form.expPrinciple = draft.expPrinciple || ''
    form.expCaution = draft.expCaution || ''
    form.expDanger = draft.expDanger || ''
    form.classHour = draft.classHour ?? null
    form.status = draft.status || 'c'
    form.gradeId = draft.gradeId || ''
    form.coursebookId = draft.coursebookId || ''
    form.unitId = draft.unitId || ''
    form.chapterId = draft.chapterId || ''
    form.sectionId = draft.sectionId || ''
    form.linkExpId = draft.linkExpId || ''
    form.linkExpName = draft.linkExpName || ''
  }
  await loadStepStats()
}



watch(
  () => route.query.expId,
  async (id) => {
    if (id) {
      await loadByExpId(id)
    }
  },
  { immediate: true }
)


onMounted(async () => {
  if (!route.query.expId) {
    await loadDicts()
    await loadDraft()
  }
})

onBeforeUnmount(() => {})
</script>

<style scoped src="./css/ExpStandardCreateView.css"></style>
