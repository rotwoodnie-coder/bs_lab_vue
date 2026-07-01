import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const cache = new Map()
const CACHE_MAX = 50

/**
 * Strip dangerous HTML tags/attributes for XSS safety (v-html usage).
 */
function sanitize(html) {
  return html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/on\w+\s*=\s*"[^"]*"/gi, '')
    .replace(/on\w+\s*=\s*'[^']*'/gi, '')
}

/**
 * Render markdown text to safe HTML with in-memory caching.
 * @param {string} text - Raw markdown string
 * @returns {string} Sanitized HTML
 */
export function renderMarkdown(text) {
  if (!text) return ''
  if (cache.has(text)) return cache.get(text)
  const html = sanitize(marked.parse(text))
  cache.set(text, html)
  if (cache.size > CACHE_MAX) cache.delete(cache.keys().next().value)
  return html
}
