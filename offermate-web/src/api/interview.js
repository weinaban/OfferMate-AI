import request from '../utils/request'

export function createInterview(data) {
  return request({
    url: '/interviews',
    method: 'post',
    data
  })
}

export function getCompanyInterviews(params) {
  return request({
    url: '/interviews/company',
    method: 'get',
    params
  })
}

export function getMyInterviews(params) {
  return request({
    url: '/interviews/my',
    method: 'get',
    params
  })
}

export function acceptInterview(id) {
  return request({
    url: `/interviews/${id}/accept`,
    method: 'put'
  })
}

export function rejectInterview(id) {
  return request({
    url: `/interviews/${id}/reject`,
    method: 'put'
  })
}

export function cancelInterview(id) {
  return request({
    url: `/interviews/${id}/cancel`,
    method: 'put'
  })
}
