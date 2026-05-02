import request from '../utils/request'

export function createResume(data) {
  return request({
    url: '/resumes',
    method: 'post',
    data
  })
}

export function updateResume(id, data) {
  return request({
    url: `/resumes/${id}`,
    method: 'put',
    data
  })
}

export function deleteResume(id) {
  return request({
    url: `/resumes/${id}`,
    method: 'delete'
  })
}

export function getResumeDetail(id) {
  return request({
    url: `/resumes/${id}`,
    method: 'get'
  })
}

export function getMyResumes() {
  return request({
    url: '/resumes/my',
    method: 'get'
  })
}

export function setDefaultResume(id) {
  return request({
    url: `/resumes/${id}/default`,
    method: 'put'
  })
}
