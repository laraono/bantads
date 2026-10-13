import { ref } from "vue";
import type { UserRole } from "@/services/authService";

export interface Usuario {
    cpf: string
    nome: string
    email: string
}

const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY
const ROLE_KEY = import.meta.env.VITE_ROLE_KEY
const USER_KEY = import.meta.env.VITE_USER_KEY

function loadUsuario(): Usuario | null {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
        return JSON.parse(raw) as Usuario
    } catch {
        return null
    }
}

const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
const role = ref<UserRole | null>(localStorage.getItem(ROLE_KEY) as UserRole || null)
const usuario = ref<Usuario | null>(loadUsuario())

function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
}

function setRole(newRole: UserRole) {
    role.value = newRole
    localStorage.setItem(ROLE_KEY, newRole)
}

function setUsuario(newUsuario: Usuario) {
    usuario.value = newUsuario
    localStorage.setItem(USER_KEY, JSON.stringify(newUsuario))
}

function logout() {
    token.value = null
    role.value = null
    usuario.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
    localStorage.removeItem(USER_KEY)
}

export function useAuth() {
    return {
        token,
        role,
        usuario,
        isAuthenticated: () => token.value !== null,
        getUserRole: () => role.value,
        setToken,
        setRole,
        setUsuario,
        logout,
    }
}
