import { onMounted, onUnmounted, nextTick } from 'vue'

/**
 * 安全的 lucide 图标初始化 composable
 *
 * - 用 _mounted 标志防止组件卸载后操作 DOM
 * - 两个 nextTick 等待 Vue DOM 完全稳定
 * - try-catch 静默忽略 insertBefore 等渲染竞争错误
 *
 * 用法: const { initIcons } = useLucideIcons()
 *       然后在 onMounted 或 finally 末尾调用 initIcons()
 */
export function useLucideIcons() {
  let _mounted = true

  onUnmounted(() => { _mounted = false })

  async function initIcons() {
    await nextTick()
    await nextTick()
    try {
      const { createIcons, icons } = await import('lucide')
      if (_mounted) createIcons({ icons })
    } catch {
      // 静默忽略：组件已卸载或 DOM 被 Vue 重渲染
    }
  }

  return { initIcons }
}
