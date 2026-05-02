import request from '../utils/request'

function cleanParams(params = {}) {
  return Object.entries(params).reduce((result, [key, value]) => {
    if (value !== '' && value !== undefined && value !== null) {
      result[key] = value
    }

    return result
  }, {})
}

export function getAdminUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params: cleanParams(params)
  })
}

export function disableAdminUser(id) {
  return request({
    url: `/admin/users/${id}/disable`,
    method: 'put'
  })
}

export function enableAdminUser(id) {
  return request({
    url: `/admin/users/${id}/enable`,
    method: 'put'
  })
}

export function getAdminCompanyAudits(params) {
  return request({
    url: '/admin/companies/audit',
    method: 'get',
    params: cleanParams(params)
  })
}

export function auditAdminCompany(id, auditStatus) {
  return request({
    url: `/admin/companies/${id}/audit`,
    method: 'put',
    data: {
      auditStatus,
      status: auditStatus
    }
  })
}

export function getAdminJobAudits(params) {
  return request({
    url: '/admin/jobs/audit',
    method: 'get',
    params: cleanParams(params)
  })
}

export function auditAdminJob(id, auditStatus) {
  return request({
    url: `/admin/jobs/${id}/audit`,
    method: 'put',
    data: {
      auditStatus,
      status: auditStatus
    }
  })
}

export function getAdminOperationLogs(params) {
  return request({
    url: '/admin/operation-logs',
    method: 'get',
    params: cleanParams(params)
  })
}
