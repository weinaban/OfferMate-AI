import request from '../utils/request'

export function updateSeekerAvatar(url) {
  return request({
    url: '/seeker/avatar',
    method: 'post',
    data: {
      avatar: url,
      url
    }
  })
}
