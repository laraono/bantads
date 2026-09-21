export interface AccountData {
    id: number
    clientCpf: string
    accountNumber: string
    createdAt: Date
    managerCpf: string
    balance: number
    deletedAt: Date | null
}

