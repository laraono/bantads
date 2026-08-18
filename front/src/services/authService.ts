import { http } from '@/services/http'

export type UserRole = 'cliente' | 'gerente'

export interface LoginPayload {
  email: string
  password: string
  role: UserRole
}

export interface LoginResponse {
  token: string
}

export function login(payload: LoginPayload) {
  return http.post<LoginResponse>('/auth/login', payload).then((res) => res.data)
}
