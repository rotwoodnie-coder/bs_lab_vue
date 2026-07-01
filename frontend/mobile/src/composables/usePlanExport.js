import { ref } from 'vue'

/**
 * A4 document export composable (html2canvas + jspdf).
 * Renders a DOM element into a PDF with A4 proportions.
 *
 * @returns {{ exporting: Ref<boolean>, exportToPdf: Function }}
 */
export function usePlanExport() {
  const exporting = ref(false)

  /**
   * Export a DOM element to A4 PDF.
   * @param {HTMLElement|string} elementOrSelector - Target element or CSS selector
   * @param {object} [options]
   * @param {string} [options.filename='实验方案.pdf']
   * @param {string} [options.title='实验方案']
   * @param {number} [options.margin=16] - Margin in mm
   */
  async function exportToPdf(elementOrSelector, options = {}) {
    const { filename = '实验方案.pdf', title = '实验方案', margin = 16 } = options

    const element = typeof elementOrSelector === 'string'
      ? document.querySelector(elementOrSelector)
      : elementOrSelector

    if (!element) {
      console.warn('[usePlanExport] target element not found')
      return
    }

    exporting.value = true

    try {
      const { default: html2canvas } = await import('html2canvas')
      const { default: jsPDF } = await import('jspdf')

      const canvas = await html2canvas(element, {
        scale: 2,
        useCORS: true,
        logging: false,
        backgroundColor: '#ffffff',
      })

      const imgData = canvas.toDataURL('image/jpeg', 0.95)
      const imgWidth = canvas.width
      const imgHeight = canvas.height

      // A4: 210 x 297 mm
      const pdfWidth = 210 - margin * 2
      const pdfHeight = (imgHeight * pdfWidth) / imgWidth

      const pdf = new jsPDF('p', 'mm', 'a4')
      let yOffset = margin

      // Optional title line
      if (title) {
        pdf.setFontSize(14)
        pdf.text(title, margin, yOffset, { align: 'left' })
        yOffset += 10
      }

      // If content exceeds one page, split across multiple pages
      const pageHeight = 297 - margin * 2
      let remainingHeight = pdfHeight
      let srcY = 0

      while (remainingHeight > 0) {
        const sliceHeight = Math.min(remainingHeight, pageHeight)
        const sliceRatio = sliceHeight / pdfHeight

        pdf.addImage(
          imgData, 'JPEG',
          margin, yOffset,
          pdfWidth, sliceHeight,
          undefined, 'FAST',
          0, srcY / imgHeight
        )

        remainingHeight -= sliceHeight
        srcY += sliceHeight * (imgHeight / pdfHeight)

        if (remainingHeight > 0) {
          pdf.addPage()
          yOffset = margin
        }
      }

      pdf.save(filename)
    } catch (err) {
      console.error('[usePlanExport] export failed:', err)
    } finally {
      exporting.value = false
    }
  }

  return { exporting, exportToPdf }
}
