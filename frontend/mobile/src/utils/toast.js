/** 轻量 Toast，替代 alert 用于非阻塞反馈 */
export function showToast(message, type = 'success', duration = 2400) {
  if (!message || typeof document === 'undefined') return
  const el = document.createElement('div')
  el.className = `toast toast-${type === 'danger' ? 'danger' : 'success'}`
  el.setAttribute('role', 'status')
  el.textContent = message
  document.body.appendChild(el)
  window.setTimeout(() => {
    el.style.opacity = '0'
    el.style.transition = 'opacity 0.2s ease'
    window.setTimeout(() => el.remove(), 200)
  }, duration)
}
