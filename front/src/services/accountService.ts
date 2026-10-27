import { http } from '@/services/http'

export interface ContaDTO {
  numero: string
  cpfCliente: string
  saldo: string
}

export type TipoMovimentacao = 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER'

export interface MovimentacaoDTO {
  historyId: number
  accountNumber: string
  type: TipoMovimentacao
  originClientCPF: string
  originClientName: string | null
  destinationClientCPF: string | null
  destinationClientName: string | null
  amount: string
  createdAt: string
  managerCPF: string | null
  managerName: string | null
}

export interface ExtratoDTO {
  saldoAbertura: string
  movimentacoes: MovimentacaoDTO[]
}

export async function getContaPorCpf(cpf: string) {
  return http.get<ContaDTO>(`/contas/cpf/${cpf}`).then((r) => r.data)
}

export async function getConta(numero: string) {
  return http.get<ContaDTO>(`/contas/${numero}`).then((r) => r.data)
}

export async function depositar(numero: string, valor: string) {
  return http.post(`/contas/${numero}/deposito`, { valor })
}

export async function sacar(numero: string, valor: string) {
  return http.post(`/contas/${numero}/saque`, { valor })
}

export async function transferir(numero: string, contaDestino: string, valor: string) {
  return http.post(`/contas/${numero}/transferencia`, { contaDestino, valor })
}

export async function getExtrato(numero: string, inicio?: string, fim?: string) {
  return http
    .get<ExtratoDTO>(`/contas/${numero}/extrato`, { params: { inicio, fim } })
    .then((r) => r.data)
}
