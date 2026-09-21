import type { AccountHistory } from "@/models/AccountHistory";
import type { Client } from "@/models/Client";
import type { Event } from "@/models/Event";
import type { Manager } from "@/models/Manager";
import type { Request } from "@/models/Request";

export const accountHistory: AccountHistory[] = [
    {
        id: 1,
        accountNumber: '1291',
        type: 'deposito',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 1000.00,
        createdAt: new Date('2020-01-01 10:00:00')
    },
    {
        id: 2,
        accountNumber: '1291',
        type: 'deposito',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 900.00,
        createdAt: new Date('2020-01-01 11:00:00')
    },
    {
        id: 3,
        accountNumber: '1291',
        type: 'saque',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 550.00,
        createdAt: new Date('2020-01-01 12:00:00')
    },
    {
        id: 4,
        accountNumber: '1291',
        type: 'saque',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 350.00,
        createdAt: new Date('2020-01-01 13:00:00')
    },
    {
        id: 5,
        accountNumber: '1291',
        type: 'deposito',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 2000.00,
        createdAt: new Date('2020-01-10 15:00:00')
    },
    {
        id: 6,
        accountNumber: '1291',
        type: 'saque',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 500.00,
        createdAt: new Date('2020-01-15 08:00:00')
    },
    {
        id: 7,
        accountNumber: '1291',
        type: 'transferencia',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: '09506382000',
        destinationClientName: 'Cleuddônio',
        amount: 1700.00,
        createdAt: new Date('2020-01-20 12:00:00')
    },
    {
        id: 8,
        accountNumber: '0950',
        type: 'transferencia',
        originClientCpf: '12912861012',
        originClientName: 'Catharyna',
        destinationClientCpf: '09506382000',
        destinationClientName: 'Cleuddônio',
        amount: 1700.00,
        createdAt: new Date('2020-01-20 12:00:00')
    },
    {
        id: 9,
        accountNumber: '0950',
        type: 'deposito',
        originClientCpf: '09506382000',
        originClientName: 'Cleuddônio',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 1000.00,
        createdAt: new Date('2025-01-01 12:00:00')
    },
    {
        id: 10,
        accountNumber: '0950',
        type: 'deposito',
        originClientCpf: '09506382000',
        originClientName: 'Cleuddônio',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 5000.00,
        createdAt: new Date('2025-01-02 10:00:00')
    },
    {
        id: 11,
        accountNumber: '0950',
        type: 'saque',
        originClientCpf: '09506382000',
        originClientName: 'Cleuddônio',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 200.00,
        createdAt: new Date('2025-01-10 10:00:00')
    },
    {
        id: 12,
        accountNumber: '0950',
        type: 'deposito',
        originClientCpf: '09506382000',
        originClientName: 'Cleuddônio',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 7000.00,
        createdAt: new Date('2025-02-05 10:00:00')
    },
    {
        id: 13,
        accountNumber: '0950',
        type: 'saque',
        originClientCpf: '09506382000',
        originClientName: 'Cleuddônio',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 4500.00,
        createdAt: new Date('2025-03-06 11:00:00')
    },
    {
        id: 14,
        accountNumber: '8573',
        type: 'deposito',
        originClientCpf: '85733854057',
        originClientName: 'Catianna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 1000.00,
        createdAt: new Date('2025-05-05 10:00:00')
    },
    {
        id: 15,
        accountNumber: '8573',
        type: 'saque',
        originClientCpf: '85733854057',
        originClientName: 'Catianna',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 800.00,
        createdAt: new Date('2025-05-06 10:00:00')
    },
    {
        id: 16,
        accountNumber: '5887',
        type: 'deposito',
        originClientCpf: '58872160006',
        originClientName: 'Cutardo',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 150000.00,
        createdAt: new Date('2025-06-01 10:00:00')
    },
    {
        id: 17,
        accountNumber: '7617',
        type: 'deposito',
        originClientCpf: '76179646090',
        originClientName: 'Coândrya',
        destinationClientCpf: null,
        destinationClientName: null,
        amount: 1500.00,
        createdAt: new Date('2025-07-01 10:00:00')
    }
];

export const accountsData = [
    {
        id: 1,
        clientCpf: '12912861012',
        accountNumber: '1291',
        createdAt: new Date('2000-01-01 00:00:00'),
        managerCpf: '98574307084',
        balance: 800.00,
        deletedAt: null
    },
    {
        id: 2,
        clientCpf: '09506382000',
        accountNumber: '0950',
        createdAt: new Date('1990-10-10 00:00:00'),
        managerCpf: '64065268052',
        balance: 10000.00,
        deletedAt: null
    },
    {
        id: 3,
        clientCpf: '85733854057',
        accountNumber: '8573',
        createdAt: new Date('2012-12-12 00:00:00'),
        managerCpf: '23862179060',
        balance: 200.00,
        deletedAt: null
    },
    {
        id: 4,
        clientCpf: '58872160006',
        accountNumber: '5887',
        createdAt: new Date('2022-02-22 00:00:00'),
        managerCpf: '98574307084',
        balance: 150000.00,
        deletedAt: null
    },
    {
        id: 5,
        clientCpf: '76179646090',
        accountNumber: '7617',
        createdAt: new Date('2025-01-01 00:00:00'),
        managerCpf: '64065268052',
        balance: 1500.00,
        deletedAt: null
    }
];

