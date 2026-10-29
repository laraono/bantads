import { isAxiosError, type AxiosResponse } from 'axios'
import { http } from '@/services/http'

export type JobStatus = 'PENDENTE' | 'CONCLUIDO' | 'SUCESSO' | 'FALHA'

export interface Job {
  jobId: string
  status: JobStatus
  resultType: 'resource' | 'inline' | null
  dominio?: string | null
  resourceId?: string | null
  erro?: string | null
}

export class JobFalhouError extends Error {}
export class JobTimeoutError extends Error {}
export class JobNaoEncontradoError extends Error {}

const INTERVALO_MS = 1000
const TEMPO_MAXIMO_MS = 60_000

const esperar = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

export function getJobStatus(jobId: string) {
  return http.get<Job>(`/jobs/${jobId}/status`).then((r) => r.data)
}

export function getJobResult<T>(jobId: string) {
  return http.get<T>(`/jobs/${jobId}/result`).then((r) => r.data)
}

// Consulta /jobs/{id}/status até o job sair de PENDENTE. O orquestrador grava
// "SUCESSO" ao final da SAGA; o enunciado usa "CONCLUIDO" — os dois valem.
export async function aguardarJob(jobId: string): Promise<Job> {
  const limite = Date.now() + TEMPO_MAXIMO_MS
  while (Date.now() < limite) {
    let job: Job
    try {
      job = await getJobStatus(jobId)
    } catch (err) {
      // se o job não existe/expirou não continua o polling
      if (isAxiosError(err) && err.response?.status === 404) {
        throw new JobNaoEncontradoError('Não foi possível acompanhar a operação (job não encontrado). Atualize a página para ver o resultado.')
      }
      throw err
    }
    if (job.status === 'CONCLUIDO' || job.status === 'SUCESSO') return job
    if (job.status === 'FALHA') throw new JobFalhouError(job.erro || 'A operação falhou.')
    await esperar(INTERVALO_MS)
  }
  throw new JobTimeoutError('A operação ainda está em processamento.')
}

// Operações assíncronas podem responder 202 (com jobId) ou já o status normal;
// só faz polling no 202.
export async function resolverAssincrono(response: AxiosResponse<{ jobId?: string }>): Promise<Job | null> {
  if (response.status !== 202 || !response.data?.jobId) return null
  return aguardarJob(response.data.jobId)
}
