import { AccountDataService } from "./AccountDataService";
import { AccountHistoryService } from "./AccountHistoryService";
import type { AccountData } from "@/models/AccountData";

export type TransactionType = 'deposito' | 'saque' | 'transferencia';

export interface GetAccountDTO {
    numero: string;
    cpfCliente: string;
    saldo: string;
}

export class AccountService {

    constructor(
        private accountDataService: AccountDataService,
        private accountHistoryService: AccountHistoryService
    ) {}

    getCPF(accountNumber: string): string {
        return this.accountDataService.getCPF(accountNumber);
    }

    getBalance(accountNumber: string): number {
        return this.accountDataService.getBalance(accountNumber);
    }

    getAccountsByManager(managerCpf: string): AccountData[] {
        return this.accountDataService.getAccountsByManager(managerCpf);
    }

    getAccountsCountByManager() {
        return this.accountDataService.getAccountsCountByManager();
    }

    getExtract(accountNumber: string, start: string | null, end: string | null, userCPF: string) {
        return this.accountHistoryService.getExtract(accountNumber, start, end, userCPF);
    }

    getAccountData(accountNumber: string): GetAccountDTO {
        const account = this.accountDataService.findByAccountNumber(accountNumber);
        if (!account) throw new Error(`Conta ${accountNumber} não foi encontrada`);

        return {
            numero: account.accountNumber,
            cpfCliente: account.clientCpf,
            saldo: Number(account.balance).toFixed(2),
        };
    }

    createAccountData(params: {
        accountNumber: string;
        clientCpf: string;
        managerCpf: string;
        balance: number;
    }): AccountData {
        const accountData = {
            accountNumber: params.accountNumber,
            clientCpf: params.clientCpf,
            managerCpf: params.managerCpf,
            balance: params.balance,
            createdAt: new Date(),
            deletedAt: null,
        } as unknown as AccountData;

        return this.accountDataService.createAccountData(accountData);
    }

    updateAccountBalance(params: {
        accountNumber: string;
        type: TransactionType;
        amount: number;
        originClientCpf: string;
        originClientName: string;
        destinationClientCpf?: string | null;
        destinationClientName?: string | null;
        managerCpf?: string | null;
        managerName?: string | null;
        destinationAccountNumber?: string | null;
    }): AccountData {
        this.accountHistoryService.createAcountHistory({
            accountNumber: params.accountNumber,
            type: params.type,
            originClientCpf: params.originClientCpf,
            originClientName: params.originClientName,
            destinationClientCpf: params.destinationClientCpf ?? null,
            destinationClientName: params.destinationClientName ?? null,
            amount: params.amount,
            managerCpf: params.managerCpf ?? null,
            managerName: params.managerName ?? null,
        });

        if (params.type === 'transferencia' && params.destinationAccountNumber) {
            this.accountHistoryService.createAcountHistory({
                accountNumber: params.destinationAccountNumber,
                type: 'transferencia',
                originClientCpf: params.originClientCpf,
                originClientName: params.originClientName,
                destinationClientCpf: params.destinationClientCpf ?? null,
                destinationClientName: params.destinationClientName ?? null,
                amount: params.amount,
                managerCpf: '',
                managerName: '',
            });
        }

        return this.accountDataService.updateAccountBalance(params.amount, params.accountNumber);
    }

    applyBalanceDelta(accountNumber: string, delta: number): AccountData {
        return this.accountDataService.updateAccountBalance(delta, accountNumber);
    }

    updateAccountManager(managerCpf: string, accountNumber: string): AccountData {
        return this.accountDataService.updateAccountManager(managerCpf, accountNumber);
    }
}