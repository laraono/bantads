import { http } from '@/services/http'

export interface ClienteDTO {
  cpf: string
  nome: string
  email: string
  telefone: string
  salario: string | null
  cidade: string | null
  uf: string | null
  numeroConta: string | null
  saldo: string | null
}

// R11 — API Composition no gateway (MS Cliente + MS Conta), já ordenada por nome
export function listarClientes() {
  return http.get<{ clientes: ClienteDTO[] }>('/clientes').then((r) => r.data.clientes)
}
