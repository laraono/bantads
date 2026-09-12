<script setup lang="ts">
import { computed, ref } from 'vue'
import { DateTime } from 'luxon'
import Sidebar from '../components/Sidebar.vue'

interface Transaction {
  id: number
  date: string
  time: string
  operation: 'Depósito' | 'Transferência' | 'Saque'
  party: string
  amount: number
}

const user = {
  fullName: 'John Doe',
  email: 'john@example.com',
  initials: 'AD'
}

const DEFAULT_RANGE_DAYS = 30
const MAX_RANGE_DAYS = 365
const OPENING_BALANCE = 8000

const today = DateTime.now().startOf('day')

function daysAgo(days: number, hour: number, minute: number): { date: string; time: string } {
  const dt = today.minus({ days }).set({ hour, minute })
  return { date: dt.toFormat('dd/MM/yyyy'), time: dt.toFormat('HH:mm') }
}

const transactions: Transaction[] = [
  { id: 1, ...daysAgo(12, 8, 12), operation: 'Depósito', party: 'Ana Silva', amount: 2500.75 },
  { id: 2, ...daysAgo(12, 9, 47), operation: 'Transferência', party: 'Helena Rocha', amount: 450.0 },
  { id: 3, ...daysAgo(12, 10, 5), operation: 'Transferência', party: 'Juliana Ferreira', amount: 1200.0 },
  { id: 4, ...daysAgo(10, 10, 33), operation: 'Saque', party: 'Fernanda Lima', amount: -800.0 },
  { id: 5, ...daysAgo(10, 11, 2), operation: 'Depósito', party: 'Carlos Mendes', amount: 300.0 },
  { id: 6, ...daysAgo(10, 11, 18), operation: 'Transferência', party: 'Carlos Mendes', amount: -150.0 },
  { id: 7, ...daysAgo(8, 12, 40), operation: 'Transferência', party: 'Helena Rocha', amount: 340.2 },
  { id: 8, ...daysAgo(8, 13, 15), operation: 'Transferência', party: 'Juliana Ferreira', amount: 0.0 },
  { id: 9, ...daysAgo(8, 13, 52), operation: 'Transferência', party: 'Ana Silva', amount: 780.5 },
  { id: 10, ...daysAgo(5, 14, 9), operation: 'Transferência', party: 'Ana Silva', amount: 150.0 },
  { id: 11, ...daysAgo(5, 14, 47), operation: 'Transferência', party: 'Carlos Mendes', amount: 900.0 },
  { id: 12, ...daysAgo(5, 15, 21), operation: 'Transferência', party: 'Beatriz Santos', amount: -250.5 },
  { id: 13, ...daysAgo(3, 15, 58), operation: 'Transferência', party: 'Beatriz Santos', amount: 450.0 },
  { id: 14, ...daysAgo(3, 16, 14), operation: 'Transferência', party: 'Fernanda Lima', amount: 690.0 },
  { id: 15, ...daysAgo(3, 16, 39), operation: 'Transferência', party: 'Helena Rocha', amount: 120.0 },
  { id: 16, ...daysAgo(1, 17, 3), operation: 'Transferência', party: 'Juliana Ferreira', amount: 1800.0 },
  { id: 17, ...daysAgo(1, 17, 26), operation: 'Transferência', party: 'Carlos Mendes', amount: -350.0 },
  { id: 18, ...daysAgo(0, 8, 2), operation: 'Transferência', party: 'Ana Silva', amount: 1200.0 },
  { id: 19, ...daysAgo(0, 9, 47), operation: 'Transferência', party: 'Beatriz Santos', amount: 50.0 },
  { id: 20, ...daysAgo(0, 10, 30), operation: 'Transferência', party: 'Fernanda Lima', amount: -300.0 }
]

function parseTxDateTime(tx: Transaction): DateTime {
  return DateTime.fromFormat(`${tx.date} ${tx.time}`, 'dd/MM/yyyy HH:mm')
}

