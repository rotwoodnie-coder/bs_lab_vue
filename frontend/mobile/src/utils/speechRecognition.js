/**
 * Browser Web Speech API wrapper (zh-CN).
 * Returns null when SpeechRecognition is unavailable.
 */
export function createSpeechRecognizer(options = {}) {
  if (typeof window === 'undefined') return null
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) return null

  const rec = new SpeechRecognition()
  rec.lang = options.lang || 'zh-CN'
  rec.interimResults = !!options.interimResults
  rec.maxAlternatives = 1

  rec.onresult = (event) => {
    const result = event.results?.[event.results.length - 1]
    const text = result?.[0]?.transcript?.trim()
    if (text) options.onResult?.(text)
  }
  rec.onerror = (event) => {
    options.onError?.(event.error || 'unknown')
  }
  rec.onend = () => {
    options.onEnd?.()
  }
  return rec
}

export function isSpeechRecognitionSupported() {
  if (typeof window === 'undefined') return false
  return !!(window.SpeechRecognition || window.webkitSpeechRecognition)
}
