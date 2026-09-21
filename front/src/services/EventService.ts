import type { Event } from "@/models/Event";
import type { AccountService } from "./AccountService";
import { events } from "./seed";

const LS_KEY = 'events';

export class EventService {

    constructor(
        private accountService: AccountService
    ) {}

    listAll(): Event[] {
        const eventsList = localStorage[LS_KEY];
        if (!eventsList) {
            localStorage.setItem(LS_KEY, JSON.stringify(events))
            return [...events]
        }
        return eventsList ? JSON.parse(eventsList) : [];
    }

    insert(event: Event): void {
        const events = this.listAll();
        event.id = new Date().getTime();
        events.push(event);
        localStorage[LS_KEY] = JSON.stringify(events);
    }

    findByAccountNumber(accountNumber: string): Event[] {
        const events = this.listAll();
        return events.filter(event => event.objectId === accountNumber);
    }

    getVersion(objectId: string): number {
        const events = this.findByAccountNumber(objectId)
            .sort((a, b) => a.version - b.version);

        if (events.length === 0) return 1;

        const index = events.length - 1

        if(!events[index]) {
            throw new Error('Erro no servidor')
        }

        return events[index].version + 1;
    }

    doesAccountExists(objectId: string): boolean {
        return this.findByAccountNumber(objectId)
            .some(e => e.eventType === 'criado');
    }

    checkUserCPF(userCPF: string, objectId: string): void {
        const created = this.findByAccountNumber(objectId)
            .find(e => e.eventType === 'criado');

        if (!created) {
            throw new Error('Conta não existe');
        }

        const payload = typeof created.payload === 'string'
            ? JSON.parse(created.payload)
            : created.payload;

        if (payload.cpfCliente !== userCPF) {
            throw new Error('Não é a sua conta');
        }
    }

    getBalance(objectId: string): number {
        const events = this.findByAccountNumber(objectId)
            .sort((a, b) => a.version - b.version);

        let balance = 0;

        for (const event of events) {
            const payload = typeof event.payload === 'string'
                ? JSON.parse(event.payload)
                : event.payload;

            if (event.eventType === 'deposito') {
                balance += Number(payload.valor);
            } else if (event.eventType === 'saque') {
                balance -= Number(payload.valor);
            } else if (event.eventType === 'transferencia_destino') {
                balance += Number(payload.origem?.valor ?? payload.valor);
            } else if (event.eventType === 'transferencia_origem') {
                balance -= Number(payload.origem?.valor ?? payload.valor);
            }
        }

        return balance;
    }

    checkBalance(objectId: string, payload: Record<string, any>): void {
        const balance = this.getBalance(objectId);
        const value = Number(payload.valor);

        if (value > balance) {
            throw new Error('Sem saldo suficiente');
        }
    }

    createObjectId(): string {
        const maxTries = 100;
        for (let i = 0; i < maxTries; i++) {
            const accountNumber = Math.floor(Math.random() * 10000);
            const objectId = String(accountNumber).padStart(4, '0');
            if (!this.doesAccountExists(objectId)) {
                return objectId;
            }
        }
        throw new Error('Não foi possível gerar um número de conta único');
    }

    createAccount(payload: Record<string, any>): Event {
        const objectId = this.createObjectId();

        const event: Event = {
            id: 0,
            objectId,
            version: this.getVersion(objectId),
            eventType: 'criado',
            payload: JSON.stringify(payload),
            createdAt: new Date(),
        } as unknown as Event;

        this.insert(event);

        this.accountService.createAccountData({
            accountNumber: objectId,
            clientCpf: String(payload.cpfCliente),
            managerCpf: String(payload.cpfGerente),
            balance: Number(payload.valor ?? 0),
        });

        return event;
    }

