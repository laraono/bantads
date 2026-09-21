<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { DateTime } from 'luxon'
import Sidebar from '../components/Sidebar.vue'
import { useAccount, type AccountTransaction } from '../composables/useAccount'
import { session } from '@/store/session.ts'
import { accountHistoryService } from '@/services/index.ts'
import type { AccountHistory } from '@/models/AccountHistory'

const DEFAULT_RANGE_DAYS = 30
const MAX_RANGE_DAYS = 365

const user = computed(() => ({
  fullName: session.userName,
  email: session.email,
  initials: session.userName.split(' ')
    .map(n => n[0]).slice(0, 2).join('').toUpperCase() || 'AD',
}))

const today = DateTime.now().startOf('day')

function parseTxDateTime(tx: AccountTransaction): DateTime {
  return DateTime.fromFormat(`${tx.date} ${tx.time}`, 'dd/MM/yyyy HH:mm')
}

function toIsoDate(brDate: string): string {
  const [day, month, year] = brDate.split('/')
  return `${year}-${month}-${day}`
}

function mapHistoryToTx(h: AccountHistory, userCPF: string): AccountTransaction {
  const dt = DateTime.fromJSDate(new Date(h.createdAt))
  const amount = Number(h.amount)
  const operation =
    h.type === 'deposito' ? 'Depósito' :
    h.type === 'saque'    ? 'Saque'    : 'Transferência'

  const party =
    h.type === 'deposito' ? (h.originClientName || 'Depósito em conta') :
    h.type === 'saque'    ? (h.originClientName || 'Saque em caixa') :
    h.originClientCpf === userCPF
      ? (h.destinationClientName ?? 'Transferência enviada')
      : (h.originClientName ?? 'Transferência recebida')

  return {
    id: h.id,
    date: dt.toFormat('dd/MM/yyyy'),
    time: dt.toFormat('HH:mm'),
    operation,
    party,
    amount,
  }
}

const startDate = ref(today.minus({ days: DEFAULT_RANGE_DAYS }).toISODate() ?? '')
const endDate = ref(today.toISODate() ?? '')
const appliedStart = ref(startDate.value)
const appliedEnd = ref(endDate.value)
const rangeError = ref('')
const rangeTransactions = ref<AccountTransaction[]>([])

function loadRange() {
  if (!appliedStart.value || !appliedEnd.value) {
    rangeTransactions.value = []
    return
  }
  try {
    const extract = accountHistoryService.getExtract(
      session.accountNumber,
      appliedStart.value,
      appliedEnd.value,
      session.userCPF
    )
    rangeTransactions.value = extract.movimentacoes
      .map(h => mapHistoryToTx(h, session.userCPF))
      .sort((a, b) => parseTxDateTime(b).toMillis() - parseTxDateTime(a).toMillis())
  } catch (e) {
    rangeError.value = e instanceof Error ? e.message : 'Erro ao consultar extrato'
    rangeTransactions.value = []
  }
}

const groupedByDay = computed(() => {
  if (!appliedStart.value || !appliedEnd.value) return []

  const allHistory = accountHistoryService.findByAccountNumber(session.accountNumber)
  const deltaByDay = new Map<string, number>()
  for (const h of allHistory) {
    const day = DateTime.fromJSDate(new Date(h.createdAt)).toISODate()
    if (!day) continue
    const value = Number(h.amount)
    let delta = 0
    if (h.type === 'deposito') delta = value
    else if (h.type === 'saque') delta = -value
    else if (h.type === 'transferencia') {
      if (h.destinationClientCpf === session.userCPF) delta += value
      if (h.originClientCpf === session.userCPF) delta -= value
    }
    deltaByDay.set(day, (deltaByDay.get(day) ?? 0) + delta)
  }

  const beforeWindow = accountHistoryService.getInitialBalance(
    session.accountNumber,
    DateTime.fromISO(appliedStart.value).startOf('day').toJSDate(),
    session.userCPF
  )

  const byDayTx = new Map<string, AccountTransaction[]>()
  for (const tx of rangeTransactions.value) {
    const key = toIsoDate(tx.date)
    if (!byDayTx.has(key)) byDayTx.set(key, [])
    byDayTx.get(key)!.push(tx)
  }

  const days: {
    key: string; 
    label: string; 
    sortKey: number;
    items: AccountTransaction[]; 
    consolidatedBalance: number
  }[] = []

  let running = beforeWindow
  let cursor = DateTime.fromISO(appliedStart.value)
  const end = DateTime.fromISO(appliedEnd.value)

  while (cursor <= end) {
    const key = cursor.toISODate()!
    running += deltaByDay.get(key) ?? 0
    days.push({
      key,
      label: cursor.setLocale('pt-BR').toFormat("dd 'de' LLLL"),
      sortKey: cursor.toMillis(),
      items: byDayTx.get(key) ?? [],
      consolidatedBalance: running,
    })
    cursor = cursor.plus({ days: 1 })
  }

  return days.sort((a, b) => b.sortKey - a.sortKey)
})

function handleSearch() {
  rangeError.value = ''

  if (!startDate.value || !endDate.value) {
    rangeError.value = 'Informe a data de início e a data de fim.'
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

// Load initially and whenever the applied range changes
watch([appliedStart, appliedEnd], loadRange, { immediate: true })

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
            <tr v-if="group.items.length === 0" class="empty-row">
              <td colspan="4">Nenhuma movimentação</td>
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
          <span class="footer-count">Showing {{ rangeTransactions.length }} clients</span>
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

.empty-row td {
  color: #9ca3af;
  font-style: italic;
  white-space: nowrap;
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
