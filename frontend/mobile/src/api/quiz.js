import request from './request'

export function fetchTodayQuiz() {
  return request.get('/mobile/quiz/today')
}

export function fetchQuizRecord(date) {
  return request.get('/mobile/quiz/record', {
    params: { date: date || undefined }
  })
}

export function fetchQuizReview(date) {
  return request.get('/mobile/quiz/review', {
    params: { date: date || undefined }
  })
}

export function submitQuiz(payload) {
  return request.post('/mobile/quiz/submit', payload)
}
