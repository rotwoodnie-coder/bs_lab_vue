/** 学生端作品类型角标（homework 统一展示为「作品」；兼容历史 API 返回「作业」） */
export function workTypeTagLabel(typeOrLabel) {
  if (typeOrLabel === 'homework' || typeOrLabel === '作业') return '作品'
  if (typeOrLabel === 'remix') return '拍同款'
  if (typeOrLabel === 'creative') return '创意'
  return typeOrLabel || '作品'
}
