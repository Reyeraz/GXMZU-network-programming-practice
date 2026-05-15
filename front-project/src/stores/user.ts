import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const username = ref<string>('')
  const userId = ref<number | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(newToken: string, newUsername: string, newUserId: number) {
    token.value = newToken
    username.value = newUsername
    userId.value = newUserId
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('userId', String(newUserId))
  }

  function loadAuth() {
    const savedToken = localStorage.getItem('token')
    const savedUsername = localStorage.getItem('username')
    const savedUserId = localStorage.getItem('userId')
    if (savedToken && savedUsername && savedUserId) {
      token.value = savedToken
      username.value = savedUsername
      userId.value = Number(savedUserId)
    }
  }

  function logout() {
    token.value = ''
    username.value = ''
    userId.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
  }

  return {
    token,
    username,
    userId,
    isLoggedIn,
    setAuth,
    loadAuth,
    logout
  }
})
