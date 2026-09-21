export interface EventPayloadCreated {
    nome: string;
    cpfCliente: string;
    cpfGerente: string;
}

export interface EventPayloadTransaction {
    nome: string;
    cpfCliente: string;
    valor: string;
}

export interface EventPayloadTransfer {
    destino: {
        conta: string;
        nome: string;
        cpfCliente: string;
    };
    origem: {
        nome: string;
        valor: string;
    };
}

export type EventType = 
    | 'criado' 
    | 'deposito' 
    | 'saque' 
    | 'transferencia_origem' 
    | 'transferencia_destino';

export interface Event {
    id: number
    objectId: string;
    version: number;
    eventType: EventType;
    payload: string;
    createdAt: Date;
}
