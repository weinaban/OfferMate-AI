import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadNotificationCount } from '../api/notification'

function normalizeCount(data) {
  if (typeof data === 'number') {
    return data
  }

  if (typeof data === 'string') {
    return Number(data) || 0
  }

  return Number(data?.count ?? data?.unreadCount ?? data?.total ?? 0) || 0
}

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      unreadCount.value = normalizeCount(await getUnreadNotificationCount())
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

  function clearUnreadCount() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    setUnreadCount,
    decreaseUnreadCount,
    clearUnreadCount
  }
})
