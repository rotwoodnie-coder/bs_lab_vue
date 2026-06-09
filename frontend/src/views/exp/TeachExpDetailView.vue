<template>
  <div class="page-card">
    <el-card class="user-card detail-card">
      <template #header>
        <div class="user-page-header create-header">
          <div class="user-page-title-wrap">
            <div class="title-row">
              <span class="user-page-title">{{ pageTitle }}</span>
              <el-tag :type="statusTagType(form.status)" effect="light" class="status-tag">
                {{ statusLabel(form.status) }}
              </el-tag>
            </div>
            <div class="page-subtitle">只读查看实验全部步骤内容</div>
          </div>
          <div class="create-header-actions">
            <el-button @click="goBack">返回列表</el-button>
          </div>
        </div>
      </template>

      <div class="step-wrap">
        <el-steps :active="activeStep" align-center finish-status="success">
          <el-step @click="goStep(0)"><template #title><span class="step-title-link">基础信息</span></template></el-step>
          <el-step @click="goStep(1)"><template #title><span class="step-title-link">实验视频</span></template></el-step>
          <el-step @click="goStep(2)"><template #title><span class="step-title-link">实验材料</span></template></el-step>
          <el-step @click="goStep(3)"><template #title><span class="step-title-link">实验步骤</span></template></el-step>
          <el-step @click="goStep(4)"><template #title><span class="step-title-link">实验结果</span></template></el-step>
          <el-step @click="goStep(5)"><template #title><span class="step-title-link">教学与安全</span></template></el-step>
          <el-step @click="goStep(6)"><template #title><span class="step-title-link">参考与故事</span></template></el-step>
        </el-steps>
      </div>

      <div v-loading="loading" class="step-content">
        <template v-if="activeStep === 0">
          <el-descriptions border :column="2">
            <el-descriptions-item label="实验名称">{{ form.expName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="必做/选做">{{ chooseTypeLabel(form.chooseType) }}</el-descriptions-item>
            <el-descriptions-item label="学科">{{ form.subjectName || getSubjectLabel(form.subjectId) }}</el-descriptions-item>
            <el-descriptions-item label="学段">{{ form.schoolLevelName || getSchoolLevelLabel(form.schoolLevelId) }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ form.gradeNames }}</el-descriptions-item>
            <el-descriptions-item label="难度">{{ getDifficultyLabel(form.difficultyId) }}</el-descriptions-item>
            
            <el-descriptions-item label="实验原理" :span="2">
              <div class="rich-view rich-view--panel" v-html="normalizeHtml(form.expPrinciple)" />
            </el-descriptions-item>
            
          </el-descriptions>
        </template>

        <template v-else-if="activeStep === 1">
          <div class="section-group">
            <div class="section-group__title">实验视频</div>
            <div v-if="videoList.length" class="video-grid read-grid">
              <div v-for="(item, index) in videoList" :key="item.seqId || item.fileId || index" class="video-card read-card">
                <div class="video-card__preview read-preview">
                  <video :src="resolveFileUrl(item.videoUrl || '')" controls playsinline preload="metadata"></video>
                </div>
                <div class="video-card__body">
                  <div class="video-card__title">{{ item.fileName || '视频素材' }}</div>
                  <div class="video-card__meta">排序 {{ item.sortOrder ?? index + 1 }}</div>
                </div>
              </div>
            </div>
            <div v-else class="step-placeholder">暂无视频</div>
          </div>
        </template>

        <template v-else-if="activeStep === 2">
          <div class="section-group">
            <div class="section-group__title">实验材料</div>
            <div v-if="expMaterialList.length" class="material-grid read-grid read-material-grid">
              <div v-for="(item, index) in expMaterialList" :key="item.expMaterialId || item.materialId || index" class="material-card read-card read-material-card">
                <div class="material-card__preview read-preview read-material-preview">
                  <el-image v-if="resolveFileUrl(item.mainPicUrl)" :src="resolveFileUrl(item.mainPicUrl)" fit="contain" :preview-src-list="[resolveFileUrl(item.mainPicUrl)]" preview-teleported />
                  <div v-else class="material-card__empty">无主图</div>
                </div>
                <div class="material-card__body">
                  <div class="material-card__name">{{ item.materialName || '未命名材料' }}</div>
                  <div class="material-card__tag">用量：{{ item.materialNum || '-' }}</div>
                </div>
              </div>
            </div>
            <div v-else class="step-placeholder">暂无材料</div>
          </div>
        </template>

        <template v-else-if="activeStep === 3">
          <div class="section-group">
            <div class="section-group__title">实验步骤</div>
            <div v-if="stepList.length" class="step-list">
              <div v-for="(item, index) in stepList" :key="item.stepId || item.uid" class="step-card read-card">
                <div class="step-card__header">
                  <div class="step-card__title">步骤 {{ index + 1 }}：{{ item.stepName || '未命名步骤' }}</div>
                </div>
                <div class="rich-view rich-view--panel" v-html="normalizeHtml(item.stepComments)" />
              </div>
            </div>
            <div v-else class="step-placeholder">暂无步骤</div>
          </div>
        </template>

        <template v-else-if="activeStep === 4">
          <div class="section-group">
            <div class="section-group__title">实验结果</div>
            <div v-if="resultList.length" class="step-list">
              <div v-for="(item, index) in resultList" :key="item.resultId || item.uid" class="step-card read-card">
                <div class="step-card__header">
                  <div class="step-card__title">结果 {{ index + 1 }}：{{ item.resultName || '未命名结果' }}</div>
                </div>
                <div class="rich-view rich-view--panel" v-html="normalizeHtml(item.resultComments)" />
              </div>
            </div>
            <div v-else class="step-placeholder">暂无结果</div>
          </div>
        </template>

        <template v-else-if="activeStep === 5">
          <el-descriptions border :column="2">
            <el-descriptions-item label="学科">{{ form.subjectName }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ form.gradeName }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ form.semesterName }}</el-descriptions-item>
            <el-descriptions-item label="教材">{{ form.coursebookName }}</el-descriptions-item>
            <el-descriptions-item label="单元">{{ form.unitName }}</el-descriptions-item>
            <el-descriptions-item label="章">{{ form.chapterName }}</el-descriptions-item>
            <el-descriptions-item label="节">{{ form.sectionName }}</el-descriptions-item>
            <el-descriptions-item label="课时">{{ form.classHour ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="注意事项" :span="2">
              <div class="rich-view rich-view--panel" v-html="normalizeHtml(form.expCaution)" />
            </el-descriptions-item>
            <el-descriptions-item label="危险提示" :span="2">
              <div class="rich-view rich-view--panel" v-html="normalizeHtml(form.expDanger)" />
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-else-if="activeStep === 6">
          <div class="section-group">
            <div class="section-group__title">参考信息</div>
            <div v-if="referenceList.length" class="step-list">
              <div v-for="(item, index) in referenceList" :key="item.referenceId || item.uid" class="step-card read-card">
                <div class="step-card__header">
                  <div class="step-card__title">参考 {{ index + 1 }}：{{ item.referenceName || '未命名引用' }}</div>
                </div>
                <div class="step-card__meta">出处：{{ item.referenceSource || '-' }}</div>
                <div class="rich-view rich-view--panel" v-html="normalizeHtml(item.referenceComments)" />
              </div>
            </div>
            <div v-else class="step-placeholder">暂无参考信息</div>
          </div>
          <div class="section-group">
            <div class="section-group__title">科学家故事</div>
            <div v-if="scientistList.length" class="step-list">
              <div v-for="(item, index) in scientistList" :key="item.scientistId || item.uid" class="step-card read-card">
                <div class="step-card__header">
                  <div class="step-card__title">故事 {{ index + 1 }}：{{ item.storyName || '未命名故事' }}</div>
                </div>
                <div class="step-card__meta">科学家：{{ item.scientistName || '-' }}</div>
                <div class="rich-view rich-view--panel" v-html="normalizeHtml(item.storyComments)" />
              </div>
            </div>
            <div v-else class="step-placeholder">暂无科学家故事</div>
          </div>
        </template>
      </div>

      <div class="step-actions">
        <el-button :disabled="activeStep === 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < stepTitles.length - 1" type="primary" @click="nextStep">下一步</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchDataDictItems } from '../../api/data'
import { fetchExpStandardDetail, fetchExpVideos, fetchExpMaterials, fetchExpSteps, fetchExpResults, fetchExpReferences, fetchExpScientists } from '../../api/exp'

const route = useRoute()
const router = useRouter()
const expId = ref(String(route.query.expId || '').trim())
const loading = ref(false)
const activeStep = ref(0)
const stepTitles = ['基础信息', '实验视频', '实验材料', '实验步骤', '实验结果', '教学与安全', '参考与故事']
const form = reactive({ expName: '', chooseType: '', subjectId: '', subjectName: '', schoolLevelId: '', schoolLevelName: '', gradeId: '', gradeNames: [], difficultyId: '', expPrinciple: '', expCaution: '', expDanger: '', classHour: null, status: '' })
const videoList = ref([])
const expMaterialList = ref([])
const stepList = ref([])
const resultList = ref([])
const referenceList = ref([])
const scientistList = ref([])
const securityText = ref('')
const subjectOptions = ref([])
const schoolLevelOptions = ref([])
const gradeOptions = ref([])
const difficultyOptions = ref([])
const pageTitle = computed(() => '实验详情')
const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''

const normalizeOptions = (rows, valueKey, labelKey) => (rows || []).map(item => ({ value: String(item[valueKey]), label: item[labelKey] }))
const chooseTypeLabel = (value) => ({ must: '必做', choose: '选做' }[value] || value || '-')
const statusLabel = (value) => ({ c: '草稿', t: '待审核', y: '通过', n: '不通过' }[value] || value || '-')
const statusTagType = (value) => ({ c: 'info', t: 'warning', y: 'success', n: 'danger' }[value] || 'info')
const getSubjectLabel = (value) => subjectOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getSchoolLevelLabel = (value) => schoolLevelOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getGradeLabel = (value) => gradeOptions.value.find(item => item.value === String(value))?.label || value || '-'
const getDifficultyLabel = (value) => difficultyOptions.value.find(item => item.value === String(value))?.label || value || '-'
const gradeNamesText = computed(() => {
  const names = Array.isArray(form.gradeNames) ? form.gradeNames : []
  if (names.length) return names.join('、')
  return getGradeLabel(form.gradeId)
})

const normalizeHtml = (value) => {
  const text = String(value || '').trim()
  if (!text) return '<span class="empty-rich">-</span>'
  if (/<[a-z][\s\S]*>/i.test(text)) return text
  return text.replace(/\r?\n/g, '<br>')
}

const resolveFileUrl = (value) => {
  if (!value) return ''
  const raw = String(value).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
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

const loadDetail = async () => {
  if (!expId.value) {
    ElMessage.warning('缺少实验ID')
    return
  }
  loading.value = true
  try {
    const res = await fetchExpStandardDetail(expId.value)
    if (res.data.code === 200 && res.data.data) {
      const draft = res.data.data
      Object.assign(form, {
        expName: draft.expName || '',
        chooseType: draft.chooseType || '',
        subjectId: draft.subjectId || '',
        subjectName: draft.subjectName || '',
        schoolLevelId: draft.schoolLevelId || '',
        schoolLevelName: draft.schoolLevelName || '',
        gradeId: draft.gradeId || '',
        gradeName: draft.gradeName || '',
        gradeNames: draft.gradeNames || '',
        coursebookName: draft.coursebookName || '',
        unitName: draft.unitName || '',
        chapterName: draft.chapterName || '',
        sectionName: draft.sectionName || '',
        semesterName: draft.semesterName || '',
        difficultyId: draft.difficultyId || '',
        expPrinciple: draft.expPrinciple || '',
        expCaution: draft.expCaution || '',
        expDanger: draft.expDanger || '',
        classHour: draft.classHour ?? null,
        status: draft.status || ''
      })
      securityText.value = [draft.expCaution || '', draft.expDanger || ''].filter(Boolean).join('<br/>')
    }

    const [videoRes, materialRes, stepRes, resultRes, referenceRes, scientistRes] = await Promise.all([
      fetchExpVideos(expId.value),
      fetchExpMaterials(expId.value),
      fetchExpSteps(expId.value),
      fetchExpResults(expId.value),
      fetchExpReferences(expId.value),
      fetchExpScientists(expId.value)
    ])

    if (videoRes.data.code === 200) videoList.value = Array.isArray(videoRes.data.data) ? videoRes.data.data : []
    if (materialRes.data.code === 200) expMaterialList.value = Array.isArray(materialRes.data.data) ? materialRes.data.data : []
    if (stepRes.data.code === 200) stepList.value = Array.isArray(stepRes.data.data) ? stepRes.data.data : []
    if (resultRes.data.code === 200) resultList.value = Array.isArray(resultRes.data.data) ? resultRes.data.data : []
    if (referenceRes.data.code === 200) referenceList.value = Array.isArray(referenceRes.data.data) ? referenceRes.data.data : []
    if (scientistRes.data.code === 200) scientistList.value = Array.isArray(scientistRes.data.data) ? scientistRes.data.data : []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '加载详情失败')
  } finally {
    loading.value = false
  }
}

const nextStep = () => {
  if (activeStep.value < stepTitles.length - 1) activeStep.value += 1
}

const prevStep = () => {
  if (activeStep.value > 0) activeStep.value -= 1
}

const goStep = (index) => {
  if (index < 0 || index >= stepTitles.length) return
  activeStep.value = index
}

const goBack = () => router.push('/admin/exp/exp-standard')

onMounted(async () => {
  await loadDicts()
  await loadDetail()
})
</script>

<style scoped>
.create-header{display:flex;align-items:flex-start;justify-content:space-between;gap:12px}
.title-row{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
.page-subtitle{margin-top:4px;font-size:13px;color:#909399}
.status-tag{vertical-align:middle}
.section-group{margin-bottom:18px;padding:16px;border:1px solid #ebeef5;border-radius:12px;background:#fff}
.section-group__title{font-size:16px;font-weight:600;margin-bottom:12px;color:#303133}
.detail-card{width:100%;margin:0 auto}
.step-content{margin:18px auto 0;width:95%;}
.step-actions{display:flex;justify-content:space-between;margin-top:18px}
.step-placeholder{padding:18px 12px;color:#909399;background:#fafafa;border:1px dashed #dcdfe6;border-radius:10px;text-align:center}
.step-wrap :deep(.el-step__title){cursor:pointer}
.step-title-link{cursor:pointer}
.rich-view{line-height:1.9;word-break:break-word;overflow-wrap:anywhere}
.rich-view--panel{padding:12px 14px;background:#fafafa;border:1px solid #ebeef5;border-radius:10px}
:deep(.el-descriptions__label){width:108px;min-width:108px;max-width:108px}
.rich-view :deep(p){margin:0 0 10px}
.rich-view :deep(p:last-child){margin-bottom:0}
.rich-view :deep(img){max-width:100%;height:auto;display:block;margin:10px auto;border-radius:8px;box-shadow:0 4px 14px rgba(0,0,0,.08)}
.rich-view :deep(ul), .rich-view :deep(ol){padding-left:22px;margin:8px 0}
.rich-view :deep(li){margin:4px 0}
.empty-rich{color:#c0c4cc}
.read-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:16px;align-items:stretch}
.read-card{display:flex;flex-direction:column;gap:10px;padding:14px;border:1px solid #ebeef5;border-radius:12px;background:#fff;box-shadow:0 2px 10px rgba(0,0,0,.04)}
.read-preview{width:100%;height:220px;background:#000;border-radius:10px;overflow:hidden;display:flex;align-items:center;justify-content:center}
.read-preview video{width:100%;height:100%;display:block;object-fit:contain;background:#000}
.read-preview :deep(.el-image){width:100%;height:100%}
.read-material-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:16px;align-items:stretch}
.read-material-card{min-height:320px}
.read-material-preview{background:#f5f7fa}
.read-material-preview :deep(.el-image__inner){width:100%;height:100%;object-fit:contain}
.video-card__body,.material-card__body{padding-top:2px}
.step-card__meta{margin:8px 0 10px;color:#606266;font-size:13px}
.material-card__empty{display:flex;align-items:center;justify-content:center;height:200px;color:#909399;background:#fafafa}
@media (max-width:768px){.create-header{flex-direction:column;align-items:flex-start}.step-actions{gap:12px;flex-direction:column}.step-actions .el-button{width:100%}}
</style>
