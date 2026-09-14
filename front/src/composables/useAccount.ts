import { computed, reactive } from 'vue'
import { DateTime } from 'luxon'

export type OperationType = 'Depósito' | 'Transferência' | 'Saque'

export interface AccountTransaction {
  id: number
  date: string // dd/MM/yyyy
  time: string // HH:mm
  operation: OperationType
  party: string
  amount: number // positivo = entrada, negativo = saída
}

const ACCOUNT_NUMBER = '•••• 4829'
export const OPENING_BALANCE = 8000

const today = DateTime.now().startOf('day')

function daysAgo(days: number, hour: number, minute: number): { date: string; time: string } {
  const dt = today.minus({ days }).set({ hour, minute })
  return { date: dt.toFormat('dd/MM/yyyy'), time: dt.toFormat('HH:mm') }
}

// Histórico mockado (fará o papel do MS Conta enquanto o back-end não está integrado).
const transactions = reactive<AccountTransaction[]>([
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
])

let nextId = transactions.length + 1

const balance = computed(() => transactions.reduce((sum, tx) => sum + tx.amount, OPENING_BALANCE))

function registerTransaction(operation: OperationType, party: string, amount: number) {
  const now = DateTime.now()
  transactions.unshift({
    id: nextId++,
    date: now.toFormat('dd/MM/yyyy'),
    time: now.toFormat('HH:mm'),
    operation,
    party,
    amount
  })
}

function deposit(amount: number) {
  registerTransaction('Depósito', 'Depósito em conta', amount)
}

function withdraw(amount: number) {
  if (amount > balance.value) throw new Error('Saldo insuficiente')
  registerTransaction('Saque', 'Saque em caixa eletrônico', -amount)
}

function transfer(destinationAccount: string, amount: number) {
  if (amount > balance.value) throw new Error('Saldo insuficiente')
  registerTransaction('Transferência', `Conta ${destinationAccount}`, -amount)
}

export function useAccount() {
  return {
    accountNumber: ACCOUNT_NUMBER,
    transactions,
    balance,
    deposit,
    withdraw,
    transfer
  }
}
