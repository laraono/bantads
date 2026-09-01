package com.bantads.dto.event;

import com.bantads.entity.event.EventType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDTO {

    private String destinationObjectId;

    private Map<String, String> payload;

    private EventType eventType;

}
