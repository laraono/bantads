package com.bantads.entity;

public enum RequestStatus {
    PENDING("PENDENTE"),
    APPROVED("APROVADO"),
    REJECTED("NAO_APROVADO");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}