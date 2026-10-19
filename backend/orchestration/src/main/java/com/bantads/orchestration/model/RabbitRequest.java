package com.bantads.orchestration.model;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RabbitRequest {
    private UUID sagaId;

    private String type;

    private boolean compensation;

    private int stepIndex;

    private int attempt;

    private Map<String, Object> payload;

    private String status;

    private String error;
}
