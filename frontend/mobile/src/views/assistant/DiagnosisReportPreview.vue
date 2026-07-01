<template>
  <div v-if="visible" class="report-overlay" @click.self="handleClose">
    <div class="report-shell">
      <!-- Toolbar -->
      <div class="report-toolbar">
        <button type="button" class="btn btn-ghost btn-sm" @click="handleClose">
          <i data-lucide="x" class="icon"></i> 关闭
        </button>
        <span class="report-toolbar__title">实验诊断报告</span>
        <div class="report-toolbar__actions">
          <button
            type="button"
            class="btn btn-ghost btn-sm"
            :class="{ 'is-active': mode === 'edit' }"
            @click="mode = 'edit'"
          >编辑</button>
          <button
            type="button"
            class="btn btn-ghost btn-sm"
            :class="{ 'is-active': mode === 'preview' }"
            @click="mode = 'preview'"
          >预览</button>
          <button
            type="button"
            class="btn btn-primary btn-sm"
            :disabled="exporting"
            @click="handleExport"
          >
            <i data-lucide="download" class="icon"></i>
            {{ exporting ? '导出中…' : '导出 PDF' }}
          </button>
        </div>
      </div>

      <!-- Edit mode -->
      <div v-show="mode === 'edit'" class="report-scroll">
        <div class="report-edit">
          <p class="report-edit__hint">可像医学影像报告一样逐栏编辑，保存后再导出 PDF。</p>

          <div class="form-group">
            <label class="form-label">报告编号</label>
            <input v-model="localReport.reportNo" type="text" class="input" readonly />
          </div>

          <div class="form-row">
            <div class="form-group flex-1">
              <label class="form-label">学生姓名</label>
              <input v-model="localReport.studentName" type="text" class="input" />
            </div>
            <div class="form-group flex-1">
              <label class="form-label">年级</label>
              <input v-model="localReport.gradeLevel" type="text" class="input" placeholder="如：三年级 / 中段" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group flex-1">
              <label class="form-label">实验项目</label>
              <input v-model="localReport.experimentTitle" type="text" class="input" placeholder="实验名称" />
            </div>
            <div class="form-group flex-1">
              <label class="form-label">报告日期</label>
              <input v-model="localReport.reportDate" type="text" class="input" />
            </div>
          </div>

          <div v-if="localReport.imageUrl" class="form-group">
            <label class="form-label">实验照片（影像资料）</label>
            <img :src="localReport.imageUrl" alt="实验照片" class="report-edit__image" />
          </div>

          <div class="form-group">
            <label class="form-label">一、实验说明</label>
            <textarea v-model="localReport.description" class="input" rows="3" placeholder="学生描述的实验过程与背景"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">二、观察所见</label>
            <textarea v-model="localReport.findings" class="input" rows="5" placeholder="从照片和描述中观察到的现象"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">三、诊断意见</label>
            <textarea v-model="localReport.diagnosis" class="input" rows="5" placeholder="对实验结果与问题的诊断分析"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">四、改进建议</label>
            <textarea v-model="localReport.recommendations" class="input" rows="4" placeholder="后续实验或操作的改进建议"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">五、备注</label>
            <textarea v-model="localReport.remarks" class="input" rows="2" placeholder="可选"></textarea>
          </div>
        </div>
      </div>

      <!-- Preview mode (A4 print layout, medical-report style) -->
      <div v-show="mode === 'preview'" class="report-scroll report-scroll--preview">
        <div ref="previewRef" class="report-a4">
          <div class="report-a4__header">
            <div class="report-a4__org">科学探究实验诊断中心</div>
            <h1 class="report-a4__title">实验诊断报告</h1>
            <div class="report-a4__subtitle">Science Experiment Diagnostic Report</div>
          </div>

          <table class="report-a4__meta">
            <tbody>
              <tr>
                <td class="report-a4__meta-label">报告编号</td>
                <td>{{ localReport.reportNo || '-' }}</td>
                <td class="report-a4__meta-label">报告日期</td>
                <td>{{ localReport.reportDate || '-' }}</td>
              </tr>
              <tr>
                <td class="report-a4__meta-label">姓名</td>
                <td>{{ localReport.studentName || '-' }}</td>
                <td class="report-a4__meta-label">年级</td>
                <td>{{ localReport.gradeLevel || '-' }}</td>
              </tr>
              <tr>
                <td class="report-a4__meta-label">实验项目</td>
                <td colspan="3">{{ localReport.experimentTitle || '（未填写）' }}</td>
              </tr>
            </tbody>
          </table>

          <section v-if="localReport.imageUrl" class="report-a4__block">
            <h2 class="report-a4__block-title">【影像资料】实验照片</h2>
            <div class="report-a4__image-wrap">
              <img :src="localReport.imageUrl" alt="实验照片" class="report-a4__image" />
            </div>
          </section>

          <section class="report-a4__block">
            <h2 class="report-a4__block-title">一、实验说明</h2>
            <p class="report-a4__text">{{ localReport.description || '（未填写）' }}</p>
          </section>

          <section class="report-a4__block">
            <h2 class="report-a4__block-title">二、观察所见</h2>
            <p class="report-a4__text pre-line">{{ localReport.findings || '（未填写）' }}</p>
          </section>

          <section class="report-a4__block">
            <h2 class="report-a4__block-title">三、诊断意见</h2>
            <p class="report-a4__text pre-line">{{ localReport.diagnosis || '（未填写）' }}</p>
          </section>

          <section class="report-a4__block">
            <h2 class="report-a4__block-title">四、改进建议</h2>
            <p class="report-a4__text pre-line">{{ localReport.recommendations || '（未填写）' }}</p>
          </section>

          <section v-if="localReport.remarks" class="report-a4__block">
            <h2 class="report-a4__block-title">五、备注</h2>
            <p class="report-a4__text pre-line">{{ localReport.remarks }}</p>
          </section>

          <div class="report-a4__footer">
            <div class="report-a4__sign-line">
              <span>报告生成：AI 实验分析助手</span>
              <span>审核：____________</span>
            </div>
            <p class="report-a4__disclaimer">本报告由 AI 辅助生成，供教学参考，最终解释以指导教师为准。</p>
          </div>
        </div>
      </div>

      <!-- Footer actions (edit mode) -->
      <div v-if="mode === 'edit'" class="report-footer">
        <button type="button" class="btn btn-ghost" @click="handleClose">取消</button>
        <button type="button" class="btn btn-primary" @click="handleSave">保存报告</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { buildDiagnosisReport, exportDiagnosisReportPdf } from '@/utils/diagnosisReport'
