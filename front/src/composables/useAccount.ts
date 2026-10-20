import { computed, ref } from 'vue'
import { DateTime } from 'luxon'
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
  amount: number // positivo = entrada, negativo = saída
}

const OPERATION_LABEL: Record<MovimentacaoDTO['type'], OperationType> = {
  DEPOSIT: 'Depósito',
  WITHDRAW: 'Saque',
  TRANSFER: 'Transferência',
}

const accountNumber = ref('')
const balance = ref(0)
const loaded = ref(false)

function parseValor(v: string): number {
  return parseFloat(v)
}

function toTransaction(mov: MovimentacaoDTO, cpfProprio: string): AccountTransaction {
  // o backend grava createdAt em UTC (container sem TZ) sem indicar o fuso na
  // string; sem isso o Luxon assumiria que já está no fuso local do navegador
  const dt = DateTime.fromISO(mov.createdAt, { zone: 'utc' }).toLocal()
  const abs = Math.abs(parseValor(mov.amount))

  let amount = abs
  let party = ''

  if (mov.type === 'DEPOSIT') {
    amount = abs
    party = 'Depósito em conta'
  } else if (mov.type === 'WITHDRAW') {
    amount = -abs
    party = 'Saque em conta'
  } else {
    // TRANSFER: o lado do extrato (origem/destino) indica se é entrada ou saída pra essa conta
    const souOrigem = mov.originClientCPF === cpfProprio
    amount = souOrigem ? -abs : abs
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
    if (balance.value !== before) return
    await new Promise((resolve) => setTimeout(resolve, 400))
  }
}

async function deposit(amount: number) {
  await depositar(accountNumber.value, amount.toFixed(2))
  await refreshBalance()
}

async function withdraw(amount: number) {
  await sacar(accountNumber.value, amount.toFixed(2))
  await refreshBalance()
}

async function transfer(destinationAccount: string, amount: number) {
  await transferir(accountNumber.value, destinationAccount, amount.toFixed(2))
  await refreshBalance()
}

interface Extrato {
  saldoAbertura: number
  transactions: AccountTransaction[]
}

async function fetchExtrato(inicio?: string, fim?: string): Promise<Extrato> {
  const { usuario } = useAuth()
  if (!accountNumber.value || !usuario.value) return { saldoAbertura: 0, transactions: [] }
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
