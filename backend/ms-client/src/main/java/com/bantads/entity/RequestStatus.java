package com.bantads.entity;

public enum RequestStatus {
    PENDENTE("PENDENTE"),
    APROVADA("APROVADO"),
    NÃO_APROVADA("NAO_APROVADO");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}