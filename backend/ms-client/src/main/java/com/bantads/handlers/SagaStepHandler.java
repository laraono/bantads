package com.bantads.handlers;

import java.util.Map;

public interface SagaStepHandler {
    String action();

    Map<String, Object> execute(Map<String, Object> payload);

    default void compensate(Map<String, Object> payload) {
    }

    default void compensate(Map<String, Object> payload, String error) {
        compensate(payload);
    }
}
