package com.bantads.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestStatus {
    PENDENTE("PENDENTE"),
    APROVADA("APROVADA"),
    REJEITADA("NAO_APROVADA");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}