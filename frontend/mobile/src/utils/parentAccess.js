import { getUserInfo, USER_INFO_KEY } from '@/utils/authStorage'
import { normalizeRole, isParentRole } from '@/utils/role'
import { fetchSession } from '@/api/auth'

export const PARENT_AUDIT_PATH = '/bind-success'

const APPROVAL_ACK_PREFIX = 'parent_bind_approved_ack_'

/** 家长账号停用，或尚无任何已通过的孩子绑定 */
export function isParentRestricted(userInfo = getUserInfo()) {
  if (!isParentRole(userInfo?.userRoleId)) return false
  if (userInfo?.parentRestricted === true) return true
  if (userInfo?.status === 'n') return true
  return false
}

export function isParentRoleUser(userInfo = getUserInfo()) {
  return normalizeRole(userInfo?.userRoleId) === 'parent'
}

export function hasAcknowledgedParentApproval(userId = getUserInfo()?.userId) {
  if (!userId) return false
  return localStorage.getItem(`${APPROVAL_ACK_PREFIX}${userId}`) === '1'
}

export function markParentApprovalAcknowledged(userId = getUserInfo()?.userId) {
  if (!userId) return
  localStorage.setItem(`${APPROVAL_ACK_PREFIX}${userId}`, '1')
}

/** 绑定已通过且尚未展示过「进入家长端」提示 */
export function shouldShowParentApprovalCelebration(userInfo = getUserInfo()) {
  if (!isParentRole(userInfo?.userRoleId)) return false
  if (isParentRestricted(userInfo)) return false
  return !hasAcknowledgedParentApproval(userInfo.userId)
}

export function patchStoredUserInfo(partial) {
  const info = { ...getUserInfo(), ...partial }
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  return info
}

/** 从服务端刷新家长准入状态，避免绑定通过后仍停留在审核页 */
export async function syncParentAccessState() {
  const info = getUserInfo()
  if (!isParentRole(info?.userRoleId)) return info
  try {
    const res = await fetchSession()
    if (res?.code === 200 && res.data) {
      const d = res.data
      return patchStoredUserInfo({
        userId: d.userId || info.userId,
        username: d.username || info.username,
        loginName: d.loginName || info.loginName,
        userRoleId: d.userRoleId || info.userRoleId,
        status: d.status ?? info.status,
        rootOrgId: d.rootOrgId || info.rootOrgId,
        rootOrgName: d.rootOrgName || info.rootOrgName,
        parentRestricted: d.parentRestricted === true
      })
    }
  } catch {
    // ignore network errors; fall back to cached state
  }
  return info
}
