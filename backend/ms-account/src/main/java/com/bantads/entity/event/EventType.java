package com.bantads.entity.event;

public enum EventType {
    CREATED("criado"),
    DEPOSIT("deposito"),
    WITHDRAW("saque"),
    ORIGINTRANSFER("transferencia_origem"),
    DESTINATIONTRANSFER("transferencia_destino"),
    UPDATEMANAGER("gerente_alterado");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}