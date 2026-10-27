import type { AxiosResponse } from 'axios'
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
    const job = await getJobStatus(jobId)
    if (job.status === 'CONCLUIDO' || job.status === 'SUCESSO') return job
    if (job.status === 'FALHA') throw new JobFalhouError(job.erro || 'A operação falhou.')
    await esperar(INTERVALO_MS)
  }
  throw new JobTimeoutError('A operação ainda está em processamento.')
}

// Seção 5.8: "resource" -> GET /{dominio}/{resourceId}; "inline" -> GET /jobs/{id}/result.
// A SAGA já terminou com sucesso aqui, então uma falha ao buscar o dado não
// deve transformar a operação em erro na tela.
async function buscarResultado<T>(jobId: string, job: Job): Promise<T | null> {
  try {
    if (job.resultType === 'resource' && job.dominio && job.resourceId) {
      return await http.get<T>(`/${job.dominio}/${job.resourceId}`).then((r) => r.data)
    }
    if (job.resultType === 'inline') {
      return await getJobResult<T>(jobId)
    }
  } catch {
    // ignora: o status do job é o que importa para o usuário
  }
  return null
}

export interface JobResolvido<T> {
  job: Job
  resultado: T | null
}

// Operações assíncronas podem responder 202 (com jobId) ou já o status normal;
// só faz polling no 202.
export async function resolverAssincrono<T = unknown>(
  response: AxiosResponse<{ jobId?: string }>,
): Promise<JobResolvido<T> | null> {
  const jobId = response.status === 202 ? response.data?.jobId : undefined
  if (!jobId) return null
  const job = await aguardarJob(jobId)
  return { job, resultado: await buscarResultado<T>(jobId, job) }
}