import { resolveGradeLabel } from '@/utils/gradeLevel'
import { useLucideIcons } from '@/composables/useLucideIcons'
import { useProfileStore } from '@/stores/profile'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  visible: { type: Boolean, default: false },
  report: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['close', 'save'])

const profileStore = useProfileStore()
const userStore = useUserStore()
const previewRef = ref(null)
const mode = ref('edit')
const localReport = ref(buildDiagnosisReport())
const exporting = ref(false)
const { initIcons } = useLucideIcons()

function mergeReportDefaults(report = {}) {
  const merged = { ...report }
  if (!merged.studentName) {
    merged.studentName =
      profileStore.profile.userName ||
      userStore.userInfo.username ||
      ''
  }
  if (!merged.gradeLevel) {
    merged.gradeLevel = resolveGradeLabel({
      profile: profileStore.profile,
      userInfo: userStore.userInfo,
    })
  }
  return merged
}

function applyLocalReport(report = {}) {
  localReport.value = buildDiagnosisReport(mergeReportDefaults(report))
}

watch(
  () => props.visible,
  async (show) => {
    if (show) {
      await profileStore.loadProfile()
      applyLocalReport(props.report)
      mode.value = 'edit'
      initIcons()
    }
  }
)

watch(
  () => props.report,
  (val) => {
    if (props.visible && val) {
      applyLocalReport(val)
    }
  },
  { deep: true }
)

function handleSave() {
  emit('save', { ...localReport.value, userEdited: true })
  mode.value = 'preview'
}

function handleClose() {
  emit('close')
}

