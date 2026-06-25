import { onMounted, onUnmounted, nextTick } from 'vue'
import { initLucideIcons } from '@/utils/lucideIcons'

/**
 * 按需 lucide 图标初始化（见 utils/lucideIcons.js）
 * @param {HTMLElement|null|undefined} root 限定扫描范围，默认 document
 */
export function useLucideIcons() {
  let _mounted = true

  onUnmounted(() => { _mounted = false })

  async function initIcons(root) {
    await nextTick()
    await nextTick()
    if (!_mounted) return
    try {
      initLucideIcons(root || undefined)
    } catch {
      // 组件已卸载或 DOM 被重渲染
    }
  }

  return { initIcons }
}
