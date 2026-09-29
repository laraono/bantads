package com.bantads.orchestration.model;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStates implements Serializable{
    private UUID sagaId;
    private SagaStatus status;
    private SagaSteps step;
    private int stepIndex;
    private String error;
    private Map<String, Object> payload;

    public boolean isLastStep() {
        return stepIndex == step.getStepsSize() - 1;
    }

    public boolean hasNextStep() {
        return stepIndex < step.getStepsSize() - 1;
    }

    public String currentStep() {
        return step.getSteps().get(stepIndex);
    }

    public String compensationKey(){
        return "saga:comp:" + sagaId + ":" + step.name();
    }
}
