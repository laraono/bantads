import type { Client } from "./Client"

export type RequestStatus = 'aprovado' | 'rejeitado' | 'pendente'

export type Request = {
    id: number
    client: Client
    status: RequestStatus
    rejectReason?: string
    approvedAt?: Date
}