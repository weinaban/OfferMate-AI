import request from '../utils/request'

export function getCompanyInfo() {
  return request({
    url: '/company/info',
    method: 'get'
  })
}

export function updateCompanyInfo(data) {
  return request({
    url: '/company/info',
    method: 'put',
    data
  })
}

export function getCompanyPublicInfo(id) {
  return request({
    url: `/company/${id}`,
    method: 'get'
  })
}
