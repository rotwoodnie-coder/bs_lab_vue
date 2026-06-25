/** 封面缩略图：最长边上限 */
export const COVER_MAX_EDGE = 480
/** 超过该字节数或最长边则压缩 */
export const COVER_COMPRESS_BYTES = 300 * 1024
export const COVER_COMPRESS_MAX_EDGE = 1280
export const COVER_JPEG_QUALITY = 0.82

/** 视频封面：在前 N 秒内采样选最优帧 */
export const VIDEO_COVER_SAMPLE_WINDOW_SEC = 5
export const VIDEO_COVER_SAMPLE_COUNT = 8
/** 评分用小图宽度（加速，不影响最终封面尺寸） */
const VIDEO_COVER_SCORE_MAX_WIDTH = 160

const IMAGE_MIME = /^image\/(png|jpe?g|webp|gif|bmp)$/i

export function isImageFileType(file) {
  if (!file) return false
  if (file.type && IMAGE_MIME.test(file.type)) return true
  return /\.(png|jpe?g|webp|gif|bmp)$/i.test(String(file.name || ''))
}

export function isVideoFileType(file) {
  if (!file) return false
  if (file.type?.startsWith('video/')) return true
  return /\.(mp4|mov|avi|mkv|webm)$/i.test(String(file.name || ''))
}

export async function shouldCompressForCover(file, { maxBytes = COVER_COMPRESS_BYTES, maxEdge = COVER_COMPRESS_MAX_EDGE } = {}) {
  if (!file || !isImageFileType(file)) return false
  if (file.size > maxBytes) return true
  const { width, height } = await readImageDimensions(file)
  return Math.max(width, height) > maxEdge
}

function readImageDimensions(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve({ width: img.naturalWidth, height: img.naturalHeight })
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('无法读取图片尺寸'))
    }
    img.src = url
  })
}

function loadImageFromFile(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片加载失败'))
    }
    img.src = url
  })
}

function fitInside(width, height, maxEdge) {
  const longest = Math.max(width, height)
  if (longest <= maxEdge) return { width, height }
  const scale = maxEdge / longest
  return {
    width: Math.max(1, Math.round(width * scale)),
    height: Math.max(1, Math.round(height * scale))
  }
}

/**
 * 将图片压缩为 JPEG Blob（用于封面上传）
 * @param {File|Blob} file
 * @param {{ maxEdge?: number, quality?: number }} options
 */
export async function compressImageToBlob(file, { maxEdge = COVER_MAX_EDGE, quality = COVER_JPEG_QUALITY } = {}) {
  const img = await loadImageFromFile(file)
  const { width, height } = fitInside(img.naturalWidth, img.naturalHeight, maxEdge)
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) throw new Error('无法创建画布')
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, width, height)
  ctx.drawImage(img, 0, 0, width, height)
  const blob = await new Promise((resolve, reject) => {
    canvas.toBlob((result) => {
      if (result) resolve(result)
      else reject(new Error('图片压缩失败'))
    }, 'image/jpeg', quality)
  })
  return blob
}

/** 大图生成封面用小图；小图直接返回原 File */
export async function prepareCoverImageFile(file) {
  const needCompress = await shouldCompressForCover(file)
  if (!needCompress) return file
  const blob = await compressImageToBlob(file)
  const base = String(file.name || 'cover').replace(/\.[^.]+$/, '') || 'cover'
  return new File([blob], `${base}_cover.jpg`, { type: 'image/jpeg' })
}

/**
 * 在前若干秒内均匀采样多帧，按清晰度+亮度+色彩丰富度选最优封面
 * @param {File} file
 * @param {{ sampleWindowSec?: number, sampleCount?: number, maxEdge?: number }} options
 */
export function captureVideoPosterBlob(file, {
  sampleWindowSec = VIDEO_COVER_SAMPLE_WINDOW_SEC,
  sampleCount = VIDEO_COVER_SAMPLE_COUNT,
  maxEdge = COVER_MAX_EDGE
} = {}) {
  const sampleWindow = sampleWindowSec
  const sampleN = sampleCount
  const edge = maxEdge

  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const video = document.createElement('video')
    video.preload = 'auto'
    video.muted = true
    video.playsInline = true

    const cleanup = () => {
      URL.revokeObjectURL(url)
      video.removeAttribute('src')
      video.load()
    }

    const fail = (message) => {
      cleanup()
      reject(new Error(message))
    }

    video.onerror = () => fail('视频加载失败，无法生成封面')

    video.onloadedmetadata = async () => {
      try {
        const duration = Number.isFinite(video.duration) ? video.duration : sampleWindow
        const sampleTimes = buildVideoSampleTimes(duration, sampleWindow, sampleN)
        const scored = []

        for (const time of sampleTimes) {
          await seekVideo(video, time)
          const score = scoreVideoFrame(video)
          scored.push({ time, score })
        }

        const best = scored.reduce((a, b) => (b.score > a.score ? b : a), scored[0])
        const bestTime = best?.time ?? sampleTimes[0] ?? 0

        await seekVideo(video, bestTime)
        const blob = await renderVideoFrameToBlob(video, edge)
        cleanup()
        resolve(blob)
      } catch (err) {
        fail(err?.message || '视频封面生成失败')
      }
    }

    video.src = url
  })
}

