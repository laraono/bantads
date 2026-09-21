import type { Address } from "./Address"

export type Client = {
    id: number
    cpf: string
    name: string
    email: string
    address: Address
    salary: number
    deleted: boolean
}