import request from '../utils/request'

export async function uploadFile(file, bizType) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)

  const data = await request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    timeout: 60000
  })

  if (typeof data === 'string') {
    return data
  }

  return data?.url || data?.data?.url || ''
}
