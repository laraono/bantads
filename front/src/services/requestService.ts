import { http } from '@/services/http'

export interface SolicitacaoPayload {
  nome: string
  email: string
  cpf: string
  telefone: string
  salario: string
  endereco: {
    logradouro: string
    numero: string
    complemento?: string
    cep: string
    cidade: string
    uf: string
  }
}

export function criarSolicitacao(payload: SolicitacaoPayload) {
  return http.post('/solicitacoes', payload).then((r) => r.data)
}

export type StatusSolicitacao = 'PENDENTE' | 'APROVADA' | 'NAO_APROVADA'

export interface SolicitacaoDTO {
  cpf: string
  nome: string
  email: string
  salario: string | null
  status: StatusSolicitacao
  motivo: string | null
  dataDecisao: string | null
}

// R8
export function listarSolicitacoes() {
  return http.get<{ solicitacoes: SolicitacaoDTO[] }>('/solicitacoes').then((r) => r.data.solicitacoes)
}

// R9 — inicia a SAGA; responde 202 { jobId }
export function aprovarSolicitacao(cpf: string) {
  return http.post<{ jobId?: string }>(`/solicitacoes/${cpf}/aprovacao`)
}

// R10 — síncrona (200)
export function rejeitarSolicitacao(cpf: string, motivo: string) {
  return http.post<SolicitacaoDTO>(`/solicitacoes/${cpf}/rejeicao`, { motivo }).then((r) => r.data)
}
