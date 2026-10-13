package com.bantads.entity;

public enum RequestStatus {
    PENDENTE("PENDENTE"),
    APROVADA("APROVADA"),
    REJEITADA("NAO_APROVADA");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}