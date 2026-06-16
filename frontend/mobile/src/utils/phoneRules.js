export const PHONE_HINT = '请输入 11 位中国大陆手机号'

export function validatePhone(phone) {
  const value = String(phone || '').trim()
  if (!value) return '请输入手机号'
  if (!/^1\d{10}$/.test(value)) return '手机号格式不正确'
  return ''
}

export function maskPhone(phone) {
  if (!phone || phone.length < 7) return ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}
