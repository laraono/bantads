import type { AccountData } from "@/models/AccountData";
import { accountsData } from "./seed";

const LS_KEY = 'account';

export interface AccountsByManager {
    managerCpf: string;
    count: number;
}

export class AccountDataService {

    listAll(): AccountData[] {
        const accounts = localStorage[LS_KEY];
        if (!accounts) {
            localStorage.setItem(LS_KEY, JSON.stringify(accountsData))
            return [...accountsData]
        }
        return accounts ? JSON.parse(accounts) : [];
    }

    private persist(accounts: AccountData[]): void {
        localStorage[LS_KEY] = JSON.stringify(accounts);
    }

    insert(account: AccountData): void {
        const accounts = this.listAll();
        account.id = new Date().getTime();
        accounts.push(account);
        this.persist(accounts);
    }

    findById(id: number): AccountData | undefined {
        return this.listAll().find(a => a.id === id);
    }

    findByClient(clientCpf: string): AccountData | undefined {
        return this.listAll().find(a => a.clientCpf === clientCpf);
    }

    findByManager(managerCpf: string): AccountData[] {
        return this.listAll().filter(a => a.managerCpf === managerCpf);
    }

    findByAccountNumber(accountNumber: string): AccountData | undefined {
        return this.listAll().find(a => a.accountNumber === accountNumber);
    }

    getCPF(accountNumber: string): string {
        const account = this.findByAccountNumber(accountNumber);

        if (!account) {
            throw new Error(`Conta ${accountNumber} não existe`);
        }

        return account.clientCpf;
    }

    getBalance(accountNumber: string): number {
        const account = this.findByAccountNumber(accountNumber);

        if (!account) {
            throw new Error(`Conta ${accountNumber} não existe`);
        }

        return Number(account.balance);
    }

    getAccountsByManager(managerCpf: string): AccountData[] {
        return this.findByManager(managerCpf);
    }

    getAccountsCountByManager(): AccountsByManager[] {
        const counts = new Map<string, number>();

        for (const account of this.listAll()) {
            counts.set(account.managerCpf, (counts.get(account.managerCpf) ?? 0) + 1);
        }

        return Array.from(counts.entries()).map(([managerCpf, count]) => ({
            managerCpf,
            count,
        }));
    }

    getAccountData(accountNumber: string): AccountData {
        const account = this.findByAccountNumber(accountNumber);

        if (!account) {
            throw new Error(`Conta ${accountNumber} não foi encontrada`);
        }

        return account;
    }

    createAccountData(accountData: AccountData): AccountData {
        if (this.findByAccountNumber(accountData.accountNumber)) {
            throw new Error('Conta já existe');
        }

        this.insert(accountData);
        return accountData;
    }

    updateAccountBalance(value: number, accountNumber: string): AccountData {
        const accounts = this.listAll();
        const index = accounts.findIndex(a => a.accountNumber === accountNumber);

        if (index === -1) {
            throw new Error(`Conta ${accountNumber} não existe`);
        }

        if(!accounts[index]) {
            throw new Error('Erro de servidor')
        }

        accounts[index].balance = Number(accounts[index].balance) + value;

        this.persist(accounts);
        return accounts[index];
    }

    updateAccountManager(managerCpf: string, accountNumber: string): AccountData {
        const accounts = this.listAll();
        const index = accounts.findIndex(a => a.accountNumber === accountNumber);

        if (index === -1) {
            throw new Error(`Conta ${accountNumber} não existe`);
        }

        if(!accounts[index]) {
            throw new Error('Erro de servidor')
        }

        accounts[index].managerCpf = managerCpf;
        this.persist(accounts);
        return accounts[index];
    }
}

