/**
 * 统一文档展示格式化：序号分行、bullet 分行、段落首行缩进 2em、HTML/换行兼容
 */

import { rewriteRichTextMediaUrls } from './fileUrl'

const HTML_TAG_RE = /<[a-z][\s\S]*>/i
const COMPLEX_HTML_RE = /<(ul|ol|table|thead|tbody|tr|th|td|img|iframe|video|embed|a)\b/i
const SERIAL_MARKER_RE = /(\d+[\.．、]|[（(]\d+[）)]|[①-⑳])/g
const SERIAL_ITEM_RE = /^(\d+[\.．、]\s*|[（(]\d+[）)]\s*|[①-⑳])\s*(.*)$/s
const BULLET_LINE_RE = /^\s*[-•*·]\s+/
const VALID_MARKER_BEFORE_RE = /[\s，。；：、！？;,]/

/** 实验长文：原理、故事、参考资料等 */
export const FORMAT_EXP_LONG = {
  mode: 'auto',
  paragraph: 'strict',
  minItems: 2,
  minCharsForIndent: 12
}

/** 步骤/结果说明：常一行一句 */
export const FORMAT_EXP_STEP = {
  mode: 'auto',
  paragraph: 'loose',
  minItems: 2,
  minCharsForIndent: 8
}

/** 任务摘要等：优先序号，段落关闭 */
export const FORMAT_EXP_BRIEF = {
  mode: 'auto',
  paragraph: 'off',
  minItems: 2
}

/** 单行短文本 */
export const FORMAT_PLAIN = {
  mode: 'plain',
  paragraph: 'off'
}

export function hasRichContent(value) {
  return Boolean(String(value || '').trim())
}

export function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/**
 * @param {string} value
 * @param {object} [options]
 */
export function formatDisplayHtml(value, options = {}) {
  const opts = {
    mode: 'auto',
    paragraph: 'strict',
    minItems: 2,
    minCharsForIndent: 12,
    ...options
  }

  const raw = String(value ?? '').trim()
  if (!raw) return ''

  if (opts.mode === 'html-only') {
    return enhanceHtml(raw)
  }

  let text = raw
  if (opts.mode === 'auto' && HTML_TAG_RE.test(raw)) {
    if (COMPLEX_HTML_RE.test(raw)) {
      return enhanceHtml(raw)
    }
    text = stripSimpleHtml(raw)
    if (!text) return enhanceHtml(raw)
  }

  text = normalizeSerialSeparators(text)

  if (opts.mode !== 'plain') {
    const serial = splitSerialList(text, opts.minItems)
    if (serial) {
      return renderSerialList(serial, opts)
    }

    const bullet = splitBulletList(text)
    if (bullet) {
      return renderBulletList(bullet)
    }
  }

  if (opts.paragraph !== 'off') {
    return formatParagraphs(text, opts)
  }

  return escapeHtml(text).replace(/\r?\n/g, '<br>')
}

/** @deprecated 请使用 formatDisplayHtml；保留兼容 */
export function normalizeHtml(value, options) {
  return formatDisplayHtml(value, options ?? { mode: 'auto' })
}

/** 将 ;2. / ，3. 等整理为可识别的序号分隔 */
function normalizeSerialSeparators(text) {
  return String(text)
    .replace(/([;；,，])\s*(?=\d+[\.．、])/g, ' ')
    .replace(/([。！？])\s*(?=\d+[\.．、])/g, '$1 ')
}

function stripSimpleHtml(html) {
  return String(html)
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p>/gi, '\n')
    .replace(/<p[^>]*>/gi, '')
    .replace(/<div[^>]*>/gi, '\n')
    .replace(/<\/div>/gi, '')
    .replace(/<span[^>]*>/gi, '')
    .replace(/<\/span>/gi, '')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/\u00a0/g, ' ')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function isValidSerialMarker(text, index) {
  if (index === 0) return true
  const prev = text[index - 1]
  if (VALID_MARKER_BEFORE_RE.test(prev)) return true

  const slice = text.slice(Math.max(0, index - 4), index)
  if (/[a-zA-Z]$/.test(slice)) return false
  if (/[\d\.]$/.test(slice) && /^\d+[\.．、]/.test(text.slice(index))) return false
  return false
}

