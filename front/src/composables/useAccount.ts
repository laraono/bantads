// src/composables/useAccount.ts
import { reactive, ref, computed, watch } from 'vue'
import { DateTime } from 'luxon'
import { eventService, accountService, accountHistoryService } from '@/services'
import { session } from '@/store/session'
import type { AccountHistory } from '@/models/AccountHistory'

export type OperationType = 'Depósito' | 'Transferência' | 'Saque'

export interface AccountTransaction {
  id: number
  date: string
  time: string
  operation: OperationType
  party: string
  amount: number
}

// ---- module-level singleton state ----
const transactions = reactive<AccountTransaction[]>([])
const balance = ref(0)
const loading = ref(false)

function mapOperation(type: string): OperationType {
  if (type === 'deposito') return 'Depósito'
  if (type === 'saque') return 'Saque'
  return 'Transferência'
}

function resolveParty(h: AccountHistory, userCPF: string): string {
  if (h.type === 'deposito') return h.originClientName || 'Depósito em conta'
  if (h.type === 'saque') return h.originClientName || 'Saque em caixa'
  return h.originClientCpf === userCPF
    ? (h.destinationClientName ?? 'Transferência enviada')
    : (h.originClientName ?? 'Transferência recebida')
}

export function refreshAccount() {
  if (!session.accountNumber || !session.userCPF) {
    transactions.splice(0, transactions.length)
    balance.value = 0
    return
  }

  loading.value = true
  try {
    balance.value = accountService.getBalance(session.accountNumber)

    const extract = accountHistoryService.getExtract(
      session.accountNumber,
      null,
      null,
      session.userCPF
    )

    transactions.splice(0, transactions.length)
    for (const h of extract.movimentacoes) {
      const dt = DateTime.fromJSDate(new Date(h.createdAt))
      transactions.push({
        id: h.id,
        date: dt.toFormat('dd/MM/yyyy'),
        time: dt.toFormat('HH:mm'),
        operation: mapOperation(h.type),
        party: resolveParty(h, session.userCPF),
        amount: h.amount
      })
    }
  } finally {
    loading.value = false
  }
}

// React to login/logout/account switch
watch(() => session.accountNumber, refreshAccount, { immediate: true })

// ---- public API ----
export function useAccount() {
  function deposit(amount: number) {
    eventService.deposit(
      session.userCPF,
      { valor: amount.toFixed(2), nome: session.userName },
      session.accountNumber
    )
    refreshAccount()
  }

  function withdraw(amount: number) {
    eventService.withdraw(
      session.userCPF,
      { valor: amount.toFixed(2), nome: session.userName },
      session.accountNumber
    )
    refreshAccount()
  }

  function transfer(destinationAccount: string, amount: number) {
    const destData = accountService.getAccountData(destinationAccount)
    eventService.transfer(session.userCPF, session.accountNumber, {
      destino: { conta: destinationAccount, cpf: destData.cpfCliente, nome: destData.cpfCliente },
      origem: { nome: session.userName, valor: amount.toFixed(2), cpf: session.userCPF },
    })
    refreshAccount()
  }

  return {
    accountNumber: computed(() => `•••• ${session.accountNumber.slice(-4)}`),
    transactions,
    balance: computed(() => balance.value),
    loading,
    deposit,
    withdraw,
    transfer,
    refresh: refreshAccount,
  }
}