/** 在前 sampleWindowSec 秒内生成均匀采样时间点 */
function buildVideoSampleTimes(duration, sampleWindowSec, sampleCount) {
  const safeDuration = Math.max(0.1, duration || sampleWindowSec)
  const windowEnd = Math.min(sampleWindowSec, Math.max(0.2, safeDuration - 0.05))
  const windowStart = Math.min(0.25, windowEnd * 0.08)
  const count = Math.max(3, Math.min(sampleCount, 12))

  if (windowEnd <= windowStart) {
    return [0]
  }

  const times = []
  for (let i = 0; i < count; i++) {
    const ratio = count === 1 ? 0 : i / (count - 1)
    times.push(windowStart + (windowEnd - windowStart) * ratio)
  }
  return times
}

function seekVideo(video, time) {
  return new Promise((resolve, reject) => {
    const onSeeked = () => {
      video.removeEventListener('seeked', onSeeked)
      video.removeEventListener('error', onError)
      resolve()
    }
    const onError = () => {
      video.removeEventListener('seeked', onSeeked)
      video.removeEventListener('error', onError)
      reject(new Error('视频定位失败'))
    }
    video.addEventListener('seeked', onSeeked)
    video.addEventListener('error', onError)
    try {
      video.currentTime = Math.max(0, time)
    } catch (err) {
      onSeeked()
    }
  })
}

/**
 * 帧质量评分：清晰度（梯度）× 亮度适中 × 色彩方差（排除黑屏/纯色）
 */
function scoreVideoFrame(video) {
  const vw = video.videoWidth
  const vh = video.videoHeight
  if (!vw || !vh) return 0

  const scale = VIDEO_COVER_SCORE_MAX_WIDTH / vw
  const width = Math.max(1, Math.round(vw * scale))
  const height = Math.max(1, Math.round(vh * scale))

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d', { willReadFrequently: true })
  if (!ctx) return 0
  ctx.drawImage(video, 0, 0, width, height)

  const { data } = ctx.getImageData(0, 0, width, height)
  const pixels = width * height
  let lumSum = 0
  let lumSumSq = 0
  let gradientSum = 0
  let gradientCount = 0

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const i = (y * width + x) * 4
      const lum = 0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2]
      lumSum += lum
      lumSumSq += lum * lum

      if (x > 0 && y > 0) {
        const il = ((y * width + x - 1) * 4)
        const iu = (((y - 1) * width + x) * 4)
        const lumL = 0.299 * data[il] + 0.587 * data[il + 1] + 0.114 * data[il + 2]
        const lumU = 0.299 * data[iu] + 0.587 * data[iu + 1] + 0.114 * data[iu + 2]
        gradientSum += Math.abs(lum - lumL) + Math.abs(lum - lumU)
        gradientCount += 2
      }
    }
  }

  const mean = lumSum / pixels
  const variance = Math.max(0, lumSumSq / pixels - mean * mean)
  const std = Math.sqrt(variance)
  const sharpness = gradientCount > 0 ? gradientSum / gradientCount : 0

  let brightnessFactor = 1
  if (mean < 40) brightnessFactor = mean / 40
  else if (mean > 215) brightnessFactor = (255 - mean) / 40

  let varianceFactor = 1
  if (std < 14) varianceFactor = std / 14

  return sharpness * brightnessFactor * varianceFactor
}

function renderVideoFrameToBlob(video, maxEdge) {
  const { width, height } = fitInside(video.videoWidth, video.videoHeight, maxEdge)
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) return Promise.reject(new Error('无法创建画布'))
  ctx.drawImage(video, 0, 0, width, height)
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) resolve(blob)
      else reject(new Error('视频封面导出失败'))
    }, 'image/jpeg', COVER_JPEG_QUALITY)
  })
}
