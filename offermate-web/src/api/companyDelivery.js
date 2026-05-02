import request from '../utils/request'

export function getCompanyDeliveries() {
  return request({
    url: '/deliveries/company',
    method: 'get'
  })
}

export function updateDeliveryStatus(id, status) {
  return request({
    url: `/deliveries/${id}/status`,
    method: 'put',
    data: { status }
  })
}
