package com.bantads.orchestration.model;

import java.util.List;

import com.bantads.orchestration.config.RabbitMQConfig;

public enum SagaSteps {
    // em ordem de execução de cada fluxo completo
    CRIACAO_CONTA(List.of(
        RabbitMQConfig.MS_CLIENTE_CMD,
        RabbitMQConfig.MS_GERENTE_CMD,
        RabbitMQConfig.MS_CONTA_CMD,
        RabbitMQConfig.MS_CLIENTE_CMD,
        RabbitMQConfig.MS_AUTH_CMD,
        RabbitMQConfig.MS_CONTA_CMD,
        RabbitMQConfig.MS_EMAIL_CMD
    ));
    
    List<String> steps;

    private SagaSteps(List<String> steps) {
        this.steps = steps;
    }

    public List<String> getSteps() {
        return steps;
    }

    public int getStepsSize() {
        return steps.size();
    }

    public static SagaSteps getByString(String step) {
        return valueOf(step);
    }

}
