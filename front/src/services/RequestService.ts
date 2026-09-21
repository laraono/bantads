import type { Request } from '@/models/Request'
import { MOCK_USERS } from './authService'
import type { Client } from '@/models/Client'
import { requests } from './seed'

const LS_KEY = 'requests'

export interface CreateRequestInput {
  name: string
  cpf: string
  email: string
  cep: string
  street: string
  number: string
  complement?: string
  city: string
  state: string
  salary: number
}

export class RequestService {
    listAll(): Request[] {
        const raw = localStorage.getItem(LS_KEY)
        if (!raw) {
        localStorage.setItem(LS_KEY, JSON.stringify(requests))
        return [...requests]
        }
        return JSON.parse(raw)
    }

    insert(request: Request): Request {
        const all = this.listAll()
        if (!request.id || request.id === 0) {
            request.id = all.length > 0 ? Math.max(...all.map(r => r.id)) + 1 : 1
        }
        all.push(request)
        localStorage.setItem(LS_KEY, JSON.stringify(all))
        return request
    }

    findByEmail(email: string): Request | undefined {
        return this.listAll().find(r => r.client.email === email)
    }

    findByCpf(cpf: string): Request | undefined {
        return this.listAll().find(r => r.client.cpf === cpf)
    }

    findById(id: number): Request | undefined {
        return this.listAll().find(r => r.id === id)
    }

    createFromForm(input: CreateRequestInput): Request {
        if (this.findByCpf(input.cpf)) {
            throw new Error('Já existe uma solicitação para este CPF')
        }
        if (this.findByEmail(input.email)) {
            throw new Error('Já existe uma solicitação para este e-mail')
        }

        const now = new Date()

        const client: Client = {
            id: 0,
            name: input.name,
            cpf: input.cpf,
            email: input.email,
            salary: input.salary,
            address: {
                street: input.street,
                number: Number(input.number),
                city: input.city,
                state: input.state,
                cep: input.cep,
            },
            deleted: false
        }

        const request: Request = {
        id: 0,
        client,
        status: 'pendente'
        }

        return this.insert(request)
    }

    update(request: Request): void {
        const all = this.listAll()
        const index = all.findIndex(r => r.id === request.id)
        if (index === -1) throw new Error(`Solicitação ${request.id} não encontrada`)

        all[index] = {
        ...all[index],
        ...request,
        approvedAt: new Date()
        }

        if (request.status === 'aprovado' && request.client.email) {
        MOCK_USERS[request.client.email] = { password: 'tads', role: 'cliente' }
        }

        localStorage.setItem(LS_KEY, JSON.stringify(all))
    }
}