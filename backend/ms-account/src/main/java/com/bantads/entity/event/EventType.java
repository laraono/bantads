package com.bantads.entity.event;

import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getLabel() {
        return label;
    }
}