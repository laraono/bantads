import { http } from '@/services/http'
import type { Usuario } from '@/composables/useAuth'

export type UserRole = 'cliente' | 'gerente'

export interface LoginPayload {
  email: string
  senha: string
}

export interface LoginResponse {
  token: string
  role: UserRole
  usuario: Usuario
}

// Login falso para navegar pelas telas sem o docker-compose no ar.
// Ativado via VITE_MOCK_AUTH=true (ver .env.development).
const MOCK_USERS: Record<string, { senha: string; role: UserRole; usuario: Usuario }> = {
  'cli1@bantads.com.br': {
    senha: 'tads',
    role: 'cliente',
    usuario: { cpf: '12912861012', nome: 'Catharyna', email: 'cli1@bantads.com.br' },
  },
  'ger1@bantads.com.br': {
    senha: 'tads',
    role: 'gerente',
    usuario: { cpf: '98574307084', nome: 'Geniéve', email: 'ger1@bantads.com.br' },
  },
}

function mockLogin(payload: LoginPayload): Promise<LoginResponse> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = MOCK_USERS[payload.email]
      if (user && user.senha === payload.senha) {
        resolve({ token: `mock-token-${user.role}`, role: user.role, usuario: user.usuario })
      } else {
        reject(new Error('Credenciais inválidas'))
      }
    }, 300)
  })
}

// tipo do backend ("CLIENTE"/"GERENTE") -> role usada nas rotas do front
function roleFromTipo(tipo: string): UserRole {
  return tipo?.toUpperCase() === 'GERENTE' ? 'gerente' : 'cliente'
}

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  if (import.meta.env.VITE_MOCK_AUTH === 'true') {
    return mockLogin(payload)
  }
  const { data } = await http.post('/login', payload)
  return {
    token: data.token,
    role: roleFromTipo(data.tipo),
    usuario: data.usuario,
  }
}

export async function logout(): Promise<void> {
  if (import.meta.env.VITE_MOCK_AUTH === 'true') {
    return
  }
  await http.post('/logout').catch(() => {
    // logout no back é best-effort: mesmo se falhar, o front limpa a sessão local
  })
}
