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
    config.headers['x-access-token'] = token.value
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const { logout } = useAuth()
      logout()
      // sessão expirada (30 min de inatividade) ou revogada: volta para o login.
      // import dinâmico porque o router importa as views, que importam este arquivo
      import('@/router').then(({ default: router }) => {
        const atual = router.currentRoute.value
        if (atual.name !== 'login') {
          router.push({ name: 'login', query: { redirect: atual.fullPath } })
        }
      })
    }
    return Promise.reject(error)
  },
)