export type HistoryType = 'deposito' | 'saque' | 'transferencia';

export interface AccountHistory {
    id: number
    accountNumber: string
    type: HistoryType
    originClientCpf: string
    originClientName: string
    destinationClientCpf: string | null
    destinationClientName: string | null
    amount: number
    createdAt: Date
}