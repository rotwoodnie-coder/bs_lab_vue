export const PASSWORD_HINT = '不少于 8 位，且包含字母和数字'

export function validatePassword(password) {
  const value = password || ''
  if (value.length < 8) return '密码不少于 8 位'
  if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) return '密码需同时包含字母和数字'
  return ''
}
