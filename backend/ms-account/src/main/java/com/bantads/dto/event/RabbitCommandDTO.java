package com.bantads.dto.event;

import com.bantads.entity.event.EventType;
import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RabbitCommandDTO {
    private UUID sagaId;

    private EventType type;

    private Date timestamp;

    private Map<String, String> payload;

    private String accountNumber;

}
