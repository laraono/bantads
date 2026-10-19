import { isAxiosError } from 'axios'
import { DateTime } from 'luxon'

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

// valores monetários chegam do back como string (seção 5.1)
export function formatMoeda(valor: string | number | null | undefined): string {
  if (valor === null || valor === undefined || valor === '') return '—'
  const numero = typeof valor === 'number' ? valor : Number(valor)
  return Number.isNaN(numero) ? '—' : moeda.format(numero)
}

export function isNegativo(valor: string | number | null | undefined): boolean {
  return valor !== null && valor !== undefined && Number(valor) < 0
}

export function somenteDigitos(valor: string | null | undefined): string {
  return (valor ?? '').replace(/\D/g, '')
}

export function formatCpf(cpf: string | null | undefined): string {
  const d = somenteDigitos(cpf)
  if (d.length !== 11) return cpf ?? ''
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`
}

export function formatTelefone(telefone: string | null | undefined): string {
  const d = somenteDigitos(telefone)
  if (d.length === 11) return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
  if (d.length === 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`
  return telefone ?? ''
}

export function formatDataHora(iso: string | null | undefined): string {
  if (!iso) return ''
  const dt = DateTime.fromISO(iso)
  return dt.isValid ? dt.toLocal().toFormat("dd/MM/yyyy', 'HH:mm") : ''
}

export function iniciais(nome: string | null | undefined): string {
  const partes = (nome ?? '').trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) return '?'
  const primeira = partes[0]!.charAt(0)
  const ultima = partes.length > 1 ? partes[partes.length - 1]!.charAt(0) : ''
  return (primeira + ultima).toUpperCase()
}

// extrai a mensagem de erro devolvida pelo API Gateway ({ error } ou { message })
export function mensagemErro(err: unknown, padrao: string): string {
  if (isAxiosError(err)) {
    const data = err.response?.data as { error?: string; message?: string } | undefined
    return data?.error || data?.message || padrao
  }
  return (err instanceof Error && err.message) || padrao
}
