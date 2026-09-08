package com.bantads.dto.read;

import com.bantads.entity.event.EventType;
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
public class RabbitQueryDTO implements Serializable {
    private UUID sagaId;

    private EventType type;

    private Date timestamp;

    private Map<String, Object> payload;

    private Status status;

    private String error;
}
