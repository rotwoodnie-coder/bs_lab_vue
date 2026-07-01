/**
 * 诊断报告：解析 AI 输出、精简文字、构建报告结构
 */

const SECTION_MAP = [
  {
    key: 'findings',
    patterns: [/现象观察/, /观察所见/, /实验现象/, /影像所见/, /^观察/, /看到了/],
  },
  {
    key: 'diagnosis',
    patterns: [/诊断意见/, /问题说明/, /问题诊断/, /诊断分析/, /初步诊断/, /存在的危险/, /错误分析/, /问题/],
  },
  {
    key: 'recommendations',
    patterns: [/改进建议/, /处理建议/, /正确操作/, /改进办法/, /^建议/, /应该/],
  },
]

/** 去除 Markdown 装饰，保留要点 */
export function stripMarkdown(text = '') {
  return String(text)
    .replace(/^#{1,6}\s*/gm, '')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/^[-*•]\s+/gm, '• ')
    .replace(/^\d+[.、．)]\s+/gm, (m) => m)
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

/** 精简为报告适用的短文本（控制 A4 单页容量） */
export function condenseText(text = '', maxLen = 180, maxBullets = 3) {
  if (!text) return ''
  let t = stripMarkdown(text)
  const lines = t.split(/\n+/).map((l) => l.trim()).filter(Boolean)
  if (lines.length > maxBullets) {
    t = lines.slice(0, maxBullets).join('\n')
  } else {
    t = lines.join('\n')
  }
  if (t.length > maxLen) {
    t = `${t.slice(0, maxLen - 1)}…`
  }
  return t
}

/**
 * 从 AI 分析文本拆分三节。
 * 支持 ### 小标题、## 标题、以及「标签：」行首格式。
 */
export function parseAnalysisSections(text = '') {
  const raw = String(text || '').trim()
  const empty = { findings: '', diagnosis: '', recommendations: '' }
  if (!raw) return empty

  const result = { ...empty }

  // 1) 按 Markdown 标题切块（支持文首即为 ###）
  const parts = raw.split(/(?=#{2,3}\s+)/).map((p) => p.trim()).filter(Boolean)
  if (parts.length > 1 || (parts.length === 1 && /^#{2,3}\s/.test(parts[0]))) {
    for (const part of parts) {
      const lines = part.split('\n')
      const header = lines[0].replace(/^#{1,6}\s*/, '').replace(/[：:]\s*$/, '').trim()
      const body = stripMarkdown(lines.slice(1).join('\n'))
      assignSection(result, header, body)
    }
  }

  // 2) 若标题拆分不足，再按「标签：」行匹配
  if (!result.findings && !result.diagnosis && !result.recommendations) {
    const labelRegex =
      /(?:^|\n)\s*(?:#{1,3}\s*)?(现象观察|观察所见|问题说明|诊断意见|问题诊断|改进建议|处理建议)\s*[：:]\s*/gi
    const matches = [...raw.matchAll(labelRegex)]
    if (matches.length > 0) {
      for (let i = 0; i < matches.length; i += 1) {
        const label = matches[i][1]
        const start = matches[i].index + matches[i][0].length
        const end = i + 1 < matches.length ? matches[i + 1].index : raw.length
        assignSection(result, label, stripMarkdown(raw.slice(start, end)))
      }
    }
  }

  // 3) 兜底：全文放入观察所见
  if (!result.findings && !result.diagnosis && !result.recommendations) {
    result.findings = stripMarkdown(raw)
  }

  return {
    findings: condenseText(result.findings, 200, 3),
    diagnosis: condenseText(result.diagnosis, 180, 3),
    recommendations: condenseText(result.recommendations, 150, 3),
  }
}

function assignSection(result, header, body) {
  if (!body) return
  for (const { key, patterns } of SECTION_MAP) {
    if (result[key]) continue
    if (patterns.some((p) => p.test(header))) {
      result[key] = body
      return
    }
  }
}

export function buildDiagnosisReport(source = {}) {
  const now = new Date()
  const reportDate =
    source.reportDate ||
    `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  const reportNo =
    source.reportNo ||
    `EXP-${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}-${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`

  const raw = source.analysisRaw || source.analysis || ''
  let sections
  let useStructuredDirect = false

  if (source.userEdited) {
    sections = {
      findings: source.findings || '',
      diagnosis: source.diagnosis || '',
      recommendations: source.recommendations || '',
    }
  } else if (source.findings || source.diagnosis || source.recommendations) {
    sections = {
      findings: source.findings || '',
      diagnosis: source.diagnosis || '',
      recommendations: source.recommendations || '',
    }
    useStructuredDirect = true
  } else if (raw) {
    sections = parseAnalysisSections(raw)
  } else {
    sections = {
      findings: source.findings || '',
      diagnosis: source.diagnosis || '',
      recommendations: source.recommendations || '',
    }
  }

  const normalizeSection = (text, maxLen, maxBullets) =>
    useStructuredDirect ? (text || '').trim() : condenseText(text, maxLen, maxBullets)

  return {
    reportNo,
    reportDate,
    studentName: source.studentName || '',
    gradeLevel: source.gradeLevel || '',
    experimentTitle: source.experimentTitle || source.description?.slice(0, 30) || '',
    imageUrl: source.imageUrl || '',
    description: condenseText(source.description || '', 80, 2),
    findings: normalizeSection(sections.findings, 200, 3),
    diagnosis: normalizeSection(sections.diagnosis, 180, 3),
    recommendations: normalizeSection(sections.recommendations, 150, 3),
    remarks: source.remarks || '',
    analysisRaw: raw,
    userEdited: Boolean(source.userEdited),
  }
}

export function mergeDiagnosisReport(existing, patch = {}) {
  return { ...existing, ...patch }
}

/** 诊断报告专用 PDF 导出：单页 A4、正确比例 */
export async function exportDiagnosisReportPdf(element, filename = '实验诊断报告.pdf') {
  if (!element) return

  const { default: html2canvas } = await import('html2canvas')
  const { default: jsPDF } = await import('jspdf')

  const A4_W = 210
  const A4_H = 297
  const margin = 8

  element.classList.add('report-a4--export')
  await new Promise((r) => requestAnimationFrame(r))

  try {
    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff',
      width: element.scrollWidth,
      height: element.scrollHeight,
    })

    const pdf = new jsPDF('p', 'mm', 'a4')
    const contentW = A4_W - margin * 2
    const contentH = A4_H - margin * 2

    let drawW = contentW
    let drawH = (canvas.height * drawW) / canvas.width

    // 缩放到单页 A4 内
    if (drawH > contentH) {
      const scale = contentH / drawH
      drawH = contentH
      drawW = drawW * scale
    }

    const imgData = canvas.toDataURL('image/png', 1.0)
    const x = margin + (contentW - drawW) / 2
    pdf.addImage(imgData, 'PNG', x, margin, drawW, drawH)
    pdf.save(filename)
  } finally {
    element.classList.remove('report-a4--export')
  }
}
