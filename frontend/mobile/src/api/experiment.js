import request from './request'

export function fetchExpDetail(expId) {
  return request.get(`/mobile/content/exp/${expId}/detail`)
}

export function fetchExpVideos(expId) {
  return request.get(`/mobile/content/exp/${expId}/videos`)
}

export function fetchExpSteps(expId) {
  return request.get(`/mobile/content/exp/${expId}/steps`)
}

export function fetchExpMaterials(expId) {
  return request.get(`/mobile/content/exp/${expId}/materials`)
}

export function fetchExpResults(expId) {
  return request.get(`/mobile/content/exp/${expId}/results`)
}

export function fetchExpReferences(expId) {
  return request.get(`/mobile/content/exp/${expId}/references`)
}

export function fetchExpScientists(expId) {
  return request.get(`/mobile/content/exp/${expId}/scientists`)
}
