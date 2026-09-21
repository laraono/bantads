import type { Client } from '@/models/Client'
import type { ManagerService } from './ManagerService';
import type { EventService } from './EventService';
import type { AccountData } from '@/models/AccountData';
import { MOCK_USERS } from './authService';
import type { Manager } from '@/models/Manager';
import { clients } from './seed';

const LS_KEY = 'clients'

export class ClientService {

    constructor(private managerService: ManagerService, private eventService: EventService) {}

    listAll(): Client[] {
        const clientsList = localStorage[LS_KEY];
        
        if (!clientsList) {
            localStorage.setItem(LS_KEY, JSON.stringify(clients))
        }
              
        const parsedClients: Client[] = clientsList ? JSON.parse(clientsList) : clients;

        return parsedClients.filter(client => !client.deleted)
    }

    insert(client: Client): void {
        const clients = this.listAll();
        client.id = new Date().getTime();
        clients.push(client);
        localStorage[LS_KEY] = JSON.stringify(clients);

        MOCK_USERS[client.email] = { password: 'tads', role: 'cliente' }
    }

    findById(id: number): Client | undefined {
        const clients = this.listAll();
        return clients.find(client => client.id === id);
    }

    findByEmail(email: string): Client | undefined {
        const clients = this.listAll();
        return clients.find(client => client.email === email);
    }

    update(client: Client): void {
        const clients = this.listAll();
        clients.forEach( (obj, index, objs) => {
            if (client.id === obj.id) {
            objs[index] = client
        }
        });
        localStorage[LS_KEY] = JSON.stringify(clients);
    }

    remove(id: number): void {
        const clients = this.listAll();
        clients.forEach( (obj) => {
            if (obj.id === id) {
                obj.deleted = true
            }
        });
        localStorage[LS_KEY] = JSON.stringify(clients);
    }

    approveClientRegistration(accountNumber: string) {
        const assignedManager = this.managerService.findActiveManagerWithLeastClients();

        if (!assignedManager) {
            throw new Error("Nenhum gerente ativo disponível para atribuição.");
        }

        const payload: Record<string, any> = {
            'cpfGerente': assignedManager.cpf
        }

        this.eventService.updateManager(
            payload,
            accountNumber
        )
    }

    private loadAccounts(): AccountData[] {
        const raw = localStorage.getItem('accounts');
        return raw ? JSON.parse(raw) : [];
    }

    listAllWithBalance() {
        const accounts = this.loadAccounts();

        const rows = this.listAll().map(client => {
            const account = accounts.find(a =>
                a.clientCpf === client.cpf && a.deletedAt == null
            );

            return {
                cpf: client.cpf,
                name: client.name,
                city: client.address.city,
                state: client.address.state,
                balance: account ? Number(account.balance) : 0,
            };
        });

        rows.sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
        return rows;
    }

    search(cpfQuery: string, nameQuery: string) {
        const cpf = (cpfQuery ?? '').replace(/\D/g, '');
        const name = (nameQuery ?? '').trim().toLowerCase();

        return this.listAllWithBalance().filter(row => {
            const matchCpf = cpf.length === 0 || row.cpf.includes(cpf);
            const matchName = name.length === 0 || row.name.toLowerCase().includes(name);
            return matchCpf && matchName;
        });
    }

    listAllWithDetails() {
        const accounts = this.loadAccounts();
        const managers = this.managerService.listAll();

        const managerByCpf = new Map<string, Manager>(
            managers.map(m => [m.cpf, m])
        );

        const rows = this.listAll().map(client => {
            const account = accounts.find(a =>
                a.clientCpf === client.cpf && a.deletedAt == null
            );

            const managerCpf = account?.managerCpf ?? null;
            const manager = managerCpf ? managerByCpf.get(managerCpf) : undefined;

            return {
                cpf: client.cpf,
                name: client.name,
                email: client.email,
                salary: Number(client.salary),
                accountNumber: account?.accountNumber ?? null,
                balance: account ? Number(account.balance) : 0,
                managerCpf: managerCpf,
                managerName: manager?.name ?? null,
            };
        });

        rows.sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
        return rows;
    }

}