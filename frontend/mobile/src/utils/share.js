/**
 * 分享当前页面：优先 Web Share API，否则复制链接到剪贴板。
 */
import { showToast } from '@/utils/toast'

export async function sharePage({ title, text, url } = {}) {
  const shareUrl = url || (typeof window !== 'undefined' ? window.location.href : '')
  const shareTitle = title || document?.title || '宝山区小实验社区'
  const shareText = text || shareTitle

  if (typeof navigator !== 'undefined' && navigator.share) {
    try {
      await navigator.share({ title: shareTitle, text: shareText, url: shareUrl })
      return true
    } catch (e) {
      if (e?.name === 'AbortError') return false
    }
  }

  try {
    await navigator.clipboard.writeText(shareUrl)
    showToast('链接已复制')
    return true
  } catch {
    window.prompt('复制链接', shareUrl)
    return true
  }
}
