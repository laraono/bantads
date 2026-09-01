package com.bantads.dto.event;

import com.bantads.entity.event.EventType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventDTO {

    private Map<String, String> payload;

    private EventType eventType;

}
