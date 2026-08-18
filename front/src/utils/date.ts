import { DateTime } from 'luxon'

export function formatDate(value: string | Date): string {
  const dt = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value)
  return dt.setLocale('pt-BR').toFormat('dd/MM/yyyy')
}

export function formatDateTime(value: string | Date): string {
  const dt = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value)
  return dt.setLocale('pt-BR').toFormat('dd/MM/yyyy HH:mm')
}

export function now(): DateTime {
  return DateTime.now()
}
