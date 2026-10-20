import { http } from '@/services/http'

export interface GerenteDTO {
  cpf: string
  nome: string
  email: string
  telefone: string
  ativo: boolean
  quantidadeClientes: number
}

export interface NovoGerentePayload {
  nome: string
  cpf: string
  email: string
  telefone: string
  senha: string
}

export interface AtualizarGerentePayload {
  nome: string
  telefone: string
}

// R12 — API Composition no gateway (MS Gerente + quantidade de clientes do MS Conta)
export function listarGerentes() {
  return http.get<{ gerentes: GerenteDTO[] }>('/gerentes').then((r) => r.data.gerentes)
}

// R13 — SAGA; responde 202 { jobId }
export function inserirGerente(payload: NovoGerentePayload) {
  return http.post<{ jobId?: string }>('/gerentes', payload)
}

// R14 — e-mail e CPF não são alteráveis
export function atualizarGerente(cpf: string, payload: AtualizarGerentePayload) {
  return http.put<GerenteDTO>(`/gerentes/${cpf}`, payload).then((r) => r.data)
}

// R15 — SAGA; responde 202 { jobId }
export function removerGerente(cpf: string) {
  return http.delete<{ jobId?: string }>(`/gerentes/${cpf}`)
}
