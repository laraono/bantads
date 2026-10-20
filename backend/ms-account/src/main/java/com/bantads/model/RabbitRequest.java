package com.bantads.model;

import lombok.*;

import java.util.Date;
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
    
    private String stepIndex;

    private int attempt;
    
    private Map<String, Object> payload;

    private String status;

    private String error;

    private Date timestamp;
}