// Running balance computed chronologically over the whole history, independent of the active filter.
const runningBalanceById = (() => {
  const chronological = [...transactions].sort((a, b) => parseTxDateTime(a).toMillis() - parseTxDateTime(b).toMillis())
  const map = new Map<number, number>()
  let running = OPENING_BALANCE
  for (const tx of chronological) {
    running += tx.amount
    map.set(tx.id, running)
  }
  return map
})()

const startDate = ref(today.minus({ days: DEFAULT_RANGE_DAYS }).toISODate() ?? '')
const endDate = ref(today.toISODate() ?? '')
const appliedStart = ref(startDate.value)
const appliedEnd = ref(endDate.value)
const rangeError = ref('')

function toIsoDate(brDate: string): string {
  const [day, month, year] = brDate.split('/')
  return `${year}-${month}-${day}`
}

const filteredTransactions = computed(() => {
  return transactions
    .filter((tx) => {
      const iso = toIsoDate(tx.date)
      if (appliedStart.value && iso < appliedStart.value) return false
      if (appliedEnd.value && iso > appliedEnd.value) return false
      return true
    })
    .sort((a, b) => parseTxDateTime(b).toMillis() - parseTxDateTime(a).toMillis())
})

// Consolidated (running) balance per day via Luxon, most recent day first.
const groupedByDay = computed(() => {
  const groups = new Map<string, { label: string; sortKey: number; items: Transaction[] }>()

  for (const tx of filteredTransactions.value) {
    const dt = DateTime.fromFormat(tx.date, 'dd/MM/yyyy')
    const key = dt.toISODate() ?? tx.date
    if (!groups.has(key)) {
      groups.set(key, { label: dt.setLocale('pt-BR').toFormat("dd 'de' LLLL"), sortKey: dt.toMillis(), items: [] })
    }
    groups.get(key)!.items.push(tx)
  }

  return Array.from(groups.entries())
    .map(([key, group]) => {
      // group.items[0] is the day's latest transaction (filteredTransactions is sorted most-recent-first),
      // so its running balance is the correct end-of-day consolidated balance.
      const consolidated = runningBalanceById.get(group.items[0]?.id ?? -1) ?? 0
      return { key, ...group, consolidatedBalance: consolidated }
    })
    .sort((a, b) => b.sortKey - a.sortKey)
})

function handleSearch() {
  rangeError.value = ''

  if (!startDate.value || !endDate.value) {
    appliedStart.value = startDate.value
    appliedEnd.value = endDate.value
    return
  }

  const start = DateTime.fromISO(startDate.value)
  const end = DateTime.fromISO(endDate.value)

  if (end < start) {
    rangeError.value = 'A data de fim não pode ser antes da data de início.'
    return
  }

  const rangeDays = end.diff(start, 'days').days
  if (rangeDays > MAX_RANGE_DAYS) {
    rangeError.value = `O intervalo máximo permitido é de ${MAX_RANGE_DAYS} dias.`
    return
  }

  appliedStart.value = startDate.value
  appliedEnd.value = endDate.value
}

function openPicker(event: MouseEvent) {
  const input = event.currentTarget as HTMLInputElement & { showPicker?: () => void }
  input.showPicker?.()
}

