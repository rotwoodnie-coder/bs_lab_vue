import request from './request'

export function fetchExpStandards(params) {
  return request.get('/exp/standards', { params })
}

export function fetchExpStandardsMy(params) {
  return request.get('/exp/standards/my', { params })
}

export function fetchExpStandardsAll(params) {
  return request.get('/exp/standards', { params })
}

export function fetchExpTeaches(params) {
  return request.get('/exp/teaches', { params })
}

export function fetchExpTeachesMy(params) {
  return request.get('/exp/teaches/my', { params })
}

export function fetchLatestExpTeachesDraft() {
  return request.get('/exp/teaches/draft/latest')
}
export function createExpTeachesFromStandard(data) {
  return request.post('/exp/teaches/from', data)
}
export function updateExpTeachSimulator(id, simulatorId) {
  return request.post(`/exp/teaches/${id}/simulator`, { simulatorId })
}
export function updateExpTeach(id, data) {
  return request.post(`/exp/teaches/${id}`, data)
}
export function fetchExpStandard(id) {
  return request.get(`/exp/standards/${id}`)
}

export function fetchExpStandardDetail(id) {
  return request.get(`/exp/standards/detail/${id}`)
}

export function createExpStandard(data) {
  return request.post('/exp/standards', data)
}

export function updateExpStandard(id, data) {
  return request.post(`/exp/standards/${id}`, data)
}

export function auditExpStandard(id, data) {
  return request.post(`/exp/standards/${id}/audit`, data)
}

export function deleteExpStandard(id) {
  return request.delete(`/exp/standards/${id}`)
}

export function fetchExpLogs(expId) {
  return request.get('/exp/logs', { params: { expId } })
}

export function fetchMyTeachBySection(sectionId) {
  return request.get('/exp/teaches/my/by-section', { params: { sectionId } })
}

export function fetchLatestExpStandardDraft() {  return request.get('/exp/standards/draft/latest')
}

//exp videos
export function fetchExpVideos(expId) {
  return request.get(`/exp/videos/${expId}`)
}

export function saveExpVideos(expId, videos) {
  return request.post(`/exp/videos/${expId}`, videos)
}

export function saveExpVideo(expId, video) {
  return request.post(`/exp/videos/${expId}/one`, video)
}

export function updateExpVideos(expId, videos) {
  return request.post(`/exp/videos/${expId}`, videos)
}

export function deleteExpVideo(seqId) {
  return request.delete(`/exp/videos/${seqId}`)
}

//exp materials
export function fetchExpMaterials(expId) {
  return request.get(`/exp/materials/${expId}`)
}

export function saveExpMaterials(expId, materials) {
  return request.post(`/exp/materials/${expId}`, materials)
}

export function saveExpMaterial(expId, material) {
  return request.post(`/exp/materials/${expId}/one`, material)
}

export function updateExpMaterial(expMaterialId, material) {
  return request.post(`/exp/materials/one/${expMaterialId}`, material)
}

export function fetchExpMaterialPics(expMaterialId) {
  return request.get(`/exp/materials/pics/${expMaterialId}`)
}

export function deleteExpMaterial(expMaterialId) {
  return request.delete(`/exp/materials/${expMaterialId}`)
}

//exp simulators
export function fetchExpSimulators(params) {
  return request.get('/exp/simulators', { params })
}

export function fetchExpSimulator(id) {
  return request.get(`/exp/simulators/${id}`)
}

export function createExpSimulator(data) {
  return request.post('/exp/simulators', data)
}

export function updateExpSimulator(id, data) {
  return request.post(`/exp/simulators/${id}`, data)
}

export function deleteExpSimulator(id) {
  return request.delete(`/exp/simulators/${id}`)
}

export function recordExpSimulatorLog(simulatorId) {
  return request.post(`/exp/simulators/${simulatorId}/logs`)
}

//exp steps
export function fetchExpSteps(expId) {
  return request.get(`/exp/steps/${expId}`)
}

export function saveExpSteps(expId, steps) {
  return request.post(`/exp/steps/${expId}`, steps)
}

export function saveExpStep(expId, step) {
  return request.post(`/exp/steps/${expId}/one`, step)
}

export function deleteExpStep(stepId) {
  return request.delete(`/exp/steps/${stepId}`)
}

//exp results
export function fetchExpResults(expId) {
  return request.get(`/exp/results/${expId}`)
}

export function saveExpResults(expId, results) {
  return request.post(`/exp/results/${expId}`, results)
}

export function saveExpResult(expId, result) {
  return request.post(`/exp/results/${expId}/one`, result)
}

export function deleteExpResult(resultId) {
  return request.delete(`/exp/results/${resultId}`)
}

//exp references
export function fetchExpReferences(expId) {
  return request.get(`/exp/references/${expId}`)
}

export function saveExpReferences(expId, references) {
  return request.post(`/exp/references/${expId}`, references)
}

export function saveExpReference(expId, reference) {
  return request.post(`/exp/references/${expId}/one`, reference)
}

export function deleteExpReference(referenceId) {
  return request.delete(`/exp/references/${referenceId}`)
}

//exp scientists
export function fetchExpScientists(expId) {
  return request.get(`/exp/scientists/${expId}`)
}

export function saveExpScientists(expId, scientists) {
  return request.post(`/exp/scientists/${expId}`, scientists)
}

export function saveExpScientist(expId, scientist) {
  return request.post(`/exp/scientists/${expId}/one`, scientist)
}

export function deleteExpScientist(scientistId) {
  return request.delete(`/exp/scientists/${scientistId}`)
}

//exp questions
export function fetchExpQuestions(params) {
  return request.get('/exp/questions', { params })
}

export function fetchExpQuestion(id) {
  return request.get(`/exp/questions/${id}`)
}

export function createExpQuestion(data) {
  return request.post('/exp/questions', data)
}

export function updateExpQuestion(id, data) {
  return request.post(`/exp/questions/${id}`, data)
}

export function deleteExpQuestion(id) {
  return request.delete(`/exp/questions/${id}`)
}

export function saveExpQuestionSelects(questionId, data) {
  return request.post(`/exp/questions/${questionId}/selects`, data)
}

export function deleteExpQuestionSelect(questionId, selectId) {
  return request.delete(`/exp/questions/${questionId}/selects/${selectId}`)
}

export function fetchExpQuestionSelects(questionId) {
  return request.get(`/exp/questions/${questionId}/selects`)
}