async function handleExport() {
  const prevMode = mode.value
  mode.value = 'preview'
  await new Promise((r) => setTimeout(r, 150))
  if (!previewRef.value) return
  exporting.value = true
  try {
    await exportDiagnosisReportPdf(
      previewRef.value,
      `实验诊断报告_${localReport.value.reportNo || localReport.value.reportDate || 'report'}.pdf`
    )
  } finally {
    exporting.value = false
    mode.value = prevMode
  }
}

onMounted(initIcons)
</script>

<style scoped>
.report-overlay {
  position: fixed;
  inset: 0;
  z-index: 300;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: stretch;
  justify-content: center;
  padding: 0;
}
.report-shell {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  width: min(96vw, 920px);
  height: min(96vh, 100%);
  max-height: 96vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  margin: auto;
}
@media (max-width: 640px) {
  .report-shell {
    width: 100vw;
    height: 100vh;
    max-height: 100vh;
    border-radius: 0;
    margin: 0;
  }
}
.report-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}
.report-toolbar__title {
  flex: 1;
  font-weight: 600;
  font-size: var(--text-sm);
  text-align: center;
}
.report-toolbar__actions {
  display: flex;
  gap: 4px;
  align-items: center;
}
.report-toolbar__actions .is-active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
.report-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}
.report-scroll--preview {
  display: flex;
  justify-content: center;
  background: #e8e8e8;
}
.report-edit__hint {
  font-size: var(--text-xs);
  color: var(--color-text-2);
  margin-bottom: 12px;
  line-height: 1.5;
}
.report-edit__image {
  max-width: 100%;
  max-height: 160px;
  object-fit: contain;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}
.form-row {
  display: flex;
  gap: 10px;
}
.form-group {
  margin-bottom: 12px;
}
.form-label {
  display: block;
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-text-2);
  margin-bottom: 4px;
}
.report-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
}

/* A4 medical-style print page — compact for single page */
.report-a4 {
  background: #fff;
  color: #1a1a1a;
  width: 210mm;
  max-width: calc(100vw - 32px);
  min-height: 297mm;
  padding: 10mm 12mm;
  border: 1px solid #ccc;
  font-size: 10.5px;
  line-height: 1.4;
  box-sizing: border-box;
  flex-shrink: 0;
}
.report-a4--export {
  width: 794px;
  min-height: auto;
  max-width: 794px;
  padding: 28px 32px;
  font-size: 11px;
}
.report-a4__header {
  text-align: center;
  border-bottom: 2px solid #1a1a1a;
  padding-bottom: 8px;
  margin-bottom: 10px;
}
.report-a4__org {
  font-size: 9px;
  color: #666;
}
.report-a4__title {
  font-size: 18px;
  font-weight: 700;
  margin: 4px 0 2px;
  letter-spacing: 0.12em;
}
.report-a4__subtitle {
  font-size: 9px;
  color: #888;
}
.report-a4__meta {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 10px;
  font-size: 10px;
}
.report-a4__meta td {
  border: 1px solid #bbb;
  padding: 4px 6px;
}
.report-a4__meta-label {
  background: #f0f0f0;
  font-weight: 600;
  width: 64px;
  color: #444;
  white-space: nowrap;
}
.report-a4__block {
  margin-bottom: 8px;
}
.report-a4__block-title {
  font-size: 11px;
  font-weight: 700;
  color: #222;
  border-left: 3px solid #2563eb;
  padding-left: 6px;
  margin-bottom: 4px;
}
.report-a4__text {
  padding-left: 4px;
  color: #333;
  margin: 0;
  font-size: 10.5px;
}
.report-a4__text.pre-line {
  white-space: pre-line;
}
.report-a4__image-wrap {
  text-align: center;
  padding: 4px;
  border: 1px dashed #ccc;
  background: #fafafa;
}
.report-a4__image {
  max-width: 100%;
  max-height: 90px;
  object-fit: contain;
}
.report-a4--export .report-a4__image {
  max-height: 110px;
}
.report-a4__footer {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #ccc;
}
.report-a4__sign-line {
  display: flex;
  justify-content: space-between;
  font-size: 9px;
  color: #555;
  margin-bottom: 4px;
}
.report-a4__disclaimer {
  font-size: 8px;
  color: #999;
  text-align: center;
  margin: 0;
}
</style>
