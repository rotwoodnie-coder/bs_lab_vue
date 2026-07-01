import request from '@/api/request'

/**
 * Review an experiment image via AI vision
 * @param {string} imageBase64 - Base64-encoded image data
 * @param {object} [context={}] - Additional context (e.g. { scene: 'experiment_review', title: 'xxx' })
 * @returns {Promise<{ success: boolean, data: { score: number, feedback: string, suggestions: string[] } }>}
 */
export function reviewImage(imageBase64, context = {}) {
  return request.post('/mobile/vision/review', {
    image_base64: imageBase64,
    context: { scene: 'experiment_review', ...context }
  })
}
