import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, register as registerApi } from '../api/user'
import { clearAuth, getStoredUser, getToken, setStoredUser, setToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getStoredUser() || null)

  const isLoggedIn = computed(() => Boolean(token.value && userInfo.value?.role))

  async function login(loginForm) {
    const data = await loginApi(loginForm)
    const nextUserInfo = {
      userId: data.userId,
      username: data.username,
      role: Number(data.role)
    }

    token.value = data.token
    userInfo.value = nextUserInfo
    setToken(data.token)
    setStoredUser(nextUserInfo)

    return nextUserInfo
  }

  async function register(registerForm) {
    return registerApi({
      username: registerForm.username,
      password: registerForm.password,
      role: Number(registerForm.role)
    })
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
  }

  function updateUserInfo(partial) {
    userInfo.value = {
      ...(userInfo.value || {}),
      ...partial
    }
    setStoredUser(userInfo.value)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout,
    updateUserInfo
  }
})
