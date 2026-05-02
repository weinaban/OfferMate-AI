import { getToken } from './auth'

function getDefaultWsBaseUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://localhost:8080`
}

export function createChatWebSocket({ onMessage, onOpen, onClose, onError } = {}) {
  let socket = null
  let reconnectTimer = null
  let manuallyClosed = false

  function buildUrl() {
    const baseUrl = import.meta.env.VITE_WS_BASE_URL || getDefaultWsBaseUrl()
    const token = encodeURIComponent(getToken() || '')
    return `${baseUrl.replace(/\/$/, '')}/ws/chat?token=${token}`
  }

  function connect() {
    manuallyClosed = false
    socket = new WebSocket(buildUrl())

    socket.onopen = () => {
      onOpen?.()
    }

    socket.onmessage = (event) => {
      try {
        onMessage?.(JSON.parse(event.data))
      } catch (error) {
        onMessage?.(event.data)
      }
    }

    socket.onerror = (event) => {
      onError?.(event)
    }

    socket.onclose = () => {
      if (!manuallyClosed) {
        onClose?.()
        reconnectTimer = window.setTimeout(connect, 3000)
      }
    }
  }

  function send(data) {
    if (!socket || socket.readyState !== WebSocket.OPEN) {
      return false
    }

    socket.send(typeof data === 'string' ? data : JSON.stringify(data))
    return true
  }

  function close() {
    manuallyClosed = true
    window.clearTimeout(reconnectTimer)

    if (socket) {
      socket.close()
      socket = null
    }
  }

  return {
    connect,
    send,
    close,
    get readyState() {
      return socket?.readyState
    }
  }
}
