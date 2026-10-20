package com.bantads.orchestration.model;

// Status possíveis para uma saga (não confundir com status de SUCESSO/FALHA de cada step)
public enum SagaStatus {
    EM_ANDAMENTO,
    CONCLUIDO,
    COMPENSACAO, 
    FALHA
}
