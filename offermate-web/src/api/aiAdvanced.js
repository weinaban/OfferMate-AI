import request from '../utils/request'

const AI_TIMEOUT = 120000

export function matchJob(data) {
  return request({
    url: '/ai/job/match',
    method: 'post',
    data,
    timeout: AI_TIMEOUT
  })
}

export function createInterviewSession(data) {
  return request({
    url: '/ai/interview/session',
    method: 'post',
    data,
    timeout: AI_TIMEOUT
  })
}

export function generateInterviewQuestion(sessionId) {
  return request({
    url: `/ai/interview/session/${sessionId}/question`,
    method: 'post',
    timeout: AI_TIMEOUT
  })
}

export function submitInterviewAnswer(sessionId, data) {
  return request({
    url: `/ai/interview/session/${sessionId}/answer`,
    method: 'post',
    data,
    timeout: AI_TIMEOUT
  })
}

export function getInterviewReport(sessionId) {
  return request({
    url: `/ai/interview/session/${sessionId}/report`,
    method: 'get',
    timeout: AI_TIMEOUT
  })
}
