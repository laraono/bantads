import { ref } from 'vue'

const TOKEN_KEY = 'bantads.token'

const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))

function setToken(newToken: string) {
  token.value = newToken
  localStorage.setItem(TOKEN_KEY, newToken)
}

function logout() {
  token.value = null
  localStorage.removeItem(TOKEN_KEY)
}

export function useAuth() {
  return {
    token,
    isAuthenticated: () => token.value !== null,
    setToken,
    logout,
  }
}
