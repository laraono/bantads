import type { Manager } from '@/models/Manager';
import type { EventService } from './EventService';
import type { AccountData } from '@/models/AccountData';
import { MOCK_USERS } from './authService';
import { managers } from './seed';

const LS_KEY = 'managers'

export class ManagerService {

    constructor(private eventService: EventService) {}

    listAll(): Manager[] {
        const managersList = localStorage[LS_KEY];

        if (!managersList) {
            localStorage.setItem(LS_KEY, JSON.stringify(managers))
        }
      
        const parsedManagers: Manager[] = managersList ? JSON.parse(managersList) : managers;

        return parsedManagers.filter(manager => manager.isActive)
    }

    list(): Manager[] {
        const managersList = localStorage[LS_KEY];

        if (!managersList) {
            localStorage.setItem(LS_KEY, JSON.stringify(managers))
        }
      
        return managersList ? JSON.parse(managersList) : managers;
    }

    findActiveManagerWithLeastClients(excludeManagerCpf?: string): Manager | undefined {
        const activeManagers = this.listAll().filter(m => m.cpf !== excludeManagerCpf);
        if (activeManagers.length === 0) {
            return undefined;
        }

        const accountsDataStr = localStorage.getItem('accounts');
        const accountsData: AccountData[] = accountsDataStr ? JSON.parse(accountsDataStr) : [];

        const clientCountByManager: Record<string, number> = {};
        
        activeManagers.forEach(m => {
            clientCountByManager[m.cpf] = 0;
        });

        accountsData.filter(acc => !acc.deletedAt).forEach(acc => {
            if (clientCountByManager[acc.managerCpf] !== undefined) {
                clientCountByManager[acc.managerCpf] += 1;
            }
        });

        let selectedManager = activeManagers[0];

        if(!selectedManager) {
            throw new Error('Erro no servidor')
        }
        let minCount = clientCountByManager[selectedManager.cpf];

        for (const manager of activeManagers) {
            const count = clientCountByManager[manager.cpf];
            if (count < minCount) {
                minCount = count;
                selectedManager = manager;
            }
        }

        return selectedManager;
    }


    insert(manager: Manager): void {
        const managers = this.list();
        manager.id = new Date().getTime();
        managers.push(manager);
        localStorage[LS_KEY] = JSON.stringify(managers);

        MOCK_USERS[manager.email] = { password: 'tads', role: 'gerente' }
    }

    findById(id: number): Manager | undefined {
        const managers = this.listAll();
        return managers.find(manager => manager.id === id && manager.isActive);
    }

    update(manager: Manager): void {
        const managers = this.list();
        managers.forEach( (obj, index, objs) => {
            if (manager.id === obj.id) {
            objs[index] = manager
        }
        });
        localStorage[LS_KEY] = JSON.stringify(managers);
    }

    remove(id: number): void {
        const managers = this.list();
        const managerToRemove = managers.find(m => m.id === id);

        if (!managerToRemove) {
            throw new Error("Gerente não encontrado.");
        }

        const targetManager = this.findActiveManagerWithLeastClients(managerToRemove.cpf);

        if (!targetManager) {
            throw new Error("Não é possível remover o gerente pois não há outro gerente ativo para receber os clientes.");
        }

        const accountsDataStr = localStorage.getItem('accounts');
        const accountsData: AccountData[] = accountsDataStr ? JSON.parse(accountsDataStr) : [];

        const accountNumbers: string[] = []
        accountsData.forEach(account => {
            if (account.managerCpf === managerToRemove.cpf && !account.deletedAt) {
                account.managerCpf = targetManager.cpf;
                accountNumbers.push(account.accountNumber)
            }
        });

        managers.forEach(m => {
            if (m.id === id) {
                m.isActive = false;
            }
        });
        localStorage.setItem(LS_KEY, JSON.stringify(managers));

        const payload: Record<string, any> = {
            'cpfGerente': targetManager.cpf
        }

        accountNumbers.forEach(accountNumber => {
            this.eventService.updateManager(
                payload,
                accountNumber
            )
        })
    }

    listActiveWithClientCount() {
        const accounts = this.loadAccounts();

        const countByManager = new Map<string, number>();
        for (const account of accounts) {
            if (account.deletedAt != null) continue;
            countByManager.set(
                account.managerCpf,
                (countByManager.get(account.managerCpf) ?? 0) + 1
            );
        }

        const rows = this.listAll().map(m => ({
            name: m.name,
            cpf: m.cpf,
            email: m.email,
            phone: m.phone,
            clientCount: countByManager.get(m.cpf) ?? 0,
        }));

        rows.sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
        return rows;
    }

    private loadAccounts(): AccountData[] {
        const raw = localStorage.getItem('accounts');
        return raw ? JSON.parse(raw) : [];
    }


}