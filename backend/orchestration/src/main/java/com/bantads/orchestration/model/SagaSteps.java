package com.bantads.orchestration.model;

import java.util.List;

import com.bantads.orchestration.config.RabbitMQConfig;

public enum SagaSteps {
    // em ordem de execução de cada fluxo completo
    CRIACAO_CONTA(List.of(
            new StepDefinition(RabbitMQConfig.MS_CLIENTE_CMD, "cliente.aprovar-solicitacao"),
            new StepDefinition(RabbitMQConfig.MS_GERENTE_CMD, "gerente.listar-gerentes"),
            new StepDefinition(RabbitMQConfig.MS_CONTA_CMD, "conta.decidir-gerente"),
            new StepDefinition(RabbitMQConfig.MS_CLIENTE_CMD, "cliente.criar-client"),
            new StepDefinition(RabbitMQConfig.MS_AUTH_CMD, "auth.criar-auth"),
            new StepDefinition(RabbitMQConfig.MS_CONTA_CMD, "conta.criar-conta"),
            new StepDefinition(RabbitMQConfig.MS_EMAIL_CMD, "email.enviar-senha")
    ));

    private final List<StepDefinition> steps;

    SagaSteps(List<StepDefinition> steps) {
        this.steps = steps;
    }

    public List<StepDefinition> getSteps() { return steps; }

    public int getStepsSize() {
        return steps.size();
    }

    public static SagaSteps getByString(String step) {
        return valueOf(step);
    }

}