function splitSerialList(text, minItems) {
  const markers = []
  SERIAL_MARKER_RE.lastIndex = 0
  let match
  while ((match = SERIAL_MARKER_RE.exec(text)) !== null) {
    if (isValidSerialMarker(text, match.index)) {
      markers.push({ index: match.index, marker: match[0] })
    }
  }

  if (markers.length < minItems) return null

  const intro = text.slice(0, markers[0].index).trim()
  const items = markers.map((entry, idx) => {
    const start = entry.index
    const end = idx + 1 < markers.length ? markers[idx + 1].index : text.length
    return text.slice(start, end).trim()
  })

  return { intro, items }
}

function splitBulletList(text) {
  const lines = text.split(/\r?\n/).map((line) => line.trim()).filter(Boolean)
  if (lines.length < 2) return null
  if (!lines.every((line) => BULLET_LINE_RE.test(line))) return null
  return lines.map((line) => line.replace(BULLET_LINE_RE, '').trim())
}

function renderSerialList({ intro, items }, opts) {
  const parts = []

  if (intro) {
    parts.push(formatParagraphs(intro, { ...opts, paragraph: 'strict' }))
  }

  for (const item of items) {
    const parsed = item.match(SERIAL_ITEM_RE)
    if (!parsed) {
      parts.push(`<div class="text-serial-line"><span class="text-serial-body">${escapeHtml(item)}</span></div>`)
      continue
    }
    const marker = parsed[1].trim()
    const body = parsed[2].trim()
    parts.push(
      `<div class="text-serial-line">` +
        `<span class="text-serial-no">${escapeHtml(marker)}</span>` +
        `<span class="text-serial-body">${escapeHtml(body)}</span>` +
      `</div>`
    )
  }

  return parts.join('')
}

function renderBulletList(items) {
  return items.map((item) =>
    `<div class="text-bullet-line"><span class="text-bullet-dot" aria-hidden="true">•</span><span class="text-bullet-body">${escapeHtml(item)}</span></div>`
  ).join('')
}

function formatParagraphs(text, opts) {
  let blocks
  if (opts.paragraph === 'loose') {
    blocks = text.split(/\r?\n+/).map((s) => s.trim()).filter(Boolean)
  } else {
    blocks = text.split(/\n{2,}/).map((s) => s.trim().replace(/\r?\n/g, ' ')).filter(Boolean)
    if (blocks.length <= 1 && text.includes('\n') && !text.includes('\n\n')) {
      blocks = text.split(/\r?\n/).map((s) => s.trim()).filter(Boolean)
    }
  }

  if (!blocks.length) return ''

  const indent = shouldParagraphIndent(blocks, opts)

  return blocks.map((block) => {
    const inner = escapeHtml(block).replace(/\r?\n/g, '<br>')
    const cls = indent ? 'text-para' : 'text-plain'
    return `<p class="${cls}">${inner}</p>`
  }).join('')
}

function shouldParagraphIndent(blocks, opts) {
  if (blocks.length >= 2) return true
  const only = blocks[0] || ''
  return only.length >= (opts.minCharsForIndent || 12)
}

function enhanceHtml(html) {
  return rewriteRichTextMediaUrls(String(html).trim())
}

/** 开发期自测样例 */
export function __formatDisplayHtmlFixtures() {
  const cases = [
    { in: '1. A 2. B 3. C', expect: 'text-serial-line', count: 3 },
    { in: '1.看一看：A。2.看一看：B。3.闻一闻：C。', expect: 'text-serial-line', count: 3 },
    { in: '1.严禁品尝 ;2.尝水时 ;3.将水倒入', expect: 'text-serial-line', count: 3 },
    { in: '<p>1.第一项 2.第二项 3.第三项</p>', expect: 'text-serial-line', count: 3 },
    { in: '第一段\n\n第二段', expect: 'text-para', count: 2 },
    { in: '- A\n- B', expect: 'text-bullet-line', count: 2 },
    { in: '注意安全', expect: 'text-plain', count: 1 }
  ]
  return cases.map(({ in: input, expect, count }) => {
    const html = formatDisplayHtml(input, { paragraph: 'loose' })
    const matched = (html.match(new RegExp(expect, 'g')) || []).length
    return { input: input.slice(0, 40), expect, count, matched, ok: html.includes(expect) && matched >= Math.min(count, 2) }
  })
}
