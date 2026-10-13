package com.bantads.handlers;

import java.util.Map;

public interface SagaStepHandler {
    String action();

    Map<String, Object> execute(Map<String, Object> payload);

    void compensate(Map<String, Object> payload);

}
