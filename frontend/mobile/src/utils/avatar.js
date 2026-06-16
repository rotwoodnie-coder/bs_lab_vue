/** 按用户角色返回头像占位渐变 class */
export function avatarGradByRole(role) {
  const r = String(role || 'student').toLowerCase()
  if (r.includes('parent')) return 'avatar-grad-cool'
  if (r.includes('teacher')) return 'avatar-grad-ocean'
  return 'avatar-grad-warm'
}

/** 从显示名取首字 */
export function avatarInitial(name, fallback = '用') {
  const text = String(name || '').trim()
  return text ? text.charAt(0) : fallback
}
