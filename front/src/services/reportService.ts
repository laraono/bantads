import { http } from '@/services/http'
import { aguardarJob, getJobResult } from '@/services/jobService'

export interface ClienteRelatorioDTO {
  cpf: string
  nome: string
  email: string
  salario: string | number | null
  numeroConta: string
  saldo: string | number | null
  cpfGerente: string
  nomeGerente: string | null
}

// R16 — job assíncrono com resultType inline: 202 { jobId } -> polling -> /jobs/{id}/result
export async function gerarRelatorioClientes(): Promise<ClienteRelatorioDTO[]> {
  const response = await http.get<{ jobId?: string; clientes?: ClienteRelatorioDTO[] }>('/relatorios/clientes')

  if (response.status !== 202 || !response.data.jobId) {
    return response.data.clientes ?? []
  }

  await aguardarJob(response.data.jobId)
  const resultado = await getJobResult<{ clientes: ClienteRelatorioDTO[] }>(response.data.jobId)
  return resultado?.clientes ?? []
}
