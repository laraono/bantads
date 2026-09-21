import { http } from '@/services/http'
import { accountDataService, clientService } from '.'
import { setSession } from '@/store/session'

export type UserRole = 'cliente' | 'gerente'

export interface LoginPayload {
  email: string
  password: string
  role: UserRole
}

export interface LoginResponse {
  token: string
}

// Login falso para navegar pelas telas enquanto o ms-auth ainda não está de pé.
// Ativado via VITE_MOCK_AUTH=true (ver .env.development).
export const MOCK_USERS: Record<string, { password: string; role: UserRole }> = {
  'cli1@bantads.com.br': { password: 'tads', role: 'cliente' },
  'cli2@bantads.com.br': { password: 'tads', role: 'cliente' },
  'cli3@bantads.com.br': { password: 'tads', role: 'cliente' },
  'cli4@bantads.com.br': { password: 'tads', role: 'cliente' },
  'cli5@bantads.com.br': { password: 'tads', role: 'cliente' },
  'ger1@bantads.com.br': { password: 'tads', role: 'gerente' },
  'ger2@bantads.com.br': { password: 'tads', role: 'gerente' },
  'ger3@bantads.com.br': { password: 'tads', role: 'gerente' },
  'ger4@bantads.com.br': { password: 'tads', role: 'gerente' },
}

function mockLogin(payload: LoginPayload): Promise<LoginResponse> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = MOCK_USERS[payload.email]
      if (user && user.password === payload.password && user.role === payload.role) {
        resolve({ token: `mock-token-${payload.role}` })
        const client = clientService.findByEmail(payload.email)
        const account = accountDataService.findByClient(client!.cpf)
        setSession({
          accountNumber: account!.accountNumber, 
          userCPF: client!.cpf, 
          userName: client!.name, 
          email: payload.email
        }
      )
      } else {
        reject(new Error('Credenciais inválidas'))
      }
    }, 300)
  })
}

export function login(payload: LoginPayload) {
  if (import.meta.env.VITE_MOCK_AUTH === 'true') {
    return mockLogin(payload)
  }
  return http.post<LoginResponse>('/auth/login', payload).then((res) => res.data)
}