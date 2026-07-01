/**
 * Load an image File into an HTMLImageElement.
 */
function loadImage(file) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => { URL.revokeObjectURL(url); resolve(img) }
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('Image load failed')) }
    img.src = url
  })
}

/**
 * Compress an image file to a base64 data URI.
 *
 * @param {File} file - Input image file
 * @param {object} [options]
 * @param {number} [options.maxWidth=800]
 * @param {number} [options.maxHeight=800]
 * @param {number} [options.quality=0.7] - Initial JPEG quality (0-1)
 * @param {number} [options.maxSizeKB=500] - Target max file size in KB
 * @returns {Promise<{ base64: string, originalSize: number, compressedSize: number, width: number, height: number, quality: number }>}
 */
export async function compressImage(file, options = {}) {
  const { maxWidth = 800, maxHeight = 800, quality: initialQuality = 0.7, maxSizeKB = 500 } = options
  const originalSize = file.size

  const img = await loadImage(file)
  let { width, height } = img

  // Resize if needed (maintain aspect ratio)
  if (width > maxWidth || height > maxHeight) {
    const ratio = Math.min(maxWidth / width, maxHeight / height)
    width = Math.round(width * ratio)
    height = Math.round(height * ratio)
  }

  // Iteratively adjust quality to meet maxSizeKB
  let quality = initialQuality
  let base64 = ''
  const minQuality = 0.15
  const step = 0.1

  for (let attempt = 0; attempt < 8; attempt++) {
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, 0, 0, width, height)

    base64 = canvas.toDataURL('image/jpeg', quality)
    const compressedBytes = Math.round((base64.length * 3) / 4)
    const compressedKB = compressedBytes / 1024

    if (compressedKB <= maxSizeKB || quality <= minQuality) break
    quality = Math.max(minQuality, quality - step)
  }

  const compressedBytes = Math.round((base64.length * 3) / 4)

  return {
    base64,
    originalSize,
    compressedSize: compressedBytes,
    width,
    height,
    quality: Math.round(quality * 100) / 100
  }
}
