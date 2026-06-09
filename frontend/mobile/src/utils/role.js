import { getUserInfo } from '@/utils/authStorage'

/** @returns {'student'|'teacher'|'parent'} */
export function normalizeRole(role) {
  const r = String(role || 'student').toLowerCase()
  if (r === 'teacher' || r.includes('teacher')) return 'teacher'
  if (r === 'parent') return 'parent'
  return 'student'
}

export function currentRole() {
  const info = getUserInfo()
  return normalizeRole(info?.userRoleId)
}

export function isTeacherRole(role) {
  return normalizeRole(role) === 'teacher'
}

export function isParentRole(role) {
  return normalizeRole(role) === 'parent'
}

export function isStudentRole(role) {
  return normalizeRole(role) === 'student'
}
