import request from '../utils/request'

function cleanParams(params = {}) {
  return Object.entries(params).reduce((result, [key, value]) => {
    if (value !== '' && value !== undefined && value !== null) {
      result[key] = value
    }

    return result
  }, {})
}

export function getJobPage(params) {
  return request({
    url: '/jobs/page',
    method: 'get',
    params: cleanParams(params)
  })
}

export function searchJobs(params) {
  return request({
    url: '/jobs/search',
    method: 'get',
    params: cleanParams(params)
  })
}

export function getJobDetail(id) {
  return request({
    url: `/jobs/${id}`,
    method: 'get'
  })
}