export const events: Event[] = [
    {
        id: 1,
        objectId: '1291',
        version: 1,
        eventType: 'criado',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', cpfGerente: '98574307084' }),
        createdAt: new Date('2000-01-01 00:00:00')
    },
    {
        id: 2,
        objectId: '1291',
        version: 2,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', valor: '1000.00' }),
        createdAt: new Date('2020-01-01 10:00:00')
    },
    {
        id: 3,
        objectId: '1291',
        version: 3,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', valor: '900.00' }),
        createdAt: new Date('2020-01-01 11:00:00')
    },
    {
        id: 4,
        objectId: '1291',
        version: 4,
        eventType: 'saque',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', valor: '550.00' }),
        createdAt: new Date('2020-01-01 12:00:00')
    },
    {
        id: 5,
        objectId: '1291',
        version: 5,
        eventType: 'saque',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', valor: '350.00' }),
        createdAt: new Date('2020-01-01 13:00:00')
    },
    {
        id: 6,
        objectId: '1291',
        version: 6,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', valor: '2000.00' }),
        createdAt: new Date('2020-01-10 15:00:00')
    },
    {
        id: 7,
        objectId: '1291',
        version: 7,
        eventType: 'saque',
        payload: JSON.stringify({ nome: 'Catharyna', cpfCliente: '12912861012', valor: '500.00' }),
        createdAt: new Date('2020-01-15 08:00:00')
    },
    {
        id: 8,
        objectId: '1291',
        version: 8,
        eventType: 'transferencia_origem',
        payload: JSON.stringify({
            destino: { conta: '0950', nome: 'Cleuddônio', cpfCliente: '09506382000' },
            origem: { nome: 'Catharyna', valor: '1700.00' }
        }),
        createdAt: new Date('2020-01-20 12:00:00')
    },
    {
        id: 9,
        objectId: '0950',
        version: 1,
        eventType: 'criado',
        payload: JSON.stringify({ nome: 'Cleuddônio', cpfCliente: '09506382000', cpfGerente: '64065268052' }),
        createdAt: new Date('1990-10-10 00:00:00')
    },
    {
        id: 10,
        objectId: '0950',
        version: 2,
        eventType: 'transferencia_destino',
        payload: JSON.stringify({
            destino: { conta: '0950', nome: 'Cleuddônio', cpfCliente: '09506382000' },
            origem: { nome: 'Catharyna', valor: '1700.00' }
        }),
        createdAt: new Date('2020-01-20 12:00:00')
    },
    {
        id: 11,
        objectId: '0950',
        version: 3,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Cleuddônio', cpfCliente: '09506382000', valor: '1000.00' }),
        createdAt: new Date('2025-01-01 12:00:00')
    },
    {
        id: 12,
        objectId: '0950',
        version: 4,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Cleuddônio', cpfCliente: '09506382000', valor: '5000.00' }),
        createdAt: new Date('2025-01-02 10:00:00')
    },
    {
        id: 13,
        objectId: '0950',
        version: 5,
        eventType: 'saque',
        payload: JSON.stringify({ nome: 'Cleuddônio', cpfCliente: '09506382000', valor: '200.00' }),
        createdAt: new Date('2025-01-10 10:00:00')
    },
    {
        id: 14,
        objectId: '0950',
        version: 6,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Cleuddônio', cpfCliente: '09506382000', valor: '7000.00' }),
        createdAt: new Date('2025-02-05 10:00:00')
    },
    {
        id: 15,
        objectId: '0950',
        version: 7,
        eventType: 'saque',
        payload: JSON.stringify({ nome: 'Cleuddônio', cpfCliente: '09506382000', valor: '4500.00' }),
        createdAt: new Date('2025-03-06 11:00:00')
    },
    {
        id: 16,
        objectId: '8573',
        version: 1,
        eventType: 'criado',
        payload: JSON.stringify({ nome: 'Catianna', cpfCliente: '85733854057', cpfGerente: '23862179060' }),
        createdAt: new Date('2012-12-12 00:00:00')
    },
    {
        id: 17,
        objectId: '8573',
        version: 2,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Catianna', cpfCliente: '85733854057', valor: '1000.00' }),
        createdAt: new Date('2025-05-05 10:00:00')
    },
    {
        id: 18,
        objectId: '8573',
        version: 3,
        eventType: 'saque',
        payload: JSON.stringify({ nome: 'Catianna', cpfCliente: '85733854057', valor: '800.00' }),
        createdAt: new Date('2025-05-06 10:00:00')
    },
    {
        id: 19,
        objectId: '5887',
        version: 1,
        eventType: 'criado',
        payload: JSON.stringify({ nome: 'Cutardo', cpfCliente: '58872160006', cpfGerente: '98574307084' }),
        createdAt: new Date('2022-02-22 00:00:00')
    },
    {
        id: 20,
        objectId: '5887',
        version: 2,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Cutardo', cpfCliente: '58872160006', valor: '150000.00' }),
        createdAt: new Date('2025-06-01 10:00:00')
    },
    {
        id: 21,
        objectId: '7617',
        version: 1,
        eventType: 'criado',
        payload: JSON.stringify({ nome: 'Coândrya', cpfCliente: '76179646090', cpfGerente: '64065268052' }),
        createdAt: new Date('2025-01-01 00:00:00')
    },
    {
        id: 22,
        objectId: '7617',
        version: 2,
        eventType: 'deposito',
        payload: JSON.stringify({ nome: 'Coândrya', cpfCliente: '76179646090', valor: '1500.00' }),
        createdAt: new Date('2025-07-01 10:00:00')
    }
];