function formatCurrency(value: number): string {
  const sign = value < 0 ? '-' : ''
  return `${sign}R$ ${Math.abs(value).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
</script>

<template>
  <div class="layout">
    <Sidebar active="extrato" :user="user" />

    <main class="content">
      <h1 class="title">Extrato Detalhado</h1>

      <div class="filters">
        <label class="field">
          <span class="field-label">Data de Início</span>
          <input v-model="startDate" type="date" @click="openPicker" />
        </label>
        <label class="field">
          <span class="field-label">Data de Fim</span>
          <input v-model="endDate" type="date" @click="openPicker" />
        </label>
        <button class="search-btn" @click="handleSearch">Pesquisar</button>
      </div>

      <span v-if="rangeError" class="range-error">{{ rangeError }}</span>

      <div class="table-card">
        <table class="table">
          <thead>
            <tr>
              <th>Data/Hora</th>
              <th>Operação</th>
              <th>Origem/Destino</th>
              <th class="align-right">Valor</th>
            </tr>
          </thead>
          <tbody v-for="group in groupedByDay" :key="group.key">
            <tr class="day-row">
              <td colspan="3" class="day-label">{{ group.label }}</td>
              <td class="align-right day-balance">
                Saldo do dia: {{ formatCurrency(group.consolidatedBalance) }}
              </td>
            </tr>
            <tr v-for="tx in group.items" :key="tx.id">
              <td class="cell-date">
                <span class="cell-date-day">{{ tx.date }}</span>
                <span class="cell-date-time">{{ tx.time }}</span>
              </td>
              <td class="cell-operation">{{ tx.operation }}</td>
              <td>
                <span class="party-pill">{{ tx.party }}</span>
              </td>
              <td class="align-right" :class="tx.amount < 0 ? 'negative' : 'positive'">
                {{ formatCurrency(tx.amount) }}
              </td>
            </tr>
          </tbody>
        </table>

        <div class="table-footer">
          <span class="footer-count">Showing {{ filteredTransactions.length }} clients</span>
          <div class="pagination">
            <button class="page-btn" disabled>
              <svg viewBox="0 0 24 24" class="page-icon"><path d="m15 18-6-6 6-6" /></svg>
              Previous
            </button>
            <button class="page-btn">
              Next
              <svg viewBox="0 0 24 24" class="page-icon"><path d="m9 18 6-6-6-6" /></svg>
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.layout {
  display: flex;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background: #f3f4f6;
}

.content {
  flex: 1;
  min-width: 0;
  padding: 28px 32px 48px;
  max-width: 1040px;
  margin: 0 auto;
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}

.filters {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 24px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
}

.field input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 9px 12px;
  font-size: 13px;
  color: #1f2937;
  background: #fff;
  outline: none;
  font-family: inherit;
}

.field input:focus {
  border-color: #f0c968;
}

.range-error {
  display: block;
  color: #dc2626;
  font-size: 13px;
  margin: -12px 0 16px;
}

.search-btn {
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 8px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  cursor: pointer;
  transition: background 0.15s;
}

.search-btn:hover {
  background: #f3f4f6;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.table {
  width: 100%;
  table-layout: fixed;
  border-collapse: collapse;
}

.table th,
.table td {
  width: 25%;
}

.table thead th {
  text-align: left;
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: none;
  padding: 14px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.table tbody td {
  padding: 14px 24px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.table tbody td.cell-date {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: visible;
  text-overflow: clip;
  white-space: normal;
}

.day-row {
  background: #f8f9fb;
}

.table tbody td.day-label,
.table tbody td.day-balance {
  padding: 10px 24px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 12px;
  font-weight: 700;
  color: #000;
}

.day-label {
  text-transform: capitalize;
}

.cell-date-day {
  color: #374151;
  font-size: 13px;
}

.cell-date-time {
  color: #9ca3af;
  font-size: 11px;
}

.cell-operation {
  color: #6b7280;
}

.party-pill {
  display: inline-block;
  background: #f3f4f6;
  color: #374151;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
}

.table th.align-right,
.table td.align-right {
  text-align: right;
}

.table tbody td.positive {
  color: #2563eb;
  font-weight: 500;
}

.table tbody td.negative {
  color: #dc2626;
  font-weight: 500;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
}

.footer-count {
  font-size: 13px;
  color: #6b7280;
}

.pagination {
  display: flex;
  gap: 8px;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: #fff;
  color: #374151;
  transition: background 0.15s, border-color 0.15s;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fb;
  border-color: #d1d5db;
}

.page-btn:disabled {
  cursor: default;
  color: #c1c5cc;
  border-color: #f1f5f9;
}

.page-icon {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
