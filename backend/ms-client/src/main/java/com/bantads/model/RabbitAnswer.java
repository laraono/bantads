package com.bantads.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RabbitAnswer implements Serializable {
    private UUID sagaId;

    private String type;

    private Date timestamp;

    private Map<String, Object> payload;

    private Status status;

    private String error;

    private int attempt;
}
