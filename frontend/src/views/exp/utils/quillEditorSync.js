/** 归一化富文本 HTML，用于签名比较 */
export function normalizeRichTextHtml(html) {
  if (!html) return ''
  return String(html)
    .replace(/<p><br><\/p>/gi, '')
    .replace(/<p>\s*<\/p>/gi, '')
    .replace(/\s+/g, ' ')
    .trim()
}
