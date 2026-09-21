import type { AccountHistory } from "@/models/AccountHistory";
import { accountHistory } from "./seed";

const LS_KEY = 'history';

export type TransactionType = 'deposito' | 'saque' | 'transferencia';

export interface AccountHistoryDTO {
    accountNumber: string;
    type: TransactionType;
    originClientCpf: string;
    originClientName: string;
    destinationClientCpf: string | null;
    destinationClientName: string | null;
    amount: number;
    managerCpf?: string | null;
    managerName?: string | null;
}

export interface ExtractDTO {
    saldoAbertura: string;
    movimentacoes: AccountHistory[];
}

export class AccountHistoryService {

    listAll(): AccountHistory[] {
        const history = localStorage[LS_KEY];
        if (!history) {
            localStorage.setItem(LS_KEY, JSON.stringify(accountHistory))
            return [...accountHistory]
        }
        return history ? JSON.parse(history) : [];
    }

    private persist(history: AccountHistory[]): void {
        localStorage[LS_KEY] = JSON.stringify(history);
    }

    insert(accountHistory: AccountHistory): void {
        const history = this.listAll();
        accountHistory.id = new Date().getTime();
        history.push(accountHistory);
        this.persist(history);
    }

    findById(id: number): AccountHistory | undefined {
        return this.listAll().find(h => h.id === id);
    }

    findByClient(clientCpf: string): AccountHistory[] {
        return this.listAll().filter(h => h.originClientCpf === clientCpf);
    }

    findByAccountNumber(accountNumber: string): AccountHistory[] {
        return this.listAll().filter(h => h.accountNumber === accountNumber);
    }

    createAcountHistory(dto: AccountHistoryDTO): AccountHistory {
        const accountHistory: AccountHistory = {
            id: 0,
            accountNumber: dto.accountNumber,
            type: dto.type,
            originClientCpf: dto.originClientCpf,
            originClientName: dto.originClientName,
            destinationClientCpf: dto.destinationClientCpf,
            destinationClientName: dto.destinationClientName,
            amount: dto.amount,
            createdAt: new Date(),
        } as unknown as AccountHistory;

        this.insert(accountHistory);
        return accountHistory;
    }

    private parseDateTime(dateStr: string | null | undefined, isStart: boolean): Date {
        if (this.isStringNull(dateStr)) {
            const d = new Date();
            if (isStart) {
                d.setDate(d.getDate() - 30);
                d.setHours(0, 0, 0, 0);
            } else {
                d.setHours(23, 59, 59, 999);
            }
            return d;
        }

        const trimmed = dateStr!.trim();

        if (trimmed.length === 10) {
            const d = new Date(`${trimmed}T00:00:00`);
            if (!isStart) d.setHours(23, 59, 59, 999);
            return d;
        }

        if (trimmed.includes(' ') && !trimmed.includes('T')) {
            return new Date(trimmed.replace(' ', 'T'));
        }

        return new Date(trimmed);
    }

    getExtract(
        accountNumber: string,
        start: string | null,
        end: string | null,
        userCPF: string
    ): ExtractDTO {
        const startDate = this.parseDateTime(start, true);
        const endDate = this.parseDateTime(end, false);

        this.checkDate(startDate, endDate);

        const saldoAbertura = this.getInitialBalance(accountNumber, startDate, userCPF);

        const movimentacoes = this.listAll()
            .filter(h =>
                h.accountNumber === accountNumber &&
                new Date(h.createdAt) >= startDate &&
                new Date(h.createdAt) <= endDate
            );

        return {
            saldoAbertura: saldoAbertura.toFixed(2),
            movimentacoes,
        };
    }

    getInitialBalance(accountNumber: string, start: Date, userCPF: string): number {
        const history = this.listAll()
            .filter(h =>
                h.accountNumber === accountNumber &&
                new Date(h.createdAt) < start
            );

        let balance = 0;

        for (const ac of history) {
            const value = Number(ac.amount);

            if (ac.type === 'deposito') {
                balance += value;
            } else if (ac.type === 'saque') {
                balance -= value;
            } else if (ac.type === 'transferencia') {
                if (ac.destinationClientCpf === userCPF) balance += value;
                if (ac.originClientCpf === userCPF) balance -= value;
            }
        }

        return balance;
    }

    checkDate(start: Date, end: Date): void {
        const daysBetween = (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24);

        if (daysBetween > 365) {
            throw new Error('Filtro não pode ter diferença maior que 365 dias');
        }
    }

    isStringNull(str: string | null | undefined): boolean {
        return str == null || str.length === 0;
    }
}


