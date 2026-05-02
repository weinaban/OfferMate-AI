import request from '../utils/request'

export function createDelivery(data) {
  return request({
    url: '/deliveries',
    method: 'post',
    data
  })
}

export function getMyDeliveries() {
  return request({
    url: '/deliveries/my',
    method: 'get'
  })
}

export function deleteDelivery(id) {
  return request({
    url: `/deliveries/${id}`,
    method: 'delete'
  })
}
