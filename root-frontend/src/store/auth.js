import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from '../services/api'

export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = ref(!!localStorage.getItem('access_token'))
  const router = useRouter()

  async function login(credentials) {
    try {
      const response = await apiClient.post('/auth/login/user', credentials)
      if (response.data.code === 200) {
        const { access_token, refresh_token } = response.data.data
        console.log('Access Token:', access_token)
        console.log('Refresh Token:', refresh_token)
        localStorage.setItem('access_token', access_token)
        localStorage.setItem('refresh_token', refresh_token)
        isAuthenticated.value = true
        router.push('/')
      } else {
        // Handle login failure
        console.error(response.data.message)
      }
    } catch (error) {
      console.error('Login failed:', error)
    }
  }

  function logout() {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    isAuthenticated.value = false
    router.push('/login')
  }

  return { isAuthenticated, login, logout }
})
