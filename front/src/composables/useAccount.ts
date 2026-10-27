import { computed, ref, shallowRef } from 'vue'
import { DateTime } from 'luxon'
import Decimal from 'decimal.js'
import { useAuth } from '@/composables/useAuth'
import {
  getContaPorCpf,
  getConta,
  getExtrato,
  depositar,
  sacar,
  transferir,
  type MovimentacaoDTO,
} from '@/services/accountService'

export type OperationType = 'Depósito' | 'Transferência' | 'Saque'

export interface AccountTransaction {
  id: number
  date: string // dd/MM/yyyy
  time: string // HH:mm
  operation: OperationType
  party: string
  amount: Decimal // positivo = entrada, negativo = saída
}

const OPERATION_LABEL: Record<MovimentacaoDTO['type'], OperationType> = {
  DEPOSIT: 'Depósito',
  WITHDRAW: 'Saque',
  TRANSFER: 'Transferência',
}

const accountNumber = ref('')
const balance = shallowRef<Decimal>(new Decimal(0))
const loaded = ref(false)

function parseValor(v: string): Decimal {
  return new Decimal(v)
}

function toTransaction(mov: MovimentacaoDTO, cpfProprio: string): AccountTransaction {
  // o backend grava createdAt em UTC (container sem TZ) sem indicar o fuso na
  // string; sem isso o Luxon assumiria que já está no fuso local do navegador
  const dt = DateTime.fromISO(mov.createdAt, { zone: 'utc' }).toLocal()
  const abs = parseValor(mov.amount).abs()

  let amount = abs
  let party = ''

  if (mov.type === 'DEPOSIT') {
    amount = abs
    party = 'Depósito em conta'
  } else if (mov.type === 'WITHDRAW') {
    amount = abs.negated()
    party = 'Saque em conta'
  } else {
    // TRANSFER: o lado do extrato (origem/destino) indica se é entrada ou saída pra essa conta
    const souOrigem = mov.originClientCPF === cpfProprio
    amount = souOrigem ? abs.negated() : abs
    party = souOrigem ? (mov.destinationClientName ?? 'Conta destino') : (mov.originClientName ?? 'Conta origem')
  }

  return {
    id: mov.historyId,
    date: dt.toFormat('dd/MM/yyyy'),
    time: dt.toFormat('HH:mm'),
    operation: OPERATION_LABEL[mov.type],
    party,
    amount,
  }
}

async function loadAccount() {
  const { usuario } = useAuth()
  if (!usuario.value) return
  const conta = await getContaPorCpf(usuario.value.cpf)
  accountNumber.value = conta.numero
  balance.value = parseValor(conta.saldo)
  loaded.value = true
}

// Reconsulta o saldo algumas vezes com pequenos intervalos: o lado de leitura
// (CQRS) é assíncrono e pode demorar alguns instantes para refletir a operação.
async function refreshBalance() {
  if (!accountNumber.value) return
  const before = balance.value
  for (let tentativa = 0; tentativa < 6; tentativa++) {
    const conta = await getConta(accountNumber.value)
    balance.value = parseValor(conta.saldo)
    if (!balance.value.equals(before)) return
    await new Promise((resolve) => setTimeout(resolve, 400))
  }
}

async function deposit(amount: Decimal) {
  await depositar(accountNumber.value, amount.toFixed(2))
  await refreshBalance()
}

async function withdraw(amount: Decimal) {
  await sacar(accountNumber.value, amount.toFixed(2))
  await refreshBalance()
}

async function transfer(destinationAccount: string, amount: Decimal) {
  await transferir(accountNumber.value, destinationAccount, amount.toFixed(2))
  await refreshBalance()
}

interface Extrato {
  saldoAbertura: Decimal
  transactions: AccountTransaction[]
}

async function fetchExtrato(inicio?: string, fim?: string): Promise<Extrato> {
  const { usuario } = useAuth()
  if (!accountNumber.value || !usuario.value) return { saldoAbertura: new Decimal(0), transactions: [] }
  const extrato = await getExtrato(accountNumber.value, inicio, fim)
  return {
    saldoAbertura: parseValor(extrato.saldoAbertura),
    transactions: extrato.movimentacoes.map((mov) => toTransaction(mov, usuario.value!.cpf)),
  }
}

export function useAccount() {
  return {
    accountNumber: computed(() => accountNumber.value),
    balance,
    loaded: computed(() => loaded.value),
    loadAccount,
    deposit,
    withdraw,
    transfer,
    fetchExtrato,
  }
}
