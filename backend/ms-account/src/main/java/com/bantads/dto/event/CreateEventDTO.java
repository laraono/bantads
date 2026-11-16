package com.bantads.dto.event;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventDTO {

    private Map<String, Object> payload;

}
