package com.bantads.entity.read;

public enum TransactionType {
    DEPOSIT("deposito"),
    WITHDRAW("saque"),
    TRANSFER("transferencia");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}