import request from '../utils/request'

export function getNotifications(params) {
  return request({
    url: '/notifications',
    method: 'get',
    params
  })
}

export function getUnreadNotificationCount() {
  return request({
    url: '/notifications/unread/count',
    method: 'get'
  })
}

export function markNotificationRead(id) {
  return request({
    url: `/notifications/${id}/read`,
    method: 'put'
  })
}

export function markAllNotificationsRead() {
  return request({
    url: '/notifications/read/all',
    method: 'put'
  })
}
