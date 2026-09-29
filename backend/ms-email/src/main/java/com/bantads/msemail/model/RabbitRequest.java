package com.bantads.msemail.model;

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

    private Map<String, Object> payload;
}
