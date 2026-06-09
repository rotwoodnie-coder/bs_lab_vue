const CHOOSE_TYPE = { must: '必做', choose: '选做' }
const EXP_TYPE = {
  standard: '标准实验',
  teacher: '教学实验',
  teaching: '教学实验',
  student: '学生实验'
}

export function chooseTypeLabel(value) {
  return CHOOSE_TYPE[value] || ''
}

export function expTypeLabel(value) {
  return EXP_TYPE[value] || '实验'
}

export function formatClassHour(value) {
  if (value == null || value === '') return ''
  const n = Number(value)
  if (Number.isNaN(n)) return `${value} 课时`
  return `${n} 课时`
}

/** 对照教材：按层级返回标签行（仅含有效字段） */
export function curriculumRows(detail) {
  if (!detail) return []
  const candidates = [
    { key: 'coursebook', label: '教材', value: detail.coursebookName },
    { key: 'unit', label: '单元', value: detail.unitName },
    { key: 'chapter', label: '章', value: detail.chapterName },
    { key: 'section', label: '节', value: detail.sectionName }
  ]
  return candidates.filter((row) => row.value)
}

/** 标题下方显示精简属性：学科、年级、课时 */
export function metaChipItems(detail) {
  if (!detail) return []
  const chips = []
  if (detail.subjectName) chips.push({ key: 'subject', label: detail.subjectName })
  if (detail.gradeNames) {
    const g = String(detail.gradeNames).replace(/,/g, '、')
    if (g) chips.push({ key: 'grade', label: g })
  }
  const hour = formatClassHour(detail.classHour)
  if (hour) chips.push({ key: 'hour', label: hour })
  return chips
}
