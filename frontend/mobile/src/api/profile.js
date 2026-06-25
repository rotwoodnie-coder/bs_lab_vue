import request from './request'

/**
 * 获取当前用户个人中心信息
 * @returns {Promise<{userId, userName, loginName, userNickName, userPhone, userEmail, userLogo, rootOrgName, userRoleId, perResume, perScore, prefTitleName, createTime, lastLoginTime}>}
 */
export function fetchProfile() {
  return request.get('/mobile/profile')
}

/**
 * 更新个人资料
 * @param {object} data - { userName?, userNickName?, userPhone?, userEmail?, perResume?, userLogo? }
 */
export function updateProfile(data) {
  return request.post('/mobile/profile', data)
}

/**
 * 修改密码
 * @param {string} oldPassword
 * @param {string} newPassword
 */
export function changePassword(oldPassword, newPassword) {
  return request.post('/mobile/profile/password', { oldPassword, newPassword })
}

