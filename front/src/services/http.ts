import axios from 'axios'
import { useAuth } from '@/composables/useAuth'

export const http = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const { token } = useAuth()
  if (token.value) {
    config.headers.Authorization = `Bearer ${token.value}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const { logout } = useAuth()
      logout()
    }
    return Promise.reject(error)
  },
)