    deposit(userCPF: string, payload: Record<string, any>, objectId: string): void {
        if (!this.doesAccountExists(objectId)) throw new Error('Conta não existe');
        this.checkUserCPF(userCPF, objectId);

        const event: Event = {
            id: 0,
            objectId,
            version: this.getVersion(objectId),
            eventType: 'deposito',
            payload: JSON.stringify(payload),
            createdAt: new Date(),
        } as unknown as Event;

        this.insert(event);

        this.accountService.updateAccountBalance({
            accountNumber: objectId,
            type: 'deposito',
            amount: Number(payload.valor),
            originClientCpf: userCPF,
            originClientName: String(payload.nome ?? ''),
            managerCpf: payload.cpfGerente != null ? String(payload.cpfGerente) : null,
            managerName: payload.nomeGerente != null ? String(payload.nomeGerente) : null,
        });
    }

    withdraw(userCPF: string, payload: Record<string, any>, objectId: string): void {
        if (!this.doesAccountExists(objectId)) throw new Error('Conta não existe');
        this.checkUserCPF(userCPF, objectId);
        this.checkBalance(objectId, payload);

        const event: Event = {
            id: 0,
            objectId,
            version: this.getVersion(objectId),
            eventType: 'saque',
            payload: JSON.stringify(payload),
            createdAt: new Date(),
        } as unknown as Event;

        this.insert(event);

        this.accountService.updateAccountBalance({
            accountNumber: objectId,
            type: 'saque',
            amount: -Number(payload.valor),
            originClientCpf: userCPF,
            originClientName: String(payload.nome ?? ''),
            managerCpf: payload.cpfGerente != null ? String(payload.cpfGerente) : null,
            managerName: payload.nomeGerente != null ? String(payload.nomeGerente) : null,
        });
    }

    updateManager(payload: Record<string, any>, objectId: string): void {
        if (!this.doesAccountExists(objectId)) throw new Error('Conta não existe');

        const event: Event = {
            id: 0,
            objectId,
            version: this.getVersion(objectId),
            eventType: 'atualizacao_gerente',
            payload: JSON.stringify(payload),
            createdAt: new Date(),
        } as unknown as Event;

        this.insert(event);

        this.accountService.updateAccountManager(String(payload.cpfGerente), objectId);
    }

    transfer(
        userCPF: string,
        originObjectId: string,
        payload: Record<string, any>
    ): { nome: string } {
        const destination = payload.destino;
        const origin = payload.origem;

        const destinationCPF = String(destination.cpf);
        const destinationObjectId = String(destination.conta);

        if (userCPF === destinationCPF) {
            throw new Error('Não pode transferir para sua própria conta');
        }
        if (!this.doesAccountExists(originObjectId) || !this.doesAccountExists(destinationObjectId)) {
            throw new Error('Conta não existe');
        }

        this.checkUserCPF(userCPF, originObjectId);
        this.checkBalance(originObjectId, origin);

        const originTransfer: Event = {
            id: 0,
            objectId: originObjectId,
            version: this.getVersion(originObjectId),
            eventType: 'transferencia_origem',
            payload: JSON.stringify(payload),
            createdAt: new Date(),
        } as unknown as Event;
        this.insert(originTransfer);

        const destinationTransfer: Event = {
            id: 0,
            objectId: destinationObjectId,
            version: this.getVersion(destinationObjectId),
            eventType: 'transferencia_destino',
            payload: JSON.stringify(payload),
            createdAt: new Date(),
        } as unknown as Event;
        this.insert(destinationTransfer);

        const amount = Number(origin.valor);

        this.accountService.updateAccountBalance({
            accountNumber: originObjectId,
            type: 'transferencia',
            amount: -amount,
            originClientCpf: userCPF,
            originClientName: String(origin.nome ?? ''),
            destinationClientCpf: destinationCPF,
            destinationClientName: String(destination.nome ?? ''),
            destinationAccountNumber: null, 
        });

        this.accountService.updateAccountBalance({
            accountNumber: destinationObjectId,
            type: 'transferencia',
            amount: amount,
            originClientCpf: userCPF,
            originClientName: String(origin.nome ?? ''),
            destinationClientCpf: destinationCPF,
            destinationClientName: String(destination.nome ?? ''),
            destinationAccountNumber: null,
        });

        return { nome: String(destination.nome) };
    }
}
