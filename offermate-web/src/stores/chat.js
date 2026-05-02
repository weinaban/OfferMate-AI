import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getChatSessions } from '../api/chat'

function normalizeList(data) {
  if (Array.isArray(data)) {
    return data
  }

  return data?.records || data?.list || data?.rows || []
}

function sumUnreadCount(list) {
  return normalizeList(list).reduce((total, session) => {
    return total + Number(session?.unreadCount || 0)
  }, 0)
}

export const useChatStore = defineStore('chat', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      unreadCount.value = sumUnreadCount(await getChatSessions())
    } catch (error) {
      unreadCount.value = 0
    }
  }

  function setUnreadCount(count) {
    unreadCount.value = Math.max(0, Number(count) || 0)
  }

  function decreaseUnreadCount(step = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - Number(step || 1))
  }

  function increaseUnreadCount(step = 1) {
    unreadCount.value += Number(step || 1)
  }

  function clearUnreadCount() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    setUnreadCount,
    decreaseUnreadCount,
    increaseUnreadCount,
    clearUnreadCount
  }
})
