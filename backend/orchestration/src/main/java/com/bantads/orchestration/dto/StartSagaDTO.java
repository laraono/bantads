package com.bantads.orchestration.dto;

import com.bantads.orchestration.model.SagaSteps;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartSagaDTO {
    private SagaSteps step;
    private Map<String, Object> payload;
}
