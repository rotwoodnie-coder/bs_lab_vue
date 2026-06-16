import { getUserInfo } from '@/utils/authStorage'

/** @returns {'student'|'teacher'|'parent'|'researcher'|'admin'} */
export function normalizeRole(role) {
  const r = String(role || 'student').toLowerCase()
  if (r === 'teacher' || r.includes('teacher')) return 'teacher'
  if (r === 'parent') return 'parent'
  if (r === 'researcher') return 'researcher'
  if (r === 'sys_admin' || r === 'school_admin' || r.includes('admin')) return 'admin'
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

export function isResearcherRole(role) {
  return normalizeRole(role) === 'researcher'
}

export function isAdminRole(role) {
  return normalizeRole(role) === 'admin'
}

export function canAuditExperiments(role) {
  const r = normalizeRole(role)
  return r === 'researcher' || r === 'admin'
}
