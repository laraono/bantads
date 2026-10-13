package com.bantads.orchestration.model;

import java.util.List;

import com.bantads.orchestration.config.RabbitMQConfig;

public enum SagaSteps {
    // em ordem de execução de cada fluxo completo
    CRIACAO_CONTA(List.of(
            new StepDefinition(RabbitMQConfig.MS_CLIENTE_CMD, "cliente.aprovar-solicitacao"),
            new StepDefinition(RabbitMQConfig.MS_GERENTE_CMD, "gerente.listar-gerentes"),
            new StepDefinition(RabbitMQConfig.MS_CONTA_CMD, "conta.decidir-gerente"),
            new StepDefinition(RabbitMQConfig.MS_CLIENTE_CMD, "cliente.criar-cliente"),
            new StepDefinition(RabbitMQConfig.MS_AUTH_CMD, "auth.criar-auth"),
            new StepDefinition(RabbitMQConfig.MS_CONTA_CMD, "conta.criar-conta"),
            new StepDefinition(RabbitMQConfig.MS_EMAIL_CMD, "email.enviar-senha")
    )),

    DELETAR_GERENTE(List.of(
            new StepDefinition(RabbitMQConfig.MS_GERENTE_CMD, "gerente.deletar-gerente"),
            new StepDefinition(RabbitMQConfig.MS_CONTA_CMD, "conta.deletar-gerente"),
            new StepDefinition(RabbitMQConfig.MS_CLIENTE_CMD, "cliente.obter-email-clientes"),
            new StepDefinition(RabbitMQConfig.MS_EMAIL_CMD, "email.enviar-avisar-delecao"),
            new StepDefinition(RabbitMQConfig.MS_AUTH_CMD, "auth.deletar-gerente")
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
