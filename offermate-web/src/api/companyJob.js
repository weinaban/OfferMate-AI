import request from '../utils/request'

export function getCompanyJobs() {
  return request({
    url: '/jobs/company',
    method: 'get'
  })
}

export function createCompanyJob(data) {
  return request({
    url: '/jobs',
    method: 'post',
    data
  })
}

export function updateCompanyJob(id, data) {
  return request({
    url: `/jobs/${id}`,
    method: 'put',
    data
  })
}

export function deleteCompanyJob(id) {
  return request({
    url: `/jobs/${id}`,
    method: 'delete'
  })
}

export function offlineCompanyJob(id) {
  return request({
    url: `/jobs/${id}/offline`,
    method: 'put'
  })
}
