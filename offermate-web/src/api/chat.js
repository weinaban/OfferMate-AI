import request from '../utils/request'

export function createOrGetChatSession(data) {
  const targetUserId = data?.targetUserId ?? data?.otherUserId ?? data?.seekerId ?? data?.recruiterId ?? data?.userId

  return request({
    url: '/chats/sessions',
    method: 'post',
    data: {
      jobId: data?.jobId,
      targetUserId
    }
  })
}

export function getChatSessions() {
  return request({
    url: '/chats/sessions',
    method: 'get'
  })
}

export function getChatMessages(sessionId) {
  return request({
    url: `/chats/sessions/${sessionId}/messages`,
    method: 'get'
  })
}

export function markSessionRead(sessionId) {
  return request({
    url: `/chats/sessions/${sessionId}/read`,
    method: 'put'
  })
}
