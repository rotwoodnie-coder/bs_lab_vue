/**
 * Lucide 图标（npm 本地打包，离线可用）。
 * 使用完整图标集，避免按需注册遗漏导致页面图标空白。
 */
import { createIcons, icons } from 'lucide'

const ICON_OPTIONS = {
  icons,
  attrs: {
    'stroke-width': 2
  },
  nameAttr: 'data-lucide'
}

export function initLucideIcons(root) {
  createIcons({ ...ICON_OPTIONS, root })
}
