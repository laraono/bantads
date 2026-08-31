import { ref } from "vue";
import type { UserRole } from "@/services/authService";

const TOKEN_KEY = 'bantads.token'
const ROLE_KEY = 'bantads.role'

const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
const role = ref<UserRole | null>(localStorage.getItem(ROLE_KEY) as UserRole || null)

function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
}

function setRole(newRole: UserRole) {
    role.value = newRole
    localStorage.setItem(ROLE_KEY, newRole)
}

function logout() {
    token.value = null
    role.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
}

export function useAuth() {
    return {
        token,
        role,
        isAuthenticated: () => token.value !== null,
        getUserRole: () => role.value,
        setToken,
        setRole,
        logout,
    }
}