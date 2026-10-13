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
