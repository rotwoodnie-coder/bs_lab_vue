/** 首页 / 模拟实验共用的年级 Chip 配置 */

export const GRADE_FILTERS = [
  { key: 'all', label: '全部' },
  { key: 'g12', label: '1-2年级' },
  { key: 'g34', label: '3-4年级' },
  { key: 'g56', label: '5-6年级' }
]

export function gradeEmptyHint(gradeKey) {
  if (!gradeKey || gradeKey === 'all') return '暂无内容'
  const item = GRADE_FILTERS.find((g) => g.key === gradeKey)
  const label = item ? item.label : '该年级'
  return `${label}暂无内容，试试其他年级吧`
}