export const addresses = [
    {cep: '82720-140', street: 'R. Jorn. Ali Chehayde Bark', number: 421, city: 'Curitba', addionalInfo: '', state: 'Paraná'},
    {cep: '80730-090', street: 'R. Ferdinando Darif', number: 203, city: 'Curitba', addionalInfo: '', state: 'Paraná'},
    {cep: '82010-060', street: 'R. Luiz Sieracki', number: 537, city: 'Curitba', addionalInfo: '', state: 'Paraná'},
    {cep: '82590-300', street: 'R. Rockefeller', number: 300, city: 'Curitba', addionalInfo: '', state: 'Paraná'},
    {cep: '82010-280', street: 'R. Simão Lissa', number: 71, city: 'Curitba', addionalInfo: '', state: 'Paraná'},
]

export const clients: Client[] = [
    {cpf: '12912861012', email: 'cli1@bantads.com.br', id: 1, name: 'Catharyna', salary: 10000.00, address: addresses[0]!, deleted: false }, 
    {cpf: '09506382000', email: 'cli2@bantads.com.br', id: 2, name: 'Cleuddônio', salary: 20000.00, address: addresses[1]!, deleted: false }, 
    {cpf: '85733854057', email: 'cli3@bantads.com.br', id: 3, name: 'Catianna', salary: 3000.00, address: addresses[2]!, deleted: false }, 
    {cpf: '58872160006', email: 'cli4@bantads.com.br', id: 4, name: 'Cutardo', salary: 500.00, address: addresses[3]!, deleted: false }, 
    {cpf: '76179646090', email: 'cli5@bantads.com.br', id: 5, name: 'Coândrya', salary: 1500.00, address: addresses[4]!, deleted: false }, 
]

export const requests: Request[] = [
  {
    id: 1,
    client: clients[0]!,
    status: 'aprovado',
    approvedAt: new Date('2000-01-01 00:00:00'),
  },
  {
    id: 2,
    client: clients[1]!,
    status: 'aprovado',
    approvedAt:  new Date('1990-10-10 00:00:00'),
  },
  {
    id: 3,
    client: clients[2]!,
    status: 'aprovado',
    approvedAt: new Date('2012-12-12 00:00:00'),
  },
  {
    id: 4,
    client: clients[3]!,
    status: 'aprovado',
    approvedAt: new Date('2022-02-22 00:00:00'),
  },
  {
    id: 5,
    client: clients[4]!,
    status: 'aprovado',
    approvedAt: new Date('2025-01-01 00:00:00'),
  },
  {
    id: 6,
    client: {
      name: 'Beatriz Santos',
      cpf: '11122233344',
      email: 'beatriz@example.com',
      id: Number(new Date().toString()),
      salary: 3200,
      address: { street: 'Rua Itupava', number: 1500, city: 'Curitiba', state: 'PR', cep: '80530-000' },
      deleted: false
    },
    status: 'rejeitado',
    rejectReason: 'CPF com restrição no sistema',
    approvedAt: new Date('2025-09-17T15:45:00'),
  },
]

export const managers: Manager[] = [
    {name: 'Geniéve', cpf: '98574307084', email: 'ger1@bantads.com.br', phone: '(41)92194-2818', isActive: true, id: 1},
    {name: 'Godophredo', cpf: '64065268052', email: 'ger2@bantads.com.br', phone: '(41)96414-7595', isActive: true, id: 2},
    {name: 'Gyândula', cpf: '23862179060', email: 'ger3@bantads.com.br', phone: '(41)98094-2121', isActive: true, id: 3},
    {name: 'Gadamântio', cpf: '40501740066', email: 'ger4@bantads.com.br', phone: '(41)93592-7764', isActive: true, id: 4}
]