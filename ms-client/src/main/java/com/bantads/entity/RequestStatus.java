package com.bantads.entity;

public enum RequestStatus {
    PENDING("pendente"),
    APPROVED("aprovado"),
    REJECTED("nao_aprovado");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}